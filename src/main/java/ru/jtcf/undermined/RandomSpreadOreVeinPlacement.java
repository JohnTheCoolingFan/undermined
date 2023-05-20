package ru.jtcf.undermined;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

// TODO: move more generation values into here so this can be used as feature spread
/**
 * @param spacing spacing between region origins, defines a "grid" size
 * @param separation minimum distance between features
 * @param radius controls cutoff radius
 * @param spreadType ore vein spreading type
 * @param salt randomness salt
 */
public record RandomSpreadOreVeinPlacement(int spacing, int separation, float radius, RandomSpreadType spreadType,
                                           int salt) {
    private static final float TAU = (float)Math.PI * 2.0f;

    public static final Codec<RandomSpreadOreVeinPlacement> CODEC =
            RecordCodecBuilder.<RandomSpreadOreVeinPlacement>mapCodec((builder) -> builder.group(
                                    Codec.intRange(0, 4096)
                                            .fieldOf("spacing")
                                            .forGetter(RandomSpreadOreVeinPlacement::spacing),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("separation")
                                            .forGetter(RandomSpreadOreVeinPlacement::separation),
                                    Codec.floatRange(Math.ulp(1.0f), 4096.0f)
                                            .fieldOf("radius")
                                            .forGetter(RandomSpreadOreVeinPlacement::radius),
                                    RandomSpreadType.CODEC
                                            .optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                            .forGetter(RandomSpreadOreVeinPlacement::spreadType),
                                    ExtraCodecs.NON_NEGATIVE_INT
                                            .fieldOf("salt")
                                            .forGetter(RandomSpreadOreVeinPlacement::salt))
                                .apply(builder, RandomSpreadOreVeinPlacement::new))
                    .flatXmap((spread) -> spread.spacing <= spread.separation ?
                                    DataResult.error("Spacing has to be larger than separation") :
                                    DataResult.success(spread),
                            DataResult::success).codec();

    public OreVeinPlacement getValuesForChunk(long seed, int chunkX, int chunkZ) {
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
        float scaled_dot = Vec2.UNIT_X.dot(chunkOffset) / distance;
        float angle = (float)Math.acos(scaled_dot);
        // The offset is below X axis, flip the angle (so that it extends beyond 180 degrees
        angle = chunkOffset.y < 0.0 ? TAU - angle : angle;

        //float angleOffset = this.getAngleOffset(seed, regionX, regionZ, angle);
        float angleOffset = this.getContinuousAngleOffset(worldgenRandom, angle);

        // Distance modifier is in a range [0.0; 1.0]. The steepness of a falloff on the edge of the vein is
        // determined by radius. Angle offset changes the value of a chunk based on its angle relative to the center
        // chunk.
        return new OreVeinPlacement(Math.max(0.0f, 1.0f - distance / this.radius()), angleOffset);
    }

    // FIXME: The offset would be the same for the same angle, but it is not continuous across the whole range of angles
    private float getAngleOffset(long seed, int regionX, int regionZ, float angle) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        // angle is used as part of the seed/salt to ensure that each angle has a different value
        worldgenRandom.setLargeFeatureWithSalt(seed, regionX, regionZ, this.salt() + Float.floatToRawIntBits(angle));
        return worldgenRandom.nextFloat(-1.0f, 1.0f);
    }

    private static final int OFFSETS_DIVISIONS = 36;
    private static final float OFFSET_RANGE = TAU / OFFSETS_DIVISIONS;

    // I'm not sure whether this works correctly or not, need to be tested
    private float getContinuousAngleOffset(WorldgenRandom worldgenRandom, float angle) {
        List<Float> offsets = new ArrayList<>();

        for (int i = 0; i < OFFSETS_DIVISIONS; i++) {
            offsets.add(worldgenRandom.nextFloat(-1.0f, 1.0f));
        }

        int offsetIndexLower = (int)Math.floor(OFFSETS_DIVISIONS * (TAU / angle));
        int offsetIndexUpper = offsetIndexLower < OFFSETS_DIVISIONS - 1 ? offsetIndexLower + 1 : 0;
        float remainder = angle % offsetIndexLower;
        float baseline = offsets.get(offsetIndexLower);
        return baseline + (offsets.get(offsetIndexUpper) - baseline) * OFFSET_RANGE / remainder;
    }

    /**
     * @param distanceModifier a modifier that is describing how far away from ore vein center the chunk is
     * @param angleOffset an offset for the chunk value derived from an angle relative to the center chunk of the ore
     *                   vein
     */
    public record OreVeinPlacement(float distanceModifier, float angleOffset) {
    }

}
