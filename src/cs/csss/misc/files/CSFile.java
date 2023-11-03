package cs.csss.misc.files;

import static cs.core.utils.CSUtils.specify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CSFile {

	/**
	 * Creates a file at the given location of the given name, if none exists already, returning the result.
	 * 
	 * @param location — folder to create this file at
	 * @param name — name of created file
	 * @return {@link java.io.File File} representing this file. 
	 */
	public static File makeFile(CSFolder location , String name) {

		try {
			
			Path filePath = Paths.get(location.getRealPath() + CSFolder.separator + name);
			if(!Files.exists(filePath)) return Files.createFile(filePath).toFile();
			else return filePath.toFile();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			throw new IllegalStateException();
			
		}
		
	}
	
	public static CSFile delete(CSFile file) {
		
		try {
			
			Files.delete(Paths.get(file.getRealPath()));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
	protected String name;
	protected final CSFolder location;
	protected FileComposition composition;
	
	public CSFile(final CSFolder location , final String name , FileComposition composition)  {

		this(location , name);		
		this.composition = composition;	
		
	}

	public CSFile(final CSFolder location , final String name) {

		specify(location , "Parent directory cannot be null");
		name(name);
		this.location = location;
		makeFile(location, name);
						
	}
	
	public void name(String newName) {
		
		specify(newName , "CSFiles must have a name.");		
		this.name = newName;
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	public String getRealPath() {
		
		return location.getRealPath() + CSFolder.separator + name;
		
	}
	
	public File asFile() {
		
		return new File(getRealPath());
		
	}
	
	public CSFile copyInto(CSFolder location) {
		
		CSFile copy = new CSFile(location , this.name , composition);
		write();
		return copy;
		
	}

	public void write() {
		
		try(FileOutputStream writer = new FileOutputStream(location.getVirtualPath() + CSFolder.separator + name)) {
			
			composition.write(writer);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void read() {
		
		try(FileInputStream reader = new FileInputStream(location.getVirtualPath() + CSFolder.separator + name)) {
			
			composition.read(reader);
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
}
