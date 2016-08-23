package io.github.elytra.copo.client.gui.shell;

public abstract class Program {
	protected GuiVTShell parent;
	public Program(GuiVTShell parent) {
		this.parent = parent;
	}
	public abstract void render(int rows, int cols);
	public abstract void keyTyped(char typedChar, int keyCode);
	public abstract String getName();
	public abstract void update();
}
