package ru.jtcf.undermined.setup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import ru.jtcf.undermined.UnderMined;
import ru.jtcf.undermined.worldgen.ResourceVeinPlacementConfiguration;
import ru.jtcf.undermined.worldgen.ResourceVeinResourceConfiguration;

import java.util.function.Supplier;

public class Registration {
    public final static DeferredRegister<Block> BLOCKS = createDeferredRegister(ForgeRegistries.BLOCKS);
    public final static DeferredRegister<Item> ITEMS = createDeferredRegister(ForgeRegistries.ITEMS);
    public final static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            createDeferredRegister(ForgeRegistries.BLOCK_ENTITIES);
    public final static DeferredRegister<Feature<?>> FEATURES = createDeferredRegister(ForgeRegistries.FEATURES);
    // TODO: use KubeJS to set veins and placements
    // TODO: Provide defaults
    public final static DeferredRegister<ResourceVeinResourceConfiguration> RESOURCE_VEINS =
            DeferredRegister.create(new ResourceLocation("undermined", "ore_veins"), "undermined");
    public final static Supplier<IForgeRegistry<ResourceVeinResourceConfiguration>> RESOURCE_VEINS_REGISTRY_SUPPLIER
            = RESOURCE_VEINS.makeRegistry(ResourceVeinResourceConfiguration.class, RegistryBuilder::new);
    public final static DeferredRegister<ResourceVeinPlacementConfiguration> RESOURCE_VEIN_PLACEMENTS =
            DeferredRegister.create(new ResourceLocation("undermined", "ore_vein_placement"), "undermined");
    public final static Supplier<IForgeRegistry<ResourceVeinPlacementConfiguration>> RESOURCE_VEIN_PLACEMENTS_REGISTRY_SUPPLIER
            = RESOURCE_VEIN_PLACEMENTS.makeRegistry(ResourceVeinPlacementConfiguration.class, RegistryBuilder::new);

    private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> createDeferredRegister(IForgeRegistry<T> registry) {
        return DeferredRegister.create(registry, UnderMined.MODID);
    }

    public static void register() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        FEATURES.register(modEventBus);
        RESOURCE_VEINS.register(modEventBus);
        RESOURCE_VEIN_PLACEMENTS.register(modEventBus);

        ModBlocks.register();
        ModItems.register();
        ModCapabilities.register();
        ModBlockEntityTypes.register();
        ModWorldgen.register();
    }
}
