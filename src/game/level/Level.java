package game.level;

import java.util.ArrayList;
import java.util.Random;

import game.TG;
import game.level.enemy.Enemy;
import game.level.player.PlayerLogic;
import game.level.player.Hud;
import game.level.player.Player;
import game.level.resources.Battery;
import game.level.resources.PushDownDoor;
import game.level.resources.PressurePlate;
import game.level.resources.Rock;
import game.parallax.ParallaxRender;
import game.state.Playing;
import sk.audio.AudioManager;
import sk.entity.Container;
import sk.entity.Node;
import sk.game.Time;
import sk.game.Window;
import sk.gfx.Camera;
import sk.gfx.Mesh;
import sk.gfx.SpriteSheet;
import sk.gfx.Texture;
import sk.gfx.Transform;
import sk.gfx.Vertex2D;
import sk.physics.Body;
import sk.physics.Shape;
import sk.physics.World;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector3f;

public class Level extends Node {
	
	public LevelData[] data;
	
	public int currentSheet = 0;
	
	public Chunk[][][] chunks;
	public World[] worlds;
	public Body[] terrain;
	
	public float CameraScaleSpeed = 1.75f;
	public float CameraMoveSpeed = 1.5f;

	public float boundsPadding = 0.1f;
	
	public static final short P1_LAYER = 0b0000000000000001;
	public static final short P2_LAYER = 0b0000000000000010;
	
	public Player player1, player2;
	public Hud hud;
	
	private Container enemies;
	private Container entities;
	
	private ParallaxRender[] pr_1;
	private ParallaxRender[] pr_2;
	
	private ArrayList<SpawnPoint> spawnPoints;
	
	public Level(Player player1, Player player2, LevelData... levelData) {
		this.player1 = player1;
		this.player2 = player2;
		this.data = levelData;
		
		enemies = new Container();
		entities = new Container();
		
		player1.get(PlayerLogic.class).setLevel(this);
		player2.get(PlayerLogic.class).setLevel(this);
		
		hud = new Hud();
		
		worlds = new World[levelData.length];
		terrain = new Body[levelData.length];
		//r_bg = new Renderer[levelData.length];
		
		createParallax();
		
		for(int i = 0; i < worlds.length; i++) {
			worlds[i] = new World();
			
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
		
		spawnPoints = new ArrayList<>();
		
		for(int i = 0; i < levelData.length; i++) {
			
			ArrayList<Shape> shapes = new ArrayList<>();
			
			for(int j = 0; j < data[i].terrain.size(); j++) {
				shapes.add(new Shape(data[i].terrain.get(j)));
			}
			
			Transform t = new Transform();
			t.position.x += .5f;
			
			terrain[i] = new Body(false, 1, 100, 0, shapes);
			terrain[i].decouple(t);
			terrain[i].setLayer((short) (P2_LAYER | P1_LAYER));
			
			worlds[i].addBody(terrain[i]);

			worlds[i].addBody(player1.body);
			worlds[i].addBody(player1.pickupTrigger);
			worlds[i].addBody(player2.body);
			worlds[i].addBody(player2.pickupTrigger);

			player1.get(Body.class).setLayer((short) P1_LAYER);
			player2.get(Body.class).setLayer((short) P2_LAYER);
			spawnEntities(i);
		}
		//Enemy e = new Enemy(this, 0, Enemy.Type.SWALLOWER, .1f, -.4f);
		//enemies.add(e);
		//worlds[0].addBody(e.get(Body.class));
		entities.add(new Battery(this, 1, -0.1f, -0.4f));
		entities.add(new Rock(this, 0, 0.1f, 0.0f));
		
		PushDownDoor temp = new PushDownDoor(this, 0, 0.2f, 0.2f);
		temp.setA(new Vector2f(0,  0.5f));
		temp.setB(new Vector2f(0, -0.4f));
		temp.setSpeed(0.1f);
		
		PressurePlate plate = new PressurePlate(this, 0, -0.1f, -0.4f);
		plate.connect(temp.getConnectable());

		entities.add(plate);
		entities.add(temp);
		
		System.out.println("TODO: REMOVE ENEMY\nADJUST BG LOADING");

		terrain[1].setTag("ice");
		
		spawnPlayers();
	}
	
	private void spawnPlayers() {
		if(spawnPoints.size() <= 0) {
			throw new IllegalStateException("There must be at least one spawnpoint");
		} else if(spawnPoints.size() == 1) {
			player1.get(Transform.class).position.x = spawnPoints.get(0).position.x;
			player1.get(Transform.class).position.y = spawnPoints.get(0).position.y;
			player2.get(Transform.class).position.x = spawnPoints.get(0).position.x;
			player2.get(Transform.class).position.y = spawnPoints.get(0).position.y;
		} else {
			Random random = new Random();
			
			int r1 = random.nextInt(spawnPoints.size());
			
			int r2 =random.nextInt(spawnPoints.size());
			while(r2 == r1) {
				r2 =random.nextInt(spawnPoints.size());
			}
			
			player1.get(Transform.class).position.x = spawnPoints.get(r1).position.x;
			player1.get(Transform.class).position.y = spawnPoints.get(r1).position.y;
			player2.get(Transform.class).position.x = spawnPoints.get(r2).position.x;
			player2.get(Transform.class).position.y = spawnPoints.get(r2).position.y;
		}
		
		player1.get(Transform.class).position.x += .5f;
		player2.get(Transform.class).position.x += .5f;
		
		currentSheet = spawnPoints.get(0).layer;
	}
	
	private void spawnEntities(int i) {
		
		for(EntityData ed : data[i].entities) {
			System.out.println(ed.value);
			switch(ed.id) {
			case 0: // Spawn points
				spawnPoints.add(new SpawnPoint(ed.position, i));
				break;
			case 1: // Swallower
				enemies.add(new Enemy(this, i, Enemy.Type.SWALLOWER,
						ed.position.x + .5f, ed.position.y));
				break;
			}
		}
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
		
		player1.switchTime();
		player2.switchTime();
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
		
		// Update the position of the listener
		AudioManager.setListenerPosition(new Vector3f(Camera.DEFAULT.position.x, Camera.DEFAULT.position.y, 0));
	}
	
	@Override
	public void update(double delta) {
		
		worlds[currentSheet].update(delta);
		
		checkBounds();
		
		player1.update(delta);
		player2.update(delta);		
		
		enemies.update(delta);
		entities.update(delta);
		
		hud.update(delta);
		
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
		
		if (player2.playerLogic.isHeld()) {
			player2.draw();			
			player1.draw();
		} else {
			player1.draw();
			player2.draw();			
		}

		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[currentSheet][i][j].draw();
			}
		}
		
		enemies.draw();
		entities.draw();
		
		hud.draw();
	}
	
	@Override
	public void destroy() {
		for(int i = 0; i < data.length; i++)
			data[i].spriteSheet.destroy();
	}
	
	private class SpawnPoint {
		public Vector2f position;
		public int layer;
		
		public SpawnPoint(Vector2f position, int layer) {
			this.position = position;
			this.layer = layer;
		}
	}
}
