package ru.jtcf.undermined.vein_scan;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ScanIndex extends SavedData {
    private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap<>();

    public ScanIndex() {
        this.usedAuxIds.defaultReturnValue(-1);
    }

    public static ScanIndex get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(ScanIndex::load, ScanIndex::new,
                "scanidcounts");
    }

    public static ScanIndex load(CompoundTag nbt) {
        ScanIndex scanIndex = new ScanIndex();

        for (String s : nbt.getAllKeys()) {
            if (nbt.contains(s, Tag.TAG_ANY_NUMERIC)) {
                scanIndex.usedAuxIds.put(s, nbt.getInt(s));
            }
        }

        return scanIndex;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        for (Object2IntMap.Entry<String> entry : usedAuxIds.object2IntEntrySet()) {
            nbt.putInt(entry.getKey(), entry.getIntValue());
        }

        return nbt;
    }

    public int getFreeScanId() {
        int id = usedAuxIds.getInt("map") + 1;
        usedAuxIds.put("map", id);
        setDirty();
        return id;
    }
}
