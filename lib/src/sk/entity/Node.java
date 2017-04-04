package sk.entity;

public abstract class Node {
	
	/**
	 * 
	 * Updates this node.
	 * 
	 * @param delta the time passed since the previous frame.
	 */
	public abstract void update(double delta);
	
	/**
	 * 
	 * Creates a draw call for each subsequent node.
	 * 
	 */
	public abstract void draw();
	
	/**
	 * 
	 * Destroys all subsequent nodes.
	 * 
	 */
	public abstract void destroy();
}