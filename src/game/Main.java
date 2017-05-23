package game;
import java.io.IOException;

import game.shaders.GameShaders;
import sk.audio.AudioManager;
import sk.game.Game;
import sk.game.GameProperties;
import sk.game.Window;
import sk.sst.SST;
import sk.util.vector.Vector4f;

public class Main {
	
	private static String saveLocation = "res/settings";
	public static final void main(String[] args) {
		
		GameProperties gp = new GameProperties();
		gp.clearColor = new Vector4f();
		gp.width = 800;
		gp.height = 600;
		gp.startState = TG.GS_MAIN_MENU;
		gp.fullscreen = false;
		gp.resizable = false;
		gp.title = "Time Game";
		gp.vSync = true;
		gp.inputMapPath = "res/input.map";
		gp.icon = "res/texture/logo.png";
		
		
		// Try to load in the settings
		try {
			SST save = new SST();
			save.load(saveLocation);
			gp.width = (int) save.get("w");
			gp.height = (int) save.get("h");
			gp.fullscreen = (int) save.get("fs") == 1;
			gp.display = (int) save.get("display");
			gp.globalLoopGain = (float) save.get("glg");
			gp.globalTempGain = (float) save.get("gtg");
		} catch (Exception e) {}
		
		System.out.println("Starting game...");
		
		AudioManager.start();
		
		Game.start(gp);
		
		GameShaders.destroyShaders();

		dumpSave();
		
		System.out.println("Game successfully exited");
	}
	
	public static final void dumpSave() {
		// Save out the settings
		SST save = new SST();
		save.store("w", Window.getWidth());
		save.store("h", Window.getHeight());
		save.store("fs", Window.isFullscreen() ? 1 : 0);
		save.store("display", Window.getCurrentDisplay());
		save.store("glg", AudioManager.getGlobalLoopGain());
		save.store("gtg", AudioManager.getGlobalTempGain());

		try {
			save.dump(saveLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
