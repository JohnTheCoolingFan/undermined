package ru.jtcf.undermined.vein_scan;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;
import ru.jtcf.undermined.item.ScanMapItem;
import ru.jtcf.undermined.worldgen.ResourceVein;

import java.util.HashMap;
import java.util.Map;

public class ClientScanMapData {
    private static final Map<String, ScanMapItemSavedData> scanData = new HashMap<>();

    public static void set(int scanId, byte scale, Map<Pair<Integer, Integer>, ResourceVein> veins) {
        String scanIdKey = ScanMapItem.makeScanKey(scanId);
        if (scanData.containsKey(scanIdKey)) {
            ScanMapItemSavedData recordedScanData = scanData.get(scanIdKey);
            recordedScanData.scale = scale;
            recordedScanData.veins = veins;
        } else {
            scanData.put(scanIdKey, ScanMapItemSavedData.createForClient(scale, veins));
        }
    }

    public static void set(String scanId, ScanMapItemSavedData data) {
        scanData.put(scanId, data);
    }

    @Nullable
    public static ScanMapItemSavedData get(String scanId) {
        return scanData.get(scanId);
    }
}
