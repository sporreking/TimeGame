package sk.game;

/*
 * TODO: Add toString() methods to each class
 */

import sk.gamestate.GameState;
import sk.util.vector.Vector4f;

/**
 * 
 * This class is used to supply properties to the engine upon game start.
 * 
 * @author Alfred Sporre
 *
 */
public class GameProperties {
	
	public String title = new String();
	public GameState startState;
	public boolean resizable = false;
	public int width = 800;
	public int height = 600;
	public boolean vSync = true;
	public Vector4f clearColor = new Vector4f(0, 0, 0, 1);
	
}