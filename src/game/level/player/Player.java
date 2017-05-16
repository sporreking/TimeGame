package game.level.player;

import game.TG;
import game.level.Chunk;
import game.level.resources.Launchable;
import sk.debug.Debug;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.gfx.Vertex2D;
import sk.physics.Body;
import sk.physics.Collision;
import sk.physics.Shape;
import sk.physics.TriggerBody;
import sk.util.vector.Vector2f;

public class Player extends Entity {
	
	static float TIME_STEP = 1 / 60.0f;
	float timer = 0;
	
	int height = 12;
	int width = 8;

	final float SCALE = 1f / Chunk.SIZE;

	boolean isBoy;
	boolean alive = true;
	
	public Transform transform;
	public Body body;
	public TriggerBody pickupTrigger;
	public PlayerLogic playerLogic;
	public Renderer renderer;
	public PlayerAnimation ah;
	
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
		
		pickupTrigger = new TriggerBody("reach", Shape.GEN_QUAD(0.75f));
		pickupTrigger.setTrigger(true);
		
		playerLogic = new PlayerLogic(isBoy);
		renderer = new Renderer(new Mesh(new Vertex2D[] {
				new Vertex2D(-1f, .4f / .4f, 0, 0),
				new Vertex2D(1f, .4f / .4f, 1, 0),
				new Vertex2D(1f, -.2f / .4f, 1, 1),
				new Vertex2D(-1f, -.2f / .4f, 0, 1)
		}, 0, 1, 3, 3, 1, 2));
		
		ah = new PlayerAnimation(this);
		
		add(transform);
		add(body);
		add(pickupTrigger);
		add(playerLogic);
		add(renderer);
		add(ah);
	}
	
	public void kill() {
		body.setTrigger(true);
		alive = false;
		
		TG.GS_PLAYING.playLevel();
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

		if (!playerLogic.isHeld() && !body.isTrigger()) {
			for (Collision c : body.getCollisions()) {
				if (c.other.isTrigger()) continue;
				if (c.collisionDepth > 1f / 128 * 5) {
					kill();
					return;
				}
			}
		}
		
		if (body.isCollidingWithTags("death")) {
			kill();
			return;
		}
		
		timer += delta;
		while (timer > TIME_STEP) {
			timer -= TIME_STEP;
			super.update(TIME_STEP);
		}
		
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
	
	public Player setAlive(boolean alive) {
		this.alive = alive;
		
		return this;
	}

	public void switchTime() {
		if (!playerLogic.launchableIsPlayer())
			playerLogic.tryThrow();
	}

	public boolean isGrounded() {
		return grounded;
	}
	
	public int getDir() {
		return dir;
	}
}
