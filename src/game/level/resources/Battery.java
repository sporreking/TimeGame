package game.level.resources;

import game.level.Level;
import game.level.player.Hud;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Collision;
import sk.physics.Shape;

public class Battery extends Entity {
	Body body;
	Renderer renderer;
	Transform transform;
	
	Level level;
	
	private static final float ENERGY = 0.5f;
	private static final float SCALE = 0.025f;
	private static final int SCORE = 100; 
	
	boolean taken = false;
	
	public Battery(Level level, float x, float y) {
		this.level = level;

		transform = new Transform();
		transform.position.x = x;
		transform.position.y = y;
		transform.scale.x = SCALE;
		transform.scale.y = SCALE;
		
		body = new Body(Shape.QUAD);
		body.setTrigger(true);
		body.setDynamic(false);
		body.setTag("battery");
		level.worlds[0].addBody(body);
		
		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new Texture("res/texture/temp.png"));
		
		add(transform);
		add(body);
		add(renderer);
	}
	
	@Override
	public void update(double delta) {
		if (taken) {
			return;
		}
		
		System.out.println(body);
		
		if (body.isCollidingWithTags("p1", "p2")) {
			System.out.println("Battery collected!");
			Hud.changeEnergy(ENERGY);
			Hud.addScore(SCORE);
			taken = true;
			return;
		}
		
		super.update(delta);
	}
	
	@Override
	public void draw() {
		if (taken) {
			return;
		}
		
		super.draw();
	}
}
