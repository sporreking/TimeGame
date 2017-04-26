package sk.physics;


import sk.gfx.Transform;
import sk.util.vector.Vector2f;

/**
 * A class that handles collision information.
 * It then uses that information to solve the
 * collision.
 * 
 * @author Ed
 * 
 */
public class CollisionData {

	// If you're not getting generated collision
	// events when bodies are on top of each other,
	// this is the guy to increase. But if you're
	// sinking in too much, make it smaller.
	public static float INACCURACY = 0.001f;
	
	// The normal
	public Vector2f normal;
	// The penetration depth
	public float collisionDepth = Float.MAX_VALUE;
	// Which transform is grouped with the normal
	public Transform normalOwner;
	// The point of the collision
	public Vector2f point;
	// The bodies involved
	public Body a, b;
	// The other body
	public Body other;
	// Some relevant physics variables
	public float impactForce = 0;
	public float normalVelocity = 0;
	public float tangentVelocity = 0;
	
	/**
	 * Default constructor, new collision objects
	 * shouldn't be created outside of the engines
	 * collision code. 
	 */
	protected CollisionData() {}

	/**
	 * Copies the specified collision data.
	 * 
	 * @param c the CollisionData object you wish to copy from.
	 * 
	 */
	protected CollisionData(CollisionData c) {
		normal = c.normal;
		collisionDepth = c.collisionDepth;
		normalOwner = c.normalOwner;
		a = c.a;
		b = c.b;
		other = c.other;
	}
	
	/**
	 * Fuses two arrays into one. This is used with the collision test and runs
	 * almost every frame. Thus, it should be be well optimized. If you know of 
	 * improvements, let me know.
	 * 
	 * @param a the first array.
	 * @param b the second array.
	 * @return the combined array.
	 */
	public static Vector2f[] fuseArrays(Vector2f[] a, Vector2f[] b) {
		Vector2f[] out = new Vector2f[a.length + b.length];
		System.arraycopy(a, 0, out, 0, a.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}
	
	/**
	 * Does a <em>Separate Axis Theorem</em> test on the two shapes.
	 * 
	 * @param a the first shape you want to check collision against.
	 * @param b the second shape you want to check collision against.
	 * @return the collision object with the appropriate 
	 * data for the collision, returns null if no collision.
	 */
	public static CollisionData SATtest(Shape a, Transform ta, Shape b, Transform tb) {
		Vector2f distance = Vector2f.sub(
				a.getCenter(ta),
				b.getCenter(tb),
				null);
		
		
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

					// Calculate the collision point
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
	 * Solves the contained collision.
	 * <p>
	 * Note: This applies friction and bounce to all objects.
	 * </p>
	 */
	public void solve(float delta) {
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
		Vector2f.sub(a.getNextVelocity(), b.getNextVelocity(), relativeVelocity);
		
		normalVelocity = Vector2f.dot(relativeVelocity, normal);
		
		// Make sure we're not moving away, if we are, just return
		if (0.0f > normalVelocity) return;
		
		// Bounce
		float bounce = Math.min(a.getBounce(), b.getBounce());
		float bounceImpulse = normalVelocity * (bounce + 1.0f);
		if (dynamicCollision) {
			bounceImpulse /= a.getInvertedMass() + b.getInvertedMass();
			Vector2f bounceForce = normal.clone().scale(bounceImpulse);
			b.addForce(bounceForce.scale(a.getMass()));
			a.addForce(bounceForce.scale(-a.getInvertedMass() * b.getMass()));
		} else {
			bounceImpulse *= b.getMass();
			Vector2f bounceForce = normal.clone().scale(bounceImpulse);
			b.addForce(bounceForce);
		}
		
		// Store it for future use.
		impactForce = bounceImpulse;
		
		// Friction
		float mu = Math.min(a.getFriction(), b.getFriction());
		if (mu == 0.0f) return;
		float frictionImpulse = (float) Math.abs(bounceImpulse * mu * delta);
		float totalMass = dynamicCollision ? a.getMass() + b.getMass() : b.getMass();
		// Super fast manual rotation and creation
		Vector2f tangent = new Vector2f(normal.y, -normal.x);
		// Make sure we're slowing down in the right direction
		tangentVelocity = relativeVelocity.dot(tangent);
		if (0.0f > tangentVelocity) {
			// If they're pointing the same way, flip them
			tangent.negate();
		}
		
		float frictionStrength = Math.abs(tangentVelocity);
		
		// If the friction force will slow us down too much, clamp it
		if (frictionStrength < frictionImpulse) {
			tangent.scale(frictionStrength * totalMass);
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
