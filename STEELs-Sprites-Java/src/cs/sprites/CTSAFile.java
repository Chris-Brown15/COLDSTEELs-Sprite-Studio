/**
 * Licenced under MIT.
 */
package cs.sprites;

import static cs.sprites.CSFileOperations.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class modeling the animation file exported by STEEL'S Sprite Studio. If this file was located in a jar file, a copy of the specification
 * for the {@code .ctsa} file can be found at the root of the jar file.
 * 
 * <p>
 * 	This class is used to instantiate animations. It is read from disk and passed to constructors of 
 * 	{@link cs.sprites.CSAnimation CSAnimation} objects. Instances of this can be kept in memory to recreate animations, but that is not 
 * 	necessary because {@code CSAnimation} provides means of creating deep copies of instances.
 * </p>
 * <p>
 * 	Currently, CTSA files only support animations that are a single horizontal row of frames of an image. In addition, these frames must go
 * 	from left to right. STEEL's Sprite Studio can produce these files through its animation system, but you are free to write them yourself 
 * 	using the constructs of this library.
 * </p>
 *  
 */
public class CTSAFile {

	public static final String FILE_EXTENSION = ".ctsa";
	
	private final String readFilePath;
	
	private String animationName;
	
	private int numberFrames;
	
	private float 
		leftU ,
		bottomV ,
		topV ,
		widthU;
	
	private CSFrameChunk[] frames;
	
	/**
	 * Prepares the resulting instance for a call to {@link CTSAFile#read read}, which will read a corresponding file from disk and parse 
	 * it. Note that the correctness of the given filepath is only evaluated at the invokation to {@code read} and only if assertions are
	 * enabled.
	 * 
	 * @param filepath — file path to read from.
	 */
	public CTSAFile(String filepath) {
		
		this.readFilePath = filepath;
			
	}

	/**
	 * Reads a ctsp file from disk and loads its data into the calling instance. 
	 * 
	 * @throws IOException if the {@code FileInputStream} used throws an exception.
	 */
	public void read() throws IOException {
		
		assert Files.exists(Paths.get(readFilePath)) : readFilePath + " does not name a file path.";		
		assert readFilePath.endsWith(FILE_EXTENSION) : readFilePath + " does not have the .ctsa file path.";
		
		try(FileInputStream reader = new FileInputStream(readFilePath)) {
			
			animationName = getString(reader);
			
			setByteOrder(ByteOrder.nativeOrder());
			
			numberFrames = getInt(reader);
			leftU = getFloat(reader);
			bottomV = getFloat(reader);
			topV = getFloat(reader);
			widthU = getFloat(reader);
			frames = getFrameChunks(numberFrames , reader);
			
		} finally {
			
			setByteOrder(ByteOrder.BIG_ENDIAN);
			
		}
		
	}
	
	/**
	 * Helper for reading frame chunks from disk.
	 * 
	 * @param numberChunks — number of chunks to expect
	 * @param reader — file input stream reader
	 * @return Array of {@code FrameChunk}
	 * @throws IOException if the {@code FileInputStream} used throws an exception.
	 */
	private CSFrameChunk[] getFrameChunks(int numberChunks , FileInputStream reader) throws IOException {
		
		CSFrameChunk[] chunks = new CSFrameChunk[numberChunks];		
		for(int i = 0 ; i < numberChunks ; i++) chunks[i] = new CSFrameChunk(getFloat(reader) , getInt(reader) , getByte(reader));		
		return chunks;
		
	}
		
	/**
	 * Returns the number of frames of the animation modeled by this file.
	 * 
	 * @return Number of animation frames of the animation modeled by this file.
	 */
	public int numberFrames() {
	
		return numberFrames;
		
	}
	
	/**
	 * Returns the starting left U coordinate of the animation modeled by this file.
	 * 
	 * @return Starting left U coordinate of the animation modeled by this file.
	 */
	public float leftU() {
		
		return leftU;
		
	}

	/**
	 * Returns the bottom V coordinate of the animation modeled by this file.
	 * 
	 * @return Bottom V coordinate of the animation modeled by this file.
	 */
	public float bottomV() {
		
		return bottomV;
		
	}

	/**
	 * Returns the top V coordinate of the animation modeled by this file.
	 * 
	 * @return Top V coordinate of the animation modeled by this file.
	 */
	public float topV() {
		
		return topV;
		
	}

	/**
	 * Returns the U-wise width of frames of the animation modeled by this file.
	 * 
	 * @return Width in U coordinate space of frames of the animation modeled by this file.
	 */
	public float widthU() {
		
		return widthU;
		
	}

	/**
	 * Returns the array of animation frame chunks read from disk by this instance.
	 * 
	 * @return Array of frame chunks.
	 */
	public CSFrameChunk[] frames() {
		
		return frames;
		
	}

	/**
	 * Returns the name of the animation modeled by this file.
	 * 
	 * @return The name of the animation modeled by this file.
	 */
	public String animationName() {
		
		return animationName;
		
	}

}
