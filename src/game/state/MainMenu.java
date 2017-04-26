package game.state;

import org.lwjgl.glfw.GLFW;

import game.TG;
import sk.gamestate.GameState;
import sk.gamestate.GameStateManager;
import sk.util.io.Keyboard;

public class MainMenu implements GameState {
	
	@Override
	public void init() {
		
	}
	
	@Override
	public void update(double delta) {
		if(Keyboard.pressed(GLFW.GLFW_KEY_L))
			GameStateManager.enterState(TG.GS_PLAYING);
	}
	
	@Override
	public void draw() {
		
	}
	
	@Override
	public void exit() {
		
	}
}