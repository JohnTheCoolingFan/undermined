package ru.jtcf.undermined.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.jtcf.undermined.setup.Registration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResourceVeinManager extends SavedData {
    private final Map<ChunkPos, ResourceVein> veinMap = new HashMap<>();

    public static ResourceVeinManager get(LevelAccessor level) {
        if (level.isClientSide()) {
            throw new RuntimeException("Access on client-side is not allowed!");
        }

        DimensionDataStorage storage = ((ServerLevel) level).getDataStorage();

        return storage.computeIfAbsent(ResourceVeinManager::new, ResourceVeinManager::new, "ore_vein_manager");
    }

    @SubscribeEvent
    public static void onChunkLoadEvent(ChunkEvent.Load event) {
        if (event.getWorld().isClientSide()) {
            return;
        }
        ServerLevel level = (ServerLevel)event.getWorld();
        ResourceVeinManager veinManager = ResourceVeinManager.get(event.getWorld());
        ChunkPos chunk = event.getChunk().getPos();
        ResourceLocation dimension = level.dimension().location();
        ResourceVeinPlacementConfiguration config =
                Registration.RESOURCE_VEIN_PLACEMENTS_REGISTRY_SUPPLIER.get().getValue(dimension);
        long seed = level.getSeed();
        veinManager.getVein(seed, chunk, config);
    }

    @Nullable
    public ResourceVein getVein(long seed, ChunkPos pos, @Nullable ResourceVeinPlacementConfiguration config) {
        return veinMap.computeIfAbsent(pos, cp -> {
            if (config != null) {
                Collection<ResourceVeinResourceConfiguration> resourceConfigs =
                Registration.RESOURCE_VEINS_REGISTRY_SUPPLIER.get().getValues();
                ResourceVein vein = config.getVeinForChunk(seed, cp, resourceConfigs);
                if (vein.value() > 0) {
                    return vein;
                }
            }
            return null;
        });
    }

    public ResourceVeinManager() {
    }

    public ResourceVeinManager(CompoundTag nbt) {
        ListTag veins = nbt.getList("veins", Tag.TAG_COMPOUND);
        for (Tag t : veins) {
            CompoundTag vein = (CompoundTag) t;
            int veinValue = vein.getInt("value");
            ResourceLocation resource = new ResourceLocation(vein.getString("resource"));
            ChunkPos pos = new ChunkPos(vein.getInt("x"), vein.getInt("z"));
            veinMap.put(pos, new ResourceVein(veinValue, resource));
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