package game.level.enemy;

import java.util.Random;

import game.level.Level;
import game.level.player.Player;
import sk.entity.Component;
import sk.gfx.Animation;
import sk.gfx.SpriteSheet;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Shape;
import sk.util.vector.Vector2f;

public class Swallower extends Component {
	
	public static final double TIMER_START = 2.0;
	private static final float SIZE = .12f;
	private static final float SIZE_BIG = .14f;
	
	private Player swallowed = null;
	
	private Enemy e;
	
	private double timer = TIMER_START;
	
	private boolean shouldDie = false;
	
	// Animations
	private Animation a_idle;
	private Animation a_murder;
	private Animation a_death;
	
	private Animation a_current;
	
	private Random random;
	
	private int dir = -1;
	private float pushWeight = 0.9f;
	
	public Swallower() {
		random = new Random();
	}
	
	@Override
	public void init() {
		e = (Enemy) getParent();
		
		e.transform.scale.x = SIZE;
		e.transform.scale.y = SIZE;
		
		Body body = new Body(Shape.GEN_QUAD(0.25f))
			.setTrigger(true).setDynamic(false)
			.setLayer((short) (Level.P1_LAYER | Level.P2_LAYER));
		
		e.add(body);
		
		e.l.worlds[e.w].addBody(body);
		
		initAnimations();
	}
	
	private void initAnimations() {
		a_idle = new Animation(ss_idle, 8, 0, 1, 2, 3);
		a_death = new Animation(ss_death, 16, 0, 1, 2, 3);
		a_murder = new Animation(ss_murder, 8, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
		
		a_current = a_idle;
		e.add(a_current);
	}
	
	@Override
	public void update(double delta) {
		if(e.dead)
			return;
		
		if(shouldDie) {
			if(a_current.getCurrentFrame() == 3)
				e.dead = true;
			
			return;
		}
		
		if(swallowed == null) {
			
			if(random.nextInt(100) == 0) {
				flip();
			}
			
			if(e.get(Body.class).isCollidingWithTags("p1")) {
				swallow(e.l.player1);
			} else if(e.get(Body.class).isCollidingWithTags("p2")) {
				swallow(e.l.player2);
			}
			
			return;
		} else if(swallowed == e.l.player1) {
			if(e.get(Body.class).isCollidingWithTags("p2")) {
				pop();
				push(e.l.player2, e.l.player1);
			}
		} else if(swallowed == e.l.player2) {
			if(e.get(Body.class).isCollidingWithTags("p1")) {
				pop();
				push(e.l.player1, e.l.player2);
			}
		}
		
		timer -= delta;
		
		if(timer <= 0)
			devour();
	}

	// Send them flying
	private void push(Player poper, Player swallowed) {
		
		e.l.shakeCamera(0.1f, 0.03f);
		
		Vector2f distance = poper.transform.position.clone().sub(e.transform.position.clone());
		
 		distance.normalise();
 		distance.y += 0.25f;
		distance.scale(pushWeight);
		
		poper.playerLogic.hit(0.25f, distance);
		swallowed.playerLogic.hit(0.25f, distance.negate());
	}
	
	private void flip() {
		dir *= -1;
		
		flip(dir);
	}
	
	private void animate(Animation a) {
		if(a_current != null)
			e.remove(Animation.class);
		
		a_current = a;
		e.animationToAdd = a;
	}
	
	private void flip(int dir) {
		this.dir = dir;
		e.get(Transform.class).scale.x = (swallowed == null ? SIZE : SIZE_BIG) * dir;
		e.get(Transform.class).scale.y = swallowed == null ? SIZE : SIZE_BIG;
	}
	
	private void devour() {
		swallowed.alive = false;
		swallowed = null;
		timer = TIMER_START;
		
		animate(a_idle);
	}
	
	private void pop() {
		animate(a_death);
		swallowed.get(Body.class).setDynamic(true);
		swallowed.enabled = true;
		swallowed = null;
		shouldDie = true;
		e.l.worlds[e.w].removeBody(e.get(Body.class));
	}
	
	private void swallow(Player p) {
		swallowed = p;
		flip((int) Math.signum(e.transform.position.x - p.get(Transform.class).position.x));
		p.enabled = false;
		swallowed.get(Body.class).setDynamic(false).setVelocity(new Vector2f());
		swallowed.get(Transform.class).position.x = e.transform.position.x;
		swallowed.get(Transform.class).position.y = e.transform.position.y;
		animate(a_murder);
	}
	
	private static final SpriteSheet ss_idle = new SpriteSheet("res/texture/enemy/frog_idle.png", 4, 1);
	private static final SpriteSheet ss_death = new SpriteSheet("res/texture/enemy/frog_death.png", 4, 1);
	private static final SpriteSheet ss_murder = new SpriteSheet("res/texture/enemy/frog_murder.png", 14, 1);
}
