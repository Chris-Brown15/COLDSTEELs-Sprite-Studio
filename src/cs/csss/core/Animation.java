package cs.csss.core;

import static cs.core.utils.CSUtils.specify;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import cs.core.utils.FloatConsumer;
import cs.core.utils.FloatSupplier;
import cs.csss.artboard.Artboard;
import cs.csss.misc.files.FileComposition;

public class Animation {
	
	public static boolean isValidAnimationName(final String prospectiveName) {
		
		return !(
			prospectiveName.equals("") ||
			prospectiveName.contains("0") || 
			prospectiveName.contains("1") || 
			prospectiveName.contains("2") || 
			prospectiveName.contains("3") || 
			prospectiveName.contains("4") || 
			prospectiveName.contains("5") || 
			prospectiveName.contains("6") || 
			prospectiveName.contains("7") || 
			prospectiveName.contains("8") || 
			prospectiveName.contains("9") || 
			prospectiveName.contains("&") ||
			prospectiveName.contains("_")
		);
		
	}
	
	private final LinkedList<Artboard> frames = new LinkedList<>();
	private final LinkedList<NonDefaultFrameTime> nonDefaultFrames = new LinkedList<>();
	
	private float frameTime;

	public final FloatSupplier getFrameTime = () -> frameTime;
	public final FloatConsumer setFrameTime = newDefaultFrameTime -> frameTime = newDefaultFrameTime;
	
	private String name;
	
	private final CSSSProject project;
			
	Animation(final String name , CSSSProject project) {
	
		this.name = name;	
		this.project = project;
		
	}

	public void appendArtboard(final Artboard artboard) {
		
		frames.add(artboard);
		
	}
	
	public void putArtboard(final Artboard artboard, final int index) {
		
		validateIndex(index);
		frames.add(index, artboard);
		
	}
	
	public void popArtboard() {
		
		frames.removeLast();
		
	}
	
	public void removeArtboard(int index) {
		
		validateIndex(index);
		frames.remove(index);
		
	}
	
	public void setFrameAsNonDefault(int index , final float newFrameTime) {
		
		validateIndex(index);
		nonDefaultFrames.add(new NonDefaultFrameTime(frames.get(index) , newFrameTime));
		
	}
	
	public void setNonDefaultFrameTime(int index , final float newFrameTime) {
		
		validateIndexToNonDefault(index);		
		nonDefaultFrames.remove(index);
		nonDefaultFrames.add(index, new NonDefaultFrameTime(frames.get(index), newFrameTime));
		
	}

	public float getNonDefaultFrameTime(int index) {
		
		validateIndexToNonDefault(index);
		return nonDefaultFrames.get(index).time();
		
	}
	
	public Artboard getNonDefaultArtboardAt(final int index) {
		
		validateIndexToNonDefault(index);
		return nonDefaultFrames.get(index).frame();
		
	}
	
	private void validateIndex(final int index) {
		
		specify(index >= 0 && index < frames.size() , index + " is an out of bounds index.");
		
	}
	
	private void validateIndexToNonDefault(final int index) {
		
		specify(index >= 0 && index < nonDefaultFrames.size() , index + " is an out of bounds index.");
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	/**
	 * Creates an animation directory and writes a metadata file to it given the project directory.
	 * 
	 * @param projectAbsPath — absolute path to the directory representing the project this animation belongs to
	 * @throws IOException if a failure occurs in any of the IO operations of this method.
	 */
	public void save(final String projectAbsPath) throws IOException {
		
		Path animPath = Paths.get(projectAbsPath + "/" + name);	
		Files.createDirectory(animPath);
		writeMeta(animPath);
		
	}

	private void writeMeta(Path animationDirectory) throws FileNotFoundException, IOException {
		
		//frameIDs represents the list of artboards from the project that are in the animation. The element at each index of the array is
		//the index of the artboard to play at that index.
		
		//nonDefaultIDs contains the IDs of the artboards who are have non default frame times 
		
		int[] 
			frameIDs = new int[frames.size()] ,
			nonDefaultIDs = new int[nonDefaultFrames.size()]
		;
		
		//contains the times of artboards whose times are nondefault
		
		float[] nonDefaultTimes = new float[nonDefaultFrames.size()];
		
		int 
			i = 0 ,
			j = 0
		;
		
		for(var x : frames) {
			
			int frameID = project.getArtboardIndex(x);
			frameIDs[i++] = frameID;			
			
			for(var y : nonDefaultFrames) if(y.frame == x) { 
				
				nonDefaultIDs[j] = frameID;
				nonDefaultTimes[j++] = y.time;
				
			}
						
		}
		
		FileComposition file = new FileComposition()
			.addString("name" , name)
			.addFloat("frame time" , frameTime)
			.addIntArray("frames" , frameIDs)
			.addIntArray("non default frames" , nonDefaultIDs)
			.addFloatArray("non default times" , nonDefaultTimes)
		;
		
		try(FileOutputStream writer = new FileOutputStream(animationDirectory + "/" + name + ".csssameta")) {
			
			file.write(writer);
			
		}
		
	}
	
	private record NonDefaultFrameTime(Artboard frame , float time) {}
	
}
