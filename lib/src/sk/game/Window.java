package sk.game;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import sk.util.io.Mouse;
import sk.util.io.Keyboard;
import sk.util.vector.Vector4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

public final class Window {
	
	private static GLFWVidMode primary;
	
	private static long window;
	
	private static Vector4f clearColor;
	
	/**
	 * 
	 * Creates window, GL-context and sets up callbacks.
	 * 
	 */
	protected static final void create() {
		
		//Setup error callback
		GLFWErrorCallback.createPrint(System.err).set();
		
		//Initialize GLFW
		if(!glfwInit())
			throw new IllegalStateException("Failed to initialize GLFW");
		
		//Hints
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, Game.properties.resizable ? GLFW_TRUE : GLFW_FALSE);
		
		//Create window
		window = glfwCreateWindow(Game.properties.width, Game.properties.height,
				Game.properties.title, MemoryUtil.NULL, MemoryUtil.NULL);
		
		if(window == MemoryUtil.NULL)
			throw new IllegalStateException("Failed to create window");
		
		//Primary screen mode
		primary = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		//Center window
		glfwSetWindowPos(window, (primary.width() - getWidth()) / 2,
				(primary.height() - getHeight()) / 2);
		
		//Make context current
		glfwMakeContextCurrent(window);
		
		//Detect context
		GL.createCapabilities();
		
		//VSync?
		glfwSwapInterval(Game.properties.vSync ? 1 : 0);
		
		//Clear color
		setClearColor(Game.properties.clearColor);
		
		//Viewport
		glViewport(0, 0, Game.properties.width, Game.properties.height);
		
		//Setup key callback
		Keyboard.INSTANCE.set(window);
		Mouse.INSTANCE.set(window);
		Mouse.Cursor.INSTANCE.set(window);
	}
	
	/**
	 * 
	 * Fetches the current window width.
	 * 
	 * @return the width of the window in pixels.
	 */
	public static final int getWidth() {
		int[] width = new int[1];
		
		glfwGetWindowSize(window, width, null);
		
		return width[0];
	}
	
	
	/**
	 * 
	 * Fetches the current window height.
	 * 
	 * @return the height of the window in pixels.
	 */
	public static final int getHeight() {
		int[] height = new int[1];
		
		glfwGetWindowSize(window, null, height);
		
		return height[0];
	}
	
	/**
	 * 
	 * Returns the aspect ratio of the window.
	 * 
	 * @return the aspect ratio of the window.
	 */
	public static final float getAspectRatio() {
		return ((float) getWidth()) / ((float) getHeight());
	}
	
	/**
	 * 
	 * Clears the window with the pre-selected color.
	 * 
	 */
	public static final void clear() {
		glClear(GL_COLOR_BUFFER_BIT);
	}
	
	/**
	 * 
	 * Sets the clear color of the window.
	 * 
	 * @param clearColor the new clear color
	 */
	public static final void setClearColor(Vector4f clearColor) {
		Window.clearColor = clearColor;
		glClearColor(clearColor.getX(), clearColor.getY(), clearColor.getZ(), clearColor.getW());
	}
	
	/**
	 * 
	 * Gets the current clear color.
	 * 
	 * @return the current clear color.
	 */
	public static final Vector4f getClearColor() {
		return new Vector4f(clearColor);
	}
	
	/**
	 * 
	 * Swaps buffers with OpenGL and displays what has been rendered.
	 * 
	 */
	public static final void swapBuffers() {
		glfwSwapBuffers(window);
	}
	
	/**
	 * 
	 * Returns whether or not the window's close button has been pressed.
	 * 
	 * @return true if the close button has been pressed.
	 */
	protected static final boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}
	
	/**
	 * 
	 * Makes the window visible.
	 * 
	 */
	protected static final void show() {
		glfwShowWindow(window);
	}
	
	/**
	 * 
	 * Frees callbacks and destroys window.
	 * 
	 */
	protected static final void destroy() {
		Callbacks.glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
	}
}