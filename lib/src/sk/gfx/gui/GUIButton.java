package sk.gfx.gui;

import sk.entity.Component;
import sk.entity.component.AABB;
import sk.game.Window;
import sk.gfx.Transform;
import sk.util.io.Mouse;
import sk.util.vector.Vector3f;

public class GUIButton extends GUIElement {

	private int mouseButton;
	private boolean isDown = false;
	private boolean isOver = false;
	
	private Event onClick, onRelease, onHover, onUnhover;
	
	/**
	 * 
	 * Creates a new GUI button.
	 * 
	 * @param anchorX the x-coordinate of this GUI element's anchor point. 
	 * @param anchorY the y-coordinate of this GUI element's anchor point.
	 * @param offsetX the x-axis offset in pixels from the anchor point.
	 * @param offsetY the y-axis offset in pixels from the anchor point.
	 * @param width the width of this GUI element in pixels.
	 * @param height the height of this GUI element in pixels.
	 */
	public GUIButton(float anchorX, float anchorY, int offsetX, int offsetY, int width, int height) {
		this(anchorX, anchorY, offsetX, offsetY, width, height, 0);
	}
	
	/**
	 * 
	 * Creates a new GUI button.
	 * 
	 * @param anchorX the x-coordinate of this GUI element's anchor point. 
	 * @param anchorY the y-coordinate of this GUI element's anchor point.
	 * @param offsetX the x-axis offset in pixels from the anchor point.
	 * @param offsetY the y-axis offset in pixels from the anchor point.
	 * @param width the width of this GUI element in pixels.
	 * @param height the height of this GUI element in pixels.
	 * @param mouseButton the button to react to.
	 */
	public GUIButton(float anchorX, float anchorY, int offsetX, int offsetY, int width, int height, int mouseButton) {
		this(anchorX, anchorY, offsetX, offsetY, width, height, 0, new Vector3f(), "");
	}
	
	/**
	 * 
	 * Creates a new GUI button.
	 * 
	 * @param anchorX the x-coordinate of this GUI element's anchor point. 
	 * @param anchorY the y-coordinate of this GUI element's anchor point.
	 * @param offsetX the x-axis offset in pixels from the anchor point.
	 * @param offsetY the y-axis offset in pixels from the anchor point.
	 * @param width the width of this GUI element in pixels.
	 * @param height the height of this GUI element in pixels.
	 * @param mouseButton the button to react to.
	 * @param textColor the color the text should have.
	 * @param text the text that should display over the button.
	 */
	public GUIButton(float anchorX, float anchorY, int offsetX, int offsetY, int width, int height, int mouseButton, Vector3f textColor, String text) {
		super(anchorX, anchorY, offsetX, offsetY, width, height);
		this.mouseButton = mouseButton;
	}
	
	@Override
	public void init() {
		super.init();
		
		if(!getParent().has(AABB.class))
			getParent().add(0, new AABB(2.0f * width / Window.getWidth(),
					2.0f * height / Window.getHeight(), transform));
	}
	
	@Override
	public void update(double delta) {
		if (Mouse.wasChanged()) {
			if (getParent().get(AABB.class).contains(Mouse.projectPosition(camera.getProjection()))) {
				if (!isOver && onHover != null) {
					onHover.fire();
				}
				isOver = true;
			} else {
				if (isOver && onUnhover != null) {
					onUnhover.fire();
				}
				isOver = false;
			}
			
			if (Mouse.pressed(mouseButton) && isOver && onClick != null) {
				onClick.fire();
			}
			
			if (Mouse.released(mouseButton) && isOver && onRelease != null) {
				onRelease.fire();
			}
		}
	}
	
	/**
	 * 
	 * Returns what mouse button this GUI button is listening to.
	 * 
	 * @return the mouse button this GUI button is listening to.
	 */
	public int getMouseButton() {
		return mouseButton;
	}
	
	/**
	 * 
	 * Sets what mouse button this GUI button should listen to.
	 * 
	 * @param mouseButton the mouse button.
	 */
	public void setMouseButton(int mouseButton) {
		this.mouseButton = mouseButton;
	}
	
	/**
	 * 
	 * Returns the currently set click event, or null if none has been set.
	 * 
	 * @return the currently set click event.
	 */
	public Event getOnClick() {
		return onClick;
	}
	
	/**
	 * 
	 * Sets a click event for this GUI button.
	 * 
	 * @param onClick the click event.
	 */
	public void setOnClick(Event onClick) {
		this.onClick = onClick;
	}
	
	/**
	 * 
	 * Returns the currently set release event, or null if none has been set.
	 * 
	 * @return the release event.
	 */
	public Event getOnRelease() {
		return onRelease;
	}
	
	/**
	 * 
	 * Sets a release event for this GUI button.
	 * 
	 * @param onRelease the release event.
	 */
	public void setOnRelease(Event onRelease) {
		this.onRelease = onRelease;
	}
	
	/**
	 * 
	 * Returns the currently set hover event, or null if none has been set.
	 * 
	 * @return the hover event.
	 */
	public Event getOnHover() {
		return onHover;
	}
	
	/**
	 * 
	 * Sets a hover event for this GUI button.
	 * 
	 * @param onHover the hover event.
	 */
	public void setOnHover(Event onHover) {
		this.onHover = onHover;
	}
	
	/**
	 * 
	 * Returns the currently set unhover event, or null if none has been set.
	 * 
	 * @return the unhover event.
	 */
	public Event getOnUnhover() {
		return onUnhover;
	}
	
	/**
	 * 
	 * Sets an unhover event for this GUI button.
	 * 
	 * @param onUnhover the unhover event.
	 */
	public void setOnUnhover(Event onUnhover) {
		this.onUnhover = onUnhover;
	}
	
	/**
	 * 
	 * Returns whether or not the button is currently being pressed.
	 * 
	 * @return {@code true} if the button is currently being pressed.
	 */
	public boolean isDown() {
		return isDown;
	}
	
	/**
	 * 
	 * Returns whether or not the mouse is hovering over the button.
	 * 
	 * @return {@code true} if the button is being hovered over.
	 */
	public boolean isOver() {
		return isOver;
	}
	
}
