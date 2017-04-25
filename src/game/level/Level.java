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
	
	public World world;
	
	public Entity player;
	
	public Level(Entity player, LevelData... levelData) {
		this.player = player;
		this.data = levelData;
		
		world = new World();
		world.gravity = new Vector2f();
		
		chunks = new Chunk[2][data[0].chunksY][data[0].chunksX];
		
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[0][i][j] = new Chunk(j, i, data[0].spriteSheet.getTexture(j, i));
				chunks[1][i][j] = new Chunk(j, i, data[1].spriteSheet.getTexture(j, i));
			}
		}
		
		for(int i = 0; i < data[0].terrain.size(); i++) {
			Transform t = new Transform();
			t.position.x += .5f;
			
			Body b = new Body(new Shape(data[0].terrain.get(i)), 1, 0, 0);
			b.setDynamic(false);
			b.decoupple(t);
			world.addBody(b);
		}
		
		world.addBody(player.get(Body.class));
	}
	
	@Override
	public void update(double delta) {
		
		if(Keyboard.pressed(GLFW.GLFW_KEY_I)) {
			currentSheet++;
			currentSheet %= 2;
		}
		
		world.update(delta);
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