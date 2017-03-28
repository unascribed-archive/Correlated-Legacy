package com.elytradev.correlated.client;

import java.util.Deque;
import java.util.List;
import java.util.Random;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.shader.Framebuffer;

public class DocumentationPage {

	public abstract class Action {}
	
	public class NavigateAction extends Action {
		public final String target;
		
		public NavigateAction(String target) {
			this.target = target;
		}
	}

	public class ClickRegion {
		public final Action action;
		public final int x;
		public final int y;
		public final int width;
		public final int height;
		
		public ClickRegion(Action action, int x, int y, int width, int height) {
			this.action = action;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		public boolean intersects(int x, int y) {
			return x >= (this.x) && y >= (this.y) &&
					x < (this.x + this.width) && y < (this.y + this.height);
		}
		
		
	}

	public class PageRenderContext implements Cloneable {
		public int width = 0;
		public int height = 0;
		public int x = 0;
		public int y = 0;
		public int indent = 0;
		public float scale = 1;
		
		private Deque<PageRenderContext> stack = Queues.newArrayDeque();
		
		public void wrap() {
			x = indent;
			down();
		}
		
		public void down() {
			y += measureY();
			stretch();
		}
		
		public void indent() {
			indent += 24;
			x += 24;
		}
		
		public void outdent() {
			indent -= 24;
			x -= 24;
			if (indent < 0) indent = 0;
			if (x < 0) x = 0;
		}
		
		public void stretch() {
			if (width < x) {
				width = x;
			}
			if (height < y+measureY()) {
				height = y+measureY();
			}
		}
		
		public int measure(String str) {
			return (int)(IBMFontRenderer.measure(str)*scale);
		}
		
		public int measureY() {
			return (int)(8*scale);
		}
		
		public void push() {
			stack.addLast(clone());
		}
		
		public void pop() {
			PageRenderContext that = stack.removeLast();
			this.width = that.width;
			this.height = that.height;
			this.x = that.x;
			this.y = that.y;
			this.indent = that.indent;
			this.scale = that.scale;
		}
		
		@Override
		public PageRenderContext clone() {
			try {
				return (PageRenderContext) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError(e);
			}
		}
	}

	private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.BREAKING_WHITESPACE);
	
	private final String key;
	private final Node node;
	public Framebuffer fb;
	
	private int width;
	private int height;
	
	private List<ClickRegion> clickRegions = Lists.newArrayList();
	
	public DocumentationPage(String key, Node node) {
		this.key = key;
		this.node = node;
	}
	
	public ClickRegion getRegionClicked(int mouseX, int mouseY) {
		for (ClickRegion cr : clickRegions) {
			if (cr.intersects(mouseX, mouseY)) {
				return cr;
			}
		}
		return null;
	}
	
	public void measure() {
		Rectangle r = doRender(true);
		width = r.getWidth();
		height = r.getHeight();
	}
	
	public void render(int x, int y, int viewportX, int viewportY, int viewportWidth, int viewportHeight, int color) {
		if (fb == null) {
			measure();
			fb = new Framebuffer(getWidth()*2, getHeight()*2, false);
			
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.pushMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GlStateManager.pushMatrix();
			
			fb.bindFramebuffer(false);
			
			GlStateManager.viewport(0, 0, fb.framebufferWidth, fb.framebufferHeight);
			
			GlStateManager.clearColor(0, 0, 0, 0);
			GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.loadIdentity();
			GlStateManager.scale(1, -1, 1);
			GlStateManager.ortho(0, width, height, 0, 1000, 3000);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0, 0, -2000);
			
			GlStateManager.disableDepth();
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.15f);
			GlStateManager.pushMatrix();
			GlStateManager.disableCull();
			doRender(false);
			GlStateManager.popMatrix();
			
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
			GlStateManager.enableDepth();
			GlStateManager.enableBlend();
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GlStateManager.popMatrix();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		}
		fb.bindFramebufferTexture();
		GlStateManager.pushMatrix();
		GlStateManager.color(((color >> 16)&0xFF)/255f, ((color >> 8)&0xFF)/255f, (color&0xFF)/255f);
		Gui.drawModalRectWithCustomSizedTexture(x, y, viewportX, viewportY, viewportWidth, viewportHeight, fb.framebufferTextureWidth/2, fb.framebufferTextureHeight/2);
		GlStateManager.popMatrix();
	}
	
	private Rectangle doRender(boolean simulate) {
		Random rand = new Random(key.hashCode());
		PageRenderContext prc = new PageRenderContext();
		clickRegions.clear();
		node.accept(new AbstractVisitor() {
			@Override
			public void visit(Text text) {
				Node parent = text.getParent();
				int listItem = 0;
				int idx = 0;
				boolean unordered = false;
				boolean numbered = false;
				while (parent != null) {
					if (parent instanceof ListItem) {
						listItem++;
						Node prev = parent.getPrevious();
						if (idx == 0) {
							idx = 1;
							while (prev != null) {
								idx++;
								prev = prev.getPrevious();
							}
						}
					}
					if (parent instanceof BulletList) {
						unordered = true;
						numbered = false;
					} else if (parent instanceof OrderedList) {
						unordered = false;
						numbered = true;
					}
					parent = parent.getParent();
				}
				if (listItem > 0) {
					String prefix = null;
					if (unordered) {
						prefix = "• ";
					} else if (numbered) {
						prefix = idx+". ";
					}
					if (prefix != null && !simulate) {
						IBMFontRenderer.drawString(prc.x-prc.measure(prefix), prc.y, prefix, -1);
					}
				}
				drawStringWrapped(prc, text.getLiteral(), simulate);
				super.visit(text);
			}
			
			@Override
			public void visit(Heading heading) {
				prc.down();
				float oldScale = prc.scale;
				prc.scale = ((6-heading.getLevel())*0.2f)+1;
				super.visit(heading);
				prc.wrap();
				prc.scale = oldScale;
			}
			
			@Override
			public void visit(HardLineBreak hardLineBreak) {
				prc.wrap();
				super.visit(hardLineBreak);
			}
			
			@Override
			public void visit(BlockQuote blockQuote) {
				
			}
			
			@Override
			public void visit(BulletList bulletList) {
				prc.indent();
				super.visit(bulletList);
				prc.outdent();
				if (!(bulletList.getParent().getParent() instanceof BulletList)) {
					prc.wrap();
				}
			}
			
			@Override
			public void visit(Code code) {
				super.visit(code);
			}
			
			@Override
			public void visit(CustomBlock customBlock) {
				if (customBlock instanceof TableBlock) {
					
					return;
				}
				super.visit(customBlock);
			}
			
			@Override
			public void visit(CustomNode customNode) {
				int startX = prc.x;
				int startY = prc.y;
				super.visit(customNode);
				if (customNode instanceof Strikethrough) {
					if (!simulate) {
						Gui.drawRect(startX, startY+4, prc.x-1, prc.y+5, -1);
					}
				}
			}
			
			@Override
			public void visit(Document document) {
				// TODO Auto-generated method stub
				super.visit(document);
			}
			
			@Override
			public void visit(Emphasis emphasis) {
				// TODO Auto-generated method stub
				super.visit(emphasis);
			}
			
			@Override
			public void visit(FencedCodeBlock fencedCodeBlock) {
				// TODO Auto-generated method stub
				super.visit(fencedCodeBlock);
			}
			
			@Override
			public void visit(HtmlBlock htmlBlock) {
				// TODO Auto-generated method stub
				super.visit(htmlBlock);
			}
			
			@Override
			public void visit(HtmlInline htmlInline) {
				if (htmlInline.getLiteral().startsWith("<!--") &&
						htmlInline.getLiteral().endsWith("-->")) {
					String comment = htmlInline.getLiteral().substring(4, htmlInline.getLiteral().length()-3).trim();
					if (comment.startsWith("fuzz")) {
						int amt = Integer.parseInt(comment.substring(5));
						for (int i = 0; i < amt; i++) {
							char c = IBMFontRenderer.CP437.charAt(rand.nextInt(IBMFontRenderer.CP437.length()));
							if (prc.x+4 >= 225) {
								prc.wrap();
							}
							drawString(prc, Character.toString(c), simulate);
						}
					}
				}
				super.visit(htmlInline);
			}
			
			@Override
			public void visit(Image image) {
			}
			
			@Override
			public void visit(IndentedCodeBlock indentedCodeBlock) {
				// TODO Auto-generated method stub
				super.visit(indentedCodeBlock);
			}
			
			@Override
			public void visit(Link link) {
				int startX = prc.x;
				int startY = prc.y;
				super.visit(link);
				if (!simulate) {
					Gui.drawRect(startX, startY+7, prc.x-4, prc.y+8, -1);
				}
				clickRegions.add(new ClickRegion(new NavigateAction(link.getDestination()), startX, startY, prc.x-startX, (prc.y-startY)+8));
			}
			
			@Override
			public void visit(ListItem listItem) {
				super.visit(listItem);
				prc.y -= prc.measureY();
			}
			
			@Override
			public void visit(OrderedList orderedList) {
				prc.indent();
				super.visit(orderedList);
				prc.wrap();
				prc.outdent();
			}
			
			@Override
			public void visit(Paragraph paragraph) {
				super.visit(paragraph);
				prc.wrap();
				prc.wrap();
			}
			
			@Override
			public void visit(SoftLineBreak softLineBreak) {
				if (prc.x > 225) {
					prc.wrap();
				}
				super.visit(softLineBreak);
			}
			
			@Override
			public void visit(StrongEmphasis strongEmphasis) {
				int oldX = prc.x;
				int oldY = prc.y;
				super.visit(strongEmphasis);
				prc.x = oldX+1;
				prc.y = oldY;
				super.visit(strongEmphasis);
			}
			
			@Override
			public void visit(ThematicBreak thematicBreak) {
				// TODO Auto-generated method stub
				super.visit(thematicBreak);
			}
			
		});
		return new Rectangle(0, 0, prc.width, prc.height);
	}
	
	public String getKey() {
		return key;
	}

	protected void drawStringWrapped(PageRenderContext prc, String literal, boolean simulate) {
		for (String s : WHITESPACE_SPLITTER.split(literal)) {
			int w = (int)(IBMFontRenderer.measure(s)*prc.scale);
			if (prc.x+w+8 >= 225) {
				prc.wrap();
			}
			drawString(prc, s, simulate);
			prc.x += 4;
		}
	}

	private void drawString(PageRenderContext prc, String str, boolean simulate) {
		int w = prc.measure(str);
		int h = prc.measureY();
		if (width < prc.x+w) width = prc.x+w;
		if (height < prc.y+h) height = prc.y+h;
		if (!simulate) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(prc.scale, prc.scale, 1);
			IBMFontRenderer.drawString((int)(prc.x/prc.scale), (int)(prc.y/prc.scale), str, -1);
			GlStateManager.popMatrix();
		}
		prc.x += w;
		prc.stretch();
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void destroy() {
		if (fb != null) fb.deleteFramebuffer();
		fb = null;
	}

}
