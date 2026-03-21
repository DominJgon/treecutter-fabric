package me.dominjgon.treecutter;

import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.Set;

public class TreeFloodFill {
    public static Set<TreeBlock> findAttachedTree(World world, BlockPos origin, int maxLoops) {

        if(maxLoops == 0)
            maxLoops = 16;

        boolean hasLeaves = false;
        boolean isOnSoil = false;
        boolean containsMangroveRoots = false;

        Set<BlockPos> visitedLocations = new HashSet<>(maxLoops * 9);
        Set<TreeBlock> foundBlocks = new HashSet<>(maxLoops * 9);

        Set<BlockPos> positionsToSample = new HashSet<>(maxLoops);
        Set<BlockPos> nextPositionsToSample = new HashSet<>(maxLoops);

        positionsToSample.add(origin);

        do {
            nextPositionsToSample.clear();

            for(BlockPos sampledPosition : positionsToSample) {

                Set<BlockPos> positionsAroundSampled = getPositionsAroundCenter(sampledPosition);

                for(BlockPos sampledAroundPosition : positionsAroundSampled) {

                    if(visitedLocations.contains(sampledAroundPosition))
                        continue;

                    visitedLocations.add(sampledAroundPosition);

                    if(!world.isInBuildLimit(sampledAroundPosition))
                        continue;

                    var blockState = world.getBlockState(sampledAroundPosition);

                    if(blockState.isAir())
                        continue;

                    if(blockState.isIn(BlockTags.LOGS)) {
                        foundBlocks.add(new TreeBlock(sampledAroundPosition, TreeBlockType.Log));
                        nextPositionsToSample.add(sampledAroundPosition);
                        continue;
                    }

                    if(blockState.isIn(BlockTags.LEAVES)) {
                        hasLeaves = true;

                        continue;
//                        foundBlocks.add(new TreeBlock(sampledAroundPosition, TreeBlockType.Leaves));
//                        nextPositionsToSample.add(sampledAroundPosition);
//                        continue;
                    }

                    if (blockState.isOf(Blocks.MANGROVE_ROOTS) || blockState.isOf(Blocks.MUDDY_MANGROVE_ROOTS)) {
                        containsMangroveRoots = true;
                        foundBlocks.add(new TreeBlock(sampledAroundPosition, TreeBlockType.Roots));
                        nextPositionsToSample.add(sampledAroundPosition);
                        continue;
                    }
                }
            }

            positionsToSample.clear();
            positionsToSample.addAll(nextPositionsToSample);

        } while(!positionsToSample.isEmpty() && --maxLoops > 0);

        if(foundBlocks.isEmpty())
            return null;

        TreeBlock lowestBlock = new TreeBlock(origin, TreeBlockType.Log);
        int lowestYBlock = lowestBlock.positon.getY();

        for (TreeBlock block : foundBlocks) {
            if(block.blockType != TreeBlockType.Log)
                continue;

            if(block.positon.getY() < lowestYBlock){
                lowestYBlock = block.positon.getY();
                lowestBlock = block;
            }
        }

        int x = lowestBlock.positon.getX();
        int y = lowestBlock.positon.getY();
        int z = lowestBlock.positon.getZ();

        if(!containsMangroveRoots) {
            --y;
            BlockState soilBlockState = world.getBlockState(new BlockPos(x, y, z));
            isOnSoil = canGrowOnBlock(soilBlockState.getBlock());
        }

        if(containsMangroveRoots) {

            int triesLeft = 9; //TODO magic number for mangrove height, max distance i found plus two

            do {
                BlockState soilBlockState = world.getBlockState(new BlockPos(x, --y, z));
                if (soilBlockState.isAir() || !soilBlockState.isOpaque())
                    continue;

                isOnSoil = canGrowOnBlock(soilBlockState.getBlock());
                break;
            } while (triesLeft-- > 0);
        }



        Treecutter.LogInfo("Tree block is ({}), it's on ({}), isOnSoil ({})", lowestBlock.positon, world.getBlockState(new BlockPos(x, y, z)).getBlock().getName(), isOnSoil);

        if(hasLeaves && isOnSoil)
            return foundBlocks;

        return null;
    }

    private static Set<BlockPos> getPositionsAroundCenter(BlockPos initialPosition) {
        Set<BlockPos> positions = new HashSet<>(26);

        Vector3i center = initialPosition.asVector3i();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {

                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    positions.add(new BlockPos(
                            center.x + dx,
                            center.y + dy,
                            center.z + dz
                    ));
                }
            }
        }

        return positions;
    }

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