package player;

import game.level.Chunk;
import game.level.Level;
import sk.entity.Entity;
import sk.gfx.Animation;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.gfx.Vertex2D;
import sk.physics.Body;
import sk.physics.Shape;
import sk.util.vector.Vector2f;

public class Player extends Entity {
	
	int height = 12;
	int width = 8;

	final float SCALE = 1f / Chunk.SIZE;

	boolean isBoy;
	boolean alive = true;
	
	Transform transform;
	Body body;
	Movement movement;
	Renderer renderer;
	AnimationHandler ah;
	
	protected boolean running = false;
	protected boolean grounded = false;
	protected int dir = -1;
	
	public boolean enabled = true;
	
	public Player(boolean isBoy) {
		super();
		this.isBoy = isBoy;
		transform = new Transform();
		transform.scale.x = width * SCALE; 
		transform.scale.y = height * SCALE;
		body = new Body(1, 0, 0, new Shape(new Vector2f[] {
				new Vector2f(-0.5f,  0.5f),
				new Vector2f( 0.5f,  0.5f),
				new Vector2f( 0.5f, -0.35f),
				new Vector2f( 0.0f, -0.5f),
				new Vector2f(-0.5f, -0.35f)
				})).setTag(isBoy ? "p1" : "p2");
		
		body.setOnlyOverlap(true);
		
		movement = new Movement(isBoy);
		renderer = new Renderer(new Mesh(new Vertex2D[] {
				new Vertex2D(-1f, .4f / .4f, 0, 0),
				new Vertex2D(1f, .4f / .4f, 1, 0),
				new Vertex2D(1f, -.2f / .4f, 1, 1),
				new Vertex2D(-1f, -.2f / .4f, 0, 1)
		}, 0, 1, 3, 3, 1, 2));
		
		ah = new AnimationHandler(this);
		
		add(transform);
		add(1, body);
		add(-1, movement);
		add(renderer);
		add(ah);
	}
	
	public void kill() {
		body.setTrigger(true);
		alive = false;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	@Override
	public void update(double delta) {
		if (!enabled) {
			return;
		}

		if (!alive) {
			return;
		}

		if(body.hasDeepCollision(1f / 128 * 5)) {
			kill();
			return;
		}
		
		if (body.isCollidingWithTag("death")) {
			kill();
			return;
		}
		
	
		super.update(delta);
		
		if (ah.animationToAdd != null) {
			add(ah.animationToAdd);
			ah.animationToAdd.setOffset(0);
			ah.animationToAdd = null;
		}
	}
	
	@Override
	public void draw() {
		if (alive && enabled) {
			super.draw();
		}
	}
}
