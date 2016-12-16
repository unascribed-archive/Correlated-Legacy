package io.github.elytra.correlated.network;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.client.gui.shell.GuiTerminalShell;
import io.github.elytra.correlated.client.gui.shell.RSOD;
import io.github.elytra.correlated.tile.TileEntityTerminal;
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
		super(Correlated.inst.network);
		this.pos = pos;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		TileEntity te = sender.worldObj.getTileEntity(pos);
		if (te instanceof TileEntityTerminal) {
			GuiTerminalShell gui = new GuiTerminalShell(null, null);
			gui.program = new RSOD(gui, ((TileEntityTerminal) te).getError());
			gui.palette = 1;
			Minecraft.getMinecraft().displayGuiScreen(gui);
		}
	}

}
