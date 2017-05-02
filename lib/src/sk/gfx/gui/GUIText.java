package sk.gfx.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import sk.gfx.FontTexture;
import sk.gfx.ShaderProgram;
import sk.gfx.Texture;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector3f;

/**
 * GUI text is text that can be drawn on any GUIElement.
 * 
 * The text is renderd on the CPU to a bit-map that later is used as a texture
 * where the red chanel is replaced with the color specified. As sutch, color
 * switching is very cheep. But changing the font a lot is very costly, since
 * the entire image has to be re draw.
 *
 */
public class GUIText {
	protected String text = "";
	protected FontTexture texture = null;
	protected Vector3f color = null;
	protected Font font = null;

	protected int width = -1;
	protected int height = -1;

	protected Vector2f padding = new Vector2f();
	protected Vector2f offset = new Vector2f();
	GUITextPosition position = GUITextPosition.CENTER;

	private boolean dirty = true;;

	/**
	 * For the person who doesn't want anything written displayed.
	 */
	public GUIText() {
	}

	/**
	 * @param text
	 *            the text we wish to write
	 * @param width
	 *            the width of our new image
	 * @param height
	 *            the height of out new image
	 * @param font
	 *            the font that should be used
	 */
	public GUIText(String text, int width, int height, Font font) {
		this(text, width, height, font, new Vector3f(), GUITextPosition.CENTER, new Vector2f());
	}

	/**
	 * @param text
	 *            the text we wish to write
	 * @param width
	 *            the width of our new image
	 * @param height
	 *            the height of out new image
	 * @param font
	 *            the font that should be used
	 * @param color
	 *            a color that will be used for when the font is displayed
	 */
	public GUIText(String text, int width, int height, Font font, Vector3f color) {
		this(text, width, height, font, color, GUITextPosition.CENTER, new Vector2f());
	}

	/**
	 * @param text
	 *            the text we wish to write
	 * @param width
	 *            the width of our new image
	 * @param height
	 *            the height of out new image
	 * @param font
	 *            the font that should be used
	 * @param position
	 *            the position of the text, enumeration wise
	 */
	public GUIText(String text, int width, int height, Font font, GUITextPosition position) {
		this(text, width, height, font, new Vector3f(), position, new Vector2f());
	}

	/**
	 * The do it all constructor, it constructs everything
	 * 
	 * @param text
	 *            the text we wish to write
	 * @param width
	 *            the width of our new image
	 * @param height
	 *            the height of out new image
	 * @param font
	 *            the font that should be used
	 * @param color
	 *            a color that will be used for when the font is displayed
	 * @param position
	 *            the position of the text, enumeration wise
	 * @param padding
	 *            the padding of the text position in pixels
	 */
	public GUIText(String text, int width, int height, Font font, Vector3f color, GUITextPosition position,
			Vector2f padding) {
		this.text = text;
		this.width = width;
		this.height = height;
		this.font = font;
		this.color = color;
		this.position = position;
		this.padding = padding.clone();
		texture = new FontTexture();
	}

	/**
	 * Binds all the necessities for the text to be drawn properly
	 * 
	 * @return
	 */
	public GUIText bind() {
		// Make sure it doesn't need to re draw
		draw();

		if (texture.getID() != 0) {
			// We have a valid texture so send in all the information
			ShaderProgram.GUI.send1i("b_has_text", 1);

			// Bind the rendered text
			ShaderProgram.GUI.send1i("t_text", 3);
			texture.bind(3);

			// Send in the color
			ShaderProgram.GUI.send4f("v_text_color", color.x, color.y, color.z, 1.0f);
		} else {
			// Tell the shader it doesn't need to draw text
			ShaderProgram.GUI.send1i("b_has_text", 0);
		}
		return this;
	}

	/**
	 * Generates the texture for the text, which later can be bound
	 * 
	 * @return itself for chaining
	 */
	public GUIText draw() {
		// If it isn't dirty, don't draw it
		if (!dirty)
			return this;
		
		// Delete the texture if we have it
		if (texture.getID() != 0)
			texture.destroy();

		// If we have no text, just return
		if (text.equals("")) {
			dirty = false;
			return this;
		}

		// Position the text
		Graphics gfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();
		gfx.setFont(font);
		
		FontMetrics fm = gfx.getFontMetrics();
		int fontWidth = fm.stringWidth(text);
		int fontHeight = fm.getHeight();

		int x = 0;
		int y = 0;

		if (position.and(GUITextPosition.TOP)) {
			y = fontHeight + (int) padding.y;
		} else if (position.and(GUITextPosition.BOTTOM)) {
			y = height - fontHeight / 2 - (int) padding.y;
		} else {
			y = height / 2 + fontHeight / 2;
		}

		if (position.and(GUITextPosition.LEFT)) {
			x = (int) padding.x;
		} else if (position.and(GUITextPosition.RIGHT)) {
			x = width - fontWidth - (int) padding.x;
		} else {
			x = width / 2 - fontWidth / 2;
		}

		texture.generate(text, width, height, x, y, font, new Vector3f(1.0f, 0.0f, 0.0f));

		// Now we're clean, so flip the flag
		dirty = false;
		return this;
	}

	public String getText() {
		return text;
	}

	public GUIText setText(String text) {
		if (this.text.equals(text)) return this;
		
		this.text = text;
		dirty = true;
		return this;
	}

	public Vector3f getColor() {
		return color;
	}

	public GUIText setColor(Vector3f color) {
		this.color = color;
		return this;
	}

	public Font getFont() {
		return font;
	}

	public GUIText setFont(Font font) {
		this.font = font;
		dirty = true;
		return this;
	}

	public int getWidth() {
		return width;
	}

	public GUIText setWidth(int width) {
		this.width = width;
		dirty = true;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public GUIText setHeight(int height) {
		this.height = height;
		dirty = true;
		return this;
	}

	public Vector2f getPadding() {
		return padding;
	}

	public GUIText setPadding(Vector2f padding) {
		this.padding = padding;
		dirty = true;
		return this;
	}

	public GUITextPosition getPosition() {
		return position;
	}

	public GUIText setPosition(GUITextPosition position) {
		this.position = position;
		dirty = true;
		return this;
	}
}
