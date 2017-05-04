package sk.gfx.gui;

/**
 * The position of the text when it is renderd to a texture.
 * 
 * The positions can be any corner or side in a rectangle or the center.
 */
public enum GUITextPosition {
	CENTER(0), BOTTOM(1), TOP(2), LEFT(4), BOTTOM_LEFT(5), TOP_LEFT(6), RIGHT(8), BOTTOM_RIGHT(9), TOP_RIGHT(10);

	protected int value;

	private GUITextPosition(int value) {
		this.value = value;
	}

	public boolean and(int n) {
		return (n & value) != 0;
	}

	public boolean and(GUITextPosition n) {
		return (n.value & value) != 0;
	}
}