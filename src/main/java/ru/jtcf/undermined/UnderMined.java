package ru.jtcf.undermined;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import ru.jtcf.undermined.setup.Registration;
import ru.jtcf.undermined.worldgen.ResourceVein;
import ru.jtcf.undermined.worldgen.ResourceVeinManager;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(UnderMined.MODID)
public class UnderMined {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "undermined";

    public UnderMined() {
        Registration.register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(UnderMined.class);
        MinecraftForge.EVENT_BUS.register(ResourceVeinManager.class);
    }

    // Debug print but in-game
    @SubscribeEvent
    public static void onRenderGameOverlayText(RenderGameOverlayEvent.Text event) {
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if (integratedServer == null) {
            event.getRight().add("Sorry, debug resource vein exploring isn't available in multiplayer, use a scan map");
            return;
        }
        ServerLevel level = integratedServer.overworld();
        ResourceVeinManager manager = ResourceVeinManager.get(level);
        ResourceVein vein = manager.getVein(level.getSeed(), Minecraft.getInstance().player.chunkPosition(), null);
        if (vein != null) {
            event.getRight().add("resource: " + vein.resource().toString() + "  value: " + vein.value());
        }
    }
}
