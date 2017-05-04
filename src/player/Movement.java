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
	
	private float bufferMaxTime = 0.1f;
	private float bufferTime = 0;
	private boolean jumping = false;
	
	private float gravity = -2.8f;
	private float groundAcc = 15f;
	private float airAcc = 2.0f;
	private float fallAcc = 0.75f;
	private float maxFallSpeed = 4.0f;
	private float maxSpeed = 0.5f;
	private float jumpVel = 1.0f;
	private float minGroundAngle = 0.3f;
	
	private float groundFriction = 0.4f;
	private float airFriction = 1.0f;
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
		
			grounded = body.dotCollisionNormals(UP) > minGroundAngle;
			Vector2f v = new Vector2f(0, gravity * TIME_STEP * (grounded ? 0 : 1));
			
			float acc = (grounded ? groundAcc : airAcc);
			
			if (Keyboard.down(keyLeft)) {
				v.x -= acc * TIME_STEP;
			}
			
			if (Keyboard.down(keyRight)) {
				v.x += acc * TIME_STEP;
			}
		
			if (Keyboard.down(keyDown)) {
				v.y -= fallAcc * TIME_STEP;
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
		
			float friction = grounded ? groundFriction : airFriction;
			Vector2f bodyVelocity = body.getVelocity(); 
			
			// Add in the velocity we normally have
			v = Vector2f.add(
					v, 
					new Vector2f(bodyVelocity.x * friction, 
						Math.min(bodyVelocity.y, 
								bodyVelocity.y * (Keyboard.down(keyJump) ? 1 : jumpFriction))
					), null);

			if (Math.abs(v.x) > maxSpeed) {
				v.x = Math.signum(v.x) * maxSpeed;
			}
			
			float fall = maxFallSpeed;
			if (Keyboard.down(keyDown)) {
				fall += fallAcc;
			}
			
			if (v.y < -fall) {
				v.y = -fall;
			}
			
			if (Keyboard.pressed(keyJump)) {
				jumping = true;
				bufferTime = 0;
			}
			
			if (jumping) {
				bufferTime += TIME_STEP;
				jumping = bufferTime < bufferMaxTime;
			}
			
			if (jumping && grounded) {
				jumping = false;
				v.y = jumpVel;
			} else {
				if (grounded) {
					for (Collision c : body.getCollisions()) {
						if (c.normal.dot(UP) > minGroundAngle) {
							Vector2f n = c.normal.clone();
							v.add(n.scale(-0.9f * n.dot(v)));
						}
					}
					v.y = 0;
				}
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
