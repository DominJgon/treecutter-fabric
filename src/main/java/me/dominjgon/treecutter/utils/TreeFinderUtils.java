package me.dominjgon.treecutter.utils;

import me.dominjgon.treecutter.Treecutter;
import me.dominjgon.treecutter.data.ConfigKeys;
import me.dominjgon.treecutter.data.TreeBlock;
import me.dominjgon.treecutter.data.TreeBlockType;
import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class TreeFinderUtils {
    public static Set<TreeBlock> findAttachedTree(World world, BlockPos origin, int maxLoops) {

        if (maxLoops == 0)
            maxLoops = 16;

        Set<TreeBlock> foundBlocks = findWoodenStructure(world, origin, maxLoops);

        if (foundBlocks.isEmpty())
            return null;

        TreeBlock lowestBlock = new TreeBlock(origin, TreeBlockType.Log);
        int lowestYBlock = lowestBlock.positon.getY();

        for (TreeBlock block : foundBlocks) {
            if (block.blockType != TreeBlockType.Log)
                continue;

            if (block.positon.getY() < lowestYBlock) {
                lowestYBlock = block.positon.getY();
                lowestBlock = block;
            }
        }

        boolean containsMangroveRoots = checkForMangroveRoots(foundBlocks);

        boolean isOnSoil = false;
        int x = lowestBlock.positon.getX();
        int y = lowestBlock.positon.getY();
        int z = lowestBlock.positon.getZ();

        if (!containsMangroveRoots) {
            isOnSoil = isOnTopOfSoilRegular(world, y, x, z);
        } else
            isOnSoil = isOnTopOfSoilMangrove(world, x, y, z);

        if (!isOnSoil)
            return null;

        LinkedHashSet<TreeBlock> leaves = gatherLeaves(world, foundBlocks);

        if (leaves.isEmpty())
            return null;

        Treecutter.LogInfo("Found {} leaves blocks", leaves.size());
        foundBlocks.addAll(leaves);

        return foundBlocks;
    }

    private static boolean isOnTopOfSoilRegular(World world, int y, int x, int z) {
        boolean isOnSoil;
        --y;
        BlockState soilBlockState = world.getBlockState(new BlockPos(x, y, z));
        isOnSoil = SaplingLookupUtils.canGrowOnBlock(soilBlockState.getBlock());
        return isOnSoil;
    }

    private static boolean isOnTopOfSoilMangrove(World world, int x, int y, int z) {
        int triesLeft = 9; //TODO magic number for mangrove height, max distance i found plus two

        do {
            BlockState soilBlockState = world.getBlockState(new BlockPos(x, --y, z));
            if (soilBlockState.isAir() || !soilBlockState.isOpaque())
                continue;

            return SaplingLookupUtils.canGrowOnBlock(soilBlockState.getBlock());
        } while (triesLeft-- > 0);

        return false;
    }

    private static LinkedHashSet<TreeBlock> gatherLeaves(World world, Set<TreeBlock> foundBlocks) {

        LinkedHashSet<TreeBlock> leaves = new LinkedHashSet<>(foundBlocks.size() * 8);
        Map<BlockPos, TreeBlock> bestByPos = new HashMap<>(foundBlocks.size() * 8);
        Deque<TreeBlock> queue = new ArrayDeque<>(foundBlocks.stream().filter(item -> item.blockType.equals(TreeBlockType.Log)).toList());

        Treecutter.LogInfo("Finding leaves for {} logs", queue.size());

        while (!queue.isEmpty()) {
            TreeBlock treeBlock = queue.poll();
            List<BlockPos> traversalPositions = BlockPosOffsetUtils.orderedTraversalPositions(treeBlock.positon, false);

            for (BlockPos position : traversalPositions) {

                if(world.isAir(position))
                    continue;

                if (world.getBlockState(position).isIn(BlockTags.LEAVES)) {
                    int timeToLive = treeBlock.timeToLive - 1;
                    if (timeToLive < 0)
                        continue;

                    TreeBlock existing = bestByPos.get(position);

                    if (existing == null || timeToLive > existing.timeToLive) {
                        TreeBlock candidate = new TreeBlock(position, TreeBlockType.Leaves, timeToLive);
                        bestByPos.put(position, candidate);
                        leaves.remove(existing);
                        leaves.add(candidate);
                        queue.add(candidate);
                    }
                }
            }
        }

        Treecutter.LogInfo("Queue was exhausted, found {} leaves", leaves);
//
//        LinkedHashSet<TreeBlock> sorted = foundBlocks.stream()
//                .sorted(Comparator.comparingInt((TreeBlock b) -> b.timeToLive).reversed())
//                .collect(Collectors.toCollection(LinkedHashSet::new));

        return leaves;
    }

    private static boolean checkForMangroveRoots(Set<TreeBlock> foundBlocks) {
        return foundBlocks.stream()
                .anyMatch(block -> block.blockType.equals(TreeBlockType.Roots));
    }

    private static LinkedHashSet<TreeBlock> findWoodenStructure(World world, BlockPos origin, int maxLoops) {

        LinkedHashSet<TreeBlock> foundBlocks = new LinkedHashSet<>(maxLoops * 9);
        Set<BlockPos> visitedLocations = new HashSet<>(maxLoops * 9);
        Set<BlockPos> positionsToSample = new LinkedHashSet<>(maxLoops);
        Set<BlockPos> nextPositionsToSample = new LinkedHashSet<>(maxLoops);
        int maxTTL = Treecutter.configManager.get(ConfigKeys.LEAVES_DETECTION_TTL).asInt();

        positionsToSample.add(origin);
        TreeBlock sampledTreeBlock = new TreeBlock(origin, TreeBlockType.Log, maxTTL);

        do {
            nextPositionsToSample.clear();

            for (BlockPos sampledPosition : positionsToSample) {

                List<BlockPos> positionsAroundSampled = BlockPosOffsetUtils.orderedTraversalPositions(sampledPosition, false);

                if (sampledTreeBlock == null)
                    sampledTreeBlock = foundBlocks.stream().filter(item -> item.positon.equals(sampledPosition)).findFirst().get();

                for (BlockPos subPosition : positionsAroundSampled) {

                    if (sampledTreeBlock.timeToLive == 0)
                        continue;

                    if (visitedLocations.contains(subPosition))
                        continue;

                    visitedLocations.add(subPosition);

                    if (!world.isInBuildLimit(subPosition))
                        continue;

                    var blockState = world.getBlockState(subPosition);

                    if (blockState.isAir())
                        continue;

                    //only log can detect other log
                    if (sampledTreeBlock.blockType.equals(TreeBlockType.Log))
                        if (blockState.isIn(BlockTags.LOGS)) {
                            foundBlocks.add(new TreeBlock(subPosition, TreeBlockType.Log, maxTTL));
                            nextPositionsToSample.add(subPosition);
                            continue;
                        }


                    /*Skipping leaves for now, later will be added as next method*/
//                    if (blockState.isIn(BlockTags.LEAVES)) {
//                        foundBlocks.add(new TreeBlock(subPosition, TreeBlockType.Leaves, sampledTreeBlock.timeToLive - 1));
//                        nextPositionsToSample.add(subPosition);
//                        continue;
//                    }

                    if (blockState.isOf(Blocks.MANGROVE_ROOTS) || blockState.isOf(Blocks.MUDDY_MANGROVE_ROOTS)) {
                        //prevent roots from detecting up
                        if (subPosition.getY() > sampledPosition.getY())
                            continue;
                        foundBlocks.add(new TreeBlock(subPosition, TreeBlockType.Roots, 0));
                        nextPositionsToSample.add(subPosition);
                        continue;
                    }
                }

                sampledTreeBlock = null;
            }

            positionsToSample.clear();
            positionsToSample.addAll(nextPositionsToSample);

        } while (!positionsToSample.isEmpty() && --maxLoops > 0);

        return foundBlocks;
    }
}