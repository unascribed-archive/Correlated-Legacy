package com.elytradev.correlated.tile;

import java.util.List;
import java.util.Set;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockDriveBay;
import com.elytradev.correlated.block.BlockImporterChest;
import com.elytradev.correlated.block.BlockTerminal;
import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileEntityNetworkImporter extends TileEntityImporter {
	
	public TileEntityNetworkImporter() {
		super(3);
	}
	
	@Override
	protected void doImport() {
		if (!Correlated.inst.importNetworks) {
			Correlated.log.warn("Skipping import of old network at {}, {}, {}, this may cause glitches!", getPos().getX(), getPos().getY(), getPos().getZ());
			substitute(new TileEntityController(), true);
			return;
		}
		
		boolean justDelete = !(Correlated.inst.refundBlocks || Correlated.inst.refundContent || Correlated.inst.refundDriveComponents);
		if (justDelete) {
			Correlated.log.info("DELETING OLD NETWORK AT {}, {}, {}, as requested in config!", getPos().getX(), getPos().getY(), getPos().getZ());
		} else {
			Correlated.log.info("Scanning old network at {}, {}, {}...", getPos().getX(), getPos().getY(), getPos().getZ());
		}
		Set<BlockPos> seen = Sets.newHashSet();
		List<BlockPos> queue = Lists.newArrayList(getPos());
		
		Set<BlockPos> delete = Sets.newHashSet();

		List<ItemStack> blockRefunds = Lists.newArrayList();
		List<ItemStack> contentRefunds = Lists.newArrayList();
		
		Multiset<EnumFacing> directions = EnumMultiset.create(EnumFacing.class);
		
		blockRefunds.add(new ItemStack(Correlated.controller));
		
		int refundIron = 0;
		int refundCeramicPlatters = 0;
		int refundMetallicPlatters = 0;
		int refundPearls = 0;
		int refundDiamonds = 0;
		
		while (!queue.isEmpty()) {
			BlockPos pos = queue.remove(0);
			seen.add(pos);
			TileEntity te = getWorld().getTileEntity(pos);
			if (te instanceof TileEntityNetworkMember || te == this) {
				for (EnumFacing ef : EnumFacing.VALUES) {
					BlockPos p = pos.offset(ef);
					if (seen.contains(p)) continue;
					if (world.getTileEntity(p) == null) {
						seen.add(p);
						continue;
					}
					queue.add(p);
				}
				if (te != this) {
					if (te instanceof TileEntityDriveBay) {
						if (Correlated.inst.refundContent) {
							for (ItemStack is : ((TileEntityDriveBay)te)) {
								if (is.getMetadata() != 4) {
									contentRefunds.add(toCore(is));
									switch (is.getItemDamage()) {
										case 0: {
											refundIron += 7;
											refundCeramicPlatters += 1;
											break;
										}
										case 1: {
											refundIron += 5;
											refundCeramicPlatters += 3;
											break;
										}
										case 2: {
											refundIron += 5;
											refundDiamonds += 2;
											refundMetallicPlatters += 1;
											break;
										}
										case 3: {
											refundDiamonds += 3;
											refundPearls += 1;
											refundMetallicPlatters += 4;
											break;
										}
									}
								} else {
									// refund the void drive as-is, it doesn't need changing
									contentRefunds.add(is);
								}
							}
						}
						blockRefunds.add(new ItemStack(Correlated.drive_bay));
						directions.add(world.getBlockState(pos).getValue(BlockDriveBay.FACING));
						delete.add(pos);
					} else if (te instanceof TileEntityTerminal) {
						blockRefunds.add(new ItemStack(Correlated.terminal));
						directions.add(world.getBlockState(pos).getValue(BlockTerminal.FACING));
						delete.add(pos);
					} else if (te instanceof TileEntityWirelessReceiver) {
						blockRefunds.add(new ItemStack(Correlated.wireless_endpoint, 1, 0));
						delete.add(pos);
					} else if (te instanceof TileEntityWirelessTransmitter) {
						blockRefunds.add(new ItemStack(Correlated.wireless_endpoint, 1, 1));
						delete.add(pos);
					} else if (te instanceof TileEntityInterface) {
						TileEntityInterface tei = (TileEntityInterface)te;
						for (int i = 0; i > tei.getSizeInventory(); i++) {
							ItemStack is = tei.getStackInSlot(i);
							if (is != null) {
								contentRefunds.add(is);
							}
						}
						blockRefunds.add(new ItemStack(Correlated.iface));
						delete.add(pos);
					} else if (te instanceof TileEntityController || te instanceof TileEntityNetworkImporter) {
						blockRefunds.add(new ItemStack(Correlated.controller));
						delete.add(pos);
					} else if (te instanceof TileEntityMemoryBay) {
						Correlated.log.info("Aborting import. Found a memory bay, which is indicative of a new network");
						substitute(new TileEntityController(), true);
						return;
					}
				}
			}
		}
		for (BlockPos d : delete) {
			world.removeTileEntity(d);
			world.setBlockToAir(d);
		}
		if (justDelete) {
			Correlated.log.info("Sucessfully deleted old network at {}, {}, {}", getPos().getX(), getPos().getY(), getPos().getZ());
			substitute(Blocks.AIR.getDefaultState(), null, false);
			return;
		}
		TileEntityImporterChest chest = new TileEntityImporterChest();
		EnumFacing facing = EnumFacing.NORTH;
		int weight = 0;
		for (Multiset.Entry<EnumFacing> en : directions.entrySet()) {
			if (en.getCount() > weight) {
				facing = en.getElement();
				weight = en.getCount();
			}
		}
		if (Correlated.inst.refundDriveComponents) {
			if (refundIron > 0) chest.addItemToNetwork(new ItemStack(Items.IRON_INGOT, refundIron));
			if (refundCeramicPlatters > 0) chest.addItemToNetwork(new ItemStack(Correlated.misc, refundCeramicPlatters, 1));
			if (refundMetallicPlatters > 0) chest.addItemToNetwork(new ItemStack(Correlated.misc, refundMetallicPlatters, 2));
			if (refundPearls > 0) chest.addItemToNetwork(new ItemStack(Correlated.misc, refundPearls, 3));
			if (refundDiamonds > 0) chest.addItemToNetwork(new ItemStack(Items.DIAMOND, refundDiamonds));
		}
		if (Correlated.inst.refundBlocks) {
			for (ItemStack is : blockRefunds) {
				chest.addItemToNetwork(is);
			}
		}
		if (Correlated.inst.refundContent) {
			for (ItemStack is : contentRefunds) {
				chest.addItemToNetwork(is);
			}
		}
		int sum = 0;
		for (int i = 0; i < chest.getSizeInventory(); i++) {
			ItemStack is = chest.getStackInSlot(i);
			if (is != null) {
				sum += is.getCount();
			}
		}
		Correlated.log.info("Sucessfully imported old network at {}, {}, {}, refunding {} items", getPos().getX(), getPos().getY(), getPos().getZ(), sum);
		substitute(Correlated.importer_chest.getDefaultState().withProperty(BlockImporterChest.FACING, facing), chest, false);
	}

	private ItemStack toCore(ItemStack is) {
		ItemStack core = new ItemStack(Correlated.misc, 1, 8);
		if (is.hasTagCompound()) {
			core.setTagCompound(is.getTagCompound().copy());
		} else {
			core.setTagCompound(new NBTTagCompound());
		}
		return core;
	}

}
