package ru.jtcf.undermined.setup;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.RegistryObject;
import ru.jtcf.undermined.worldgen.OreVeinFeature;
import ru.jtcf.undermined.worldgen.OreVeinFeatureConfiguration;

public class ModWorldgen {
    public static final RegistryObject<Feature<OreVeinFeatureConfiguration>> ORE_VEIN_FEATURE =
            Registration.FEATURES.register("ore_vein", OreVeinFeature::new);

    static void register() {
    }
}
