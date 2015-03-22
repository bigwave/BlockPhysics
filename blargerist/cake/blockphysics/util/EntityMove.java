package blargerist.cake.blockphysics.util;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import blargerist.cake.blockphysics.ModInfo;
import blargerist.cake.blockphysics.events.BPEventHandler;

public class EntityMove
{
	public static void onUpdate(EntityFallingBlock entity)
	{
		if (entity.getBlock().getBlock().getMaterial() == net.minecraft.block.material.Material.air)
        {
            entity.setDead();
        }
        else
        {
            entity.prevPosX = entity.posX;
            entity.prevPosY = entity.posY;
            entity.prevPosZ = entity.posZ;
            ++entity.fallTime;
            entity.motionY -= 0.03999999910593033D;
            entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ);
            entity.motionX *= 0.9800000190734863D;
            entity.motionY *= 0.9800000190734863D;
            entity.motionZ *= 0.9800000190734863D;

            if (!entity.worldObj.isRemote)
            {
                int i = MathHelper.floor_double(entity.posX);
                int j = MathHelper.floor_double(entity.posY);
                int k = MathHelper.floor_double(entity.posZ);
                	BlockPos blockPos = new BlockPos(i,j,k);

                if (entity.fallTime == 1)
                {
                    if (entity.worldObj.getBlockState(blockPos) != entity.getBlock())
                    {
                        entity.setDead();
                        return;
                    }

                    entity.worldObj.setBlockToAir(blockPos);
                }
                else if (entity.fallTime == 4)
                {
                	entity.noClip = false;
                }
                String blockName = Block.blockRegistry.getNameForObject(entity.getBlock().getBlock()).toString();
                int blockMeta = entity.getBlock().getBlock().getMetaFromState(entity.getBlock());
                BlockDef blockDef = DefinitionMaps.getBlockDef(blockName, blockMeta);
                if (BlockMove.canMoveTo(entity.worldObj,
                                        i, 
                                        j - 1, 
                                        k, 
                                        blockDef.mass) == 2)
                {
                	entity.worldObj.setBlockToAir(blockPos.down());
                }

                if (entity.onGround)
                {
                	if (entity.motionX < 0.0025 && entity.motionX > -0.0025 && entity.motionZ < 0.0025 && entity.motionZ > -0.0025)
                	{
                        boolean developmentEnvironment = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
                		if (entity.posX > Math.round(entity.posX))
                		{
                		    if (developmentEnvironment)
                		    {
                		        ModInfo.Log.info("Entity posX > " + entity.posX);
                		    }
                			entity.motionX = 0.07;
                		}
                		else if (entity.posX < Math.round(entity.posX))
                		{
                            if (developmentEnvironment)
                            {
                                ModInfo.Log.info("Entity posX < " + entity.posX);
                            }
                			entity.motionX = -0.07;
                		}
                		if (entity.posZ > Math.round(entity.posZ))
                		{
                            if (developmentEnvironment)
                			{
                                ModInfo.Log.info("Entity posZ > " + entity.posZ);
                			}
                			entity.motionZ = 0.07;
                		}
                		else if (entity.posZ < Math.round(entity.posZ))
                		{
                            if (developmentEnvironment)
                			{
                                ModInfo.Log.info("Entity posZ < " + entity.posZ);
                			}
                			entity.motionZ = -0.07;
                		}
                	}
                    entity.motionX *= 0.899999988079071D;
                    entity.motionZ *= 0.899999988079071D;
                    entity.motionY *= -0.5D;

                    blockPos = new BlockPos(i,j,k);
                    if (entity.worldObj.getBlockState(blockPos).getBlock() != net.minecraft.init.Blocks.piston_extension)
                    {

                        if (entity.worldObj.canBlockBePlaced(
                                entity.getBlock().getBlock(), 
                                blockPos,
                                true,
                                EnumFacing.DOWN, 
                                (Entity)null, 
                                (ItemStack)null) && 
                            !BlockFalling.canFallInto(entity.worldObj, new BlockPos(i, j - 1, k)) &&
                            entity.worldObj.setBlockState(blockPos, entity.getBlock(), 3))
                        {
                            entity.setDead();
                            
                            if (entity.getBlock() instanceof BlockFalling)
                            {
                                ((BlockFalling)entity.getBlock()).onNeighborBlockChange(entity.worldObj,
                                                                                        blockPos,
                                                                                        entity.worldObj.getBlockState(blockPos),
                                                                                        entity.worldObj.getBlockState(blockPos).getBlock());
                            }

                            if (entity.tileEntityData != null && entity.getBlock().getBlock() instanceof ITileEntityProvider)
                            {
                                TileEntity tileentity = entity.worldObj.getTileEntity(blockPos);

                                if (tileentity != null)
                                {
                                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                                    tileentity.writeToNBT(nbttagcompound);
                                    Iterator iterator = entity.tileEntityData.getKeySet().iterator();

                                    while (iterator.hasNext())
                                    {
                                        String s = (String)iterator.next();
                                        NBTBase nbtbase = entity.tileEntityData.getTag(s);

                                        if (!s.equals("x") && !s.equals("y") && !s.equals("z"))
                                        {
                                            nbttagcompound.setTag(s, nbtbase.copy());
                                        }
                                    }

                                    tileentity.readFromNBT(nbttagcompound);
                                    tileentity.markDirty();
                                }
                            }
                        }
                        else if (entity.shouldDropItem && (entity.fallTime > 600 || (entity.fallTime > 200 && entity.onGround)))
                        {
                        	entity.setDead();
                            entity.entityDropItem(new ItemStack(entity.getBlock().getBlock(),
                                                                1, 
                                                                entity.getBlock().getBlock().damageDropped(entity.getBlock())), 
                                                  0.0F);
                        }
                    }
                }
                else if (entity.fallTime > 100 && !entity.worldObj.isRemote && (j < 1 || j > 256))
                {
                    if (entity.shouldDropItem)
                    {
                        entity.entityDropItem(new ItemStack(entity.getBlock().getBlock(),
                                                            1,
                                                            entity.getBlock().getBlock().damageDropped(entity.getBlock())),
                                              0.0F);
                    }

                    entity.setDead();
                }
            }
        }
	}
	
	public void moveEntity(EntityFallingBlock entity, double p_70091_1_, double p_70091_3_, double p_70091_5_)
    {
        if (entity.noClip)
        {
            entity.getBoundingBox().offset(p_70091_1_, p_70091_3_, p_70091_5_);
            entity.posX = (entity.getBoundingBox().minX + entity.getBoundingBox().maxX) / 2.0D;
            entity.posY = entity.getBoundingBox().minY + (double)entity.getYOffset() - (double)entity.height;
            entity.posZ = (entity.getBoundingBox().minZ + entity.getBoundingBox().maxZ) / 2.0D;
        }
        else
        {
            entity.worldObj.theProfiler.startSection("move");
            entity.height *= 0.4F;
            double d3 = entity.posX;
            double d4 = entity.posY;
            double d5 = entity.posZ;

            double d6 = p_70091_1_;
            double d7 = p_70091_3_;
            double d8 = p_70091_5_;
            AxisAlignedBB axisalignedbb = entity.getBoundingBox();

            List list = entity.worldObj.getCollidingBoundingBoxes(entity, entity.getBoundingBox().addCoord(p_70091_1_, p_70091_3_, p_70091_5_));

            for (int i = 0; i < list.size(); ++i)
            {
                p_70091_3_ = ((AxisAlignedBB)list.get(i)).calculateYOffset(entity.getBoundingBox(), p_70091_3_);
            }

            entity.getBoundingBox().offset(0.0D, p_70091_3_, 0.0D);

            if (d7 != p_70091_3_)
            {
                p_70091_5_ = 0.0D;
                p_70091_3_ = 0.0D;
                p_70091_1_ = 0.0D;
            }

            boolean flag1 = entity.onGround || d7 != p_70091_3_ && d7 < 0.0D;
            int j;

            for (j = 0; j < list.size(); ++j)
            {
                p_70091_1_ = ((AxisAlignedBB)list.get(j)).calculateXOffset(entity.getBoundingBox(), p_70091_1_);
            }

            entity.getBoundingBox().offset(p_70091_1_, 0.0D, 0.0D);

            if ( d6 != p_70091_1_)
            {
                p_70091_5_ = 0.0D;
                p_70091_3_ = 0.0D;
                p_70091_1_ = 0.0D;
            }

            for (j = 0; j < list.size(); ++j)
            {
                p_70091_5_ = ((AxisAlignedBB)list.get(j)).calculateZOffset(entity.getBoundingBox(), p_70091_5_);
            }

            entity.getBoundingBox().offset(0.0D, 0.0D, p_70091_5_);

            if ( d8 != p_70091_5_)
            {
                p_70091_5_ = 0.0D;
                p_70091_3_ = 0.0D;
                p_70091_1_ = 0.0D;
            }

            double d10;
            double d11;
            int k;
            double d12;

            if (entity.stepHeight > 0.0F && flag1 && entity.height < 0.05F && (d6 != p_70091_1_ || d8 != p_70091_5_))
            {
                d12 = p_70091_1_;
                d10 = p_70091_3_;
                d11 = p_70091_5_;
                p_70091_1_ = d6;
                p_70091_3_ = (double)entity.stepHeight;
                p_70091_5_ = d8;
                AxisAlignedBB axisalignedbb1 = entity.getBoundingBox();
                entity.getBlock().getBlock().setBlockBounds(
                        (float)axisalignedbb.minX, 
                        (float)axisalignedbb.minY, 
                        (float)axisalignedbb.minZ, 
                        (float)axisalignedbb.maxX, 
                        (float)axisalignedbb.maxY, 
                        (float)axisalignedbb.maxZ);

                list = entity.worldObj.getCollidingBoundingBoxes(entity, entity.getBoundingBox().addCoord(d6, p_70091_3_, d8));

                for (k = 0; k < list.size(); ++k)
                {
                    p_70091_3_ = ((AxisAlignedBB)list.get(k)).calculateYOffset(entity.getBoundingBox(), p_70091_3_);
                }

                entity.getBoundingBox().offset(0.0D, p_70091_3_, 0.0D);

                if ( d7 != p_70091_3_)
                {
                    p_70091_5_ = 0.0D;
                    p_70091_3_ = 0.0D;
                    p_70091_1_ = 0.0D;
                }

                for (k = 0; k < list.size(); ++k)
                {
                    p_70091_1_ = ((AxisAlignedBB)list.get(k)).calculateXOffset(entity.getBoundingBox(), p_70091_1_);
                }

                entity.getBoundingBox().offset(p_70091_1_, 0.0D, 0.0D);

                if ( d6 != p_70091_1_)
                {
                    p_70091_5_ = 0.0D;
                    p_70091_3_ = 0.0D;
                    p_70091_1_ = 0.0D;
                }

                for (k = 0; k < list.size(); ++k)
                {
                    p_70091_5_ = ((AxisAlignedBB)list.get(k)).calculateZOffset(entity.getBoundingBox(), p_70091_5_);
                }

                entity.getBoundingBox().offset(0.0D, 0.0D, p_70091_5_);

                if ( d8 != p_70091_5_)
                {
                    p_70091_5_ = 0.0D;
                    p_70091_3_ = 0.0D;
                    p_70091_1_ = 0.0D;
                }

                if ( d7 != p_70091_3_)
                {
                    p_70091_5_ = 0.0D;
                    p_70091_3_ = 0.0D;
                    p_70091_1_ = 0.0D;
                }
                else
                {
                    p_70091_3_ = (double)(-entity.stepHeight);

                    for (k = 0; k < list.size(); ++k)
                    {
                        p_70091_3_ = ((AxisAlignedBB)list.get(k)).calculateYOffset(entity.getBoundingBox(), p_70091_3_);
                    }

                    entity.getBoundingBox().offset(0.0D, p_70091_3_, 0.0D);
                }

                if (d12 * d12 + d11 * d11 >= p_70091_1_ * p_70091_1_ + p_70091_5_ * p_70091_5_)
                {
                    p_70091_1_ = d12;
                    p_70091_3_ = d10;
                    p_70091_5_ = d11;
                    entity.getBlock().getBlock().setBlockBounds(
                            (float)axisalignedbb1.minX, 
                            (float)axisalignedbb1.minY, 
                            (float)axisalignedbb1.minZ, 
                            (float)axisalignedbb1.maxX, 
                            (float)axisalignedbb1.maxY, 
                            (float)axisalignedbb1.maxZ);
                }
            }

            entity.worldObj.theProfiler.endSection();
            entity.worldObj.theProfiler.startSection("rest");
            entity.posX = (entity.getBoundingBox().minX + entity.getBoundingBox().maxX) / 2.0D;
            entity.posY = entity.getBoundingBox().minY + (double)entity.getYOffset() - (double)entity.height;
            entity.posZ = (entity.getBoundingBox().minZ + entity.getBoundingBox().maxZ) / 2.0D;
            entity.isCollidedHorizontally = d6 != p_70091_1_ || d8 != p_70091_5_;
            entity.isCollidedVertically = d7 != p_70091_3_;
            entity.onGround = d7 != p_70091_3_ && d7 < 0.0D;
            entity.isCollided = entity.isCollidedHorizontally || entity.isCollidedVertically;
            updateFallState(entity, p_70091_3_, entity.onGround);

            if (d6 != p_70091_1_)
            {
                entity.motionX = 0.0D;
            }

            if (d7 != p_70091_3_)
            {
                entity.motionY = 0.0D;
            }

            if (d8 != p_70091_5_)
            {
                entity.motionZ = 0.0D;
            }

            d12 = entity.posX - d3;
            d10 = entity.posY - d4;
            d11 = entity.posZ - d5;

            try
            {
                checkEntityBlockCollisions(entity);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                entity.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            entity.worldObj.theProfiler.endSection();
        }
    }
	
	protected void updateFallState(EntityFallingBlock entity, double p_70064_1_, boolean p_70064_3_)
    {
        if (p_70064_3_)
        {
            if (entity.fallDistance > 0.0F)
            {
            	entity.fallDistance = 0.0F;
            }
        }
        else if (p_70064_1_ < 0.0D)
        {
        	entity.fallDistance = (float)((double)entity.fallDistance - p_70064_1_);
        }
    }
	
	public static void checkEntityBlockCollisions(Entity entity)
	{
	    if (entity.getBoundingBox() == null)
	    {
	        return;
	    }
		int i = MathHelper.floor_double(entity.getBoundingBox().minX + 0.001D);
		int j = MathHelper.floor_double(entity.getBoundingBox().minY + 0.001D);
		int k = MathHelper.floor_double(entity.getBoundingBox().minZ + 0.001D);
		int l = MathHelper.floor_double(entity.getBoundingBox().maxX - 0.001D);
		int i1 = MathHelper.floor_double(entity.getBoundingBox().maxY - 0.001D);
		int j1 = MathHelper.floor_double(entity.getBoundingBox().maxZ - 0.001D);

        byte b0 = 32;
        boolean chunksExist = true;
        for (int iIncrement = i; iIncrement <= l; iIncrement++)
        {
            for (int jIncrement = j; jIncrement <= i1; jIncrement++)
            {
                if (!entity.worldObj.getChunkProvider().chunkExists(i  + iIncrement, j + jIncrement))
                {
                    chunksExist = false;
                    break;
                }
            }
            if (!chunksExist)
            {
                break;
            }
        }
       if (chunksExist)
		{
			for (int k1 = i; k1 <= l; ++k1)
			{
				for (int l1 = j; l1 <= i1; ++l1)
				{
					for (int i2 = k; i2 <= j1; ++i2)
					{
					    BlockPos blockPos = new BlockPos(k1, l1, i2);
                        IBlockState blockState = entity.worldObj.getBlockState(blockPos);
                        Block block = blockState.getBlock();

						int meta = block.getMetaFromState(blockState);
						String blockName = Block.blockRegistry.getNameForObject(block).toString();
						if (DefinitionMaps.getBlockDef(blockName, meta).fragile > 0)
						{
							ModInfo.Log.info("Block Fragile: " + blockName);
							BPEventHandler.onFragileBlockCollision(entity, k1, l1, i2); 
						}
						else
						{
							try
							{
								block.onEntityCollidedWithBlock(entity.worldObj, blockPos, entity);
							}
							catch (Throwable throwable)
							{
								CrashReport crashreport = CrashReport.makeCrashReport(throwable , "Colliding entity with block");
								CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being collided with");
								CrashReportCategory.addBlockInfo(crashreportcategory, blockPos, block, meta);
								throw new ReportedException(crashreport);
							}
						}
					}
				}
			}
		}
	}
}