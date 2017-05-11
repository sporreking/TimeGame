package game.movement;

import org.lwjgl.glfw.GLFW;

import sk.entity.Component;
import sk.gfx.Transform;
import sk.physics.Body;
import sk.util.io.Keyboard;
import sk.util.vector.Vector2f;

public class Move extends Component{
	float moveForce= 0.25f;
	float normalConstant = 0.8f;
	Body body;
	
	public Move() {

	}
	@Override
	public void init() {
		body = getParent().get(Body.class);
		body.setBounce(0);
		body.setFriction(0);
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
		if(Keyboard.down(GLFW.GLFW_KEY_D)) {
			body.addForce(new Vector2f((float) (moveForce* delta), 0f));
		}
		if(Keyboard.down(GLFW.GLFW_KEY_A)) {
			body.addForce(new Vector2f((float) (-moveForce* delta), 0f));
		}
		if(Keyboard.down(GLFW.GLFW_KEY_W && ((Object) body).dotCollisionNormals(new Vector2f(0f, 1f)) > normalConstant)) {
			body.addForce(new Vector2f(0f, (float) (1)));
		}
		
	}
	
}
