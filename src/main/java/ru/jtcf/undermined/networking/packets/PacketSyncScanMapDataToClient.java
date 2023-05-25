package ru.jtcf.undermined.networking.packets;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;
import ru.jtcf.undermined.vein_scan.ClientScanMapData;
import ru.jtcf.undermined.worldgen.ResourceVein;

import java.util.Map;
import java.util.function.Supplier;

public class PacketSyncScanMapDataToClient {
    public final int scanId;
    private final byte scale;
    @Nullable
    private final Map<Pair<Integer, Integer>, ResourceVein> veins;

    public PacketSyncScanMapDataToClient(int scanId, byte scale, @Nullable Map<Pair<Integer, Integer>, ResourceVein> veins) {
        this.scanId = scanId;
        this.scale = scale;
        this.veins = veins;
    }

    public PacketSyncScanMapDataToClient(FriendlyByteBuf buf) {
        this.scanId = buf.readInt();
        this.scale = buf.readByte();
        if (buf.readBoolean()) {
            this.veins = buf.readMap((posBuf) -> Pair.of(posBuf.readInt(), posBuf.readInt()), ResourceVein::new);
        } else {
            this.veins = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(scanId);
        buf.writeByte(scale);
        if (veins != null) {
            buf.writeBoolean(true);
            buf.writeMap(veins, (posBuf, pos) -> {
                posBuf.writeInt(pos.getFirst());
                posBuf.writeInt(pos.getSecond());
            }, (veinBuf, vein) -> {
                vein.toBytes(veinBuf);
            });
        } else {
            buf.writeBoolean(false);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> ClientScanMapData.set(scanId, scale, veins));
        return true;
    }
}
