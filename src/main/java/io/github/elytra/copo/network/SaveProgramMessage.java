package io.github.elytra.copo.network;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.entity.automaton.Instruction;
import io.github.elytra.copo.entity.automaton.Opcode;
import io.github.elytra.copo.entity.automaton.Opcode.ArgumentSpec;
import io.github.elytra.copo.helper.ItemStacks;
import io.github.elytra.copo.inventory.ContainerVT;
import io.github.elytra.copo.item.ItemFloppy;
import io.github.elytra.copo.tile.TileEntityVT;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class SaveProgramMessage extends Message {
	@MarshalledAs("string-list")
	public List<String> opcodes;
	@MarshalledAs("string-list-list")
	public List<List<String>> arguments;
	
	public SaveProgramMessage(NetworkContext ctx) {
		super(ctx);
	}
	public SaveProgramMessage(List<String> opcodes, List<List<String>> arguments) {
		super(CoPo.inst.network);
		this.opcodes = opcodes;
		this.arguments = arguments;
	}
	
	@Override
	protected void handle(EntityPlayer sender) {
		EntityPlayerMP p = (EntityPlayerMP)sender;
		Container c = p.openContainer;
		if (!(c instanceof ContainerVT)) return;
		int wi = p.currentWindowId;
		TileEntityVT vt = ((TileEntityVT)((ContainerVT)c).vt);
		ItemStack floppy = vt.getStackInSlot(1);
		if (floppy != null && floppy.getItem() instanceof ItemFloppy) {
			ItemFloppy item = (ItemFloppy)floppy.getItem();
			if (item.isWriteProtected(floppy)) {
				new SetEditorStatusMessage(wi, "Write protect error").sendTo(sender);
				return;
			} else {
				ItemStacks.ensureHasTag(floppy).getTagCompound().removeTag("Compiled");
				NBTTagList src = new NBTTagList();
				for (int i = 0; i < opcodes.size(); i++) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setString("Opcode", opcodes.get(i));
					NBTTagList args = new NBTTagList();
					for (int j = 0; j < arguments.get(i).size(); j++) {
						args.appendTag(new NBTTagString(arguments.get(i).get(j)));
					}
					tag.setTag("Arguments", args);
					src.appendTag(tag);
				}
				ItemStacks.ensureHasTag(floppy).getTagCompound().setTag("SourceCode", src);
			}
		} else {
			new SetEditorStatusMessage(wi, "Not ready reading drive A").sendTo(sender);
			return;
		}
		try {
			List<Instruction> li = Lists.newArrayList();
			String lastWarning = null;
			String error = null;
			int warn = 0;
			int cap = 0;
			glass: for (int i = 0; i < opcodes.size(); i++) {
				Opcode oc = Opcode.lookup(opcodes.get(i));
				List<String> args = arguments.get(i);
				if (oc == null) {
					cap++;
					li.add(null); // invalid instruction
					warn++;
					lastWarning = "line "+(i+1)+": Bad opcode "+opcodes.get(i);
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
				new SetEditorStatusMessage(wi, "Save OK, compile fail: "+error).sendTo(sender);
				return;
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
			new SetEditorStatusMessage(wi, status).sendTo(sender);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			new SetEditorStatusMessage(wi, "Save OK, compile fail: Internal error").sendTo(sender);
			return;
		}
	}

}
