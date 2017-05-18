package game.level.resources;

import game.level.Level;
import sk.gfx.Texture;
import sk.physics.Collision;

public class OneshotPressurePlate extends PressurePlate {
	
	public OneshotPressurePlate(Level level, int layer, float x, float y) {
		super(level, layer, x, y);
		renderer.setTexture(new Texture("res/texture/temp.png"));
	}

	@Override
	public void update(double delta) {
		if (down) return;
		for (Collision c : body.getCollisions()) {
			if (c.other.isDynamic()) {
				if (!down) {
					press();
					down = true;
				}
			}
		}
	}
}
