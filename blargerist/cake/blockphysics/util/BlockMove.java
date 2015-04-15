package blargerist.cake.blockphysics.util;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import blargerist.cake.blockphysics.ModConfig;

public class BlockMove
{
	private static Random rand = new Random();

	public static void fall(World world, BlockPos inBlockPos)
	{
        int x = inBlockPos.getX();
        int y = inBlockPos.getY();
        int z = inBlockPos.getZ();
        
		if (!world.isRemote)
		{
			if (playersWithinRange(world, x, y, z))
			{
				byte b0 = 32;
				BlockPos start = new BlockPos (x - b0, y - b0, z - b0);
				BlockPos end = new BlockPos(x + b0, y + b0, z + b0);
		        if (world.isAreaLoaded(start, end))
		        {
					Block block = world.getBlockState(inBlockPos).getBlock();
					String blockName = Block.blockRegistry.getNameForObject(block).toString();
					int meta = world.getBlockState(inBlockPos).getBlock().getMetaFromState(world.getBlockState(inBlockPos));
					BlockDef blockDef = DefinitionMaps.getBlockDef(blockName, meta);

					if (blockDef.canMove)
					{
						MoveDef moveDef = DefinitionMaps.getMovedef(blockDef.id);
						//TODO use an array to store blocks in the area as they are required, to cut down on use of world.getblock
						if (!floating(world, x, y, z, moveDef.floatingRadius, moveDef.floatingBlock, moveDef.floatingMeta))
						{
							int canFall = canMoveTo(world,x, y - 1 , z, blockDef.mass / 10);

							if (canFall != 0 && (moveDef.moveType == 1 || moveDef.moveType == 2))
							{
								if (!hanging(world, x, y, z, moveDef.hanging, blockName, meta))
								{
									if (!attached(world, x, y, z, moveDef.attached, blockName, meta))
									{
										if (!tree(world, x, y, z, moveDef.tree, blockName, meta))
										{
										if (!nCorbel(world, x, y, z, moveDef.nCorbel))
										{
											if (!corbel(world, x, y, z, moveDef.corbel, blockName, meta))
											{
												if (!moveDef.ceiling || !ceiling(world, x, y, z))
												{
													if (!smallArc(world, x, y, z, moveDef.smallArc))
													{
														if (!bigArc(world, x, y, z, moveDef.bigArc))
														{
															if (!moveDef.branch || !branch(world, x, y, z, blockName, meta))
															{
																EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, 
																																(double) ((float) x + 0.5F), 
																																(double) ((float) y + 0.5F), 
																																(double) ((float) z + 0.5F), 
																																block.getStateFromMeta(meta));
																entityfallingblock.setHurtEntities (blockDef.hurts);
																entityfallingblock.noClip = false;
																if (canFall == 2)
																{
																	BlockPos blockPosDownOne = new BlockPos(x, y - 1, z);
																	world.setBlockToAir(blockPosDownOne);
																}
																world.spawnEntityInWorld(entityfallingblock);
																return;
															}
														}
													}
												}
											}
										}
									}
								}
								}
							}
							else if (canFall == 0 && moveDef.moveType == 2 && moveDef.slideChance >= (rand.nextInt(100) + 1))
							{
								String[] slideDirs = new String[8];
								int length = 0;
								boolean north = false;
								boolean south = false;
								boolean east = false;
								boolean west = false;

								if (canMoveTo(world, x, y, z + 1, blockDef.mass) == 1 && canMoveTo(world, x, y - 1, z + 1, blockDef.mass) == 1)
								{
									slideDirs[length] = "north";
									length++;
									north = true;
								}
								if (canMoveTo(world, x, y, z - 1, blockDef.mass) == 1 && canMoveTo(world, x, y - 1, z - 1, blockDef.mass) == 1)
								{
									slideDirs[length] = "south";
									length++;
									south = true;
								}
								if (canMoveTo(world, x + 1, y, z, blockDef.mass) == 1 && canMoveTo(world, x + 1, y - 1, z, blockDef.mass) == 1)
								{
									slideDirs[length] = "east";
									length++;
									east = true;
								}
								if (canMoveTo(world, x - 1, y, z, blockDef.mass) == 1 && canMoveTo(world, x - 1, y - 1, z, blockDef.mass) == 1)
								{
									slideDirs[length] = "west";
									length++;
									west = true;
								}
								if (length > 0)
								{
									if ((north || south) && (east || west))
									{
										if (north && east && canMoveTo(world, x + 1, y, z + 1, blockDef.mass) == 1 && canMoveTo(world, x + 1, y - 1, z + 1, blockDef.mass) == 1)
										{
											slideDirs[length] = "northeast";
											length++;
										}
										if (north && west && canMoveTo(world, x - 1, y, z + 1, blockDef.mass) == 1 && canMoveTo(world, x - 1, y - 1, z + 1, blockDef.mass) == 1)
										{
											slideDirs[length] = "northwest";
											length++;
										}
										if (south && east && canMoveTo(world, x + 1, y, z - 1, blockDef.mass) == 1 && canMoveTo(world, x + 1, y - 1, z - 1, blockDef.mass) == 1)
										{
											slideDirs[length] = "southeast";
											length++;
										}
										if (south && west && canMoveTo(world, x - 1, y, z - 1, blockDef.mass) == 1 && canMoveTo(world, x - 1, y - 1, z - 1, blockDef.mass) == 1)
										{
											slideDirs[length] = "southwest";
											length++;
										}
									}
									if (!hanging(world, x, y, z, moveDef.hanging, blockName, meta))
									{
										if (!attached(world, x, y, z, moveDef.attached, blockName, meta))
										{
											if (!nCorbel(world, x, y, z, moveDef.nCorbel))
											{
												if (!corbel(world, x, y, z, moveDef.corbel, blockName, meta))
												{
													if (!moveDef.ceiling || !ceiling(world, x, y, z))
													{
														if (!smallArc(world, x, y, z, moveDef.smallArc))
														{
															if (!bigArc(world, x, y, z, moveDef.bigArc))
															{
																if (!moveDef.branch || !branch(world, x, y, z, blockName, meta))
																{
																	String direction = slideDirs[rand.nextInt(length)];
																	double motionX = 0;
																	double motionZ = 0;
																	if (direction.equals("north"))
																	{
																		motionZ = 0.135;
																	}
																	else if (direction.equals("south"))
																	{
																		motionZ = -0.135;
																	}
																	else if (direction.equals("east"))
																	{
																		motionX = 0.135;
																	}
																	else if (direction.equals("west"))
																	{
																		motionX = -0.135;
																	}
																	else if (direction.equals("northeast"))
																	{
																		motionZ = 0.135;
																		motionX = 0.135;
																	}
																	else if (direction.equals("northwest"))
																	{
																		motionZ = 0.135;
																		motionX = -0.135;
																	}
																	else if (direction.equals("southeast"))
																	{
																		motionZ = -0.135;
																		motionX = 0.135;
																	}
																	else if (direction.equals("southwest"))
																	{
																		motionZ = -0.135;
																		motionX = -0.135;
																	}
																	
																	EntityFallingBlock entityfallingblock = new EntityFallingBlock(world,
																	                                                               (double) ((float) x + 0.5F), 
																	                                                               (double) ((float) y + 0.5F), 
																	                                                               (double) ((float) z + 0.5F), 
																	                                                               block.getStateFromMeta(meta));
																	entityfallingblock.motionZ = motionZ;
                                                                    entityfallingblock.motionX = motionX;
                                                                    entityfallingblock.setHurtEntities (blockDef.hurts);
                                                                    world.spawnEntityInWorld(entityfallingblock);
                                                                    return;

																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
							else if (moveDef.moveType == 3)
							{

							}
						}
					}
				}
			}
		}
	}

	public static int canMoveTo(World world, int x, int y, int z, int mass)
	{
	    BlockPos blockPos = new BlockPos(x, y, z);
		Block block = world.getBlockState(blockPos).getBlock();
		if (block == Blocks.air)
		{
			return 1;
		}
		if (block.getBlockHardness(world, blockPos) == -1)
		{
			return 0;
		}
		Object blockName = Block.blockRegistry.getNameForObject(block);
		String blockNameString = blockName.toString();
		int meta = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
		BlockDef blockDef = DefinitionMaps.getBlockDef(blockNameString, meta);
		if ((blockDef.fragile > 0 && blockDef.strength < mass))
		{
			return 2;
		}
		Material material = block.getMaterial();
		return (material == Material.air || material == Material.fire || material.isLiquid() || material == Material.plants || material == Material.vine || material == Material.circuits) ? 1 : 0;
	}

	public static boolean playersWithinRange(World world, int x, int y, int z)
	{
		for (Object object : world.playerEntities)
		{
			EntityPlayer player = (EntityPlayer) object;

			if (Math.abs(x - MathHelper.floor_double(player.posX)) <= ModConfig.fallRange && Math.abs(z - MathHelper.floor_double(player.posZ)) <= ModConfig.fallRange)
			{
				return true;
			}
		}
		return false;
	}

	private static boolean floating(World world, int x, int y, int z, int radius, String blockName, int meta)
	{
		for (int yy = y - radius; yy <= y + radius; yy++)
		{
			for (int xx = x - radius; xx <= x + radius; xx++)
			{
				for (int zz = z - radius; zz <= z + radius; zz++)
				{
				    BlockPos blockPos = new BlockPos(xx, yy, zz);
			        Block block = world.getBlockState(blockPos).getBlock();

				    String otherBlockName = Block.blockRegistry.getNameForObject(block).toString();
				    int otherMeta = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));   
					if (sameBlock(otherBlockName, 
					              otherMeta, 
					              blockName, 
					              meta))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean hanging(World world, int x, int y, int z, int hanging, String blockName, int meta)
	{
		if (hanging <= 0)
		{
			return false;
		}
		String blockName2;
		int meta2;
		hanging = hanging + y;

		for (int i = y; i < hanging; i++)
		{
		    BlockPos blockPos = new BlockPos(x, i, z);
			blockName2 = Block.blockRegistry.getNameForObject(world.getBlockState(blockPos).getBlock()).toString();
			meta2 = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
			if (DefinitionMaps.getBlockDef(blockName2, meta2).supportiveBlock)
			{
				return true;
			}
			else if (sameBlock(blockName, meta, blockName2, meta2))
			{
				return false;
			}
		}
		return false;
	}

	private static boolean attached(World world, int x, int y, int z, int attached, String blockName, int meta)
	{
		if (attached <= 0)
		{
			return false;
		}

		String blockName2;
		int meta2;
		int i;

		for (i = 1; i <= attached; i++)
		{
		    BlockPos blockPos = new BlockPos(x + 1, y, z);
			blockName2 = Block.blockRegistry.getNameForObject(world.getBlockState(blockPos).getBlock()).toString();
			meta2 = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
			if (DefinitionMaps.getBlockDef(blockName2, meta2).supportiveBlock)
			{
				return true;
			}
			else if (!sameBlock(blockName, meta, blockName2, meta2))
			{
				break;
			}
		}
		
		for (i = 1; i <= attached; i++)
		{
		    BlockPos blockPos = new BlockPos(x - i, y, z);
			blockName2 = Block.blockRegistry.getNameForObject(world.getBlockState(blockPos).getBlock()).toString();
			meta2 = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
			if (DefinitionMaps.getBlockDef(blockName2, meta2).supportiveBlock)
			{
				return true;
			}
			else if (!sameBlock(blockName, meta, blockName2, meta2))
			{
				break;
			}
		}
		
		for (i = 1; i <= attached; i++)
		{
            BlockPos blockPos = new BlockPos(x, y, z + i);
            blockName2 = Block.blockRegistry.getNameForObject(world.getBlockState(blockPos).getBlock()).toString();
			meta2 = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
			if (DefinitionMaps.getBlockDef(blockName2, meta2).supportiveBlock)
			{
				return true;
			}
			else if (!sameBlock(blockName, meta, blockName2, meta2))
			{
				break;
			}
		}
		
		for (i = 1; i <= attached; i++)
		{
            BlockPos blockPos = new BlockPos(x, y, z - i);
            blockName2 = Block.blockRegistry.getNameForObject(world.getBlockState(blockPos).getBlock()).toString();
            meta2 = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
			if (DefinitionMaps.getBlockDef(blockName2, meta2).supportiveBlock)
			{
				return true;
			}
			else if (!sameBlock(blockName, meta, blockName2, meta2))
			{
				break;
			}
		}
		return false;
	}

	private static boolean nCorbel(World world, int x, int y, int z, int nCorbel)
	{
		if (nCorbel <= 0)
		{
			return false;
		}
		int i;

		for (i = 1; i <= nCorbel; i++)
		{
			if (isSupportiveBlock(world, x - i, y, z))
			{
				if (isSupportiveBlock(world, x - i, y - 1, z))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		for (i = 1; i <= nCorbel; i++)
		{
			if (isSupportiveBlock(world, x + i, y, z))
			{
				if (isSupportiveBlock(world, x + i, y - 1, z))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		for (i = 1; i <= nCorbel; i++)
		{
			if (isSupportiveBlock(world, x, y, z + i))
			{
				if (isSupportiveBlock(world, x, y - 1, z + i))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		for (i = 1; i <= nCorbel; i++)
		{
			if (isSupportiveBlock(world, x, y, z - i))
			{
				if (isSupportiveBlock(world, x, y - 1, z - i))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		return false;
	}

	private static boolean corbel(World world, int x, int y, int z, int corbel, String blockName, int meta)
	{
		if (corbel <= 0 || !DefinitionMaps.getBlockDef(blockName, meta).supportiveBlock)
		{
			return false;
		}
		int i;

		for (i = 1; i <= corbel; i++)
		{
			if (sameBlock(world, x + i, y, z, blockName, meta))
			{
				if (sameBlock(world, x + i, y - 1, z, blockName, meta))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		for (i = 1; i <= corbel; i++)
		{
			if (sameBlock(world, x - i, y, z, blockName, meta))
			{
				if (sameBlock(world, x - i, y - 1, z, blockName, meta))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		for (i = 1; i <= corbel; i++)
		{
			if (sameBlock(world, x, y, z + i, blockName, meta))
			{
				if (sameBlock(world, x, y - 1, z + i, blockName, meta))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		for (i = 1; i <= corbel; i++)
		{
			if (sameBlock(world, x, y, z - i, blockName, meta))
			{
				if (sameBlock(world, x, y - 1, z - i, blockName, meta))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		return false;
	}

	private static boolean ceiling(World world, int x, int y, int z)
	{
		if (isSupportiveBlock(world, x - 1, y, z) && 
		        isSupportiveBlock(world, x + 1, y, z))
		{
			return true;
		}
		if (isSupportiveBlock(world, x, y, z - 1) &&
		isSupportiveBlock(world, x, y, z + 1))
		{
			return true;
		}
		if (isSupportiveBlock(world, x - 1, y, z - 1) &&
		    isSupportiveBlock(world, x + 1, y, z + 1))
		{
			return true;
		}
		if (isSupportiveBlock(world, x - 1, y, z + 1) && 
		        isSupportiveBlock(world, x + 1, y, z - 1))
		{
			return true;
		}
		return false;
	}

	private static boolean smallArc(World world, int x, int y, int z, int smallArc)
	{
		if (smallArc <= 0)
		{
			return false;
		}
		if (isSupportiveBlock(world, x - 1, y, z) &&
		    isSupportiveBlock(world, x + 1, y, z))
		{
			if (isSupportiveBlock(world, x - 1, y - 1, z) &&
			    isSupportiveBlock(world, x + 1, y - 1, z))
			{
				return true;
			}
			if (smallArc > 1)
			{
				int i;
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x - i, y, z))
					{
						if (isSupportiveBlock(world, x - i, y - 1, z))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x + i, y, z))
					{
						if (isSupportiveBlock(world, x + i, y - 1, z))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
		}
		if (isSupportiveBlock(world, x, y, z - 1) && 
		        isSupportiveBlock(world, x, y, z + 1))
		{
			if (isSupportiveBlock(world, x, y - 1, z - 1) &&
			        isSupportiveBlock(world, x, y - 1, z + 1))
			{
				return true;
			}
			if (smallArc > 1)
			{
				int i;
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x, y, z - i))
					{
						if (isSupportiveBlock(world, x, y - 1, z - i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x, y, z + i))
					{
						if (isSupportiveBlock(world, x, y - 1, z + i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
		}
		if (isSupportiveBlock(world, x - 1, y, z + 1) && 
		        isSupportiveBlock(world, x + 1, y, z - 1))
		{
			if (isSupportiveBlock(world, x - 1, y - 1, z + 1) &&
			        isSupportiveBlock(world, x + 1, y - 1, z - 1))
			{
				return true;
			}
			if (smallArc > 1)
			{
				int i;
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x - i, y, z + i))
					{
						if (isSupportiveBlock(world, x - i, y - 1, z + i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x + i, y, z - i))
					{
						if (isSupportiveBlock(world, x + i, y - 1, z - i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
		}
		if (isSupportiveBlock(world, x + 1, y, z + 1) && 
		        isSupportiveBlock(world, x - 1, y, z - 1))
		{
			if (isSupportiveBlock(world, x + 1, y - 1, z + 1) && 
			        isSupportiveBlock(world, x - 1, y - 1, z - 1))
			{
				return true;
			}
			if (smallArc > 1)
			{
				int i;
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x + i, y, z + i))
					{
						if (isSupportiveBlock(world, x + i, y - 1, z + i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
				for (i = 2; i <= smallArc; i++)
				{
					if (isSupportiveBlock(world, x - i, y, z - i))
					{
						if (isSupportiveBlock(world, x - i, y - 1, z - i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
		}
		return false;
	}

	private static boolean bigArc(World world, int x, int y, int z, int bigArc)
	{
		if (bigArc <= 0)
		{
			return false;
		}
		if (!isSupportiveBlock(world, x, y + 1, z))
		{
			int i;
			if (isSupportiveBlock(world, x + 1, y + 1, z))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x - i, y, z))
					{
						if (isSupportiveBlock(world, x - i, y - 1, z))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
			if (isSupportiveBlock(world, x - 1, y + 1, z))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x + i, y, z))
					{
						if (isSupportiveBlock(world, x + i, y - 1, z))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
			if (isSupportiveBlock(world, x, y + 1, z + 1))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x, y, z - i))
					{
						if (isSupportiveBlock(world, x, y - 1, z - i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
			if (isSupportiveBlock(world, x, y + 1, z - 1))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x, y, z + i))
					{
						if (isSupportiveBlock(world, x, y - 1, z + i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
			if (isSupportiveBlock(world, x + 1, y + 1, z + 1))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x - i, y, z - i))
					{
						if (isSupportiveBlock(world, x - i, y - 1, z - i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
			if (isSupportiveBlock(world, x - 1, y + 1, z - 1))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x + i, y, z + i))
					{
						if (isSupportiveBlock(world, x + i, y - 1, z + i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
			if (isSupportiveBlock(world, x + 1, y + 1, z - 1))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x - i, y, z + i))
					{
						if (isSupportiveBlock(world, x - i, y - 1, z + i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
			if (isSupportiveBlock(world, x - 1, y + 1, z + 1))
			{
				for (i = 1; i <= bigArc; i++)
				{
					if (isSupportiveBlock(world, x + i, y, z - i))
					{
						if (isSupportiveBlock(world, x + i, y - 1, z - i))
						{
							return true;
						}
					}
					else
					{
						break;
					}
				}
			}
		}
		return false;
	}

	private static boolean branch(World world, int x, int y, int z, String blockName, int meta)
	{
		if (sameBlock(world, x + 1, y - 1, z, blockName, meta))
		{
			return true;
		}
		if (sameBlock(world, x - 1, y - 1, z, blockName, meta))
		{
			return true;
		}
		if (sameBlock(world, x, y - 1, z + 1, blockName, meta))
		{
			return true;
		}
		if (sameBlock(world, x, y - 1, z - 1, blockName, meta))
		{
			return true;
		}
		if (sameBlock(world, x + 1, y - 1, z + 1, blockName, meta))
		{
			return true;
		}
		if (sameBlock(world, x - 1, y - 1, z - 1, blockName, meta))
		{
			return true;
		}
		if (sameBlock(world, x - 1, y - 1, z + 1, blockName, meta))
		{
			return true;
		}
		if (sameBlock(world, x + 1, y - 1, z - 1, blockName, meta))
		{
			return true;
		}
		return false;
	}
	
	private static boolean tree(World world, int x, int y, int z, int tree, String blockName, int meta)
	{
		if (tree <= 0)
		{
			return false;
		}

		int i;
		int i2;

		for (i = 1; i <= tree; i++)
		{
			if (isSupportiveBlock(world, x + i, y, z))
			{
				return true;
			}
			else if (!sameBlock(world, x + i, y, z, blockName, meta))
			{
				break;
			}
			else
			{
			    if (isSupportiveBlock(world, x + i, y + 1, z))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x + i, y - 1, z))
				{
					return true;
				}
				if (isSupportiveBlock(world, x + i, y, z + 1))
				{
					return true;
				}
				if (isSupportiveBlock(world, x + i, y, z - 1))
				{
					return true;
				}
			}
		}
		
		for (i = 1; i <= tree; i++)
		{
		    if (isSupportiveBlock(world, x - i, y, z))
			{
				return true;
			}
			else if (!sameBlock(world, x - i, y, z, blockName, meta))
			{
				break;
			}
			else
			{
			    if (isSupportiveBlock(world, x - i, y + 1, z))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x - i, y - 1, z))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x - i, y, z + 1))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x - i, y, z - 1))
				{
					return true;
				}
			}
		}
		
		for (i = 1; i <= tree; i++)
		{
		    if (isSupportiveBlock(world, x, y, z + i))
			{
				return true;
			}
			else if (!sameBlock(world, x, y, z + i, blockName, meta))
			{
				break;
			}
			else
			{
			    if (isSupportiveBlock(world, x, y + 1, z + i))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x, y - 1, z + i))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x + 1, y, z + i))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x - 1, y, z + i))
				{
					return true;
				}
			}
		}
		
		for (i = 1; i <= tree; i++)
		{
		    if (isSupportiveBlock(world, x, y, z - i))
			{
				return true;
			}
			else if (!sameBlock(world, x, y, z - i, blockName, meta))
			{
				break;
			}
			else
			{
			    if (isSupportiveBlock(world, x, y + 1, z - i))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x, y - 1, z - i))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x + 1, y, z - i))
				{
					return true;
				}
			    if (isSupportiveBlock(world, x - 1, y, z - i))
				{
					return true;
				}
			}
		}
		return false;
	}
    private static boolean sameBlock(World world, int x, int y, int z, String name, int meta)
    {
        BlockPos blockPos = new BlockPos(x, y, z);
        IBlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        int blockMeta = block.getMetaFromState(blockState);
        String blockName = Block.blockRegistry.getNameForObject(block).toString();
    
        return sameBlock(blockName, blockMeta, name, meta);
    }
        
	private static boolean sameBlock(String blockName, int blockMetadata, String name, int meta)
	{
		if (blockName.equals(name))
		{
			if (blockName.equals("minecraft:leaves") || blockName.equals("minecraft:leaves2"))
			{
				return iunno(blockMetadata) == iunno(meta);
			}
		}
		return blockName.equals(name) && blockMetadata == meta;
	}
	
    private static boolean isSupportiveBlock(World world,int x,int y,int z)
    {
        BlockPos blockPos = new BlockPos(x, y, z);
        IBlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        int blockMeta = block.getMetaFromState(blockState);
        String blockName = Block.blockRegistry.getNameForObject(block).toString();
        
        return DefinitionMaps.getBlockDef(blockName, blockMeta).supportiveBlock;
    }

	private static int iunno(int i)
	{
		while (i > 3)
		{
			i -= 4;
		}
		return i;
	}
}