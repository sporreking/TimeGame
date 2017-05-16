package editor;

import java.nio.ByteBuffer;

public interface EditObject {
	
	public EditObject copy();
	public void store(ByteBuffer buffer);
	public int bytes();
	public void update(double delta);
	public void draw();
	public void setCurrent(boolean current);
	public void changeCurrent(int inc);
	public void remove();
	
}