package sk.physics;

import java.util.ArrayList;

import sk.gfx.Transform;
import sk.gfx.Vertex2D;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector3f;

/**
 * A shape is an object that needs a body
 * to function. A body is the actual physics
 * part. The shape is the restriction that
 * prevents overlap.
 * 
 * A body is not limited to be used by one
 * single body. It can be added to any number
 * of bodies.
 * 
 * @author Ed
 *
 */
public class Shape {
	
	private Vector2f[] points;
	private Vector2f[] normals;
	
	// A vector that points from the center of the body, to the center of the shape
	// (The body is always at 0, 0
	private Vector2f center;
	
	private float broadPhaseLength = 0.0f;
	
	/**
	 * The points will be joined in the order you supplied, 
	 * where clockwise is expected
	 * Note that they are not allowed to be concave.
	 * 
	 * @param points The points that make up the shape.
	 * 
	 */
	public Shape(Vertex2D... points) {
		this.points = new Vector2f[points.length];
		for (int i = 0; i < points.length; i++) {
			this.points[i] = new Vector2f(points[i].getData(0));
		}
		processPoints();
	}
	
	/**
	 * This constructor casts Vertecies to Vector2f and uses them
	 * as points, but other than that this doesn't do any checks
	 * what so ever on the data, this is if you need some serious
	 * speed in your load times or have found a cool exploit. 
	 * 
	 * Note that normals that suffice the condition:
	 *  |V dot U| = 1
	 * Only require one of them to be submitted as normals for
	 * an accurate collision check.
	 * 
	 * @param points The points that make up the shape
	 * @param normals The normals of the shape
	 */
	public Shape(Vertex2D[] points, Vector2f[] normals) {
		this.points = new Vector2f[points.length];
		for (int i = 0; i < points.length; i++) {
			this.points[i] = new Vector2f(points[i].getData(0));
		}
		
		calculateBPRange();
		
		this.normals = normals.clone();
	}
	
	/**
	 * This constructor doesn't do any checks what so ever on 
	 * the data, this is if you need some serious speed in 
	 * your load times or have found a cool exploit. 
	 * 
	 * Note that normals that suffice the condition:
	 *  |V dot U| = 1
	 * Only require one of them to be submitted as normals for
	 * an accurate collision check.
	 * 
	 * @param points The points that make up the shape
	 * @param normals The normals of the shape
	 */
	public Shape(Vector2f[] points, Vector2f[] normals) {
		this.points = points.clone();
		this.normals = normals.clone();
	
		calculateBPRange();
	}
	
	/**
	 * 
	 * @param points The points that make up the shape.
	 * The points will be joined in the order you supplied, 
	 * where clockwise is expected
	 * Note that they are not allowed to be concave.
	 */
	public Shape(Vector2f... points) {
		this.points = points;
		processPoints();
	}
	
	/**
	 * Processes the points in the object and generates the
	 * appropriate normal and edge data. This speeds up the 
	 * collision check by doing some preprocessing
	 */
	private void processPoints() {
		// We don't want multiple of the same normals, so we calculate them on the fly
		ArrayList<Vector2f> normals = new ArrayList<Vector2f>();
		Vector2f edge = new Vector2f();

		if (points.length < 3) {
			throw new IllegalArgumentException("There must be more than two points in a polygon... Shame on you!");
		}
		
		// Calculate the center
		center = new Vector2f();
		for (Vector2f p : points) {
			center.add(p);		
		}
		center.scale(1.0f / (float) points.length);
		
		// Subtract the center from each point so it's centerd
		for (Vector2f p : points) {
			p.sub(center);
		}
		
		// Calculate the direction to loo through them
		Vector2f edgeA = points[points.length - 1].clone().sub(points[0]);
		Vector2f edgeB = points[1].clone().sub(points[0]);
		
		boolean rightHand = edgeA.x * edgeB.y - edgeB.x * edgeA.y < 0;
		
		for (int i = 0; i < points.length; i++) {
			Vector2f.sub(points[(i + 1) % points.length], points[i], edge);
			
			// Check if the current point is further away then the current
			float length = points[i].length();
			broadPhaseLength = Math.max(length, broadPhaseLength);
			
			// Calculated of 90 degree rotation matrix
			
			Vector2f normal;
			if (rightHand) {
				normal = new Vector2f(-edge.y, edge.x);
			} else {
				normal = new Vector2f(edge.y, -edge.x);
			}
			normal.normalise();
			int j = 0;
			for (; j < normals.size(); j++) {
				if (Math.abs(normals.get(j).dot(normal)) == 1.0f) {
					break;
				}
			}
			
			if (j == normals.size()) {
				normals.add(normal);
			}
		}
		
		this.normals = new Vector2f[normals.size()];
		normals.toArray(this.normals);
	}
	
	/**
	 * Calculates the Broad Phase Length of the body, nice and easy.
	 */
	private void calculateBPRange() {
		broadPhaseLength = 0.0f;
		for (Vector2f p : points) {
			broadPhaseLength = Math.max(broadPhaseLength, Math.abs(p.length()));
		}
	}
	
	/**
	 * Returns the center position
	 * @return the position
	 */
	public Vector2f getCenter(Transform t) {
		return new Vector2f(center.x * t.scale.x, center.y * t.scale.y).add(t.position);
	}
	
	/**
	 * Draws a shape really sloppily. Don't use this in production.
	 * 
	 * @param t the transform of the shape.
	 * @param color the color you want.
	 */
	public void _draw(Transform t, Vector3f color) {
		// Bounds
		for (int i = 0; i < points.length; i++) {
			Vector2f a = points[i].clone().add(center);
			Vector2f b = points[(i+1) % points.length].clone().add(center);
			
			// Scale
			a.x *= t.scale.x;
			a.y *= t.scale.y;
			// Rotate
			a = Vector2f.rotate(a, (float) t.rotation, null);
			// Translate
			a.add(t.position);
			
			// Scale
			b.x *= t.scale.x;
			b.y *= t.scale.y;
			// Rotate
			b = Vector2f.rotate(b, (float) t.rotation, null);
			// Translate
			b.add(t.position);
			
			Debug.Debug.drawLine(a, b, color);
		}
		
		// The broadphase check
		Debug.Debug.drawCircle(getCenter(t), getBP(t));
	}
	
	/**
	 * This value is used for broad phase checks.
	 * The check is a simple circular check,
	 * where the extents are the maximum width of
	 * the Shape in question.
	 * 
	 * @param t the transform for the current shape.
	 * 
	 * @return The largest distance from origo.
	 */
	public float getBP(Transform t) {
		return broadPhaseLength * Math.abs(Math.max(t.scale.x, t.scale.y));
	}
	
	/**
	 * Transforms a copy of the point p according to the transform.
	 * 
	 * @param axis the axis you want to cast along.
	 * @param t the transform.
	 * @param p the point.
	 * @return the dot of the axis and the transformed point.
	 */
	public float castAlong(Vector2f axis, Transform t, Vector2f p) {
		Vector2f transformed = p.clone();
		// Scale
		transformed.x *= t.scale.x;
		transformed.y *= t.scale.y;
		// Rotate
		transformed.rotate((float) t.rotation);
		// Cast
		return Vector2f.dot(transformed, axis);
	}
	
	/**
	 * Casts the shape along the normal specified returning the maximum point
	 * @param axis The axis you want to cast along
	 * @return The longest distance along the axis
	 * of all the points
	 */
	public float castAlongMax(Vector2f axis, Transform t) {
		float max = 0;
		for (Vector2f p : points) {
			max = Math.max(max, castAlong(axis, t, p));
		}
		return max;
	}
	
	/**
	 * Casts the shape along the normal specified returning the minimum point
	 * @param axis The axis you want to cast along
	 * @return The longest distance along the axis
	 * of all points
	 */
	public float castAlongMin(Vector2f axis, Transform t) {
		float min = 0;
		for (Vector2f p : points) {
			min = Math.min(min, castAlong(axis, t, p));
		}
		return min;
	}

	/**
	 * @return The normals of this shape
	 */
	public Vector2f[] getNormals() {
		return normals.clone();
	}
}
