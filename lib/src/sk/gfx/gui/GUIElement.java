package sk.gfx.gui;

import sk.entity.Component;
import sk.game.Window;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.ShaderProgram;
import sk.gfx.Transform;

public class GUIElement extends Renderer {
	
	protected float anchorX;
	protected float anchorY;
	protected int offsetX;
	protected int offsetY;
	protected int width;
	protected int height;
	
	protected GUIText text;
	
	/**
	 * Creates a new GUI element, and attaches it to the screen.
	 * 
	 * @param anchorX the x-coordinate of this GUI element's anchor point. 
	 * @param anchorY the y-coordinate of this GUI element's anchor point.
	 * @param offsetX the x-axis offset in pixels from the anchor point.
	 * @param offsetY the y-axis offset in pixels from the anchor point.
	 * @param width the width of this GUI element in pixels.
	 * @param height the height of this GUI element in pixels.
	 */
	public GUIElement(float anchorX, float anchorY, int offsetX, int offsetY, int width, int height) {
		super(Mesh.QUAD);
		camera = Camera.GUI;
		
		this.anchorX = anchorX;
		this.anchorY = anchorY;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		
		text = null;
		
		updateTransform();
	}
	
	@Override
	public void init() {
		if(getParent().has(Transform.class))
			transform = getParent().get(Transform.class);
		
		updateTransform();
	}
	
	/**
	 * 
	 * Updates the transform of this GUI element.
	 * 
	 * @param anchorX the x-coordinate of this GUI element's anchor point. 
	 * @param anchorY the y-coordinate of this GUI element's anchor point.
	 * @param offsetX the x-axis offset in pixels from the anchor point.
	 * @param offsetY the y-axis offset in pixels from the anchor point.
	 * @param width the width of this GUI element in pixels.
	 * @param height the height of this GUI element in pixels.
	 */
	public void updateTransform() {
		
		transform.scale.x = 2.0f * width /  Window.getWidth();
		transform.scale.y = 2.0f * height / Window.getHeight();
		
		transform.position.x = anchorX + 2.0f * offsetX / Window.getWidth();
		transform.position.y = anchorY + 2.0f * offsetY / Window.getHeight();
	}
	
	/**
	 * 
	 * Translates the GUI element.
	 * 
	 * @param dx the distance to translate on the x-axis.
	 * @param dy The distance to translate on the y-axis.
	 */
	public void translate(float dx, float dy) {
		transform.position.x += dx;
		transform.position.y += dy;
	}
	
	/**
	 * Adds or changes the text that is displayed on top of this GUI
	 * @param text the new GUIText
	 */
	public void setText(GUIText text) {
		this.text = text;
	}
	
	@Override	
	public void update(double delta) {}
	
	@Override
	public void draw() {
		setupShader();
	
		//Send the texture id
		ShaderProgram.GUI.send1i("t_sampler", 0);

		//Tell the shader that this is NOT a fader
		ShaderProgram.GUI.send1i("b_is_fader", 0);
		
		//Bind the texture
		getTexture().bind(0);

		//Binds the text if it is available
		if (text != null) {
			text.bind();
		}
		
		getMesh().draw();
	}
	
	/**
	 * 
	 * Called before each draw call to bind the GUI shader program and send it's appropriate matrices.
	 * 
	 */
	protected void setupShader() {
		//Select shader program
		ShaderProgram.GUI.use();
		
		//Send projection matrix
		ShaderProgram.GUI.sendM4("projection", camera.getProjection());
		
		//Send view matrix
		ShaderProgram.GUI.sendM4("view", camera.getMatrix());
		
		//Send model matrix
		ShaderProgram.GUI.sendM4("model", transform.getMatrix());
	}
}