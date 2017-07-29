package com.elytradev.correlated.network.documentation;

import com.elytradev.concrete.network.Message;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.correlated.client.gui.GuiDocumentation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class OpenDocumentationMessage extends Message {

	private String topic;
	private String domain;
	private boolean playAnimation;
	
	public OpenDocumentationMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public OpenDocumentationMessage(String topic, String domain, boolean playAnimation) {
		super(CNetwork.CONTEXT);
		this.topic = topic;
		this.domain = domain;
		this.playAnimation = playAnimation;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer sender) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiDocumentation(topic, domain, playAnimation));
	}

}
