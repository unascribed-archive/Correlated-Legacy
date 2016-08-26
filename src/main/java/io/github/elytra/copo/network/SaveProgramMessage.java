package io.github.elytra.copo.network;

import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.github.elytra.copo.entity.automaton.Instruction;
import io.github.elytra.copo.entity.automaton.Opcode;
import io.github.elytra.copo.entity.automaton.Opcode.ArgumentSpec;
import io.github.elytra.copo.helper.ItemStacks;
import io.github.elytra.copo.inventory.ContainerVT;
import io.github.elytra.copo.item.ItemFloppy;
import io.github.elytra.copo.tile.TileEntityVT;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SaveProgramMessage implements IMessage, IMessageHandler<SaveProgramMessage, SetEditorStatusMessage> {
	public List<String> opcodes;
	public List<List<String>> arguments;
	
	public SaveProgramMessage() {}
	public SaveProgramMessage(List<String> opcodes, List<List<String>> arguments) {
		this.opcodes = opcodes;
		this.arguments = arguments;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		int len = buf.readUnsignedShort();
		opcodes = Lists.newArrayList();
		arguments = Lists.newArrayList();
		byte[] opcodeBuf = new byte[3];
		for (int i = 0; i < len; i++) {
			buf.readBytes(opcodeBuf);
			opcodes.add(new String(opcodeBuf, Charsets.US_ASCII));
			List<String> args = Lists.newArrayList();
			arguments.add(args);
			int argsLen = buf.readUnsignedShort();
			for (int j = 0; j < argsLen; j++) {
				args.add(ByteBufUtils.readUTF8String(buf));
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeShort(opcodes.size());
		for (int i = 0; i < opcodes.size(); i++) {
			String padded = Strings.padEnd(opcodes.get(i), 3, ' ');
			buf.writeBytes(padded.getBytes(Charsets.US_ASCII));
			List<String> args = arguments.get(i);
			buf.writeShort(args.size());
			for (String s : args) {
				ByteBufUtils.writeUTF8String(buf, s);
			}
		}
	}
	
	@Override
	public SetEditorStatusMessage onMessage(SaveProgramMessage message, MessageContext ctx) {
		EntityPlayerMP p = ctx.getServerHandler().playerEntity;
		Container c = p.openContainer;
		if (!(c instanceof ContainerVT)) return null;
		int wi = p.currentWindowId;
		TileEntityVT vt = ((TileEntityVT)((ContainerVT)c).vt);
		ItemStack floppy = vt.getStackInSlot(1);
		if (floppy != null && floppy.getItem() instanceof ItemFloppy) {
			ItemFloppy item = (ItemFloppy)floppy.getItem();
			if (item.isWriteProtected(floppy)) {
				return new SetEditorStatusMessage(wi, "Write protect error");
			} else {
				ByteBuf buf = Unpooled.buffer();
				message.toBytes(buf);
				byte[] bys = new byte[buf.readableBytes()];
				buf.readBytes(bys);
				ItemStacks.ensureHasTag(floppy).getTagCompound().removeTag("Compiled");
				ItemStacks.ensureHasTag(floppy).getTagCompound().setByteArray("SourceCode", bys);
			}
		} else {
			return new SetEditorStatusMessage(wi, "Not ready reading drive A");
		}
		try {
			List<Instruction> li = Lists.newArrayList();
			String lastWarning = null;
			String error = null;
			int warn = 0;
			int cap = 0;
			glass: for (int i = 0; i < message.opcodes.size(); i++) {
				Opcode oc = Opcode.lookup(message.opcodes.get(i));
				List<String> args = message.arguments.get(i);
				if (oc == null) {
					cap++;
					li.add(null); // invalid instruction
					warn++;
					lastWarning = "line "+(i+1)+": Bad opcode "+message.opcodes.get(i);
				} else {
					if (args.size() != oc.getArgumentSpec().size()) {
						error = "line "+(i+1)+": Not enough arguments";
						break;
					} else {
						Instruction ins = new Instruction();
						ins.opcode = oc;
						int size = 0;
						List<Object> parsed = Lists.newArrayList();
						for (int j = 0; j < oc.getArgumentSpec().size(); j++) {
							ArgumentSpec as = oc.getArgumentSpec().get(j);
							String str = args.get(j);
							try {
								Object o = as.parser.apply(str);
								parsed.add(o);
								size += as.sizer.apply(o);
							} catch (Exception e) {
								error = "line "+(i+1)+", Invalid argument "+j;
								break glass;
							}
						}
						ByteBuf buf = Unpooled.buffer(size, size);
						for (int j = 0; j < oc.getArgumentSpec().size(); j++) {
							ArgumentSpec as = oc.getArgumentSpec().get(j);
							Object o = parsed.get(j);
							as.writer.accept(buf, o);
						}
						ins.arguments = buf;
						li.add(ins);
						cap++;
						cap += size;
					}
				}
			}
			if (error != null) {
				return new SetEditorStatusMessage(wi, "Save OK, compile fail: "+error);
			}
			String status;
			if (warn <= 0) {
				status = "Save and compile OK";
			} else if (warn == 1) {
				status = "Save OK, compile warn: "+lastWarning;
			} else {
				status = "Save OK, compile warn x"+warn;
			}
			ByteBuf buf = Unpooled.buffer(cap);
			for (Instruction ins : li) {
				if (ins == null || ins.opcode == null) {
					// HLT
					buf.writeByte(0xFF);
				} else {
					buf.writeByte(ins.opcode.getBytecode());
					buf.writeBytes(ins.arguments);
				}
			}
			byte[] bys = new byte[buf.readableBytes()];
			ItemStacks.ensureHasTag(floppy).getTagCompound().setByteArray("Compiled", bys);
			return new SetEditorStatusMessage(wi, status);
		} catch (Exception e) {
			e.printStackTrace();
			return new SetEditorStatusMessage(wi, "Save OK, compile fail: Internal error");
		}
	}

}
