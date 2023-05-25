package ru.jtcf.undermined.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ResourceVein(int value, ResourceLocation resource, boolean isCentral) {
    public ResourceVein(CompoundTag nbt) {
        this(
                nbt.getInt("value"),
                new ResourceLocation(nbt.getString("resource")),
                nbt.contains("is_central", Tag.TAG_ANY_NUMERIC) && nbt.getBoolean("is_central")
        );
    }

    public ResourceVein(FriendlyByteBuf buf) {
        this(
                buf.readInt(),
                buf.readResourceLocation(),
                buf.readBoolean()
        );
    }

    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("value", value());
        nbt.putString("resource", resource().toString());
        if (isCentral()) {
            nbt.putBoolean("is_central", true);
        }
        return nbt;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(value);
        buf.writeResourceLocation(resource);
        buf.writeBoolean(isCentral);
    }
}