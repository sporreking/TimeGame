package game.level.enemy;

import game.level.Level;
import sk.entity.Entity;
import sk.gfx.Animation;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.util.vector.Vector2f;

public class Enemy extends Entity {
	
	public final Type TYPE;
	
	protected Transform transform;
	protected Renderer renderer;
	
	protected Level l;
	protected int w;
	
	public boolean dead = false;
	
	protected Animation animationToAdd = null;
	
	public Enemy(Level l, int world, Type type, float x, float y) {
		this.TYPE = type;
		this.l = l;
		this.w = world;
		transform = new Transform();
		transform.position = new Vector2f(x, y);
		
		add(transform);
		
		renderer = new Renderer(Mesh.QUAD);
		add(renderer);
		
		switch(type) {
		case SWALLOWER:
			add(new Swallower());
			break;
		}
	}
	
	@Override
	public void update(double delta) {
		if(animationToAdd != null) {
			add(animationToAdd);
			animationToAdd = null;
		}
		super.update(delta);
	}
	
	@Override
	public void draw() {
		if(l.currentSheet == w)
			super.draw();
	}
	
	public enum Type {
		SWALLOWER
	}
	
}