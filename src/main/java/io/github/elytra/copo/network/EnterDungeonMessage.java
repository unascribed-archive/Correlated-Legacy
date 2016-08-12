package io.github.elytra.copo.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EnterDungeonMessage implements IMessage, IMessageHandler<EnterDungeonMessage, IMessage> {
	@Override public void fromBytes(ByteBuf buf) { }
	@Override public void toBytes(ByteBuf buf) { }

	@Override
	public IMessage onMessage(EnterDungeonMessage message, MessageContext ctx) {
		ctx.getServerHandler().playerEntity.mcServer.addScheduledTask(() -> {
			EntityPlayerMP p = ctx.getServerHandler().playerEntity;
			int dim = CoPo.limboDimId;
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
		});
		return null;
	}

}
