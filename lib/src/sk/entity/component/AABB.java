package sk.entity.component;
import sk.entity.Component;
import sk.entity.Entity;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.util.vector.Vector2f;

public class AABB extends Component {
	
	private float halfWidth, halfHeight;
	private Vector2f min = new Vector2f();
	private Vector2f max = new Vector2f();
	
	private Transform transform;
	
	/**
	 * 
	 * Creates a new AABB (Axis Aligned Bounding Box).
	 * 
	 */
	public AABB() {
		
	}
	
	/**
	 * 
	 * Creates a new AABB (Axis Aligned Bounding Box) with the given width and height.
	 * 
	 * @param width the width of the AABB.
	 * @param height the height of the AABB.
	 */
	public AABB(float width, float height) {
		setWidth(width);
		setHeight(height);
	}
	
	/**
	 * 
	 * Creates a new AABB (Axis Aligned Bounding Box) with the supplied information.
	 * 
	 * @param width the width of the AABB.
	 * @param height the height of the AABB.
	 * @param transform the transform of the AABB.
	 */
	public AABB(float width, float height, Transform transform) {
		setWidth(width);
		setHeight(height);
		
		this.transform = transform;
	}
	
	/**
	 * 
	 * Sets the width of this AABB.
	 * 
	 * @param width the new width.
	 */
	public void setWidth(float width) {
		halfWidth = (float) (width / 2.0);
	}

	/**
	 * 
	 * Sets the height of this AABB.
	 * 
	 * @param height the new height.
	 */
	public void setHeight(float height) {
		halfHeight = (float) (height / 2.0);
	}
	
	/**
	 * 
	 * Returns the width of this AABB.
	 * 
	 * @return the width of this AABB.
	 */
	public float getWidth() {
		return halfWidth * 2;
	}
	
	/**
	 * 
	 * Returns the height of this AABB.
	 * 
	 * @return the height of this AABB.
	 */
	public float getHeight() {
		return halfHeight * 2;
	}
	
	@Override
	public void init() {
		if(getParent().has(Transform.class)) {
			transform = getParent().get(Transform.class);
		}
	}
	
	/**
	 * 
	 * Updates the boundaries of this AABB with respect to the transform.
	 * 
	 */
	private void updateBoundaries() {		
		min.x = -halfWidth + transform.position.x;
		max.x =  halfWidth + transform.position.x;
		
		min.y = -halfHeight + transform.position.y;
		max.y =  halfHeight + transform.position.y;
	}
	
	/**
	 * 
	 * Checks if this bounding box contains the specified point.
	 * 
	 * @param point the point to check.
	 * @return {@code true} if the point is contained.
	 */
	public boolean contains(Vector2f point) {
		//Make sure the boundaries are up to date
		updateBoundaries();
		
		return 
			(min.x < point.x && point.x < max.x) &&
			(min.y < point.y && point.y < max.y);
	}
	
	/**
	 * 
	 * Checks whether the entity's AABB intersects with this one.
	 * 
	 * @param entity The entity we wish to check against.
	 * @return {@code true} if the specified entity's AABB intersects with this one.
	 */
	public boolean intersects(Entity entity) {
		AABB aabb = entity.get(AABB.class);
		if (aabb != null) {
			return intersects(aabb);
		} else {
			throw new IllegalArgumentException("The entity supplied does not have an AABB.");
		}
	}
	
	/**
	 * 
	 * Checks whether the specified AABB intersects with this one.
	 * 
	 * @param aabb the other AABB to check with.
	 * @return {@code true} if the specified AABB intersects with this one.
	 */
	public boolean intersects(AABB aabb) {		
		//Make sure the boundaries are up to date
		updateBoundaries();
		aabb.updateBoundaries();
		
		return 
			((min.x < aabb.min.x && aabb.min.x < max.x) || 
			 (min.x < aabb.max.x && aabb.max.x < max.x)) &&
			((min.y < aabb.min.y && aabb.min.y < max.y) || 
			 (min.y < aabb.max.y && aabb.max.y < max.y));
	}
}
