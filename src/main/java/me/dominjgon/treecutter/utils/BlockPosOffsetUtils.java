package me.dominjgon.treecutter.utils;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BlockPosOffsetUtils {

    //tries to get up first, then diagonals up, sides, diagonal down and finally bottom
    public static List<BlockPos> orderedTraversalPositions(BlockPos center, boolean includeCenter) {
        List<BlockPos> positions = new ArrayList<>(27);

        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        // 1. Center
        if(includeCenter)
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

    public static List<BlockPos> axisAlignedTraversalPositions(BlockPos center, boolean includeCenter){
        List<BlockPos> positions = new ArrayList<>(27);

        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        // 1. Center
        if(includeCenter)
            positions.add(center);

        // 2. Axis-aligned (6 directions)
        positions.add(new BlockPos(x + 1, y, z));
        positions.add(new BlockPos(x - 1, y, z));
        positions.add(new BlockPos(x, y + 1, z));
        positions.add(new BlockPos(x, y - 1, z));
        positions.add(new BlockPos(x, y, z + 1));
        positions.add(new BlockPos(x, y, z - 1));

        return positions;
    }
}