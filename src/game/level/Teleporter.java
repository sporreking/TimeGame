package game.level;

import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Collision;
import sk.physics.Shape;
import sk.physics.World;
import sk.util.vector.Vector2f;

public class Teleporter extends Entity {
	
	Transform transform;
	Body body;
	Renderer renderer;
	
	boolean clearVelocity = true;
	
	Level level;
	
	Vector2f target;
	
	public Teleporter(Level level, Vector2f target) {
		this(level, target, new Vector2f());
	}
	
	public Teleporter(Level level, Vector2f target, Vector2f position) {
		this.target = target;
		this.level = level;
		
		transform = new Transform();
		transform.position = position.clone();
		transform.scale.x = 20 * Chunk.PIXEL_SCALE;
		transform.scale.y = 20 * Chunk.PIXEL_SCALE;
		add(transform);
		
		body = new Body(1, Shape.GEN_QUAD(0.3f));
		body.setDynamic(false);
		body.setTrigger(true);
		body.setTag("teleporter");
		body.setLayer((short) (level.P1_LAYER | level.P2_LAYER));
		add(body);
		level.worlds[0].addBody(body);
		
		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(Texture.DEFAULT);
		add(renderer);
	}
	
	@Override
	public void update(double delta) {
		Collision c;
		for (int i = 0; i < 2; i++) {
			switch (i) {
			case 0:	
				c = body.getCollisionWithTag("p1");
				break;
			default:
				c = body.getCollisionWithTag("p2");
				break;
			}
			if (c != null) {
				c.other.getParent().get(Transform.class).position.set(target.x, target.y);
				if (clearVelocity) {
					c.other.setVelocity(new Vector2f());
				}
			}
		}	
		
		super.update(delta);
	}
	
	public void draw() {
		super.draw();
	}
}
