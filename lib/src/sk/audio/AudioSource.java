package sk.audio;

import org.lwjgl.openal.AL10;

public class AudioSource {
	
	private int id;
	
	private float gain = 1f, pitch = 1f;
	
	private float targetPitch = 1f, targetGain = 1f;
	
	private float deltaPitch, deltaGain;
	
	//true if to stop on gain zero and false if to pause
	private boolean stop;
	
	/**
	 * 
	 * Creates a new audio source.
	 * 
	 */
	public AudioSource() {
		id = AL10.alGenSources();
	}
	
	/**
	 * 
	 * Plays audio from this source.
	 * 
	 * @param audio the audio to play.
	 * @param loop {@code true} if the audio should loop.
	 */
	public void play(Audio audio, boolean loop) {
		
		AL10.alSourcei(id, AL10.AL_BUFFER, audio.getID());
		AL10.alSourcef(id, AL10.AL_PITCH, pitch);
		AL10.alSourcef(id, AL10.AL_GAIN, gain);
		AL10.alSourcef(id, AL10.AL_MAX_GAIN, 1);
		AL10.alSourcef(id, AL10.AL_MIN_GAIN, 0);
		AL10.alSource3f(id, AL10.AL_POSITION, 0, 0, 0);
		AL10.alSource3f(id, AL10.AL_VELOCITY, 0, 0, 0);
		AL10.alSourcei(id, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
		
		AL10.alSourcePlay(id);
	}
	
	/**
	 * 
	 * Called each frame to update gain and pitch.
	 * 
	 * @param delta the time passed since the previous update.
	 */
	public void update(double delta) {
		adjustGain(delta);
		adjustPitch(delta);
	}
	
	/**
	 * 
	 * Adjusts gain over time to simulate fading.
	 * 
	 * @param delta the time passed since the previous update.
	 */
	private void adjustGain(double delta) {
		if(gain < targetGain) {
			addGain((float)delta * deltaGain);
			if(gain >= targetGain)
				setGain(targetGain);
		} else if(gain > targetGain) {
			addGain((float)delta * deltaGain);
			if(gain <= targetGain) {
				setGain(targetGain);
				if(gain == 0) {
					if(stop)
						stop();
					else
						pause();
				}
			}
		}
	}
	
	/**
	 * 
	 * Adjusts pitch over time to simulate fading.
	 * 
	 * @param delta the time passed since the previous update.
	 */
	private void adjustPitch(double tick) {
		if(pitch < targetPitch) {
			addPitch((float)tick * deltaPitch);
			if(pitch >= targetPitch)
				setPitch(targetPitch);
		} else if(pitch > targetPitch) {
			addPitch((float)tick * deltaPitch);
			if(pitch <= targetPitch)
				setPitch(targetPitch);
		}
	}
	
	/**
	 * 
	 * Pauses this source.
	 * 
	 */
	public void pause() {
		AL10.alSourcePause(id);
	}
	
	/**
	 * 
	 * Stops this source.
	 * 
	 */
	public void stop() {
		AL10.alSourceStop(id);
	}
	
	/**
	 * 
	 * Sets a new pitch fade target.
	 * 
	 * @param target the pitch to fade to.
	 * @param time the duration to fade over in seconds.
	 */
	public void fadePitch(float target, float time) {
		targetPitch = target;
		deltaPitch = target - pitch;
		
		if(time == 0) {
			setPitch(target);
			return;
		}
		
		deltaPitch /= time;
	}
	
	/**
	 * 
	 * Sets a new gain fade target.
	 * 
	 * @param target the gain to fade to.
	 * @param time the duration to fade over in seconds.
	 */
	public void fadeGain(float target, float time) {
		targetGain = target;
		deltaGain = target - gain;
		
		if(time == 0) {
			setGain(target);
			return;
		}
		
		deltaGain /= time;
	}
	
	/**
	 * 
	 * Increases the gain of this source.
	 * 
	 * @param gain the gain to increase by.
	 */
	public void addGain(float gain) {
		this.gain += gain;
		AL10.alSourcef(id, AL10.AL_GAIN, this.gain);
	}
	
	/**
	 * 
	 * Increases the pitch of this source.
	 * 
	 * @param pitch the source to increase by.
	 */
	public void addPitch(float pitch) {
		this.pitch += pitch;
		AL10.alSourcef(id, AL10.AL_PITCH, this.pitch);
	}
	
	/**
	 * 
	 * Sets the gain of this source.
	 * 
	 * @param gain the gain to set.
	 */
	public void setGain(float gain) {
		this.gain = gain;
		targetGain = gain;
		AL10.alSourcef(id, AL10.AL_GAIN, gain);
	}
	
	/**
	 * 
	 * Sets the pitch of this source.
	 * 
	 * @param pitch the pitch to set.
	 */
	public void setPitch(float pitch) {
		this.pitch = pitch;
		targetPitch = pitch;
		AL10.alSourcef(id, AL10.AL_PITCH, pitch);
	}
	
	/**
	 * 
	 * Returns {@code true} if this source is currently playing.
	 * 
	 * @return {@code true} if this source is currently playing.
	 */
	public boolean isPlaying() {
		return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}
	
	/**
	 * 
	 * Sets if the source should stop when the gain hits 0.
	 * 
	 * @param stop
	 */
	public void setZeroStop(boolean stop) {
		this.stop = stop;
	}
	
	/**
	 * 
	 * Destroys the OpenAL generated source ID.
	 * 
	 */
	public void destroy() {
		AL10.alDeleteSources(id);
	}
}