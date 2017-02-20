package com.elytradev.correlated.entity.automaton;

import com.google.common.io.BaseEncoding;

import io.netty.buffer.ByteBuf;

public class Instruction {
	public Opcode opcode;
	public ByteBuf arguments;
	
	public void toBytes(ByteBuf buf) {
		if (opcode == null) {
			buf.writeByte(0xFF);
		}
	}
	
	@Override
	public String toString() {
		return opcode.getMnemonic()+" "+BaseEncoding.base16().encode(arguments.array());
	}
}
