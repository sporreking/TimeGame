package editor;

import java.awt.Font;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import sk.entity.Entity;
import sk.gfx.Camera;
import sk.gfx.FontTexture;
import sk.gfx.Mesh;
import sk.gfx.Renderer;
import sk.gfx.Transform;
import sk.gfx.gui.GUIText;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector3f;

public class EntityObject implements EditObject {
	
	private EditState es;
	private int index;
	public int value;
	private Vector2f position;
	private boolean current;
	
	private Renderer renderer;
	
	public EntityObject(EditState es, int index, int value, float x, float y) {
		this.es = es;
		this.index = index;
		this.value = value;
		position = new Vector2f(x, y);
		
		Transform t = new Transform();
		t.position.x = x;
		t.position.y = y;
		
		renderer = new Renderer(Mesh.QUAD);
		renderer.setTexture(new FontTexture().generate(Integer.toString(index), 50, 50,
				25, 25, new Font("Arial", 0, 12), new Vector3f(1.0f, 0.0f, 0.0f)));
		renderer.transform.position.x = x * 4f / 3f;
		renderer.transform.position.y = y;
		renderer.transform.scale.x = .2f;
		renderer.transform.scale.y = .2f;
	}
	
	@Override
	public EditObject copy() {
		EntityObject e = new EntityObject(es, index, value, position.x, position.y);
		e.current = current;
		
		return e;
	}
	
	@Override
	public void store(ByteBuffer buffer) {
		buffer.put((byte) 0x02);
		buffer.putShort((short) index);
		buffer.putInt(value);
		buffer.putFloat(position.x);
		buffer.putFloat(position.y);

	}
	
	@Override
	public int bytes() {
		return 1 + 2 + 4 + 2 * 4;
	}

	@Override
	public void update(double delta) {
	}

	@Override
	public void draw() {
		GL20.glUseProgram(0);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glPushMatrix();

		GL11.glScalef(1f / Camera.DEFAULT.scale.x, 1f / Camera.DEFAULT.scale.y, 1);
		
		GL11.glTranslatef(-Camera.DEFAULT.position.x * 3f / 4f,
				-Camera.DEFAULT.position.y, 0);
		
		if(current)
			GL11.glColor3f(1, 0, 0);
		else
			GL11.glColor3f(0, 0, 1);
		
		GL11.glBegin(GL11.GL_POINTS);
		{
			GL11.glVertex2f(position.x, position.y);
		}
		GL11.glEnd();
		
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		if(current)
			renderer.draw();
	}

	public Vector2f getPosition() {
		return position;
	}
	
	public void updatePosition(Vector2f position) {
		this.position = position;
		renderer.transform.position = new Vector2f(position.x * 4.0f / 3.0f, position.y);
	}
	
	@Override
	public void setCurrent(boolean current) {
		this.current = current;
	}

	@Override
	public void changeCurrent(int inc) {
		index += inc;
		System.out.println("New type: " + index);
		
		renderer.getTexture().destroy();
		renderer.setTexture(new FontTexture().generate(Integer.toString(index), 50, 50,
				25, 25, new Font("Arial", 0, 12), new Vector3f(1.0f, 0.0f, 0.0f)));
	}

	@Override
	public void remove() {
		es.removeSelected();
	}
}