package player;

import game.level.Chunk;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Shape;
import sk.util.vector.Vector2f;

public class Player extends Entity {
	
	int height = 12;
	int width = 8;

	final float SCALE = 1f / Chunk.SIZE;

	boolean isBoy;
	
	Transform transform;
	Body body;
	Movement movement;
	Renderer renderer;
	
	public Player(boolean isBoy) {
		super();
		this.isBoy = isBoy;
		transform = new Transform();
		transform.scale.x = width * SCALE; 
		transform.scale.y = height * SCALE;
		transform.position.x = .1f;
		body = new Body(1, 0, 0, new Shape(new Vector2f[] {
				new Vector2f(-0.5f,  0.5f),
				new Vector2f( 0.5f,  0.5f),
				new Vector2f( 0.5f, -0.35f),
				new Vector2f( 0.0f, -0.5f),
				new Vector2f(-0.5f, -0.35f)
				}));
		movement = new Movement(isBoy);
		renderer = new Renderer(Mesh.QUAD).setTexture(new Texture("res/texture/wood.png"));
		
		add(transform);
		add(body);
		add(movement);
		add(renderer);
	}
}
