package game.state;

import java.io.File;
import java.util.ArrayList;

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
	
	public static final String PREFIX_URL = "res/level/";
	
	private Player player1, player2;
	private Level level;
	
	//TODO: Change to dynamic choice
	public String chapter = "test";
	
	private ArrayList<String> levels;
	private int current;
	
	@Override
	public void init() {
		Camera.DEFAULT.scale.x = .75f;
		Camera.DEFAULT.scale.y = .75f;
		
		player1 = new Player(true);
		player2 = new Player(false);
		
		setupChapter();
		
		playLevel();
		
	}
	
	private void playLevel() {
		String prefix = chapter + "/" + levels.get(current);
		level = new Level(player1, player2, LevelLoader.load(prefix + "_0"),
				LevelLoader.load(prefix + "_1"));
	}
	
	private void setupChapter() {
		levels = new ArrayList<>();
		
		File base = new File(PREFIX_URL + chapter + "/");
		
		for(String s : base.list()) {
			if(s.matches("^lvl[0-9]+_0\\.png")) {
				levels.add(s.substring(0, 4));
			}
		}
		
		current = 0;
	}
	
	@Override
	public void update(double delta) {
		level.update(delta);
	}
	
	@Override
	public void draw() {
		level.draw();
	}
	
	@Override
	public void exit() {
		level.destroy();
		player1.destroy();
		player2.destroy();
	}
}