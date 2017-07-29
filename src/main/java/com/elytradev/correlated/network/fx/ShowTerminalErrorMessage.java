package com.elytradev.correlated.network.fx;

import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.client.gui.shell.GuiTerminalShell;
import com.elytradev.correlated.client.gui.shell.RSOD;
import com.elytradev.correlated.tile.TileEntityTerminal;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class ShowTerminalErrorMessage extends Message {
	public BlockPos pos;
	
	public ShowTerminalErrorMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public ShowTerminalErrorMessage(BlockPos pos) {
		super(CNetwork.CONTEXT);
		this.pos = pos;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		TileEntity te = sender.world.getTileEntity(pos);
		if (te instanceof TileEntityTerminal) {
			GuiTerminalShell gui = new GuiTerminalShell(null, null);
			gui.program = new RSOD(gui, ((TileEntityTerminal) te).getError());
			gui.palette = 1;
			Minecraft.getMinecraft().displayGuiScreen(gui);
		}
	}

}
