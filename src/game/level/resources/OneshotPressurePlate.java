package game.level.resources;

import game.AudioLib;
import game.level.Level;
import sk.audio.AudioManager;
import sk.gfx.Texture;
import sk.physics.Collision;

public class OneshotPressurePlate extends PressurePlate {
	
	public OneshotPressurePlate(Level level, int layer, float x, float y) {
		super(level, layer, x, y);
		renderer.setTexture(new Texture("res/texture/entity/oneshot_off.png"));
	}

	@Override
	public void update(double delta) {
		if (down) return;
		for (Collision c : body.getCollisions()) {
			if (c.other.isDynamic()) {
				if (!down) {
					press();
					renderer.setTexture(new Texture("res/texture/entity/oneshot_on.png"));
					level.shakeCamera(.1f, .02f);
					AudioManager.play(2f, .5f, transform.position.x, transform.position.y, 0, true, AudioLib.S_LAND);
					down = true;
				}
			}
		}
	}
}
