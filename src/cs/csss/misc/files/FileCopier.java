package cs.csss.misc.files;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileCopier {

	public static void copy(String absPathToSource , String absPathToDestination) {
		
		main(absPathToSource , absPathToDestination);
		
	}
	
	public static void main(String... args) {
	
		try(FileOutputStream writer = new FileOutputStream(args[1])) {
			
			writer.write(Files.readAllBytes(Paths.get(args[0])));
			
		} catch(IOException e) {
			
			e.printStackTrace();
			
			
		}
		
	}

	private FileCopier() {}

}
