package me.dominjgon.treecutter.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class SaplingLookupUtils {
    public static boolean canGrowOnBlock(Block soil) {
        return soil == Blocks.GRASS_BLOCK
                || soil == Blocks.DIRT
                || soil == Blocks.COARSE_DIRT
                || soil == Blocks.ROOTED_DIRT
                || soil == Blocks.PODZOL
                || soil == Blocks.MYCELIUM
                || soil == Blocks.MUD
                || soil == Blocks.MUDDY_MANGROVE_ROOTS
                || soil == Blocks.CRIMSON_NYLIUM
                || soil == Blocks.WARPED_NYLIUM
                || soil == Blocks.SOUL_SOIL
                || soil == Blocks.NETHERRACK;
    }
}
