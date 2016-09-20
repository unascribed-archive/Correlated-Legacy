package io.github.elytra.copo.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public abstract class TileEntityImporter extends TileEntity implements ITickable {

	private NBTTagCompound capturedNbt;
	private int ticks = 0;
	
	private final int triggerTicks;
	
	public TileEntityImporter(int triggerTicks) {
		this.triggerTicks = triggerTicks;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		capturedNbt = compound;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.merge(capturedNbt);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if (!hasWorldObj()) return;
		if (worldObj.isRemote) return;
		ticks++;
		if (ticks <= triggerTicks) return;
		doImport();
	}
	
	protected abstract void doImport();
	
	protected void substitute(TileEntity te, boolean importOldNbt) {
		if (importOldNbt) {
			te.readFromNBT(capturedNbt);
		}
		worldObj.setTileEntity(getPos(), te);
	}
	
	protected void substitute(IBlockState state, TileEntity te, boolean importOldNbt) {
		if (importOldNbt) {
			te.readFromNBT(capturedNbt);
		}
		worldObj.setBlockState(getPos(), state);
		worldObj.setTileEntity(getPos(), te);
	}

}
