package sk.gfx;

import sk.entity.Component;
import sk.util.vector.Matrix4f;
import sk.util.vector.Vector2f;

public class Transform extends Component {
	
	public Vector2f position = new Vector2f();
	public Vector2f scale = new Vector2f(1, 1);
	public float rotation = 0;
	
	/**
	 * 
	 * Returns a matrix representation of this transform.
	 * 
	 * @return a matrix representation of the transform.
	 */
	public Matrix4f getMatrix() {
		Matrix4f translation = (Matrix4f) new Matrix4f().setIdentity();
		translation.m30 = position.getX();
		translation.m31 = position.getY();
		
		Matrix4f rot = (Matrix4f) new Matrix4f().setIdentity();
		rot.m00 = (float) Math.cos(rotation);
		rot.m10 = (float)-Math.sin(rotation);
		rot.m01 = (float) Math.sin(rotation);
		rot.m11 = (float) Math.cos(rotation);
		
		Matrix4f scale = (Matrix4f) new Matrix4f().setIdentity();
		scale.m00 = this.scale.x;
		scale.m11 = this.scale.y;
		
		// What is used to be...
		//return Matrix4f.mul(Matrix4f.mul(rot, translation, null), scale, null);
		// What it actually should be... You silly gose...
		return Matrix4f.mul(Matrix4f.mul(translation, rot, null), scale, null);
	}
}