package io.github.elytra.correlated.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.world.LimboProvider;
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
		super(Correlated.inst.network);
	}

	@Override
	protected void handle(EntityPlayer sender) {
		EntityPlayerMP p = (EntityPlayerMP)sender;
		int dim = Correlated.limboDimId;
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
