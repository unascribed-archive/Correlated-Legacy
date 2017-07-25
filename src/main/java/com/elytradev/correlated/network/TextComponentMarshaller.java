package com.elytradev.correlated.network;

import com.elytradev.concrete.network.Marshaller;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class TextComponentMarshaller implements Marshaller<ITextComponent> {

	public static final String NAME = "com.elytradev.correlated.network.TextComponentMarshaller";
	public static final TextComponentMarshaller INSTANCE = new TextComponentMarshaller();
	
	@Override
	public void marshal(ByteBuf out, ITextComponent t) {
		ByteBufUtils.writeUTF8String(out, ITextComponent.Serializer.componentToJson(t));
	}
	
	@Override
	public ITextComponent unmarshal(ByteBuf in) {
		return ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(in));
	}
	
}
