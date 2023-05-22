package ru.jtcf.undermined.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OreVeinManager extends SavedData {
    private final Map<ChunkPos, OreVein> veinMap = new HashMap<>();

    public static OreVeinManager get(Level level) {
        if (level.isClientSide()) {
            throw new RuntimeException("Access on client-side is not allowed!");
        }

        DimensionDataStorage storage = ((ServerLevel) level).getDataStorage();

        return storage.computeIfAbsent(OreVeinManager::new, OreVeinManager::new, "ore_vein_manager");
    }

    @Nullable
    public OreVein getVein(long seed, ChunkPos pos, @Nullable OreVeinFeatureConfiguration config) {
        return veinMap.computeIfAbsent(pos, cp -> {
            if (config != null) {
                OreVein vein = config.getVeinForChunk(seed, cp);
                if (vein.value() > 0) {
                    return vein;
                }
            }
            return null;
        });
    }

    public OreVeinManager() {
    }

    public OreVeinManager(CompoundTag nbt) {
        ListTag veins = nbt.getList("veins", Tag.TAG_COMPOUND);
        for (Tag t : veins) {
            CompoundTag vein = (CompoundTag) t;
            int veinValue = vein.getInt("value");
            ResourceLocation resource = new ResourceLocation(vein.getString("resource"));
            ChunkPos pos = new ChunkPos(vein.getInt("x"), vein.getInt("z"));
            veinMap.put(pos, new OreVein(veinValue, resource));
        }
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag veins = new ListTag();
        veinMap.forEach((pos, vein) -> {
            CompoundTag veinTag = new CompoundTag();
            veinTag.putInt("x", pos.x);
            veinTag.putInt("z", pos.z);
            veinTag.putInt("value", vein.value());
            veinTag.putString("resource", vein.resource().toString());
            veins.add(veinTag);
        });
        pCompoundTag.put("veins", veins);
        return pCompoundTag;
    }
}