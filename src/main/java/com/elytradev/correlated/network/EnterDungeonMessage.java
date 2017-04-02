package com.elytradev.correlated.network;

import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.world.LimboProvider;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class EnterDungeonMessage extends Message {

	public EnterDungeonMessage(NetworkContext ctx) {
		super(ctx);
	}
	public EnterDungeonMessage() {
		super(CNetwork.CONTEXT);
	}

	@Override
	protected void handle(EntityPlayer sender) {
		EntityPlayerMP p = (EntityPlayerMP)sender;
		int dim = CConfig.limboDimId;
		Teleporter teleporter;
		WorldServer world = p.mcServer.worldServerForDimension(dim);
		WorldProvider provider = world.provider;
		if (provider instanceof LimboProvider) {
			teleporter = ((LimboProvider)provider).getTeleporter();
			if (!((LimboProvider)provider).isEntering(p.getGameProfile().getId())) {
				// no free entry
				return;
			}
		} else {
			teleporter = world.getDefaultTeleporter();
		}
		p.mcServer.getPlayerList().transferPlayerToDimension(p, dim, teleporter);
		
		p.inventory.clear();
		p.clearActivePotions();
		p.clearElytraFlying();
		p.clearInvulnerableDimensionChange();
		p.resetCooldown();
		
		p.getFoodStats().setFoodLevel(5);
		p.getFoodStats().setFoodSaturationLevel(2);
		p.setHealth(10);
	}

}
