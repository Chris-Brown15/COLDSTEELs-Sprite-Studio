package cs.csss.project;

import java.util.function.Supplier;

import cs.core.utils.CSRefInt;
import cs.csss.utils.FloatReference;

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
	
	public void time(FloatReference time) {
		
		this.time = time;
		
	}
	
	public AnimationSwapType swapType() {
		
		return swapTypeGetter.get();
		
	}

	void swapType(Supplier<AnimationSwapType> swapTypeGetter) {
		
		this.swapTypeGetter = swapTypeGetter;
		
	}
	
}
