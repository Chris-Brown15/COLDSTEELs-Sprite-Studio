/**
 * Licenced under MIT.
 */
package cs.sprites;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * Animation class offering methods and data to animate sprite sheets.
 * 
 * @author Chris Brown
 */
public class CSAnimation implements Comparable<CSAnimation> , Cloneable {

	/**
	 * Name of this animation.
	 */
	public final String name;
	
	/**
	 * Constant information about this animation's UVs.
	 */
	public final float 
		leftU ,
		bottomV ,
		topV ,
		widthU;

	/**
	 * Width and height in pixels of a single animation frame of this animation. 
	 */
	public final float 
		frameWidthPixels ,
		frameHeightPixels;
	
	/*
	 * Animation timing mechanisms
	 */
	
	private final CSTimer frameTimer = new CSTimer();

	/**
	 * Unmodifiable list of animation frames.
	 */
	public final CSFrameSet frames;
	
	private volatile int 
		currentFrame = 0 ,
		updates = 0;
	
	private volatile float
		currentLeftU,
		currentRightU;
	
	/**
	 * Abstract method to invoke when an update complets.
	 */
	private volatile Runnable onUpdate = null;
	private volatile FloatBiConsumer onUpdateReceiveUs = null;
	
	/**
	 * Creates an animation object from an animation file. This constructor requires the image's source width and height in pixels.
	 * 
	 * @param source — a {@code CTSAFile} containing animation data to pull from
	 * @param sourceImageWidth — the width in pixels of the image to which this animation corresponds
	 * @param sourceImageHeight — the height in pixels of the image to which this animation corresponds
	 */
	public CSAnimation(CTSAFile source , int sourceImageWidth , int sourceImageHeight) {

		frames = new CSFrameSet(source.frames());
		this.name = source.animationName();
		this.leftU = source.leftU();
		this.bottomV = source.bottomV();
		this.topV = source.topV();
		this.widthU = source.widthU();
		
		this.frameWidthPixels = (int) (sourceImageWidth * widthU);
		this.frameHeightPixels = (int) (sourceImageHeight * (topV - bottomV));
		
		currentLeftU = leftU;
		currentRightU = leftU + widthU;
		
	}
	
	protected CSAnimation(CSAnimation source) {
		
		frames = source.frames.clone();
		this.name = source.name;
		this.leftU = source.leftU;    
		this.bottomV = source.bottomV;
		this.topV = source.topV;      
		this.widthU = source.widthU;
		this.frameWidthPixels = source.frameWidthPixels;
		this.frameHeightPixels = source.frameHeightPixels;

		currentLeftU = leftU;
		currentRightU = leftU + widthU;
		
	}
	
	/**
	 * Updates the states of timing mechanisms and moves to the next frame if appropriate.
	 */
	public synchronized void updateAnimation() {
		
		CSAnimationFrame current = current();
		
		boolean goToNextFrame = false;
		
		if(current.swapType() == CSAnimationFrame.SWAP_BY_TIME) { 
				
			//if true, we can move to the next animation frame
			if(frameTimer.elapsed() >= current.time()) goToNextFrame = true;
			
		} else if(current.swapType() == CSAnimationFrame.SWAP_BY_UPDATES) {
			
			//increment our timer integer 
			updates++;
			//if true we can go to the next frame
			if(updates == current.updates()) goToNextFrame = true;
						
		}		
		
		//resets all timers and moves to the next frame
		if(goToNextFrame) {

			bumpCurrentFrame();
			bumpUs();
			frameTimer.start();
			updates = 0;
			
		}
		
	}

	/**
	 * 
	 * Calculates and returns the total time in milliseconds this animation <i>should</i> take to complete. The resulting time from this 
	 * method is based on the assumption that every frame will take exactly {@code millisecondsPerUpdate} to complete.
	 * 
	 * @param millisecondsPerUpdate — expected number of milliseconds a single program frame should take
	 * @return Amount of milliseconds this animation should take to complete.
	 */
	public double getAnimationTotalMilliseconds(double millisecondsPerUpdate) {
		
		double timeAccum = 0;
		
		for(CSAnimationFrame x : frames) {
			
			if(x.swapType() == CSAnimationFrame.SWAP_BY_TIME) timeAccum += x.time();
			else timeAccum += (x.updates() * millisecondsPerUpdate);
			
		}
		
		return timeAccum;
		
	}
	
	/**
	 * Retrieve the UV coordinates of the current frame via {@code FloatConsumer}s.
	 * 
	 * @param leftUConsumer — {@code FloatConsumer} who recieves the left U coordinate of the current animation
	 * @param rightUConsumer — {@code FloatConsumer} who recieves the right U coordinate of the current animation
	 * @param bottomVConsumer — {@code FloatConsumer} who recieves the bottom V coordinate of the current animation
	 * @param topVConsumer — {@code FloatConsumer} who recieves the top V coordinate of the current animation
	 */
	public void getFrameUVs(
		FloatConsumer leftUConsumer , 
		FloatConsumer rightUConsumer , 
		FloatConsumer bottomVConsumer , 
		FloatConsumer topVConsumer
	) {
		
		leftUConsumer.accept(currentLeftU);
		rightUConsumer.accept(currentRightU);
		bottomVConsumer.accept(bottomV);
		topVConsumer.accept(topV);
		
	}

	/**
	 * Retrieve the U coordinates only of the current frame via {@code FloatConsumer}s.
	 * 
	 * @param leftUConsumer — {@code FloatConsumer} who recieves the left U coordinate of the current animation
	 * @param rightUConsumer — {@code FloatConsumer} who recieves the right U coordinate of the current animation
	 */
	public void getFrameUs(FloatConsumer leftUConsumer , FloatConsumer rightUConsumer) {
		
		leftUConsumer.accept(currentLeftU);
		rightUConsumer.accept(currentRightU);
		
	}
	
	/**
	 * Retrieve the UV coordinates of the current frame by writing them to a provided array, starting at the given index.
	 * 
	 * @param floatBuffer — float array to write UV coordinates to
	 * @param startIndex — the first index to write to
	 */
	public void getFrameUVs(float[] floatBuffer , int startIndex) {

		Objects.checkFromIndexSize(startIndex , 4 , floatBuffer.length);
		
		floatBuffer[startIndex] = currentLeftU;
		floatBuffer[startIndex + 1] = currentRightU;
		floatBuffer[startIndex + 2] = bottomV;		
		floatBuffer[startIndex + 3] = topV;
		
	}

	/**
	 * Retrieve only the U coordinates of the current frame by writing them to a provided array, starting at the given index.
	 * 
	 * @param floatBuffer — float array to write UV coordinates to
	 * @param startIndex — the first index to write to
	 */
	public void getFrameUs(float[] floatBuffer , int startIndex) {

		Objects.checkFromIndexSize(startIndex , 2 , floatBuffer.length);
		
		floatBuffer[startIndex] = currentLeftU;
		floatBuffer[startIndex + 1] = currentRightU;
		
	}
	
	/**
	 * Retrieve the UV coordinates of the current frame by putting them in the given buffer. The writes occur from the buffer's position
	 * and the position value of the buffer is unchanged when it is returned. 
	 * 
	 * @param buffer — buffer to write to
	 */
	public void getFrameUVs(FloatBuffer buffer) {
		
		int position = buffer.position();
		Objects.checkFromIndexSize(position , 4, buffer.limit());
		
		buffer.put(currentLeftU);
		buffer.put(currentRightU);
		buffer.put(bottomV);
		buffer.put(topV);
		
		buffer.position(position);
		
	}

	/**
	 * Retrieve only the U coordinates of the current frame by putting them in the given buffer. The writes occur from the buffer's position
	 * and the position value of the buffer is unchanged when it is returned. 
	 * 
	 * @param buffer — buffer to write to
	 */
	public void getFrameUs(FloatBuffer buffer) {

		int position = buffer.position();
		Objects.checkFromIndexSize(position , 2, buffer.limit());
		
		buffer.put(currentLeftU);
		buffer.put(currentRightU);
		
		buffer.position(position);
		
	}

	/**
	 * Retrieve the UV coordinates of the current frame by putting them in the given buffer. The writes occur from the buffer's position
	 * and the position value of the buffer is unchanged when it is returned. 
	 * 
	 * @param buffer — buffer to write to
	 */
	public void getFrameUVs(ByteBuffer buffer) {

		int position = buffer.position();
		Objects.checkFromIndexSize(position , 4, buffer.limit());
		
		buffer.putFloat(currentLeftU);
		buffer.putFloat(currentRightU);
		buffer.putFloat(bottomV);
		buffer.putFloat(topV);

		buffer.position(position);
		
	}

	/**
	 * Retrieve only the U coordinates of the current frame by putting them in the given buffer. The writes occur from the buffer's position
	 * and the position value of the buffer is unchanged when it is returned. 
	 * 
	 * @param buffer — buffer to write to
	 */
	public void getFrameUs(ByteBuffer buffer) {
		
		buffer.putFloat(currentLeftU);
		buffer.putFloat(currentRightU);
		
	}
	
	/**
	 * Returns the current value of the left U coordinate.
	 * 
	 * @return The current value of the left U coordinate.
	 */
	public float leftU() {
		
		return currentLeftU;
		
	}

	/**
	 * Returns the current value of the right U coordinate.
	 * 
	 * @return The current value of the right U coordinate.
	 */
	public float rightU() {
		
		return currentRightU;
		
	}
	
	/**
	 * Returns the current frame of this animation.
	 * 
	 * @return Current frame of this animation.
	 */
	public CSAnimationFrame current() {
		
		return frames.get(currentFrame);
		
	}

	/**
	 * Updates the current frame index.
	 */
	private void bumpCurrentFrame() {
		
		currentFrame++;
		if(currentFrame == frames.size()) currentFrame = 0;
		
	}

	/**
	 * Updates the {@link CSAnimation#currentLeftU} and {@link CSAnimation#currentRightU} values.
	 */
	private void bumpUs() {
		
		if(currentFrame > 0) {

			currentLeftU += widthU;
			currentRightU += widthU;
			
		} else {
			
			currentLeftU = leftU;
			currentRightU = leftU + widthU;
			
		}
		
		if(onUpdate != null) onUpdate.run(); 
		if(onUpdateReceiveUs != null) onUpdateReceiveUs.accept(currentLeftU , currentRightU);
		
	}

	/**
	 * Allows a callback to be set which will be invoked immediately after the animation has moved to the next frame.
	 * <p>
	 * 	A callback from both this method and {@link CSAnimation#onAdvanceFrame(FloatBiConsumer) onAdvanceFrame(FloatBiConsumer)} can be 
	 * 	present at the same time. If the {@code Runnable} callback is present, it is invoked first.
	 * </p>
	 * 
	 * @param callback — code to invoke
	 */
	public void onAdvanceFrame(Runnable callback) {
		
		this.onUpdate = callback;
		
	}
	
	/**
	 * Allows a callback to be set which will be invoked immediately after the animation has moved to the next frame. This method receives
	 * the new left U coordinate as its first parameter and the new right U its second parameter. 
	 * <p>
	 * 	A callback from both this method and {@link CSAnimation#onAdvanceFrame(Runnable) onAdvanceFrame(Runnable)} can be present at the 
	 * 	same time. If the {@code Runnable} callback is present, it is invoked first.
	 * </p>
	 * 
	 * @param callback — receiver of the left and right U coodinates
	 */
	public void onAdvanceFrame(FloatBiConsumer callback) {
		
		this.onUpdateReceiveUs = callback;
		
	}
	
	/**
	 * Returns whether this animation's {@link CSAnimation#frames frames} structure is identical to {@code other}s.
	 */
	@Override public int compareTo(CSAnimation other) {
	
		return this.frames.compareTo(other.frames);
		
	}
	
	/**
	 * Returns true if {@code other} is a {@code CSAnimation} and its {@link CSAnimation#frames frames} is equal to this's {@code frames}. 
	 * 
	 * @param other — another object
	 * @return {@code true} if {@code other} is another animation that requires the exact amount of time to complete as this and has the 
	 * 		   same number of elements.
	 */
	public boolean equals(Object other) {
	
		return other instanceof CSAnimation && frames.equals(((CSAnimation)other).frames);
		
	}
	
	@Override public CSAnimation clone() {
		
		return new CSAnimation(this);
		
	}

	/**
	 * Returns this {@code CSAnimation}'s hash code, which is delegated to by its {@link CSAnimation#frames frames}.
	 *  
	 * @return Hash code of this object.
	 */
	public int hashCode() {
		
		return frames.hashCode();
		
	}

	@Override public String toString() {
		
		return "CSAnimation " + name + " with " + frames.size() + " frames.";
		
	}
	
}
