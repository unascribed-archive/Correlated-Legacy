package com.elytradev.correlated.network;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.client.gui.GuiAutomaton;
import com.elytradev.correlated.client.gui.GuiDrive;
import com.elytradev.correlated.client.gui.GuiImporterChest;
import com.elytradev.correlated.client.gui.GuiInterface;
import com.elytradev.correlated.client.gui.GuiTerminal;
import com.elytradev.correlated.entity.EntityAutomaton;
import com.elytradev.correlated.inventory.ContainerAutomaton;
import com.elytradev.correlated.inventory.ContainerDrive;
import com.elytradev.correlated.inventory.ContainerImporterChest;
import com.elytradev.correlated.inventory.ContainerInterface;
import com.elytradev.correlated.inventory.ContainerTerminal;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.item.ItemHandheldTerminal;
import com.elytradev.correlated.storage.WirelessTerminal;
import com.elytradev.correlated.tile.TileEntityInterface;
import com.elytradev.correlated.tile.TileEntityTerminal;
import com.elytradev.correlated.tile.importer.TileEntityImporterChest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CorrelatedGuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 0: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityTerminal) {
					return new ContainerTerminal(player.inventory, player, (TileEntityTerminal)te);
				} else {
					CLog.warn("Expected TileEntityTerminal at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 1: {
				Container open = player.openContainer;
				if (open != null && open instanceof ContainerTerminal) {
					ItemStack drive = ((ContainerTerminal)open).terminal.getMaintenanceSlotContent();
					if (drive.getItem() instanceof ItemDrive) {
						return new ContainerDrive((ContainerTerminal)open, player.inventory, player);
					} else {
						CLog.warn("Expected a drive, got {} instead", drive);
						break;
					}
				} else {
					CLog.warn("Expected a ContainerTerminal, got {} instead", open == null ? "null" : open.getClass());
					break;
				}
			}
			case 2: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityInterface) {
					return new ContainerInterface(player.inventory, player, (TileEntityInterface)te);
				} else {
					CLog.warn("Expected TileEntityInterface at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 3: {
				ItemStack terminal = x == -1 ? player.inventory.offHandInventory.get(0) : player.inventory.getStackInSlot(x);
				if (terminal.getItem() instanceof ItemHandheldTerminal) {
					return new ContainerTerminal(player.inventory, player, new WirelessTerminal(world, player, (ItemHandheldTerminal)terminal.getItem(), terminal));
				} else {
					CLog.warn("Expected a wireless terminal, got {} instead", terminal);
					break;
				}
			}
			case 4: {
				Entity ent = world.getEntityByID(x);
				if (ent != null && ent instanceof EntityAutomaton) {
					return new ContainerAutomaton(player.inventory, player, (EntityAutomaton)ent);
				} else {
					CLog.warn("Expected an Automaton, got {} instead", ent == null ? "null" : ent.getClass());
					break;
				}
			}
			case 5: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityImporterChest) {
					return new ContainerImporterChest(player.inventory, player, (TileEntityImporterChest)te);
				} else {
					CLog.warn("Expected TileEntityImporterChest at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
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
				if (te instanceof TileEntityTerminal) {
					return new GuiTerminal(new ContainerTerminal(player.inventory, player, (TileEntityTerminal)te));
				} else {
					CLog.warn("Expected TileEntityTerminal at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 1: {
				GuiScreen open = Minecraft.getMinecraft().currentScreen;
				if (open != null && open instanceof GuiTerminal) {
					ContainerTerminal ct = ((ContainerTerminal)((GuiTerminal)open).inventorySlots);
					ItemStack drive = ct.maintenanceSlot.getStack();
					if (drive.getItem() instanceof ItemDrive) {
						return new GuiDrive((GuiTerminal)open, new ContainerDrive(ct, player.inventory, player));
					} else {
						CLog.warn("Expected a drive, got {} instead", drive);
						break;
					}
				} else {
					CLog.warn("Expected a GuiTerminal, got {} instead", open == null ? "null" : open.getClass());
					break;
				}
			}
			case 2: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityInterface) {
					return new GuiInterface(new ContainerInterface(player.inventory, player, (TileEntityInterface)te));
				} else {
					CLog.warn("Expected TileEntityInterface at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 3: {
				ItemStack terminal = x == -1 ? player.inventory.offHandInventory.get(0) : player.inventory.getStackInSlot(x);
				if (terminal.getItem() instanceof ItemHandheldTerminal) {
					return new GuiTerminal(new ContainerTerminal(player.inventory, player, new WirelessTerminal(world, player, (ItemHandheldTerminal)terminal.getItem(), terminal)));
				} else {
					CLog.warn("Expected a wireless terminal, got {} instead", terminal);
					break;
				}
			}
			case 4: {
				Entity ent = world.getEntityByID(x);
				if (ent != null && ent instanceof EntityAutomaton) {
					return new GuiAutomaton(new ContainerAutomaton(player.inventory, player, (EntityAutomaton)ent));
				} else {
					CLog.warn("Expected an Automaton, got {} instead", ent == null ? "null" : ent.getClass());
					break;
				}
			}
			case 5: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityImporterChest) {
					return new GuiImporterChest(new ContainerImporterChest(player.inventory, player, (TileEntityImporterChest)te));
				} else {
					CLog.warn("Expected TileEntityImporterChest at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
		}
		return null;
	}

}
