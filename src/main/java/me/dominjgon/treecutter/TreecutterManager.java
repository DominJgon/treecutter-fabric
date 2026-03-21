package me.dominjgon.treecutter;

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

    public void onBreakBlock(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState){

        Treecutter.LogInfo("Player {} broke block {}", playerEntity.getName(), blockState.getBlock().getName());

        if(!blockState.isIn(BlockTags.LOGS))
            return;

        Treecutter.LogInfo("Block is typeof LOGS");


        Item item = playerEntity.getMainHandStack().getItem();
        if(!(item instanceof AxeItem axe))
            return;

        if(!playerEntity.getMainHandStack().getName().getString().toLowerCase().contains("[tc]"))
            return;

        Treecutter.LogInfo("Axe is treecutter");

        Set<TreeBlock> treeBlockSet = TreeFloodFill.findAttachedTree(world, blockPos, 16);
        if(treeBlockSet == null) {
            Treecutter.LogInfo("It's not tree enough");
            return;
        }

        ArrayDeque<TreeBlock> dequeue = new ArrayDeque<>(treeBlockSet.size());

        for (TreeBlock treeBlock : treeBlockSet) {
            dequeue.offer(treeBlock);
        }

        Treecutter.LogInfo("Cutting {} blocks", treeBlockSet.size());

        cuttingOperationSet.add(new TreeCuttingOperation(playerEntity.getUuid(), (ServerWorld) world, dequeue));

        Treecutter.LogInfo("Queued tree cutting operation");
    }

    public void onTick(MinecraftServer server){
        if(cuttingOperationSet.isEmpty())
            return;

        for (TreeCuttingOperation cuttingOperation : cuttingOperationSet) {
            handleQueueElement(cuttingOperation);
        }
        cuttingOperationSet.removeIf(item -> item.treeBlocks.isEmpty());
    }

    private void handleQueueElement(TreeCuttingOperation cuttingOperation){
        if(cuttingOperation.treeBlocks.isEmpty())
            return;

        breakNextBlock(cuttingOperation);
    }

    private void breakNextBlock(TreeCuttingOperation cuttingOperation) {

        for(int index = 1; index < 5; ++index) {

            var treeBlock = cuttingOperation.treeBlocks.poll();
            if(treeBlock == null)
                break;
            if (cuttingOperation.world.getBlockState(treeBlock.positon) == null)
                return;
            cuttingOperation.world.breakBlock(treeBlock.positon, true);

        }
    }

    public void purge() {
        cuttingOperationSet.clear();
    }
}
