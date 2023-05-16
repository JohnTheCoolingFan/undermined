package ru.jtcf.undermined.setup;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab {
    public static final CreativeModeTab UNDERMINED_CREATIVE_TAB = new CreativeModeTab("undermined") {
        @Override
        public ItemStack makeIcon() {
            return null; // TODO: make icon
        }
    };
}
