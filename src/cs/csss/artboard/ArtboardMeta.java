package cs.csss.artboard;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cs.csss.misc.files.FileComposition;

public class ArtboardMeta  {
	
	private static final String
		WIDTH_ENTRY = "Width" ,
		HEIGHT_ENTRY = "Height" ,
		POSITION_ENTRY = "Position"
	;

	private final FileComposition meta = new FileComposition()
		.addInt(WIDTH_ENTRY)
		.addInt(HEIGHT_ENTRY)
		.addFloatArray(POSITION_ENTRY)
	;
	
	public ArtboardMeta() {}
	
	ArtboardMeta bindWidth(int width) {
		
		meta.bindInt(WIDTH_ENTRY, width);
		return this;
		
	}
	
	ArtboardMeta bindHeight(int height ) {
		
		meta.bindInt(HEIGHT_ENTRY, height);
		return this;
		
	}
	
	ArtboardMeta bindPosition(float[] position) {
		
		meta.bindFloatArray(POSITION_ENTRY, position);
		return this;
			
	}

	public void write(FileOutputStream writer) throws IOException {
		
		meta.write(writer);
		
	}
	
	public void read(FileInputStream reader) throws IOException {
		
		meta.read(reader);
		
	}

	public int width() {
		
		return (int) meta.get(WIDTH_ENTRY);
		
	}

	public int height() {
		
		return (int) meta.get(HEIGHT_ENTRY);
		
	}
	
	public float[] position() {
		
		return (float[]) meta.get(POSITION_ENTRY);
		
	}
	
}

