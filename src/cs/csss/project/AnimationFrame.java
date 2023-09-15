package cs.csss.project;

import java.util.function.Supplier;

import cs.core.utils.CSRefInt;
import cs.csss.utils.FloatReference;

/**
 * Class for singlular animation frames
 */
public class AnimationFrame {
	
	final Artboard board;
	FloatReference time;
	CSRefInt frames;
	
	/**
	 * This is used in a similar way to the reference primitives above. If the owning animation changes its default swap type, we want
	 * animation frames that are 'default' to reflect that change.
	 */
	private Supplier<AnimationSwapType> swapTypeGetter;
	
	AnimationFrame(Artboard frame , CSRefInt frames , FloatReference time , Supplier<AnimationSwapType> swapType) {
		
		this.board = frame;
		this.time = time;
		this.swapTypeGetter = swapType;
		this.frames = frames;
		
	}
	
	/**
	 * Returns the name of the artboard belonging to this frame.
	 * 
	 * @return The name of the artboard belonging to this frame.
	 */
	public String artboardName() {
		
		return board.name;
		
	}
	
	/**
	 * Returns the time of this frame.
	 * 
	 * @return The time of this frame.
	 */
	public float time() {
		
		return time.get();
		
	}
	
	/**
	 * Returns the number of updates of this frame.
	 * 
	 * @return The number of updates of this frame.
	 */
	public int updates() {
		
		return frames.intValue();
		
	}
	
	/**
	 * Sets the time of this animation frame.
	 * 
	 * @param time — a new frame time
	 */
	public void time(FloatReference time) {
		
		this.time = time;
		
	}
	
	/**
	 * Sets the amount of updates this animation frame.
	 * 
	 * @param updates — a new amount of updates
	 */
	public void updates(CSRefInt updates) {
		
		this.frames = updates;
		
	}
	
	/**
	 * Returns the swap type of this animation frame.
	 * 
	 * @return Swap type of this animation frame.
	 */
	public AnimationSwapType swapType() {
		
		return swapTypeGetter.get();
		
	}

	/**
	 * Returns the artboard corresponding to this frame.
	 * 
	 * @return Artboard corresponding to this frame.
	 */
	public Artboard board() {
		
		return board;
		
	}
	
	void swapType(Supplier<AnimationSwapType> swapTypeGetter) {
		
		this.swapTypeGetter = swapTypeGetter;
		
	}
	
}
