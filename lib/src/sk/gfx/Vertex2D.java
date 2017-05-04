package sk.gfx;

import sk.util.vector.Vector2f;

public class Vertex2D extends Vertex {
	
	/**
	 * 
	 * Creates a 2-dimension vertex with x, y and texture coordinates specified.
	 * 
	 * @param xy the position of the vertex.
	 * @param st the texture coordinates of the vertex.
	 */
	public Vertex2D(Vector2f xy, Vector2f st) {
		components.add(xy);
		components.add(st);
	}
	
	/**
	 * 
	 * Creates a 2-dimension vertex with x, y and texture coordinates specified.
	 * 
	 * @param xy the position of the vertex.
	 * @param s the horizontal texture coordinate.
	 * @param t the vertical texture coordinate.
	 */
	public Vertex2D(Vector2f xy, float s, float t) {
		this(xy, new Vector2f(s, t));
	}
	
	/**
	 * 
	 * Creates a 2-dimension vertex with x, y and texture coordinates specified.
	 * 
	 * @param x the x coordinate of the vertex position.
	 * @param y the y coordinate of the vertex position.
	 * @param st the texture coordinates of the vertex.
	 */
	public Vertex2D(float x, float y, Vector2f st) {
		this(new Vector2f(x, y), st);
	}
	
	/**
	 * 
	 * Creates a 2-dimension vertex with x, y and texture coordinates specified.
	 * 
	 * @param x the x coordinate of the vertex position.
	 * @param y the y coordinate of the vertex position.
	 * @param s the horizontal texture coordinate.
	 * @param t the vertical texture coordinate.
	 */
	public Vertex2D(float x, float y, float s, float t) {
		this(new Vector2f(x, y), new Vector2f(s, t));
	}
}