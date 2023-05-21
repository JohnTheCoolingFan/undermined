package ru.jtcf.undermined.worldgen;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class OreVeinFeature extends Feature<OreVeinFeatureConfiguration> {
    public OreVeinFeature() {
        super(OreVeinFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreVeinFeatureConfiguration> pContext) {
        OreVeinFeatureConfiguration config = pContext.config();
        ChunkPos chunk = new ChunkPos(pContext.origin());
        long seed = pContext.level().getSeed();
        OreVeinManager veinManager = OreVeinManager.get(pContext.level().getLevel());
        OreVein vein = veinManager.getVein(seed, chunk, config);
        return vein != null;
    }
}