package io.github.elytra.copo.entity.automaton;

import java.nio.ByteBuffer;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import io.github.elytra.copo.entity.EntityAutomaton;

public abstract class Opcode {
	public enum ArgumentSpec {
		INTEGER('I'),
		POINTER('↑');
		public final String hint;
		private ArgumentSpec(char hint) {
			this.hint = Character.toString(hint);
		}
	}
	
	// XXX should this be an FML registry so that addons can register opcodes?
	private static final Map<Integer, Opcode> byBytecode = Maps.newHashMap();
	private static final Map<String, Opcode> byMnemonic = Maps.newHashMap();
	
	public static Opcode byBytecode(int bytecode) {
		if (bytecode < 0 || bytecode > 255) return null;
		return byBytecode.get(bytecode);
	}
	
	public static Opcode byMnemonic(String mnemonic) {
		return byMnemonic.get(mnemonic);
	}
	
	protected static void register(Opcode oc) {
		byBytecode.put(oc.getBytecode(), oc);
		byMnemonic.put(oc.getMnemonic(), oc);
	}
	
	protected final String mnemonic;
	protected final int bytecode;
	protected final ImmutableList<ArgumentSpec> args;
	protected final int techLevel;
	
	public Opcode(String mnemonic, int bytecode, int techLevel, ArgumentSpec... args) {
		if (mnemonic == null || mnemonic.length() > 3) {
			throw new IllegalArgumentException("Mnemonics must be less than or equal to 3 chars long");
		}
		if (bytecode < 0 || bytecode > 255) {
			throw new IllegalArgumentException("Bytecodes must fit in an unsigned byte");
		}
		if (techLevel < -1) {
			throw new IllegalArgumentException("Tech level must be >= -1");
		}
		this.mnemonic = mnemonic;
		this.bytecode = bytecode;
		this.techLevel = techLevel;
		this.args = ImmutableList.copyOf(args);
	}
	
	/**
	 * @return three characters that represent this opcode in a human-readable fashion
	 */
	public final String getMnemonic() {
		return mnemonic;
	}
	/**
	 * @return the tech level of this opcode, defining which manual it is contained in.
	 * 		tech level 0 means it can be used without a manual, -1 means it is never known
	 */
	public final int getTechLevel() {
		return techLevel;
	}
	/**
	 * @return a number from 0x00 to 0xFF representing this opcode in storage
	 */
	public final int getBytecode() {
		return bytecode;
	}
	public final ImmutableList<ArgumentSpec> getArgumentSpec() {
		return args;
	}
	public abstract void execute(EntityAutomaton context, ByteBuffer arguments);
	
	public static void init() {
		register(new OpcodeNOP());
		register(new OpcodeTST());
	}
	
}
