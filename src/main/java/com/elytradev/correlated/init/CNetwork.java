package com.elytradev.correlated.init;

import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.network.APNRequestMessage;
import com.elytradev.correlated.network.APNResponseMessage;
import com.elytradev.correlated.network.AddStatusLineMessage;
import com.elytradev.correlated.network.AnimationSeenMessage;
import com.elytradev.correlated.network.AutomatonSpeakMessage;
import com.elytradev.correlated.network.ChangeAPNMessage;
import com.elytradev.correlated.network.CorrelatedGuiHandler;
import com.elytradev.correlated.network.EnterDungeonMessage;
import com.elytradev.correlated.network.InsertAllMessage;
import com.elytradev.correlated.network.LeaveDungeonMessage;
import com.elytradev.correlated.network.OpenDocumentationMessage;
import com.elytradev.correlated.network.RecipeTransferMessage;
import com.elytradev.correlated.network.SaveProgramMessage;
import com.elytradev.correlated.network.SetAutomatonNameMessage;
import com.elytradev.correlated.network.SetEditorStatusMessage;
import com.elytradev.correlated.network.DungeonTransitionMessage;
import com.elytradev.correlated.network.SetSearchQueryClientMessage;
import com.elytradev.correlated.network.SetSearchQueryServerMessage;
import com.elytradev.correlated.network.SetSlotSizeMessage;
import com.elytradev.correlated.network.ShowTerminalErrorMessage;
import com.elytradev.correlated.network.SignalStrengthMessage;
import com.elytradev.correlated.network.StartWeldthrowingMessage;
import com.google.common.base.Predicates;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CNetwork {
	public static NetworkContext CONTEXT;
	
	public static void register() {
		CONTEXT = NetworkContext.forChannel("Correlated");
		
		CONTEXT.register(SetSearchQueryClientMessage.class);
		CONTEXT.register(SetSearchQueryServerMessage.class);
		CONTEXT.register(SetSlotSizeMessage.class);
		CONTEXT.register(StartWeldthrowingMessage.class);
		CONTEXT.register(DungeonTransitionMessage.class);
		CONTEXT.register(EnterDungeonMessage.class);
		CONTEXT.register(SetAutomatonNameMessage.class);
		CONTEXT.register(LeaveDungeonMessage.class);
		CONTEXT.register(AddStatusLineMessage.class);
		CONTEXT.register(AutomatonSpeakMessage.class);
		CONTEXT.register(SetEditorStatusMessage.class);
		CONTEXT.register(SaveProgramMessage.class);
		CONTEXT.register(RecipeTransferMessage.class);
		CONTEXT.register(ShowTerminalErrorMessage.class);
		CONTEXT.register(InsertAllMessage.class);
		CONTEXT.register(ChangeAPNMessage.class);
		CONTEXT.register(SignalStrengthMessage.class);
		CONTEXT.register(APNRequestMessage.class);
		CONTEXT.register(APNResponseMessage.class);
		CONTEXT.register(OpenDocumentationMessage.class);
		CONTEXT.register(AnimationSeenMessage.class);
		
		NetworkRegistry.INSTANCE.registerGuiHandler(Correlated.inst, new CorrelatedGuiHandler());
	}

	public static void sendUpdatePacket(TileEntity te) {
		sendUpdatePacket(te, te.getUpdateTag());
	}

	public static void sendUpdatePacket(TileEntity te, NBTTagCompound nbt) {
		WorldServer ws = (WorldServer)te.getWorld();
		Chunk c = te.getWorld().getChunkFromBlockCoords(te.getPos());
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(te.getPos(), te.getBlockMetadata(), nbt);
		for (EntityPlayerMP player : te.getWorld().getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
			if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.x, c.z)) {
				player.connection.sendPacket(packet);
			}
		}
	}
}
