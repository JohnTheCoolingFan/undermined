package ru.jtcf.undermined;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import ru.jtcf.undermined.setup.Registration;
import ru.jtcf.undermined.worldgen.OreVein;
import ru.jtcf.undermined.worldgen.OreVeinManager;

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
    }

    @SubscribeEvent
    public static void onRenderGameOverlayText(RenderGameOverlayEvent.Text event) {
        ServerLevel level = Minecraft.getInstance().getSingleplayerServer().overworld();
        OreVeinManager manager = OreVeinManager.get(level);
        OreVein vein = manager.getVein(level.getSeed(), Minecraft.getInstance().player.chunkPosition(), null);
        if (vein != null) {
            event.getRight().add("resource: " + vein.resource().toString() + "  value: " + vein.value());
        }
    }

    @SubscribeEvent
    public static void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        event.getGeneration()
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Holder.Reference.createStandAlone(
                        BuiltinRegistries.PLACED_FEATURE,
                        ResourceKey.create(
                                BuiltinRegistries.PLACED_FEATURE.key(),
                                new ResourceLocation("undermined", "ore_vein_placed")
                        )
                ));
    }
}
