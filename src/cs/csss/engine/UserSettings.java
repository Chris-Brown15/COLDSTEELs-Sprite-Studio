package cs.csss.engine;

import static cs.csss.misc.files.FileOperations.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.project.IndexTexture;

/**
 * Class responsible for storing and retrieving user settings. This class is made for the purpose of scalability. If offers a single place 
 * to add and remove user settings.
 */
class UserSettings {

	private static final String fileName = "user settings";	
	private static final CSFolder directory = CSFolder.getRoot("program");

	UserSettings() {}

	/**
	 * Writes the user setting file by binding the current state of all user setting fields and writes them to a file.
	 * 
	 * @param engine — the engine
	 */
	void write(Engine engine) {

		CSFile.makeFile(directory, fileName);
		
		try(FileOutputStream writer = new FileOutputStream(directory.getVirtualPath() + fileName)) {
			
			putInt(engine.realtimeTargetFPS() , writer);
			putInt(IndexTexture.backgroundWidth , writer);
			putInt(IndexTexture.backgroundHeight , writer);
			for(Iterator<Control> controls = Control.iterator() ; controls.hasNext() ; ) {
				
				Control x = controls.next();
				putBoolean(x.isKeyboard() , writer);
				putShort((short)x.key() , writer);
				
			}
			
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

			engine.setRealtimeTargetFPS(getInt(reader));
			IndexTexture.backgroundWidth = getInt(reader);
			IndexTexture.backgroundHeight = getInt(reader);
			for(Iterator<Control> controls = Control.iterator() ; controls.hasNext() ; ) {
				
				Control x = controls.next();
				x.isKeyboard(getBoolean(reader));
				x.key((short)getShort(reader));
				
			}

		} catch (IOException e) {
			
			throw new IllegalStateException(e);
			
		}
				
	}
		
}