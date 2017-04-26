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
	
	// Force and velocity
	private Vector2f velocity = new Vector2f();
	private Vector2f force = new Vector2f();
	
	// One way collisions
	private Vector2f direction = new Vector2f(0, 1);
	private float leniency = 1.0f;
	
	// The mass
	private float mass = 0.0f;
	private float invertedMass = 0.0f;
	
	// The friction coefficient
	private float friction = 0.0f;
	
	// The bounce factor
	private float bounce = 0.0f;
	
	// If the body is dynamic
	private boolean dynamic = true;
	
	// If the body should trigger any collision response
	private boolean trigger = false;
	
	// A bit-mask that says with which bodies we should collide
	private short layer = 256;
	
	// A reference to the shape
	private ArrayList<Shape> shapes = new ArrayList<>();
	
	// A quick reference to the transform
	private Transform transform = new Transform();
	
	// A tag that makes it easier to search for collisions
	private String tag;
	
	// A list of all collisions this frame
	private ArrayList<CollisionData> collisions = new ArrayList<CollisionData>();
	
	/**
	 * Creates a new body with the specified shapes. 
	 * 
	 * Mass is set to 1, friction is set to 1 and bounce is set to 0.
	 * 
	 * @param isDynamic if the body should be dynamic or not.
	 * @param shapes the shapes you want the body to have.
	 */
	public Body(boolean isDynamic, List<Shape> shapes) {
		this(isDynamic, 1, 1, 0, shapes);
	}
	
	/**
	 * Creates a new body with the specified properties.
	 * 
	 * @param isDynamic if the body is dynamic.
	 * @param mass the mass of the body.
	 * @param friction the friction of the body.
	 * @param bounce the bounce of the body. 
	 * @param shapes the shapes you want the body to have.
	 */
	public Body(boolean isDynamic, float mass, float friction, float bounce, List<Shape> shapes) {
		this(mass, friction, bounce, shapes.get(0));
		setDynamic(isDynamic);
		for (int i = 1; i < shapes.size(); i++) {
			addShape(shapes.get(i));
		}
	}
	
	/**
	 * Initializes the body to a default state of
	 * 1.0 Mass, 1.0 Friction and 0 Bounce (Elasticity)
	 * 
	 * @param shapes the shapes you want to use as colliders.
	 * 
	 */
	public Body(Shape... shapes) {
		this(1, 1, 0, shapes);
	}

	/**
	 * Creates a body that can be set to dynamic or static.
	 * 
	 * @param isDynamic if the body should be dynamic.
	 * @param friction the friction for the body.
	 * @param bounce the bounce for the body.
	 * @param shapes the shapes of the body.
	 */
	public Body(boolean isDynamic, float friction, float bounce, Shape... shapes) {
		this(1.0f, friction, bounce, shapes);
		this.setDynamic(isDynamic);
	}
	
	/**
	 * Creates a body with the specified mass and shapes.
	 * Friction and bounce (elasticity) will both be set to 1.0f
	 * 
	 * @param mass the mass of your new body.
	 * @param shapes the shapes you want to use as colliders.
	 */
	public Body(float mass, Shape... shapes) {
		this(mass, 1.0f, 1.0f, shapes);
	}
	
	/**
	 * Creates a body with the specified mass and friction.
	 * Bounce (elasticity) will be set to 1.0f
	 *
	 * @param mass the mass of your new body.
	 * @param friction the friction for your new body.
	 * @param shapes the shapes you want to use as colliders.
	 */
	public Body(float mass, float friction, Shape... shapes) {
		this(mass, friction, 1.0f, shapes);
	}
	
	/**
	 * Creates a new body with the specified mass, friction, bounce and shapes.
	 * 
	 * @param mass the mass of the new body.
	 * @param friction the friction of the new body.
	 * @param bounce the bounce factor of your new body.
	 * @param shapes the shape you want to use as colliders.
	 */
	public Body(float mass, float friction, float bounce, Shape... shapes) {
		for(Shape s : shapes)
			this.shapes.add(s);
		setMass(mass);
		setFriction(friction);
		setBounce(bounce);
	}
	
	/**
	 * 
	 * Draws all shapes associated with this body.
	 * <p>
	 * Note: This function uses OpenGL Immediate Mode. Thus, it may not work properly.
	 * </p>
	 */
	public void _draw() {
		for (Shape s : shapes) {
			s._draw(transform, new Vector3f(0.5f, 0.0f, 1.0f));
		}
	}
	
	/**
	 * Decouples the body from the entity component system, so an entity isn't needed.
	 * 
	 * @param transform the new transform you want the body to have when it has been decoupled.
	 */
	public void decouple(Transform transform) {
		this.transform = transform;
	}

	/**
	 * Returns a collision, if there is one, with the supplied body.
	 * 
	 * @param b the body you want to look for.
	 * @return a collision if it is found, null otherwise.
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
	 * @param tag the tag you want to search for.
	 * @return a list of collisions with the tag.
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
	 * Performs a dot operation with all collision normals
	 * against a vector and returns the max result.
	 * 
	 * The default return value is {@link Float#MIN_VALUE}, if
	 * there are no collisions.
	 * <p>
	 * Note: In order to retrieve the minimum dot product. Just flip the normal.
	 * </p>
	 * @param normal the normal you wish to dot.
	 * @return the maximum dot of all the normals in the collisions.
	 */
	public float dotCollisionNormals(Vector2f normal) {
		float maxDot = Float.MIN_VALUE;
		for (CollisionData c : collisions) {
			maxDot = Math.max((float) maxDot, (float) c.normal.dot(normal));
		}
		return maxDot;
	}
	
	/**
	 * Returns the number of collisions that occurred this frame.
	 * 
	 * @return the number of collisions that occured this frame. 
	 */
	public int numCollisions() {
		return collisions.size();
	}
	
	/**
	 * Loops through all collision depths and returns the deepest.
	 * 
	 * @return the deepest collision depth.
	 */
	public float getMaxCollisionDepth() {
		float maxDepth = Float.MIN_VALUE;
		for (CollisionData c : collisions) {
			maxDepth = Math.max((float) maxDepth, (float) c.collisionDepth);
		}
		return maxDepth;
	}
	
	/**
	 * Returns a list of the collision data. Normals are always facing away from the shape.
	 * 
	 * @return the list of collisions.
	 */
	public CollisionData[] getCollisions() {
		return (CollisionData[]) collisions.toArray();
	}
	
	/**
	 * Loops through the collisions and checks if any of them
	 * are deeper than the supplied value.
	 * 
	 * @param value the largest collision depth to returns true.
	 * @return true if there was a collision depth equal to or beyond the supplied value.
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
	 * Returns whether or not there are any collisions with the tag.
	 * 
	 * @param tag the tag you want to search for.
	 * @return true if the tag was found on a colliding body.
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
	 * 
	 * This functions makes sure all normals are pointing away
	 * from the this body.
	 * 
	 * @param c the collision our superior overlords wish to add.
	 */
	protected void addCollision(CollisionData c) {
		c = new CollisionData(c);
		c.normal = c.normal.clone();
		if (c.a == this) {
			c.other = c.b;
		} else {
			c.other = c.a;
			// We need to flip the normal, to make sure it is pointing
			// away from the body. 
			c.normal.negate();
		}

		collisions.add(c);
	}
	
	/**
	 * Returns the shape with the specified index.
	 * 
	 * @param i the index of the shape to return.
	 * @return the shape with the specified index.
	 */
	public Shape getShape(int i) {
		return shapes.get(i);
	}
	
	/**
	 * Returns whether or not this body is dynamic.
	 * 
	 * @return true if the body is dynamic.
	 */
	public boolean isDynamic() {
		return dynamic;
	}
	
	/**
	 * Sets whether or not this body should be dynamic.
	 * 
	 * A non-dynamic body will not be affected by forces.
	 * 
	 * @param dynamic whether the body should be dynamic or not.
	 * @return this body instance.
	 */
	public Body setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
		
		return this;
	}
	
	/**
	 * Sets the friction constant (mu) for this body.
	 * <br>
	 * This is taken into consideration when two bodies
	 * are grinding against each other - the smallest
	 * friction constants will be picked.
	 * 
	 * @param friction the new friction constant.
	 * @return this body instance.
	 * @throws IllegalArgumentException if the specified friction constant is 0 or lower.
	 */
	public Body setFriction(float friction) {
		if (mass <= 0.0f) {
			throw new IllegalArgumentException("Zero or negative friction supplied.");
		}
		this.friction = friction;
		
		return this;
	}
	
	/**
	 * Returns the friction constant of this body.
	 * 
	 * @return the friction constant.
	 */
	public float getFriction() {
		return friction;
	}
	
	/**
	 * Sets the mass of the body.
	 * 
	 * @param mass the new mass.
	 * @throws IllegalArgumentException if the supplied mass is 0 or lower.
	 * @return this body instance.
	 */
	public Body setMass(float mass) {
		if (mass <= 0.0f) {
			throw new IllegalArgumentException("Zero or negative mass supplied.");
		}
		this.mass = mass;
		this.invertedMass = 1.0f / mass;
		
		return this;
	}

	/**
	 * Returns the mass of this body.
	 * 
	 * @return the mass of the body.
	 */
	public float getMass() {
		return mass;
	}
	
	/**
	 * Returns the inverted mass of this body (1/mass).
	 * 
	 * @return the inverted mass of this body.
	 */
	public float getInvertedMass() {
		return invertedMass;
	}
	
	/**
	 * Returns the current velocity of this body.
	 * 
	 * @return the velocity of the body.
	 */
	public Vector2f getVelocity() {
		return velocity.clone();
	}
	
	/**
	 * Returns the velocity of the next frame (v + F / m).
	 * 
	 * @return the velocity after the next frame if nothing unexpected affects the body.
	 */
	public Vector2f getNextVelocity() {
		return getVelocity().add(force.clone().scale(invertedMass));
	}
	
	/**
	 * Returns the bounce (elasticity of this body.
	 * 
	 * @return the bounce (elasticity) of this body.
	 */
	public float getBounce() {
		return bounce;
	}
	
	/**
	 * Sets the bounce (elasticity) of the body. This decides how 
	 * much energy should be lost in a collision with this 
	 * body. A high elasticity will preserve more energy 
	 * than a collision with low elasticity. 
	 * 
	 * @param bounce the new bounce.
	 * @throws IllegalArgumentException if bounce is less than 0.
	 * @return this body instance.
	 */
	public Body setBounce(float bounce) {
		if (bounce < 0.0f) {
			throw new IllegalArgumentException("Negative bounce supplied.");
		}
		this.bounce = bounce;
		
		return this;
	}
	
	/**
	 * Adds a force to this body.
	 * 
	 * @param force the force to add to this body.
	 * @return this body instance.
	 */
	public Body addForce(Vector2f force) {
		Vector2f.add((Vector2f) force, this.force, this.force);
		
		return this;
	}
	
	/**
	 * Adds a force along the x axis to this body.
	 * 
	 * @param x the force to apply.
	 * @return this body instance.
	 */
	public Body addForceX(float x) {
		return addForce(new Vector2f(x, 0));
	}
	
	/**
	 * Adds a force along the y axis to this body.
	 * 
	 * @param y the force to apply.
	 * @return this body instance.
	 */
	public Body addForceY(float y) {
		return addForce(new Vector2f(0, y));
	}
	
	/**
	 * Adds velocity to this body.
	 * 
	 * <p>
	 * This should not be used if you want to make
	 * things look physically accurate, but it may be useful in
	 * gameplay situations.
	 * </p>
	 * 
	 * @param vel the velocity to add.
	 * @return this body instance.
	 */
	public Body addVelocity(Vector2f vel) {
		Vector2f.add(vel, velocity, velocity);
		
		return this;
	}
	
	/**
	 * Adds velocity along the x axis of this body.
	 * 
	 * <p>
	 * This should not be used if you want to make
	 * things look physically accurate, but it may be useful in
	 * gameplay situations.
	 * </p>
	 * 
	 * @param x the velocity to apply.
	 * @return this body instance.
	 */
	public Body addVelocityX(float x) {
		Vector2f.add(new Vector2f(x, 0), velocity, velocity);
		
		return this;
	}
	
	/**
	 * Adds velocity along the y axis of this body.
	 * 
	 * <p>
	 * This should not be used if you want to make
	 * things look physically accurate, but it may be useful in
	 * gameplay situations.
	 * </p>
	 * 
	 * @param y the velocity to apply.
	 * @return this body instance.
	 */
	public Body addVelocityY(float y) {
		Vector2f.add(new Vector2f(0, y), velocity, velocity);
		
		return this;
	}
	
	/**
	 * Sets velocity to the supplied value.
	 * 
	 * <p>
	 * This should not be used if you want to make
	 * things look physically accurate, but it may be useful in
	 * gameplay situations.
	 * </p>
	 * 
	 * @param vel the new velocity.
	 * @return this body instance.
	 */
	public Body setVelocity(Vector2f vel) {
		velocity = vel.clone();
		
		return this;
	}
	
	/**
	 * Sets the current velocity along the x axis.
	 * 
	 * <p>
	 * This should not be used if you want to make
	 * things look physically accurate, but it may be useful in
	 * gameplay situations.
	 * </p>
	 * 
	 * @param x the velocity to set.
	 * @return this body instance.
	 */
	public Body setVelocityX(float x) {
		velocity.x = x;
		
		return this;
	}
	
	/**
	 * Sets the current velocity along the y axis.
	 * 
	 * <p>
	 * This should not be used if you want to make
	 * things look physically accurate, but it may be useful in
	 * gameplay situations.
	 * </p>
	 * 
	 * @param y the velocity to set.
	 * @return this body instance.
	 */
	public Body setVelocityY(float y) {
		velocity.y = y;
		
		return this;
	}
	
	
	
	/**
	 * Returns the current transform of this body. If this body has an entity parent,
	 * and {@link #decouple(Transform)} has not been called, the transform will be
	 * equal to its parent's.
	 * 
	 * @return the transform of this body.
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
	 * @param delta the time passed since the last frame.
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
	 * Returns the momentum of this body.
	 * 
	 * @return the momentum (velocity * mass).
	 */
	public Vector2f getMomentum() {
		return (Vector2f) getVelocity().scale(mass);
	}

	/**
	 * @return true if this body is a trigger.
	 */
	public boolean isTrigger() {
		return trigger;
	}

	/**
	 * Sets whether this body should be a trigger or not.
	 * 
	 * A trigger body will not generate collision response.
	 * It will only check whether something is overlapping
	 * with it.
	 * 
	 * @param trigger if the body should be a trigger.
	 * @return this body instance.
	 */
	public Body setTrigger(boolean trigger) {
		this.trigger = trigger;
		
		return this;
	}
	
	/**
	 * Returns the current layer of the body.
	 * 
	 * @return the layer the body is on.
	 */
	public short getLayer() {
		return layer;
	}

	/**
	 * The layers are handled as bitmaps.
	 * If two bodies have overlapping layer bits,
	 * they will respond to each other.
	 * 
	 * @param layer the new layer bitmap.
	 * @return this body instance.
	 */
	public Body setLayer(short layer) {
		this.layer = layer;
		
		return this;
	}
	
	/**
	 * Checks if the other body shares a layer with this
	 * body (if the bit maps are overlapping).
	 * 
	 * @param b the other body.
	 * @return true if they share a layer.
	 */
	public boolean sharesLayer(Body b) {
		return (getLayer() & b.getLayer()) != 0;
	}
	
	/**
	 * Returns the tag of this body.
	 * 
	 * @return the tag of this body.
	 */
	public String getTag() {
		return tag;
	}
	
	/**
	 * Sets the tag of this body.
	 * 
	 * @param tag the new tag of this body.
	 * @return this body instance.
	 */
	public Body setTag(String tag) {
		this.tag = tag;
		
		return this;
	}
	
	/**
	 * Returns the number of shapes in this body.
	 * 
	 * @return the number of shapes in this body.
	 */
	public int getNumberOfShapes() {
		return shapes.size();
	}
	
	/**
	 * Adds a new shape to this body.
	 * 
	 * @param shape the shape you wish to add.
	 * @return this body instance.
	 * @throws IllegalArgumentException if the shape to be added already exists.
	 */
	public Body addShape(Shape shape) {
		for (Shape s : shapes) {
			if (s == shape) {
				throw new IllegalArgumentException("The same shape may not be added twice!");
			}
		}
		
		shapes.add(shape);
		return this;
	}
	
	/**
	 * Adds new shapes to this body.
	 * 
	 * @param shapes the shapes you wish to add.
	 * @throws IllegalArgumentException if a shape to be added already exists.
	 * @return this body instance.
	 */
	public Body addShape(List<Shape> shapes) {
		for (Shape s : shapes) {
			addShape(s);
		}
		return this;
	}
	
	/**
	 * Removes the specified shape.
	 * 
	 * @param shape the shape you want to remove.
	 * @return true if the specified shape could be removed.
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
	 * Returns the complete list of all shapes on the body.
	 * 
	 * @return a list of shapes on the body.
	 */
	public ArrayList<Shape> getShapes() {
		return shapes;
	}

	/**
	 * Returns a safe copy of the current direction.
	 * 
	 * @return a safe copy of the current direction.
	 */
	public Vector2f getOneWayDirection() {
		return direction.clone();
	}
	
	/**
	 * Sets the direction of this body from a vector. The vector will be normalized.
	 * 
	 * @param direction the new direction.
	 * @return this body instance.
	 */
	public Body setOneWayDirection(Vector2f direction) {
		this.direction = (Vector2f) direction.normalise();
		
		return this;
	}
	
	/**
	 * How lenient collisions set to one way should be.
	 * 
	 * 1 means "allow all directions".
	 * 0 means "only allow the specified direction".
	 * 0.5 means "allow ones that are facing at most 90 degrees off".
	 * 
	 * @return the leniency.
	 */
	public float getOneWayLeniency() {
		return leniency;
	}

	/**
	 * How lenient collisions set to one way should be.<br>
	 * 1 means "allow all direction".
	 * 0 means "allow NO directions".
	 * 0.5 means "allow ones that are facing at most 90 degrees off".
	 * 
	 * @return this body instance.
	 * @throws IllegalArgumentException if the specified leniency is not in range <code>0 - 1</code>.
	 */
	public Body setOneWayLeniency(float leniency) {
		if (leniency < 0 || leniency > 1) {
			throw new IllegalArgumentException("The specified leanancy is not in the valid range of 0 to 1");
		}
		this.leniency = leniency;
		
		return this;
	}
	
	
	/**
	 * Checks if the supplied normal lives up to the
	 * demands put on it by society and how well
	 * it points in the OneWayDirection.
	 * 
	 * @param n the normal to check.
	 * @return true if the normal lives up to the demands.
	 */
	public boolean oneWayCheck(Vector2f n) {
		if (leniency == 1) return true;
		
		float dot = n.dot(direction);
		if ((dot + 1.0f) * 0.5f >= leniency) {
			return true;
		}
		return false;
	}
}