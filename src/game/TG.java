package game;

import game.state.*;

public class TG {
	
	// Gamestates
	public static final MainMenu GS_MAIN_MENU = new MainMenu();
	public static final ChapterMenu GS_CHAPTER_MENU = new ChapterMenu();
	public static final ParallaxState GS_PARALLAX_TEST = new ParallaxState();
	public static final Playing GS_PLAYING = new Playing();
}
