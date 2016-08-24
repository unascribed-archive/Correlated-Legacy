package io.github.elytra.copo.client.gui.shell;

import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.IBMFontRenderer;
import io.github.elytra.copo.entity.automaton.Opcode;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

public class AutomatonProgrammer extends Program {
	private boolean dirty = false;
	private List<String> opcodes = Lists.newArrayList();
	private List<List<String>> arguments = Lists.newArrayList();
	
	private int cursorRow = 0;
	private int cursorCol = 0;
	
	private int cursorField = 0;
	
	private int totalCols;
	private int totalRows;
	
	private String status;
	private int statusTicks;
	
	public AutomatonProgrammer(GuiVTShell parent) {
		super(parent);
	}

	@Override
	public void render(int rows, int cols) {
		totalRows = rows;
		totalCols = cols;
		if (dirty) {
			drawStringInverseVideo(cols-1, 0, "○");
		} else {
			drawStringInverseVideo(cols-1, 0, " ");
		}
		
		drawStringInverseVideo(0, rows-1, "^O");
		drawString(3, rows-1, "Save");
		
		drawStringInverseVideo(8, rows-1, "^X");
		drawString(11, rows-1, "Exit");
		
		drawStringInverseVideo(16, rows-1, "^Y");
		drawString(19, rows-1, "Prev");
		
		drawStringInverseVideo(24, rows-1, "^V");
		drawString(27, rows-1, "Next");
		
		drawStringInverseVideo(32, rows-1, "^K");
		drawString(35, rows-1, "Kill");
		
		drawStringInverseVideo(40, rows-1, "^U");
		drawString(43, rows-1, "UnKill");
		rows--;
		
		if (status != null) {
			rows--;
			drawStringInverseVideo(((cols-(status.length()+4))/2), rows, "[ "+status+" ]");
		}
		
		if (cursorField == 0) {
			if (cursorCol == 0) {
				cursorCol = 1;
			}
			if (cursorCol >= 4) {
				if (cursorRow < opcodes.size()) {
					Opcode oc = lookupOpcode(opcodes.get(cursorRow));
					if (oc == null || !oc.getArgumentSpec().isEmpty()) {
						cursorField = 1;
						cursorCol = 6;
					} else {
						cursorCol = 3;
					}
				} else {
					cursorCol = 3;
				}
			}
		} else {
			if (cursorCol == 5 || cursorRow >= arguments.size()) {
				cursorField = 0;
				cursorCol = 3;
			} else if (cursorRow < arguments.size()) {
				List<String> args = arguments.get(cursorRow);
				int x = 5;
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
			Opcode oc = lookupOpcode(s);
			if (cursorRow == y && cursorField == 0) {
				for (int x = 0; x < 5; x++) {
					int i = x-1;
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
				drawString(1, y, s);
				if (oc == null && idx < opcodes.size()) {
					drawString(0, y, s.startsWith("x") ? "?" : "‼");
				}
			}
			y++;
		}
		y = 0;
		for (List<String> li : arguments) {
			Opcode oc = lookupOpcode(opcodes.get(y));
			int x = 5;
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

	private Opcode lookupOpcode(String str) {
		if (str.charAt(0) == 'x') {
			try {
				int i = Integer.parseInt(str.substring(1), 16);
				return Opcode.byBytecode(i);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return Opcode.byMnemonic(str.toUpperCase(Locale.ROOT));
		}
	}

	private void drawStringInverseVideo(int x, int y, String str) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x*4.5f, y*8f, 0);
		IBMFontRenderer.drawStringInverseVideo(0, 0, str, CoPo.proxy.getColor("other", 64));
		GlStateManager.popMatrix();
	}
	
	private void drawString(int x, int y, String str) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x*4.5f, y*8f, 0);
		IBMFontRenderer.drawString(0, 0, str, CoPo.proxy.getColor("other", 64));
		GlStateManager.popMatrix();
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_UP) {
			if (cursorRow > 0) {
				cursorRow--;
			}
		} else if (keyCode == Keyboard.KEY_DOWN) {
			if (cursorRow < opcodes.size()) {
				cursorRow++;
			}
		} else if (keyCode == Keyboard.KEY_LEFT) {
			if (cursorCol > 0) {
				cursorCol--;
			}
		} else if (keyCode == Keyboard.KEY_RIGHT) {
			if (cursorCol < totalCols) {
				cursorCol++;
			}
		} else if (GuiScreen.isCtrlKeyDown()) {
			if (keyCode == Keyboard.KEY_K) {
				if (cursorRow < opcodes.size()) {
					// TODO buffer
					opcodes.remove(cursorRow);
					arguments.remove(cursorRow);
				}
			} else if (keyCode == Keyboard.KEY_X) {
				if (dirty) {
					status = "There are unsaved changes. Really exit?";
					statusTicks = 0;
				} else {
					parent.program = new CommandInterpreter(parent);
				}
			}
		} else if (cursorField == 0) {
			if (Character.isAlphabetic(typedChar) || Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK) {
				if (keyCode == Keyboard.KEY_BACK) {
					typedChar = ' ';
				}
				if (cursorRow >= opcodes.size()) {
					char c;
					if (cursorCol == 1 && keyCode == Keyboard.KEY_X) {
						c = 'x';
					} else {
						c = Character.toUpperCase(typedChar);
					}
					opcodes.add(Strings.padEnd(Strings.padStart(Character.toString(c), cursorCol-1, ' '), 3, ' '));
					arguments.add(Lists.newArrayList());
					cursorCol++;
					dirty = true;
				} else {
					int pos = cursorCol - 1;
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
				Opcode oc = lookupOpcode(opcodes.get(cursorRow));
				int x = 5;
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

}
