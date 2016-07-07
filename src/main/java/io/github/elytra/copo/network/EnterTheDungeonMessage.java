package io.github.elytra.copo.network;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.world.LimboTeleporter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EnterTheDungeonMessage implements IMessage, IMessageHandler<EnterTheDungeonMessage, IMessage> {
	@Override public void fromBytes(ByteBuf buf) { }
	@Override public void toBytes(ByteBuf buf) { }

	@Override
	public IMessage onMessage(EnterTheDungeonMessage message, MessageContext ctx) {
		ctx.getServerHandler().playerEntity.mcServer.addScheduledTask(() -> {
			int dim = CoPo.limboDimId;
			EntityPlayerMP p = ctx.getServerHandler().playerEntity;
			PlayerList r = p.mcServer.getPlayerList();
			int i = p.dimension;
			WorldServer worldserver = p.mcServer.worldServerForDimension(p.dimension);
			p.dimension = dim;
			WorldServer worldserver1 = p.mcServer.worldServerForDimension(p.dimension);
			p.connection.sendPacket(new SPacketRespawn(p.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), p.interactionManager.getGameType()));
			p.isDead = false;
			r.transferEntityToWorld(p, i, worldserver, worldserver1, new LimboTeleporter(p.mcServer.worldServerForDimension(dim)));
			r.preparePlayer(p, worldserver);
			p.connection.setPlayerLocation(p.posX, p.posY, p.posZ, p.rotationYaw, p.rotationPitch);
			p.interactionManager.setWorld(worldserver1);
			p.connection.sendPacket(new SPacketPlayerAbilities(p.capabilities));
			r.updateTimeAndWeatherForPlayer(p, worldserver1);
			
			p.inventory.clear();
			p.clearActivePotions();
			p.clearElytraFlying();
			p.clearInvulnerableDimensionChange();
			p.resetCooldown();
			
			r.syncPlayerInventory(p);
			net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(p, i, dim);
		});
		return null;
	}

}
