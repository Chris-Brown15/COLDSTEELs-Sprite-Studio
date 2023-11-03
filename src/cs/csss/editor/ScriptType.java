/**
 * 
 */
package cs.csss.editor;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Describes the various types of scripts.  
 */
public enum ScriptType {
	
	//for now we use the folder name string as the Steam item metadata
	
	ARTBOARD("artboards") ,
	PROJECT("projects") ,
	SIMPLE("simple brushes"),
	MODIFYING("modifying brushes"),		
	SELECTING("selecting brushes") ,
	EXPORTER("exporters") ,
	PALETTE("palettes")
	;
	
	/**
	 * Name of the script folder associated with this script type.
	 */
	public final String associatedFolderName;
	
	ScriptType(String folderName) {
	
		this.associatedFolderName = folderName;
		
	}		
	
	/**
	 * Returns a tag name for the Sprite Studio Steam Workshop based on this enumeration's name. 
	 * 
	 * @return Tag name for this type.
	 */
	public String asTagName() {
		
		char[] nameChars = this.name().toLowerCase().toCharArray();
		nameChars[0] = Character.toUpperCase(nameChars[0]);
		return new String(nameChars);
		
	}

	/**
	 * Returns a type of script based on {@code directoryName}, which is understood to be one of the eumerations' {@code associatedFolderName}.
	 * 
	 * @param directoryName — name of an associated directory
	 * @return Type who has an associated folder name equal to {@code directoryName}.
	 * @throws NullPointerException if {@code directoryName} is {@code null}.
	 * @throws NoSuchElementException if {@code directoryName} does not name an associated folder name of an enumeration.
	 */
	public static ScriptType getTypeFromDirectoryName(String directoryName) {
		
		Objects.requireNonNull(directoryName);
		ScriptType[] types = values();
		for(int i = 0 ; i < types.length ; i++) if(types[i].associatedFolderName.equals(directoryName)) return types[i];
		throw new NoSuchElementException(directoryName + " does not name a script type via associated folder name.");
		
	}
	
}
