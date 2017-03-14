package com.elytradev.correlated.tile;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.EnergyUnit;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.energy.IStrictEnergyAcceptor;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(modid="ic2", iface="ic2.api.energy.tile.IEnergySink")
public abstract class TileEntityEnergyAcceptor extends TileEntityNetworkMember implements IEnergySink, IStrictEnergyAcceptor {

	protected int potential;
	
	public abstract int getMaxPotential();
	public abstract int getReceiveCap();
	
	public abstract boolean canReceivePotential();
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		potential = compound.getInteger("Energy");
		if (potential > getMaxPotential()) potential = getMaxPotential();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setLong("Energy", potential);
		return compound;
	}
	
	public int receivePotential(int maxReceive, boolean simulate) {
		int energyReceived = Math.min(getMaxPotential() - potential,
				Math.min(getReceiveCap()+1, maxReceive));

		if (!simulate) {
			potential += energyReceived;
		}
		return energyReceived;
	}

	public int getPotentialStored() {
		return potential;
	}
	
	public void modifyEnergyStored(int energy) {
		potential += energy;
		if (potential > getMaxPotential()) {
			potential = getMaxPotential();
		} else if (potential < 0) {
			potential = 0;
		}
	}
	
	private Object forge;
	private Object tesla;
	private Object mj;
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == null) return false;
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		} else if (capability == Correlated.MJ_RECEIVER) {
			return true;
		} else if (capability == Correlated.TESLA_CONSUMER) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == null) return null;
		if (capability == CapabilityEnergy.ENERGY) {
			if (forge == null) forge = new ForgeEnergyStorage();
			return (T) forge;
		} else if (capability == Correlated.MJ_RECEIVER) {
			if (mj == null) mj = new MJReceiver();
			return (T) mj;
		} else if (capability == Correlated.TESLA_CONSUMER) {
			if (tesla == null) tesla = new TeslaConsumer();
			return (T) tesla;
		}
		return super.getCapability(capability, facing);
	}
	
	// IC2 BEGIN
	
	@Override
	public double getDemandedEnergy() {
		return Correlated.convertFromPotential(getMaxPotential()-getPotentialStored(), EnergyUnit.ENERGY_UNITS);
	}
	
	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
		return canReceivePotential();
	}
	
	@Override
	public int getSinkTier() {
		return Integer.MAX_VALUE;
	}
	
	// IC2 END
	
	// MEKANISM BEGIN
	
	@Override
	public boolean canReceiveEnergy(EnumFacing side) {
		return canReceivePotential();
	}
	
	@Override
	public double getEnergy() {
		return Correlated.convertFromPotential(getPotentialStored(), EnergyUnit.JOULES);
	}
	
	@Override
	public double getMaxEnergy() {
		return Correlated.convertFromPotential(getMaxPotential(), EnergyUnit.JOULES);
	}
	
	@Override
	public void setEnergy(double energy) {
		Correlated.log.warn("Energy content has been forcefully set via Mekanism's API - this is dangerous!");
		energy = Correlated.convertToPotential((int)energy, EnergyUnit.JOULES);
	}
	
	@Override
	public double transferEnergyToAcceptor(EnumFacing side, double amount) {
		return Correlated.convertFromPotential(receivePotential(Correlated.convertToPotential((int)amount, EnergyUnit.JOULES), false), EnergyUnit.JOULES);
	}
	
	// MEKANISM END
	
	@Override
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		int p = Correlated.convertToPotential((int)amount, EnergyUnit.ENERGY_UNITS);
		double leftover = amount-(Correlated.convertFromPotential(p, EnergyUnit.ENERGY_UNITS));
		int excess = p-receivePotential(p, false);
		return leftover+Correlated.convertFromPotential(excess, EnergyUnit.ENERGY_UNITS);
	}
	
	private class TeslaConsumer implements ITeslaConsumer {

		@Override
		public long givePower(long power, boolean simulate) {
			return Correlated.convertFromPotential(receivePotential(Correlated.convertToPotential(power, EnergyUnit.TESLA), simulate), EnergyUnit.TESLA);
		}

	}
	
	private class MJReceiver implements IMjReceiver {

		@Override
		public boolean canConnect(IMjConnector arg0) {
			return canReceivePotential();
		}

		@Override
		public long getPowerRequested() {
			return Correlated.convertFromPotential(getMaxPotential()-getPotentialStored(), EnergyUnit.MINECRAFT_JOULES)*1000000L;
		}

		@Override
		public long receivePower(long amt, boolean simulate) {
			int p = Correlated.convertToPotential(amt/1000000L, EnergyUnit.MINECRAFT_JOULES);
			long leftover = amt-(Correlated.convertFromPotential(p, EnergyUnit.MINECRAFT_JOULES)*1000000L);
			int excess = p-receivePotential(p, simulate);
			return leftover+(Correlated.convertFromPotential(excess, EnergyUnit.MINECRAFT_JOULES)*1000000L);
		}

	}
	
	private class ForgeEnergyStorage implements IEnergyStorage {

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return receivePotential(Correlated.convertToPotential(maxReceive, EnergyUnit.FORGE_UNITS), simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return Correlated.convertFromPotential(getPotentialStored(), EnergyUnit.FORGE_UNITS);
		}

		@Override
		public int getMaxEnergyStored() {
			return Correlated.convertFromPotential(getMaxPotential(), EnergyUnit.FORGE_UNITS);
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return canReceivePotential();
		}

	}
	

}
