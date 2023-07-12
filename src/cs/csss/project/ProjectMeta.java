package cs.csss.project;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cs.csss.misc.files.FileComposition;

public class ProjectMeta {

	private static final String
		TITLE_ENTRY = "Title" ,
		CHANNELS_PER_PIXEL_ENTRY = "Channels Per Pixel" ,
		PALETTED_ENTRY = "Paletted" ,
		NONVISUAL_LAYER_SIZES_ENTRY = "Nonvisual Layer Sizes",
		NONVISUAL_LAYER_NAMES_ENTRY = "Nonvisual Layer Names",
		VISUAL_LAYER_NAMES_ENTRY = "Visual Layer Names",
		ANIMATIONS_ENTRY = "Animations"
	;
	
	private final FileComposition file = new FileComposition()
		.addString(TITLE_ENTRY)
		.addByte(CHANNELS_PER_PIXEL_ENTRY)
		.addBoolean(PALETTED_ENTRY)
		.addByteArray(NONVISUAL_LAYER_SIZES_ENTRY)
		.addStringArray(NONVISUAL_LAYER_NAMES_ENTRY)
		.addStringArray(VISUAL_LAYER_NAMES_ENTRY)
		.addStringArray(ANIMATIONS_ENTRY)			
	;
		
	public ProjectMeta() {

	}
	
	ProjectMeta bindName(String name) {
		
		file.bindString(TITLE_ENTRY, name);
		return this;
		
	}
	
	ProjectMeta bindChannelsPerPixel(int channels) {
		
		file.bindByte(CHANNELS_PER_PIXEL_ENTRY, (byte)channels);
		return this;
		
	}
	
	ProjectMeta bindPaletted(boolean paletted) {
		
		file.bindBoolean(PALETTED_ENTRY	, paletted);
		return this;
		
	}

	ProjectMeta bindNonVisualLayerSizes(byte[] sizes) {
		
		file.bindByteArray(NONVISUAL_LAYER_SIZES_ENTRY , sizes);
		return this;
		
	}

	ProjectMeta bindNonVisualLayerNames(String[] names) {
		
		file.bindStringArray(NONVISUAL_LAYER_NAMES_ENTRY , names);
		return this;
		
	}

	ProjectMeta bindVisualLayerNames(String[] names) {
		
		file.bindStringArray(VISUAL_LAYER_NAMES_ENTRY , names);
		return this;
		
	}

	ProjectMeta bindAnimations(String[] animations) {
		
		file.bindStringArray(ANIMATIONS_ENTRY, animations);
		return this;
		
	}
	
	void write(FileOutputStream writer) throws IOException {
		
		file.write(writer);
		
	}
	
	public void read(FileInputStream reader) throws IOException {
		
		file.read(reader);
		
	}
	
	public String name() {
		
		return (String) file.get(TITLE_ENTRY);
		
	}
	
	public int channelsPerPixel() {
		
		return (byte) file.get(CHANNELS_PER_PIXEL_ENTRY);
		
	}
	
	public boolean paletted() {
		
		return (boolean) file.get(PALETTED_ENTRY);
		
	}
	
	public byte[] nonVisualLayerSizes() {
		
		return (byte[]) file.get(NONVISUAL_LAYER_SIZES_ENTRY);
		
	}
	
	public String[] nonVisualLayerNames() {
		
		return (String[]) file.get(NONVISUAL_LAYER_NAMES_ENTRY);
		
	}

	public String[] visualLayerNames() {
		
		return (String[]) file.get(VISUAL_LAYER_NAMES_ENTRY);
		
	}
	
	public String[] animations() {
		
		return (String[]) file.get(ANIMATIONS_ENTRY);
		
	}
	
}
