package game.state;

import java.awt.Font;

import com.sun.xml.internal.bind.v2.model.util.ArrayInfoUtil;

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
import sk.gfx.gui.GUITextPosition;
import sk.sst.SST;
import sk.util.io.InputManager;
import sk.util.vector.Vector2f;
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
	
	private int highlighted = -1;

	public static final int BUTTON_SPACING = 60;

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
		highlighted = -1;
		state = MenuState.MAIN_MENU;
		
		buttons = generateButtons(0, 0, 0, -50, WIDTH, "Start", "Credits", "Settings", "Exit");
		
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
		highlighted = -1;
		state = MenuState.CREDITS;
		
		buttons = new Entity[1];
		buttons[0] = generateButton(0, -1, 0, - (2 * BUTTON_SPACING + HEIGHT / 2), WIDTH, "Back");
		
		buttons[0].get(GUIButton.class).setOnClick((sst) -> {
			enterMain();
		});
	}
	
	public void enterSettings() {
		highlighted = -1;
		state = MenuState.SETTINGS;
		
		buttons = generateButtons(-1, 0, WIDTH, (int) (1.5 * (BUTTON_SPACING)), WIDTH * 2, "Fullscreen", "SFX Volume", "Music Volume", "Back");
		
		String[] resolutions = 
			{
				"Resolutions",
				"700x525",
				"800x600",
				"848x480",
				"960x540",
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
			button.setOnClick((element) -> {
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
		
		buttons[0].get(GUIButton.class).setOnClick((sst) -> {
			if (!Window.isFullscreen()) {
				Window.enterBorderless();
			} else {
				Window.enterFloating(0, 0, Game.properties.width, Game.properties.height);
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
		
		if (InputManager.pressed("down")) {
			unhover();
			
			do {
				highlighted++;
				if (highlighted > buttons.length) {
					highlighted -= buttons.length;
				}
				highlighted %= buttons.length;
			} while (buttons[highlighted].get(GUIButton.class).getOnClick() == null);
			
			hover(highlighted);
		}
		
		if (InputManager.pressed("up")) {
			unhover();

			if (highlighted == -1) {
				highlighted = 1;
			}
			
			do {
				highlighted--;
				if (highlighted < 0) {
					highlighted += buttons.length;
				}
				highlighted %= buttons.length;
			} while (buttons[highlighted].get(GUIButton.class).getOnClick() == null);
			
			hover(highlighted);
		}
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
			buttons[id].get(GUIButton.class).click();
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
