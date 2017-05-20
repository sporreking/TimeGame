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
		gp.fullscreen = true;
		gp.resizable = true;
		gp.title = "Time Game";
		gp.vSync = true;
		
		System.out.println("Starting game...");
		
		AudioManager.start();
		AudioManager.setGlobalLoopGain(1f);
		AudioManager.setGlobalTempGain(.25f);
		AudioManager.setRandomGainRange(0);
		
		Game.start(gp);
		
		GameShaders.destroyShaders();
		
		System.out.println("Game successfully exited");
	}
}
