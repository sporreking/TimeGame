package sk.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;


public class AudioHandler implements Runnable {
	
	public static final int UPS = 60;
	
	//The audio event queues
	private volatile ArrayList<AudioEvent> tempQueue;
	private volatile ArrayList<AudioEvent> loopQueue;
	
	volatile boolean running = true;
	
	//The sources to play from
	private AudioSource[] loopSources;
	private AudioSource[] tempSources;
	
	private int error = 0;
	
	//OpenAL Soft
	private long device;
	private long context;
	
	private volatile boolean processingQueue = false;
	
	protected volatile boolean ready = false;
	
	public void run() {
		try {
			init();
			
			while(running)
				loop();
			
			destroy(error);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Called each frame to update the audio sources.
	 * 
	 */
	private void loop() {
		
		for(AudioSource a : loopSources)
			a.update(1d / UPS);
		
		for(AudioSource a : tempSources)
			a.update(1d / UPS);
		
		handleEvents();
		
		try {
			Thread.sleep(1000 / UPS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Handles the event queues.
	 * 
	 */
	private void handleEvents() {
		
		if(tempQueue.size() > 0) {
			processingQueue = true;
			for(AudioEvent ae : tempQueue) {
				boolean terminate = false;
				for(int i = 0; i < AudioManager.MAX_TEMP_SOURCES; i++) {
					if(!tempSources[i].isPlaying()) {
						switch(ae.EVENT) {
						case AudioEvent.EVENT_PLAY:
							tempSources[i].setGain(ae.PARAMS[0]);
							tempSources[i].setPitch(ae.PARAMS[1]);
							tempSources[i].play(ae.AUDIO, ae.LOOP);
							break;
						case AudioEvent.EVENT_PLAY_FADE:
							tempSources[i].setGain(0);
							tempSources[i].setPitch(ae.PARAMS[1]);
							tempSources[i].play(ae.AUDIO, ae.LOOP);
							tempSources[i].fadeGain(ae.PARAMS[0], ae.PARAMS[2]);
							break;
						case AudioEvent.EVENT_PAUSE:
							tempSources[i].pause();
							break;
						case AudioEvent.EVENT_PAUSE_FADE:
							tempSources[i].fadeGain(0, ae.PARAMS[0]);
							break;
						case AudioEvent.EVENT_STOP:
							tempSources[i].stop();
							break;
						case AudioEvent.EVENT_STOP_FADE:
							tempSources[i].fadeGain(0, ae.PARAMS[0]);
							break;
						}
						
						break;
					} else if(i >= AudioManager.MAX_TEMP_SOURCES - 1) {
						terminate = true;
						break;
					}
				}
				
				if(terminate)
					break;
			}
			
			tempQueue.clear();
		}
		
		if(loopQueue.size() == 0) {
			processingQueue = false;
			return;
		}
		
		processingQueue = true;
		for(AudioEvent ae : loopQueue) {
			int source = (int)ae.PARAMS[0];
			switch(ae.EVENT) {
			case AudioEvent.EVENT_PLAY:
				loopSources[source].setGain(ae.PARAMS[1]);
				loopSources[source].setPitch(ae.PARAMS[2]);
				loopSources[source].play(ae.AUDIO, ae.LOOP);
				break;
			case AudioEvent.EVENT_PLAY_FADE:
				loopSources[source].setGain(0);
				loopSources[source].setPitch(ae.PARAMS[2]);
				loopSources[source].play(ae.AUDIO, ae.LOOP);
				loopSources[source].fadeGain(ae.PARAMS[1], ae.PARAMS[3]);
				break;
			case AudioEvent.EVENT_PAUSE:
				loopSources[source].pause();
				break;
			case AudioEvent.EVENT_PAUSE_FADE:
				loopSources[source].setZeroStop(false);
				loopSources[source].fadeGain(0, ae.PARAMS[1]);
				break;
			case AudioEvent.EVENT_STOP:
				loopSources[source].stop();
				break;
			case AudioEvent.EVENT_STOP_FADE:
				loopSources[source].setZeroStop(true);
				loopSources[source].fadeGain(0, ae.PARAMS[1]);
				break;
			case AudioEvent.EVENT_FADE_GAIN:
				loopSources[source].fadeGain(ae.PARAMS[1], ae.PARAMS[2]);
				break;
			case AudioEvent.EVENT_FADE_PITCH:
				loopSources[source].fadePitch(ae.PARAMS[1], ae.PARAMS[2]);
				break;
			}
		}
		
		loopQueue.clear();
		
		processingQueue = false;
	}
	
	/**
	 * 
	 * Initializes the audio handler. Sets up OpenAL context, queues and sources.
	 * 
	 * @throws IllegalStateException the OpenAL context could not be created.
	 */
	private void init() throws IllegalStateException {
		//Start by acquiring the default device
		device = ALC10.alcOpenDevice((ByteBuffer)null);

		//Create a handle for the device capabilities, as well.
		ALCCapabilities deviceCaps = ALC.createCapabilities(device);
		// Create context (often already present, but here, necessary)
		IntBuffer contextAttribList = ByteBuffer.allocateDirect(16 << 2)
				.order(ByteOrder.nativeOrder()).asIntBuffer();

		// Note the manner in which parameters are provided to OpenAL...
		contextAttribList.put(ALC10.ALC_REFRESH);
		contextAttribList.put(60);

		contextAttribList.put(ALC10.ALC_SYNC);
		contextAttribList.put(ALC10.ALC_FALSE);
		
		contextAttribList.put(0);
		
		contextAttribList.flip();
		
		//create the context with the provided attributes
		context = ALC10.alcCreateContext(device, contextAttribList);
		
		if(!ALC10.alcMakeContextCurrent(context)) {
			throw new IllegalStateException("Failed to make context current");
		}
		
		AL.createCapabilities(deviceCaps);
		
		tempQueue = new ArrayList<>();
		loopQueue = new ArrayList<>();
		
		loopSources = new AudioSource[AudioManager.MAX_LOOP_SOURCES];
		
		for(int i = 0; i < loopSources.length; i++)
			loopSources[i] = new AudioSource();
		
		tempSources = new AudioSource[AudioManager.MAX_TEMP_SOURCES];
		
		for(int i = 0; i < tempSources.length; i++)
			tempSources[i] = new AudioSource();
		
		FloatBuffer buffer = ByteBuffer.allocateDirect(6 << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		buffer.put(new float[]{0, 0, -1, 0, 1, 0});
		
		buffer.flip();
		
		AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
		AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
		AL10.alListenerfv(AL10.AL_ORIENTATION, buffer);
		
		System.out.println("OpenAL v." + AL10.alGetString(AL10.AL_VERSION));
		
		ready = true;
	}
	
	/**
	 * 
	 * Adds audio events to the queues.
	 * 
	 * @param audio the audio events to add.
	 */
	public synchronized void queue(AudioEvent... audio) {
		while(processingQueue);
		
		for(AudioEvent a : audio) {
			if(a.TEMP)
				tempQueue.add(a);
			else
				loopQueue.add(a);
		}
	}
	
	/**
	 * 
	 * Sets the error code for the audio handler.
	 * 
	 * @param error the error code.
	 */
	public synchronized void setError(int error) {
		this.error = error;
	}
	
	/**
	 * 
	 * Destroys the audio manager with the specified error code.
	 * Everything other than 0 counts as an error.
	 * 
	 * @param error the error code, 0 if no error.
	 */
	public synchronized void destroy(int error) {
		ALC10.alcDestroyContext(context);
		ALC10.alcCloseDevice(device);
		
		
		if(error != 0)
			System.err.println("Destroyed AudioManager with errors! (" + error + ")");
	}
}