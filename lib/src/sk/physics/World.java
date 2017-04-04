package sk.physics;

import java.util.ArrayList;

import sk.game.Time;
import sk.util.vector.Vector2f;

/**
 * The World class handles all the collisions in the world, 
 * literally. This is the object that should be updated
 * to update all bodies in the world.
 * @author Ed
 *
 */
public class World {
	
	static ArrayList<Body> bodies = new ArrayList<Body>();
	
	public static Vector2f gravity = new Vector2f(0.001f, 0.1f);
	public static float stepLength = 1.0f / 60.0f;
	private static float timer = 0.0f;
	
	/**
	 * Adds a body to this horrid world of collision
	 * @param body The body you wish to add
	 */
	static public void addBody(Body body) {
		// Make sure there's only one of each body
		if (bodies.contains(body)) return;
		bodies.add(body);
	}
	
	/**
	 * Removes the body from the list of bodies
	 * @param body The body that should be removed
	 */
	static public void removeBody(Body body) {
		bodies.remove(body);
	}
	
	/**
	 * Updates the world, checks for collisions and
	 * steps forward through the simulation.
	 * @param delta The time step
	 */
	static public void update(double delta) {
		timer += delta;
		// Make sure we only step if we need to
		while (stepLength < timer) {
			timer -= stepLength;
			// Update all bodies
			Vector2f deltaGravity = (Vector2f) gravity.clone().scale((float) stepLength);
			for (Body a : bodies) {
				if (a.isDynamic())
					a.addVelocity(deltaGravity);
				a.step(stepLength);
			}
			
			// Check for collisions
			for (int i = 0; i < bodies.size(); i++) {
				for (int j = 0; j < i; j++) {
					Body a = bodies.get(i);
					Body b = bodies.get(j);
					// Check if they share a layer
					if (!a.sharesLayer(b)) continue;
					// Make sure not both are static
					if (!a.isDynamic() && !b.isDynamic()) continue;
					// Make sure not both are triggers
					if (a.isTrigger() && b.isTrigger()) continue;
 					// Check if they're in roughly the same area
					float bpRange = (float) Math.pow(
							a.getShape().getBP() * Math.max(
									a.getTransform().scale.x, 
									a.getTransform().scale.y) + 
							b.getShape().getBP() * Math.max(
									b.getTransform().scale.x, 
									b.getTransform().scale.y), 2.0f);
					float distanceSq = Vector2f.sub(a.getTransform().position, b.getTransform().position, null).lengthSquared();
					if (bpRange <= distanceSq) continue;

					CollisionData c = CollisionData.SATtest(a.getShape(), a.getTransform(), b.getShape(), b.getTransform());
					
					// If there was a collision, handle it
					if (c == null) continue;
					
					// Add them so that a is static if any of them are static
					if (b.isDynamic()) {
						c.a = a;
						c.b = b;
					} else {
						c.a = b;
						c.b = a;
					}
										
					// Add their collisions to the bodies
					a.addCollision(c);
					b.addCollision(c);
					
					// If one of them is a trigger we are done
					if (a.isTrigger() || b.isTrigger()) continue;
					
					// Now we just solve the collision and everyone is happy
					c.solve();
				}
			}
		}
	}
}