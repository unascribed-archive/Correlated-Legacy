package com.elytradev.correlated.entity.automaton;

import com.elytradev.correlated.entity.EntityAutomaton;

import io.netty.buffer.ByteBuf;

public class OpcodeNOP extends Opcode {

	public OpcodeNOP() {
		super("NOP", 0x00, 0);
	}

	@Override
	public void execute(EntityAutomaton context, ByteBuf arguments) {}

}
