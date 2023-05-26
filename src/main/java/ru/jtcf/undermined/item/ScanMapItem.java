package ru.jtcf.undermined.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import ru.jtcf.undermined.setup.ModCreativeTab;
import ru.jtcf.undermined.setup.ModItems;
import ru.jtcf.undermined.vein_scan.ScanMapItemSavedData;
import ru.jtcf.undermined.worldgen.ResourceVein;

import java.util.Map;
import java.util.stream.Collectors;

// TODO: reset veins when scan map is used in a craft, or just disallow scaling. Revert to regular map by using
//  single item in craft
public class ScanMapItem extends MapItem {

    public ScanMapItem() {
        super(new Properties().tab(ModCreativeTab.UNDERMINED_CREATIVE_TAB));
    }

    public static void updateScanData(ItemStack scanMapStack, Map<Pair<Integer, Integer>, ResourceVein> veins,
                                      Level level) {
        ScanMapItemSavedData scanData = ScanMapItem.getSavedScanData(scanMapStack, level);
        if (scanData != null) {
            updateScanData(veins, scanData);
        }
    }

    public static void updateScanData(Map<Pair<Integer, Integer>, ResourceVein> veins,
                                      @NonNull ScanMapItemSavedData scanData) {
        scanData.veins.putAll(veins);
    }

    public static void updateScanData(ItemStack scanMapStack, Level level, Map<ChunkPos, ResourceVein> veins) {
        ScanMapItemSavedData scanData = ScanMapItem.getSavedScanData(scanMapStack, level);
        if (scanData != null) {
            ChunkPos mapChunkPosMin = new ChunkPos(new BlockPos(scanData.x, 0, scanData.z));
            ChunkPos mapChunkPosMax = new ChunkPos(new BlockPos(
                    scanData.x + 128 * (1 << scanData.scale),
                    0,
                    scanData.z + 128 * (1 << scanData.scale)
            ));
            updateScanData(
                    veins.entrySet().stream().filter((entry) ->
                            entry.getKey().x < mapChunkPosMax.x
                                    && entry.getKey().x >= mapChunkPosMin.x
                                    && entry.getKey().z < mapChunkPosMax.z
                                    && entry.getKey().z >= mapChunkPosMin.z
                    ).collect(Collectors.toMap((entry) -> {
                        ChunkPos chunkPos = entry.getKey();
                        return Pair.of(chunkPos.x - mapChunkPosMin.x, chunkPos.z - mapChunkPosMin.z);
                    }, Map.Entry::getValue)),
                    scanData
            );
        }
    }

    @Nullable
    public static ItemStack createFromMap(Level level, ItemStack originalMapStack) {
        MapItemSavedData mapData = MapItem.getSavedData(originalMapStack, level);
        if (mapData != null) {
            ItemStack scanMapStack = new ItemStack(ModItems.SCAN_MAP_ITEM.get());
            createAndStoreScanData(originalMapStack, mapData, scanMapStack, level);
            return scanMapStack;
        }
        return null;
    }

    private static void createAndStoreScanData(ItemStack originalMapStack,
                                               MapItemSavedData mapData, ItemStack scanMapStack, Level level) {
        CompoundTag mapTag = originalMapStack.getTag();
        scanMapStack.setTag(mapTag);
        int scanId = createNewScanData(mapData, level);
        storeScanData(scanMapStack, scanId);
    }

    private static int createNewScanData(MapItemSavedData mapData, Level level) {
        ScanMapItemSavedData newData = ScanMapItemSavedData.createFresh(mapData);
        int scanId = ScanMapItemSavedData.getFreeScanId(level);
        ScanMapItemSavedData.set(level, newData, makeScanKey(scanId));
        return scanId;
    }

    private static void storeScanData(ItemStack stack, int scanId) {
        stack.getOrCreateTag().putInt("scan", scanId);
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
