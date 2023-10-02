package cs.csss.project.io;

import static cs.core.utils.CSUtils.specify;

import static org.lwjgl.system.MemoryUtil.memFree;

import static cs.csss.misc.files.FileOperations.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import cs.core.utils.CSRefInt;
import cs.csss.engine.Logging;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.project.AnimationFrame;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.project.Layer;
import cs.csss.project.NonVisualLayer;
import cs.csss.project.VisualLayer;

/**
 * 
 * Class representation of a .ctsp file. 
 * <p>
 * 	A CTSP file is a file containing all needed information to save and load a project's state. The following data are saved:
 *	<ul>
 *		<li>
 *			Project Data
 *		</li>
 *		<li>
 *			Palettes
 *		</li>
 *		<li>
 *			Layers
 *		</li>
 *		<li>
 *			Artboards
 *		</li>
 *		<li>
 *			Animations
 *		</li>
 *	</ul>
 * </p>
 * 
 * @author Chris Brown
 * 
 */
public class CTSPFile {
	
	private static final CSFolder projectRoot = CSFolder.getRoot("data").getOrCreateSubdirectory("projects");
	
	public static final String FILE_EXTENSION = ".ctsp";

	/**
	 * name of a ctsp file + FILE_EXTENSION
	 */
	private final String fullFileName;
	
	/**
	 * Project to write, only present when the write constructor was called.
	 */
	private final CSSSProject project; 
	
	/*
	 * Data read from a file, available upon reading a ctsp file.
	 */
	
	private String projectName;
	private byte channels;
	private int
		numberSourceArtboards ,
		numberVisualLayers ,
		numberNonVisualLayers ,
		numberAnimations;
	
	private PaletteChunk[] palettes;
	private String[] visualLayerNames;
	private NonVisualLayerChunk[] nonvisualLayers;
	private ArtboardChunk[] artboards;
	private AnimationChunk[] animations;	
	
	/**
	 * Write constructor for a ctsp file.
	 * 
	 * @param project — a project to save
	 * @param saveAs — a name for the file to save
	 */
	public CTSPFile(CSSSProject project , String saveAs) {
		
		this.project = project;
		this.fullFileName = saveAs + FILE_EXTENSION;
		
		
	}
	
	/**
	 * Read constructor for a ctsp file.
	 * 
	 * @param fileName — a name for the file to save
	 */ 
	public CTSPFile(String fileName) {
		
		project = null;
		fullFileName = fileName;
		
	}
	
	/**
	 * Writes the contents of a project to disk.
	 * 
	 * @throws IOException if an exception occurs in writing.
	 */
	public void write() throws IOException {
		
		CSFile.makeFile(projectRoot, fullFileName);
		
		try(FileOutputStream writer = new FileOutputStream(projectRoot.getRealPath() + CSFolder.separator + fullFileName)) {
			
			putString(fullFileName.substring(0, fullFileName.indexOf(FILE_EXTENSION)) , writer);
			putByte((byte) project.channelsPerPixel() , writer);
			putInt(project.getNumberNonCopiedArtboards() , writer);
			putInt(project.numberVisualLayers() , writer);
			putInt(project.numberNonVisualLayers() , writer);
			putInt(project.numberAnimations() , writer);
			writePaletteChunks(writer);
			putStringArray(visualLayerNamesToArray() , writer);
			writeNonVisualLayers(writer);
			writeArtboardChunks(writer);
			writeAnimationChunks(writer);
			
		}
		
		Logging.sysDebug("File Write Complete");
		
	}
	
	/**
	 * Reads the contents of a project file from disk, storing the resulting data in {@code this}.
	 * 
	 * @throws IOException if an exception occurs in reading.
	 */
	public void read() throws IOException {
		
		specify(Files.exists(Paths.get(projectRoot.getRealPath() + CSFolder.separator + fullFileName)) , "File does not exist.");
		
		try(FileInputStream reader = new FileInputStream(projectRoot.getRealPath() + CSFolder.separator + fullFileName)) {
			
			projectName = getString(reader);
			channels = getByte(reader);
			numberSourceArtboards = getInt(reader);
			numberVisualLayers = getInt(reader);
			numberNonVisualLayers = getInt(reader);
			numberAnimations = getInt(reader);
			palettes = readPaletteChunks(reader);
			
			visualLayerNames = getStringArray(reader);
			nonvisualLayers = readNonVisualLayers(numberNonVisualLayers, reader);
			artboards = readArtboards(numberSourceArtboards , numberVisualLayers , numberNonVisualLayers , reader);
			animations = readAnimationChunks(numberAnimations , reader);
			
			
		}
		
		Logging.sysDebug("File Read Complete");
		
	}

	/* helper methods */
	
	private void writePaletteChunks(FileOutputStream writer) throws IOException {
		
		writePalette(project.visualPalette() , writer);
		writePalette(project.getNonVisualPaletteBySize(1) , writer);
		writePalette(project.getNonVisualPaletteBySize(2) , writer);
		writePalette(project.getNonVisualPaletteBySize(3) , writer);
		writePalette(project.getNonVisualPaletteBySize(4) , writer);
		
	}
	
	private void writePalette(ArtboardPalette palette , FileOutputStream writer) throws IOException {
		
		int 
			width = palette.width() ,
			height = palette.height() ,
			col = palette.currentCol() ,
			row = palette.currentRow() ,
			channels = palette.channelsPerPixel();
		
		putInt(width , writer);
		putInt(height , writer);
		putByte((byte)channels , writer);
		
		ByteBuffer texels = palette.texelData();
		
		int used = (row * width * channels) + (col * channels);
		
		writeSize(writer , used);
		for(int i = 0 ; i < used ; i++) putByte(texels.get(i) , writer);
		
	}
	
	private PaletteChunk[] readPaletteChunks(FileInputStream reader) throws IOException {
		
		return new PaletteChunk[] {
			readPaletteChunk(reader) , 
			readPaletteChunk(reader) , 
			readPaletteChunk(reader) , 
			readPaletteChunk(reader) , 
			readPaletteChunk(reader)
		};
		
	}
	
	private PaletteChunk readPaletteChunk(FileInputStream reader) throws IOException {
		
		return new PaletteChunk(getInt(reader) , getInt(reader) , getByte(reader) , getByteArray(reader));
		
	}
	
	private String[] visualLayerNamesToArray() {
		
		String[] names = new String[project.numberVisualLayers()];
		CSRefInt i = new CSRefInt(0);
		project.forEachVisualLayerPrototype(prototype -> {
			
			names[i.intValue()] = prototype.name();
			i.inc();
			
		});
		
		return names;
		
	}
	
	private void writeNonVisualLayers(FileOutputStream writer) {

		project.forEachNonVisualLayerPrototype(prototype -> {
			
			try {
				
				putString(prototype.name() , writer);
				putByte((byte) prototype.sizeBytes() , writer);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				throw new IllegalStateException();
				
			}
			
		});
				
	}
	
	private NonVisualLayerChunk[] readNonVisualLayers(int numberNonVisualLayers , FileInputStream reader) throws IOException {
		
		NonVisualLayerChunk[] nvls = new NonVisualLayerChunk[numberNonVisualLayers];		
		for(int i = 0 ; i < numberNonVisualLayers ; i++) nvls[i] = new NonVisualLayerChunk(getString(reader) , getByte(reader));		
		return nvls;
		
	}

	private void writeArtboardChunks(FileOutputStream writer) {
		
		project.forEachNonShallowCopiedArtboard(artboard -> {
			
			try {
			
				putString(artboard.name , writer);
				putInt(artboard.width() , writer);
				putInt(artboard.height() , writer);
				putInt(activeLayerIndex(artboard) , writer);
				putBoolean(artboard.isActiveLayerVisual() , writer);
				writeVisualLayerChunks(artboard , writer);
				writeNonVisualLayerChunks(artboard , writer);
				
			} catch(IOException e) {
				
				e.printStackTrace();
				throw new IllegalStateException();
				
			}
			
		});
		
	}

	private void writeVisualLayerChunks(Artboard artboard , FileOutputStream writer) throws IOException {
		
		var iter = artboard.visualLayers();
		while(iter.hasNext()) {
			
			VisualLayer x = iter.next();
			putString(x.name , writer);
			putBoolean(x.locked() , writer);
			putBoolean(x.hiding() , writer);
			writeLayerPixelData(x, writer);			
			
		}
		
	}

	private void writeNonVisualLayerChunks(Artboard artboard , FileOutputStream writer) throws IOException {
		
		var iter = artboard.nonVisualLayers();
		while(iter.hasNext()) {
			
			NonVisualLayer x = iter.next();
			
			putString(x.name , writer);
			putBoolean(x.locked() , writer);
			putBoolean(x.hiding() , writer);
			putByte((byte) x.bytesPerPixel() , writer); 
			writeLayerPixelData(x , writer);
			
		}
		
	}
	
	private void writeLayerPixelData(Layer layer , FileOutputStream writer) throws IOException {

		ByteBuffer layerPixelBuffer = layer.encode();
		if(layerPixelBuffer.limit() == 0) { 
			
			memFree(layerPixelBuffer);
			layerPixelBuffer = layer.toByteBuffer();
			putBoolean(false , writer);
			
		} else putBoolean(true , writer);

		writeSize(writer , layerPixelBuffer.limit());
		for(int i = 0 ; i < layerPixelBuffer.limit() ; i++) putByte(layerPixelBuffer.get(i) , writer);
		
		memFree(layerPixelBuffer);
		
	}
	
	private ArtboardChunk[] readArtboards(
		int numberArtboards , 
		int numberVisualLayers , 
		int numberNonVisualLayers , 
		FileInputStream reader
	) throws IOException {
		
		ArtboardChunk[] reads = new ArtboardChunk[numberArtboards];
		
		for(int i = 0 ; i < numberArtboards ; i++) reads[i] = new ArtboardChunk(
			getString(reader) ,
			getInt(reader) ,
			getInt(reader) ,
			getInt(reader) ,
			getBoolean(reader) ,
			readVisualLayerChunks(numberVisualLayers , reader) ,
			readNonVisualLayerChunks(numberNonVisualLayers , reader)
		);
		
		return reads;
		
	}
	
	private VisualLayerDataChunk[] readVisualLayerChunks(int numberVisualLayers , FileInputStream reader) throws IOException {
	
		VisualLayerDataChunk[] chunks = new VisualLayerDataChunk[numberVisualLayers];
		
		for(int i = 0 ; i < numberVisualLayers ; i ++) chunks[i] = new VisualLayerDataChunk(
			getString(reader) ,
			getBoolean(reader) ,
			getBoolean(reader) ,
			getBoolean(reader) ,
			getByteArray(reader)
		);
		
		return chunks;
		
	}
	
	private NonVisualLayerDataChunk[] readNonVisualLayerChunks(int numberNonVisualLayers , FileInputStream reader) throws IOException {

		NonVisualLayerDataChunk[] chunks = new NonVisualLayerDataChunk[numberNonVisualLayers];
		
		for(int i = 0 ; i < numberNonVisualLayers ; i ++) chunks[i] = new NonVisualLayerDataChunk(
			getString(reader) ,
			getBoolean(reader) ,
			getBoolean(reader) ,
			getByte(reader) ,
			getBoolean(reader) ,
			getByteArray(reader)
		);
		
		return chunks;
		
	}
	
	private int activeLayerIndex(Artboard artboard) {
		
		if(artboard.isActiveLayerVisual()) {
			
			var iter = artboard.visualLayers();
			int i = 0;
			while(iter.hasNext()) { 
				
				if(artboard.isActiveLayer(iter.next())) return i;
				i++;
				
			}
			
		} else {

			var iter = artboard.nonVisualLayers();
			int i = 0;
			while(iter.hasNext()) { 
				
				if(artboard.isActiveLayer(iter.next())) return i;
				i++;
				
			}
			
		}	
		
		throw new IllegalStateException("No layer is active");
		
	}
	
	private void writeAnimationChunks(FileOutputStream writer) {
		
		project.forEachAnimation(animation -> {
			
			try {
				
				int numberFrames = animation.numberFrames();
				
				putString(animation.name() , writer);
				putInt(numberFrames , writer);
				putFloat(animation.getFrameTime.getAsFloat() , writer);
				putInt(animation.getUpdates.getAsInt() , writer);
				putString(animation.defaultSwapType().name() , writer);
				
				for(int i = 0 ; i < numberFrames ; i++) writeAnimationFrameChunk(animation.getFrame(i) , writer);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				throw new IllegalStateException(e);
								
			}
			
			
		});
		
	}
	
	private void writeAnimationFrameChunk(AnimationFrame frame , FileOutputStream writer) throws IOException {
		
		putString(frame.artboardName() , writer);
		putFloat(frame.time() , writer);
		putInt(frame.updates() , writer);
		putString(frame.swapType().name() , writer);
		
	}
	
	private AnimationChunk[] readAnimationChunks(int numberAnimations , FileInputStream reader) throws IOException {
		
		AnimationChunk[] animations = new AnimationChunk[numberAnimations];
		for(int i = 0 ; i < animations.length ; i++) {
			
			int numberFrames;
			
			animations[i] = new AnimationChunk(
				getString(reader) ,
			 	numberFrames = getInt(reader) ,
				getFloat(reader) ,
				getInt(reader) ,
				getString(reader) ,
				readAnimationFrameChunks(numberFrames , reader)
			);
			
		}
		
		return animations;
		
	}
	
	private AnimationFrameChunk[] readAnimationFrameChunks(int numberFrames , FileInputStream reader) throws IOException {
		
		AnimationFrameChunk[] frames = new AnimationFrameChunk[numberFrames];
		
		for(int i  = 0 ; i < frames.length ; i++) {
			
			frames[i] = new AnimationFrameChunk(getString(reader) , getFloat(reader) , getInt(reader) , getString(reader));
			
		}
		
		return frames;		
		
	}
	
	/* accessor methods for file data */
	
	/**
	 * Returns the name of the loaded project.
	 * 
	 * @return Name of the loaded project.
	 */
	public String name() {

		verifyReadValid();
		return projectName;
		
	}
	
	/**
	 * Returns the channels per pixel of the loaded project.
	 * 
	 * @return Channels per pixel of the loaded project.
	 */
	public byte channelsPerPixel() {

		verifyReadValid();
		return channels;
		
	}
	
	/**
	 * Returns the palette chunks of the loaded project.
	 * 
	 * @return Palette chunks of the loaded project.
	 */
	public PaletteChunk[] paletteChunks() {

		verifyReadValid();
		return palettes;
		
	}

	/**
	 * Returns the artboard chunks of the loaded project.
	 * 
	 * @return Artboard chunks of the loaded project.
	 */
	public ArtboardChunk[] artboardChunks() {

		verifyReadValid();
		return artboards;
		
	}

	/**
	 * Returns the animation chunks of the loaded project.
	 * 
	 * @return Animation chunks of the loaded project.
	 */
	public AnimationChunk[] animationChunks() {

		verifyReadValid();
		return animations;
		
	}
	
	/**
	 * Returns the names of visual layers of the loaded project.
	 * 
	 * @return Names of visual layers of the loaded project.
	 */
	public String[] visualLayerNames() {

		verifyReadValid();
		return visualLayerNames;
		
	}

	/**
	 * Returns the nonvisual layer chunks of the loaded project.
	 * 
	 * @return Nonvisual layer chunks of the loaded project.
	 */
	public NonVisualLayerChunk[] nonVisualLayerChunks() {

		verifyReadValid();
		return nonvisualLayers;
		
	}
	
	private void verifyReadValid() {

		specify(project == null && projectName != null , "Read constructor was not called or read() was not called");
		
	}

	/**
	 * Contains loaded metadata from a nonvisual layer chunk.
	 */
	public record NonVisualLayerChunk(
		String name , 
		int size
	) {}

	/**
	 * Contains loaded data from an artboard chunk.
	 */
	public record ArtboardChunk(
		String name , 
		int width , 
		int height , 
		int activeLayerIndex , 
		boolean isActiveLayerVisual , 
		VisualLayerDataChunk[] visualLayers , 
		NonVisualLayerDataChunk[] nonVisualLayers
	) {}

	/**
	 * Contains loaded data from a visual layer chunk.
	 */
	public record VisualLayerDataChunk(
		String name , 
		boolean locked , 
		boolean hiding , 
		boolean isCompressed , 
		byte[] pixelData
	) {}

	/**
	 * Contains loaded data from a nonvisual layer chunk.
	 */
	public record NonVisualLayerDataChunk(
		String name , 
		boolean locked , 
		boolean hiding , 
		byte bytesPerPixel, 
		boolean isCompressed , 
		byte[] pixelData 
	) {}

	/**
	 * Contains loaded data from a palette chunk.
	 */
	public record PaletteChunk(
		int width , 
		int height , 
		byte channels , 
		byte[] pixelData
	) {}

	/**
	 * Contains loaded data from an animation chunk.
	 */
	public record AnimationChunk(
		String name , 
		int numberFrames , 
		float defaultSwapTime , 
		int defaultUpdates , 
		String defaultSwapType , 
		AnimationFrameChunk[] frames
	) {}

	/**
	 * Contains loaded data from an animation frame chunk.
	 */
	public record AnimationFrameChunk(
		String artboardName ,
		float frameTime ,
		int frameUpdates ,
		String swapType
	) {}
	
}