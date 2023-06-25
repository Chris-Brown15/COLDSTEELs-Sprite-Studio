package cs.csss.artboard;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cs.csss.misc.files.FileComposition;

public class LayerMeta {

	private static final String
		LOCKED_ENTRY = "Locked" ,
		HIDING_ENTRY = "Hiding" ,
		NAME_ENTRY = "Name"
	;
	
	private final FileComposition meta = new FileComposition()
		.addBoolean(LOCKED_ENTRY)
		.addBoolean(HIDING_ENTRY)
		.addString(NAME_ENTRY)
	;
	
	public LayerMeta() {}

	LayerMeta bindName(String name) {
		
		meta.bindString(NAME_ENTRY, name);
		return this;
		
	}
	
	LayerMeta bindLocked(boolean locked) {
		
		meta.bindBoolean(LOCKED_ENTRY, locked);
		return this;		
		
	}
	
	LayerMeta bindHiding(boolean hiding) {
		
		meta.bindBoolean(HIDING_ENTRY, hiding);
		return this;
		
	}
	
	public String name() {
		
		return (String) meta.get(NAME_ENTRY);
		
	}
	
	public boolean locked() {
		
		return (boolean) meta.get(LOCKED_ENTRY);
		
	}
	
	public boolean hiding() {
		
		return (boolean) meta.get(HIDING_ENTRY);
		
	}
	
	public void write(FileOutputStream writer) throws IOException {
		
		meta.write(writer);
		
	}
	
	public void read(FileInputStream reader) throws IOException {
		
		meta.read(reader);
		
	}
	
}
