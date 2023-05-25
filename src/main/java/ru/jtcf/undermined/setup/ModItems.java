package ru.jtcf.undermined.setup;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import ru.jtcf.undermined.item.ScanMapItem;

public class ModItems {
    public static final RegistryObject<Item> SCAN_MAP_ITEM = Registration.ITEMS.register("scan_map", ScanMapItem::new);

    static void register() {
    }
}
