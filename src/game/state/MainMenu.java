package game.state;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import game.Main;
import game.TG;
import sk.audio.Audio;
import sk.audio.AudioManager;
import sk.entity.Entity;
import sk.entity.component.AABB;
import sk.game.Game;
import sk.game.Time;
import sk.game.Window;
import sk.gamestate.GameState;
import sk.gamestate.GameStateManager;
import sk.gfx.Camera;
import sk.gfx.Texture;
import sk.gfx.gui.GUIButton;
import sk.gfx.gui.GUIElement;
import sk.gfx.gui.GUIFader;
import sk.gfx.gui.GUIText;
import sk.gfx.gui.GUITextPosition;
import sk.sst.SST;
import sk.util.io.InputManager;
import sk.util.io.Mouse;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector4f;

public class MainMenu implements GameState {

	enum MenuState {
		MAIN_MENU,
		SETTINGS,
		CREDITS,
		CHAPTER,
	}
	
	static class KeyRepeateTimer {
		// The input we want to check for
		String key;
		
		final float START = 0.75f;
		final float END = 0.05f;
		final float TIME_TO_MAX = 1.0f;
		
		float heldDownFor = 0;
		float timer = 0;
		
		public KeyRepeateTimer(String key) {
			this.key = key;
		}
		
		public boolean step(double delta) {
			if (InputManager.down(key)) {
				if (heldDownFor == 0) {
					heldDownFor += delta;
					return true;
				} else {
					heldDownFor += delta;
					timer += delta;
					float lerp = (float) Math.min(Math.pow((heldDownFor / TIME_TO_MAX), 0.5), 1);
					float limit = (1 - lerp) * START + lerp * END;
					
					if (limit < timer) {
						timer -= limit;
						return true;
					}
				}
			} else {
				heldDownFor = 0;
				timer = 0;
			}
			
			return false;
		}
	}
	
	private Entity buttons[];
	private GUIElement logo;
	private Entity globalVolume;
	private Entity tempVolume;
	
	private GUIElement credits;
	
	private KeyRepeateTimer up, down, left, right;
	
	private MenuState state;
	
	private int highlighted = -1;
	
	private int display = 0;

	public static final String GENERATE_CREDITS() {
		String text =
			"A game by: \n\n"
			+ "Programming: \n"
			+ "   %s, %s,\n"
			+ "   %s\n"
			+ "\nGraphics: \n"
			+ "   %s\n"
			+ "\nSound: \n"
			+ "    %s, %s,\n"
			+ "    %s\n"
			+ "\n\n(Copyread, no rights reserved)";
		
		String ed = "Edvard Thörnros";
		String al = "Alfred Sporre";
		String killMe = "Gustav \"Gösta\" Andersson";
		String es = "Eric Sjöö";
		
		String[] programmers = shuffle(ed, al, es);
		
		String[] graphics = shuffle(killMe);
		
		String[] sound = shuffle(killMe, al, es);
		
		return String.format(text, programmers[0], programmers[1], programmers[2], graphics[0], sound[0], sound[1], sound[2]);
	}
	
	private static String[] shuffle(String... strings) {
		String[] shuffled = strings.clone();
		
		if (shuffled.length < 2) {
			return shuffled;
		}
		
		Random random = new Random((int) (Time.getTime() * 100));
		for (int i = 0; i < shuffled.length; i++) {
			int index = random.nextInt(shuffled.length - 1);
			String temp = shuffled[index];
			shuffled[index] = shuffled[i];
			shuffled[i] = temp;
		}
		
		return shuffled;
	}
	
	public static Audio TEST_SOUND;
	
	public static final float MAX_TEMP_GAIN = .1f;
	public static final float MAX_LOOP_GAIN = 1;
	
	public static final int BUTTON_SPACING = 60;

	public static final int TEXT_WIDTH  = 150;
	public static final int TEXT_HEIGHT = 50;

	public static final int WIDTH  = 150;
	public static final int HEIGHT = 50;

	public static final Font MENU_FONT = new Font(Font.SANS_SERIF, 0, 25);

	public static final Vector4f DEFAULT_TEXT_COLOR = new Vector4f(1, 1, 1, 1);
	public static final Vector4f HOVER_TEXT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1);

	public static final Vector4f DEFAULT_COLOR = new Vector4f(0, 0, 0, 1);
	public static final Vector4f HOVER_COLOR = new Vector4f(0.2f, 0.2f, 0.5f, 1);

	public static Texture LOGO;

	public Entity[] generateButtons(float anchorX, float anchorY, int offsetX, int offsetY, int width, String... names) {
		Entity[] buttons = new Entity[names.length];
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			buttons[i] = generateButton(anchorX, anchorY, offsetX, offsetY - BUTTON_SPACING * i, width, name);
		}
		
		return buttons;
	}
	
	public Entity generateButton(float anchorX, float anchorY, int offsetX, int offsetY, int width, String name) {
		GUIButton button = new GUIButton(anchorX, anchorY, offsetX, offsetY, width, HEIGHT);
		Entity e = new Entity();
		e.add(button);

		button.setTexture(Texture.DEFAULT);
		button.setText(new GUIText(name, width, TEXT_HEIGHT, MENU_FONT, DEFAULT_TEXT_COLOR));
		button.setHue(DEFAULT_COLOR);
		button.setOnHover((element) -> {
			unhover();
			element.setHue(HOVER_COLOR);
			element.getText().setColor(HOVER_TEXT_COLOR);
		});

		button.setOnUnhover((element) -> {
			element.setHue(DEFAULT_COLOR);
			element.getText().setColor(DEFAULT_TEXT_COLOR);
		});
		return e;
	}
	
	public void enterMain() {
		Main.dumpSave();
		
		if (highlighted != -1) {
			switch (state) {
			case CHAPTER:
				highlighted = 0;
				break;
			case CREDITS:
				highlighted = 2;
				break;
			case SETTINGS:
				highlighted = 1;
				break;
			default:
				break;
	
			}
		}
		
		state = MenuState.MAIN_MENU;
		
		buttons = generateButtons(0, 0, 0, -50, WIDTH, "Start", "Settings", "Credits", "Exit");
		
		((GUIButton) buttons[0].get(GUIButton.class)).setOnRelease((sst) -> {
			//GameStateManager.enterState(TG.GS_CHAPTER_MENU);
			enterChapter();
		});
		
		((GUIButton) buttons[1].get(GUIButton.class)).setOnRelease((sst) -> {
			enterSettings();
		});

		((GUIButton) buttons[2].get(GUIButton.class)).setOnRelease((sst) -> {
			enterCredits();
		});

		((GUIButton) buttons[3].get(GUIButton.class)).setOnRelease((sst) -> {
			Game.stop();
		});
		
		if (highlighted != -1) {
			hover(highlighted);
		}
	}
	
	public void enterCredits() {
		state = MenuState.CREDITS;
		
		
		credits.getText().setText(GENERATE_CREDITS());
		
		buttons = new Entity[1];
		buttons[0] = generateButton(0, -1, 0, BUTTON_SPACING, WIDTH, "Back");
		
		buttons[0].get(GUIButton.class).setOnRelease((sst) -> {
			enterMain();
		});
		
		if (highlighted != -1) {
			highlighted = 0;
			hover(highlighted);
		}
	}
	
	public void enterSettings() {
		state = MenuState.SETTINGS;
		
		buttons = generateButtons(-1, 0, WIDTH, (int) (1.5 * (BUTTON_SPACING)), WIDTH * 2, "Fullscreen", "Music Volume", "SFX Volume", "Back");
		
		String[] resolutions = 
			{
				"Resolutions",
				"800x600",
				"1280x720",
				"1920x1080"
			};
		
		Entity[] displaySettings = new Entity[resolutions.length];
		{
			displaySettings[0] = generateButton(1, 0, (int) (-WIDTH * 0.75),
					(int) (resolutions.length * BUTTON_SPACING / 2 - BUTTON_SPACING * 0.5), (int) (WIDTH * 1.5),
					resolutions[0]);

			GUIButton header = displaySettings[0].get(GUIButton.class);
			header.setOnHover((element) -> {});
			header.setOnUnhover((element) -> {});
		}
		
		for (int i = 1; i < resolutions.length; i++) {
			Entity item = generateButton(1, 0, 
					(int) (-WIDTH * 0.75), (int) (resolutions.length * BUTTON_SPACING / 2 - BUTTON_SPACING * (i + 0.5)), 
					(int) (WIDTH * 1.5), resolutions[i]);
			item.add(new SST());
			{	
				// So we only have to write one function for setting the screen resoluton
				String[] widthAndHeight = resolutions[i].split("x");
				int w = Integer.parseInt(widthAndHeight[0]);
				int h = Integer.parseInt(widthAndHeight[1]);
				
				item.get(SST.class).store("w", w);
				item.get(SST.class).store("h", h);
			}
			
			GUIButton button = item.get(GUIButton.class);
			button.getText().setPosition(GUITextPosition.TOP);
			button.setOnRelease((element) -> {
				int w = (int) element.getParent().get(SST.class).get("w");
				int h = (int) element.getParent().get(SST.class).get("h");
				
				Window.setSize(w, h);
				
			});
			
			displaySettings[i] = item;
		}
		
		{
			int a = buttons.length;
			int b = displaySettings.length;
			Entity[] target = new Entity[a + b];
			
			System.arraycopy(buttons, 	      0, target, 0, a);
			System.arraycopy(displaySettings, 0, target, a, b);
			
			buttons = target;
		}
		
		for (int i = 0; i < buttons.length; i++) {
			GUIButton b = buttons[i].get(GUIButton.class);
			b.getText().setOffset(new Vector2f(10, 0));
			b.getText().setPosition(GUITextPosition.LEFT);
		}
		
		buttons[0].get(GUIButton.class).setOnRelease((sst) -> {
			if (!Window.isFullscreen()) {
				Window.enterBorderless(display);
				display++;
				display %= Window.getNumberOfDisplays();
			} else {
				Window.enterFloating(0, 0, Game.properties.width, Game.properties.height);
			}
		});
		
		buttons[1].get(GUIButton.class).setOnRelease((sst) -> {
		});
		
		buttons[2].get(GUIButton.class).setOnRelease((sst) -> {
		});
		
		buttons[3].get(GUIButton.class).setOnRelease((sst) -> {
			enterMain();
		});
		
		if (highlighted != -1) {
			highlighted = 0;
			hover(highlighted);
		}
	}

	void enterChapter() {
		state = MenuState.CHAPTER;
		ArrayList<String> chapters = new ArrayList<>();

		String BASE_FOLDER = "res/level/";
		File base = new File(BASE_FOLDER);

		// Makesure these are directories.
		for (File f : base.listFiles((current, name) -> {
			return new File(current, name).isDirectory(); })) {
			chapters.add(f.getName());
		}

		// Hack in the back button, we can make the buttons from this list.
		chapters.add("Back");
		
		String[] captions = new String[chapters.size()];
		chapters.toArray(captions);
		
		buttons = generateButtons(0, 0, 0, (int) (BUTTON_SPACING * captions.length / 2.0f), 2 * WIDTH, captions);
		
		// We don't want to set the back button twice, that's a waste of CPU cycles
		for (int i = 0; i < buttons.length - 1; i++) {
			buttons[i].get(GUIButton.class).setOnRelease((element) -> {
				String caption = element.getText().getText();
				TG.GS_PLAYING.chapter = caption;
				GameStateManager.enterState(TG.GS_PLAYING);
			});
		}

		buttons[buttons.length - 1].get(GUIButton.class).setOnRelease((element) -> {
			enterMain();
		});
		
		if (highlighted != -1) {
			highlighted = 0;
			hover(highlighted);
		}
	}

	@Override
	public void init() {
		TEST_SOUND = new Audio("res/audio/select.wav");
		
		// Only load the logo once
		if (LOGO == null) {
			LOGO = new Texture(Game.properties.icon);
			logo = new GUIElement(0, 1, 0, -150, 300, 300);
			logo.setText(new GUIText("", 0, 0, MENU_FONT));
			logo.setTexture(LOGO);
		}
		
		int CREDITS_HEIGHT = 500;
		int CREDITS_WIDTH = 600;
		credits = new GUIElement(0, 1, 0, -CREDITS_HEIGHT / 2, CREDITS_WIDTH, CREDITS_HEIGHT);
		credits.setHue(new Vector4f(0, 0, 0, 0));
		credits.setText(new GUIText(GENERATE_CREDITS(), 
				CREDITS_WIDTH, CREDITS_HEIGHT, MENU_FONT, new Vector4f(1, 1, 1, 1), GUITextPosition.TOP_LEFT, new Vector2f(10, 0)));
		
		Texture mask = new Texture("res/texture/hud/bar_mask.png");
		Texture off   = new Texture("res/texture/hud/bar_full.png");
		Texture on  = new Texture("res/texture/hud/bar_empty.png");
		
		globalVolume = new Entity();
		globalVolume.add(new GUIFader(-1, 0, (int) (2.5 * WIDTH) + 10, (int) (.5 * (BUTTON_SPACING)), WIDTH, HEIGHT,
				mask, on, off));
		globalVolume.add(new AABB(WIDTH, HEIGHT, globalVolume.get(GUIFader.class).transform));
		
		tempVolume = new Entity();
		tempVolume.add(new GUIFader(-1, 0, (int) (2.5 * WIDTH) + 10, (int) -(.5 * (BUTTON_SPACING)), WIDTH, HEIGHT,
				mask, on, off));
		tempVolume.add(new AABB(WIDTH, HEIGHT, tempVolume.get(GUIFader.class).transform));
		
		up = new KeyRepeateTimer("up");
		down = new KeyRepeateTimer("down");
		left = new KeyRepeateTimer("left");
		right = new KeyRepeateTimer("right");
		enterMain();
	}

	float timer = 0;
	int frames = 0;
	
	@Override
	public void update(double delta) {
		
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].update(delta);
		}
		
		if (InputManager.pressed("exit")) {
			if (state != MenuState.MAIN_MENU) {
				enterMain();
			}
		}
		
		if (InputManager.pressed("select")) {
			click(highlighted);
		}
		
		if (down.step(delta)) {
			do {
				highlighted++;
				if (highlighted > buttons.length) {
					highlighted -= buttons.length;
				}
				highlighted %= buttons.length;
			} while (buttons[highlighted].get(GUIButton.class).getOnRelease() == null);
			
			hover(highlighted);
		}
		
		if (up.step(delta)) {
			if (highlighted == -1) {
				highlighted = 1;
			}
			
			do {
				highlighted--;
				if (highlighted < 0) {
					highlighted += buttons.length;
				}
				highlighted %= buttons.length;
			} while (buttons[highlighted].get(GUIButton.class).getOnRelease() == null);
			
			hover(highlighted);
		}
		
		if (state == MenuState.SETTINGS) {
			if (left.step(delta)) {
				// @Hardcode: Should really make this more general...
				// This assumes 4 elements on the left hand side
				if (highlighted == 1 || highlighted == 2) {
					// DUDE, WE'RE ON A FKING SLIDER!
					if (highlighted == 1) {
						float newGain = AudioManager.getGlobalLoopGain() - MAX_LOOP_GAIN / 20;
						newGain = Math.max(0, Math.min(MAX_LOOP_GAIN, newGain));
						AudioManager.setGlobalLoopGain(newGain);
					} else {
						float newGain = AudioManager.getGlobalTempGain() - MAX_TEMP_GAIN / 20;
						newGain = Math.max(0, Math.min(MAX_TEMP_GAIN, newGain));
						AudioManager.setGlobalTempGain(newGain);
						
						AudioManager.play(1, 1, true, TEST_SOUND);
					}
				} else {
					if (highlighted < 2) {
						highlighted = 5;
					} else if (highlighted == 3) {
						highlighted = 7;
					} else if (highlighted > 6) {
						highlighted = 3;
					} else {
						highlighted = 0;
					}
					
					hover(highlighted);
				}
			}
		}
		
		if (state == MenuState.SETTINGS) {
			if (right.step(delta)) {
				// @Hardcode: Should really make this more general...
				// This assumes 4 elements on the left hand side
				if (highlighted == 1 || highlighted == 2) {
					if (highlighted == 1) {
						float newGain = AudioManager.getGlobalLoopGain() + MAX_LOOP_GAIN / 20;
						newGain = Math.max(0, Math.min(MAX_LOOP_GAIN, newGain));
						AudioManager.setGlobalLoopGain(newGain);
					} else {
						float newGain = AudioManager.getGlobalTempGain() + MAX_TEMP_GAIN / 20;
						newGain = Math.max(0, Math.min(MAX_TEMP_GAIN, newGain));
						AudioManager.setGlobalTempGain(newGain);
						
						AudioManager.play(1, 1, true, TEST_SOUND);
					}
				} else {
					
					if (highlighted < 2) {
						highlighted = 5;
					} else if (highlighted == 3) {
						highlighted = 7;
					} else if (highlighted >= 6) {
						highlighted = 3;
					} else {
						highlighted = 0;
					}
					
					hover(highlighted);
				}
			}
			if (Mouse.down(0)) {
				Vector2f mousePos = Mouse.projectPosition(Camera.GUI.getProjection());
				AABB aabb = globalVolume.get(AABB.class);
				if (aabb.getMin().y < mousePos.y && mousePos.y < aabb.getMax().y) {
					float newGain = (mousePos.x - aabb.getMin().x);

					newGain /= aabb.getWidth();
					if (-0.1f < newGain && newGain < 1.1f) {
						newGain = Math.max(0, Math.min(newGain, 1));
						AudioManager.setGlobalLoopGain(newGain * MAX_LOOP_GAIN);
					}
				} else { 
					aabb = tempVolume.get(AABB.class);
					if (aabb.getMin().y < mousePos.y && mousePos.y < aabb.getMax().y) {
						float newGain = (mousePos.x - aabb.getMin().x);
						newGain /= aabb.getWidth();
						if (-0.1f < newGain && newGain < 1.1f) {
							newGain = Math.max(0, Math.min(newGain, 1));
							AudioManager.setGlobalTempGain(newGain * MAX_TEMP_GAIN);

							if (Mouse.pressed(0))
								AudioManager.play(1, 1, true, TEST_SOUND);
						}
					}
				}
			}
		}
		
		// @SaveTheFrames: This doesn't need to go here... but it is here now.
		globalVolume.get(GUIFader.class).setThreshold(AudioManager.getGlobalLoopGain() / MAX_LOOP_GAIN);
		tempVolume  .get(GUIFader.class).setThreshold(AudioManager.getGlobalTempGain() / MAX_TEMP_GAIN);
	}
	
	private void unhover() {
		for (Entity e : buttons) {
			e.get(GUIButton.class).unhover();
		}
	}
	
	private void hover(int id) {
		if (0 <= id && id < buttons.length) {
			buttons[id].get(GUIButton.class).hover();
		}
	}
	
	private void click(int id) {
		if (0 <= id && id < buttons.length) {
			buttons[id].get(GUIButton.class).release();
		}
	}
	
	@Override
	public void draw() {
		switch (state) {
		case MAIN_MENU:
			logo.draw();
			break;
		case CREDITS:
			credits.draw();
			break;
		case SETTINGS:
			globalVolume.draw();
			tempVolume.draw();
			break;
		case CHAPTER:
			break;
		default:
			break;
		}

		for (int i = 0; i < buttons.length; i++) {
			buttons[i].draw();
		}
	}

	@Override
	public void exit() {
		for (Entity e : buttons) {
			e.destroy();
		}
	}
}
