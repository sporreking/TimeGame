package sk.gfx;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import sk.util.vector.Matrix4f;

public class ShaderProgram {
	
	private int id;
	
	private Shader vert, frag;
	
	private HashMap<String, Integer> uniformLocations = new HashMap<>();
	
	/**
	 * 
	 * Creates an empty shader program.
	 * {@link #create(String vertPath, String fragPath) create()} should be called before use.
	 * 
	 */
	public ShaderProgram() {
		
	}
	
	/**
	 * 
	 * Creates a shader program from the vertex and fragment shader source files
	 * specified via the paths.
	 * 
	 * @param vertPath the path to the vertex shader.
	 * @param fragPath the path to the fragment shader.
	 */
	public ShaderProgram(String vertPath, String fragPath) {
		create(vertPath, fragPath);
	}
	
	/**
	 * 
	 * Creates a shader program from the vertex and fragment shader source files
	 * specified via the paths. Should not be called if data has already been supplied
	 * via constructor
	 * 
	 * @param vertPath the path to the vertex shader.
	 * @param fragPath the path to the fragment shader.
	 */
	public void create(String vertPath, String fragPath) {
		
		vert = new Shader(vertPath, GL_VERTEX_SHADER);
		frag = new Shader(fragPath, GL_FRAGMENT_SHADER);
		
		id = glCreateProgram();
		
		glAttachShader(id, vert.getID());
		glAttachShader(id, frag.getID());
		
		glLinkProgram(id);
		
		if(glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
			int length = glGetProgrami(id, GL_INFO_LOG_LENGTH);
			throw new IllegalStateException("Could not link program v:\""
					+ vertPath + "\" & f:\"" + fragPath + "\": "
					+ glGetProgramInfoLog(id, length));
		}
		
		glDetachShader(id, vert.getID());
		glDetachShader(id, frag.getID());
		vert.destroy();
		frag.destroy();
	}
	
	/**
	 * 
	 * Tells OpenGL to use this shader program until otherwise is told.
	 * 
	 */
	public void use() {
		glUseProgram(id);
	}
	
	/**
	 * 
	 * Sends 1 integer to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param i the integer to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send1i(String location, int i) {
		glUniform1i(getUniformLocation(location), i);
		
		return this;
	}
	
	/**
	 * 
	 * Sends 2 integers to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param i the first integer to send.
	 * @param j the second integer to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send2i(String location, int i, int j) {
		glUniform2i(getUniformLocation(location), i, j);
		
		return this;
	}
	
	/**
	 * 
	 * Sends 3 integers to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param i the first integer to send.
	 * @param j the second integer to send.
	 * @param k the third integer to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send3i(String location, int i, int j, int k) {
		glUniform3i(getUniformLocation(location), i, j, k);
		
		return this;
	}
	
	/**
	 * 
	 * Sends 4 integers to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param i the first integer to send.
	 * @param j the second integer to send.
	 * @param k the third integer to send.
	 * @param l the fourth integer to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send4i(String location, int i, int j, int k, int l) {
		glUniform4i(getUniformLocation(location), i, j, k, l);
		
		return this;
	}
	
	/**
	 * 
	 * Sends 1 float to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param f the float to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send1f(String location, float f) {
		glUniform1f(getUniformLocation(location), f);
		
		return this;
	}
	
	/**
	 * 
	 * Sends 2 floats to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param f1 the first float to send.
	 * @param f2 the second float to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send2f(String location, float f1, float f2) {
		glUniform2f(getUniformLocation(location), f1, f2);
		
		return this;
	}
	
	/**
	 * 
	 * Sends 3 floats to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param f1 the first float to send.
	 * @param f2 the second float to send.
	 * @param f3 the third float to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send3f(String location, float f1, float f2, float f3) {
		glUniform3f(getUniformLocation(location), f1, f2, f3);
		
		return this;
	}
	
	/**
	 * 
	 * Sends 4 floats to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param f1 the first integer to send.
	 * @param f2 the second integer to send.
	 * @param f3 the third integer to send.
	 * @param f4 the fourth integer to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram send4f(String location, float f1, float f2, float f3, float f4) {
		glUniform4f(getUniformLocation(location), f1, f2, f3, f4);
		
		return this;
	}
	
	/**
	 * 
	 * Sends a 4x4 matrix to the specified uniform in this program.
	 * 
	 * @param location the name of the uniform.
	 * @param mat4 the matrix to send.
	 * @return this shader program instance.
	 */
	public ShaderProgram sendM4(String location, Matrix4f mat4) {
		
		FloatBuffer buffer = ByteBuffer.allocateDirect((4 * 4) << 2)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		buffer.position(0);
		mat4.store(buffer);
		
		buffer.flip();
		
		glUniformMatrix4fv(getUniformLocation(location), false, buffer);
		
		return this;
	}
	
	/**
	 * 
	 * Gets the location id of the specified uniform in this program.
	 * The first call for a specific location may be slower.
	 * 
	 * @param location the uniform name.
	 * @return the id.
	 */
	public int getUniformLocation(String location) {
		Integer i = uniformLocations.get(location);
		
		if(i == null) {
			i = glGetUniformLocation(id, location);
			uniformLocations.put(location, i);
		}
		
		return i;
	}
	
	/**
	 * 
	 * Deletes the OpenGL object generated by this shader program.
	 * 
	 */
	public void destroy() {
		glDeleteProgram(id);
	}
	
	public static final ShaderProgram ORTHO;
	public static final ShaderProgram GUI;
	
	static {
		ORTHO = new ShaderProgram("res/shader/ortho.vert", "res/shader/ortho.frag");
		GUI = new ShaderProgram("res/shader/gui.vert", "res/shader/gui.frag");
	}
	
	/**
	 * 
	 * Destroys all engine-created shader programs.
	 * 
	 */
	public static final void destroyAll() {
		ORTHO.destroy();
		GUI.destroy();
	}
}