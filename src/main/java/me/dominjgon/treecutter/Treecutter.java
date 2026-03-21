package me.dominjgon.treecutter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Treecutter implements ModInitializer {

    public static final String MOD_ID = "treecutter";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final TreecutterManager treecutterManager = new TreecutterManager();

    public static void LogInfo(String text, Object... args){
        LOGGER.info("Treecutter: " + text, args);
    }

    @Override
    public void onInitialize() {

        LogInfo("Treecutter initializing");

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {

        });

        PlayerBlockBreakEvents.AFTER.register((world, playerEntity, blockPos, blockState, blockEntity) -> {
            treecutterManager.onBreakBlock(world, playerEntity, blockPos, blockState);
        });

        ServerTickEvents.END_SERVER_TICK.register(serverWorld -> {
            treecutterManager.onTick(serverWorld);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            treecutterManager.purge();
        });
    }
}
