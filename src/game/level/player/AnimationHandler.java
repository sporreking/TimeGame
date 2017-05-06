package game.level.player;

import sk.entity.Component;
import sk.gfx.Animation;
import sk.gfx.SpriteSheet;

public class AnimationHandler extends Component {
	
	private boolean running = false;
	private int dir = -1;
	private boolean grounded = false;
	
	protected Animation animationToAdd;
	
	private Player player;
	
	public AnimationHandler(Player player) {
		this.player = player;
	}
	
	@Override
	public void init() {
		if(player.isBoy)
			player.add(dude1_idle);
		else
			player.add(dude2_idle);
	}
	
	@Override
	public void update(double delta) {
		
		if(player.dir != dir) {
			switchDir();
			this.dir = player.dir;
		}
		
		if(player.running != running) {
			player.remove(Animation.class);
			animationToAdd = player.running ? (player.isBoy ? dude1_run : dude2_run)
					: (player.isBoy ? dude1_idle : dude2_idle);
			
			running = player.running;
		} else 
		
		if(player.grounded != grounded) {
			player.remove(Animation.class);
			animationToAdd = player.grounded ? (player.isBoy ? (player.running ? dude1_run : dude1_idle) : (player.running ? dude2_run : dude2_idle))
					: (player.isBoy ? dude1_jump : dude2_jump);
			grounded = player.grounded;
			
			if(grounded) {
				(player.isBoy ? dude1_jump : dude2_jump).setSpeed(16);
			}
		}
		
		if(!grounded) {
			if((player.isBoy ? dude1_jump : dude2_jump).getOffset() == 2) {
				(player.isBoy ? dude1_jump : dude2_jump).setSpeed(0);
			}
		}
	}
	
	private void switchDir() {
		if(Math.signum(player.transform.scale.x) != dir) {
			player.transform.scale.x *= -1;
		}
	}
	
	public static Animation dude1_idle;
	public static Animation dude2_idle;
	public static Animation dude1_run;
	public static Animation dude2_run;
	public static Animation dude1_jump;
	public static Animation dude2_jump;
	
	static {
		dude1_idle = new Animation(new SpriteSheet("res/texture/character/dude1_idle.png",
				8, 1), 5, 0, 1, 2, 3, 4, 5, 6, 7);
		
		dude2_idle = new Animation(new SpriteSheet("res/texture/character/dude2_idle.png",
				8, 1), 5, 0, 1, 2, 3, 4, 5, 6, 7);
		
		dude1_run = new Animation(new SpriteSheet("res/texture/character/dude1_run.png",
				8, 1), 16, 0, 1, 2, 3, 4, 5, 6, 7);
		
		dude2_run = new Animation(new SpriteSheet("res/texture/character/dude2_run.png",
				8, 1), 16, 0, 1, 2, 3, 4, 5, 6, 7);
		
		dude1_jump = new Animation(new SpriteSheet("res/texture/character/dude1_jump.png",
				3, 1), 16, 0, 1, 2);
		
		dude2_jump = new Animation(new SpriteSheet("res/texture/character/dude2_jump.png",
				3, 1), 16, 0, 1, 2);
	}
}