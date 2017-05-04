package sk.gamestate;

public interface GameState {
	
	/**
	 * 
	 * Called when the game state is entered.
	 * Should be used to initialize everything required for the game state to run.
	 * 
	 */
	public void init();
	
	/**
	 * 
	 * Called each frame. Should only be used to perform game logic.
	 * 
	 * @param delta the time passed since the previous frame.
	 */
	public void update(double delta);
	
	/**
	 * 
	 * Called each frame. Should only be used to render.
	 * 
	 */
	public void draw();
	
	/**
	 * 
	 * Called when the game state is exited.
	 * Should destroy and clean up everything left over.
	 * 
	 */
	public void exit();
	
}