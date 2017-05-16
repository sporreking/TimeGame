package game.level.resources;

import game.level.Chunk;
import game.level.Level;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Shape;
import sk.util.vector.Vector2f;

public class MoveableDoor extends Entity {
	
	Transform transform;
	Moveable movable;
	Renderer renderer;
	Body body;
	
	Level level;
	int layer;
	
	float width = 10;
	float height = 20;
	
	Connectable connectable = new PushDownConnectable();

	private class PushDownConnectable extends Connectable {
		
		@Override
		public void pressed() {
			getParent().get(Moveable.class).open();
		}
		
		@Override
		public void released() {
			getParent().get(Moveable.class).close();
		}
	}

	public MoveableDoor (Level level, int layer, float x, float y) {
		this.level = level;
		this.layer = layer;
		
		transform = new Transform();
		transform.position.x = x;
		transform.position.y = y;

		
		transform.scale.x = width  * Chunk.PIXEL_SCALE;
		transform.scale.y = height * Chunk.PIXEL_SCALE;
		
		movable = new Moveable(transform.position, transform.position, 0, 1);
		movable.setTarget(0, transform.position.clone());
		movable.setSpeed(0.2f);
		
		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new Texture("res/texture/temp.png"));
		
		body = new Body(Shape.QUAD);
		body.setDynamic(false);
		body.setTag("moving-platform");
		level.worlds[layer].addBody(body);
		
		add(transform);
		add(body);
		add(movable);
		add(renderer);
		add(connectable);
	}
	
	public Connectable getConnectable() {
		return connectable;
	}
	
	public void setA(Vector2f a) {
		movable.setTarget(0, a);
	}
	
	public void setB(Vector2f b) {
		movable.setTarget(1, b);
	}
	
	@Override
	public void draw() {
		if (level.currentSheet != layer) return;
		
		super.draw();
	}

	public void setSpeed(float speed) {
		get(Moveable.class).setSpeed(speed);
	}
}
