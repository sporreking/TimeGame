package game.level.resources;

import game.level.Chunk;
import game.level.Level;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Collision;
import sk.physics.Shape;

public class LockedDoor extends Entity{
	
	Level level;
	int layer;

	Transform transform;
	Body body;
	Renderer renderer;
	
	float width = 10;
	float height = 20;

	boolean locked = true;

	public LockedDoor (Level level, int layer, float x, float y) {
		this.level = level;
		this.layer = layer;
		
		transform = new Transform();
		transform.position.x = x;
		transform.position.y = y;

		transform.scale.x = width * Chunk.PIXEL_SCALE;
		transform.scale.y = height * Chunk.PIXEL_SCALE;
	
		body = new Body(Shape.QUAD);
		body.setTrigger(false);
		body.setDynamic(false);
		body.setTag("locked");

		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new Texture("res/texture/temp.png"));

		add(transform);
		add(body);
		level.worlds[layer].addBody(body);
		add(renderer);
	};

	@Override
	public void update(double delta) {
		if (!locked) return;

		for (Collision c : body.getCollisions()) {
			if (c.other.getTag().equals("key")) {
				locked = false;
				body.setTrigger(true);
				((Key) c.other.getParent()).useKey();
			}
		}

		super.update(delta);
	}

	@Override
	public void draw() {
		if (!locked) return;

		super.draw();
	}
}
