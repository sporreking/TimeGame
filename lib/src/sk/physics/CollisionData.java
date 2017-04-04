package sk.physics;

import sk.gfx.Transform;
import sk.util.vector.Vector2f;

/**
 * A class that handles collision information,
 * it then uses that information to solve the
 * collision.
 * 
 * @author Ed
 * 
 */
public class CollisionData {
	// The normal
	public Vector2f normal;
	// The penetration depth
	public float collisionDepth = Float.MAX_VALUE;
	// Which transform is grouped with the normal
	public Transform normalOwner;
	// The bodies involved
	public Body a, b;
	// The other body
	public Body other;
	
	public static final float INACCURACY = 0.001f;
	
	/**
	 * Default constructor, new collision objects
	 * shouldn't be created outside of the engines
	 * collision code. 
	 */
	public CollisionData() {}

	/**
	 * Copy constructor
	 * @param c The CollisionData object you wish to copy
	 * 
	 */
	public CollisionData(CollisionData c) {
		normal = c.normal;
		collisionDepth = c.collisionDepth;
		normalOwner = c.normalOwner;
		a = c.a;
		b = c.b;
		other = c.other;
	}
	
	/**
	 * Fuses two arrays into one. This is used with the collision test and runs
	 * almost every frame and should thus be well optimized. If you know of 
	 * improvements let me know
	 * @param a The first array
	 * @param b The second array
	 * @return The combined array
	 */
	public static Vector2f[] fuseArrays(Vector2f[] a, Vector2f[] b) {
		Vector2f[] out = new Vector2f[a.length + b.length];
		System.arraycopy(a, 0, out, 0, a.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}
	
	/**
	 * Does a Separate Axis Theorem test on the two shapes
	 * @param a The first shape you want to check collision against
	 * @param b The second shape you want to check collision against
	 * @return The collision object with the appropriate 
	 * data for the collision, returns null if no collision
	 */
	public static CollisionData SATtest(Shape a, Transform ta, Shape b, Transform tb) {
		Vector2f distance = new Vector2f();
		Vector2f.sub(ta.position, tb.position, distance);
		
		CollisionData collision = new CollisionData();
		
		float max;
		float min;
		float dotDistance = 0.0f;
		float depth = 0.0f;
		
		// Fuse the arrays into one
		Vector2f[] normals = fuseArrays(a.getNormals(), b.getNormals());
		
		Vector2f n;
		int split = a.getNormals().length;

		
		for (int i = 0; i < normals.length; i++) {
			n = normals[i];
			n = Vector2f.rotate(n, i < split ? ta.rotation : tb.rotation, null);
			
			// Cast along the normal			
			if (n.dot(distance) < 0.0f) {
				max = a.castAlongMax(n, ta);
				min = -b.castAlongMin(n, tb);	
			} else {
				max = b.castAlongMax(n, tb);
				min = -a.castAlongMin(n, ta);				
			}
			
			dotDistance = Math.abs(Vector2f.dot(distance, n));
			// Check along the current axis
			depth = (max + min) - dotDistance;
						
			if (0 < depth) {
				if (depth < collision.collisionDepth) {
					collision.collisionDepth = depth;
					collision.normal = n;
					if (i < split) {
						collision.normalOwner = ta;
					} else {
						collision.normalOwner = tb;
					}
				}
			} else {
				return null;
			}
		}
		
		// The normal should point from A to B
		// Find a way to write this without if-s and I will buy you
		// an ice-cream, seriously
		if (collision.normalOwner == ta) {
			if (collision.normal.dot(distance) < 0.0f) {
				collision.normal.negate();
			}
		} else {
			if (collision.normal.dot(distance) > 0.0f) {
				collision.normal.negate();
			}
		}
		return collision;
	}
	
	/**
	 * Solves the collision that is stored in this object.
	 * 
	 * This applies friction and bounce to all objects
	 */
	public void solve() {
		// If both bodies are dynamic
		boolean dynamicCollision = a.isDynamic();
			
		// The normal should point from the static body
		if (a.getTransform() == normalOwner) {
			normal.negate();
		}
		
		// Move it back
		Vector2f reverse;
		if (dynamicCollision) {
			reverse = (Vector2f) normal.clone().scale(0.5f * (collisionDepth - INACCURACY));
			a.getTransform().position.sub(reverse);
		} else {
			reverse = (Vector2f) normal.clone().scale(collisionDepth - INACCURACY);
		}
		
		b.getTransform().position.add(reverse);
		
		
		// Change the velocity
		Vector2f relativeVelocity = new Vector2f();
		Vector2f.sub(a.getVelocity(), b.getVelocity(), relativeVelocity);
		
		float normalVelocity = Vector2f.dot(relativeVelocity, normal);
		// Make sure we're not moving away, if we are, just return
		if (0.0f > normalVelocity) return;
		
		// Bounce
		float bounce = Math.min(a.getBounce(), b.getBounce());
		float bounceImpulse = normalVelocity * (bounce + 1.0f);
		if (dynamicCollision) {
			bounceImpulse = normalVelocity * (bounce + 1.0f);
			bounceImpulse /= a.getInvertedMass() + b.getInvertedMass();
			Vector2f bounceForce = normal.clone().scale(bounceImpulse);
			b.addForce(bounceForce.scale(a.getMass()));
			a.addForce(bounceForce.scale(-a.getInvertedMass() * b.getMass()));
		} else {
			bounceImpulse *= b.getMass();
			Vector2f bounceForce = normal.clone().scale(bounceImpulse);
			b.addForce(bounceForce);
		}
		
		// Friction
		float mu = Math.min(a.getFriction(), b.getFriction());
		if (mu == 0.0f) return;
		float frictionImpulse = Math.abs(bounceImpulse * mu);
		float totalMass = dynamicCollision ? a.getMass() + b.getMass() : b.getMass();
		// Super fast manual rotation and creation
		Vector2f tangent = new Vector2f(normal.y, -normal.x);
		// Make sure we're slowing down in the right direction
		float frictionDirection = relativeVelocity.dot(tangent);
		if (0.0f > frictionDirection) {
			// If they're pointing the same way, flip them
			tangent.negate();
		}
		
		frictionDirection = Math.abs(frictionDirection);
		
		// If the friction force will slow us down too much, clamp it
		if (frictionDirection < frictionImpulse) {
			tangent.scale(frictionDirection * totalMass);
		} else {
			tangent.scale(frictionImpulse * totalMass);
		}

		// Add it to the body
		if (dynamicCollision) {
			b.addForce(tangent.scale(0.5f));
			a.addForce(tangent.scale(-1.0f));
		} else {
			b.addForce(tangent);									
		}
	}
}
