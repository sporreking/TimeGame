package sk.util.io;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import sk.game.Window;
import sk.util.io.Keyboard.KeyState;
import sk.util.vector.Matrix4f;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector4f;

public class Mouse extends GLFWMouseButtonCallback {
	
	private static boolean changed = false;
	private static final HashMap<Integer, KeyState> states = new HashMap<>();

	@Override
	public void invoke(long window, int button, int action, int mods) {
		
		if(action == GLFW_PRESS) {
			states.put(button, KeyState.PRESSED);
		} else if(action == GLFW_RELEASE) {
			states.put(button, KeyState.RELEASED);
		}
		
		changed = true;
	}
	
	
	/**
	 * 
	 * Returns whether or not the key is currently down.
	 * This will even return true when the key was pressed, however not when it was released.
	 * 
	 * @param key the key to check on.
	 * @return true if the key is currently down.
	 */
	public static final boolean down(int key) {
		if(states.containsKey(key))
			if(states.get(key) != KeyState.RELEASED)
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * Should not be called by the user.
	 * Changes pressed keys to the down state, and removes released keys.
	 * 
	 */
	public static final void _update() {
		ArrayList<Integer> trash = new ArrayList<>();
		
		for(int key : states.keySet()) {
			if(states.get(key) == KeyState.PRESSED)
				states.put(key, KeyState.DOWN);
			else if(states.get(key) == KeyState.RELEASED)
				trash.add(key);
		}
		
		for(int key : trash)
			states.remove(key);
		
		trash.clear();
		
		changed = false;
	}
	
	/**
	 * 
	 * Returns true if the specified key is pressed.
	 * 
	 * @param key The key to check.
	 * @return If the key is pressed.
	 */
	public static final boolean pressed(int key) {
		if(states.containsKey(key))
			return states.get(key) == KeyState.PRESSED;
		
		return false;
	}
	
	/**
	 * 
	 * Returns true if the specified key is released.
	 * 
	 * @param key The key to check.
	 * @return If the key is pressed.
	 */
	public static final boolean released(int key) {
		if(states.containsKey(key))
			if(states.get(key) == KeyState.RELEASED)
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * Returns the current mouse position.
	 * 
	 * @return the current mouse position.
	 */
	public static Vector2f getPosition() {
		return new Vector2f((float) Cursor.x, (float) Cursor.y);
	}
	
	/**
	 * 
	 * Returns the current mouse position after passing it through a projection.
	 * 
	 * @return the projected mouse position.
	 */
	public static Vector2f projectPosition(Matrix4f projection) {
		
		Vector4f vec = Matrix4f.transform(projection,
				new Vector4f(2.0f * ((float) Cursor.x) / Window.getWidth() - 1,
						1 - 2.0f * ((float) Cursor.y) / Window.getHeight(), 0, 1), null);
		
		return new Vector2f(vec.x, vec.y);
	}
	
	/**
	 * 
	 * Returns whether or not the mouse position has been changed and/or a mouse button has been pressed
	 * since the previous frame.
	 * 
	 * @return {@code true} if a mouse button has been pressed and/or a the mouse position has changed.
	 */
	public static final boolean wasChanged() {
		return changed;
	}
	
	public static final GLFWMouseButtonCallback INSTANCE = new Mouse();
	
	
	public static final class Cursor extends GLFWCursorPosCallback {

		private static double x, y;
		
		@Override
		public void invoke(long window, double xPos, double yPos) {	
			x = xPos;
			y = yPos;
			Mouse.changed = true;
		}
		
		public static final GLFWCursorPosCallback INSTANCE = new Cursor();
		
	}
}
