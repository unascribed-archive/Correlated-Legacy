package com.elytradev.hallways;

import java.util.Random;

import javax.swing.JFrame;

import com.elytradev.correlated.math.Vec2i;
import com.elytradev.hallways.DungeonTile.TileType;

/**
 * @author Falkreon
 */
public class HallwayGenerator {
	
	private static Random defaultRandom = new Random();
	
	public static void main(String... args) {
		
		DungeonTile reserved = new DungeonTile(TileType.HALLWAY);
		DungeonTile roomz = new DungeonTile(TileType.ROOM);
		DungeonTile startRoom = new DungeonTile(TileType.ENTRANCE);
		DungeonTile goal = new DungeonTile(TileType.EXIT);
		
		final int COMPLEXITY_IDEAL = 40;
		
		VectorField<DungeonTile> selected = null;
		int highestComplexity = 0;
		int evaluated = 0;
		
		for(int i=0; i<40; i++) {
			evaluated++;
			VectorField<DungeonTile> field = new VectorField<>(64,64);
			int complexity = generateInto(field, reserved, roomz, startRoom, goal);
			
			if (selected==null || highestComplexity<complexity) {
				selected = field;
				highestComplexity = complexity;
			}
			
			field.getTag().setInteger("complexity", complexity);
			if (complexity>COMPLEXITY_IDEAL) {
				//break;
			}
		}
		
		System.out.println(""+evaluated+" plans evaluated, yielding a final complexity of "+highestComplexity);
		
		JFrame main = new JFrame("Hallway Simulator 20XX");
		main.add(new VectorPanel(selected, 16));
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.pack();
		main.setVisible(true);
		
	}
	
	public static int generateInto(VectorField<DungeonTile> field, DungeonTile hallTemplate, DungeonTile roomTemplate, DungeonTile startRoom, DungeonTile goal) {
		return generateInto(defaultRandom, field, hallTemplate, roomTemplate, startRoom, goal);
	}
	
	public static int generateInto(long seed, VectorField<DungeonTile> field, DungeonTile hallTemplate, DungeonTile roomTemplate, DungeonTile startRoom, DungeonTile goal) {
		return generateInto(new Random(seed), field, hallTemplate, roomTemplate, startRoom, goal);
	}
	
	public static int generateInto(Random rand, VectorField<DungeonTile> field, DungeonTile hallTemplate, DungeonTile roomTemplate, DungeonTile startRoom, DungeonTile goal) {
		int complexity = 1; //Freebie because of initial room
		
		Vec2i curNode = new Vec2i(10,10);
		Cardinal curDirection = Cardinal.NORTH;
		
		Vec2i initialRoom = genRoom(rand, field, curNode.x, curNode.y, 2, 5, startRoom, 5);
		if (initialRoom!=null) {
			curDirection = Cardinal.fromTo(curNode, initialRoom).cw().cw(); //ensure that halls facing directly outwards add complexity
			curNode = initialRoom;
		}
		
		for(int i=0; i<120; i++) {
			
			if (rand.nextInt(8)==0) {
				Vec2i deadEnd = genHall(rand, field, curNode.x, curNode.y, 2, 8, hallTemplate, roomTemplate, 5);
				if (deadEnd!=null) {
					complexity+= 2; //Branches are worth double complexity
					genHall(rand, field, deadEnd.x, deadEnd.y, 2, 8, hallTemplate, roomTemplate, 5);
				}
			}
			
			Vec2i next = genHall(rand, field, curNode.x, curNode.y, 2, 6, hallTemplate, roomTemplate, 5);
			if (next!=null) {
				Cardinal newDirection = Cardinal.fromTo(curNode, next);
				if (newDirection!=curDirection) complexity++;
				
				
				/*
				 * At this point, you could bail at a certain maximum-complexity to prevent one
				 * player from getting the stronghold to end all strongholds.
				 *
				 */
				
				
				curDirection = newDirection;
				
				curNode = next;
				
				field.put(curNode.x, curNode.y, null);
				next = genRoom(rand, field, curNode.x, curNode.y, 2, 5, roomTemplate, 5);
				if (next!=null) {
					curDirection = Cardinal.fromTo(curNode, next).cw().cw();
					curNode = next;
					complexity++;
				} else {
					//No room this time around, but replace the node for safety.
					field.put(curNode.x, curNode.y, roomTemplate);
					
					
				}
				
			} else {
				//We lost this round completely. Overall complexity will suffer if this happens a lot.
				
			}
		}
		
		if (!field.isInBounds(curNode.x, curNode.y)) {
			//No wonder we're having problems! MULLIGAN!
			return 0;
		}
		
		field.put(curNode.x, curNode.y, goal);
		genRoom(rand, field, curNode.x, curNode.y, 2, 5, goal, 5);
		
		//System.out.println("Complexity: "+complexity+" pieces");
		return complexity;
	}
	

	
	
	public static Vec2i spitball(Random rand, VectorField<DungeonTile> dungeon, int x, int y, int minLength, int maxLength, final DungeonTile hall, final DungeonTile node) {
		
		int x1 = x;
		int y1 = y;
		int x2 = x;
		int y2 = y;
		int deltaLength = maxLength-minLength;
		
		int dir = rand.nextInt(4);
		Cardinal flow = Cardinal.NORTH;
		int len = rand.nextInt(deltaLength)+minLength;
		
		switch(dir) {
		case 0: //+X
			x1++;
			x2 = x1 + len;
			flow = Cardinal.EAST;
			break;
		case 1: //+Y
			y1++;
			y2 = y1 + len;
			flow = Cardinal.SOUTH;
			break;
		case 2: //-X
			x1--;
			x2 = x1 - len;
			flow = Cardinal.WEST;
			break;
		default:
		case 3: //-Y
			y1--;
			y2 = y1 - len;
			flow = Cardinal.NORTH;
			break;
		}
		
		if (!dungeon.isInBounds(x2, y2)) return null; //failed because we threw a ball OOB
		
		//We're pointed at somewhere ACTUALLY IN the map. Test for problems
		final MutableCollisionResult collision = new MutableCollisionResult();
		dungeon.visitLine(x1, y1, x2, y2,
				(VectorField<DungeonTile> field, int xi, int yi)->{
					if (field.get(xi, yi)!=null) collision.result=true;
				});
		if (dungeon.get(x2, y2)!=null) collision.result=true;
		
		if (collision.result) {
			//System.out.println("Failed hallway!");
			return null; //We tried to generate a corridor but it ran over existing map.
		} else {
			DungeonTile hallModified = hall.clone();
			hallModified.setExits(flow.cw(), flow.ccw());
			
			dungeon.visitLine(x1, y1, x2, y2,
					(VectorField<DungeonTile> field, int xi, int yi)->{
						field.put(xi, yi, hallModified);
					});
			dungeon.put(x2, y2, node); //overwrite the end of the hall with a node.
			return new Vec2i(x2,y2);
		}
	}
	
	/** Turn a 1-square node into a room **/
	public static Vec2i spitballRoom(Random rand, VectorField<DungeonTile> dungeon, int x, int y, int minSize, int maxSize, final DungeonTile material) {
		//this is an interesting problem: We're trying to create a rectangle with the given coordinates on its border.
		//We're going to project the box out in a direction, then slide it along the opposite axis before finally committing to a hit test.
		
		
		int sizeDelta = maxSize-minSize;
		int x1 = x;
		int y1 = y;
		int x2 = x;
		int y2 = y;
		
		int len = rand.nextInt(sizeDelta) + minSize;
		int breadth = len - (len/4); if (breadth<minSize) breadth=minSize;
		if (rand.nextBoolean()) {
			int tmp = len;
			len = breadth;
			breadth = tmp;
		}
		int halfBreadth = breadth/2;
		
		int dir = rand.nextInt(4);
		switch(dir) {
		case 0: //+X
			//x1++;
			x2 = x1 + len;
			y1-=halfBreadth;
			y2+=halfBreadth;
			break;
		case 1: //+Y
			//y1++;
			y2 = y1 + len;
			x1-=halfBreadth;
			x2+=halfBreadth;
			break;
		case 2: //-X
			//x1--;
			x2 = x1 - len;
			y1-=halfBreadth;
			y2+=halfBreadth;
			break;
		default:		
		case 3: //-Y
			//y1--;
			y2 = y1 - len;
			x1-=halfBreadth;
			x2+=halfBreadth;
			break;
		}
		
		if (!dungeon.isInBounds(x1, y1) || !dungeon.isInBounds(x2, y2)) {
			return null;
		}
		
		int width = Math.abs(x2-x1)+1;
		int height = Math.abs(y2-y1)+1;
		
		//We now have a generated Room.
		final MutableCollisionResult collision = new MutableCollisionResult();
		dungeon.visitRect(x1, y1, width, height,
				(VectorField<DungeonTile> field, int xi, int yi)->{
					if (field.get(xi, yi)!=null) collision.result=true;
				});
		
		
		if (collision.result) {
			return null;
		}
		
		dungeon.visitRect(x1, y1, width, height,
				(VectorField<DungeonTile> field, int xi, int yi)->{
					field.put(xi, yi, material);
				});
		
		//We've pasted in the rectangle, now we project out to a side
		Vec2i result = new Vec2i(x,y);
		int dir2 = rand.nextInt(3);
		if (dir2>=dir) dir2++; //anything but the side we already picked
		switch(dir2) {
		case 0: //-X
			result.x = x1; result.y = y1 + (height/2)+1;
			break;
		case 1: //-Y
			result.y = y1; result.x = x1 + (width/2)+1;
			break;
		case 2: //+X
			result.x = x2; result.y = y1 + (height/2)+1;
			break;
		default:		
		case 3: //+Y
			result.y = y2; result.x = x1 + (width/2)+1;
			break;
		}
		
		return result;
	}
	
	public static Vec2i genRoom(Random rand, VectorField<DungeonTile> dungeon, int x, int y, int minSize, int maxSize, final DungeonTile material, int tries) {
		for(int i=0; i<tries; i++) {
			Vec2i result = spitballRoom(rand, dungeon, x, y, minSize, maxSize, material);
			if (result!=null) return result;
		}
		return null;
	}
	
	public static Vec2i genHall(Random rand, VectorField<DungeonTile> dungeon, int x, int y, int minLength, int maxLength, final DungeonTile hall, final DungeonTile node, int tries) {
		for(int i=0; i<tries; i++) {
			Vec2i result = spitball(rand, dungeon, x, y, minLength, maxLength, hall, node);
			if (result!=null) return result;
		}
		return null;
	}
	
	private static class MutableCollisionResult {
		public boolean result = false;
	}
}
