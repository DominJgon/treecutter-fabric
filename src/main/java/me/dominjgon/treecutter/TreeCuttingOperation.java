package me.dominjgon.treecutter;

import net.minecraft.server.world.ServerWorld;

import java.util.ArrayDeque;
import java.util.UUID;

public class TreeCuttingOperation {
    public UUID player;
    public ServerWorld world;
    public ArrayDeque<TreeBlock> treeBlocks;

    public TreeCuttingOperation(UUID player, ServerWorld world, ArrayDeque<TreeBlock> treeBlocks){
        this.player = player;
        this.world = world;
        this.treeBlocks = treeBlocks;
    }
}
