package game.state;

import org.lwjgl.glfw.GLFW;

import game.parallax.ParallaxRender;
import sk.entity.Entity;
import sk.entity.Root;
import sk.gamestate.GameState;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.util.io.Keyboard;

public class Parallax implements GameState {

	Root root = new Root();
	Entity playLayerFloor = new Entity();
	Entity playLayerBox = new Entity();
	Entity backLayer = new Entity();
	Entity frontLayer = new Entity();
	
	@Override
	public void init() {
		Transform t = new Transform();
		t.position.y = -1.2f;
		t.scale.x = 3.0f;
		playLayerFloor.add(0, t);
		playLayerFloor.add(0, new Renderer(Mesh.QUAD));
		root.add(0, "playLayerFloor", playLayerFloor);
		
		t = new Transform();
		t.position.x = 0.2f;
		t.scale.x = 0.5f;
		t.scale.y = 0.5f;
		backLayer.add(0, t);
		backLayer.add(0, new ParallaxRender(Mesh.QUAD, 2));
		root.add(0, "backLayer", backLayer);
		
		t = new Transform();
		t.position.y = 0.8f;
		t.scale.x = 0.5f;
		t.scale.y = 0.5f;
		frontLayer.add(0, t);
		frontLayer.add(0, new ParallaxRender(Mesh.QUAD, -2));
		root.add(0, "frontLayer", frontLayer);
	}

	@Override
	public void update(double delta) {
		root.update(delta);
		
		if (Keyboard.down(GLFW.GLFW_KEY_W)) {
			Camera.DEFAULT.position.y += delta;
		}
		
		if (Keyboard.down(GLFW.GLFW_KEY_S)) {
			Camera.DEFAULT.position.y -= delta;
		}
		
		if (Keyboard.down(GLFW.GLFW_KEY_A)) {
			Camera.DEFAULT.position.x -= delta;
		}
		
		if (Keyboard.down(GLFW.GLFW_KEY_D)) {
			Camera.DEFAULT.position.x += delta;
		}
		
	}

	@Override
	public void draw() {
		root.draw();
	}

	@Override
	public void exit() {}
}
