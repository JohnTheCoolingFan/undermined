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
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public record OreVeinFeatureConfiguration(Map<ResourceLocation, OreVeinFeatureResourceConfiguration> resources,
                                          int spacing, int separation, int salt) implements FeatureConfiguration {
    public static final Codec<OreVeinFeatureConfiguration> CODEC =
            RecordCodecBuilder.<OreVeinFeatureConfiguration>mapCodec((builder) -> builder.group(
                                    Codec.unboundedMap(ResourceLocation.CODEC, OreVeinFeatureResourceConfiguration.CODEC)
                                            .fieldOf("resources")
                                            .forGetter(OreVeinFeatureConfiguration::resources),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("spacing")
                                            .forGetter(OreVeinFeatureConfiguration::spacing),
                                    Codec.intRange(0, 4096)
                                            .fieldOf("separation")
                                            .forGetter(OreVeinFeatureConfiguration::separation),
                                    ExtraCodecs.NON_NEGATIVE_INT
                                            .fieldOf("salt")
                                            .forGetter(OreVeinFeatureConfiguration::salt))
                            .apply(builder, OreVeinFeatureConfiguration::new))
                    .flatXmap((spread) -> spread.spacing <= spread.separation ?
                            DataResult.error("Spacing has to be larger than separation") :
                            DataResult.success(spread), DataResult::success)
                    .codec();

    public OreVein getVeinForChunk(long seed, ChunkPos chunk) {
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

        WeightedResourceConfig selectedResource = getRandomResourceConfig(worldgenRandom);
        ResourceLocation resource = selectedResource.resource;
        OreVeinFeatureResourceConfiguration resourceConfig = selectedResource.config;

        int value = resourceConfig.getValue(worldgenRandom, offsetLimit, chunk, regionX, regionZ, spacing);

        return new OreVein(value, resource);
    }

    private WeightedResourceConfig getRandomResourceConfig(Random random) {
        List<WeightedResourceConfig> weightedResourceConfigList = new ArrayList<>();
        this.resources().forEach((resource, config) -> weightedResourceConfigList.add(new WeightedResourceConfig(resource, config)));
        return WeightedRandom.getRandomItem(random, weightedResourceConfigList).get();
    }

    private static class WeightedResourceConfig implements WeightedEntry {
        public ResourceLocation resource;
        public OreVeinFeatureResourceConfiguration config;

        public WeightedResourceConfig(ResourceLocation resource, OreVeinFeatureResourceConfiguration config) {
            this.resource = resource;
            this.config = config;
        }

        @Override
        @NonNull
        public Weight getWeight() {
            return config.weight();
        }
    }
}
