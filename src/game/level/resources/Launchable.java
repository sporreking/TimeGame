package game.level.resources;

import sk.entity.Component;
import sk.entity.Entity;
import sk.gfx.Transform;
import sk.util.vector.Vector2f;

/**
 * 
 * A Launchable is something that can be thrown.
 * 
 * @author Ed
 *
 */
public abstract class Launchable extends Component {
	protected Entity holder;
	protected Transform transform;
	protected Vector2f relativePosition;
	protected boolean held = false;
	
	public Launchable() {
		super();
	}
	
	/**
	 * 
	 * Tries to launch the held entity.
	 * 
	 * @param direction the direction and strength of the throw.
	 * @return if the launch was successful.
	 */
	public abstract boolean launch(Vector2f direction);
	
	/**
	 * 
	 * Tries to pick up an object. 
	 * 
	 * @param holder the one who wishes to pick it up.
	 * @return if it managed to pick it up.
	 */
	public abstract boolean pickup(Entity holder, Vector2f relativePosition);
	
	/**
	 * 
	 * If the entity is held or not.
	 * 
	 * @return if it is held or not.
	 */
	public boolean isHeld() {
		return held;
	}
	
	/**
	 * 
	 * The relative position when it is held.
	 * 
	 * @return the relative position when it is held.
	 */
	public Vector2f getRelativePosition() {
		return relativePosition.clone();
	}
	
	/**
	 * 
	 * Sets the relative position to the holder when it
	 * is held.
	 * 
	 * @param relativePosition the relative position.
	 */
	public void setRelativePosition(Vector2f relativePosition) {
		this.relativePosition.x = relativePosition.x;
		this.relativePosition.y = relativePosition.y;
	}
	
}
