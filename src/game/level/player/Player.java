package game.level.player;

import game.level.Chunk;
import sk.debug.Debug;
import sk.entity.Entity;
import sk.gfx.Animation;
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
	public boolean alive = true;
	public boolean shouldDie = false;
	
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
				new Vector2f( 0.2f, -0.5f),
				new Vector2f(-0.2f, -0.5f),
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
		if(body.isTrigger())
			return;
		
		body.setTrigger(true);
		
		body.setVelocity(new Vector2f());
		
		remove(Animation.class);
		ah.animationToAdd = isBoy ? PlayerAnimation.dude1_death : PlayerAnimation.dude2_death;
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
		
		if(shouldDie) {
			
			if((isBoy ? PlayerAnimation.dude1_death :
				PlayerAnimation.dude2_death).getCurrentFrame() == 4) {
				alive = false;
			}
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
		
		checkAnimation();
	}
	
	private void checkAnimation() {
		if (ah.animationToAdd != null) {
			add(ah.animationToAdd);
			ah.animationToAdd.setOffset(0);
			shouldDie = ah.animationToAdd == PlayerAnimation.dude1_death ||
					ah.animationToAdd == PlayerAnimation.dude2_death;
			ah.animationToAdd = null;
		}
	}
	
	@Override
	public void draw() {
		if (alive && enabled) {
			super.draw();
		}
		
		Debug.draw();
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

	public void setDir(int d) {
		dir = d;
	}
}
