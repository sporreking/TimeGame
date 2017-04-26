package game.state;

import org.lwjgl.glfw.GLFW;

import game.level.Level;
import game.level.LevelLoader;
import sk.entity.Entity;
import sk.gamestate.GameState;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Shape;
import sk.physics.World;
import sk.util.io.Keyboard;
import sk.util.vector.Vector2f;

public class Playing implements GameState {
	
	private Entity player;
	private Level level;
	
	@Override
	public void init() {
		Camera.DEFAULT.scale.x = .25f;
		Camera.DEFAULT.scale.y = .25f;
		
		player = new Entity();
		
		Transform t_player = new Transform();
		t_player.scale.set(1f / 128 * 8, 1f / 128 * 12);
		
		player.add(0, t_player);
		player.add(0, new Body(new Shape(new Vector2f[] {
				new Vector2f(-.5f, .5f),
				new Vector2f(.5f, .5f),
				new Vector2f(.5f, -.5f),
				new Vector2f(-.5f, -.5f)
		}), 1, 100, 0));
		player.add(1, new Renderer(Mesh.QUAD).setTexture(new Texture("res/texture/wood.png")));
		
		level = new Level(player, LevelLoader.load("lvl1_0"), LevelLoader.load("lvl1_1"));
	}
	
	@Override
	public void update(double delta) {
		
		if(Keyboard.down(GLFW.GLFW_KEY_W)) {
			Camera.DEFAULT.position.y += delta;
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_S)) {
			Camera.DEFAULT.position.y -= delta;
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_A)) {
			Camera.DEFAULT.position.x -= delta;
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_D)) {
			Camera.DEFAULT.position.x += delta;
		}
		
		if(Keyboard.pressed(GLFW.GLFW_KEY_UP)) {
			player.get(Body.class).addForce(new Vector2f(0, .5f));
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_DOWN)) {
			player.get(Body.class).addForce(new Vector2f(0, -(float) (2f * delta)));
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_LEFT)) {
			Vector2f v = player.get(Body.class).getVelocity();
			v.x = (float) (-.5f);
			player.get(Body.class).setVelocity(v);
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_RIGHT)) {
			Vector2f v = player.get(Body.class).getVelocity();
			v.x = (float) (.5f);
			player.get(Body.class).setVelocity(v);
		}
		
		Camera.DEFAULT.position = player.get(Transform.class).position;
		
		level.update(delta);
		player.update(delta);
	}
	
	@Override
	public void draw() {
		player.draw();
		level.draw();
	}
	
	@Override
	public void exit() {
		level.destroy();
		player.destroy();
	}
}