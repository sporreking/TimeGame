package game.level;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import editor.Polygon;
import sk.gfx.SpriteSheet;
import sk.gfx.Texture;
import sk.util.vector.Vector2f;

public class LevelLoader {
	
	public static final int HEADER_SIZE = 11;
	
	public static final String PREFIX = "res/level/";
	
	public static final LevelData load(String name) {
		return load(name, false);
	}
	
	public static final LevelData load(String name, boolean editor) {
		
		try {
			
			// Open file
			RandomAccessFile raf = new RandomAccessFile((editor ? name : PREFIX + name + ".level"), "r");
			
			FileChannel fc = raf.getChannel();
			
			long size = fc.size();
			
			ByteBuffer buffer = ByteBuffer.allocate((int) size).order(ByteOrder.LITTLE_ENDIAN);
			
			fc.read(buffer);
			
			buffer.flip();
			
			// Read data
			
			LevelData ld = load(buffer);
			
			if(!editor) {
				ld.spriteSheet = new SpriteSheet(PREFIX + name + ".png", ld.chunksX, ld.chunksY);
			}
			
			// Close file
			fc.close();
			raf.close();
			
			return ld;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static final LevelData load(ByteBuffer buffer) throws IOException {
		
		LevelData ld = new LevelData();
		
		// Level dimensions
		
		byte[] headerTop = new byte[5];
		buffer.get(headerTop, 0, 5);
		
		if(!new String(headerTop).equals(new String(HEADER_TOP)))
			throw new IOException("Invalid file header");
		
		ld.chunkSize = buffer.getShort();
		
		ld.chunksX = buffer.getShort();
		
		ld.chunksY = buffer.getShort();
		
		ld.terrain = new ArrayList<>();
		
		while(buffer.hasRemaining()) {
			byte b = buffer.get();
			
			switch(b) {
			
			// Polygon
			case 0x01:
				
				short length = buffer.getShort();
				
				Vector2f[] points = new Vector2f[length];
				
				for(int i = 0; i < length; i++) {
					points[i] = new Vector2f(buffer.getFloat() * 4f / 3f, buffer.getFloat());
				}
				
				ld.terrain.add(points);
				
				break;
			}
		}
		
		return ld;
	}
	
	public static final void save(String name, short chunkSize, short chunksX, short chunksY,
			ArrayList<Polygon> polygons) {
		
		try {
			
			// Open file
			File file = new File(name + ".level");
			
			
			
			if(file.exists())
				file.delete();
			
			file.createNewFile();
			
			RandomAccessFile raf = new RandomAccessFile(name, "rw");
			
			FileChannel fc = raf.getChannel();
			
			// Size
			int size = HEADER_SIZE;
			
			for(Polygon p : polygons) {
				size += p.bytes();
			}
			
			// Write
			ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
			
			buffer.put(HEADER_TOP);
			buffer.putShort(chunkSize);
			buffer.putShort(chunksX);
			buffer.putShort(chunksY);
			
			for(Polygon p : polygons) {
				p.store(buffer);
			}
			
			buffer.flip();
			
			fc.write(buffer);
			
			fc.close();
			
			raf.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static final byte[] HEADER_TOP = {'L', 'E', 'V', 'E', 'L'};
	
	/*
	 * .level format
	 * 
	 * Header:
	 * 
	 * 5b	|	"LEVEL"
	 * 2b	|	chunkSize
	 * 2b	|	chunksX
	 * 2b	|	chunksY
	 * 
	 * Body:
	 * 
	 * Polygon:
	 * {
	 * 2b			|	0x01
	 * 2b			|	polyCount
	 * 4b*polyCount	|	x
	 * 4b*polyCount	|	y
	 * }
	 */
}