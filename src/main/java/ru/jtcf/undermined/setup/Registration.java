package ru.jtcf.undermined.setup;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ru.jtcf.undermined.UnderMined;

public class Registration {
    public final static DeferredRegister<Block> BLOCKS = createDeferredRegister(ForgeRegistries.BLOCKS);
    public final static DeferredRegister<Item> ITEMS = createDeferredRegister(ForgeRegistries.ITEMS);
    public final static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            createDeferredRegister(ForgeRegistries.BLOCK_ENTITIES);

    private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> createDeferredRegister(IForgeRegistry<T> registry) {
        return DeferredRegister.create(registry, UnderMined.MODID);
    }

    public static void register() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        ModBlocks.register();
        ModItems.register();
        ModCapabilities.register();
        ModBlockEntityTypes.register();
    }
}
