package io.github.elytra.correlated.network;

import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.client.gui.GuiAutomaton;
import io.github.elytra.correlated.client.gui.GuiDrive;
import io.github.elytra.correlated.client.gui.GuiImporterChest;
import io.github.elytra.correlated.client.gui.GuiInterface;
import io.github.elytra.correlated.client.gui.GuiTerminal;
import io.github.elytra.correlated.entity.EntityAutomaton;
import io.github.elytra.correlated.inventory.ContainerAutomaton;
import io.github.elytra.correlated.inventory.ContainerDrive;
import io.github.elytra.correlated.inventory.ContainerImporterChest;
import io.github.elytra.correlated.inventory.ContainerInterface;
import io.github.elytra.correlated.inventory.ContainerTerminal;
import io.github.elytra.correlated.item.ItemDrive;
import io.github.elytra.correlated.item.ItemWirelessTerminal;
import io.github.elytra.correlated.storage.WirelessTerminal;
import io.github.elytra.correlated.tile.TileEntityImporterChest;
import io.github.elytra.correlated.tile.TileEntityInterface;
import io.github.elytra.correlated.tile.TileEntityTerminal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
					Correlated.log.warn("Expected TileEntityTerminal at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 1: {
				ItemStack drive = player.inventory.getStackInSlot(x);
				if (drive != null && drive.getItem() instanceof ItemDrive) {
					return new ContainerDrive(player.inventory, x, player);
				} else {
					Correlated.log.warn("Expected a drive, got {} instead", drive);
					break;
				}
			}
			case 2: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityInterface) {
					return new ContainerInterface(player.inventory, player, (TileEntityInterface)te);
				} else {
					Correlated.log.warn("Expected TileEntityInterface at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 3: {
				ItemStack terminal = player.inventory.getStackInSlot(x);
				if (terminal != null && terminal.getItem() instanceof ItemWirelessTerminal) {
					return new ContainerTerminal(player.inventory, player, new WirelessTerminal(world, player, (ItemWirelessTerminal)terminal.getItem(), terminal));
				} else {
					Correlated.log.warn("Expected a wireless terminal, got {} instead", terminal);
					break;
				}
			}
			case 4: {
				Entity ent = world.getEntityByID(x);
				if (ent != null && ent instanceof EntityAutomaton) {
					return new ContainerAutomaton(player.inventory, player, (EntityAutomaton)ent);
				} else {
					Correlated.log.warn("Expected an Automaton, got {} instead", ent == null ? "null" : ent.getClass());
					break;
				}
			}
			case 5: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityImporterChest) {
					return new ContainerImporterChest(player.inventory, player, (TileEntityImporterChest)te);
				} else {
					Correlated.log.warn("Expected TileEntityImporterChest at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
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
					Correlated.log.warn("Expected TileEntityTerminal at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 1: {
				ItemStack drive = player.inventory.getStackInSlot(x);
				if (drive != null && drive.getItem() instanceof ItemDrive) {
					return new GuiDrive(new ContainerDrive(player.inventory, x, player));
				} else {
					Correlated.log.warn("Expected a drive, got {} instead", drive);
					break;
				}
			}
			case 2: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityInterface) {
					return new GuiInterface(new ContainerInterface(player.inventory, player, (TileEntityInterface)te));
				} else {
					Correlated.log.warn("Expected TileEntityInterface at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
			case 3: {
				ItemStack terminal = player.inventory.getStackInSlot(x);
				if (terminal != null && terminal.getItem() instanceof ItemWirelessTerminal) {
					return new GuiTerminal(new ContainerTerminal(player.inventory, player, new WirelessTerminal(world, player, (ItemWirelessTerminal)terminal.getItem(), terminal)));
				} else {
					Correlated.log.warn("Expected a wireless terminal, got {} instead", terminal);
					break;
				}
			}
			case 4: {
				Entity ent = world.getEntityByID(x);
				if (ent != null && ent instanceof EntityAutomaton) {
					return new GuiAutomaton(new ContainerAutomaton(player.inventory, player, (EntityAutomaton)ent));
				} else {
					Correlated.log.warn("Expected an Automaton, got {} instead", ent == null ? "null" : ent.getClass());
					break;
				}
			}
			case 5: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityImporterChest) {
					return new GuiImporterChest(new ContainerImporterChest(player.inventory, player, (TileEntityImporterChest)te));
				} else {
					Correlated.log.warn("Expected TileEntityImporterChest at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
		}
		return null;
	}

}
