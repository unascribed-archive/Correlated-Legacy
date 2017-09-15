package com.elytradev.correlated.network.inventory;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.field.Optional;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.CLog;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.inventory.ContainerTerminal;
import com.elytradev.correlated.network.PrototypeMarshaller;
import com.elytradev.correlated.storage.Prototype;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class TerminalActionMessage extends Message {
	public enum TerminalAction {
		/**
		 * Player pressed drop key.
		 * Argument is 0 to drop one item, 1 to drop a stack.
		 */
		DROP,
		/**
		 * Player clicked.
		 * Argument is 0 to take one item, 1 to take a stack,
		 * 2 to take one item to inventory, 3 to take one stack to inventory.
		 */
		GET,
		/**
		 * Player clicked with an item in the cursor.
		 * Subject is ignored.
		 * Argument is 0 to put one item, 1 to put the entire stack.
		 */
		PUT,
		/**
		 * Player pick-blocked. This will invoke a coprocessor event on the
		 * type if one exists.
		 */
		INVOKE
	}
	
	private TerminalAction action;
	@MarshalledAs(PrototypeMarshaller.NAME)
	@Optional
	private Prototype subject;
	@MarshalledAs("varint")
	private int argument;
	
	public TerminalActionMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public TerminalActionMessage(TerminalAction action, Prototype subject, int argument) {
		super(CNetwork.CONTEXT);
		this.action = action;
		this.subject = subject;
		this.argument = argument;
	}
	
	@Override
	protected void handle(EntityPlayer player) {
		if (player.openContainer instanceof ContainerTerminal) {
			ContainerTerminal ct = (ContainerTerminal)player.openContainer;
			switch (action) {
				case DROP:
					player.dropItem(ct.removeItemsFromNetwork(subject.getStack(), argument == 0 ? 1 : subject.getStack().getMaxStackSize()), true);
					break;
				case GET:
					if (argument == 0 || argument == 1) {
						if (!player.inventory.getItemStack().isEmpty()) {
							if (player instanceof EntityPlayerMP) {
								((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
							}
							return;
						}
						player.inventory.setItemStack(ct.removeItemsFromNetwork(subject.getStack(), argument == 0 ? 1 : subject.getStack().getMaxStackSize()));
					} else if (argument == 2 || argument == 3) {
						ItemStack is = ct.removeItemsFromNetwork(subject.getStack(), argument == 2 ? 1 : subject.getStack().getMaxStackSize());
						if (!player.inventory.addItemStackToInventory(is)) {
							is = ct.addItemToNetworkSilently(is).stack;
							if (!is.isEmpty()) {
								CLog.warn("Accidentally disappeared {} items into the ether", is.getCount());
							}
						}
					}
					break;
				case PUT:
					player.inventory.setItemStack(ct.addItemToNetwork(player.inventory.getItemStack()).stack);
					if (!player.inventory.getItemStack().isEmpty()) {
						// the client assumes that their put was successful; it it wasn't, we have to tell them
						if (player instanceof EntityPlayerMP) {
							((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
						}
					}
					break;
				case INVOKE:
					ct.addStatusLine(new TextComponentTranslation("msg.correlated.invokeFailed.noAction"));
					break;
			}
		}
	}

}
