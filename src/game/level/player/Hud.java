package game.level.player;

import java.awt.Font;

import sk.entity.Entity;
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
	public static float GUI_SCALE = 1.0f;
	
	static private int score;
	static private float energy;
	
	Entity energyBar, scoreLabel;
	GUIText scoreText;
	
	
	public Hud() {
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
				new GUIFader(1, 1, (int) ((-60 - 100) * GUI_SCALE), (int) (-15 * GUI_SCALE), (int) (100 * GUI_SCALE), (int) (30 * GUI_SCALE), 
						new Texture("res/texture/mask.png"), 
						new Texture("res/texture/on.png"), 
						new Texture("res/texture/off.png")));
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
		score += delta * 100;
		scoreText.setText("score: " + score);
		scoreLabel.get(GUIElement.class).setText(scoreText);
		
		energy -= delta * 0.1f;
		if (energy < 0) {
			energy = 0;
		} else if (energy > 1) {
			energy = 1;
		}
		
		energyBar.get(GUIFader.class).setThreshold(energy);
	}
	
	@Override
	public void draw() {
		scoreLabel.draw();
		energyBar.draw();
	}

}
