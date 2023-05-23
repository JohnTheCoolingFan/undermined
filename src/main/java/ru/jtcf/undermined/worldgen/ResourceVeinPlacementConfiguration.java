package ru.jtcf.undermined.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 *
 */
public final class ResourceVeinPlacementConfiguration extends ForgeRegistryEntry<ResourceVeinPlacementConfiguration> {
    public static final Codec<ResourceVeinPlacementConfiguration> CODEC =
            RecordCodecBuilder.<ResourceVeinPlacementConfiguration>mapCodec((builder) -> builder.group(
                                    ResourceLocation.CODEC
                                            .fieldOf("dimension")
                                            .forGetter(ResourceVeinPlacementConfiguration::dimension),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("spacing")
                                            .forGetter(ResourceVeinPlacementConfiguration::spacing),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("separation")
                                            .forGetter(ResourceVeinPlacementConfiguration::separation),
                                    ExtraCodecs.NON_NEGATIVE_INT
                                            .fieldOf("salt")
                                            .forGetter(ResourceVeinPlacementConfiguration::salt))
                            .apply(builder, ResourceVeinPlacementConfiguration::new))
                    .flatXmap((spread) -> spread.spacing <= spread.separation ?
                            DataResult.error("Spacing has to be larger than separation") :
                            DataResult.success(spread), DataResult::success)
                    .codec();
    private final ResourceLocation dimension;
    private final int spacing;
    private final int separation;
    private final int salt;

    /**
     * @param spacing    Spacing between region origins. One resource per region
     * @param separation Separation between resource vein origin chunk and any of the region origins.
     * @param salt       Randomness salt.
     */
    public ResourceVeinPlacementConfiguration(ResourceLocation dimension, int spacing, int separation, int salt) {
        this.dimension = dimension;
        this.spacing = spacing;
        this.separation = separation;
        this.salt = salt;
    }

    public ResourceVein getVeinForChunk(long seed, ChunkPos chunk,
                                        Collection<ResourceVeinResourceConfiguration> resourceConfigs) {
        // Get spacing and separation as variables
        int spacing = this.spacing();
        int separation = this.separation();
        // Compute region coordinates, defined by spacing size
        int regionX = Math.floorDiv(chunk.x, spacing);
        int regionZ = Math.floorDiv(chunk.z, spacing);
        // Initialize worldgen RNG
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(seed, regionX, regionZ, this.salt());
        // A limit to the random offset from region coordinates
        int offsetLimit = spacing - separation;

        WeightedResourceConfig selectedResource = getRandomResourceConfig(worldgenRandom, resourceConfigs);
        ResourceLocation resource = selectedResource.config.resource();
        ResourceVeinResourceConfiguration resourceConfig = selectedResource.config;

        int value = resourceConfig.getValue(worldgenRandom, offsetLimit, chunk, regionX, regionZ, spacing);

        return new ResourceVein(value, resource);
    }

    private WeightedResourceConfig getRandomResourceConfig(Random random,
                                                           Collection<ResourceVeinResourceConfiguration> resourceConfigs) {
        List<WeightedResourceConfig> weightedResourceConfigList = new ArrayList<>();
        resourceConfigs.forEach((resourceConfig) -> weightedResourceConfigList.add(new WeightedResourceConfig(resourceConfig)));
        return WeightedRandom.getRandomItem(random, weightedResourceConfigList).get();
    }

    public ResourceLocation dimension() {
        return dimension;
    }

    public int spacing() {
        return spacing;
    }

    public int separation() {
        return separation;
    }

    public int salt() {
        return salt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ResourceVeinPlacementConfiguration) obj;
        return Objects.equals(this.dimension, that.dimension) &&
                this.spacing == that.spacing &&
                this.separation == that.separation &&
                this.salt == that.salt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, spacing, separation, salt);
    }

    @Override
    public String toString() {
        return "ResourceVeinPlacementConfiguration[" +
                "dimension=" + dimension + ", " +
                "spacing=" + spacing + ", " +
                "separation=" + separation + ", " +
                "salt=" + salt + ']';
    }


    private static class WeightedResourceConfig implements WeightedEntry {
        public ResourceVeinResourceConfiguration config;

        public WeightedResourceConfig(ResourceVeinResourceConfiguration config) {
            this.config = config;
        }

        @Override
        @NonNull
        public Weight getWeight() {
            return config.weight();
        }
    }
}
