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

public class Rock extends Entity {
	
	public class RockLauncher extends Launchable {
		Body body;
		Short defaultLayer;
		boolean pickedUpThisFrame = false;
		int sinceThrown = Integer.MAX_VALUE;
		int safeDistance = 3;
		
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

			get(Transform.class).position = holder.get(Transform.class).position.clone().add(relativePosition);
			body.setVelocity(direction);
			sinceThrown = 0;
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
			
			defaultLayer = body.getLayer();
			body.setLayer((short) ((~this.holder.get(Body.class).getLayer()) & defaultLayer));
			body.setOnlyOverlap(true);
			body.setOneWayLeniency(0.3f);

			pickedUpThisFrame = true;
			return true;
		}
		
		@Override
		public void update(double delta) {
			if (held) {
				if (pickedUpThisFrame) {
					pickedUpThisFrame = false;
					return;
				}
				for (Collision c : body.getCollisions()) {
					if (!c.other.isTrigger() && c.other.getParent() != holder) {
						launch(c.normal.clone().scale(0.25f));
						return;
					}
				}
				get(Transform.class).position = holder.get(Transform.class).position.clone().add(relativePosition);
			} else if (sinceThrown < safeDistance){
				sinceThrown++;
				if (sinceThrown == safeDistance) {
					body.setLayer(defaultLayer);
					body.setOnlyOverlap(false);
					body.setOneWayLeniency(-1);
				}
			}
		}
	}

	Transform transform;
	Body body;
	Renderer renderer;
	RockLauncher ra;
	
	Level level;
	int layer = 0;
	
	float size = 5;
	
	public Rock(Level level, int layer, float x, float y) {
		this.level = level;
		this.layer = layer;
		
		transform = new Transform();
		transform.position.x = x;
		transform.position.y = y;
		transform.scale.x = size * Chunk.PIXEL_SCALE;
		transform.scale.y = size * Chunk.PIXEL_SCALE;
		
		body = new Body(5, 7, 0, Shape.QUAD);
		body.setOneWayDirection(new Vector2f(0, 1));
		body.setTag("rock");
		level.worlds[layer].addBody(body);
		
		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new Texture("res/texture/temp.png"));
		
		ra = new RockLauncher();
		
		add(transform);
		add(body);
		add(ra);
		add(renderer);
	}
	
	@Override
	public void draw() {
		if (layer != level.currentSheet) {
			return;
		}
		
		super.draw();
	}
}
