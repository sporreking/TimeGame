package sk.sst;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import sk.entity.Component;

public final class SST extends Component {
	
	public static final String NAME = "Sequential Save Tag System";
	public static final String EXTENSION = "sst";
	
	public static final int DEFAULT_NUM_BACKUPS = 1;
	
	public static final Charset STRING_ENCODING = StandardCharsets.UTF_8;
	
	public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	
	private final HashMap<String, SSTComponent<?>> tags = new HashMap<>();
	
	private int numOfBackups = DEFAULT_NUM_BACKUPS;
	
	/**
	 * 
	 * Store a value.
	 * 
	 * @param tag the tag to associate the value with.
	 * @param data the value to store. Must be numeric, boolean or string.
	 */
	public final void store(String tag, Object data) {
		
		if(!(data instanceof Number || data instanceof String))
			throw new IllegalArgumentException("Tag data must be of type String or Number");
		
		SSTComponent<?> comp = tags.get(tag);
		if(comp == null)
			tags.put(tag, new SSTComponent<>(tag, data));
		else
			if(comp.data.getClass() != data.getClass()) {
				throw new IllegalArgumentException("Error in tag \"" + tag + "\"! Cannot store value of type "
						+ data.getClass().getSimpleName() + " in tag of type " + comp.data.getClass().getSimpleName());
			}
	}
	
	/**
	 * 
	 * Returns the type of the value associated with the tag.
	 * 
	 * @param tag the tag of the value.
	 * @return the type of the value.
	 */
	public final SSTType getTagType(String tag) {
		return tags.get(tag).type;
	}
	
	/**
	 * 
	 * Returns the value associated with the specified tag.
	 * 
	 * @param tag the tag of the value.
	 * @return the value associated with the tag.
	 */
	public final Object get(String tag) {
		return tags.get(tag).data;
	}
	
	/**
	 * 
	 * Outputs each tag into the specified file. If a file  with the same name already exists,
	 * the old one will be saved as a backup - assuming that backups are enabled.
	 * 
	 * @param path the file path.
	 * @throws IOException if the file could not be written to.
	 */
	public final void dump(String path) throws IOException {
		int length = 0;
		
		for(SSTComponent<?> c : tags.values()) {
			//1 byte is reserved for each tag as a type definer
			length++;
			
			//1 byte is reserved for each tag name's end indicator
			length++;
			
			if(c.type == SSTType.STRING) {
				//1 byte is reserved for each String end indicator
				length++;
				//Calculating length of String
				length += ((String)c.data).length();
			} else {
				//Adding length of numeric values
				length += c.type.size;
			}
			
			//Calculating tag name length
			length += c.tag.length();
		}
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(length).order(BYTE_ORDER);
		
		for(SSTComponent<?> c : tags.values()) {
			c.write(buffer);
		}
		
		buffer.flip();
		
		File file = new File(path);
		
		if(!file.exists()) {
			if(file.getParentFile() != null)
				file.getParentFile().mkdirs();
			file.createNewFile();
		} else if(numOfBackups > 0) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
			Date date = new Date();
			String log = dateFormat.format(date);
			
			File backup = new File(file.getParent() == null ? "Backup/" + log + ".bak" : file.getParent() + "/Backup/" + log + ".bak");
			backup.getParentFile().mkdirs();
			file.renameTo(backup);
			
			File[] backups = backup.getParentFile().listFiles();
			
			int i = 0;
			while(backups.length > numOfBackups) {
				backups[i].delete();
				backups = backup.getParentFile().listFiles();
				i++;
			}
			
			file = new File(path);
			file.createNewFile();
		}
		
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		
		FileChannel fc = raf.getChannel();
		
		fc.write(buffer);
		
		raf.close();
	}
	
	/**
	 * 
	 * Loads all tags from a previously saved file.
	 * 
	 * @param path the file path.
	 * @throws IOException if the file could not be read.
	 */
	public final void load(String path) throws IOException {
		tags.clear();
		
		RandomAccessFile raf = new RandomAccessFile(path, "r");
		
		FileChannel fc = raf.getChannel();
		
		ByteBuffer buffer = ByteBuffer.allocateDirect((int)fc.size()).order(BYTE_ORDER);
		
		fc.read(buffer);
		
		buffer.flip();
		
		while(buffer.position() < buffer.limit() - 1) {
			SSTType type;
			switch(buffer.get()) {
			default:
			case 0: type = SSTType.BYTE; break;
			case 1: type = SSTType.SHORT; break;
			case 2: type = SSTType.INT; break;
			case 3: type = SSTType.LONG; break;
			case 4: type = SSTType.FLOAT; break;
			case 5: type = SSTType.DOUBLE; break;
			case 6: type = SSTType.STRING; break;
			}
			
			String tag = readStringFromByteBuffer(buffer);
			
			
			
			switch(type) {
			default:
			case BYTE: tags.put(tag, new SSTComponent<Byte>(tag, buffer.get())); break;
			case SHORT: tags.put(tag, new SSTComponent<Short>(tag, buffer.getShort())); break;
			case INT: tags.put(tag, new SSTComponent<Integer>(tag, buffer.getInt())); break;
			case LONG: tags.put(tag, new SSTComponent<Long>(tag, buffer.getLong())); break;
			case FLOAT: tags.put(tag, new SSTComponent<Float>(tag, buffer.getFloat())); break;
			case DOUBLE: tags.put(tag, new SSTComponent<Double>(tag, buffer.getDouble())); break;
			case STRING: tags.put(tag, new SSTComponent<String>(tag, readStringFromByteBuffer(buffer))); break;
			}
		}
		
		raf.close();
	}
	
	/**
	 * 
	 * Converts a string into a byte buffer of the appropriate format.
	 * 
	 * @param buffer
	 * @return
	 */
	private static String readStringFromByteBuffer(ByteBuffer buffer) {
		buffer.mark();
		
		byte b = buffer.get();
		while(b != (byte)0xFF){
			
			b = buffer.get();
		}
		
		int tagLen = buffer.position();
		buffer.reset();
		tagLen -= buffer.position();
		ByteBuffer tagBuffer = buffer.slice();
		tagBuffer.limit(tagLen-1);
		buffer.position(buffer.position() + tagLen);
		
		return STRING_ENCODING.decode(tagBuffer).toString();
	}
	
	/**
	 * 
	 * Sets the number of backups to be used.
	 * 
	 * @param num the number of backups.
	 * @return this SST instance.
	 */
	public final SST setNumberOfBackups(int num) {
		numOfBackups = num;
		
		return this;
	}
	
	/**
	 * 
	 * Returns the number of backups to be used.
	 * 
	 * @return the number of backups to be used.
	 */
	public final int getNumberOfBackups() {
		return numOfBackups;
	}
	
	private static class SSTComponent <T> {
		
		public final String tag;
		public final SSTType type;
		
		public T data;
		
		/**
		 * 
		 * Creates a new data component.
		 * 
		 * @param tag the tag to associate the value with.
		 * @param data the value.
		 */
		public SSTComponent(String tag, T data) {
			this.tag = tag;
			this.data = data;
			switch(data.getClass().getSimpleName()) {
			default:
			case "Byte":
				type = SSTType.BYTE;
				break;
			case "Short":
				type = SSTType.SHORT;
				break;
			case "Integer":
				type = SSTType.INT;
				break;
			case "Long":
				type = SSTType.LONG;
				break;
			case "Double":
				type = SSTType.DOUBLE;
				break;
			case "String":
				type = SSTType.STRING;
				break;
			}
		}
		
		/**
		 * 
		 * Writes the data of this component to a byte buffer.
		 * 
		 * @param buffer the buffer to write to.
		 */
		public void write(ByteBuffer buffer) {
			
			buffer.put((byte)type.ordinal());
			buffer.put(tag.getBytes(StandardCharsets.UTF_8));
			buffer.put((byte)0xFF);
			
			switch(type) {
			case BYTE: buffer.put((byte)data); break;
			case SHORT: buffer.putShort((short)data); break;
			case INT: buffer.putInt((int)data); break;
			case LONG: buffer.putLong((long)data); break;
			case FLOAT: buffer.putFloat((float)data); break;
			case DOUBLE: buffer.putDouble((double)data); break;
			case STRING: buffer.put(((String)data).getBytes(STRING_ENCODING)); buffer.put((byte)(0xFF)); break;
			}
		}
		
	}
	

	
	public enum SSTType {
		BYTE(Byte.SIZE / 8), SHORT(Short.SIZE / 8), INT(Integer.SIZE / 8),
		LONG(Long.SIZE / 8), FLOAT(Float.SIZE / 8), DOUBLE(Double.SIZE / 8), STRING(-1);
		
		public final int size;
		
		SSTType(int size) {
			this.size = size;
		}
	}
	
}