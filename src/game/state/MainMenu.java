package game.state;

import game.movement.Move;
import sk.entity.Entity;
import sk.entity.Root;
import sk.gamestate.GameState;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Shape;
import sk.physics.World;
import sk.util.vector.Vector2f;

public class MainMenu implements GameState {
	
	Root root = new Root();
	
	Entity player = new Entity();
	Entity floor = new Entity();
	
	
	@Override
	public void init() {
		

		Shape s_shape = new Shape(
				new Vector2f(-0.5f, -0.5f),
				new Vector2f(-0.5f,  0.5f),
				new Vector2f( 0.5f,  0.5f),
				new Vector2f( 0.5f, -0.5f));
		
		player.add(0, new Transform());
		player.add(0, new Renderer(Mesh.QUAD));
		player.add(0, new Body(s_shape));
		player.add(0, new Move());
		root.add(0, "player", player);
		
		
		
		
		floor.add(0, new Transform());
		floor.get(Transform.class).position.y = -1;
		floor.get(Transform.class).scale.x = 4;
		floor.add(0, new Body(s_shape));
		floor.add(0, new Renderer(Mesh.QUAD));
		floor.get(Body.class).setDynamic(false);
		root.add(0, "floor", floor);
		
		
	}
	
	@Override
	public void update(double delta) {
		
	}
	
	@Override
	public void draw() {
		root.draw();
	}
	
	@Override
	public void exit() {
		
	}
}