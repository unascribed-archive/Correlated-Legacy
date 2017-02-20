package com.elytradev.hallways;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class VectorPanel extends JPanel {
	private static final Color OOB = new Color(0.4f, 0.4f, 0.4f);
	
	private final VectorField<DungeonTile> dungeon;
	private int zoom;
	
	public VectorPanel(VectorField<DungeonTile> dungeon, int zoom) {
		super();
		this.dungeon = dungeon;
		this.zoom = zoom;
		
		Dimension actualSize = new Dimension(dungeon.getWidth()*zoom, dungeon.getHeight()*zoom);
		this.setMinimumSize(actualSize);
		this.setPreferredSize(actualSize);
		this.setMaximumSize(actualSize);
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(OOB);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for(int y=0; y<dungeon.getHeight(); y++) {
			for(int x=0; x<dungeon.getWidth(); x++) {
				DungeonTile tile = dungeon.get(x, y);
				if (tile!=null) {
					//TODO: Walls
					g.setColor(new Color(tile.type.color));
					g.fillRect(x*zoom, y*zoom, zoom, zoom);
					g.setColor(new Color(tile.type.color).darker());
					if (tile.exits().contains(Cardinal.WEST)) g.fillRect(x*zoom, y*zoom, 2, zoom);
					if (tile.exits().contains(Cardinal.EAST)) g.fillRect(x*zoom + zoom-2, y*zoom, 2, zoom);
					if (tile.exits().contains(Cardinal.NORTH)) g.fillRect(x*zoom, y*zoom, zoom, 2);
					if (tile.exits().contains(Cardinal.SOUTH)) g.fillRect(x*zoom, y*zoom + zoom-2, zoom, 2);
					//g.drawRect(x*zoom, y*zoom, zoom-1, zoom-1);
				}
			}
		}
	}
}
