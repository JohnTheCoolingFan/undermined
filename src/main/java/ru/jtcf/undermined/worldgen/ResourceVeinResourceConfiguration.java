package ru.jtcf.undermined.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ResourceVeinResourceConfiguration extends ForgeRegistryEntry<ResourceVeinResourceConfiguration> {
    private static final float TAU = Mth.TWO_PI;

    public static final Codec<ResourceVeinResourceConfiguration> CODEC =
            RecordCodecBuilder.<ResourceVeinResourceConfiguration>mapCodec((builder) -> builder.group(
                                    ResourceLocation.CODEC
                                            .fieldOf("resource")
                                            .forGetter(ResourceVeinResourceConfiguration::resource),
                                    Codec.floatRange(Math.ulp(1.0f), 4096.0f)
                                            .fieldOf("radius")
                                            .forGetter(ResourceVeinResourceConfiguration::radius),
                                    Codec.floatRange(0.0f, 1.0f)
                                            .optionalFieldOf("radius_deviation", 1.0f)
                                            .forGetter(ResourceVeinResourceConfiguration::radiusDeviation),
                                    Codec.floatRange(1.0f, 4096.0f)
                                            .optionalFieldOf("falloff_rate", 1.0f)
                                            .forGetter(ResourceVeinResourceConfiguration::falloffRate),
                                    Codec.floatRange(1.0f, 16.0f)
                                            .optionalFieldOf("falloff_rate_deviation", 1.0f)
                                            .forGetter(ResourceVeinResourceConfiguration::falloffRateDeviation),
                                    Codec.intRange(1, 8192)
                                            .fieldOf("base_value")
                                            .forGetter(ResourceVeinResourceConfiguration::baseValue),
                                    Codec.intRange(0, 4096)
                                            .optionalFieldOf("value_deviation", 0)
                                            .forGetter(ResourceVeinResourceConfiguration::valueDeviation),
                                    Codec.floatRange(0.0f, 1.0f)
                                            .optionalFieldOf("angle_influence", 1.0f)
                                            .forGetter(ResourceVeinResourceConfiguration::angleInfluence),
                                    Weight.CODEC
                                            .fieldOf("weight")
                                            .forGetter(ResourceVeinResourceConfiguration::weight),
                                    RandomSpreadType.CODEC
                                            .optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                            .forGetter(ResourceVeinResourceConfiguration::spreadType))
                            .apply(builder, ResourceVeinResourceConfiguration::new))
                    .codec();

    @Nullable
    public ResourceVein getVein(WorldgenRandom random, int offsetLimit, ChunkPos chunk, int regionX, int regionZ, int spacing) {
        // Get offsets on X and Z from the region coordinates
        int offsetX = this.spreadType().evaluate(random, offsetLimit);
        int offsetZ = this.spreadType().evaluate(random, offsetLimit);
        // The chunk that is determined to be "feature chunk", in context of ore veins, this is the center of an ore
        // vein
        ChunkPos featureChunk = new ChunkPos(regionX * spacing + offsetX, regionZ * spacing + offsetZ);
        // Relative position of a target chunk to center chunk
        Vec2 chunkOffset = new Vec2(chunk.x, chunk.z).add(new Vec2(featureChunk.x, featureChunk.z).negated());

        float distance = chunkOffset.length();

        float radiusRandomOffset;
        if (this.radiusDeviation > 0.0f) {
            radiusRandomOffset = 1.0f + random.nextFloat(this.radiusDeviation);
        } else {
            radiusRandomOffset = 1.0f;
        }

        float radiusOffset = this.radius() * radiusRandomOffset;

        if (distance > radiusOffset) {
            return null;
        }
        float raw_dot = Vec2.UNIT_X.dot(chunkOffset);
        if (Float.isNaN(raw_dot)) {
            raw_dot = 0.0f;
        }
        float scaled_dot = raw_dot / distance;
        float angle = (float) Math.acos(scaled_dot);
        // The offset is below X axis, flip the angle (so that it extends beyond 180 degrees
        angle = chunkOffset.y < 0.0 ? TAU - angle : angle;

        //float angleOffset = this.getAngleOffset(seed, regionX, regionZ, angle);
        float angleOffset = this.getContinuousAngleOffset(random, angle) * (1.0f + this.angleInfluence());

        if (Float.isNaN(angleOffset)) {
            angleOffset = 1.0f;
        }

        int baseValueDeviation;
        if (this.valueDeviation > 0) {
            baseValueDeviation = random.nextInt(this.valueDeviation);
        } else {
            baseValueDeviation = 0;
        }

        int baseValueWithDeviation = this.baseValue() + baseValueDeviation;
        float valueScaledByDistance = baseValueWithDeviation * Math.max(0.0f,
                1.0f - (distance / this.falloffRate()) / this.radius);
        int value = (int) (valueScaledByDistance * angleOffset);
        return new ResourceVein(value, resource(), featureChunk == chunk);
    }

    private static final int OFFSETS_DIVISIONS = 36;
    private static final float OFFSET_RANGE = TAU / OFFSETS_DIVISIONS;
    private final ResourceLocation resource;
    private final float radius;
    private final float radiusDeviation;
    private final float falloffRate;
    private final float falloffRateDeviation;
    private final int baseValue;
    private final int valueDeviation;
    private final float angleInfluence;
    private final Weight weight;
    private final RandomSpreadType spreadType;

    /**
     * @param radius          controls cutoff radius
     * @param radiusDeviation positive radius deviation limit, scale
     * @param falloffRate     rate at which value decreases from center, bigger value means slower falloff
     * @param baseValue       base (minimum) amount of ore in the center chunk
     * @param valueDeviation  positive deviation limit for center chunk value
     * @param angleInfluence  coefficient of how strongly direction angle influences the value of a chunk
     * @param spreadType      ore vein spreading type
     */
    public ResourceVeinResourceConfiguration(ResourceLocation resource, float radius, float radiusDeviation,
                                             float falloffRate, float falloffRateDeviation, int baseValue,
                                             int valueDeviation, float angleInfluence, Weight weight,
                                             RandomSpreadType spreadType) {
        this.resource = resource;
        this.radius = radius;
        this.radiusDeviation = radiusDeviation;
        this.falloffRate = falloffRate;
        this.falloffRateDeviation = falloffRateDeviation;
        this.baseValue = baseValue;
        this.valueDeviation = valueDeviation;
        this.angleInfluence = angleInfluence;
        this.weight = weight;
        this.spreadType = spreadType;
    }

    // I'm not sure whether this works correctly or not, need to be tested
    private float getContinuousAngleOffset(WorldgenRandom worldgenRandom, float angle) {
        List<Float> offsets = new ArrayList<>();

        for (int i = 0; i < OFFSETS_DIVISIONS; i++) {
            offsets.add(worldgenRandom.nextFloat(-1.0f, 1.0f));
        }

        int offsetIndexLower = (int) Math.floor(OFFSETS_DIVISIONS * (angle / TAU));
        int offsetIndexUpper = offsetIndexLower < OFFSETS_DIVISIONS - 1 ? offsetIndexLower + 1 : 0;
        float remainder = angle % offsetIndexLower;
        float baseline = offsets.get(offsetIndexLower);
        return baseline + (offsets.get(offsetIndexUpper) - baseline) * OFFSET_RANGE / remainder;
    }

    public ResourceLocation resource() {
        return resource;
    }

    public float radius() {
        return radius;
    }

    public float radiusDeviation() {
        return radiusDeviation;
    }

    public float falloffRate() {
        return falloffRate;
    }

    public float falloffRateDeviation() {
        return falloffRateDeviation;
    }

    public int baseValue() {
        return baseValue;
    }

    public int valueDeviation() {
        return valueDeviation;
    }

    public float angleInfluence() {
        return angleInfluence;
    }

    public Weight weight() {
        return weight;
    }

    public RandomSpreadType spreadType() {
        return spreadType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ResourceVeinResourceConfiguration) obj;
        return Objects.equals(this.resource, that.resource) &&
                Float.floatToIntBits(this.radius) == Float.floatToIntBits(that.radius) &&
                Float.floatToIntBits(this.radiusDeviation) == Float.floatToIntBits(that.radiusDeviation) &&
                Float.floatToIntBits(this.falloffRate) == Float.floatToIntBits(that.falloffRate) &&
                Float.floatToIntBits(this.falloffRateDeviation) == Float.floatToIntBits(that.falloffRateDeviation) &&
                this.baseValue == that.baseValue &&
                this.valueDeviation == that.valueDeviation &&
                Float.floatToIntBits(this.angleInfluence) == Float.floatToIntBits(that.angleInfluence) &&
                Objects.equals(this.weight, that.weight) &&
                Objects.equals(this.spreadType, that.spreadType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, radius, radiusDeviation, falloffRate, falloffRateDeviation, baseValue, valueDeviation, angleInfluence, weight, spreadType);
    }

    @Override
    public String toString() {
        return "ResourceVeinResourceConfiguration[" +
                "resource=" + resource + ", " +
                "radius=" + radius + ", " +
                "radiusDeviation=" + radiusDeviation + ", " +
                "falloffRate=" + falloffRate + ", " +
                "falloffRateDeviation=" + falloffRateDeviation + ", " +
                "baseValue=" + baseValue + ", " +
                "valueDeviation=" + valueDeviation + ", " +
                "angleInfluence=" + angleInfluence + ", " +
                "weight=" + weight + ", " +
                "spreadType=" + spreadType + ']';
    }

}