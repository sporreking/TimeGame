package editor;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import sk.entity.Entity;
import sk.gfx.Camera;
import sk.util.vector.Vector2f;

public class Polygon extends Entity implements EditObject {
	
	private boolean current = false;
	
	private ArrayList<Vector2f> points = new ArrayList<>();
	
	public int currentPoint = 0;
	
	private EditState es;
	
	public Polygon(EditState es) {
		this.es = es;
	}
	
	public boolean add(float x, float y) {
		
		if(points.isEmpty()) {
			points.add(new Vector2f(x, y));
			return true;
		}
		
		ArrayList<Vector2f> temp = new ArrayList<>();
		
		for(int i = 0; i < points.size(); i++) {
			
			temp.add(points.get(i));
			
			if(i == currentPoint) {
				temp.add(new Vector2f(x, y));
			}
		}
		
		// Convex?
		if(temp.size() > 2) {
			
			int sign = 0;
			
			for(int i = 0; i < temp.size(); i++) {
				
				Vector2f a = getPoint(temp, i - 1);
				Vector2f b = getPoint(temp, i);
				Vector2f c = getPoint(temp, i + 1);
				
				Vector2f v1 = Vector2f.sub(a, b, null);
				Vector2f v2 = Vector2f.sub(c, b, null);
				
				int s = (int) Math.signum(v1.x * v2.y - v2.x * v1.y);
				
				if(i == 0) {
					sign = s;
				} else if(s != sign) {
					return false;
				}
				
			}
		}
		
		es.undoPush();
		
		points = temp;
		
		changeCurrent(1);
		
		return true;
	}
	
	public Vector2f getPoint(int i) {
		return getPoint(points, i);
	}
	
	public Vector2f getPoint(ArrayList<Vector2f> points, int i) {
		
		i %= points.size();
		
		if(i < 0)
			i = points.size() + i;
		
		return points.get(i);
	}
	
	public void changeCurrent(int inc) {
		currentPoint += inc;
		
		currentPoint %= points.size();
		
		if(currentPoint < 0)
			currentPoint = points.size() - 1;
	}
	
	public void remove() {
		ArrayList<Vector2f> temp = new ArrayList<>();
		
		for(int i = 0; i < points.size(); i++) {
			if(i != currentPoint) {
				temp.add(points.get(i));
			}
		}
		
		if(temp.isEmpty())
			es.removeSelected();
		points = temp;
		currentPoint--;
	}
	
	@Override
	public void draw() {
		
		GL20.glUseProgram(0);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glPushMatrix();

		GL11.glScalef(1f / Camera.DEFAULT.scale.x, 1f / Camera.DEFAULT.scale.y, 1);
		
		GL11.glTranslatef(-Camera.DEFAULT.position.x * 3f / 4f,
				-Camera.DEFAULT.position.y, 0);
		
		drawPolygon();
		
		if(current)
			drawPoints();
		
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
	}
	
	private void drawPolygon() {
		GL11.glBegin(GL11.GL_POLYGON);
		
		if(current)
			GL11.glColor4f(1, 0, 0, .5f);
		else
			GL11.glColor4f(0, 0, 1, .5f);
		
		for(int i = 0; i < points.size(); i++) {
			GL11.glVertex2f(points.get(i).x, points.get(i).y);
		}
		
		GL11.glColor4f(1, 1, 1, 1);
		
		GL11.glEnd();
	}
	
	private void drawPoints() {
		GL11.glBegin(GL11.GL_POINTS);
		
		for(int i = 0; i < points.size(); i++) {
			if(i == currentPoint)
				GL11.glColor4f(1, 0, 0, .5f);
			else
				GL11.glColor4f(1, 1, 0, .5f);
			
			GL11.glVertex2f(points.get(i).x, points.get(i).y);
		}
		
		GL11.glColor4f(1, 1, 1, 1);
		
		GL11.glEnd();
	}
	
	public int bytes() {
		return 1 + 2 + points.size() * 2 * 4;
	}
	
	public int size() {
		return points.size();
	}
	
	public void store(ByteBuffer buffer) {
		buffer.put((byte) 0x01);
		buffer.putShort((short) points.size());
		
		for(Vector2f v : points) {
			buffer.putFloat(v.x);
			buffer.putFloat(v.y);
		}
	}
	
	public Polygon copy() {
		Polygon p = new Polygon(es);
		
		for(int i = 0; i < points.size(); i++) {
			p.points.add(points.get(i));
		}
		
		p.current = current;
		p.currentPoint = currentPoint;
		
		return p;
	}
	
	public void setCurrent(boolean current) {
		this.current = current;
	}
}