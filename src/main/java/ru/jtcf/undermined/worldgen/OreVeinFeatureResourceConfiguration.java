package ru.jtcf.undermined.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * @param radius          controls cutoff radius
 * @param radiusDeviation positive radius deviation limit, scale
 * @param falloffRate     rate at which value decreases from center, bigger value means slower falloff
 * @param baseValue       base (minimum) amount of ore in the center chunk
 * @param valueDeviation  positive deviation limit for center chunk value
 * @param angleInfluence  coefficient of how strongly direction angle influences the value of a chunk
 * @param spreadType      ore vein spreading type
 */
public record OreVeinFeatureResourceConfiguration(float radius, float radiusDeviation, float falloffRate,
                                                  float falloffRateDeviation, int baseValue, int valueDeviation,
                                                  float angleInfluence, Weight weight,
                                                  RandomSpreadType spreadType) implements FeatureConfiguration {
    private static final float TAU = (float) Math.PI * 2.0f;

    public static final Codec<OreVeinFeatureResourceConfiguration> CODEC =
            RecordCodecBuilder.<OreVeinFeatureResourceConfiguration>mapCodec((builder) -> builder.group(
                                    Codec.floatRange(Math.ulp(1.0f), 4096.0f)
                                            .fieldOf("radius")
                                            .forGetter(OreVeinFeatureResourceConfiguration::radius),
                                    Codec.floatRange(0.0f, 1.0f)
                                            .fieldOf("radius_deviation")
                                            .forGetter(OreVeinFeatureResourceConfiguration::radiusDeviation),
                                    Codec.floatRange(1.0f, 4096.0f)
                                            .optionalFieldOf("falloff_rate", 1.0f)
                                            .forGetter(OreVeinFeatureResourceConfiguration::falloffRate),
                                    Codec.floatRange(1.0f, 16.0f)
                                            .optionalFieldOf("falloff_rate_deviation", 1.0f)
                                            .forGetter(OreVeinFeatureResourceConfiguration::falloffRateDeviation),
                                    Codec.intRange(1, 8192)
                                            .fieldOf("base_value")
                                            .forGetter(OreVeinFeatureResourceConfiguration::baseValue),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("value_deviation")
                                            .forGetter(OreVeinFeatureResourceConfiguration::valueDeviation),
                                    Codec.floatRange(0.0f, 1.0f)
                                            .optionalFieldOf("angle_influence", 1.0f)
                                            .forGetter(OreVeinFeatureResourceConfiguration::angleInfluence),
                                    Weight.CODEC
                                            .fieldOf("weight")
                                            .forGetter(OreVeinFeatureResourceConfiguration::weight),
                                    RandomSpreadType.CODEC
                                            .optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                            .forGetter(OreVeinFeatureResourceConfiguration::spreadType))
                            .apply(builder, OreVeinFeatureResourceConfiguration::new))
                    .codec();

    public int getValue(WorldgenRandom random, int offsetLimit, ChunkPos chunk, int regionX, int regionZ, int spacing) {
        // Get offsets on X and Z from the region coordinates
        int offsetX = this.spreadType().evaluate(random, offsetLimit);
        int offsetZ = this.spreadType().evaluate(random, offsetLimit);
        // The chunk that is determined to be "feature chunk", in context of ore veins, this is the center of an ore
        // vein
        ChunkPos featureChunk = new ChunkPos(regionX * spacing + offsetX, regionZ * spacing + offsetZ);
        // Relative position of a target chunk to center chunk
        Vec2 chunkOffset = new Vec2(chunk.x, chunk.z).add(new Vec2(featureChunk.x, featureChunk.z).negated());

        float distance = chunkOffset.length();
        float radiusOffset = this.radius() * (1.0f + random.nextFloat(this.radiusDeviation()));
        if (distance > radiusOffset) {
            return 0;
        }
        float scaled_dot = Vec2.UNIT_X.dot(chunkOffset) / distance;
        float angle = (float) Math.acos(scaled_dot);
        // The offset is below X axis, flip the angle (so that it extends beyond 180 degrees
        angle = chunkOffset.y < 0.0 ? TAU - angle : angle;

        //float angleOffset = this.getAngleOffset(seed, regionX, regionZ, angle);
        float angleOffset = this.getContinuousAngleOffset(random, angle) * this.angleInfluence();

        int baseValueWithDeviation = this.baseValue() + random.nextInt(this.valueDeviation());
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