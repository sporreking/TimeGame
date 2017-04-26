package sk.physics;

import java.util.ArrayList;
import java.util.List;

import sk.entity.Component;
import sk.gfx.Transform;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector3f;

/**
 * A Body is a particle in space
 * that flows around and is limited by a shape.
 * A body does NOT register collisions without
 * a shape and it is thus required.
 * 
 * @author Ed
 *
 */
public class Body extends Component {
	
	// Force and velocity, self explanatory
	private Vector2f velocity = new Vector2f();
	private Vector2f force = new Vector2f();
	
	// The mass
	private float mass = 0.0f;
	private float invertedMass = 0.0f;
	
	// The friction coefficient
	private float friction = 0.0f;
	private float invertedFriction = 0.0f;
	
	// The bounce factor
	private float bounce = 0.0f;
	
	// If the body is dynamic
	private boolean dynamic = true;
	
	// If the body should trigger any collision response
	private boolean trigger = false;
	
	// A bit-mask that says with which bodies we should collide
	private short layer = 256;
	
	// A reference to the shape
	private ArrayList<Shape> shapes = new ArrayList<Shape>();
	
	// A quick reference to the transform
	private Transform transform;
	
	// A tag that makes it easier to search for collisions
	private String tag;
	
	// A list of all collisions this frame
	private ArrayList<CollisionData> collisions = new ArrayList<CollisionData>();
	
	/**
	 * Creates a new body with the shapes as shapes. 
	 * 
	 * Mass is set to 1, friction is set to 1 and bounce is set to 0.
	 * 
	 * @param shapes the shapes you want the body to have.
	 * @param isDynamic if the body should be dynamic or not.
	 */
	public Body(List<Shape> shapes, boolean isDynamic) {
		this(shapes, isDynamic, 1, 1, 0);
	}
	
	/**
	 * Creates a new body with the specifications.
	 * 
	 * @param shapes the shapes you want the body to have.
	 * @param isDynamic if the body is dynamic.
	 * @param mass the mass of the body.
	 * @param friction the friction of the body.
	 * @param bounce the bounce of the body. 
	 */
	public Body(List<Shape> shapes, boolean isDynamic, float mass, float friction, float bounce) {
		this(shapes.get(0), mass, friction, bounce);
		setDynamic(isDynamic);
		for (int i = 1; i < shapes.size(); i++) {
			addShape(shapes.get(i));
		}
	}
	
	/**
	 * Initializes the body to a default state of
	 * 1.0 Mass, 1.0 Friction and 1.0 Bounce (Elasticity)
	 * 
	 * @param shape The shape you want to use as collider
	 * 
	 */
	public Body(Shape shape) {
		this(shape, 1.0f, 1.0f, 1.0f);
	}

	/**
	 * Creates a body that can be set to dynamic or static
	 * @param shape The shape of the body.
	 * @param isDynamic If the body should be dynamic
	 * @param friction The friction for the body
	 * @param bounce The bounce for the body
	 */
	public Body(Shape shape, boolean isDynamic, float friction, float bounce) {
		this(shape, 1.0f, friction, bounce);
		this.setDynamic(isDynamic);
	}
	
	/**
	 * Lets you set the mass of the body you create, settings
	 * the other values to.
	 * 1.0 Friction and 1.0 Bounce (Elasticity)
	 * 
	 * @param shape The shape you want to use as collider
	 * @param mass The mass of your new body
	 */
	public Body(Shape shape, float mass) {
		this(shape, mass, 1.0f, 1.0f);
	}
	
	/**
	 * Lets you set the mass and friction of the to be created body.
	 * This sets the bounce to 1.0.
	 *
	 * @param shape The shape you want to use as collider
	 * @param mass The mass of your new body
	 * @param friction The friction for your new body
	 */
	public Body(Shape shape, float mass, float friction) {
		this(shape, mass, friction, 1.0f);
	}
	
	/**
	 * The fully custom option for you who wish to create a body.
	 * You decide everything for yourself.
	 * 
	 * @param shape The shape you want to use as collider
	 * @param mass The mass of the new body
	 * @param friction The friction of the new body
	 * @param bounce The bounce factor of your new body
	 */
	public Body(Shape shape, float mass, float friction, float bounce) {
		shapes.add(shape);
		setMass(mass);
		setFriction(friction);
		setBounce(bounce);
	}
	
	/**
	 * Draws all shapes associated with this body.
	 */
	public void _draw() {
		for (Shape s : shapes) {
			s._draw(transform, new Vector3f(0.5f, 0.0f, 1.0f));
		}
	}
	
	/**
	 * Decouples the body from the entity component system, so an entity isn't needed
	 * @param transform The new transform you want the body to have when it isn't dependent on a parent
	 */
	public void decoupple(Transform transform) {
		this.transform = transform;
	}

	/**
	 * Returns a collision, if there is one, with the supplied body
	 * @param b The body you want to look for
	 * @return A collision if it is found, null otherwise
	 */
	public CollisionData getCollision(Body b) {
		for (CollisionData c : collisions) {
			if (c.other == b) {
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Returns a list of all the collisions where the other object
	 * matches the tag. 
	 * 
	 * @param tag The tag you want to search for
	 * @return The list of collisions with the tag
	 */
	public CollisionData[] getCollisionsWithTag(String tag) {
		ArrayList<CollisionData> collisions = new ArrayList<CollisionData>();
		for (CollisionData c : collisions) {
			if (c.other.getTag().equals(tag)) {
				collisions.add(c);
			}
		}
		return (CollisionData[]) collisions.toArray();
	}
	
	/**
	 * Dots against a vector and returns the max.
	 * 
	 * @param normal The normal you wish to dot
	 * @return The maximum dot of all the normals in the collisions
	 */
	public float dotCollisionNormals(Vector2f normal) {
		float maxDot = Float.MIN_VALUE;
		for (CollisionData c : collisions) {
			maxDot = Math.max((float) maxDot, (float) c.normal.dot(normal));
		}
		return maxDot;
	}
	
	/**
	 * Loops through all collisions and returns the deepest
	 * @return the deepest collision depth
	 */
	public float getMaxCollisionDepth() {
		float maxDepth = Float.MIN_VALUE;
		for (CollisionData c : collisions) {
			maxDepth = Math.max((float) maxDepth, (float) c.collisionDepth);
		}
		return maxDepth;
	}
	
	/**
	 * Loops through the collisions and checks if any of them
	 * are deeper than the supplied value
	 * @param value the largest collision depth which returns true
	 * @return if there was a collision that deep
	 */
	public boolean hasDeepCollision(float value) {
		for (CollisionData c : collisions) {
			if (value < c.collisionDepth) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * If there is any collision with the tag at all.
	 * 
	 * @param tag The tag you want to search for
	 * @return If the tag was found on a colliding body
	 */
	public boolean isCollidingWithTag(String tag) {
		for (CollisionData c : collisions) {
			if (c.other.getTag().equals(tag)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a collision to the Body, mortals should not call this
	 * function.
	 * @param c The collision our superior overlords wish to add
	 */
	public void addCollision(CollisionData c) {
		c = new CollisionData(c);
		if (c.a == this) {
			c.other = c.b;
		} else {
			c.other = c.a;
		}
		
		
		collisions.add(c);
	}
	
	/**
	 * Returns the shape of this object
	 * 
	 * @return THE shape
	 */
	public Shape getShape() {
		return shapes.get(0);
	}
	
	/**
	 * @return if the body is a dynamic body
	 */
	public boolean isDynamic() {
		return dynamic;
	}
	
	/**
	 * A dynamic body gets all kinds of forces applied to it,
	 * including gravity and collision responses.
	 * 
	 * A non-dynamic body doesn't care about anything and will
	 * keep moving at a constant speed no matter what happens to it.
	 * Unless someone changes the velocity of it...
	 * This does not stop the body from exerting forces on other bodies,
	 * such as friction.
	 * 
	 * @param dynamic If the body should be dynamic or not
	 */
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	
	/**
	 * Sets the friction constant (mu) for this body,
	 * this is taken into consideration when two bodies
	 * are grinding against each other, since the smallest
	 * friction constant will be picked.
	 * 
	 * The bigger the constant the bigger the friction force
	 * the faster the two bodies will have equal tangent 
	 * velocity.
	 * 
	 * @param friction The new friction constant.
	 */
	public void setFriction(float friction) {
		if (mass <= 0.0f) {
			throw new IllegalArgumentException("Zero or negative friction supplied.");
		}
		this.friction = friction;
		this.invertedFriction = 1.0f / friction;
	}
	
	/**
	 * @return The friction constant
	 */
	public float getFriction() {
		return friction;
	}
	
	/**
	 * @return 1 / friction constant
	 */
	public float getInvertedFriction() {
		return invertedFriction;
	}
	
	/**
	 * Sets the mass of the body, this decides
	 * its willingness to change when forces are
	 * applied to it.
	 * 
	 * A mass of 0 or bellow is illegal
	 * 
	 * @param mass The new mass
	 */
	public void setMass(float mass) {
		if (mass <= 0.0f) {
			throw new IllegalArgumentException("Zero or negative mass supplied.");
		}
		this.mass = mass;
		this.invertedMass = 1.0f / mass;
	}

	/**
	 * @return The mass of the body
	 */
	public float getMass() {
		return mass;
	}
	
	/**
	 * @return 1 / the mass of the body
	 */
	public float getInvertedMass() {
		return invertedMass;
	}
	
	/**
	 * (Note that velocity by definition is a vector)
	 * @return the velocity of the body.
	 */
	public Vector2f getVelocity() {
		return velocity.clone();
	}
	
	/**
	 * v + F / m 
	 */
	public Vector2f getNextVelocity() {
		return getVelocity().add(force.clone().scale(invertedMass));
	}
	
	/**
	 * @return The bounce (elasticity) of the body
	 */
	public float getBounce() {
		return bounce;
	}
	
	/**
	 * Sets the bouncyness of the body, this decides how 
	 * much energy should be lost in a collision with this 
	 * object, so a high elasticity will have more energy 
	 * than a collision with low elasticity. 
	 * 
	 * Note that
	 * it is impossible for collisions in the "real" world
	 * to have an elasticity over 1.0, This will create energy 
	 * when two bodies collide and should probably be placed in 
	 * the range 0.0 to ~0.9.
	 * 
	 * Sending in a bounce less than 0 is an error.
	 * 
	 * @param bounce The new bounce
	 */
	public void setBounce(float bounce) {
		if (bounce < 0.0f) {
			throw new IllegalArgumentException("Negative bounce supplied.");
		}
		this.bounce = bounce;
	}
	
	/**
	 * Adds a force to this object.
	 * 
	 * @param force A force exerted on the object
	 */
	public void addForce(Vector2f force) {
		Vector2f.add((Vector2f) force, this.force, this.force);
	}
	
	/**
	 * Adds velocity to the object, this should
	 * not be used if you want to make things look
	 * physically accurate, but it can be useful in
	 * gameplay situations.
	 * @param vel The velocity to add
	 */
	public void addVelocity(Vector2f vel) {
		Vector2f.add(vel, velocity, velocity);
	}
	
	/**
	 * Sets the velocity to the supplied. This 
	 * function is not recommended to be used since
	 * it is not physically accurate and can weird
	 * things out. A better practice is to use
	 * "addForce".
	 * 
	 * @param vel The new velocity
	 */
	public void setVelocity(Vector2f vel) {
		velocity = vel.clone();
	}
	
	/**
	 * @return The reference this object holds to 
	 * the entities transform.
	 */
	public Transform getTransform() {
		return transform;
	}
	
	@Override
	public void init() {
		transform = getParent().get(Transform.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Component>[] requirements() { 
		return (Class<? extends Component>[]) new Class<?>[] {
			Transform.class
		}; 
	}
	
	/**
	 * Steps the physics simulation forward by one step.
	 * 
	 * @param delta The time since the last step
	 */
	public void step(double delta) {
		if (isDynamic() && !isTrigger()) {
			Vector2f.add(velocity, (Vector2f) force.scale(invertedMass), velocity);
			force.set(0.0f, 0.0f);			
		}
		Vector2f.add(transform.position, (Vector2f) velocity.clone().scale((float) delta), transform.position);
		collisions.clear();
	}

	
	/**
	 * @return The momentum (Velocity * mass)
	 */
	public Vector2f getMomentum() {
		return (Vector2f) getVelocity().scale(mass);
	}

	/**
	 * @return If this body is a trigger
	 */
	public boolean isTrigger() {
		return trigger;
	}

	/**
	 * Sets if this object should be a trigger or not.
	 * 
	 * A trigger is a body that doesn't create collision
	 * responses, it only checks if something is overlapping
	 * with it. This can be used for triggers in games, hence
	 * the name. 
	 * 
	 * @param trigger If the body should be a trigger
	 */
	public void setTrigger(boolean trigger) {
		this.trigger = trigger;
	}
	
	/**
	 * @return The layers the body is on
	 */
	public short getLayer() {
		return layer;
	}

	/**
	 * The layer should be thought about as a bit-map.
	 * Where each bit is a layer and if it is on, the body
	 * is on it and can collide with other bodies on the 
	 * same active layer.
	 * @param layer The new layer bit-map
	 */
	public void setLayer(short layer) {
		this.layer = layer;
	}
	
	/**
	 * Checks if the other body shares a layer with this
	 * body.
	 * 
	 * @param b The other body
	 * @return If they share a layer
	 */
	public boolean sharesLayer(Body b) {
		return (getLayer() & b.getLayer()) != 0;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getNumberOfShapes() {
		return shapes.size();
	}
	
	/**
	 * Note: You cannot add the same shape twice
	 * @param shape the shape you wish to add
	 * @return if the shape was added or not
	 */
	public boolean addShape(Shape shape) {
		for (Shape s : shapes) {
			if (s == shape) {
				return false;
			}
		}
		
		shapes.add(shape);
		return true;
	}
	
	/**
	 * Note: You cannot add the same shape twice
	 * 
	 * @param shapes the shapes you wish to add
	 */
	public Body addShape(List<Shape> shapes) {
		for (Shape s : shapes) {
			addShape(s);
		}
		return this;
	}
	
	/**
	 * Removes the shape.
	 * @param shape the shape you want to remove.
	 * @return if it succeeded with removing or not.
	 */
	public boolean removeShape(Shape shape) {
		int i = 0;
		for (; i < shapes.size(); i++) {
			if (shapes.get(i) == shape) {
				shapes.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the complete list of all shapes on the body.
	 * @return a list of shapes on the body.
	 */
	public ArrayList<Shape> getShapes() {
		return shapes;
	}
}
