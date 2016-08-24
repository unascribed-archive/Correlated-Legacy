package io.github.elytra.copo.entity.automaton;

import java.nio.ByteBuffer;

import io.github.elytra.copo.entity.EntityAutomaton;

public class OpcodeNOP extends Opcode {

	public OpcodeNOP() {
		super("NOP", 0x00, 0);
	}

	@Override
	public void execute(EntityAutomaton context, ByteBuffer arguments) {}

}
