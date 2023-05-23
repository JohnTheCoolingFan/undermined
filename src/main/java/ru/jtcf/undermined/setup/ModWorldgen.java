package ru.jtcf.undermined.setup;

import net.minecraft.util.random.Weight;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraftforge.registries.RegistryObject;
import ru.jtcf.undermined.worldgen.ResourceVeinPlacementConfiguration;
import ru.jtcf.undermined.worldgen.ResourceVeinResourceConfiguration;

public class ModWorldgen {
    public static RegistryObject<ResourceVeinPlacementConfiguration> OVERWORLD_PLACEMENT =
            Registration.RESOURCE_VEIN_PLACEMENTS.register("overworld",
                    () -> new ResourceVeinPlacementConfiguration(Level.OVERWORLD.location(), 10, 5, 1236684071));
    public static RegistryObject<ResourceVeinPlacementConfiguration> NETHER_PLACEMENT =
            Registration.RESOURCE_VEIN_PLACEMENTS.register("nether",
                    () -> new ResourceVeinPlacementConfiguration(Level.NETHER.location(), 10, 5, 1236484171));
    public static RegistryObject<ResourceVeinPlacementConfiguration> END_PLACEMENT =
            Registration.RESOURCE_VEIN_PLACEMENTS.register("end",
                    () -> new ResourceVeinPlacementConfiguration(Level.END.location(), 10, 5, 1206674074));

    public static RegistryObject<ResourceVeinResourceConfiguration> IRON_ORE = Registration.RESOURCE_VEINS.register(
            "iron_ore", () -> new ResourceVeinResourceConfiguration(
                    Items.IRON_ORE.getRegistryName(),
                    2,
                    0,
                    1,
                    0,
                    128,
                    0,
                    0,
                    Weight.of(10),
                    RandomSpreadType.LINEAR
            ));

    static void register() {
    }
}
