package game.level.player;

import java.awt.Font;

import game.TexLib;
import game.level.Level;
import sk.entity.Entity;
import sk.game.Time;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.gfx.gui.GUIElement;
import sk.gfx.gui.GUIFader;
import sk.gfx.gui.GUIText;
import sk.gfx.gui.GUITextPosition;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector4f;

public class Hud extends Entity {
	
	public static final Font SCORE_FONT = new Font(Font.SANS_SERIF, 0, 50);
	public static final Vector4f SCORE_COLOR = new Vector4f(1, 1, 1, 0.5f);
	public static final float GUI_SCALE = 1.0f;
	public static final float RESTART_TEXT_ALPHA = 0.5f;
	public static final float RESTART_TEXT_ALPHA_RANGE = 0.3f;
	public static final float BAR_SCALE = 6.0f;
	
	private static int score;
	private static float energy;
	
	private static float energyDecreaseRate = 0.01f;

	private float restartTextAlphaTarget = 0;
	private Vector4f restartTextColor = new Vector4f(1, 1, 1, 0);
	
	Entity energyBar, scoreLabel, restartLabel;
	GUIText scoreText;
	GUIText restartText;
	
	Level level;
	
	public GUIElement switchDisplay;
	
	public Hud(Level level) {
		this.level = level;
		score = 0;
		energy = 1.0f;
			
		GUIElement element = new GUIElement(1, 1, (int) (-50 * GUI_SCALE), (int) (-15 * GUI_SCALE), (int) (100 * GUI_SCALE), (int) (30 * GUI_SCALE));
		element.setTexture(Texture.DEFAULT);
		element.setHue(new Vector4f(0, 0, 0, 0));
		scoreText = new GUIText("score: " + score, 300, 100, 
				SCORE_FONT, SCORE_COLOR, 
				GUITextPosition.LEFT, 
				new Vector2f(0, -10));
		element.setText(scoreText);
		
		scoreLabel = new Entity();
		scoreLabel.add(new Transform());
		scoreLabel.add(element);

		energyBar = new Entity();
		energyBar.add(new Transform());
		energyBar.add(
				new GUIFader(1, 1, (int) ((- 24) * GUI_SCALE * BAR_SCALE), (int) (-6 * GUI_SCALE * BAR_SCALE), (int) (48 * GUI_SCALE * BAR_SCALE), (int) (6 * GUI_SCALE * BAR_SCALE), 
						T_MASK, 
						T_EMPTY, 
						T_FULL));
	
		restartLabel = new Entity();
		element = new GUIElement(0, 1, 0, -100, (int) (400 * GUI_SCALE), (int) (40 * GUI_SCALE));
		element.setHue(new Vector4f(0, 0, 0, 0));
		restartText = new GUIText("Press 'R' to restart", (int) (600 * GUI_SCALE), (int) (60 * GUI_SCALE), SCORE_FONT);
		restartText.setColor(restartTextColor);
		element.setText(restartText);
		
		restartLabel.add(element);
		
		switchDisplay = new GUIElement(1, 1, (int) (-54 * GUI_SCALE * BAR_SCALE), (int) (-6 * GUI_SCALE * BAR_SCALE), (int) (8 * GUI_SCALE * BAR_SCALE), (int) (8 * GUI_SCALE * BAR_SCALE));
		switchDisplay.setTexture(TexLib.T_P2_FACE);
	}
	
	public static void changeEnergy(float diff) {
		energy += diff;

		if (energy < 0) {
			energy = 0;
		} else if (energy > 1) {
			energy = 1;
		}
	}
	
	public static float getEnergy() {
		return energy;
	}

	public static void addScore(int increase) {
		score += increase;
	}
	
	@Override
	public void update(double delta) {
		scoreText.setText("score: " + score);
		scoreLabel.get(GUIElement.class).setText(scoreText);
		
		energy -= delta * energyDecreaseRate;
		if (energy < 0) {
			energy = 0;
		} else if (energy > 1) {
			energy = 1;
		}
		
		energyBar.get(GUIFader.class).setThreshold(energy);

		
		if (level.isPromptingRestart()) {
			restartTextAlphaTarget = 
					(float) (RESTART_TEXT_ALPHA + 
					(Math.pow(Math.sin(Time.getTime()), 2) * RESTART_TEXT_ALPHA_RANGE));
		}
	
		Vector4f color = restartText.getColor();
		float toTarget = restartTextAlphaTarget - color.getW();
		color.setW((float) (color.getW() + toTarget * delta));
		restartText.setColor(color);
	}
	
	@Override
	public void draw() {
//		scoreLabel.draw();
		if (restartText.getColor().w != 0) {
			restartLabel.draw();
		}
		energyBar.draw();
		switchDisplay.draw();
	}
	
	public static final Texture T_MASK = new Texture("res/texture/hud/bar_mask.png");
	public static final Texture T_FULL = new Texture("res/texture/hud/bar_full.png");
	public static final Texture T_EMPTY = new Texture("res/texture/hud/bar_empty.png");
	
}
