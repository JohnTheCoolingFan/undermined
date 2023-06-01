package ru.jtcf.undermined.setup;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import ru.jtcf.undermined.block.bore_head.BoreHeadBlock;
import ru.jtcf.undermined.block.vein_scanner.VeinScannerBlock;

import java.util.function.Supplier;

public class ModBlocks {
    //public static final RegistryObject<PipeRegularBlock> PIPE_REGULAR = register("pipe_regular", PipeRegularBlock::new);

    public static final RegistryObject<BoreHeadBlock> BORE_HEAD = register("bore_head", BoreHeadBlock::new);
    public static final RegistryObject<VeinScannerBlock> VEIN_SCANNER = register("vein_scanner", VeinScannerBlock::new);

    static void register() {
    }

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> block) {
        return Registration.BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> ret = registerNoItem(name, block);
        Registration.ITEMS.register(name, () -> new BlockItem(ret.get(),
                new Item.Properties().tab(ModCreativeTab.UNDERMINED_CREATIVE_TAB)));
        return ret;
    }
}
