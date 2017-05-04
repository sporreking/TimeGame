package sk.entity;

public abstract class Component {
	
	private Entity parent = null;
	
	/**
	 * 
	 * This method is called when the component is added to an entity.
	 * 
	 */
	public void init() {}
	
	/**
	 * 
	 * This method is called when the entity is updated.
	 * 
	 * @param delta
	 */
	public void update(double delta) {}
	
	/**
	 * 
	 * This method is called when the entity is drawn.
	 * 
	 */
	public void draw() {}
	
	/**
	 * 
	 * This method is called when the component is removed from it's entity.
	 * 
	 */
	public void exit() {}
	
	/**
	 * 
	 * The returned components must already be attached to the entity before this component may be added.
	 * 
	 * @return the prerequisite components.
	 */
	public Class<? extends Component>[] requirements() { return null; }
	
	/**
	 * 
	 * Returns the parent entity of this component.
	 * 
	 * @return the parent of this component.
	 */
	public Entity getParent() {
		return parent;
	}
	
	/**
	 * 
	 * Sets the parent of this component. Should only be called by the engine.
	 * 
	 * @param parent the parent of this component.
	 */
	protected void setParent(Entity parent) {
		if(this.parent != null)
			throw new IllegalStateException("This component already has a parent");
		
		this.parent = parent;
	}
	
	/**
	 * 
	 * Removes the parent from this component. Should only be called by the engine.
	 * 
	 */
	protected void removeParent() {
		parent = null;
	}
}