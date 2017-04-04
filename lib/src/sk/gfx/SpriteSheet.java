package sk.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SpriteSheet {
	
	private Texture[] textures;
	
	private int width;
	private int height;
	private int tilesX;
	private int tilesY;
	
	/**
	 * 
	 * Creates a new empty sprite sheet. {@link #generate(String, int, int) generate()} should be called before use.
	 * 
	 */
	public SpriteSheet() {
		
	}
	
	/**
	 * 
	 * Create a new sprite sheet.
	 * 
	 * @param path the path of the texture file to create from.
	 * @param tilesX the amount of tiles on the x-axis in the file.
	 * @param tilesY the amount of tiles on the y-axis in the file.
	 */
	public SpriteSheet(String path, int tilesX, int tilesY) {
		generate(path, tilesX, tilesY);
	}
	
	/**
	 * 
	 * Generates the sprite sheet.
	 * 
	 * @param path the path of the texture file to create from.
	 * @param tilesX the amount of tiles on the x-axis in the file.
	 * @param tilesY the amount of tiles on the y-axis in the file.
	 */
	public SpriteSheet generate(String path, int tilesX, int tilesY) {
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		
		BufferedImage img = null;
		
		try {
			
			img = ImageIO.read(new File(path));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		textures = new Texture[tilesX * tilesY];
		
		width = img.getWidth();
		height = img.getHeight();
		
		int tw = width / tilesX;
		int th = height / tilesY;
		
		for(int i = 0; i < tilesY; i++) {
			for(int j = 0; j < tilesX; j++) {
				int[] pixels = new int[tw * th];
				
				img.getRGB(j * tw, i * th, tw, th, pixels, 0, tw);
				
				textures[i * tilesX + j] = new Texture().generate(tw, th, pixels);
			}
		}
		
		return this;
	}
	
	/**
	 * 
	 * Returns the width of the entire sprite sheet in pixels.
	 * 
	 * @return the width of the sprite sheet.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * 
	 * Returns the height of the entire sprite sheet in pixels.
	 * 
	 * @return the height of the sprite sheet.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * 
	 * Returns the number of tiles on the x-axis of this texture.
	 * 
	 * @return the number of tiles.
	 */
	public int getTilesX() {
		return tilesX;
	}
	
	/**
	 * 
	 * Returns the number of tiles on the y-axis of this texture.
	 * 
	 * @return the number of tiles.
	 */
	public int getTilesY() {
		return tilesY;
	}
	
	/**
	 * 
	 * Returns the width of each tile in pixels.
	 * 
	 * @return the width of each tile.
	 */
	public int getTileWidth() {
		return width / tilesX;
	}
	
	/**
	 * 
	 * Returns the height of each tile in pixels.
	 * 
	 * @return the height of each tiles in pixels.
	 */
	public int getTileHeight() {
		return height / tilesY;
	}
	
	/**
	 * 
	 * Returns the texture at the given offset in the sprite sheet.
	 * 
	 * @param offset the offset wrapped by each row.
	 * @return the texture at the given offset.
	 */
	public Texture getTexture(int offset) {
		return textures[offset];
	}
	
	/**
	 * 
	 * Returns the texture at the given coordinates in the sprite sheet.
	 * 
	 * @param x the x-coordinate of the texture.
	 * @param y the y-coordinate of the texture.
	 * @return the texture at the given coordinates.
	 */
	public Texture getTexture(int x, int y) {
		return textures[y * tilesX + x];
	}
	
	/**
	 * 
	 * Destroys all textures created by this sprite sheet.
	 * 
	 */
	public void destroy() {
		for(Texture t : textures)
			t.destroy();
	}
}