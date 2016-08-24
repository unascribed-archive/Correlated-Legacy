package io.github.elytra.copo.entity.automaton;

import java.nio.ByteBuffer;

import io.github.elytra.copo.entity.EntityAutomaton;

public class OpcodeTST extends Opcode {

	public OpcodeTST() {
		super("TST", 0xFF, 0, ArgumentSpec.INTEGER, ArgumentSpec.POINTER, ArgumentSpec.INTEGER);
	}

	@Override
	public void execute(EntityAutomaton context, ByteBuffer arguments) {}

}
