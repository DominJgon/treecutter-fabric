package me.dominjgon.treecutter;

import me.dominjgon.treecutter.data.ConfigKeys;
import me.dominjgon.treecutter.manaagers.ConfigManager;
import me.dominjgon.treecutter.manaagers.TreecutterManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Treecutter implements ModInitializer {

    public static final String MOD_ID = "treecutter";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ConfigManager configManager;
    private static final TreecutterManager treecutterManager = new TreecutterManager();

    public static void LogInfo(String text, Object... args) {
        LOGGER.info("Treecutter: " + text, args);
    }

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            Properties defaults = new Properties();
            defaults.setProperty(ConfigKeys.MAX_LOOPS, "16");
            defaults.setProperty(ConfigKeys.AUTO_DESTROY_LEAVES, "true");
            defaults.setProperty(ConfigKeys.INCLUDE_IN_TOOL_NAME, "[tc]");
            defaults.setProperty(ConfigKeys.LEAVES_DETECTION_TTL, "3");
            defaults.setProperty(ConfigKeys.CUTTER_BLOCK_BREAKS_PER_TICK, "5");

            configManager = new ConfigManager(
                    minecraftServer.getRunDirectory().resolve("config/treecutter.properties"),
                    defaults
            );
            configManager.load();

            treecutterManager.onServerStartup(minecraftServer);
        });

        PlayerBlockBreakEvents.AFTER.register((world, playerEntity, blockPos, blockState, blockEntity) -> {
            treecutterManager.onBreakBlock(world, playerEntity, blockPos, blockState);
        });

        ServerTickEvents.END_SERVER_TICK.register(serverWorld -> {
            treecutterManager.onTick(serverWorld);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            treecutterManager.purge();
            configManager = null;
        });
    }
}
