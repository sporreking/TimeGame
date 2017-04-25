package sk.physics;

import java.util.ArrayList;

import sk.entity.Component;
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
		System.out.print("Center: ");
		System.out.println(center);
		
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
	
	public void _draw(Transform t, Vector3f color) {
		Vector2f dot;
		for (Vector2f p : points) {
			dot = p.clone().add(center);
			dot.x *= t.scale.x;
			dot.y *= t.scale.y;
			dot = Vector2f.rotate(dot, t.rotation, null);
			dot.add(t.position);
		}
	}
	
	/**
	 * This value is used for broad phase checks.
	 * The check is a simple circular check,
	 * where the extents are the maximum width of
	 * the Shape in question.
	 * @return The largest distance from origo.
	 */
	public float getBP() {
		return broadPhaseLength;
	}
	
	/**
	 * Casts the shape along the normal specified returning the maximum point
	 * @param axis The axis you want to cast along
	 * @return The longest distance along the axis
	 * of all the points
	 */
	public float castAlongMax(Vector2f axis, Transform t) {
		float maxLength = 0;
		float angle = t.rotation;
		Vector2f scale = t.scale;
		Vector2f rotatedPoint = new Vector2f();
		
		for (Vector2f p : points) {
			rotatedPoint = p.clone();
			// Scale
			rotatedPoint.x *= scale.x;
			rotatedPoint.y *= scale.y;
			// Rotate
			rotatedPoint = Vector2f.rotate(rotatedPoint, (float) angle, null);
			// Cast
			maxLength = Math.max(maxLength, Vector2f.dot(rotatedPoint, axis));
		}
		return maxLength;
	}
	
	/**
	 * Casts the shape along the normal specified returning the minimum point
	 * @param axis The axis you want to cast along
	 * @return The longest distance along the axis
	 * of all the points
	 */
	public float castAlongMin(Vector2f axis, Transform t) {
		float minLength = 0;
		float angle = t.rotation;
		Vector2f scale = t.scale;
		Vector2f rotatedPoint = new Vector2f();
		
		for (Vector2f p : points) {
			rotatedPoint = p.clone();
			// Scale
			rotatedPoint.x *= scale.x;
			rotatedPoint.y *= scale.y;
			// Rotate
			rotatedPoint = Vector2f.rotate(rotatedPoint, (float) angle, null);
			// Cast
			minLength = Math.min(minLength, Vector2f.dot(rotatedPoint, axis));
		}
		return minLength;
	}

	/**
	 * @return The normals of this shape
	 */
	public Vector2f[] getNormals() {
		return normals.clone();
	}
}
