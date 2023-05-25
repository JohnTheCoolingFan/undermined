package ru.jtcf.undermined.vein_scan;

import com.mojang.datafixers.util.Pair;
import ru.jtcf.undermined.item.ScanMapItem;
import ru.jtcf.undermined.worldgen.ResourceVein;

import java.util.HashMap;
import java.util.Map;

public class ClientScanMapData {
    private static final Map<String, ScanMapItemSavedData> scanData = new HashMap<>();

    public static void set(int scanId, byte scale, Map<Pair<Integer, Integer>, ResourceVein> veins) {
        scanData.computeIfAbsent(ScanMapItem.makeScanKey(scanId),
                (sId) -> ScanMapItemSavedData.createForClient(scale, veins));
    }
}
