package me.dominjgon.treecutter;

import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class TreeFloodFill {
    public static Set<TreeBlock> findAttachedTree(World world, BlockPos origin, int maxLoops) {

        if (maxLoops == 0)
            maxLoops = 16;

        boolean hasLeaves = false;
        boolean isOnSoil = false;
        boolean containsMangroveRoots = false;

        int maxTTL = Treecutter.configManager.get(ConfigKeys.LEAVES_DETECTION_TTL).asInt();

        Set<BlockPos> visitedLocations = new HashSet<>(maxLoops * 9);
        Set<TreeBlock> foundBlocks = new LinkedHashSet<>(maxLoops * 9);

        Set<BlockPos> positionsToSample = new LinkedHashSet<>(maxLoops);
        Set<BlockPos> nextPositionsToSample = new LinkedHashSet<>(maxLoops);

        positionsToSample.add(origin);
        TreeBlock sampledTreeBlock = new TreeBlock(origin, TreeBlockType.Log, maxTTL);

        do {
            nextPositionsToSample.clear();

            for (BlockPos sampledPosition : positionsToSample) {

                List<BlockPos> positionsAroundSampled = orderedTraversalPositions(sampledPosition);

                if (sampledTreeBlock == null)
                    sampledTreeBlock = foundBlocks.stream().filter(item -> item.positon.equals(sampledPosition)).findFirst().get();

                for (BlockPos subPosition : positionsAroundSampled) {

                    if(sampledTreeBlock.timeToLive == 0)
                        continue;

                    if (visitedLocations.contains(subPosition))
                        continue;

                    visitedLocations.add(subPosition);

                    if (!world.isInBuildLimit(subPosition))
                        continue;

                    var blockState = world.getBlockState(subPosition);

                    if (blockState.isAir())
                        continue;

//                    if(sampledTreeBlock.blockType.equals(TreeBlockType.Log))
                        if (blockState.isIn(BlockTags.LOGS)) {
                            foundBlocks.add(new TreeBlock(subPosition, TreeBlockType.Log, maxTTL));
                            nextPositionsToSample.add(subPosition);
                            continue;
                        }

                        if (blockState.isIn(BlockTags.LEAVES)) {
                            hasLeaves = true;
                            foundBlocks.add(new TreeBlock(subPosition, TreeBlockType.Leaves, sampledTreeBlock.timeToLive - 1));
                            nextPositionsToSample.add(subPosition);
                            continue;
                        }

//                    if(!sampledTreeBlock.blockType.equals(TreeBlockType.Leaves))
                    if (blockState.isOf(Blocks.MANGROVE_ROOTS) || blockState.isOf(Blocks.MUDDY_MANGROVE_ROOTS)) {
                        containsMangroveRoots = true;
                        foundBlocks.add(new TreeBlock(subPosition, TreeBlockType.Roots, maxTTL));
                        nextPositionsToSample.add(subPosition);
                        continue;
                    }
                }

                sampledTreeBlock = null;
            }

            positionsToSample.clear();
            positionsToSample.addAll(nextPositionsToSample);

        } while (!positionsToSample.isEmpty() && --maxLoops > 0);

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

        int x = lowestBlock.positon.getX();
        int y = lowestBlock.positon.getY();
        int z = lowestBlock.positon.getZ();

        if (!containsMangroveRoots) {
            --y;
            BlockState soilBlockState = world.getBlockState(new BlockPos(x, y, z));
            isOnSoil = canGrowOnBlock(soilBlockState.getBlock());
        }

        if (containsMangroveRoots) {

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

        if (hasLeaves && isOnSoil)
            return foundBlocks;

        return null;
    }

    private static List<BlockPos> orderedTraversalPositions(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>(27);

        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        // 1. Center
        positions.add(center);

        // 2. Axis-aligned (6 directions)
        positions.add(new BlockPos(x + 1, y, z));
        positions.add(new BlockPos(x - 1, y, z));
        positions.add(new BlockPos(x, y + 1, z));
        positions.add(new BlockPos(x, y - 1, z));
        positions.add(new BlockPos(x, y, z + 1));
        positions.add(new BlockPos(x, y, z - 1));

        // 3. 2-axis diagonals (edges)
        int[] dirs = {-1, 1};

        for (int dx : dirs) {
            for (int dy : dirs) {
                positions.add(new BlockPos(x + dx, y + dy, z)); // XY plane
                positions.add(new BlockPos(x + dx, y, z + dy)); // XZ plane
                positions.add(new BlockPos(x, y + dx, z + dy)); // YZ plane
            }
        }

        // 4. 3-axis diagonals (corners)
        for (int dx : dirs) {
            for (int dy : dirs) {
                for (int dz : dirs) {
                    positions.add(new BlockPos(x + dx, y + dy, z + dz));
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