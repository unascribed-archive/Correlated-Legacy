package com.elytradev.correlated.network;

import com.elytradev.concrete.network.Marshaller;
import com.elytradev.correlated.storage.NetworkType;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class NetworkTypeMarshaller implements Marshaller<NetworkType> {

	public static final String NAME = "com.elytradev.correlated.network.NetworkTypeMarshaller";
	public static final String NAME_LIST = "com.elytradev.correlated.network.NetworkTypeMarshaller-list";
	public static final NetworkTypeMarshaller INSTANCE = new NetworkTypeMarshaller();
	
	@Override
	public void marshal(ByteBuf out, NetworkType t) {
		ItemStack template = t.getStack().copy();
		template.setCount(1);
		ByteBufUtils.writeItemStack(out, template);
		ByteBufUtils.writeVarInt(out, t.getStack().getCount(), 5);
		out.writeLong(t.getLastModified());
	}
	
	@Override
	public NetworkType unmarshal(ByteBuf in) {
		ItemStack stack = ByteBufUtils.readItemStack(in);
		stack.setCount(ByteBufUtils.readVarInt(in, 5));
		long lastModified = in.readLong();
		return new NetworkType(stack, lastModified);
	}
	
}
