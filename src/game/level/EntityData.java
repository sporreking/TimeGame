package game.level;

import sk.util.vector.Vector2f;

public class EntityData {
	
	public EntityData() {}
	
	public EntityData(short id, Vector2f position, int value) {
		this.id = id;
		this.position = position;
		this.value = value;
	}
	
	public short id;
	public Vector2f position;
	public int value;
}