package com.elytradev.correlated.entity.automaton;

import com.elytradev.correlated.entity.EntityAutomaton;

import io.netty.buffer.ByteBuf;

public class OpcodeTST extends Opcode {

	public OpcodeTST() {
		super("TST", 0x01, 0, ArgumentSpec.INTEGER, ArgumentSpec.POINTER, ArgumentSpec.INTEGER);
	}

	@Override
	public void execute(EntityAutomaton context, ByteBuf arguments) {}

}
