package com.unascribed.correlatedpotentialistics.network;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.WirelessTerminalVT;
import com.unascribed.correlatedpotentialistics.client.gui.GuiDrive;
import com.unascribed.correlatedpotentialistics.client.gui.GuiInterface;
import com.unascribed.correlatedpotentialistics.client.gui.GuiVT;
import com.unascribed.correlatedpotentialistics.inventory.ContainerDrive;
import com.unascribed.correlatedpotentialistics.inventory.ContainerInterface;
import com.unascribed.correlatedpotentialistics.inventory.ContainerVT;
import com.unascribed.correlatedpotentialistics.item.ItemDrive;
import com.unascribed.correlatedpotentialistics.item.ItemWirelessTerminal;
import com.unascribed.correlatedpotentialistics.tile.TileEntityInterface;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CoPoGuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 0: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityVT) {
					return new ContainerVT(player.inventory, player, (TileEntityVT)te);
				} else {
					CoPo.log.warn("Expected TileEntityVT at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 1: {
				ItemStack drive = player.inventory.getStackInSlot(x);
				if (drive != null && drive.getItem() instanceof ItemDrive) {
					return new ContainerDrive(player.inventory, x, player);
				} else {
					CoPo.log.warn("Expected a drive, got {} instead", drive);
					break;
				}
			}
			case 2: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityInterface) {
					return new ContainerInterface(player.inventory, player, (TileEntityInterface)te);
				} else {
					CoPo.log.warn("Expected TileEntityInterface at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 3: {
				ItemStack terminal = player.inventory.getStackInSlot(x);
				if (terminal != null && terminal.getItem() instanceof ItemWirelessTerminal) {
					return new ContainerVT(player.inventory, player, new WirelessTerminalVT(world, player, (ItemWirelessTerminal)terminal.getItem(), terminal));
				} else {
					CoPo.log.warn("Expected a wireless terminal, got {} instead", terminal);
					break;
				}
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 0: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityVT) {
					return new GuiVT(new ContainerVT(player.inventory, player, (TileEntityVT)te));
				} else {
					CoPo.log.warn("Expected TileEntityVT at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 1: {
				ItemStack drive = player.inventory.getStackInSlot(x);
				if (drive != null && drive.getItem() instanceof ItemDrive) {
					return new GuiDrive(new ContainerDrive(player.inventory, x, player));
				} else {
					CoPo.log.warn("Expected a drive, got {} instead", drive);
					break;
				}
			}
			case 2: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityInterface) {
					return new GuiInterface(new ContainerInterface(player.inventory, player, (TileEntityInterface)te));
				} else {
					CoPo.log.warn("Expected TileEntityInterface at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 3: {
				ItemStack terminal = player.inventory.getStackInSlot(x);
				if (terminal != null && terminal.getItem() instanceof ItemWirelessTerminal) {
					return new GuiVT(new ContainerVT(player.inventory, player, new WirelessTerminalVT(world, player, (ItemWirelessTerminal)terminal.getItem(), terminal)));
				} else {
					CoPo.log.warn("Expected a wireless terminal, got {} instead", terminal);
					break;
				}
			}
		}
		return null;
	}

}
