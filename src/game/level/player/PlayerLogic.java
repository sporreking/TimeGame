package game.level.player;

import sk.physics.Body;
import sk.physics.Collision;
import sk.util.io.InputManager;
import sk.util.vector.Vector2f;
import game.level.Level;
import game.level.resources.Key.KeyLauncher;
import game.level.resources.Launchable;
import game.level.resources.Rock.RockLauncher;
import sk.entity.Component;
import sk.entity.Entity;
import sk.gfx.Transform;

public class PlayerLogic extends Launchable {
	
	public enum PlayerStates {
		HELD,
		NORMAL,
		HIT,
	}
	
	private static final Vector2f UP = new Vector2f(0, 1);
	
	private Body body;
	
	private float bufferMaxTime = 0.1f;
	private float bufferTime = 0;
	private boolean jumping = false;
	private boolean onIce = false;
	
	private float gravity = -2.8f;
	private float groundAcc = 15f;
	private float iceAcc = 3.0f;
	private float airAcc = 2.0f;
	private float fallAcc = 0.75f;
	private float maxFallSpeed = 4.0f;
	private float maxSpeed = 0.5f;
	private float jumpVel = 1.0f;
	private float minGroundAngle = 0.3f;

	private float switchCooldown = 0.2f;
	private float switchTimer = 0.0f;
	private float switchCost = 0.1f;
	private static boolean switchTurn = false;
	
	private float groundFriction = 0.4f;
	private float iceFriction = 0.95f;
	private float airFriction = 1.0f;
	private float jumpFriction = 0.75f;
	
	private boolean holding = false;
	private float heldJump = 0.75f;
	private Vector2f holdPos = new Vector2f(0, 0.075f);
	private Launchable launchable = null;
	private boolean thrown = false;
	private float throwSpeed = 0.5f;
	
	private Vector2f platformVelocity = new Vector2f();
	
	private PlayerStates state = PlayerStates.NORMAL;

	private float hitStrength = 0.6f;
	private float hitTime = 0.3f;
	private float hitTimer = 0;;
	
	
	private boolean isBoy;
	
	private String keyLeft;
	private String keyRight;
	private String keyJump;
	private String keyDown;
	private String keySwitch;
	private String keyPickup;
	
	private Level level;
	private Player player;
	
	public PlayerLogic() {
		this(false);
	}
	
	public PlayerLogic(boolean isBoy) {
		this.setBoy(isBoy);

		// Keybindings
		keyLeft 	= (isBoy ? "p1" : "p2") + "_left";
		keyRight 	= (isBoy ? "p1" : "p2") + "_right";
		keyJump 	= (isBoy ? "p1" : "p2") + "_jump";
		keyDown 	= (isBoy ? "p1" : "p2") + "_down";
		keyPickup 	= (isBoy ? "p1" : "p2") + "_pickup";
		keySwitch 	= (isBoy ? "p1" : "p2") + "_switch";
	}
	
	@Override
	public void init() {
		player = (Player) getParent();
		body = player.body;
		transform = player.transform;
		
		switchTurn = false;
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
		
		// @Switcher
		switchTimer -= delta;
		if (InputManager.pressed(keySwitch) && switchTimer < 0 && switchTurn == isBoy) {
			if (Hud.getEnergy() != 0) {
				switchTurn = !switchTurn;
				switchTimer = switchCooldown;
				level.switchTime();
				level.shakeCamera(0.05f, 0.03f);
				Hud.changeEnergy(-switchCost);
			}
		}

		// @Picking up / Throwing
		if (InputManager.pressed(keyPickup) && !holding && !thrown) {
			Collision[] cs = player.pickupTrigger.getCollisions();
			for (Collision c : cs) {
				Launchable l = null;
				l = c.other.getParent().get(PlayerLogic.class);
				
				if (l == null) 
					l = c.other.getParent().get(RockLauncher.class);

				if (l == null) 
					l = c.other.getParent().get(KeyLauncher.class);

				if (l != null) {
					if (l.pickup(player, holdPos)) {
						holding = true;
						launchable = l;
						break;
					}
				}
			}
		} else if (InputManager.pressed(keyPickup)) {
			Vector2f dir = new Vector2f(0, 0.75f * jumpVel);
			
			if (player.getDir() != 1) 
				dir.x = -throwSpeed * 0.1f;
			else
				dir.x = throwSpeed * 0.1f;
			
			if (body.getVelocity().x > 0.1)
				dir.x += throwSpeed;
			
			if (body.getVelocity().x < -0.1)
				dir.x -= throwSpeed;
			
			if (dir.x == 0)
				dir.y *= 1.5;
			
			tryThrow(dir);
		}

		
		switch (state) {
		case HELD:
			// Jumping out
			if (InputManager.pressed(keyJump)) {
				((Player) holder).body.addForce(new Vector2f(0, -heldJump));
				launch(new Vector2f(0, heldJump));
			} else {
				body.setVelocity(new Vector2f());
				transform.position = holder.get(Transform.class).position.clone().add(relativePosition);
			}
			
			// Launch out
			for (Collision c : body.getCollisions()) {
				if (!c.other.isTrigger() && c.normal.dot(UP) < minGroundAngle) {
					launch(c.normal.clone().add(UP).scale(0.25f));
					break;
				}
			}
			break;
		case NORMAL:
			// Ground check
			groundCheck();
			
			// Movement
			Vector2f v = new Vector2f(0, (float) (gravity * delta * (player.grounded ? 0 : 1)));				
			float acc = (player.grounded ? (onIce ? iceAcc : groundAcc) : airAcc);
			
			if (InputManager.down(keyLeft) && (!thrown || !(body.getVelocity().x < -maxSpeed))) {
				v.x -= acc * delta;
			}
			
			if (InputManager.down(keyRight) && (!thrown || !(body.getVelocity().x > maxSpeed))) {
				v.x += acc * delta;
			}
		
			if (InputManager.down(keyDown)) {
				v.y -= fallAcc * delta;
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
		
			float friction = player.grounded ? (onIce ? iceFriction : groundFriction) : airFriction;
			Vector2f bodyVelocity = body.getVelocity(); 
			// Make sure we don't mess with the movement of the platform
			bodyVelocity.sub(platformVelocity);
			platformVelocity.x = 0;
			platformVelocity.y = 0;
			
			// Add in the velocity we normally have
			v.add(new Vector2f(bodyVelocity.x * friction, 
						Math.min(bodyVelocity.y, bodyVelocity.y * 
						(InputManager.down(keyJump) || thrown ? 1 : jumpFriction))));

			if (Math.abs(v.x) > maxSpeed && !thrown) {
				v.x = Math.signum(v.x) * maxSpeed;
			}
			
			float fall = maxFallSpeed;
			if (InputManager.down(keyDown)) {
				fall += fallAcc;
			}
			
			if (v.y < -fall) {
				v.y = -fall;
			}
			
			if (InputManager.pressed(keyJump)) {
				jumping = true;
				bufferTime = 0;
			}
			
			if (jumping) {
				bufferTime += delta;
				jumping = bufferTime < bufferMaxTime;
			}
			
			if (jumping && player.grounded) {
				jumping = false;
				v.y = jumpVel;
			} else {
				if (player.grounded) {
					for (Collision c : body.getCollisions()) {
						// If we get hit by a rock
						if (c.other.getTag().equals("rock") && c.other.getVelocity().lengthSquared() >= 0.1f) {
							state = PlayerStates.HIT;
							body.setTrigger(true);
							
							v.x = -Math.signum(c.distance.x);
							if (v.x == 0) {
								v.x = 1;
							}

							v.x *= 0.7f;
							v.y = 1;
							
							hitTimer = hitTime;
							body.setVelocity(v.scale(hitStrength));
							return;
						} else if (c.normal.dot(UP) > minGroundAngle) {
							// @Moving-Platforms
							if (c.other.getTag().equals("moving-platform")) {
								platformVelocity.add(c.other.getVelocity());
								transform.position.sub(c.normal.clone().scale(c.collisionDepth * c.other.getVelocity().dot(c.normal)));
							} else if (!c.other.isTrigger()) {
								Vector2f n = c.normal.clone();
								v.add(n.scale(-0.9f * n.dot(v)));
							}
							
						}
					}
					v.y = 0;
					v.add(platformVelocity);
				} else {
					for (Collision c : body.getCollisions()) {
						if (!c.other.isTrigger()) {
							Vector2f n = c.normal.clone();
							// Magic number, IDK
							v.add(n.scale(-0.25f * n.dot(v)));
						}
					}
				}
			}
			body.setVelocity(v);
			// If we're holding someone, they should also face this direction
			if (launchableIsPlayer()) {
				((Player) launchable.getParent()).setDir(player.getDir());
			}
		break;
		
		case HIT:
			tryThrow();
			
			for (Collision c : body.getCollisions()) {
				// Weed out the ones we don't want
				if (c.other.isTrigger()) continue;
				if (c.other.getTag().equals("rock")) continue;
				
				body.addVelocity(c.normal.clone().scale(-0.5f * body.getNextVelocity().dot(c.normal)));
				transform.position.add(c.normal.clone().scale(c.collisionDepth));
				
				if (c.normal.dot(UP) > minGroundAngle) {
					body.setVelocityY(Math.abs(body.getVelocity().y));
				}
			}
			
			
			body.addVelocity(UP.clone().scale((float) (gravity * delta)));
			if (hitTimer < 0) {
				state = PlayerStates.NORMAL;
				body.setTrigger(false);
			}
			
			hitTimer -= delta;
			break;
		default:
		}
	}

	boolean trace = false;
	
	public void hit(float time, Vector2f direction) {
		state = PlayerStates.HIT;
		hitTimer = time;
		body.setVelocity(direction);
		trace = true;
	}
	
	public void tryThrow() {
		tryThrow(new Vector2f());
	}
	
	public void tryThrow(Vector2f direction) {
		if (launchable != null) {
			launchable.launch(direction);
		}
	}
	
	public void groundCheck() {
		// Ground check
		onIce = body.isCollidingWithTags("ice");

		player.grounded = false;
		for (Collision c : body.getCollisions()) {
			if (c.other.isTrigger()) continue;
			if (c.other.getTag().equals("moving-platform")) c.normal.negate();
			player.grounded = c.normal.dot(UP) > minGroundAngle;
			if (player.grounded) {
				thrown = false;
				break;
			}
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
	
	public void drop() {
		launchable = null;
		holding = false;
	}

	
	@Override
	public boolean launch(Vector2f direction) {
		PlayerLogic c = null;
		if (holder != null) {
			c = holder.get(PlayerLogic.class);

			if (c != null)
				c.drop();
		}
		
		this.body.setVelocity(direction);
		
		held = false;
		holder = null;
		player.grounded = false;
		thrown = true;
		state = PlayerStates.NORMAL;
		return true;
	}

	@Override
	public boolean pickup(Entity holder, Vector2f relativePosition) {
		if (held) return false;
		
		this.relativePosition = relativePosition;
		this.holder = holder;
		
		// If you're picking up someone who is carrying you...
		if (launchable != null && holder == launchable.getParent()) {
			return false;
		}
		
		held = true;
		state = PlayerStates.HELD;
		return true;
	}

	public boolean launchableIsPlayer() {
		try {
			if (launchable == null)
				return false;
			if ((Player) launchable.getParent() == null)
				return false;
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
