package game.level.resources;

import java.util.ArrayList;

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
import sk.physics.TriggerBody;

public class PressurePlate extends Entity{

	Transform transform;
	Body body;
	Renderer renderer;

	Level level;
	int layer;

	boolean down = false;
	
	ArrayList<Connectable> connections = new ArrayList<Connectable>();

	float size = 5;
	
	public PressurePlate(Level level, int layer, float x, float y) {
		this.level = level;
		this.layer = layer;
		
		transform = new Transform();
		transform.position.x = x;
		transform.position.y = y;
		
		transform.scale.x = size * Chunk.PIXEL_SCALE;
		transform.scale.y = size * Chunk.PIXEL_SCALE;
		
		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new Texture("res/texture/temp.png"));

		body = new TriggerBody("switch", Shape.QUAD);
		level.worlds[layer].addBody(body);
		
		add(transform);
		add(body);
		add(renderer);
	}

	public void connect(Entity e) {
		if (e.has(Connectable.class)) {
			connections.add(e.get(Connectable.class));
		}
	}
	
	public void disconnect(Entity e) {
		if (e.has(Connectable.class)) {
			connections.remove(e.get(Connectable.class));
		}
	}
	
	public void connect(Connectable c) {
		connections.add(c);
	}

	public void disconnect(Connectable c) {
		connections.remove(c);
	}

	public void release() {
		for (Connectable c : connections) {
			c.released();
		}
	}
	
	public void press() {
		for (Connectable c : connections) {
			c.pressed();
		}
	}
	
	@Override
	public void update(double delta) {
		super.update(delta);
		
		boolean active = false;
		for (Collision c : body.getCollisions()) {
			if (c.other.isDynamic()) {
				active = true;
				if (!down) {
					press();
					down = true;
				}
				break;
			}
		}
		if (!active && down) {
			release();
			down = false;
		}
	}
	
	@Override
	public void draw() {
		if (level.currentSheet != layer) return;
		
		super.draw();
	}
}
