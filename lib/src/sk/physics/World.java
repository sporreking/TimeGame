package sk.physics;

import java.util.ArrayList;

import sk.entity.Entity;
import sk.game.Time;
import sk.gfx.Transform;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector3f;

/**
 * The World class handles all the collisions in the world, 
 * literally. This is the object that should be updated
 * to update all bodies in the world.
 * @author Ed
 *
 */
public class World {
	
	ArrayList<Body> bodies = new ArrayList<Body>();
	
	public Vector2f gravity = new Vector2f(0.0f, -0.5f);
	public float stepLength = 1.0f / 60.0f;
	private float timer = 0.0f;
	
	/**
	 * Adds a body to this horrid world of collision
	 * @param body The body you wish to add
	 */
	public void addBody(Body body) {
		// Make sure there's only one of each body
		if (bodies.contains(body)) return;
		bodies.add(body);
	}
	
	/**
	 * Removes the body from the list of bodies
	 * @param body The body that should be removed
	 */
	public void removeBody(Body body) {
		bodies.remove(body);
	}
	
	/**
	 * Updates the world, checks for collisions and
	 * steps forward through the simulation.
	 * @param delta The time step
	 */
	public void update(double delta) {
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
 					
					
					CollisionData c = null;
					// There are multiple shapes, skip the bread phase and try the bodies
					Transform ta = a.getTransform();
					Transform tb = b.getTransform();
					for (Shape shapeA : a.getShapes()) {
						for (Shape shapeB : b.getShapes()) {
							float bpRange = (float) Math.pow(
									a.getShape().getBP() * Math.max(
											ta.scale.x, 
											ta.scale.y) + 
									b.getShape().getBP() * Math.max(
											tb.scale.x,
											tb.scale.y), 2.0f);
							float distanceSq = Vector2f.sub(
									shapeA.getCenter(ta),
									shapeB.getCenter(tb),
									null).lengthSquared();
							
							
							if (bpRange <= distanceSq) continue;
							c = CollisionData.SATtest(shapeA, ta, shapeB, tb);
							
							if (c == null) continue;
							
							if (a.isDynamic()) {
								c.a = b;
								c.b = a;
							} else {
								c.a = a;
								c.b = b;
							}
							
							// Add their collisions to the bodies
							a.addCollision(c);
							b.addCollision(c);
							
							// If one of them is a trigger we are done
							if (a.isTrigger() || b.isTrigger()) return;
						
							c.solve();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Adds the entities body to the world
	 * @param entity the entity you whish to add
	 */
	public void addEntity(Entity entity) {
		addBody(entity.get(Body.class));
	}
}