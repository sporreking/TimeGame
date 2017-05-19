package game.state;

import java.awt.Font;

import com.sun.org.apache.bcel.internal.generic.GOTO;

import game.TG;
import sk.entity.Entity;
import sk.game.Game;
import sk.game.Window;
import sk.gamestate.GameState;
import sk.gamestate.GameStateManager;
import sk.gfx.Texture;
import sk.gfx.gui.GUIButton;
import sk.gfx.gui.GUIElement;
import sk.gfx.gui.GUIText;
import sk.sst.SST;

import sk.util.vector.Vector4f;

public class MainMenu implements GameState {

	enum MenuState {
		MAIN_MENU,
		SETTINGS,
		CREDITS,
	}
	
	private Entity buttons[];
	private GUIElement logo;
	
	private MenuState state;

	public static final int BUTTON_SPACING = -60;

	public static final int TEXT_WIDTH  = 150;
	public static final int TEXT_HEIGHT = 50;

	public static final int WIDTH  = 150;
	public static final int HEIGHT = 50;

	public static final Font MENU_FONT = new Font(Font.SANS_SERIF, 0, 25);

	public static final Vector4f DEFAULT_TEXT_COLOR = new Vector4f(0, 1, 0, 1);
	public static final Vector4f HOVER_TEXT_COLOR = new Vector4f(0, 1, 1, 1);

	public static final Vector4f DEFAULT_COLOR = new Vector4f(0, 0, 1, 1);
	public static final Vector4f HOVER_COLOR = new Vector4f(1, 0, 0, 1);

	public static Texture LOGO;

	public void generateButtons(int offsetX, int offsetY, String... names) {
		if (names.length == 0) {
			buttons = null;
			return;
		}
		
		buttons = new Entity[names.length];
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			buttons[i] = generateButton(offsetX, offsetY + BUTTON_SPACING * i, name, i);
		}
	}
	
	public Entity generateButton(int offsetX, int offsetY, String name, int index) {
		GUIButton button = new GUIButton(0, 0, offsetX, offsetY, WIDTH, HEIGHT);
		Entity e = new Entity();
		e.add(button);
		e.get(SST.class).store("index", index);

		button.setTexture(Texture.DEFAULT);
		button.setText(new GUIText(name, TEXT_WIDTH, TEXT_HEIGHT, MENU_FONT, DEFAULT_TEXT_COLOR));
		button.setHue(DEFAULT_COLOR);
		button.setOnHover((sst) -> {
			int i = (int) sst.get("index");
			buttons[i].get(GUIButton.class).setHue(HOVER_COLOR);
			buttons[i].get(GUIButton.class).getText().setColor(HOVER_TEXT_COLOR);
		});

		button.setOnUnhover((sst) -> {
			int i = (int) sst.get("index");
			buttons[i].get(GUIButton.class).setHue(DEFAULT_COLOR);
			buttons[i].get(GUIButton.class).getText().setColor(DEFAULT_TEXT_COLOR);
		});
		return e;
	}
	
	public void enterMain() {
		state = MenuState.MAIN_MENU;
		
		generateButtons(0, -50, "Start", "Credits", "Settings", "Exit");
		
		((GUIButton) buttons[0].get(GUIButton.class)).setOnClick((sst) -> {
			GameStateManager.enterState(TG.GS_CHAPTER_MENU);
		});

		((GUIButton) buttons[1].get(GUIButton.class)).setOnClick((sst) -> {
			enterCredits();
		});

		((GUIButton) buttons[2].get(GUIButton.class)).setOnClick((sst) -> {
			enterSettings();
		});

		((GUIButton) buttons[3].get(GUIButton.class)).setOnClick((sst) -> {
			Game.stop();
		});
	}
	
	public void enterCredits() {
		state = MenuState.CREDITS;
		
		buttons = new Entity[1];
		buttons[0] = generateButton(0, - (2 *  BUTTON_SPACING + HEIGHT / 2), "Back", 0);
		buttons[0].get(GUIButton.class).setAnchor(0, -1);
		buttons[0].get(GUIButton.class).setOnClick((sst) -> {
			enterMain();
		});
	}
	
	public void enterSettings() {
		state = MenuState.SETTINGS;
		
		generateButtons(2 * BUTTON_SPACING, 0, "Toggle Fullscreen", "Sound Effects Volume", "Music Volume", "Back");
		
		buttons[0].get(GUIButton.class).setOnClick((sst) -> {
			System.out.println("I am fullscreen: " + Window.isFullscreen());
			if (!Window.isFullscreen()) {
				Window.enterBorderless();
			} else {
				Window.enterFloating(0, 0, Window.getWidth(), Window.getHeight());
			}
		});
		
		buttons[1].get(GUIButton.class).setOnClick((sst) -> {
			System.out.println("WOOOO SOUND EFFECTS!");
		});
		
		buttons[2].get(GUIButton.class).setOnClick((sst) -> {
			System.out.println("WOOOO MUSIC!");
		});
		
		buttons[3].get(GUIButton.class).setOnClick((sst) -> {
			enterMain();
		});
	}

	@Override
	public void init() {
		// Only load the logo once
		LOGO = new Texture("res/texture/temp.png");
		logo = new GUIElement(0, 1, 0, -150, 300, 300);
		logo.setText(new GUIText("", 0, 0, MENU_FONT));
		logo.setTexture(LOGO);
		
		enterMain();
	}

	@Override
	public void update(double delta) {
		for (int x = 0; x < buttons.length; x++) {
			buttons[x].update(delta);
		}
	}

	@Override
	public void draw() {
		switch (state) {
		case MAIN_MENU:
			logo.draw();
			break;
		case CREDITS:
			break;
		case SETTINGS:
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