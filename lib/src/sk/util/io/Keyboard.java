package sk.util.io;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Keyboard extends GLFWKeyCallback {
	
	private static final HashMap<Integer, KeyState> states = new HashMap<>();
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if(action == GLFW_PRESS) {
			states.put(key, KeyState.PRESSED);
		} else if(action == GLFW_RELEASE) {
			states.put(key, KeyState.RELEASED);
		}
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
	}
	
	/**
	 * 
	 * Checks if the specified key was pressed this frame.
	 * 
	 * @param key the key code to check.
	 * @return {@code true} if the key was pressed.
	 */
	public static final boolean pressed(int key) {
		if(states.containsKey(key))
			return states.get(key) == KeyState.PRESSED;
		
		return false;
	}
	
	/**
	 * 
	 * Checks if the specified key was released this frame.
	 * 
	 * @param key the key code to check.
	 * @return 
	 */
	public static final boolean released(int key) {
		if(states.containsKey(key))
			if(states.get(key) == KeyState.RELEASED)
				return true;
		
		return false;
	}
	
	public static enum KeyState {
		DOWN, PRESSED, RELEASED;
	}
	
	public static final GLFWKeyCallback INSTANCE = new Keyboard();
}