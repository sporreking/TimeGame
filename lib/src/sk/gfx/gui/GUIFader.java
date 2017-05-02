package sk.gfx.gui;

import sk.gfx.ShaderProgram;
import sk.gfx.Texture;

public class GUIFader extends GUIElement{
	
	private Texture[] textures;
	private Texture mask;
	
	private float threshold;
	
	/**
	 * 
	 * Creates a new GUI element that fades between textures depending on a threshold and a mask. 
	 * It will render "textureA" if the sample from "mask" is greater than the threshold,
	 * otherwise it will render "textureB".
	 * 
	 * @param anchorX the x-coordinate of this GUI element's anchor point. 
	 * @param anchorY the y-coordinate of this GUI element's anchor point.
	 * @param offsetX the x-axis offset in pixels from the anchor point.
	 * @param offsetY the y-axis offset in pixels from the anchor point.
	 * @param width the width of this GUI element in pixels.
	 * @param height the height of this GUI element in pixels.
	 * @param mask the mask that handles the blending between the two textures.
	 * @param textureA The two textures that will have color sampled from it. 
	 */
	public GUIFader(float anchorX, float anchorY, int offsetX, int offsetY, int width, int height,
			Texture mask, Texture textureA, Texture textureB) {
		super(anchorX, anchorY, offsetX, offsetY, width, height);
		
		this.textures = new Texture[] { textureA, textureB };
		this.mask = mask;
		
		threshold = 0.5f;
	}

	/**
	 * 
	 * Sets textureA or textureB.
	 * 
	 * @param index the index of the texture, 0 = textureA and 1 = textureB.
	 * @param texture The texture.
	 */
	public void setTexture(int index, Texture texture) {
		this.textures[index] = texture;
	}
	
	/**
	 * 
	 * Sets the threshold. This value controls which texture will be drawn.
	 * It will render "textureA" if the sample from "mask" is greater than the threshold,
	 * otherwise it will render "textureB".
	 * 
	 * @param threshold the new threshold.
	 */
	public void setThreshold(float threshold) {
		threshold = Math.max(threshold, 0);
		threshold = Math.min(threshold, 1);
		this.threshold = threshold;
	}
	
	/**
	 * 
	 * Changes the threshold linearly. This value controls which texture will be drawn.
	 * 
	 * @param delta the threshold change.
	 */
	public void changeThreshold(float delta) {
		setThreshold(threshold + delta);
	}
	
	/**
	 * 
	 * Sets the mask texture.
	 * 
	 * @param mask the mask texture.
	 */
	public void setMask(Texture mask) {
		this.mask = mask;
	}
	
	@Override
	public void draw() {
		setupShader();
		
		//Tell the shader that this is a fader
		ShaderProgram.GUI.send1i("b_is_fader", 1);
		
		//Tell the shader that this is a fader
		ShaderProgram.GUI.send1f("f_value", threshold);
		
		//Send the texture id
		ShaderProgram.GUI.send1i("t_mask", 0);

		//Send the texture id
		ShaderProgram.GUI.send1i("t_sampler", 1);

		//Send the texture id
		ShaderProgram.GUI.send1i("t_sampler_on", 2);
		
		//Bind the mask to the value
		mask.bind(0);
		
		//Bind the sample textures to their values
		textures[0].bind(1);
		textures[1].bind(2);
		
		getMesh().draw();
	}
}
