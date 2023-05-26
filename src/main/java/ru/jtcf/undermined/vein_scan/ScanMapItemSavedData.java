package ru.jtcf.undermined.vein_scan;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import ru.jtcf.undermined.worldgen.ResourceVein;

import java.util.HashMap;
import java.util.Map;

public class ScanMapItemSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final int x;
    public final int z;
    public byte scale;
    private static final int MAP_SIZE = 128;
    private static final int HALF_MAP_SIZE = 64;
    public static final byte MAX_SCALE = 4;
    public Map<Pair<Integer, Integer>, ResourceVein> veins = new HashMap<>();

    public ScanMapItemSavedData(int x, int z, byte scale) {
        this.scale = scale;
        this.x = x;
        this.z = z;
        this.setDirty();
    }

    public static ScanMapItemSavedData createFresh(MapItemSavedData mapData) {
        return new ScanMapItemSavedData(mapData.x, mapData.z, mapData.scale);
    }

    public static ScanMapItemSavedData load(CompoundTag nbt) {
        int x = nbt.getInt("xCenter");
        int z = nbt.getInt("zCenter");
        byte scale = Mth.clamp(nbt.getByte("scale"), (byte) 0, MAX_SCALE);

        ScanMapItemSavedData result = new ScanMapItemSavedData(x, z, scale);

        ListTag veinTags = nbt.getList("veins", Tag.TAG_COMPOUND);

        for (Tag tag : veinTags) {
            // Here x and z are local to the map
            CompoundTag veinTag = (CompoundTag) tag;
            int veinX = veinTag.getInt("x");
            int veinZ = veinTag.getInt("z");
            ResourceVein vein = new ResourceVein(veinTag);
            result.veins.put(Pair.of(veinX, veinZ), vein);
        }

        return result;
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("xCenter", this.x);
        nbt.putInt("zCenter", this.z);
        nbt.putByte("scale", this.scale);

        ListTag veinList = new ListTag();

        this.veins.forEach((pos, vein) -> {
            CompoundTag veinTag = new CompoundTag();
            veinTag.putInt("x", pos.getFirst());
            veinTag.putInt("z", pos.getSecond());
            vein.save(veinTag);
            veinList.add(veinTag);
        });

        nbt.put("veins", veinList);

        return nbt;
    }

    public static ScanMapItemSavedData createFresh(BlockPos pos, byte scale) {
        int scaleMultiplier = 128 * (1 << scale);
        int xTemp = Mth.floor((float) (pos.getX() + 64) / scaleMultiplier);
        int zTemp = Mth.floor((float) (pos.getY() + 64) / scaleMultiplier);
        int x = xTemp * scaleMultiplier + scaleMultiplier / 2 - 64;
        int z = zTemp * scaleMultiplier + scaleMultiplier / 2 - 64;
        return new ScanMapItemSavedData(x, z, scale);
    }

    public static int getFreeScanId(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return ScanIndex.get(serverLevel).getFreeScanId();
        } else {
            return 0;
        }
    }

    public static ScanMapItemSavedData createForClient(byte scale, Map<Pair<Integer, Integer>, ResourceVein> veins) {
        return new ScanMapItemSavedData(0, 0, scale);
    }

    // TODO networking
    @Nullable
    public static ScanMapItemSavedData get(Level level, String scanId) {
        if (level instanceof ServerLevel serverLevel) {
            return getFromServer(serverLevel, scanId);
        } else if (level instanceof ClientLevel clientLevel) {
            return getFromClient(clientLevel, scanId);
        }
        return null;
    }

    public static void set(Level level, ScanMapItemSavedData data, String scanId) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().overworld().getDataStorage().set(scanId, data);
        } else if (level instanceof ClientLevel clientLevel) {
            ClientScanMapData.set(scanId, data);
        }
    }

    @Nullable
    public static ScanMapItemSavedData getFromServer(ServerLevel level, String scanId) {
        return level.getServer().overworld().getDataStorage().get(ScanMapItemSavedData::load, scanId);
    }

    @Nullable
    public static ScanMapItemSavedData getFromClient(ClientLevel level, String scanId) {
        return ClientScanMapData.get(scanId);
    }
}
