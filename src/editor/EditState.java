package editor;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import game.level.Level;
import game.level.LevelData;
import game.level.LevelLoader;
import sk.entity.Container;
import sk.entity.Entity;
import sk.entity.Node;
import sk.game.Game;
import sk.gamestate.GameState;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.gfx.gui.GUIButton;
import sk.gfx.gui.GUIText;
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
	private ArrayList<EditObject> editObjects = new ArrayList<>();
	
	private FixedStack<ArrayList<EditObject>> undoStack = new FixedStack<>(10);
	
	private AddMode mode = AddMode.POLYGON;
	
	private Container gui;
	
	private boolean guiPressed = false;
	
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
				
				editObjects.add(p);
			}
			
			for(int i = 0; i < ld.entities.size(); i++) {
				EntityObject eo = new EntityObject(this, ld.entities.get(i).id,
						ld.entities.get(i).value, ld.entities.get(i).position.x * 3f / 4f,
						ld.entities.get(i).position.y);
				
				editObjects.add(eo);
			}
			
			if(!editObjects.isEmpty()) {
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
		
		initGUI();
		
		new Thread(new InputThread()).start();
	}
	
	private void initGUI() {
		t_red = new Texture().generate(1, 1, new int[] {0xff0000ff});
		
		gui = new Container();
		
		//B1
		GUIButton b_1 = new GUIButton(-1, 1, 25, -25, 50, 50);
		b_1.setTexture(t_red);
		b_1.setOnClick(() -> guiPress(0));
		b_1.setText(new GUIText("P", 50, 50, Font.getFont("Arial")));
		gui.add(new Entity().add(b_1));
		
		//B2
		GUIButton b_2 = new GUIButton(-1, 1, 75, -25, 50, 50);
		b_2.setTexture(Texture.DEFAULT);
		b_2.setOnClick(() -> guiPress(1));
		b_2.setText(new GUIText("E", 50, 50, Font.getFont("Arial")));
		gui.add(new Entity().add(b_2));
	}
	
	private void guiPress(int i) {
		guiPressed = true;
		mode = AddMode.values()[i];
		
		for(Node n : gui.getNodes()) {
			((Entity) n).get(GUIButton.class).setTexture(Texture.DEFAULT);
		}
		
		((Entity) gui.get(i)).get(GUIButton.class).setTexture(t_red);
	}
	
	@Override
	public void update(double delta) {
		
		// Update camera
		updateCamera(delta);
		
		updateTriggers(delta);
		
		gui.update(delta);
		
		if(guiPressed) {
			guiPressed = false;
		} else {
			updateMouse(delta);
		}
		
		for(EditObject p : editObjects) {
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
			
			if(mode == AddMode.POLYGON) {
				if(tr_new) {
					undoPush();
					Polygon p = new Polygon(this);
					p.add(pos.x, pos.y);
					editObjects.add(p);
					if(currentPolygon >= 0)
						editObjects.get(currentPolygon).setCurrent(false);
					currentPolygon = editObjects.size() - 1;
					p.setCurrent(true);
				} else if(editObjects.size() > 0 && editObjects.get(currentPolygon) instanceof Polygon) {
					((Polygon) editObjects.get(currentPolygon)).add(pos.x, pos.y);
				}
			} else if(mode == AddMode.ENTITY) {
				if(tr_new) {
					undoPush();
					EntityObject eo = new EntityObject(this, 0, 0, pos.x, pos.y);
					editObjects.add(eo);
					if(currentPolygon >= 0)
						editObjects.get(currentPolygon).setCurrent(false);
					currentPolygon = editObjects.size() - 1;
					eo.setCurrent(true);
				}
			}
		}
	}
	
	private void updateTriggers(double delta) {
		tr_new = Keyboard.down(GLFW.GLFW_KEY_N);
		
		if(!Keyboard.down(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			if(Keyboard.pressed(GLFW.GLFW_KEY_Q)) {
				if(!editObjects.isEmpty()) {
					if(currentPolygon < 0 || currentPolygon >= editObjects.size())
						currentPolygon = 0;
					editObjects.get(currentPolygon).changeCurrent(-1);
				}
			} else if(Keyboard.pressed(GLFW.GLFW_KEY_E)) {
				if(!editObjects.isEmpty()) {
					if(currentPolygon < 0 || currentPolygon >= editObjects.size())
						currentPolygon = 0;
					editObjects.get(currentPolygon).changeCurrent(1);
				}
			}
		}
		
		if(Keyboard.down(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			if(Keyboard.pressed(GLFW.GLFW_KEY_Z)) {
				if(undoStack.elements() > 0)
					editObjects = undoStack.pop();
				
				if(currentPolygon >= editObjects.size())
					currentPolygon = editObjects.size() - 1;
			}
			
			if(Keyboard.pressed(GLFW.GLFW_KEY_Q)) {
				editObjects.get(currentPolygon).setCurrent(false);
				
				currentPolygon--;
				
				currentPolygon %= editObjects.size();
				
				if(currentPolygon < 0)
					currentPolygon = editObjects.size() - 1;
				
				editObjects.get(currentPolygon).setCurrent(true);
				
			} else if(Keyboard.pressed(GLFW.GLFW_KEY_E)) {
				editObjects.get(currentPolygon).setCurrent(false);
				
				currentPolygon++;
				
				currentPolygon %= editObjects.size();
				
				editObjects.get(currentPolygon).setCurrent(true);
			}
			
			if(Keyboard.pressed(GLFW.GLFW_KEY_S)) {
				LevelLoader.save(path + ".level",
						(short) chunkPixelSize, (short) chunksX, (short) chunksY, editObjects);
			}
		}
		
		if(Keyboard.pressed(GLFW.GLFW_KEY_X) && editObjects.size() > 0) {
			undoPush();
			editObjects.get(currentPolygon).remove();
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
		
		for(EditObject p : editObjects) {
			p.draw();
		}
		
		gui.draw();
	}
	
	@Override
	public void exit() {
		texture.destroy();
	}
	
	public void undoPush() {
		ArrayList<EditObject> temp = new ArrayList<>();
		
		for(int i = 0; i < editObjects.size(); i++) {
			temp.add(editObjects.get(i).copy());
		}
		
		undoStack.push(temp);
	}
	
	public void removeSelected() {
		editObjects.remove(editObjects.get(currentPolygon));
		if(currentPolygon >= editObjects.size()) {
			currentPolygon = editObjects.size() - 1;
		}
		if(editObjects.size() > 0)
			editObjects.get(currentPolygon).setCurrent(true);
	}
	
	public enum AddMode {
		POLYGON, ENTITY
	}
	
	public static Texture t_red;
	
	private class InputThread implements Runnable {
		
		private Scanner scanner;
		
		public InputThread() {
			this.scanner = new Scanner(System.in);
		}
		
		@Override
		public void run() {
			while(Game.isRunning()) {
				String cmd = scanner.nextLine();
				
				try {
					int i = Integer.parseInt(cmd);
					
					if(editObjects.get(currentPolygon) instanceof EntityObject) {
						((EntityObject) editObjects.get(currentPolygon)).value = i;
						System.out.println("New value: " + i);
					}
					
				} catch (NumberFormatException e) {
					System.err.println("\"" + cmd + "\" is not a valid integer");
				}
			}
		}
	}
}