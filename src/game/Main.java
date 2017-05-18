package game;
import game.shaders.GameShaders;
import sk.audio.AudioManager;
import sk.game.Game;
import sk.game.GameProperties;
import sk.util.vector.Vector4f;

public class Main {
	
	public static final void main(String[] args) {
		
		GameProperties gp = new GameProperties();
		gp.clearColor = new Vector4f();
		gp.width = 800;
		gp.height = 600;
		gp.startState = TG.GS_MAIN_MENU;
		gp.fullscreen = false;
		gp.resizable = true;
		gp.title = "Time Game";
		gp.vSync = true;
		gp.inputMapPath = "res/input.map";
		
		System.out.println("Starting game...");
		
		AudioManager.start();
		
		Game.start(gp);
		
		GameShaders.destroyShaders();
		
		System.out.println("Game successfully exited");
	}
}
