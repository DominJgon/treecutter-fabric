package me.dominjgon.treecutter;

import net.minecraft.util.math.BlockPos;

public class TreeBlock {
    public BlockPos positon;
    public TreeBlockType blockType;
    public int timeToLive;

    public TreeBlock(BlockPos positon, TreeBlockType type){
        this.positon = positon;
        blockType = type;
        this.timeToLive = 0;
    }

    public TreeBlock(BlockPos positon, TreeBlockType type, int timeToLive){
        this.positon = positon;
        blockType = type;
        this.timeToLive = timeToLive;
    }
}
