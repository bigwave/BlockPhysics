package blargerist.cake.blockphysics.events;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import blargerist.cake.blockphysics.util.BlockMove;

public class BPEventHandler
{
	static ArrayList<Block> tickingBlocks = new ArrayList<Block>();

	public static void onNeighborBlockChange(World world, BlockPos blockPos, Block blockNeighbor)
	{
		if (!world.isRemote)
		{
		    Block block = world.getBlockState(blockPos).getBlock();
			Material material = block.getMaterial();

			if (material == Material.air || 
				block.getBlockHardness(world, blockPos) == -1.0F || 
				material.isLiquid() || 
				material.isReplaceable() || 
				material == Material.plants || 
				material == Material.portal)
			{
				return;
			}

			if (!tickingBlocks.contains(block))
			{
				tickingBlocks.add(block);
				world.scheduleUpdate(blockPos, block, block.tickRate(world));
				tickingBlocks.remove(block);
			}
		}
	}

    @SubscribeEvent
    public void onPlayerBlockPlace(PlaceEvent event)
    {
        event.world.scheduleUpdate(event.pos, event.state.getBlock(), event.state.getBlock().tickRate(event.world));
    }

	public static void onUpdateBlock(World world, BlockPos blockPos, Random random)
	{
		if (!world.isRemote)
		{
			Block block = world.getBlockState(blockPos).getBlock();
			Material material = block.getMaterial();
			if (material == Material.air || 
				block.getBlockHardness(world, blockPos) == -1.0F || 
				material.isLiquid() || 
				material.isReplaceable() || 
				material == Material.plants || 
				material == Material.portal)
			{
				return;
			}
			BlockMove.fall(world, blockPos);
		}
	}

	public static boolean func_149831_e(World p_149831_0_, int p_149831_1_, int p_149831_2_, int p_149831_3_)
	{
		BlockPos blockPos = new BlockPos(p_149831_1_, p_149831_2_, p_149831_3_);
		Block block = p_149831_0_.getBlockState(blockPos).getBlock();

		if (block.isAir(p_149831_0_, blockPos))
		{
			return true;
		}
		else if (block == Blocks.fire)
		{
			return true;
		}
		else
		{
			Material material = block.getMaterial();
			return material == Material.water ? true : material == Material.lava;
		}
	}
	
	public static void onFragileBlockCollision(Entity entity, int x, int y, int z)
	{
	    BlockPos blockPos = new BlockPos(x, y, z);
		entity.worldObj.setBlockToAir(blockPos);
	}
}