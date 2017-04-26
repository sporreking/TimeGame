package game.level;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import sk.entity.Container;
import sk.entity.Entity;
import sk.entity.Node;
import sk.entity.Root;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.gfx.Vertex2D;
import sk.physics.Body;
import sk.physics.Shape;
import sk.physics.World;
import sk.util.io.Keyboard;
import sk.util.vector.Vector2f;

public class Level extends Node {
	
	public LevelData[] data;
	
	public int currentSheet = 0;
	
	public Chunk[][][] chunks;
	public World[] worlds;
	public Body[] terrain;
	
	public Entity player;
	
	public Level(Entity player, LevelData... levelData) {
		this.player = player;
		this.data = levelData;
		
		worlds = new World[levelData.length];
		terrain = new Body[levelData.length];
		
		for(int i = 0; i < worlds.length; i++) {
			worlds[i] = new World();
			worlds[i].gravity = new Vector2f(0, -1f);
		}
		
		chunks = new Chunk[2][data[0].chunksY][data[0].chunksX];
		
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[0][i][j] = new Chunk(j, i, data[0].spriteSheet.getTexture(j, i));
				chunks[1][i][j] = new Chunk(j, i, data[1].spriteSheet.getTexture(j, i));
			}
		}
		
		for(int i = 0; i < levelData.length; i++) {
			
			ArrayList<Shape> shapes = new ArrayList<>();
			
			for(int j = 0; j < data[i].terrain.size(); j++) {
				
				shapes.add(new Shape(data[i].terrain.get(j)));
				
			}
			
			Transform t = new Transform();
			t.position.x += .5f;
			
			terrain[i] = new Body(shapes.get(0), 1, 100, 0);
			
			if(shapes.size() > 1) {
				for(int j = 1; j < shapes.size(); j++) {
					terrain[i].addShape(shapes.get(j));
				}
			}
			
			terrain[i].setDynamic(false);
			terrain[i].decoupple(t);
			worlds[i].addBody(terrain[i]);
			worlds[i].addBody(player.get(Body.class));
		}
	}
	
	private void switchTime() {
		currentSheet++;
		currentSheet %= 2;
	}
	
	private void checkTerrainBlockage() {
		if(player.get(Body.class).hasDeepCollision(1f / 128 * 5)) {
			System.out.println("DIE!");
		}
	}
	
	@Override
	public void update(double delta) {
		
		if(Keyboard.pressed(GLFW.GLFW_KEY_I)) {
			switchTime();
		}
		
		worlds[currentSheet].update(delta);
		
		checkTerrainBlockage();
	}
	
	@Override
	public void draw() {
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[currentSheet][i][j].draw();
			}
		}
	}
	
	@Override
	public void destroy() {
		for(int i = 0; i < data.length; i++)
			data[i].spriteSheet.destroy();
	}
}