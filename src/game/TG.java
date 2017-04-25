package game;

import game.state.*;
import sk.physics.World;
import sk.util.vector.Vector2f;

public class TG {
	
	// Gamestates
	public static final MainMenu GS_MAIN_MENU = new MainMenu();
	public static final Playing GS_PLAYING = new Playing();
}