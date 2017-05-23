package game;

import sk.audio.Audio;
import sk.audio.AudioManager;

public class AudioLib {
	
	// Music
	public static final Audio M_BG = new Audio("res/audio/m_bg.wav");
	public static final Audio M_CALM = new Audio("res/audio/m_calm.wav");
	public static final Audio M_THING = new Audio("res/audio/m_thing.wav");
	
	private static float current = -1;
	
	public static final void playLevelMusic(int i) {
		if(i == current)
			return;

		AudioManager.pauseSource(1f, i == 0 ? 2 : 1);
		AudioManager.playSource(i == 0 ? 1 : 2, 1, 1, 1f, i == 0 ? M_THING : M_CALM, true);
		current = i;
	}
	
	// Sound
	public static final Audio S_FROG_DEATH = new Audio("res/audio/s_frog_death.wav");
	public static final Audio S_TIME_SWITCH = new Audio("res/audio/s_time.wav");
	public static final Audio S_JUMP = new Audio("res/audio/s_jump.wav");
	public static final Audio S_LAND = new Audio("res/audio/s_land.wav");
	public static final Audio S_POP = new Audio("res/audio/s_pop.wav");
	public static final Audio S_BUTTON = new Audio("res/audio/s_button.wav");
}