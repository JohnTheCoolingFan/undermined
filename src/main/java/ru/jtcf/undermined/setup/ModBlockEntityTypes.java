package ru.jtcf.undermined.setup;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntityTypes {
    //public static final RegistryObject<BlockEntityType<PipeRegularBlockEntity>> PIPE_REGULAR = register(
    //        "pipe_regular",
    //        PipeRegularBlockEntity::new,
    //        ModBlocks.PIPE_REGULAR
    //);

    public static void register() {
    }

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name,
                                                                                       BlockEntityType.BlockEntitySupplier<T> factory,
                                                                                       RegistryObject<? extends Block> block) {
        return Registration.BLOCK_ENTITIES.register(name,
                () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }
}
