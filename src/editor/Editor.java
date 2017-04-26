package editor;

import java.util.Scanner;

import sk.audio.AudioManager;
import sk.game.Game;
import sk.game.GameProperties;
import sk.util.vector.Vector4f;

public class Editor {
	
	private static final Scanner scanner = new Scanner(System.in);
	
	public static final void main(String[] args) {

		System.out.println("Starting editor...");
		
		String path = read("Enter file name without extension");
		boolean hasData = ask("Should an already existing .level file be loaded? (y/n)");
		
		int cx = -1;
		int cy = -1;
		
		if(!hasData) {
			cx = Integer.parseInt(read("Enter width in chunks"));
			cy = Integer.parseInt(read("Enter height in chunks"));
		}
		
		GameProperties gp = new GameProperties();
		gp.clearColor = new Vector4f();
		gp.width = 800;
		gp.height = 600;
		gp.startState = new EditState(path, hasData, cx, cy);
		gp.resizable = false;
		gp.title = "Editor";
		gp.vSync = true;
		
		AudioManager.start();
		
		Game.start(gp);
		
		System.out.println("Editor successfully exited");
		
		scanner.close();
	}
	
	private static final String read(String disp) {
		System.out.print(disp + ": ");
		return scanner.nextLine();
	}
	
	private static final boolean ask(String disp) {
		System.out.print(disp);
		switch(scanner.nextLine()) {
		case "y":
		case "yes":
			return true;
		case "n":
		case "no":
		default:
			return false;
		}
	}
	
}