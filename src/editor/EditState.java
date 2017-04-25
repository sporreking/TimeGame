package editor;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import game.level.Level;
import game.level.LevelData;
import game.level.LevelLoader;
import sk.entity.Entity;
import sk.gamestate.GameState;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.util.io.Keyboard;
import sk.util.io.Mouse;
import sk.util.vector.Vector2f;

public class EditState implements GameState {
	
	// Boolean triggers
	boolean tr_new = false;
	boolean tr_points = true;
	boolean tr_gridSnap = true;
	
	// Level data
	private String path;
	private int chunksX;
	private int chunksY;
	private int chunkPixelSize;
	private float pixelSize;
	private boolean hasData;
	
	private Texture texture;
	private Entity e_texture;
	
	private int currentPolygon = -1;
	private ArrayList<Polygon> polygons = new ArrayList<>();
	
	private FixedStack<ArrayList<Polygon>> undoPolygons = new FixedStack<>(10);
	
	public EditState(String path, boolean hasData, int cx, int cy) {
		this.path = path;
		this.chunksX = cx;
		this.chunksY = cy;
		this.hasData = hasData;
	}
	
	@Override
	public void init() {
		// GL
		GL11.glPointSize(10);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-1, 1, -1, 1, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		// Other
		texture = new Texture(path + ".png");
		
		if(hasData) {
			LevelData ld = LevelLoader.load(path + ".level", true);
			chunkPixelSize = ld.chunkSize;
			chunksX = ld.chunksX;
			chunksY = ld.chunksY;
			
			for(int i = 0; i < ld.terrain.size(); i++) {
				Polygon p = new Polygon(this);
				for(int j = 0; j < ld.terrain.get(i).length; j++) {
					p.add(ld.terrain.get(i)[j].x * 3f / 4f, ld.terrain.get(i)[j].y);
				}
				
				polygons.add(p);
			}
			
			if(!polygons.isEmpty()) {
				currentPolygon = 0;
			}
			
		} else {
			chunkPixelSize = texture.getWidth() / chunksX;
		}
		
		pixelSize = 1f / chunkPixelSize;
		
		e_texture = new Entity();
		
		Transform transform = new Transform();
		transform.scale.x = chunksX;
		transform.scale.y = chunksY;
		e_texture.add(0, transform);
		e_texture.add(0, new Renderer(Mesh.QUAD).setTexture(texture));
		
	}
	
	@Override
	public void update(double delta) {
		
		// Update camera
		updateCamera(delta);
		
		updateTriggers(delta);
		
		updateMouse(delta);
		
		for(Polygon p : polygons) {
			p.update(delta);
		}
	}
	
	private void updateMouse(double delta) {
		if(Mouse.pressed(0)) {
			Vector2f pos = Mouse.getPosition();
			
			// Projection
			pos.x /= 400;
			pos.y /= -300;
			pos.x -= 1;
			pos.y += 1f;
			
			// Transform
			pos.x *= Camera.DEFAULT.scale.x;
			pos.y *= Camera.DEFAULT.scale.y;
			
			pos.x += (Camera.DEFAULT.position.x) * 3f / 4f;
			pos.y += Camera.DEFAULT.position.y;
			
			// Grid snapping
			
			if(tr_gridSnap) {
				float dx = (pos.x % (pixelSize * 3f / 4f));
				float dy = (pos.y % pixelSize);
				
				if(dx < 0)
					dx += pixelSize * 3f / 4f;
				
				if(dy < 0)
					dy += pixelSize;
				
				if(dx > (pixelSize * 3f / 4f) / 2)
					dx -= pixelSize * 3f / 4f;
				
				if(dy > pixelSize / 2)
					dy -= pixelSize;
				
				pos.x -= dx;
				pos.y -= dy;
			}
			
			if(tr_new) {
				undoPush();
				Polygon p = new Polygon(this);
				p.add(pos.x, pos.y);
				polygons.add(p);
				if(currentPolygon >= 0)
					polygons.get(currentPolygon).current = false;
				currentPolygon = polygons.size() - 1;
				p.current = true;
			} else if(polygons.size() > 0) {
				polygons.get(currentPolygon).add(pos.x, pos.y);
			}
		}
	}
	
	private void updateTriggers(double delta) {
		tr_new = Keyboard.down(GLFW.GLFW_KEY_N);
		
		if(Keyboard.pressed(GLFW.GLFW_KEY_Q)) {
			if(!polygons.isEmpty()) {
				if(currentPolygon < 0 || currentPolygon >= polygons.size())
					currentPolygon = 0;
				polygons.get(currentPolygon).changeCurrent(-1);
			}
		} else if(Keyboard.pressed(GLFW.GLFW_KEY_E)) {
			if(!polygons.isEmpty()) {
				if(currentPolygon < 0 || currentPolygon >= polygons.size())
					currentPolygon = 0;
				polygons.get(currentPolygon).changeCurrent(1);
			}
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			if(Keyboard.pressed(GLFW.GLFW_KEY_Z)) {
				if(undoPolygons.elements() > 0)
					polygons = undoPolygons.pop();
				
				if(currentPolygon >= polygons.size())
					currentPolygon = polygons.size() - 1;
			}
			
			if(Keyboard.pressed(GLFW.GLFW_KEY_Q)) {
				polygons.get(currentPolygon).current = false;
				
				currentPolygon--;
				
				currentPolygon %= polygons.size();
				
				if(currentPolygon < 0)
					currentPolygon = polygons.size() - 1;
				
				polygons.get(currentPolygon).current = true;
				
			} else if(Keyboard.pressed(GLFW.GLFW_KEY_E)) {
				polygons.get(currentPolygon).current = false;
				
				currentPolygon++;
				
				currentPolygon %= polygons.size();
				
				polygons.get(currentPolygon).current = true;
			}
			
			if(Keyboard.pressed(GLFW.GLFW_KEY_S)) {
				LevelLoader.save(path + ".level",
						(short) chunkPixelSize, (short) chunksX, (short) chunksY, polygons);
			}
		}
		
		if(Keyboard.pressed(GLFW.GLFW_KEY_X)) {
			undoPush();
			polygons.get(currentPolygon).remove();
		}
		
		if(Keyboard.pressed(GLFW.GLFW_KEY_G)) {
			tr_gridSnap = !tr_gridSnap;
		}
	}
	
	private void updateCamera(double delta) {
		
		if(Keyboard.down(GLFW.GLFW_KEY_W) || Keyboard.down(GLFW.GLFW_KEY_UP)) {
			Camera.DEFAULT.position.y += delta;
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_S)|| Keyboard.down(GLFW.GLFW_KEY_DOWN)) {
			Camera.DEFAULT.position.y -= delta;
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_A) || Keyboard.down(GLFW.GLFW_KEY_LEFT)) {
			Camera.DEFAULT.position.x -= delta;
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_D) || Keyboard.down(GLFW.GLFW_KEY_RIGHT)) {
			Camera.DEFAULT.position.x += delta;
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_O)) {
			Camera.DEFAULT.scale.x = (float) Math.min(Camera.DEFAULT.scale.x + delta, 5f);
			Camera.DEFAULT.scale.y = (float) Math.min(Camera.DEFAULT.scale.y + delta, 5f);
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_I)) {
			Camera.DEFAULT.scale.x = (float) Math.max(Camera.DEFAULT.scale.x - delta, .1f);
			Camera.DEFAULT.scale.y = (float) Math.max(Camera.DEFAULT.scale.y - delta, .1f);
		}
	}
	
	@Override
	public void draw() {
		e_texture.draw();
		
		for(Polygon p : polygons) {
			p.draw();
		}
	}
	
	@Override
	public void exit() {
		texture.destroy();
	}
	
	public void undoPush() {
		ArrayList<Polygon> temp = new ArrayList<>();
		
		for(int i = 0; i < polygons.size(); i++) {
			temp.add(polygons.get(i).copy());
		}
		
		undoPolygons.push(temp);
	}
	
	public void removePolygon() {
		polygons.remove(polygons.get(currentPolygon));
	}
}