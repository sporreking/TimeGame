package game.level.resources;

import game.level.Chunk;
import game.level.Level;
import game.level.player.Player;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Collision;
import sk.physics.Shape;

public class Exit extends Entity {
	
	Transform transform;
	Body body;
	Renderer renderer;
	
	Level level;
	int layer;
	
	float width = 10;
	float height = 10;

	public Exit(Level level, int layer, float x, float y) {
		this.level = level;
		this.layer = layer;
		
		transform = new Transform();
		transform.position.x = x;
		transform.position.y = y;
		transform.scale.x = width  * Chunk.PIXEL_SCALE;
		transform.scale.y = height * Chunk.PIXEL_SCALE;

		body = new Body(Shape.QUAD);
		body.setTrigger(true);
		body.setDynamic(false);
		body.setTag("exit");
		level.worlds[layer].addBody(body);
		
		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new Texture("res/texture/temp.png"));

		add(transform);
		add(body);
		add(renderer);
	}

	@Override
	public void update(double delta) {
		if (level.currentSheet != layer) return;
		boolean p1 = false;
		boolean p2 = false;
		for (Collision c : body.getCollisions()) {
			if (c.other.getTag().equals("p1")) {
				if (((Player) c.other.getParent()).isGrounded()) {
					p1 = true;
				}
			}
			if (c.other.getTag().equals("p2")) {
				if (((Player) c.other.getParent()).isGrounded()) {
					p2 = true;
				}
			}
		}
		
		if (p1 && p2) {
			level.exit();
		}

		super.update(delta);
	}
	
	@Override
	public void draw() {
		if (level.currentSheet != layer) return;
		
		super.draw();
	}
}
