package com.elytradev.correlated.client.render.tile;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockController;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.proxy.ClientProxy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderControllerItemCheaty extends TileEntitySpecialRenderer<TileEntity> {

	@Override
	public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		IBlockState state = CBlocks.CONTROLLER.getDefaultState()
				.withProperty(BlockController.CHEATY, true);
		RenderMicrowaveBeam.renderBaseForItem(state);
		if (((ClientProxy)Correlated.proxy).controllerAbt != null) {
			((ClientProxy)Correlated.proxy).controllerAbt.render(state, x, y, z, 0, ClientProxy.ticks);
		}
	}
	
}
