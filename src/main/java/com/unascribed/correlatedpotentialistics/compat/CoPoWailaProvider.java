package com.unascribed.correlatedpotentialistics.compat;

import java.util.List;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.helper.Numbers;
import com.unascribed.correlatedpotentialistics.item.ItemDrive;
import com.unascribed.correlatedpotentialistics.tile.TileEntityController;
import com.unascribed.correlatedpotentialistics.tile.TileEntityDriveBay;
import com.unascribed.correlatedpotentialistics.tile.TileEntityInterface;
import com.unascribed.correlatedpotentialistics.tile.TileEntityNetworkMember;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class CoPoWailaProvider implements IWailaDataProvider {

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound nbt, World world, BlockPos pos) {
		if (te instanceof TileEntityController) {
			TileEntityController tec = (TileEntityController)te;
			nbt.setInteger("Energy", tec.getEnergyStored(EnumFacing.UP));
			nbt.setInteger("MaxEnergy", tec.getMaxEnergyStored(EnumFacing.UP));
			if (tec.error && tec.errorReason != null) {
				nbt.setString("ErrorReason", tec.errorReason);
			} else if (tec.booting) {
				nbt.setInteger("BootTicks", tec.bootTicks);
			}
		}
		if (te instanceof TileEntityNetworkMember) {
			nbt.setInteger("EnergyPerTick", ((TileEntityNetworkMember) te).getEnergyConsumedPerTick());
			nbt.setBoolean("HasController", ((TileEntityNetworkMember) te).hasController());
		}
		return nbt;
	}

	@Override
	public List<String> getWailaBody(ItemStack stack, List<String> body, IWailaDataAccessor access, IWailaConfigHandler config) {
		NBTTagCompound nbt = access.getNBTData();
		if (access.getBlock() == CoPo.controller) {
			if (nbt.hasKey("ErrorReason")) {
				body.add("\u00A7c"+I18n.format("tooltip.correlatedpotentialistics.controller_error."+nbt.getString("ErrorReason")));
			} else if (nbt.hasKey("BootTicks") && nbt.getInteger("Energy") >= nbt.getInteger("EnergyPerTick")) {
				int bootTicks = nbt.getInteger("BootTicks");
				if (bootTicks < 0) {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_booting.hard"));
				} else {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_booting"));
				}
				int seconds;
				if (bootTicks >= 0) {
					seconds = (100-bootTicks)/20;
				} else {
					seconds = ((bootTicks*-1)+100)/20;
				}
				if (seconds == 1) {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_boot_eta_one"));
				} else {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_boot_eta", seconds));
				}
			}
			body.add(I18n.format("tooltip.correlatedpotentialistics.controller_consumption_rate", nbt.getInteger("EnergyPerTick")));
			body.add(I18n.format("tooltip.correlatedpotentialistics.controller_energy_buffer", nbt.getInteger("Energy"), nbt.getInteger("MaxEnergy")));
		} else if (access.getTileEntity() instanceof TileEntityNetworkMember) {
			if (nbt.getBoolean("HasController")) {
				body.add(I18n.format("tooltip.correlatedpotentialistics.member_consumption_rate", nbt.getInteger("EnergyPerTick")));
			} else {
				body.add("\u00A7c"+I18n.format("tooltip.correlatedpotentialistics.no_controller"));
			}
		}
		if (access.getTileEntity() instanceof TileEntityDriveBay) {
			TileEntityDriveBay tedb = (TileEntityDriveBay)access.getTileEntity();
			int totalBytesUsed = 0;
			int totalMaxBytes = 0;
			int totalTypesUsed = 0;
			int totalMaxTypes = 0;
			int driveCount = 0;
			for (int i = 0; i < 8; i++) {
				if (tedb.hasDriveInSlot(i)) {
					driveCount++;
					ItemStack is = tedb.getDriveInSlot(i);
					if (is.getItem() instanceof ItemDrive && is.getItemDamage() != 4) {
						totalBytesUsed += ((ItemDrive)is.getItem()).getBitsUsed(is)/8;
						totalTypesUsed += ((ItemDrive)is.getItem()).getTypesUsed(is);
						totalMaxBytes += ((ItemDrive)is.getItem()).getMaxBits(is)/8;
						totalMaxTypes += ((ItemDrive)is.getItem()).getMaxTypes(is);
					}
				}
			}
			
			int totalTypesPercent = (int)(((double)totalTypesUsed/(double)totalMaxTypes)*100);
			int totalBytesPercent = (int)(((double)totalBytesUsed/(double)totalMaxBytes)*100);
			
			body.add(I18n.format("tooltip.correlatedpotentialistics.drive_count", driveCount));
			body.add(I18n.format("tooltip.correlatedpotentialistics.types_used", totalTypesUsed, totalMaxTypes, totalTypesPercent));
			body.add(I18n.format("tooltip.correlatedpotentialistics.bytes_used", Numbers.humanReadableBytes(totalBytesUsed), Numbers.humanReadableBytes(totalMaxBytes), totalBytesPercent));
		} else if (access.getTileEntity() instanceof TileEntityInterface) {
			TileEntityInterface tei = (TileEntityInterface)access.getTileEntity();
			EnumFacing side = access.getSide();
			body.add(I18n.format("tooltip.correlatedpotentialistics.side", I18n.format("direction.correlatedpotentialistics."+side.getName())));
			body.add(I18n.format("tooltip.correlatedpotentialistics.mode", I18n.format("tooltip.correlatedpotentialistics.iface.mode_"+tei.getModeForFace(side).getName())));
		}
		return body;
	}

	@Override
	public List<String> getWailaHead(ItemStack stack, List<String> head, IWailaDataAccessor access, IWailaConfigHandler config) {
		return head;
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor access, IWailaConfigHandler config) {
		if (access.getBlock() == CoPo.controller) {
			return new ItemStack(access.getBlock(), 1, access.getMetadata());
		} else {
			return new ItemStack(access.getBlock());
		}
	}

	@Override
	public List<String> getWailaTail(ItemStack stack, List<String> tail, IWailaDataAccessor access, IWailaConfigHandler config) {
		return tail;
	}

}
