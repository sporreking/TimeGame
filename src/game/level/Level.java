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
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.SpriteSheet;
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
	
	//private Renderer[] r_bg;
	//private ParallaxRender[] pr_frnt;
	
	public Player player1, player2;
	
	private Container enemies;
	
	private ParallaxRender[] pr_1;
	private ParallaxRender[] pr_2;
	
	public Level(Player player1, Player player2, LevelData... levelData) {
		this.player1 = player1;
		this.player2 = player2;
		this.data = levelData;
		
		enemies = new Container();
		
		player1.get(Movement.class).setLevel(this);
		player2.get(Movement.class).setLevel(this);
		
		worlds = new World[levelData.length];
		terrain = new Body[levelData.length];
		//r_bg = new Renderer[levelData.length];
		
		createParallax();
		
		for(int i = 0; i < worlds.length; i++) {
			worlds[i] = new World();
			
			worlds[i].gravity = new Vector2f(0, -2.5f);
			
			/*r_bg[i] = new Renderer(Mesh.QUAD);
			r_bg[i].camera = Camera.GUI;
			r_bg[i].transform.scale.x = 4 * 3f / 4f;
			r_bg[i].transform.scale.y = 2;
			r_bg[i].setTexture(new Texture(Playing.PREFIX_URL + TG.GS_PLAYING.chapter
					+ "/" + "bg_" + i + ".png"));*/
			
			worlds[i].gravity = new Vector2f(0, -2.8f);
		}
		
		chunks = new Chunk[2][data[0].chunksY][data[0].chunksX];
		System.out.println(TG.GS_PLAYING.getPath() + "bg" + 1 + "_0");
		SpriteSheet ss0 = new SpriteSheet(TG.GS_PLAYING.getPath() + "bg" + 1 + "_0.png",
				data[0].chunksX, data[0].chunksY);
		SpriteSheet ss1 = new SpriteSheet(TG.GS_PLAYING.getPath() + "bg" + 1 + "_1.png",
				data[1].chunksX, data[1].chunksY);
		
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[0][i][j] = new Chunk(j, i, data[0].spriteSheet.getTexture(j, i), ss0.getTexture(j, i));
				chunks[1][i][j] = new Chunk(j, i, data[1].spriteSheet.getTexture(j, i), ss1.getTexture(j, i));
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
	
	private void createParallax() {
		
		// PR1
		pr_1 = new ParallaxRender[2];
		
		for(int i = 0; i < pr_1.length; i++) {
			pr_1[i] = new ParallaxRender(new Mesh(new Vertex2D[] {
					new Vertex2D(-.5f, .5f, 0, 0),
					new Vertex2D(.5f, .5f, 2, 0),
					new Vertex2D(.5f, -.5f, 2, 2),
					new Vertex2D(-.5f, -.5f, 0, 2)
			}, 0, 1, 3, 3, 1, 2), -5f, true);
			
			pr_1[i].transform.scale.x = 4;
			pr_1[i].transform.scale.y = 2;
			
			pr_1[i].setTexture(new Texture(Playing.PREFIX_URL + TG.GS_PLAYING.chapter
					+ "/par1_" + i + ".png"));
		}
		
		//PR2
		pr_2 = new ParallaxRender[2];
		
		for(int i = 0; i < pr_2.length; i++) {
			pr_2[i] = new ParallaxRender(new Mesh(new Vertex2D[] {
					new Vertex2D(-.5f, .5f, 0, 0),
					new Vertex2D(.5f, .5f, 2, 0),
					new Vertex2D(.5f, -.5f, 2, 2),
					new Vertex2D(-.5f, -.5f, 0, 2)
			}, 0, 1, 3, 3, 1, 2), -10f, true);
			
			pr_2[i].transform.scale.x = 4;
			pr_2[i].transform.scale.y = 2;
			
			pr_2[i].setTexture(new Texture(Playing.PREFIX_URL + TG.GS_PLAYING.chapter
					+ "/par2_" + i + ".png"));
		}
	}
	
	public void switchTime() {
		currentSheet++;
		currentSheet %= 2;
	}
	
	private void checkTerrainBlockage() {
		if(player1.get(Body.class).hasDeepCollision(1f / 128 * 5)) {
			System.out.println("DIE P1!");
		}
		
		if(player2.get(Body.class).hasDeepCollision(1f / 128 * 5)) {
			System.out.println("DIE P2!");
		}
	}
	
	private void adjustCamera() {
		Transform t1 = player1.get(Transform.class);
		Transform t2 = player2.get(Transform.class);
		
		float scale = Math.max(.4f, Math.max(Math.abs(t1.position.x - t2.position.x),
				Math.abs((t1.position.y - t2.position.y) * 4f / 3f)) / 2);
		
		Camera.DEFAULT.scale.x = scale;
		Camera.DEFAULT.scale.y = scale;
		Camera.DEFAULT.position.x = (t1.position.x + t2.position.x) / 2;
		Camera.DEFAULT.position.y = (t1.position.y + t2.position.y) / 2;
	}
	
	@Override
	public void update(double delta) {
		
		worlds[currentSheet].update(delta);
		
		checkTerrainBlockage();
		
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
		pr_2[currentSheet].draw();
		pr_1[currentSheet].draw();
		
		//r_bg[currentSheet].draw();
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