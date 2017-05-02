package player;

import sk.physics.Body;
import sk.physics.Collision;
import sk.util.io.Keyboard;
import sk.util.vector.Vector2f;

import org.lwjgl.glfw.GLFW;

import game.level.Level;
import sk.entity.Component;

public class Movement extends Component {
	
	private static final Vector2f UP = new Vector2f(0, 1);
	
	private Body body;
	
	// Should be set to the same as the physics engine
	private final float TIME_STEP = 1.0f / 60.0f;
	private float timer = 0;
	
	private float acceleration = 15f;
	private float maxSpeed = 0.5f;
	private float maxFallSpeed = 4.0f;
	private float jump = 1f;
	private float fallSpeedup = 0.75f;
	private float groundThreshold = 0.3f;
	
	private float groundFriction = 0.4f;
	private float airFriction = 0.9f;
	private float jumpFriction = 0.75f;
	
	private boolean isBoy;
	
	private int keyLeft;
	private int keyRight;
	private int keyJump;
	private int keyDown;
	private int keySwitch;
	
	private Level level;
	private Player player;
	
	public Movement() {
		this(false);
	}
	
	public Movement(boolean isBoy) {
		this.setBoy(isBoy);

		// Keybindings
		keyLeft 	= isBoy ? GLFW.GLFW_KEY_A : GLFW.GLFW_KEY_LEFT;
		keyRight 	= isBoy ? GLFW.GLFW_KEY_D : GLFW.GLFW_KEY_RIGHT;
		keyJump 	= isBoy ? GLFW.GLFW_KEY_W : GLFW.GLFW_KEY_UP;
		keySwitch 	= isBoy ? GLFW.GLFW_KEY_E : GLFW.GLFW_KEY_PERIOD;
		keyDown		= isBoy ? GLFW.GLFW_KEY_S : GLFW.GLFW_KEY_DOWN;
	}
	
	@Override
	public void init() {
		body = getParent().get(Body.class);
		player = (Player) getParent();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Component>[] requirements() { 
		return (Class<? extends Component>[]) new Class<?>[] {
			Body.class
		}; 
	}
	
	@Override
	public void update(double delta) {
		timer += delta;
		while (timer > TIME_STEP) {
			timer -= TIME_STEP;
			Vector2f v = new Vector2f();
		
			player.grounded = body.dotCollisionNormals(UP) > groundThreshold;
			
			if (Keyboard.down(keyLeft)) {
				v.x -= acceleration * TIME_STEP;
			}
			
			if (Keyboard.down(keyRight)) {
				v.x += acceleration * TIME_STEP;
			}
		
			if (Keyboard.down(keyDown)) {
				v.y -= fallSpeedup * TIME_STEP;
			}
			
			if (Keyboard.pressed(keySwitch)) {
				level.switchTime();
			}
			
			if(Math.abs(v.x) > 0) {
				player.running = true;
				player.dir = (int) Math.signum(v.x);
			} else {
				player.running = false;
			}
			
			if(!player.grounded) {
				player.running = false;
			}
		
			float friction = player.grounded ? groundFriction : airFriction;
			float temp = (Keyboard.down(keyJump) ? 1 : jumpFriction);
			Vector2f bodyVelocity = body.getVelocity(); 
			
			// Add in the velocity we normally have
			v = Vector2f.add(
					v, 
					new Vector2f(bodyVelocity.x * friction, 
						Math.min(bodyVelocity.y, bodyVelocity.y * temp)
					), null);
		
			if (Math.abs(v.x) > maxSpeed) {
				v.x = Math.signum(v.x) * maxSpeed;
			}
			
			temp = maxFallSpeed;
			if (Keyboard.down(keyDown)) {
				temp += fallSpeedup;
			}
			
			if (v.y < -temp) {
				v.y = -maxFallSpeed;
			}
		
			if (player.grounded) {
				for (Collision c : body.getCollisions()) {
					if (c.normal.dot(UP) > groundThreshold) {
						
					}
				}
			}
			
			if (Keyboard.pressed(keyJump) && player.grounded) {
				v.y = jump;
			}
			
			
			body.setVelocity(v);
		}
	}

	public boolean isBoy() {
		return isBoy;
	}

	public void setBoy(boolean isBoy) {
		this.isBoy = isBoy;
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}
	
	public Level getLevel() {
		return level;
	}
}
