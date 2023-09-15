package cs.csss.project.io;

import static cs.csss.misc.files.FileOperations.*;

import static cs.core.utils.CSUtils.specify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

import cs.csss.misc.files.CSFolder;
import cs.csss.project.Animation;
import cs.csss.project.CSSSProject;

/**
 * Class modeling the animation file exported by Sprite Studio.
 * Follows a specifed format found via pdf at the root archive of this application
 */
class CTSAFile {

	public static final String FILE_EXTENSION = ".ctsa";
	
	private final Animation animation;
	private final CSSSProject project;

	private final String readFilePath;
	
	private String animationName;
	
	private int numberFrames;
	private float 
		leftU ,
		bottomV ,
		topV ,
		widthU;
	
	private FrameChunk[] frames;
	
	/**
	 * Write constructor for a {@code .ctsa} file. 
	 * 
	 * @param animation — an animation to write
	 * @param project — a project owning {@code animation}
	 */
	public CTSAFile(Animation animation , CSSSProject project) {
		
		this.animation = animation;
		this.project = project;
		readFilePath = null;
		
	}

	/**
	 * Read constructor for a {@code .ctsa} file. 
	 * 
	 * @param filepath — filepath for reading
	 */
	public CTSAFile(String filepath) {
		
		this.readFilePath = filepath;
		animation = null;
		project = null;
				
	}
	
	void write(String filepath) throws IOException {
		
		StringBuilder append = new StringBuilder();				
		if(!filepath.endsWith(CSFolder.separator)) append.append(CSFolder.separator);
		append.append(animation.name() + FILE_EXTENSION);
		
		filepath += append.toString();
		
		File newFile = new File(filepath);
		var sizes = project.getProjectSizeAndPositions();
		
		try(FileOutputStream writer = new FileOutputStream(newFile)) {
			
			putString(animation.name() , writer);
					
			setByteOrder(ByteOrder.nativeOrder());
			
			putInt(animation.numberFrames() , writer);
			putFloat(animation.leftU(sizes.leftmostX() , sizes.rightmostX()) , writer);
			putFloat(animation.bottomV(sizes.lowermostY() , sizes.uppermostY()) , writer);
			putFloat(animation.topV(sizes.lowermostY() , sizes.uppermostY()) , writer);
			putFloat(animation.widthU(sizes.leftmostX(), sizes.rightmostX()) , writer);
			putAnimationChunks(writer);
			
		} finally {
			
			setByteOrder(ByteOrder.BIG_ENDIAN);
			
		}
		
	}
	
	void read() throws IOException {
		
		specify(Files.exists(Paths.get(readFilePath)) , readFilePath + " does not name a file path.");
		specify(readFilePath.endsWith(FILE_EXTENSION) , readFilePath + " does not have the .ctsa file path.");
		
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
	
	private void putAnimationChunks(FileOutputStream writer) throws IOException{
		
		animation.forAllFrames(frame -> {
			
			putFloat(frame.time() , writer);
			putInt(frame.updates() , writer);
			putByte(frame.swapType().byteValue , writer);
			
		});
		
	}
	
	private FrameChunk[] getFrameChunks(int numberChunks , FileInputStream reader) throws IOException {
		
		FrameChunk[] chunks = new FrameChunk[numberChunks];		
		for(int i = 0 ; i < numberChunks ; i++) chunks[i] = new FrameChunk(getFloat(reader) , getInt(reader) , getByte(reader));		
		return chunks;
		
	}
		
	int getNumberFrames() {
	
		return numberFrames;
		
	}

	float getLeftU() {
		
		return leftU;
		
	}

	float getBottomV() {
		
		return bottomV;
		
	}

	float getTopV() {
		
		return topV;
		
	}

	float getWidthU() {
		
		return widthU;
		
	}

	FrameChunk[] getFrames() {
		
		return frames;
		
	}

	/**
	 * @return the animationname.
	 */
	String getAnimationName() {
		
		return animationName;
		
	}

	record FrameChunk(float time , int updates , byte swapType) {}
	
}
