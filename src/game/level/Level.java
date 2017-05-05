package game.level;

import java.awt.List;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import game.TG;
import game.level.enemy.Enemy;
import game.parallax.ParallaxRender;
import game.state.Playing;
import player.Movement;
import player.Player;
import sk.entity.Container;
import sk.entity.Entity;
import sk.entity.Node;
import sk.entity.Root;
import sk.game.Time;
import sk.game.Window;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.gfx.Vertex2D;
import sk.physics.Body;
import sk.physics.Shape;
import sk.physics.World;
import sk.util.io.Keyboard;
import sk.util.vector.Vector2f;

public class Level extends Node {
	
	public LevelData[] data;
	
	public int currentSheet = 0;
	
	public Chunk[][][] chunks;
	public World[] worlds;
	public Body[] terrain;
	
	public float CameraScaleSpeed = 1.75f;
	public float CameraMoveSpeed = 1.5f;

	public float boundsPadding = 0.1f;

	private Renderer[] r_bg;
	private ParallaxRender[] pr_frnt;
	
	public Player player1, player2;
	
	private Container enemies;
	
	public Level(Player player1, Player player2, LevelData... levelData) {
		this.player1 = player1;
		this.player2 = player2;
		this.data = levelData;
		
		enemies = new Container();
		
		player1.get(Movement.class).setLevel(this);
		player2.get(Movement.class).setLevel(this);
		
		worlds = new World[levelData.length];
		terrain = new Body[levelData.length];
		r_bg = new Renderer[levelData.length];
		
		for(int i = 0; i < worlds.length; i++) {
			worlds[i] = new World();
			
			worlds[i].gravity = new Vector2f(0, -2.5f);
			
			r_bg[i] = new Renderer(Mesh.QUAD);
			r_bg[i].camera = Camera.GUI;
			r_bg[i].transform.scale.x = 4 * 3f / 4f;
			r_bg[i].transform.scale.y = 2;
			r_bg[i].setTexture(new Texture(Playing.PREFIX_URL + TG.GS_PLAYING.chapter
					+ "/" + "bg_" + i + ".png"));
			
			worlds[i].gravity = new Vector2f(0, -2.8f);
		}
		
		chunks = new Chunk[2][data[0].chunksY][data[0].chunksX];
		
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[0][i][j] = new Chunk(j, i, data[0].spriteSheet.getTexture(j, i));
				chunks[1][i][j] = new Chunk(j, i, data[1].spriteSheet.getTexture(j, i));
			}
		}
		
		for(int i = 0; i < levelData.length; i++) {
			
			ArrayList<Shape> shapes = new ArrayList<>();
			
			for(int j = 0; j < data[i].terrain.size(); j++) {
				shapes.add(new Shape(data[i].terrain.get(j)));
			}
			
			Transform t = new Transform();
			t.position.x += .5f;
			
			terrain[i] = new Body(false, 1, 100, 0, shapes);
			terrain[i].decouple(t);
			terrain[i].setLayer((short) 0b0000000000000011);
			
			worlds[i].addBody(terrain[i]);
			worlds[i].addBody(player1.get(Body.class));
			player1.get(Body.class).setLayer((short) 0b0000000000000001);
			worlds[i].addBody(player2.get(Body.class));
			player2.get(Body.class).setLayer((short) 0b0000000000000010);
		}
		Enemy e = new Enemy(this, 0, Enemy.Type.SWALLOWER, .1f, -.4f);
		enemies.add(e);
		worlds[0].addBody(e.get(Body.class));
		System.out.println("TODO: REMOVE ENEMY\nADJUST BG LOADING");
		terrain[1].setTag("ice");
	}
	
	public void switchTime() {
		currentSheet++;
		currentSheet %= 2;
	}
	
	private void checkBounds() {
		
		Transform t;
		Player p;
		for (int i = 0; i < 2; i++) {
			switch (i) {
			case 0:
				p = player1;
				break;
			default:
				p = player2;
			}
				
			t = p.get(Transform.class);
			// No use running a check on someone who is dead
			if (!p.isAlive())
				continue;

			if (t.position.y + boundsPadding < -(chunks[0].length) + 0.5f) {
				p.kill();
			} else if (t.position.x + boundsPadding < -(chunks[0][0].length) + 0.5f) {
				p.kill();
			} else if (t.position.y - boundsPadding > chunks[0].length - 0.5f) {
				p.kill();
			} else if (t.position.x - boundsPadding > chunks[0][0].length - 0.5f) {
				p.kill();
			}
		}
	}
	
	
	private void adjustCamera() {
		Transform t1 = player1.get(Transform.class);
		Transform t2 = player2.get(Transform.class);
		
		if (!player1.isAlive()) {
			t1 = t2;
		}
		if (!player2.isAlive()) {
			t2 = t1;
		}
		
		if (!player1.isAlive() && !player2.isAlive()) {
			return;
		}
		
		float targetScale = Math.max(.4f, Math.max(Math.abs(t1.position.x - t2.position.x) * 1.1f + 0.5f,
				Math.abs((t1.position.y - t2.position.y) * Window.getAspectRatio())) / 2);
		
		float scale = (float) (Camera.DEFAULT.scale.x - (Camera.DEFAULT.scale.x - targetScale) * CameraScaleSpeed * Time.getDelta());
		
		if (Math.abs(scale - targetScale) < 0.0001f) {
			scale = targetScale;
		}
		Camera.DEFAULT.scale.x = scale;
		Camera.DEFAULT.scale.y = scale;

		Vector2f targetPosition = t1.position.clone().add(t2.position).scale(0.5f);
		
		Camera.DEFAULT.position.add(
				targetPosition.sub(Camera.DEFAULT.position)
				.scale((float) (CameraMoveSpeed * Time.getDelta())));
	}
	
	@Override
	public void update(double delta) {
		
		worlds[currentSheet].update(delta);
		
		checkBounds();
		
		player1.update(delta);
		player2.update(delta);		
		
		enemies.update(delta);
		
		checkDeaths();
		
		adjustCamera();
	}
	
	private void checkDeaths() {
		ArrayList<Enemy> trash = new ArrayList<>();
		
		for(Node n : enemies.getNodes()) {
			if(((Enemy) n).dead) {
				trash.add((Enemy) n);
			}
		}
		
		for(Enemy e : trash) {
			enemies.remove(enemies.getIndex(e));
		}
	}
	
	@Override
	public void draw() {
		r_bg[currentSheet].draw();
		player1.draw();
		player2.draw();
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[currentSheet][i][j].draw();
			}
		}
		
		enemies.draw();
	}
	
	@Override
	public void destroy() {
		for(int i = 0; i < data.length; i++)
			data[i].spriteSheet.destroy();
	}
}
