package game.level.resources;

import sk.entity.Component;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.util.vector.Vector2f;

public class Movable extends Component {
	
	Body body;
	Transform transform;
	
	Vector2f[] targets;
	int target = 0;
	float speed = 1;
	
	public Movable(Vector2f a, Vector2f b, int target, float speed) {
		targets = new Vector2f[2];
		targets[0] = a.clone();
		targets[1] = b.clone();
		
		this.target = target;
		this.speed = speed;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Component>[] requirements() {
		return (Class<? extends Component>[]) new Class<?>[] {
			Body.class
		}; 
	}
	
	
	public void init() {
		body = getParent().get(Body.class);
		transform = getParent().get(Transform.class);
	}
	
	public void switchTarget() {
		target++;
		target %= 2;
	}
	
	public void setTarget(int target) {
		this.target = target;
	}
	
	@Override
	public void update(double delta) {
		// Points toward target
		Vector2f dir = targets[target].clone().sub(transform.position);
		float length = dir.length();

		float deltaPos = (float) (speed * delta);
		
		// If we'd move past it, warp there
		if (length < deltaPos) {
			transform.position = targets[target].clone();
			body.setVelocity(new Vector2f());
		} else {
			// Move
			dir.scale(speed / length);
			body.setVelocity(dir);
		}
	}

	public void setTarget(int i, Vector2f position) {
		targets[i] = position;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
}
