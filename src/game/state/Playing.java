package game.state;

import org.lwjgl.glfw.GLFW;

import game.level.Level;
import game.level.LevelLoader;
import player.Movement;
import player.Player;
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
import sk.util.vector.Vector3f;

public class Playing implements GameState {
	
	private Player player1, player2;
	private Level level;
	
	@Override
	public void init() {
		Camera.DEFAULT.scale.x = .75f;
		Camera.DEFAULT.scale.y = .75f;
		
		player1 = new Player(true);
		player2 = new Player(false);
		
		level = new Level(player1, player2, LevelLoader.load("lvl1_0"), LevelLoader.load("lvl1_1"));
	}
	
	@Override
	public void update(double delta) {
		level.update(delta);
	}
	
	@Override
	public void draw() {
		level.draw();
		//level.terrain[0]._draw();
	}
	
	@Override
	public void exit() {
		level.destroy();
		player1.destroy();
		player2.destroy();
	}
}