package player;

import sk.physics.Body;
import sk.util.io.Keyboard;
import sk.util.vector.Vector2f;

import org.lwjgl.glfw.GLFW;

import sk.entity.Component;

public class Movement extends Component {
	
	private Body body;
	
	// Should be set to the same as the physics engine
	private final float TIME_STEP = 1.0f / 60.0f;
	
	private float acceleration = 20f;
	private float maxSpeed = 0.5f;
	private float maxFallSpeed = 4.0f;
	private float jump = 1f;
	private float fallSpeedup = 0.5f;
	private float groundThreshold = 0.6f;
	
	private float groundFriction = 0.1f;
	private float airFriction = 0.9f;
	
	private boolean isBoy;
	private boolean grounded;
	
	private int keyLeft;
	private int keyRight;
	private int keyJump;
	private int keyDown;
	private int keySwitch;
	
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
		Vector2f v = new Vector2f();
		grounded = body.dotCollisionNormals(new Vector2f(0, 1)) > groundThreshold;
		
		if (Keyboard.down(keyLeft)) {
			v.x -= acceleration * TIME_STEP;
		}
		
		if (Keyboard.down(keyRight)) {
			v.x += acceleration * TIME_STEP;
		}

		if (Keyboard.down(keyDown)) {
			v.y -= fallSpeedup;
		}
		
		if (Keyboard.pressed(keySwitch)) {
			System.out.println("SWOOSH!");
		}

		System.out.println(grounded);
		float friction = grounded ? groundFriction : airFriction;
		Vector2f sum = Vector2f.add(v, 
				new Vector2f(body.getVelocity().x * friction,
						Math.min(body.getVelocity().y, body.getVelocity().y * (Keyboard.down(keyJump) ? 1 : airFriction))
				), null);

		if (Math.abs(sum.x) > maxSpeed) {
			sum.x = Math.signum(sum.x) * maxSpeed;
		}
		
		if (sum.y < -maxFallSpeed) {
			sum.y = -maxFallSpeed;
		}
		
		if (Keyboard.pressed(keyJump) && grounded) {
			sum.y = jump;
		}
		
		System.out.println(sum);
		
		body.setVelocity(sum);
	}

	public boolean isBoy() {
		return isBoy;
	}

	public void setBoy(boolean isBoy) {
		this.isBoy = isBoy;
	}
}
