package sk.game;

import static org.lwjgl.opengl.GL11.*;

import sk.audio.AudioManager;
import sk.gamestate.GameState;
import sk.gamestate.GameStateManager;
import sk.gfx.Mesh;
import sk.gfx.ShaderProgram;
import sk.gfx.Texture;

/**
 * 
 * This class is used to start the engine.
 * 
 * @author Alfred Sporre
 *
 */
public final class Game {
	
	protected static GameProperties properties;
	
	protected static boolean running = false;
	
	/**
	 * 
	 * Starts the game and enters the main loop.
	 * 
	 * @param properties information to launch the game with.
	 */
	public static final void start(GameProperties properties) {
		Game.properties = properties;
		
		Window.create();
		initGL();
		
		Window.show();
		
		GameStateManager.enterState(properties.startState);
		
		running = true;
		while(running) {
			if(Window.shouldClose())
				running = false;
			loop();
		}
		
		destroy();
	}
	
	/**
	 * 
	 * Sets up general OpenGL properties.
	 * 
	 */
	private static final void initGL() {
		System.out.println("OpenGL v." + glGetString(GL_VERSION));
		
		glEnable(GL_TEXTURE_2D);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	/**
	 * 
	 * Main loop, called each frame.
	 * 
	 */
	private static final void loop() {
		Time.update();
		GameStateManager.update(Time.getDelta());
	}
	
	/**
	 * 
	 * Tells the game to exit after the frame has completed.
	 * The {@link GameState#exit()} method will be called before exiting the loop.
	 * 
	 */
	public static final void stop() {
		running = false;
	}
	
	/**
	 * 
	 * Returns whether or not the game is running, or should be exited.
	 * 
	 * @return {@code true} if the game is running and should not be exited.
	 */
	public static final boolean isRunning() {
		return running;
	}
	
	/**
	 * 
	 * Deletes all engine-created OpenGL objects.
	 * 
	 */
	private static final void destroy() {
		Mesh.destroyAll();
		Texture.destroyAll();
		ShaderProgram.destroyAll();
		AudioManager.destroy();
	}
}