package com.elytradev.correlated.tile;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.EnergyHelper;
import com.elytradev.correlated.EnergyUnit;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList(value={
	@Optional.Interface(modid="ic2", iface="ic2.api.energy.tile.IEnergySink"),
	@Optional.Interface(modid="redstoneflux", iface="cofh.redstoneflux.api.IEnergyReceiver")
})
public abstract class TileEntityAbstractEnergyAcceptor extends TileEntityNetworkMember
		implements IEnergySink, IEnergyReceiver {

	protected double potential;
	
	public abstract double getMaxPotential();
	public abstract double getReceiveCap();
	
	public abstract boolean canReceivePotential();
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		potential = compound.getDouble("Energy");
		if (potential > getMaxPotential()) potential = getMaxPotential();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setDouble("Energy", potential);
		return compound;
	}
	
	public double receivePotential(double maxReceive, boolean simulate) {
		double energyReceived = Math.min(getMaxPotential() - potential,
				Math.min(getReceiveCap()+1, maxReceive));

		if (!simulate) {
			potential += energyReceived;
		}
		return energyReceived;
	}

	public double getPotentialStored() {
		return potential;
	}
	
	public void modifyEnergyStored(double energy) {
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
	private Object joule;
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == null) return false;
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		} else if (capability == Correlated.MJ_RECEIVER) {
			return true;
		} else if (capability == Correlated.TESLA_CONSUMER) {
			return true;
		} else if (capability == Correlated.JOULE_ACCEPTOR || capability == Correlated.JOULE_STORAGE) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	@SuppressWarnings("unchecked")
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
		} else if (capability == Correlated.JOULE_ACCEPTOR || capability == Correlated.JOULE_STORAGE) {
			if (joule == null) joule = new JouleEnergyAcceptor();
			return (T) joule;
		}
		return super.getCapability(capability, facing);
	}
	
	// RF BEGIN
	
	@Override
	@Optional.Method(modid="redstoneflux")
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}
	
	@Override
	@Optional.Method(modid="redstoneflux")
	public int getEnergyStored(EnumFacing from) {
		return (int)EnergyHelper.convertFromPotential(getPotentialStored(), EnergyUnit.REDSTONE_FLUX);
	}
	
	@Override
	@Optional.Method(modid="redstoneflux")
	public int getMaxEnergyStored(EnumFacing from) {
		return (int)EnergyHelper.convertFromPotential(getMaxPotential(), EnergyUnit.REDSTONE_FLUX);
	}
	
	@Override
	@Optional.Method(modid="redstoneflux")
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		return (int)receivePotential(EnergyHelper.convertToPotential(maxReceive, EnergyUnit.REDSTONE_FLUX), simulate);
	}
	
	// RF END
	
	
	// IC2 BEGIN
	
	@Override
	@Optional.Method(modid="ic2")
	public double getDemandedEnergy() {
		return EnergyHelper.convertFromPotential(getMaxPotential()-getPotentialStored(), EnergyUnit.ENERGY_UNITS);
	}
	
	@Override
	@Optional.Method(modid="ic2")
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
		return canReceivePotential();
	}
	
	@Override
	@Optional.Method(modid="ic2")
	public int getSinkTier() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	@Optional.Method(modid="ic2")
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		double p = EnergyHelper.convertToPotential(amount, EnergyUnit.ENERGY_UNITS);
		double leftover = amount-(EnergyHelper.convertFromPotential(p, EnergyUnit.ENERGY_UNITS));
		double excess = p-receivePotential(p, false);
		return leftover+EnergyHelper.convertFromPotential(excess, EnergyUnit.ENERGY_UNITS);
	}
	
	@Override
	@Optional.Method(modid="ic2")
	public void onLoad() {
		if (!hasWorld() || !getWorld().isRemote) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
		}
	}
	
	@Override
	@Optional.Method(modid="ic2")
	public void onChunkUnload() {
		if (!hasWorld() || !getWorld().isRemote) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
	}
	
	// IC2 END
	
	
	private class TeslaConsumer implements ITeslaConsumer {

		@Override
		public long givePower(long power, boolean simulate) {
			return (long)EnergyHelper.convertFromPotential(receivePotential(EnergyHelper.convertToPotential(power, EnergyUnit.TESLA), simulate), EnergyUnit.TESLA);
		}

	}
	
	private class MJReceiver implements IMjReceiver {

		@Override
		public boolean canConnect(IMjConnector arg0) {
			return canReceivePotential();
		}

		@Override
		public long getPowerRequested() {
			return (long)(EnergyHelper.convertFromPotential(getMaxPotential()-getPotentialStored(), EnergyUnit.MINECRAFT_JOULES)*1000000D);
		}

		@Override
		public long receivePower(long amt, boolean simulate) {
			double p = EnergyHelper.convertToPotential(amt/1000000D, EnergyUnit.MINECRAFT_JOULES);
			double leftover = amt-(EnergyHelper.convertFromPotential(p, EnergyUnit.MINECRAFT_JOULES)*1000000D);
			double excess = p-receivePotential(p, simulate);
			return (long)(leftover+(EnergyHelper.convertFromPotential(excess, EnergyUnit.MINECRAFT_JOULES)*1000000D));
		}

	}
	
	private class ForgeEnergyStorage implements IEnergyStorage {

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return (int)receivePotential(EnergyHelper.convertToPotential(maxReceive, EnergyUnit.FORGE_UNITS), simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return (int)EnergyHelper.convertFromPotential(getPotentialStored(), EnergyUnit.FORGE_UNITS);
		}

		@Override
		public int getMaxEnergyStored() {
			return (int)EnergyHelper.convertFromPotential(getMaxPotential(), EnergyUnit.FORGE_UNITS);
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
	
	private class JouleEnergyAcceptor implements IStrictEnergyStorage, IStrictEnergyAcceptor {
		@Override
		public boolean canReceiveEnergy(EnumFacing side) {
			return canReceivePotential();
		}
		
		@Override
		public double getEnergy() {
			return EnergyHelper.convertFromPotential(getPotentialStored(), EnergyUnit.JOULES);
		}
		
		@Override
		public double getMaxEnergy() {
			return EnergyHelper.convertFromPotential(getMaxPotential(), EnergyUnit.JOULES);
		}
		
		@Override
		public void setEnergy(double energy) {
			CLog.warn("Energy content has been forcefully set via Mekanism's API - this is dangerous!");
			energy = EnergyHelper.convertToPotential(energy, EnergyUnit.JOULES);
		}
		
		@Override
		public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
			return EnergyHelper.convertFromPotential(receivePotential(EnergyHelper.convertToPotential(amount, EnergyUnit.JOULES), simulate), EnergyUnit.JOULES);
		}
	}
	

}
