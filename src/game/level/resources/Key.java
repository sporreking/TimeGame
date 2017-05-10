package game.level.resources;

import game.level.Chunk;
import game.level.Level;
import game.level.player.PlayerLogic;
import sk.entity.Component;
import sk.entity.Entity;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.physics.Collision;
import sk.physics.Shape;
import sk.util.vector.Vector2f;

public class Key extends Entity {

	public class KeyLauncher extends Launchable {
		Body body;
		boolean pickedupThisFrame = false;
		
		public void init() {
			body = getParent().get(Body.class);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends Component>[] requirements() {
			return (Class<? extends Component>[]) new Class<?>[] {
				Body.class
			}; 
		}
		
		@Override
		public boolean launch(Vector2f direction) {
			PlayerLogic c = holder.get(PlayerLogic.class);
			
			if (c != null)
				c.drop();

			get(Transform.class).position = holder.get(Transform.class).
				position.clone().add(relativePosition);
			body.setVelocity(direction);

			holder = null;
			held = false;
			return true;
		}

		@Override
		public boolean pickup(Entity holder, Vector2f relativePosition) {
			if (held) return false;
			
			this.relativePosition = relativePosition;
			this.holder = holder;
			held = true;
			pickedupThisFrame = true;

			return true;
		}
		
		@Override
		public void update(double delta) {
			if (held) {
				get(Transform.class).position = holder.get(Transform.class).
						position.clone().add(relativePosition);
				// If we picked up the rock this frame, skip it
				if (pickedupThisFrame) return;
				for (Collision c : body.getCollisions()) {
					if (!c.other.isTrigger() && c.other.getParent() != holder) {
						launch(c.normal.clone().scale(0.25f));
						return;
					}
				}
			}
		}
	}

	Level level;
	int layer;

	Transform transform;
	Renderer renderer;
	Body body;
	Launchable launchable;

	float size = 5;
	
	boolean used = false;


	public Key(Level level, int layer, float x, float y) {
		this.layer = layer;
		this.level = level;

		transform = new Transform();
		transform.position.x = x;
		transform.position.y = y;
		transform.scale.x = size * Chunk.PIXEL_SCALE;
		transform.scale.y = size * Chunk.PIXEL_SCALE;

		body = new Body(5, 6, 0, Shape.QUAD);
		// Layer so it doesn't collide with the players
		body.setLayer((short) (0b100));
		body.setTag("key");
		level.worlds[layer].addBody(body);

		launchable = new KeyLauncher();

		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new Texture("res/texture/temp.png"));

		add(transform);
		add(body);
		add(launchable);
		add(renderer);
	}

	@Override
	public void update(double delta) {
		// If we have used the key, don't render anything.
		if (used) return;
		if (level.currentSheet != layer) return;

		super.update(delta);
	}

	@Override
	public void draw() {
		if (used) return;
		if (level.currentSheet != layer) return;

		super.draw();
	}
}
