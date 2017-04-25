package Debug;
import java.util.ArrayList;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import sk.game.Window;
import sk.gfx.Camera;
import sk.gfx.ShaderProgram;
import sk.gfx.Texture;
import sk.util.vector.Matrix4f;
import sk.util.vector.Vector2f;
import sk.util.vector.Vector3f;

/**
 * A debug utility for drawing debug information to the screen.
 * Should not be used in production since a lot of this is hacked
 * together very fast and lose.
 * 
 * @author Ed
 *
 */

public class Debug {
	
	public static boolean DEBUG = false;
	
	private static ArrayList<Vector2f> points;
	private static ArrayList<Vector3f> pointColors;
	
	private static ArrayList<Vector2f> lines;
	private static ArrayList<Vector3f> lineColors;
	
	static {
		points = new ArrayList<>();
		pointColors = new ArrayList<>();
		
		lines = new ArrayList<>();
		lineColors = new ArrayList<>();
	}
	
	public static void drawPoint(float x, float y) {
		drawPoint(new Vector2f(x, y));
	}
	
	public static void drawPoint(Vector2f point) {
		drawPoint(point, new Vector3f(0.0f, 1.0f, 1.0f));
	}
	
	public static void drawPoint(Vector2f point, Vector3f color) {
		if (!DEBUG) return;
		points.add(point);
		pointColors.add(color);
	}
	
	public static void drawLine(Vector2f pointA, Vector2f pointB) {
		drawLine(pointA, pointB, new Vector3f(1.0f, 0.0f, 0.0f));
	}
	
	public static void drawLine(Vector2f pointA, Vector2f pointB, Vector3f color) {
		if (!DEBUG) return;
		lines.add(pointA);
		lines.add(pointB);
		lineColors.add(color);
	}
	
	public static Vector2f TranslateVector2f(Vector2f point, Camera camera) {
		Vector2f p = point.clone();
		p.x *= camera.scale.x;
		p.y *= camera.scale.y;
		
		p.x -= camera.position.x / Window.getAspectRatio();
		p.y -= camera.position.y;
		
		return p;
	}
	
	public static void draw() {
		if (!DEBUG) return;
		
		ShaderProgram.ORTHO.use();
		ShaderProgram.ORTHO.send1i("uses_color", 1);
		ShaderProgram.ORTHO.sendM4("projection", Camera.DEFAULT.getProjection());
		ShaderProgram.ORTHO.sendM4("view", Camera.DEFAULT.getMatrix());
		ShaderProgram.ORTHO.sendM4("model", new Matrix4f());
		
		GL11.glLineWidth(5);
		GL11.glPointSize(10);

		for (int i = 0; i < lines.size(); i += 2) {
			ShaderProgram.ORTHO.send4f("in_color", lineColors.get(i / 2).x, lineColors.get(i / 2).y, lineColors.get(i / 2).z, 1.0f);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2f(lines.get(i).x, lines.get(i).y);
			GL11.glVertex2f(lines.get(i + 1).x, lines.get(i + 1).y);
			GL11.glEnd();
		}

		for (int i = 0; i < points.size(); i++) {
			ShaderProgram.ORTHO.send4f("in_color", pointColors.get(i).x, pointColors.get(i).y, pointColors.get(i).z, 1.0f);
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex2f(points.get(i).x, points.get(i).y);
			GL11.glEnd();
		}
		
		
		ShaderProgram.ORTHO.send1i("uses_color", 0);
		
		points.clear();
		pointColors.clear();
		lines.clear();
		lineColors.clear();
	}
}
