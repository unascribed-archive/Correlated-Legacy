package io.github.elytra.copo.client.gui.shell;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.IBMFontRenderer;
import io.github.elytra.copo.entity.automaton.Opcode;
import io.github.elytra.copo.item.ItemFloppy;
import io.github.elytra.copo.network.SaveProgramMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants.NBT;

public class AutomatonProgrammer extends Program {
	private boolean dirty = false;
	private List<String> opcodes = Lists.newArrayList();
	private List<List<String>> arguments = Lists.newArrayList();
	
	private int cursorRow = 0;
	private int cursorCol = 0;
	
	private int cursorField = 0;
	
	private int totalCols;
	private int totalRows;
	
	private int scrollOffset;
	
	private String status;
	private int statusTicks;
	
	private String cutOpcode;
	private List<String> cutArguments;
	
	private int xOffset = 1;
	
	private boolean confirmExit;
	
	public AutomatonProgrammer(GuiVTShell parent) {
		super(parent);
		if (parent.container.floppySlot != null) {
			ItemStack floppy = parent.container.floppySlot.getStack();
			if (floppy != null) {
				if (floppy.hasTagCompound() && floppy.getTagCompound().hasKey("SourceCode", NBT.TAG_BYTE_ARRAY)) {
					byte[] bys = floppy.getTagCompound().getByteArray("SourceCode");
					SaveProgramMessage msg = new SaveProgramMessage();
					msg.fromBytes(Unpooled.wrappedBuffer(bys));
					opcodes = msg.opcodes;
					arguments = msg.arguments;
				}
				if (floppy.getItem() instanceof ItemFloppy) {
					if (((ItemFloppy)floppy.getItem()).isWriteProtected(floppy)) {
						status = "Warning: Floppy is write protected";
						statusTicks = 0;
					}
				}
			} else {
				status = "Warning: No floppy in drive";
				statusTicks = 0;
			}
		}
	}

	@Override
	public void render(int rows, int cols) {
		if (status != null && (status.contains("Save OK") || status.contains("compile OK"))) {
			dirty = false;
		}
		if (confirmExit && !dirty) {
			parent.program = new CommandInterpreter(parent);
			return;
		}
		float div = (cursorRow/(float)opcodes.size());
		int scrollKnobY = (int)(div*rows);
		scrollKnobY = Math.min(rows-1, Math.max(0, scrollKnobY));
		drawStringInverseVideoAbsolute(cols-1, scrollKnobY, dirty ? "○" : " ");
		
		if (confirmExit) {
			drawStringInverseVideoAbsolute(0, rows-2, Strings.padEnd("Save? (ANSWERING \"No\" WILL DESTROY CHANGES)", cols, ' '));
			drawStringInverseVideoAbsolute(0, rows-1, " Y");
			drawStringAbsolute(3, rows-1, "Yes");
			drawStringInverseVideoAbsolute(7, rows-1, " N");
			drawStringAbsolute(10, rows-1, "No");
			drawStringInverseVideoAbsolute(13, rows-1, "^C");
			drawStringAbsolute(16, rows-1, "Cancel");
			rows -= 2;
		} else {
			drawStringInverseVideoAbsolute(0, rows-1, "^O");
			drawStringAbsolute(3, rows-1, "Save");
			
			drawStringInverseVideoAbsolute(8, rows-1, "^X");
			drawStringAbsolute(11, rows-1, "Exit");
			
			drawStringInverseVideoAbsolute(16, rows-1, "^Y");
			drawStringAbsolute(19, rows-1, "Prev");
			
			drawStringInverseVideoAbsolute(24, rows-1, "^V");
			drawStringAbsolute(27, rows-1, "Next");
			
			drawStringInverseVideoAbsolute(32, rows-1, "^K");
			drawStringAbsolute(35, rows-1, "Cut");
			
			drawStringInverseVideoAbsolute(39, rows-1, "^U");
			drawStringAbsolute(42, rows-1, "UnCut");
			rows--;
		}
		
		if (status != null) {
			rows--;
			drawStringInverseVideoAbsolute(((cols-(status.length()+4))/2), rows, "[ "+status+" ]");
		}
		
		cols -= 2;
		
		totalRows = rows;
		totalCols = cols;
		
		int cursorRowScrolled = cursorRow-scrollOffset;
		if (cursorRowScrolled >= rows) {
			scrollOffset++;
		} else if (cursorRowScrolled < 0) {
			scrollOffset--;
		}
		
		if (scrollOffset > opcodes.size()) {
			scrollOffset = opcodes.size();
		} else if (scrollOffset < 0) {
			scrollOffset = 0;
		}
		if (cursorRow > opcodes.size()) {
			cursorRow = opcodes.size();
		} else if (cursorRow < 0) {
			cursorRow = 0;
		}
		
		int oldXOffset = xOffset;
		xOffset = Integer.toString(opcodes.size()).length()+1;
		
		if (oldXOffset != xOffset) {
			int change = xOffset-oldXOffset;
			cursorCol += change;
		}
		
		for (int i = 0; i < Math.min(opcodes.size()-scrollOffset, rows); i++) {
			String str = Integer.toString((scrollOffset+i)+1);
			drawStringAbsolute(xOffset-(str.length()+1), i, str);
		}
		
		if (cursorField == 0) {
			if (cursorCol <= xOffset) {
				cursorCol = xOffset+1;
			}
			if (cursorCol >= xOffset+4) {
				if (cursorRow < opcodes.size()) {
					Opcode oc = Opcode.lookup(opcodes.get(cursorRow));
					if (oc == null || !oc.getArgumentSpec().isEmpty()) {
						cursorField = 1;
						cursorCol = xOffset+6;
					} else {
						cursorCol = xOffset+3;
					}
				} else {
					cursorCol = xOffset+3;
				}
			}
		} else {
			if (cursorCol == xOffset+5 || cursorRow >= arguments.size()) {
				cursorField = 0;
				cursorCol = xOffset+3;
			} else if (cursorRow < arguments.size()) {
				List<String> args = arguments.get(cursorRow);
				int x = xOffset+5;
				int i = 0;
				for (String s : args) {
					x++;
					x += s.length();
					if (cursorCol <= x) {
						break;
					}
					x++;
					i++;
				}
				if (cursorField < i+1) {
					cursorCol++;
				}
				cursorField = i+1;
				String arg = i >= args.size() ? " " : args.get(i);
				int lim = (x-arg.length());
				if (cursorCol < lim) {
					cursorCol--;
				} else if (cursorCol > x+1) {
					cursorCol = x+1;
				}
			}
		}
		int y = 0;
		for (int idx = 0; idx <= opcodes.size(); idx++) {
			String s = idx >= opcodes.size() ? "   " : opcodes.get(idx);
			Opcode oc = Opcode.lookup(s);
			if (cursorRow == y && cursorField == 0) {
				for (int x = xOffset; x < xOffset+5; x++) {
					int i = (x-1)-xOffset;
					String c;
					if (i < 0) {
						c = oc == null ? s.startsWith("x") ? "?" : "‼" : " ";
					} else if (i >= s.length()) {
						c = " ";
					} else {
						c = Character.toString(s.charAt(i));
					}
					if (x == cursorCol) {
						drawString(x, y, c);
					} else {
						drawStringInverseVideo(x, y, c);
					}
				}
			} else {
				drawString(xOffset+1, y, s);
				if (oc == null && idx < opcodes.size()) {
					drawString(xOffset, y, s.startsWith("x") ? "?" : "‼");
				}
			}
			y++;
		}
		y = 0;
		for (List<String> li : arguments) {
			Opcode oc = Opcode.lookup(opcodes.get(y));
			int x = xOffset+5;
			int i = 0;
			for (int idx = 0; idx < (oc == null ? li.size()+1 : oc.getArgumentSpec().size()); idx++) {
				String s = idx >= li.size() ? " " : li.get(idx);
				boolean hint = false;
				if (oc != null && idx < oc.getArgumentSpec().size()) {
					if (s.trim().isEmpty()) {
						hint = true;
						s = oc.getArgumentSpec().get(idx).hint;
					}
				}
				if (cursorRow == y && cursorField == (i+1)) {
					if (cursorCol == x) {
						drawString(x, y, hint ? "(" : " ");
					} else {
						drawStringInverseVideo(x, y, hint ? "(" : " ");
					}
				} else if (hint) {
					drawString(x, y, "(");
				}
				x++;
				for (int pos = 0; pos < s.length(); pos++) {
					if (cursorRow == y && cursorField == (i+1) && cursorCol != x) {
						drawStringInverseVideo(x, y, Character.toString(s.charAt(pos)));
					} else {
						drawString(x, y, Character.toString(s.charAt(pos)));
					}
					x++;
				}
				if (cursorRow == y && cursorField == (i+1)) {
					if (cursorCol == x) {
						drawString(x, y, hint ? "(" : " ");
					} else {
						drawStringInverseVideo(x, y, hint ? ")" : " ");
					}
				} else if (hint) {
					drawString(x, y, ")");
				}
				x++;
				i++;
			}
			if (cursorRow == y && cursorField == li.size()) {
				
			}
			y++;
		}
		
	}

	private void drawStringInverseVideo(int x, int y, String str) {
		if (y < scrollOffset) return;
		y -= scrollOffset;
		if (y >= totalRows) return;
		drawStringInverseVideoAbsolute(x, y, str);
	}
	
	private void drawString(int x, int y, String str) {
		if (y < scrollOffset) return;
		y -= scrollOffset;
		if (y >= totalRows) return;
		drawStringAbsolute(x, y, str);
	}
	
	private void drawStringInverseVideoAbsolute(int x, int y, String str) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x*4.5f, y*8f, 0);
		IBMFontRenderer.drawStringInverseVideo(0, 0, str, CoPo.proxy.getColor("other", 64));
		GlStateManager.popMatrix();
	}
	
	private void drawStringAbsolute(int x, int y, String str) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x*4.5f, y*8f, 0);
		IBMFontRenderer.drawString(0, 0, str, CoPo.proxy.getColor("other", 64));
		GlStateManager.popMatrix();
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (confirmExit) {
			if (keyCode == Keyboard.KEY_Y) {
				status = "Saving...";
				statusTicks = -1;
				CoPo.inst.network.sendToServer(new SaveProgramMessage(opcodes, arguments));
			} else if (keyCode == Keyboard.KEY_N) {
				parent.program = new CommandInterpreter(parent);
			} else if (GuiScreen.isCtrlKeyDown() && keyCode == Keyboard.KEY_C) {
				confirmExit = false;
			}
			return;
		}
		if (keyCode == Keyboard.KEY_UP) {
			if (cursorRow > 0) {
				cursorRow--;
			}
		} else if (keyCode == Keyboard.KEY_DOWN) {
			if (cursorRow < opcodes.size()) {
				cursorRow++;
				if (cursorRow == opcodes.size()) {
					cursorCol = xOffset+1;
					cursorField = 0;
				}
			}
		} else if (keyCode == Keyboard.KEY_LEFT) {
			if (cursorCol > xOffset) {
				cursorCol--;
			}
		} else if (keyCode == Keyboard.KEY_RIGHT) {
			if (cursorCol < totalCols) {
				cursorCol++;
			}
		} else if (GuiScreen.isCtrlKeyDown()) {
			if (keyCode == Keyboard.KEY_K) {
				if (cursorRow < opcodes.size()) {
					cutOpcode = opcodes.remove(cursorRow);
					cutArguments = arguments.remove(cursorRow);
				}
			} else if (keyCode == Keyboard.KEY_U) {
				opcodes.add(cursorRow, cutOpcode);
				arguments.add(cursorRow, cutArguments);
				cursorRow++;
			} else if (keyCode == Keyboard.KEY_X) {
				if (dirty) {
					confirmExit = true;
				} else {
					parent.program = new CommandInterpreter(parent);
				}
			} else if (keyCode == Keyboard.KEY_V) {
				scrollOffset += totalRows;
				cursorRow += totalRows;
			} else if (keyCode == Keyboard.KEY_Y) {
				scrollOffset -= totalRows;
				cursorRow -= totalRows;
			} else if (keyCode == Keyboard.KEY_O) {
				status = "Saving...";
				statusTicks = -1;
				CoPo.inst.network.sendToServer(new SaveProgramMessage(opcodes, arguments));
			}
		} else if (cursorField == 0) {
			if (Character.isAlphabetic(typedChar) || Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK) {
				if (keyCode == Keyboard.KEY_BACK) {
					typedChar = ' ';
				}
				if (cursorRow >= opcodes.size()) {
					char c;
					if (cursorCol == xOffset+1 && keyCode == Keyboard.KEY_X) {
						c = 'x';
					} else {
						c = Character.toUpperCase(typedChar);
					}
					opcodes.add(Strings.padEnd(Strings.padStart(Character.toString(c), (cursorCol-1)-xOffset, ' '), 3, ' '));
					arguments.add(Lists.newArrayList());
					cursorCol++;
					dirty = true;
				} else {
					int pos = (cursorCol-1)-xOffset;
					if (pos >= 0 && pos < 3) {
						StringBuilder str = new StringBuilder(opcodes.get(cursorRow));
						char c;
						if (pos == 0 && keyCode == Keyboard.KEY_X) {
							c = 'x';
						} else {
							c = Character.toUpperCase(typedChar);
						}
						str.setCharAt(pos, c);
						opcodes.set(cursorRow, str.toString());
						dirty = true;
					}
					cursorCol++;
				}
				if (keyCode == Keyboard.KEY_BACK) {
					cursorCol -= 2;
				}
			}
		} else if (Character.isAlphabetic(typedChar) || Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK) {
			if (cursorRow < arguments.size() && cursorRow < opcodes.size()) {
				List<String> args = arguments.get(cursorRow);
				Opcode oc = Opcode.lookup(opcodes.get(cursorRow));
				int x = xOffset+5;
				int i = 0;
				for (String s : args) {
					x++;
					x += s.length();
					if (cursorCol <= x) {
						break;
					}
					x++;
					i++;
				}
				if (i < args.size()) {
					x -= args.get(i).length();
				}
				StringBuilder str;
				if (i >= args.size() || args.get(i).isEmpty()) {
					str = new StringBuilder(" ");
					x++;
				} else {
					str = new StringBuilder(args.get(i));
				}
				int pos = cursorCol-x;
				while (str.length() <= pos) {
					str.append(' ');
				}
				if (pos < 0) return;
				if (keyCode == Keyboard.KEY_BACK) {
					cursorCol--;
					if (pos > 0) {
						pos--;
					}
					str.deleteCharAt(pos);
				} else {
					str.setCharAt(pos, typedChar);
				}
				boolean valid = true;
				if (oc != null) {
					
				}
				if (valid) {
					while (i >= args.size()) {
						args.add("");
					}
					if (i == args.size()-1 && str.toString().trim().isEmpty()) {
						args.remove(i);
					} else {
						args.set(i, str.toString());
					}
					dirty = true;
					if (keyCode != Keyboard.KEY_BACK) {
						cursorCol++;
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "IDE.EXE";
	}

	@Override
	public void update() {
		if (statusTicks > -1) {
			statusTicks++;
			if (statusTicks > 80) {
				status = null;
				statusTicks = -1;
			}
		}
	}

	public void setStatus(String line) {
		status = line;
		statusTicks = 0;
	}

}
