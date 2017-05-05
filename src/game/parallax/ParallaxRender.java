package game.parallax;

import game.shaders.GameShaders;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.util.vector.Vector2f;

/**
 * A hacky parallax effect.
 * 
 * @author ed
 *
 */
public class ParallaxRender extends Renderer {

	private float parallax;
	private boolean invertY;
	
	public ParallaxRender(Mesh mesh, float distance, boolean invertY) {
		super(mesh);
		setDistance(distance);
		this.invertY = invertY;
	}
	
	public void setDistance(float distance) {
		parallax = (float) Math.pow(2, distance);
	}
	
	@Override
	public void draw() {
		//Select shader program
		GameShaders.PARALLAX_SHADER.use();
		
		//Send projection matrix
		GameShaders.PARALLAX_SHADER.sendM4("projection", camera.getProjection());
		
		//Send view matrix
		GameShaders.PARALLAX_SHADER.sendM4("view", camera.getMatrix());
		
		//Send model matrix
		GameShaders.PARALLAX_SHADER.sendM4("model", transform.getMatrix());
		
		GameShaders.PARALLAX_SHADER.send1i("t_sampler", 0);

		Vector2f p = camera.position.clone().scale(-parallax);
		GameShaders.PARALLAX_SHADER.send2f("parallax", p.x, p.y * (invertY ? -1 : 1));
		
		super.getTexture().bind(0);
		
		super.getMesh().draw();		
	}
}
