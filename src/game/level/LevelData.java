package game.level;

import java.util.ArrayList;

import sk.gfx.SpriteSheet;
import sk.util.vector.Vector2f;

public class LevelData {
	
	// Level dimensions
	public int chunksX = -1;
	public int chunksY = -1;
	
	public int chunkSize = -1;
	
	// Sprite sheet
	public SpriteSheet spriteSheet;
	
	// Terrain
	public ArrayList<Vector2f[]> terrain;
	
}