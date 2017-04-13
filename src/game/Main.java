package game;
import game.shaders.GameShaders;
import sk.audio.AudioManager;
import sk.game.Game;
import sk.game.GameProperties;
import sk.gfx.Vertex2D;
import sk.physics.Body;
import sk.physics.Shape;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector4f;

public class Main {
	
	public static final void main(String[] args) {
		
		GameProperties gp = new GameProperties();
		gp.clearColor = new Vector4f();
		gp.width = 800;
		gp.height = 600;
		gp.startState = TG.GS_PARALLAX_TEST;
		gp.resizable = false;
		gp.title = "Time Game";
		gp.vSync = true;
		
		//Body b = new Body(new Shape(Vector2f(0.0f, 0.0f), Vector2f(0.0f, 0.0f)));
		
		System.out.println("Starting game...");
		
		AudioManager.start();
		
		Game.start(gp);
		
		GameShaders.destroyShaders();
		
		System.out.println("Game successfully exited");
	}

	private static Vertex2D Vector2f(float f, float g) {
		// TODO Auto-generated method stub
		return null;
	}
	
}