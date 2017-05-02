package sk.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;

public class Texture {
	
	private int id = 0;
	
	private int width;
	private int height;
	
	/**
	 * 
	 * Creates a new empty texture. {@link #generate(String) generate()} should be called before use.
	 * 
	 */
	public Texture() {
		
	}
	
	/**
	 * 
	 * Creates a new texture from the specified file.
	 * 
	 * @param path the file path.
	 */
	public Texture(String path) {
		generate(path);
	}
	
	/**
	 * 
	 * Generates a texture from the specified file.
	 * 
	 * @param path the file path.
	 * @return this texture instance.
	 */
	public Texture generate(String path) {
		
		BufferedImage img = null;
		
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		
		generate(img.getWidth(), img.getHeight(), pixels);
		
		return this;
	}
	
	/**
	 * 
	 * Generates a texture from the specified bitmap.
	 * 
	 * @param width the width of the bitmap.
	 * @param height the height of the bitmap.
	 * @param pixels the bitmap.
	 * @return this texture instance.
	 */
	public Texture generate(int width, int height, int[] pixels) {
		
		if (id != 0)
			destroy();
		
		this.width = width;
		this.height = height;
		
		id = glGenTextures();
		
		bind();
		
		IntBuffer buffer = ByteBuffer.allocateDirect(pixels.length << 2)
				.order(ByteOrder.nativeOrder()).asIntBuffer();

		buffer.put(pixels);
		buffer.flip();
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
				GL_BGRA, GL_UNSIGNED_BYTE, buffer);
		
		return this;
	}
	
	/**
	 * 
	 * Binds this texture to the 0th sampler.
	 * 
	 * @return this texture instance.
	 */
	public Texture bind() {
		return bind(0);
	}
	
	/**
	 * 
	 * Binds this texture to the specified sampler.
	 * 
	 * @param target the target sampler.
	 * @return this texture instance.
	 */
	public Texture bind(int target) {
		glActiveTexture(GL_TEXTURE0 + target);
		glBindTexture(GL_TEXTURE_2D, id);
		
		return this;
	}
	
	/**
	 * 
	 * Returns the OpenGL generated ID of this texture.
	 * 
	 * @return the ID of this texture.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * 
	 * Returns the width of this texture in pixels.
	 * 
	 * @return the width of this texture.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * 
	 * Returns the height of this texture in pixels.
	 * 
	 * @return the height of this texture.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * 
	 * Destroys the generated texture.
	 * 
	 */
	public void destroy() {
		glDeleteTextures(id);
	}
	
	public static final Texture DEFAULT;
	
	static {
		DEFAULT = new Texture().generate(1, 1, new int[] {0xffffffff});
	}
	
	/**
	 * 
	 * Destroys all engine-created textures.
	 * 
	 */
	public static final void destroyAll() {
		DEFAULT.destroy();
	}
}