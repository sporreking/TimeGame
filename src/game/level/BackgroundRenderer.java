package game.level;

import sk.game.Window;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;

/**
 * 
 * A BackgroundRenderer draws object statically, but keeps the 
 * resolution in mind to make sure it looks the same on all 
 * resolutions and monitor sizes. 
 * 
 * @author Ed
 *
 */
public class BackgroundRenderer extends Renderer {
	
	/**
	 * Creates a new renderer with a basic quad mesh
	 * and loads the texture.
	 * 
	 * @param texture the path to the texture.
	 */
	public BackgroundRenderer(String texture) {
		this(new Texture(texture));
	}
	
	/**
	 * Creates a new renderer with a basic quad mesh
	 * and sets the texture.
	 * 
	 * @param texture the loaded texture.
	 */
	public BackgroundRenderer(Texture texture) {
		super(Mesh.QUAD);
		camera = Camera.GUI;
		setTexture(texture);
		updateTransform();
	}
	
	/**
	 * Calculates the scale of the background.
	 */
	public void updateTransform() {
		transform.scale.x = 4 / Window.getAspectRatio();
		transform.scale.y = 2;
	}
	
	@Override
	public void draw() {
		if (Window.resolutionHasChanged()) {
			updateTransform();
		}
		
		super.draw();
	}
	
	

}
