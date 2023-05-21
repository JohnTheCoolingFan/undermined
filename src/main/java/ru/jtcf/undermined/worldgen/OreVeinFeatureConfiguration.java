package ru.jtcf.undermined.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * @param spacing         spacing between region origins, defines a "grid" size
 * @param separation      minimum distance between features
 * @param radius          controls cutoff radius
 * @param radiusDeviation positive radius deviation limit, scale
 * @param falloffRate     rate at which value decreases from center, bigger value means slower falloff
 * @param baseValue       base (minimum) amount of ore in the center chunk
 * @param valueDeviation  positive deviation limit for center chunk value
 * @param angleInfluence  coefficient of how strongly direction angle influences the value of a chunk
 * @param resource        the resource this ore spread is for
 * @param spreadType      ore vein spreading type
 * @param salt            randomness salt
 */
public record OreVeinFeatureConfiguration(int spacing, int separation, float radius, float radiusDeviation,
                                          float falloffRate, float falloffRateDeviation, int baseValue,
                                          int valueDeviation, float angleInfluence, ResourceLocation resource,
                                          RandomSpreadType spreadType, int salt) implements FeatureConfiguration {
    private static final float TAU = (float) Math.PI * 2.0f;

    public static final Codec<OreVeinFeatureConfiguration> CODEC =
            RecordCodecBuilder.<OreVeinFeatureConfiguration>mapCodec((builder) -> builder.group(
                                    Codec.intRange(0, 4096)
                                            .fieldOf("spacing")
                                            .forGetter(OreVeinFeatureConfiguration::spacing),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("separation")
                                            .forGetter(OreVeinFeatureConfiguration::separation),
                                    Codec.floatRange(Math.ulp(1.0f), 4096.0f)
                                            .fieldOf("radius")
                                            .forGetter(OreVeinFeatureConfiguration::radius),
                                    Codec.floatRange(0.0f, 1.0f)
                                            .fieldOf("radius_deviation")
                                            .forGetter(OreVeinFeatureConfiguration::radiusDeviation),
                                    Codec.floatRange(1.0f, 4096.0f)
                                            .optionalFieldOf("falloff_rate", 1.0f)
                                            .forGetter(OreVeinFeatureConfiguration::falloffRate),
                                    Codec.floatRange(1.0f, 16.0f)
                                            .optionalFieldOf("falloff_rate_deviation", 1.0f)
                                            .forGetter(OreVeinFeatureConfiguration::falloffRateDeviation),
                                    Codec.intRange(1, 8192)
                                            .fieldOf("base_value")
                                            .forGetter(OreVeinFeatureConfiguration::baseValue),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("value_deviation")
                                            .forGetter(OreVeinFeatureConfiguration::valueDeviation),
                                    Codec.floatRange(0.0f, 1.0f)
                                            .optionalFieldOf("angle_influence", 1.0f)
                                            .forGetter(OreVeinFeatureConfiguration::angleInfluence),
                                    ResourceLocation.CODEC
                                            .fieldOf("resource")
                                            .forGetter(OreVeinFeatureConfiguration::resource),
                                    RandomSpreadType.CODEC
                                            .optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                            .forGetter(OreVeinFeatureConfiguration::spreadType),
                                    ExtraCodecs.NON_NEGATIVE_INT
                                            .fieldOf("salt")
                                            .forGetter(OreVeinFeatureConfiguration::salt))
                            .apply(builder, OreVeinFeatureConfiguration::new))
                    .flatXmap((spread) -> spread.spacing <= spread.separation ?
                                    DataResult.error("Spacing has to be larger than separation") :
                                    DataResult.success(spread),
                            DataResult::success).codec();

    public int getValuesForChunk(long seed, int chunkX, int chunkZ) {
        // Get spacing and separation as variables
        int spacing = this.spacing();
        int separation = this.separation();
        // Compute region coordinates, defined by spacing size
        int regionX = Math.floorDiv(chunkX, spacing);
        int regionZ = Math.floorDiv(chunkZ, spacing);
        // Initialize worldgen RNG
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(seed, regionX, regionZ, this.salt());
        // A limit to the random offset from region coordinates
        int offsetLimit = spacing - separation;
        // Get offsets on X and Z from the region coordinates
        int offsetX = this.spreadType().evaluate(worldgenRandom, offsetLimit);
        int offsetZ = this.spreadType().evaluate(worldgenRandom, offsetLimit);
        // The chunk that is determined to be "feature chunk", in context of ore veins, this is the center of an ore
        // vein
        ChunkPos featureChunk = new ChunkPos(regionX * spacing + offsetX, regionZ * spacing + offsetZ);
        // Relative position of a target chunk to center chunk
        Vec2 chunkOffset = new Vec2(chunkX, chunkZ).add(new Vec2(featureChunk.x, featureChunk.z).negated());

        float distance = chunkOffset.length();
        float radiusOffset = this.radius() * (1.0f + worldgenRandom.nextFloat(this.radiusDeviation()));
        if (distance > radiusOffset) {
            return 0;
        }
        float scaled_dot = Vec2.UNIT_X.dot(chunkOffset) / distance;
        float angle = (float) Math.acos(scaled_dot);
        // The offset is below X axis, flip the angle (so that it extends beyond 180 degrees
        angle = chunkOffset.y < 0.0 ? TAU - angle : angle;

        //float angleOffset = this.getAngleOffset(seed, regionX, regionZ, angle);
        float angleOffset = this.getContinuousAngleOffset(worldgenRandom, angle) * this.angleInfluence();

        int baseValueWithDeviation = this.baseValue() + worldgenRandom.nextInt(this.valueDeviation());
        float valueScaledByDistance = baseValueWithDeviation * Math.max(0.0f, 1.0f - distance / this.falloffRate());
        return (int) (valueScaledByDistance * angleOffset);
    }

    private static final int OFFSETS_DIVISIONS = 36;
    private static final float OFFSET_RANGE = TAU / OFFSETS_DIVISIONS;

    // I'm not sure whether this works correctly or not, need to be tested
    private float getContinuousAngleOffset(WorldgenRandom worldgenRandom, float angle) {
        List<Float> offsets = new ArrayList<>();

        for (int i = 0; i < OFFSETS_DIVISIONS; i++) {
            offsets.add(worldgenRandom.nextFloat(-1.0f, 1.0f));
        }

        int offsetIndexLower = (int) Math.floor(OFFSETS_DIVISIONS * (TAU / angle));
        int offsetIndexUpper = offsetIndexLower < OFFSETS_DIVISIONS - 1 ? offsetIndexLower + 1 : 0;
        float remainder = angle % offsetIndexLower;
        float baseline = offsets.get(offsetIndexLower);
        return baseline + (offsets.get(offsetIndexUpper) - baseline) * OFFSET_RANGE / remainder;
    }

}
