package cs.csss.engine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.misc.files.FileComposition;
import cs.csss.misc.files.FileEntry;
import cs.csss.project.IndexTexture;

/**
 * Class responsible for storing and retrieving user settings. This class is made for the purpose of scalability. If offers a single place 
 * to add and remove user settings.
 */
class UserSettings {

	private static final String fileName = "user settings";
	
	private static final CSFolder directory = CSFolder.getRoot("program");
	
	private FileComposition format = new FileComposition();
	
	UserSettings(CSFolder parentDirectory) {
		
		initialize();
				
	}

	/**
	 * Writes the user setting file by binding the current state of all user setting fields and writes them to a file.
	 * 
	 * @param engine — the engine
	 */
	void write(Engine engine) {
		
		format.bindInt("RTTFPS" , engine.realtimeTargetFPS());
		format.bindInt("BGWidth" , IndexTexture.backgroundWidth).bindInt("BGHeight" , IndexTexture.backgroundHeight);
		
		Iterator<Control> controls = Control.iterator();
		
		while(controls.hasNext()) {
			
			Control x = controls.next();
			format.bindBoolean(x.name + " isKB" , x.isKeyboard()).bindShort(x.name + " Code" , (short) x.key());
						
		}
				
		CSFile.makeFile(directory, fileName);
		
		try(FileOutputStream writer = new FileOutputStream(directory.getVirtualPath() + fileName)) {
			
			format.write(writer);
			
		} catch (IOException e) {
			
			throw new IllegalStateException(e);
			
		}
		
	}
	
	/**
	 * Reads user setting data from the file and stores in the engine. 
	 * 
	 * @param engine — the engine
	 */
	void read(Engine engine) {
		
		if(!Files.exists(Paths.get(directory.getVirtualPath() + fileName))) return;
		
		try(FileInputStream reader = new FileInputStream(directory.getVirtualPath() + fileName)) {
			
			format.read(reader);
			
		} catch (IOException e) {
			
			throw new IllegalStateException(e);
			
		}
		
		Iterator<FileEntry> fileContents = format.iterator();
		
		engine.setRealtimeTargetFPS((int)fileContents.next().object());
		IndexTexture.backgroundWidth = (int)fileContents.next().object();
		IndexTexture.backgroundHeight = (int)fileContents.next().object();
		
		Iterator<Control> controls = Control.iterator();
		
		while(controls.hasNext()) { 
		
			Control x = controls.next();
		
			x.isKeyboard((boolean)fileContents.next().object());
			x.key((short)fileContents.next().object());
						
		}		
		
	}
	
	/**
	 * Adds all entries to this class's file composition. This is where the layout of the file is specified.
	 */
	private void initialize() {

		format.addInt("RTTFPS").addInt("BGWidth").addInt("BGHeight");
		
		Iterator<Control> controls = Control.iterator();
		
		while(controls.hasNext()) {
			
			Control x = controls.next();	
			format.addBoolean(x.name + " isKB" , x.isKeyboard()).addShort(x.name + " Code" , (short) x.key());
			
		}
		
	}
	
}

