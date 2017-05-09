package game.level.enemy;

import game.level.player.Player;
import sk.entity.Component;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Shape;
import sk.util.vector.Vector2f;

public class Swallower extends Component {
	
	public static final double TIMER_START = 1.0;
	
	private Player swallowed = null;
	
	private Enemy e;
	
	private double timer = TIMER_START;
	
	public Swallower() {
		
	}
	
	@Override
	public void init() {
		e = (Enemy) getParent();
		
		e.renderer.setTexture(new Texture("res/texture/wood.png"));
		
		e.transform.scale.x = .1f;
		e.transform.scale.y = .1f;
		
		Body body = new Body(new Shape(new Vector2f[]{
				new Vector2f(-.5f, .5f),
				new Vector2f(.5f, .5f),
				new Vector2f(.5f, -.5f),
				new Vector2f(-.5f, -.5f)
		})).setTrigger(true).setDynamic(false).setLayer((short) 0b0000000000000011);
		
		e.add(body);
		
		e.l.worlds[e.w].addBody(body);
	}
	
	@Override
	public void update(double delta) {
		if(e.dead)
			return;
		
		if(swallowed == null) {
			if(e.get(Body.class).isCollidingWithTags("p1")) {
				swallow(e.l.player1);
			} else if(e.get(Body.class).isCollidingWithTags("p2")) {
				swallow(e.l.player2);
			}
			
			return;
		} else if(swallowed == e.l.player1) {
			if(e.get(Body.class).isCollidingWithTags("p2")) {
				pop();
			}
		} else if(swallowed == e.l.player2) {
			if(e.get(Body.class).isCollidingWithTags("p1")) {
				pop();
			}
		}
		
		timer -= delta;
		
		if(timer <= 0)
			devour();
	}
	
	private void devour() {
		e.transform.scale.x -= .01f;
		e.transform.scale.y -= .01f;
		swallowed.kill();
		swallowed = null;
		timer = TIMER_START;
	}
	
	private void pop() {
		swallowed.get(Body.class).setDynamic(true);
		swallowed.enabled = true;
		swallowed = null;
		e.dead = true;
		e.l.worlds[e.w].removeBody(e.get(Body.class));
	}
	
	private void swallow(Player p) {
		swallowed = p;
		p.enabled = false;
		swallowed.get(Body.class).setDynamic(false).setVelocity(new Vector2f());
		e.transform.scale.x += .01f;
		e.transform.scale.y += .01f;
		swallowed.get(Transform.class).position.x = e.transform.position.x;
		swallowed.get(Transform.class).position.y = e.transform.position.y;
	}
}