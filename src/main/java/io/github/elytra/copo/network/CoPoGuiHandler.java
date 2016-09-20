package io.github.elytra.copo.network;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.WirelessTerminalVT;
import io.github.elytra.copo.client.gui.GuiAutomaton;
import io.github.elytra.copo.client.gui.GuiDrive;
import io.github.elytra.copo.client.gui.GuiImporterChest;
import io.github.elytra.copo.client.gui.GuiInterface;
import io.github.elytra.copo.client.gui.GuiVT;
import io.github.elytra.copo.entity.EntityAutomaton;
import io.github.elytra.copo.inventory.ContainerAutomaton;
import io.github.elytra.copo.inventory.ContainerDrive;
import io.github.elytra.copo.inventory.ContainerImporterChest;
import io.github.elytra.copo.inventory.ContainerInterface;
import io.github.elytra.copo.inventory.ContainerVT;
import io.github.elytra.copo.item.ItemDrive;
import io.github.elytra.copo.item.ItemWirelessTerminal;
import io.github.elytra.copo.tile.TileEntityImporterChest;
import io.github.elytra.copo.tile.TileEntityInterface;
import io.github.elytra.copo.tile.TileEntityVT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
			case 4: {
				Entity ent = world.getEntityByID(x);
				if (ent != null && ent instanceof EntityAutomaton) {
					return new ContainerAutomaton(player.inventory, player, (EntityAutomaton)ent);
				} else {
					CoPo.log.warn("Expected an Automaton, got {} instead", ent == null ? "null" : ent.getClass());
					break;
				}
			}
			case 5: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityImporterChest) {
					return new ContainerImporterChest(player.inventory, player, (TileEntityImporterChest)te);
				} else {
					CoPo.log.warn("Expected TileEntityImporterChest at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
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
			case 4: {
				Entity ent = world.getEntityByID(x);
				if (ent != null && ent instanceof EntityAutomaton) {
					return new GuiAutomaton(new ContainerAutomaton(player.inventory, player, (EntityAutomaton)ent));
				} else {
					CoPo.log.warn("Expected an Automaton, got {} instead", ent == null ? "null" : ent.getClass());
					break;
				}
			}
			case 5: {
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te instanceof TileEntityImporterChest) {
					return new GuiImporterChest(new ContainerImporterChest(player.inventory, player, (TileEntityImporterChest)te));
				} else {
					CoPo.log.warn("Expected TileEntityImporterChest at {}, {}, {} - got {} instead", x, y, z, te == null ? "null" : te.getClass());
					break;
				}
			}
		}
		return null;
	}

}
