package game.level.enemy;

import game.level.Level;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.physics.World;
import sk.util.vector.Vector2f;

public class Enemy extends Entity {
	
	public final Type TYPE;
	
	protected Transform transform;
	protected Renderer renderer;
	
	protected Level l;
	protected int w;
	
	public boolean dead = false;
	
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
	
	public enum Type {
		SWALLOWER
	}
	
}