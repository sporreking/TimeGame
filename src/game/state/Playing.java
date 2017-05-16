package game.state;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.lwjgl.glfw.GLFW;

import game.TG;
import game.level.Level;
import game.level.LevelLoader;
import game.level.player.Player;
import sk.audio.Audio;
import sk.game.Game;
import sk.gamestate.GameState;
import sk.gamestate.GameStateManager;
import sk.gfx.Camera;
import sk.util.io.Keyboard;

public class Playing implements GameState {
	
	public static final String PREFIX_URL = "res/level/";
	
	private Player player1, player2;
	private Level level;
	
	//TODO: Change to dynamic choice
	public String chapter = "1";
	
	private ArrayList<String> levels;
	public int current;
	
	@Override
	public void init() {
		Camera.DEFAULT.scale.x = .75f;
		Camera.DEFAULT.scale.y = .75f;
		
		setupChapter();
		
		playLevel();
	}
	
	public void playLevel() {
		if (player1 != null)
			player1.destroy();
		if(player2 != null)
			player2.destroy();
		
		player1 = new Player(true);
		player2 = new Player(false);
		
		setupChapter();
		
		String prefix = chapter + "/" + levels.get(current);
		level = new Level(player1, player2, LevelLoader.load(prefix + "_0"),
				LevelLoader.load(prefix + "_1"));
	}
	
	private void setupChapter() {
		levels = new ArrayList<>();
		
		File base = new File(PREFIX_URL + chapter + "/");
		
		for(String s : base.list()) {
			if(s.matches("^lvl[0-9]+_0\\.png")) {
				levels.add(s.substring(0, s.length() - 6));
			}
		}
		
		// Files got jumbled and placed in a weird order, so we sort.
		Collections.sort(levels);
	}
	
	Audio s;
	
	@Override
	public void update(double delta) {
		level.update(delta);
		
		
		if (Keyboard.pressed(GLFW.GLFW_KEY_ESCAPE))
			GameStateManager.enterState(TG.GS_MAIN_MENU);
		
		if (Keyboard.pressed(GLFW.GLFW_KEY_R)) {
			playLevel();
		}
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
	
	public String getPath() {
		return PREFIX_URL + chapter + "/";
	}
	
	public String id() {
		return levels.get(current).substring(3, levels.get(current).length() - 2);
	}
	
	public Level getCurrentLevel() {
		return level;
	}
	
	public void nextLevel() {
		current++;
		
		if(current < levels.size()) {
			playLevel();
		} else {
			GameStateManager.enterState(TG.GS_MAIN_MENU);
		}
	}
}