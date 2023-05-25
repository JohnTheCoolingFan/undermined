package ru.jtcf.undermined.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.jtcf.undermined.setup.ModCreativeTab;
import ru.jtcf.undermined.vein_scan.ScanMapItemSavedData;

public class ScanMapItem extends MapItem {

    public ScanMapItem() {
        super(new Properties().tab(ModCreativeTab.UNDERMINED_CREATIVE_TAB));
    }

    @Nullable
    public static ScanMapItemSavedData getSavedScanData(@Nullable Integer scanId, Level level) {
        return scanId == null ? null : ScanMapItemSavedData.get(level, makeScanKey(scanId));
    }

    @Nullable
    public static ScanMapItemSavedData getSavedScanData(ItemStack stack, Level level) {
        Item stack_item = stack.getItem();
        if (stack_item instanceof ScanMapItem scan_map) {
            return scan_map.getCustomScanData(stack, level);
        }
        return null;
    }

    @Nullable
    public ScanMapItemSavedData getCustomScanData(ItemStack stack, Level level) {
        Integer scanId = getScanId(stack);
        return getSavedScanData(scanId, level);
    }

    @Nullable
    public static Integer getScanId(ItemStack stack) {
        CompoundTag itemTag = stack.getTag();
        return itemTag != null && itemTag.contains("scan", Tag.TAG_ANY_NUMERIC) ? itemTag.getInt("scan") : null;
    }

    public static String makeScanKey(Integer scanId) {
        return "scan_" + scanId;
    }
}
