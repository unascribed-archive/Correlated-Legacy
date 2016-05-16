package com.unascribed.correlatedpotentialistics.tile;

import com.unascribed.correlatedpotentialistics.CoPo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class TileEntityNetworkMember extends TileEntity {
	private TileEntityController controller;
	private Vec3i controllerPos;

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (controllerPos != null) {
			compound.setInteger("ControllerOffsetX", controllerPos.getX());
			compound.setInteger("ControllerOffsetY", controllerPos.getY());
			compound.setInteger("ControllerOffsetZ", controllerPos.getZ());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("ControllerOffsetX")) {
			controllerPos = new Vec3i(
					compound.getInteger("ControllerOffsetX"),
					compound.getInteger("ControllerOffsetY"),
					compound.getInteger("ControllerOffsetZ"));
		}
	}

	public boolean hasController() {
		return getController() != null;
	}

	public TileEntityController getController() {
		if (!hasWorldObj()) return null;
		if (controller != null && controller.isInvalid()) controller = null;
		if (controller == null && controllerPos != null) {
			BlockPos pos = getPos().add(controllerPos);
			TileEntity te = getWorld().getTileEntity(pos);
			if (te instanceof TileEntityController) {
				controller = (TileEntityController)te;
			} else {
				controllerPos = null;
				CoPo.log.warn("The network member at {}, {}, {} failed to find it's controller", getPos().getX(), getPos().getY(), getPos().getZ());
			}
		}
		return controller;
	}
	public void setController(TileEntityController controller) {
		if (controller == null) {
			controllerPos = null;
		} else {
			controllerPos = controller.getPos().subtract(getPos());
		}
		this.controller = controller;
		if (controller != null) {
			for (EnumFacing ef : EnumFacing.VALUES) {
				TileEntity neighbor = worldObj.getTileEntity(getPos().offset(ef));
				if (neighbor instanceof TileEntityNetworkMember) {
					TileEntityNetworkMember tenm = (TileEntityNetworkMember)neighbor;
					if (!tenm.hasController() && this.hasController()) {
						tenm.setController(controller);
						controller.updateConsumptionRate(tenm.getEnergyConsumedPerTick());
						controller.onNetworkPatched(tenm);
					}
				}
			}
		}
	}


	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public abstract int getEnergyConsumedPerTick();

	public void handleNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		TileEntity te = world.getTileEntity(neighbor);
		if (te instanceof TileEntityNetworkMember) {
			TileEntityNetworkMember tenm = (TileEntityNetworkMember)te;
			if (!tenm.hasController() && this.hasController()) {
				tenm.setController(this.getController());
				getController().updateConsumptionRate(tenm.getEnergyConsumedPerTick());
				getController().onNetworkPatched(tenm);
			}
		} else {
			if (hasController()) {
				if (getController().knowsOfMemberAt(neighbor)) {
					getController().scanNetwork();
				}
			}
		}
	}

}
