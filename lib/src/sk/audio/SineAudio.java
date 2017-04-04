package sk.audio;

import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.alGetError;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.openal.AL10;

public class SineAudio extends Audio {
	
	/**
	 * 
	 * Creates new sine wave audio from the specified frequency and duration.
	 * 
	 * @param frequency the frequency of the sine wave.
	 * @param seconds the duration of the sine wave.
	 */
	public SineAudio(int frequency, int seconds) {
		createAudio(frequency, seconds);
	}
	
	/**
	 * 
	 * Generates new sine wave audio from the specified frequency and duration.
	 * 
	 * @param frequency the frequency of the sine wave.
	 * @param seconds the duration of the sine wave.
	 */
	private void createAudio(int frequency, int seconds) {
		id = AL10.alGenBuffers();
		
		sampleRate = 44100;
		
		int frames = sampleRate * seconds;
		
		ShortBuffer buffer = ByteBuffer.allocateDirect(frames << 1).asShortBuffer();
		
		for(int i = 0; i < frames; i++)
			buffer.put((short)(Short.MAX_VALUE * Math.sin((2 * Math.PI * frequency) / sampleRate * i)));
		
		AL10.alBufferData(id, AL10.AL_FORMAT_MONO16, buffer, sampleRate);
		
		if(alGetError() != AL_NO_ERROR) {
			System.err.println("An error occured when initializing audio data!");
		}
	}
}