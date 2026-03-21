package me.dominjgon.treecutter;

import net.minecraft.util.math.BlockPos;

public class TreeBlock {
    public BlockPos positon;
    public TreeBlockType blockType;
    public int distanceToLog;

    public TreeBlock(BlockPos positon, TreeBlockType type){
        this.positon = positon;
        blockType = type;
        distanceToLog = 0;
    }

    public TreeBlock(BlockPos positon, TreeBlockType type, int distanceToLog){
        this.positon = positon;
        blockType = type;
        this.distanceToLog = distanceToLog;
    }
}
