package cs.csss.misc.files;

import static cs.core.utils.CSUtils.specify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CSFile extends FileComposition {

	protected String name;
	protected final Directory location;

	CSFile(Directory location , File source) {
		
		this(location , source.getName() , new FileComposition());
				
	}
	
	public CSFile(final Directory location , final String name , FileComposition composition) {

		super(composition);
		name(name);
		specify(location , "Parent directory cannot be null");
		this.location = location;
		
	}

	public void name(String newName) {
		
		specify(newName , "CSFiles must have a name.");
		
		this.name = newName;
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	public String getRealPath() {
		
		return location.getRealPath() + Directory.separator + name;
		
	}
	
	public File asFile() {
		
		return new File(getRealPath());
		
	}
	
	public CSFile copyInto(Directory location) {
		
		CSFile copy = new CSFile(location , this.name , this);
		write();
		return copy;
		
	}

	public void write() {
		
		try(FileOutputStream writer = new FileOutputStream(location.getVirtualPath() + Directory.separator + name)) {
			
			write(writer);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
}
