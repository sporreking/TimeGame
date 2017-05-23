package game.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import game.TG;
import game.level.enemy.Enemy;
import game.level.player.PlayerLogic;
import game.level.player.Hud;
import game.level.player.Player;
import game.level.resources.Battery;
import game.level.resources.Exit;
import game.level.resources.Key;
import game.level.resources.LockedDoor;
import game.level.resources.MoveableDoor;
import game.level.resources.OneshotPressurePlate;
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
	public Chunk background;
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
	
	private float cameraShakeTime;
	private float cameraAmplitude;
	
	private boolean promptRestart = false;
	private ArrayList<SpawnPoint> spawnPoints;
	
	public Level(Player player1, Player player2, LevelData... levelData) {
		this.player1 = player1;
		this.player2 = player2;
		this.data = levelData;
		
		enemies = new Container();
		entities = new Container();
		
		player1.get(PlayerLogic.class).setLevel(this);
		player2.get(PlayerLogic.class).setLevel(this);
		
		Texture forestBorder = new Texture();
		forestBorder = forestBorder.generate(1, 1, new int[] {0xff480000});

		Texture iceBorder = new Texture();
		iceBorder = iceBorder.generate(1, 1, new int[] {0xffb4b4ff});
		
		background = new Chunk(0, 0, forestBorder, iceBorder);
		
		hud = new Hud(this);
		
		worlds = new World[levelData.length];
		terrain = new Body[levelData.length];
		
		createParallax();
		
		for(int i = 0; i < worlds.length; i++) {
			worlds[i] = new World();
			
			worlds[i].gravity = new Vector2f(0, -2.8f);
		}
		
		chunks = new Chunk[2][data[0].chunksY][data[0].chunksX];
		
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
			terrain[i].setLayer((short) (0b100 | P2_LAYER | P1_LAYER));
			
			worlds[i].addBody(terrain[i]);

			worlds[i].addBody(player1.body);
			worlds[i].addBody(player1.pickupTrigger);
			worlds[i].addBody(player2.body);
			worlds[i].addBody(player2.pickupTrigger);

			player1.get(Body.class).setLayer((short) P1_LAYER);
			player2.get(Body.class).setLayer((short) P2_LAYER);
			spawnEntities(i);
		}
		
		terrain[1].setTag("ice");
		
		spawnPlayers();
		
		// We should let the physics simulate first, to make sure we place the players in a good positon. so the camera doesn't shake.
		
		worlds[0].update(0.5f);
		worlds[1].update(0.5f);
		
		initCamera();
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
		
		currentSheet = spawnPoints.get(0).layer;
	}

	private void spawnEntities(int i) {		
		// Holds the door positons temporarily
		HashMap<Integer, Vector2f> doorPositions = new HashMap<Integer, Vector2f>();
		HashMap<Integer, MoveableDoor> doors     = new HashMap<Integer, MoveableDoor>();
		ArrayList<PressurePlate> plates          = new ArrayList<PressurePlate>();
		ArrayList<Integer> 		plateConnections = new ArrayList<Integer>();
	
		for(EntityData ed : data[i].entities) {
			ed.position.x += 0.5f;
			switch(ed.id) {
			// GENERAL //
			case 0: // Spawn points
				spawnPoints.add(new SpawnPoint(ed.position, i));
				break;
			case 1: // Exit
				entities.add(new Exit(this, i, ed.position.x, ed.position.y));
				break;
			// ENEMIES //
			case 2: // Swallower
				enemies.add(new Enemy(this, i, Enemy.Type.SWALLOWER,
						ed.position.x, ed.position.y));
				break;
			case 3: // UNKNOWN
				enemies.add(new Enemy(this, i, Enemy.Type.SWALLOWER,
						ed.position.x + .5f, ed.position.y));
				break;
			// SPAWNABLE //
			case 4: // Key
				entities.add(new Key(this, i, ed.position.x, ed.position.y));
				break;
			case 5: // Locked door
				entities.add(new LockedDoor(this, i, ed.position.x, ed.position.y));
				break;
			case 6: // Rock
				entities.add(new Rock(this, i, ed.position.x, ed.position.y));
				break;
			// DOORS //
			case 7: // Pressure Plate
				PressurePlate plate = new PressurePlate(this, i, ed.position.x, ed.position.y);
				plates.add(plate);
				plateConnections.add(ed.value);
				entities.add(plate);
				break;
			case 8: // Oneshot Pressure Plate
				OneshotPressurePlate oplate = new OneshotPressurePlate(this, i, ed.position.x, ed.position.y);
				plates.add(oplate);
				plateConnections.add(ed.value);
				entities.add(oplate);
				break;
			case 9: { // Movable Door
				MoveableDoor door = new MoveableDoor(this, i, ed.position.x, ed.position.y);
				doors.put(ed.value, door);
				entities.add(door);
				break;
			}
			case 10: // Door Position
				doorPositions.put(ed.value, ed.position);
				break;
			// FOREGOTTEN STUFF //
			case 11: // Battery
				entities.add(new Battery(this, i, ed.position.x, ed.position.y));
				break;
			case 12: {
				MoveableDoor door = new MoveableDoor(this, i, ed.position.x, ed.position.y);
				door.get(Transform.class).rotation = (float) (Math.PI * 0.5f);
				doors.put(ed.value, door);
				entities.add(door);
				break;
			}
			default: // Rotated door
				System.out.println("Error in level file, unknown entity: " + ed.id);
				break;
		 	}	
		}

		// Linkup all the doors
		// Find the positions
		for (int key : doors.keySet()) {
			((MoveableDoor) doors.get(key)).setB(doorPositions.get(key));
		}

		// Link the pressure plates
		for (int j = 0; j < plates.size(); j++) {

			int connections = plateConnections.get(j);

			for (int n = 0; n < Integer.SIZE; n++) {
				if (connections == 0) break;
				if ((connections & 1) == 1) {
					// We connect to this door
					MoveableDoor door = doors.get(1 << n);
					if (door != null)
						plates.get(j).connect(door.getConnectable());

				}
				// Try the next bit
				connections = connections >> 1;
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
	
	public void shakeCamera(float time, float amplitude) {
		cameraShakeTime = time;
		cameraAmplitude = amplitude;
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
			
			if(t == null)
				return;
			
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
	
	private void initCamera() {
		Transform t1 = player1.get(Transform.class);
		Transform t2 = player2.get(Transform.class);
		
		float targetScale = Math.max(.4f, Math.max(Math.abs(t1.position.x - t2.position.x) * 1.1f + 0.5f,
				Math.abs((t1.position.y - t2.position.y) * Window.getAspectRatio())) / 2);
		Vector2f targetPosition = t1.position.clone().add(t2.position).scale(0.5f);
		
		// Make sure the target won't show chunks that are outside.
		if (targetPosition.y + targetScale > 0.5f) {
			targetPosition.y = 0.5f - targetScale;
		} else if (targetPosition.y - targetScale < 0.5f - data[currentSheet].chunksY) {
			targetPosition.y = 0.5f - data[currentSheet].chunksY + targetScale;
		}
		
		Camera.DEFAULT.scale.x = targetScale;
		Camera.DEFAULT.scale.y = targetScale;
		Camera.DEFAULT.position = targetPosition;
	}
	
	private void adjustCamera() {
		Transform t1 = player1.get(Transform.class);
		Transform t2 = player2.get(Transform.class);
		
		if (!player1.isAlive()) {
			promptRestart = true;
			t1 = t2;
		}
		if (!player2.isAlive()) {
			promptRestart = true;
			t2 = t1;
		}
		
		if (!player1.isAlive() && !player2.isAlive()) {
			return;
		}
		
		float targetScale = Math.max(.4f, Math.max(Math.abs(t1.position.x - t2.position.x) * 1.1f + 0.5f,
				Math.abs((t1.position.y - t2.position.y) * Window.getAspectRatio())) / 2);
		
		
		// We can't zoom out more! NOOOO!
		if (targetScale * 2 > data[currentSheet].chunksY) {
			targetScale = data[currentSheet].chunksY / 2.0f;
		} else {
			// Use the default calculation
		}
		
		float scale = (float) (Camera.DEFAULT.scale.x - (Camera.DEFAULT.scale.x - targetScale) * CameraScaleSpeed * Time.getDelta());
		
		// Take what is closer,
		Vector2f targetPosition = t1.position.clone().add(t2.position).scale(0.5f);

		if (Math.abs(scale - targetScale) < 0.0001f) {
			scale = targetScale;
		}
		Camera.DEFAULT.scale.x = scale;
		Camera.DEFAULT.scale.y = scale;

		// Make sure the target won't show chunks that are outside.
		if (targetPosition.y + scale > 0.5f) {
			targetPosition.y = 0.5f - scale;
		} else if (targetPosition.y - scale < 0.5f - data[currentSheet].chunksY) {
			targetPosition.y = 0.5f - data[currentSheet].chunksY + scale;
		}
		
		Camera.DEFAULT.position.add(
				targetPosition.sub(Camera.DEFAULT.position)
				.scale((float) (CameraMoveSpeed * Time.getDelta())));
		
		// Make sure the cam doesn't show anything, even if we change the zoom quickly
		if (Camera.DEFAULT.position.y + scale > 0.5f) {
			Camera.DEFAULT.position.y = 0.5f - scale;
		} else if (Camera.DEFAULT.position.y - scale < -0.5f - data[currentSheet].chunksY) {
			Camera.DEFAULT.position.y = 0.5f - data[currentSheet].chunksY + scale;
		}
		
		// Shake the camera if it is needed
		if (cameraShakeTime > 0) {
			float delta = (float) Time.getDelta();
			cameraShakeTime -= delta;
			
			/*
			 * If you are inspecting this code, you might be thinking:
			 * This creates tendancies towards the diagonals, but this
			 * feels better, I don't know why, but it feels more random
			 * whene the diagonals are over represented.
			 */
			
			float x = (float) (Math.random() - 0.5f) * cameraAmplitude;
			float y = (float) (Math.random() - 0.5f) * cameraAmplitude;
			Vector2f offset = new Vector2f(x, y);
			Camera.DEFAULT.position.add(offset);
		}
		
		// Update the position of the listener
		AudioManager.setListenerPosition(new Vector3f(Camera.DEFAULT.position.x, Camera.DEFAULT.position.y, 0));
	}
	
	@Override
	public void update(double delta) {		
		worlds[currentSheet].update(delta);
		
		adjustCamera();
		
		checkBounds();
		
		player1.update(delta);
		player2.update(delta);		
		
		enemies.update(delta);
		entities.update(delta);
		
		hud.update(delta);
		
		checkDeaths();
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
		// Paralax
		pr_2[currentSheet].draw();
		pr_1[currentSheet].draw();

		// background chunks
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[currentSheet][i][j].drawBG();
			}
		}

		// Ze players
		if (player2.playerLogic.isHeld()) {
			player2.draw();
			player1.draw();
		} else {
			player1.draw();
			player2.draw();			
		}

		// Draw entities and such
		enemies.draw();
		entities.draw();

		// foreground chunks
		for(int i = 0; i < data[0].chunksY; i++) {
			for(int j = 0; j < data[0].chunksX; j++) {
				chunks[currentSheet][i][j].draw();
			}
		}
		
		// The top and bottom
		Transform transform = background.get(Transform.class);
		for (int i = 0; i < data[0].chunksX; i++) {
			for (int j = -1; j < data[0].chunksY + 2; j += data[0].chunksY + 1) {
				transform.position.set(i * Chunk.SCALE, j * Chunk.SCALE);
				if (currentSheet == 0) {
					background.draw();
				} else {
					background.drawBG();
				}
			}
		}
		
		// Left and right wall
		for (int i = -1; i < data[0].chunksX + 2; i += data[0].chunksY + 2) {
			for (int j = -1; j < data[0].chunksY + 1; j++) {
				transform.position.set(i * Chunk.SCALE, j * Chunk.SCALE);
				if (currentSheet == 0) {
					background.draw();
				} else {
					background.drawBG();
				}
			}
		}
		
		if(player1.shouldDie) {
			player1.draw();
		}
		
		if(player2.shouldDie) {
			player2.draw();
		}
		
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

	public void exit() {
		TG.GS_PLAYING.nextLevel();
	}

	public boolean isPromptingRestart() {
		return promptRestart;
	}
}
