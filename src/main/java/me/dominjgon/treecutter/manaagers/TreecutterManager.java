package me.dominjgon.treecutter.manaagers;

import me.dominjgon.treecutter.Treecutter;
import me.dominjgon.treecutter.data.ConfigKeys;
import me.dominjgon.treecutter.data.TreeBlock;
import me.dominjgon.treecutter.data.TreeBlockType;
import me.dominjgon.treecutter.data.TreeCuttingOperation;
import me.dominjgon.treecutter.utils.TreeFinderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

//TODO mangrove trees grow above soil but won't connect directly, later make a cast or special check as well as include mangrove roots to cut out
public class TreecutterManager {
    private static final Set<TreeCuttingOperation> cuttingOperationSet = new HashSet<>();
    private String includeInToolName;
    private int maxLoops;
    private int cutterBlockBreaksPerTick;
    private boolean autoDestroyLeaves;

    public void onServerStartup(MinecraftServer minecraftServer){
        maxLoops = Treecutter.configManager.get(ConfigKeys.MAX_LOOPS).asInt();
        includeInToolName = Treecutter.configManager.get(ConfigKeys.INCLUDE_IN_TOOL_NAME).asString();
        cutterBlockBreaksPerTick = Treecutter.configManager.get(ConfigKeys.CUTTER_BLOCK_BREAKS_PER_TICK).asInt();
        autoDestroyLeaves = Treecutter.configManager.get(ConfigKeys.AUTO_DESTROY_LEAVES).asBoolean();
    }

    public void onBreakBlock(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState) {

        if(playerEntity.isSneaking())
            return;

        if (!blockState.isIn(BlockTags.LOGS))
            return;

        Item item = playerEntity.getMainHandStack().getItem();
        if (!(item instanceof AxeItem))
            return;

        if (!playerEntity.getMainHandStack().getName().getString().toLowerCase().contains(includeInToolName))
            return;

        Set<TreeBlock> treeBlockSet = TreeFinderUtils.findAttachedTree(world, blockPos, maxLoops);

        if (treeBlockSet == null)
            return;

        ArrayDeque<TreeBlock> dequeue = new ArrayDeque<>(treeBlockSet.size());

        for (TreeBlock treeBlock : treeBlockSet)
            dequeue.offer(treeBlock);

        cuttingOperationSet.add(new TreeCuttingOperation(playerEntity.getUuid(), (ServerWorld) world, dequeue));
    }

    public void onTick(MinecraftServer server) {
        if (cuttingOperationSet.isEmpty())
            return;

        for (TreeCuttingOperation cuttingOperation : cuttingOperationSet) {
            handleQueueElement(cuttingOperation);
        }
        cuttingOperationSet.removeIf(item -> item.treeBlocks.isEmpty());
    }

    private void handleQueueElement(TreeCuttingOperation cuttingOperation) {
        if (cuttingOperation.treeBlocks.isEmpty())
            return;

        breakNextBlock(cuttingOperation);
    }

    private void breakNextBlock(TreeCuttingOperation cuttingOperation) {

        for (int index = 1; index < cutterBlockBreaksPerTick; ++index) {

            var treeBlock = cuttingOperation.treeBlocks.poll();
            if (treeBlock == null)
                break;
            if (cuttingOperation.world.getBlockState(treeBlock.positon) == null)
                return;

            if(!autoDestroyLeaves && treeBlock.blockType.equals(TreeBlockType.Leaves))
                return;

            cuttingOperation.world.breakBlock(treeBlock.positon, true);

        }
    }

    public void purge() {
        cuttingOperationSet.clear();
    }
}