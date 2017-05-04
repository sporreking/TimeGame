package sk.gamestate;

import sk.game.Game;
import sk.game.Window;
import sk.util.io.Keyboard;
import sk.util.io.Mouse;

import static org.lwjgl.glfw.GLFW.*;

public final class GameStateManager {
	
	private static GameState currentState = null;
	private static GameState stateToEnter = null;
	private static boolean shouldEnterNewState = false;
	
	/**
	 * 
	 * Enters the specified game state when the current frame is complete.
	 * 
	 * @param gs the game state to enter.
	 */
	public static final void enterState(GameState gs) {
		stateToEnter = gs;
		shouldEnterNewState = true;
	}
	
	/**
	 * 
	 * Called each frame by the main loop. Manages the current state and its functions.
	 * 
	 * @param delta the time passed since the previous frame.
	 */
	public static final void update(double delta) {
		if(shouldEnterNewState) {
			if(currentState != null)
				currentState.exit();
			
			currentState = stateToEnter;
			currentState.init();
			
			shouldEnterNewState = false;
		}
		
		if(currentState == null)
			return;
		
		Keyboard._update();
		Mouse._update();
		glfwPollEvents();
		
		currentState.update(delta);
		
		Window.clear();
		
		currentState.draw();
		
		Window.swapBuffers();
		
		if(!Game.isRunning())
			currentState.exit();
	}
	
}