package sk.audio;

/**
 * 
 * Used to pass instructions from the audio manager thread to the audio handler.
 * 
 * @author Alfred Sporre
 *
 */
public class AudioEvent {
	public static final int EVENT_PLAY = 0;
	public static final int EVENT_PAUSE = 1;
	public static final int EVENT_STOP = 2;
	public static final int EVENT_PLAY_FADE = 3;
	public static final int EVENT_PAUSE_FADE = 4;
	public static final int EVENT_STOP_FADE = 5;
	public static final int EVENT_FADE_GAIN = 6;
	public static final int EVENT_FADE_PITCH = 7;
	
	public final Audio AUDIO;
	public final int EVENT;
	public final float[] PARAMS;
	public final boolean LOOP;
	public final boolean TEMP;
	
	public AudioEvent(Audio audio, boolean loop, boolean temp, int event, float... params) {
		AUDIO = audio;
		EVENT = event;
		PARAMS = params;
		LOOP = loop;
		TEMP = temp;
	}
}