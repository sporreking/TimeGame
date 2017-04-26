package game.shaders;

import sk.gfx.ShaderProgram;

public class GameShaders extends ShaderProgram {
	public static final ShaderProgram PARALLAX_SHADER;

	static {
		PARALLAX_SHADER = new ShaderProgram("res/shader/parallax.vert", "res/shader/parallax.frag");
	}
	
	public static void destroyShaders() {
		PARALLAX_SHADER.destroy();
	}
}
