package com.elytradev.correlated.client;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
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

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.shader.Framebuffer;

public class DocumentationPage {

	private final Node node;
	private Framebuffer fb;
	
	private int width;
	private int height;
	
	public DocumentationPage(Node node) {
		this.node = node;
		measure();
	}
	
	public void measure() {
		doRender(true);
	}
	
	public void render(int x, int y, int viewportX, int viewportY, int viewportWidth, int viewportHeight, int color) {
		destroy();
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
	
	private void doRender(boolean simulate) {
		width = 480;
		height = 0;
		MutableInt x = new MutableInt(0);
		MutableInt y = new MutableInt(0);
		MutableFloat scale = new MutableFloat(1);
		node.accept(new AbstractVisitor() {
			@Override
			public void visit(Text text) {
				String str = text.getLiteral();
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
					if (unordered) {
						str = Strings.repeat(" ", listItem)+"• "+str;
					} else if (numbered) {
						str = Strings.repeat(" ", listItem)+idx+". "+str;
					}
				}
				drawStringWrapped(x, y, scale, str, simulate);
				super.visit(text);
			}
			
			@Override
			public void visit(Heading heading) {
				float oldValue = scale.floatValue();
				scale.setValue(((6-heading.getLevel())*0.25f)+1);
				super.visit(heading);
				x.setValue(0);
				y.add(8*scale.floatValue());
				scale.setValue(oldValue);
			}
			
			@Override
			public void visit(HardLineBreak hardLineBreak) {
				x.setValue(0);
				y.add(8*scale.floatValue());
				super.visit(hardLineBreak);
			}
			
			@Override
			public void visit(BlockQuote blockQuote) {
				
			}
			
			@Override
			public void visit(BulletList bulletList) {
				super.visit(bulletList);
				if (!(bulletList.getParent().getParent() instanceof BulletList)) {
					x.setValue(0);
					y.add(8*scale.floatValue());
				}
			}
			
			@Override
			public void visit(Code code) {
				// TODO Auto-generated method stub
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
				int startX = x.intValue();
				int startY = y.intValue();
				super.visit(customNode);
				if (customNode instanceof Strikethrough) {
					if (!simulate) {
						Gui.drawRect(startX, startY+4, x.intValue()-1, y.intValue()+5, -1);
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
				// TODO Auto-generated method stub
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
				// TODO Auto-generated method stub
				super.visit(link);
			}
			
			@Override
			public void visit(ListItem listItem) {
				// TODO Auto-generated method stub
				super.visit(listItem);
			}
			
			@Override
			public void visit(OrderedList orderedList) {
				super.visit(orderedList);
				x.setValue(0);
				y.add(8*scale.floatValue());
			}
			
			@Override
			public void visit(Paragraph paragraph) {
				x.setValue(0);
				y.add(8*scale.floatValue());
				super.visit(paragraph);
			}
			
			@Override
			public void visit(SoftLineBreak softLineBreak) {
				// TODO Auto-generated method stub
				super.visit(softLineBreak);
			}
			
			@Override
			public void visit(StrongEmphasis strongEmphasis) {
				int oldX = x.intValue();
				super.visit(strongEmphasis);
				x.setValue(oldX+1);
				super.visit(strongEmphasis);
			}
			
			@Override
			public void visit(ThematicBreak thematicBreak) {
				// TODO Auto-generated method stub
				super.visit(thematicBreak);
			}
		});
	}

	protected void drawStringWrapped(MutableInt x, MutableInt y, MutableFloat scale, String literal, boolean simulate) {
		int itr = 0;
		while (true) {
			itr++;
			if (itr > 200) {
				System.err.println("INFINITE LOOP");
				break;
			}
			if (literal.isEmpty()) break;
			int maxW = 210;
			if (x.intValue() >= maxW-2) {
				x.setValue(0);
				y.add(8*scale.floatValue());
			}
			String str = literal.substring(0, Math.min(literal.length(), (int)Math.ceil((maxW-x.intValue())/(8*scale.floatValue()))));
			literal = literal.substring(str.length());
			drawString(x, y, scale, str, simulate);
		}
	}

	private void drawString(MutableInt x, MutableInt y, MutableFloat scale, String str, boolean simulate) {
		int w = (int)(IBMFontRenderer.measure(str)*scale.floatValue());
		int h = (int)(8*scale.floatValue());
		if (width < x.intValue()+w) width = x.intValue()+w;
		if (height < y.intValue()+h) height = y.intValue()+h;
		if (!simulate) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(scale.floatValue(), scale.floatValue(), 1);
			IBMFontRenderer.drawString((int)(x.intValue()/scale.floatValue()), (int)(y.intValue()/scale.floatValue()), str, -1);
			GlStateManager.popMatrix();
		}
		x.add(w);
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
