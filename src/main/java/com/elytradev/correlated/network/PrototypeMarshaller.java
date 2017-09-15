package com.elytradev.correlated.network;

import com.elytradev.concrete.network.Marshaller;
import com.elytradev.correlated.storage.Prototype;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PrototypeMarshaller implements Marshaller<Prototype> {

	public static final String NAME = "com.elytradev.correlated.network.PrototypeMarshaller";
	public static final String NAME_LIST = "com.elytradev.correlated.network.PrototypeMarshaller-list";
	public static final PrototypeMarshaller INSTANCE = new PrototypeMarshaller();
	
	@Override
	public void marshal(ByteBuf out, Prototype t) {
		ItemStack s = t.getStack().copy();
		s.setCount(1);
		ByteBufUtils.writeItemStack(out, s);
	}
	
	@Override
	public Prototype unmarshal(ByteBuf in) {
		return new Prototype(ByteBufUtils.readItemStack(in));
	}
	
}
