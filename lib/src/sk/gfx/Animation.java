package sk.gfx;

import sk.entity.Component;

public class Animation extends Component {
	
	private SpriteSheet spriteSheet;
	
	private int[] frames;
	
	private int offset;
	
	private float speed;
	
	private float stack;
	
	/**
	 * 
	 * Creates a new animation.
	 * 
	 * @param spriteSheet the sprite sheet to animate with.
	 * @param speed the speed of the animation in frames per second.
	 * @param frames the order to sample from the sprite sheet with (wrapped by each row).
	 */
	public Animation(SpriteSheet spriteSheet, float speed, int... frames) {
		this.spriteSheet = spriteSheet;
		this.frames = frames;
		this.speed = speed;
		
		offset = 0;
		stack = 0;
	}
	
	@Override
	public void init() {
		getParent().get(Renderer.class).setTexture(spriteSheet.getTexture(frames[offset]));
	}
	
	@Override
	public void update(double delta) {
		stack += delta * speed;
		
		if(stack >= 1.0f) {
			stack -= 1.0f;
			increment();
			getParent().get(Renderer.class).setTexture(spriteSheet.getTexture(frames[offset]));
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<Component>[] requirements() {
		return new Class[] { Renderer.class };
	}
	
	/**
	 * 
	 * Sets the frame order to sample from the sprite sheet with.
	 * 
	 * @param frames the frame order.
	 * @return this animation instance.
	 */
	public Animation setFrames(int... frames) {
		this.frames = frames;
		
		return this;
	}
	
	/**
	 * 
	 * Adds more frames in top of the current order.
	 * 
	 * @param frames the frames to add.
	 * @return this animation instance.
	 */
	
	public Animation addFrames(int... frames) {
		int[] newFrames = new int[this.frames.length + frames.length];
		
		for(int i = 0; i < this.frames.length; i++)
			newFrames[i] = this.frames.length;
		
		for(int i = 0; i < frames.length; i++)
			newFrames[this.frames.length + i] = frames[i];
		
		this.frames = newFrames;
		
		return this;
	}
	
	/**
	 * 
	 * Removes the frame order of this animation.
	 * 
	 * @return this animation instance.
	 */
	public Animation clearFrames() {
		frames = new int[0];
		return this;
	}
	
	/**
	 * 
	 * Returns the number of frames in this animation.
	 * 
	 * @return the number of frames.
	 */
	public int getNumOfFrames() {
		return frames.length;
	}
	
	/**
	 * 
	 * Returns the frame order of this animation.
	 * 
	 * @return the frame order.
	 */
	public int[] getFrames() {
		return frames;
	}
	
	/**
	 * 
	 * Sets the offset into the frame order of this animation.
	 * 
	 * @param offset the new frame order offset.
	 * @return this animation instance.
	 */
	public Animation setOffset(int offset) {
		this.offset = offset % frames.length;
		
		return this;
	}
	
	/**
	 * 
	 * Returns the current frame offset into the animation.
	 * 
	 * @return the offset into the animation.
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * 
	 * Sets the speed of this animation.
	 * 
	 * @param speed the new speed.
	 * @return this animation instance.
	 */
	public Animation setSpeed(float speed) {
		this.speed = speed;
		
		return this;
	}
	
	/**
	 * 
	 * Returns the current speed of the animation.
	 * 
	 * @return the speed of the animation.
	 */
	public float getSpeed() {
		return speed;
	}
	
	/**
	 * 
	 * Returns the current frame of this animation.
	 * 
	 * @return the current frame.
	 */
	public int getCurrentFrame() {
		return frames[offset];
	}
	
	/**
	 * 
	 * Increments this animation by one frame.
	 * 
	 * @return this animation instance.
	 */
	public Animation increment() {
		offset++;
		offset %= frames.length;
		
		return this;
	}
	
	/**
	 * 
	 * Decrements this animation by one frame.
	 * 
	 * @return this animation instance.
	 */
	public Animation decrement() {
		offset--;
		offset %= frames.length;
		
		return this;
	}
}