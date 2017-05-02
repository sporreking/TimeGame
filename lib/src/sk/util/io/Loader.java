package sk.util.io;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public final class Loader {
	
	/**
	 * 
	 * Loads the content of a file as a string.
	 * 
	 * @param path the file path.
	 * @return the content of the file as a string.
	 */
	public static final String loadSource(String path) {
		StringBuilder sb = new StringBuilder();
		
		try (Scanner scanner = new Scanner(new File(path))) {
			while(scanner.hasNextLine()) {
				sb.append(scanner.nextLine());
				if(scanner.hasNextLine())
					sb.append('\n');
			}
			
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
}