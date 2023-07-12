package cs.csss.project;

import static org.lwjgl.opengl.GL30C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL30C.glScissor;
import static org.lwjgl.opengl.GL11C.glDisable;
import static cs.core.utils.CSUtils.specify;

import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.joml.Matrix4f;

import cs.core.utils.CSRefInt;
import cs.core.utils.FloatConsumer;
import cs.core.utils.FloatSupplier;
import cs.core.utils.Timer;
import cs.csss.core.CSSSCamera;
import cs.csss.core.Engine;
import cs.csss.editor.ui.AnimationPanel;
import cs.csss.utils.FloatReference;

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
	
	private String name;
	final Vector<AnimationFrame> frames = new Vector<>();
	
	//used when the animation runs based on time
	private FloatReference swapTime = new FloatReference(0);
	private Timer swapTimer = new Timer();
	
	//used when the animation runs by updates
	private CSRefInt swapOnUpdates = new CSRefInt(0);
	private int currentUpdate = 0;
	
	private volatile int currentFrame;

	public final FloatSupplier getFrameTime = swapTime::get;
	public final FloatConsumer setFrameTime = swapTime::set;
	
	public final IntSupplier getUpdates = swapOnUpdates::intValue;
	public final IntConsumer setUpdates = swapOnUpdates::set;
	
	private AnimationSwapType defaultSwapType = AnimationSwapType.SWAP_BY_TIME;
	
	private boolean playing = false;

	public Animation(final String name) {
	
		this.name = name;	
		
	}

	int appendArtboard(final Artboard artboard) {
		
		frames.add(newFrame(artboard));		
		//compute the x position the artboard should move to		
		return (frames.size() - 1) * frameWidth();
		
	}
	
	int xPositionForFrameAt(int index) {
		
		return index * frameWidth();
		
	}
	
	void replaceFrame(int index , Artboard artboard) {
	
		validateIndex(index);
		frames.remove(index);
		frames.add(newFrame(artboard));
		
	}			
	
	void putArtboard(final Artboard artboard, final int index) {
		
		validateIndex(index);		
		frames.add(index, newFrame(artboard));
		
	}
	
	public AnimationFrame getCurrentFrame() {
		
		return getFrame(currentFrame);
		
	}
	
	public AnimationFrame getFrame(int index) {
		
		if(frames.size() == 0) return null;
		validateIndex(index);
		return frames.get(index);
		
	}
		
	AnimationFrame removeFrame(int index) {
		
		validateIndex(index);
		currentFrame = 0;
		return frames.remove(index);
		
	}
	
	boolean removeFrame(Artboard artboard) {
		
		for(int i = 0 ; i < frames.size() ; i++) if(frames.get(i).board == artboard) {
				
			removeFrame(i);
			return true;
				
		}
		
		return false;
		
	}
	
	public void setFrameAsNonDefault(int index , final float newFrameTime) {
		
		validateIndex(index);
		frames.get(index).time = new FloatReference(newFrameTime);
		
	}
	
	public float getNonDefaultFrameTime(int index) {
		
		validateIndex(index);
		return frames.get(index).time.get();
		
	}

	public boolean playing() {
		
		return playing;
		
	}
	
	public void togglePlaying() {
		
		if(playing) pause();
		else start();
		
	}
	
	public void start() {
		
		swapTimer.start();
		currentFrame = 0;
		playing = true;
		
	}
	
	public void update() {
		
		if(!playing) return;
		if(isCurrentFrameFinished()) advanceFrame();
						
	}
	
	public void pause() {
		
		swapTimer.reset();
		playing = false;
		
	}
	
	/**
	 * Gets the total time of the animation to complete, which is the sum of times of all frames. 
	 * 
	 * @return Total time of this animation.
	 */
	public float totalTime() {
		
		float time = 0f;
		for(AnimationFrame frame : frames) { 
		
			time += switch(frame.swapType()) {
				case SWAP_BY_TIME -> frame.time.get();
				case SWAP_BY_UPDATES -> Engine.realtimeFrameTime() * frame.frames.intValue();
			};
			
		}
		
		return time;
		
	}
	
	/**
	 * This method specifically returns the total amount of time of the current runthrough of the animation. 
	 * 
	 * @return Elapsed time since this animation's {@link Animation#start() start} method was invoked.
	 */
	public float elapsedTime() {
		
		float accum = 0f;
		for(int i = 0 ; i < currentFrame ; i++) { 
			
			accum += switch(frames.get(i).swapType()) {
				case SWAP_BY_TIME -> frames.get(i).time.get();
				case SWAP_BY_UPDATES -> Engine.realtimeFrameTime() * frames.get(i).frames.intValue();
			};
			
		}
		return accum + (float) swapTimer.getElapsedTimeMillis();
		
	}
	
	/**
	 * Returns whether the current frame is completed.
	 * 
	 * @return {@code true} if the current frame is completed.
	 */
	private boolean isCurrentFrameFinished() {
		
		AnimationFrame frame = frames.get(currentFrame);
		return switch(frame.swapType()) {		
			case SWAP_BY_TIME -> swapTimer.getElapsedTimeMillis() >= frame.time.get();
			case SWAP_BY_UPDATES -> { 
				
				currentUpdate++;
				yield currentUpdate >= frame.frames.intValue();
				
			}
		};		
		
	}
	
	/**
	 * Advances the current frame to the next, either by incrementing the current frame integer or by restarting the animation.
	 */
	private void advanceFrame() {
		
		//advance the current frame and reset the timer, special case of when we are at the last frame, in which case restart.
		if(currentFrame + 1 == frames.size()) currentFrame = 0;
		else currentFrame++;		
		
		currentUpdate = 0;
		swapTimer.start();
		
	}
	
	/**
	 * Sets the frame at {@code frameIndex} to the specified swap type.
	 * 
	 * @param frameIndex — index of a frame
	 * @param swapType — new swap type for that frame
	 */
	public void setFrameSwapType(int frameIndex , AnimationSwapType swapType) {
		
		Objects.checkIndex(frameIndex, frames.size());
		Objects.requireNonNull(swapType);
		frames.get(frameIndex).swapType(() -> swapType);
		
	}
	
	/**
	 * This method renders the current frame of the animation, an artboard, on the given UI element.
	 * 
	 * <p>
	 * 	The frames of animations are themselves just artboards. This method will render the current artboard in the given UI element. This
	 * 	is used for visualization purposes. To accomplish this, the artboard is rendered exactly the same way as it is when it is rendered
	 * 	outside any UI element. This method additionally translates the rerender so it lies exactly on the ui element where it belongs, and
	 * 	this method scissors any part of the artboard that is out of bounds.
	 * </p>
	 * 
	 * @param camera — the program's camera
	 * @param renderOnto — the animation panel, which is where the active frame will be rendered to; this object contains the data needed
	 * 					   to correctly position the frame
	 * @param screenHeight — needed because Nuklear represents its points as top left but opengl represents its points as bottom left, so 
	 * 						 we need to get the difference to get the bottom left point for the scissor test
	 * @param channels — number of channels for rendering purposes 
	 */
	public void renderCurrentFrame(
		CSSSCamera camera , 
		AnimationPanel renderOnto ,
		int screenHeight ,
		int channels
	) {		
	
		AnimationFrame frame = getCurrentFrame();
		
		if(frame == null) return;

		final Matrix4f translationToUIPoint = renderOnto.moveToMatrix();
		
		/*
		 * newZoom here is a camera zoom value applied to the camera when rendering the active frame in the UI element. It allows people to
		 * zoom into the frame being rendered on the UI element. We also store the original zoom and reset it at the end of this method.
		 */
		final float 
			newZoom = renderOnto.zoom() ,
			originalZoom = camera.zoom()
		;
		
		camera.zoom(newZoom);
		
		int[] 
			panelMidpoint = renderOnto.midpointToAnimationFrameSlot() ,
			panelDims = renderOnto.dimensionsOfAnimationFrameSlot() ,
			panelTopLeft = renderOnto.topLeftPointOfAnimationFrameSlot()
		;
		
		//here we compute the world coordinates of the point of the ui element we want to render the active frame in. We'll move the active
		//frame to that point using these values
		int worldX = (int) camera.XscreenCoordinateToWorldCoordinate(panelMidpoint[0]);
		int worldY = (int) camera.YscreenCoordinateToWorldCoordinate(panelMidpoint[1]);
		
		//undoes the artboard's previous translation, i.e., moves it back to its place before being moved by the project.
		translationToUIPoint.translate(-frame.board.midX() + worldX , -frame.board.midY() + worldY, 0);
		//applies the additional translation people can do when mousing over the ui element
		translationToUIPoint.translate(renderOnto.xTranslation(), renderOnto.yTranslation(), 0);
		
		Artboard.theArtboardShader().updatePassVariables(
			camera.projection() , 
			camera.viewTranslation() ,	
			translationToUIPoint ,
			channels
		);
		
		//resets the camera to the orignal zoom
		camera.zoom(originalZoom);
		
		glEnable(GL_SCISSOR_TEST);

		//cuts off parts of the object that are outside the bounds of the group, the random primitives are used to approximately fit
		//the artboard into the frame as perfectly as possible. this will look the same no matter the window dimensions.
		glScissor(
			panelTopLeft[0] , 
			screenHeight - panelTopLeft[1] - panelDims[1] - 5,
			panelDims[0] + 10 , 
			panelDims[1] - 1
		);
		
		frame.board.draw();
		glDisable(GL_SCISSOR_TEST);
		
	}

	public int frameWidth() {
		
		if(frames.size() == 0) return 0;
		return frames.get(0).board.width();
		
	}

	public int frameHeight() {
		
		if(frames.size() == 0) return 0;
		return frames.get(0).board.height();
		
	}

	/**
	 * Used to get the left x coordinate of the first artboard in this animation. 
	 * 
	 * @return X coordinate of the left vertex of the first artboard of this animation.
	 */
	public int leftmostX() {
		
		return -(frames.get(0).board.width() / 2);
		
	}
	
	public void currentFrameIndex(int frame) {
		
		currentFrame = frame;
		
	}
	
	public int currentFrameIndex() {
		
		return currentFrame;
		
	}
	
	public int numberFrames() {
		
		return frames.size();
		
	}
	
	public boolean hasArtboard(Artboard artboard) {
		
		return frames.stream().anyMatch(frameTime -> frameTime.board == artboard);
		
	}

	public void forAllFrames(Consumer<Artboard> callback) {
		
		frames.forEach(frame -> callback.accept(frame.board));
		
	}

	public AnimationSwapType defaultSwapType() {
		
		return defaultSwapType;
		
	}

	public void defaultSwapType(AnimationSwapType swapType) {
		
		this.defaultSwapType = swapType;
		
	}
	
	boolean matchesDimensions(Artboard artboard) {
		
		return artboard.width() == frameWidth() && artboard.height() == frameHeight();
		
	}
	
	private void validateIndex(final int index) {
		
		specify(
			(frames.size() > 0 && index >= 0 && index < frames.size()) || (frames.size() == 0 && index == 0) , 
			index + " is an out of bounds index."
		);
		
	}
		
	public String name() {
		
		return name;
		
	}

	public void setFrameTime(float time) {
		
		swapTime.set(time);
		
	}
	
	public void setUpdates(int updates) {
	
		swapOnUpdates.set(updates);
		
	}	
	
	int getTotalWidth() {
		
		return frameWidth() * numberFrames();
		
	}
	
	private AnimationFrame newFrame(Artboard frame) {
		
		return new AnimationFrame(frame , swapOnUpdates , swapTime , () -> defaultSwapType);
		
	}

}
