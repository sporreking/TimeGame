package sk.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import sk.util.vector.Vector3f;

public class FontTexture extends Texture {
	
	/**
	 * 
	 * Creates a new empty font texture.<br>
	 * {@link #generate(String, int, int, int, int, Font, Vector3f) generate()} should be called before use.
	 * 
	 */
	public FontTexture() {
		
	}
	
	/**
	 * 
	 * Creates a new font texture. The font is {@literal Arial Black} and the color is black.
	 * 
	 * @param text the text to create.
	 * @param width the width of the texture.
	 * @param height the height of the texture.
	 * @param x the x offset of the text in the texture.
	 * @param y the y offset of the text in the texture.
	 */
	public FontTexture(String text, int width, int height, int x, int y) {
		this(text, width, height, x, y, new Font("Arial Black", Font.BOLD, 11));
	}
	
	/**
	 * 
	 * Creates a new font texture. The color of the font will be black.
	 * 
	 * @param text the text to create.
	 * @param width the width of the texture.
	 * @param height the height of the texture.
	 * @param x the x offset of the text in the texture.
	 * @param y the y offset of the text in the texture.
	 * @param font the font of the text.
	 */
	public FontTexture(String text, int width, int height, int x, int y, Font font) {
		this(text, width, height, x, y, font, new Vector3f());
	}
	
	/**
	 * 
	 * Creates a new font texture.
	 * 
	 * @param text the text to create.
	 * @param width the width of the texture.
	 * @param height the height of the texture.
	 * @param x the x offset of the text in the texture.
	 * @param y the y offset of the text in the texture.
	 * @param font the font of the text.
	 * @param color the color of the text.
	 */
	public FontTexture(String text, int width, int height, int x, int y, Font font, Vector3f color) {
		generate(text, width, height, x, y, font, color);
	}
	
	/**
	 * 
	 * Generates the texture.
	 * 
	 * @param text the text to create.
	 * @param width the width of the texture.
	 * @param height the height of the texture.
	 * @param x the x offset of the text in the texture.
	 * @param y the y offset of the text in the texture.
	 * @param font the font of the text.
	 * @param color the color of the text.
	 * @return this font texture instance.
	 */
	public FontTexture generate(String text, int width, int height, int x, int y, Font font, Vector3f color) {
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics gfx = img.getGraphics();
		
		float[] hsb = new float[3];
		
		Color.RGBtoHSB((int) Math.floor(color.x * 255),
				(int) Math.floor(color.y * 255), (int) Math.floor(color.z * 255), hsb);
		
		((Graphics2D) gfx).setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		gfx.setColor(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
		gfx.setFont(font);
		gfx.drawString(text, x, y);
		
		int[] pixels = new int[width * height];
		
		img.getRGB(0, 0, width, height, pixels, 0, width);
		
		generate(width, height, pixels);
		
		return this;
	}
	
}