package cs.csss.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public final class FileUtils {

	/**
	 * Returns a String containing the entire contents of {@code filepath} as text.
	 * 
	 * @param filepath filepath to read
	 * @return String representing the file at {@code filepath} or {@code null} if an IO error occurs.
	 * @throws NullPointerException if {@code filepath} is <code>null</code>.
	 * @throws IOException if an error occurs while reading the file at {@code filepath}.
	 */
	public static String readAllCharacters(String filepath) {
		
		Scanner scanner;
		try {

			scanner = new Scanner(new File(filepath));
			StringBuilder lines = new StringBuilder();
			while(scanner.hasNextLine()) lines.append(scanner.nextLine() + System.lineSeparator());
			scanner.close();
			return lines.toString();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			throw new IllegalArgumentException(filepath + " is not a valid file path");
			
		}
		
	}	
	
	private FileUtils() {}

}
