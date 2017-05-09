package game.level;

import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;

public class Chunk extends Entity {
	
	public static final int SIZE = 128;
	public static final float SCALE = 1f;
	public static final float PIXEL_SCALE = (float) ((float) SCALE / (float) SIZE);
	
	public Chunk(int x, int y, Texture texture) {
		Transform transform = new Transform();
		transform.position.set(x * SCALE, y * SCALE);
		transform.scale.x = SCALE;
		transform.scale.y = SCALE;
		
		add(0, transform);
		add(0, new Renderer(Mesh.QUAD).setTexture(texture));
	}
	
}