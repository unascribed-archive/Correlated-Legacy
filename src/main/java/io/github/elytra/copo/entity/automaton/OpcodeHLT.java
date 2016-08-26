package io.github.elytra.copo.entity.automaton;

import io.github.elytra.copo.entity.EntityAutomaton;
import io.netty.buffer.ByteBuf;

public class OpcodeHLT extends Opcode {

	public OpcodeHLT() {
		super("HLT", 0xFF, 0);
	}
	
	@Override
	public void execute(EntityAutomaton context, ByteBuf arguments) {
		// TODO halt execution
	}

}
