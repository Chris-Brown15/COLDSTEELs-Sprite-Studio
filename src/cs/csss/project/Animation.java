package cs.csss.project;

import static org.lwjgl.opengl.GL30C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL30C.glScissor;
import static org.lwjgl.opengl.GL11C.glDisable;
import static cs.core.utils.CSUtils.specify;

import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.joml.Matrix4f;

import cs.core.utils.CSRefInt;
import cs.core.utils.FloatConsumer;
import cs.core.utils.FloatSupplier;
import cs.core.utils.Timer;
import cs.csss.editor.ui.AnimationPanel;
import cs.csss.engine.CSSSCamera;
import cs.csss.misc.utils.ThrowingConsumer;
import cs.csss.utils.FloatReference;

/**
 * Container for all information needed to create and edit animations.
 * 
 * <p>
 * 	Animations are collections of {@link cs.csss.project.AnimationFrame AnimationFrame}s contain an artboard and a time. There are two basic
 * 	ways animations can swap their frames. The first is by time. In this mode, each frame has a set number of milliseconds it will be the 
 * 	current frame, and once that time has elapsed, the timer will restart and the the next frame is set as the current frame. Alternatively,
 * 	frames can be swapped by updates. In this mode, an update method is called every program frame, and for each invokation, a counter is 
 * 	incremented. Once the counter reaches the current frame's number of updates, the counter is reset and the next frame is moved to.
 * </p>
 * 
 * <p>
 * 	Additionally, it is possible to specifically set the time or number of updates of an animation frame. This means that while there is an
 * 	animation-wide default, frames can also have a unique value. Further, it is possible to set the swap method for individual frames. This
 * 	means that some frames can be swap by time, and others can be swap by updates.
 * </p>
 * 
 * @author Chris Brown
 * 
 */
public class Animation {
	
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
	
	private final DoubleSupplier realtimeFrameTime;
	
	private AnimationSwapType defaultSwapType = AnimationSwapType.SWAP_BY_TIME;
	
	private boolean playing = false;

 	Animation(final String name , DoubleSupplier realtimeFrameTime) {
	
		this.name = name;	
		this.realtimeFrameTime = realtimeFrameTime;
		
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
				case SWAP_BY_UPDATES -> realtimeFrameTime.getAsDouble() * frame.frames.intValue();
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
				case SWAP_BY_UPDATES -> realtimeFrameTime.getAsDouble() * frames.get(i).frames.intValue();
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
	 * @param frameIndex � index of a frame
	 * @param swapType � new swap type for that frame
	 */
	public void setFrameSwapType(int frameIndex , AnimationSwapType swapType) {
		
		Objects.checkIndex(frameIndex, frames.size());
		Objects.requireNonNull(swapType);
		frames.get(frameIndex).swapType(() -> swapType);
		
	}
	
	/**
	 * Sets the frame at {@code originalIndex} to the index {@code newIndex}.
	 * 
	 * @param originalIndex � index of some a frame
	 * @param newIndex � new index for the frame
	 */
	public void setFramePosition(int originalIndex , int newIndex) {
		
		AnimationFrame frame = frames.remove(originalIndex);
		frames.add(newIndex, frame);
		
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
	 * @param camera � the program's camera
	 * @param renderOnto � the animation panel, which is where the active frame will be rendered to; this object contains the data needed
	 * 					   to correctly position the frame
	 * @param screenHeight � needed because Nuklear's origin is top left but OpenGL represents its points as bottom left, so we need to get 
	 * 						 the difference to get the bottom left point for the scissor test
	 */
	public void renderCurrentFrame(CSSSCamera camera , AnimationPanel renderOnto , int screenHeight ) {		
	
		AnimationFrame frame = getCurrentFrame();
		
		if(frame == null) return;

		final Matrix4f translationToUIPoint = renderOnto.moveToMatrix();
		
		/*
		 * newZoom here is a camera zoom value applied to the camera when rendering the active frame in the UI element. It allows people to
		 * zoom into the frame being rendered on the UI element. We also store the original zoom and reset it at the end of this method.
		 */
		final float 
			newZoom = renderOnto.zoom() ,
			originalZoom = camera.zoom();
		
		camera.zoom(newZoom);
		
		int[] 
			panelMidpoint = renderOnto.midpointToAnimationFrameSlot() ,
			panelDims = renderOnto.dimensionsOfAnimationFrameSlot() ,
			panelTopLeft = renderOnto.topLeftPointOfAnimationFrameSlot();
		
		//here we compute the world coordinates of the point of the ui element we want to render the active frame in. We'll move the active
		//frame to that point using these values
		int worldX = (int) camera.XscreenCoordinateToWorldCoordinate(panelMidpoint[0]);
		int worldY = (int) camera.YscreenCoordinateToWorldCoordinate(panelMidpoint[1]);
		
		//undoes the artboard's previous translation, i.e., moves it back to its place before being moved by the project.
		translationToUIPoint.translate(-frame.board.midX() + worldX , -frame.board.midY() + worldY, 0);
		//applies the additional translation people can do when mousing over the ui element
		translationToUIPoint.translate(renderOnto.xTranslation(), renderOnto.yTranslation(), 0);
		
		CSSSShader shader = CSSSProject.currentShader(); 
		
		shader.updatePassVariables(camera.projection() , camera.viewTranslation() , translationToUIPoint);
		
		//resets the camera to the orignal zoom
		camera.zoom(originalZoom);
		
		glEnable(GL_SCISSOR_TEST);

		//cuts off parts of the object that are outside the bounds of the group, the random primitives are used to approximately fit
		//the artboard into the frame as perfectly as possible. this will look the same no matter the window dimensions.
		glScissor(panelTopLeft[0] , screenHeight - panelTopLeft[1] - panelDims[1] - 5, panelDims[0] + 10 , panelDims[1] - 1);
		
		shader.updateTextures(frame.board.activeLayersPalette() , frame.board.indexTexture());
		shader.activate();
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
	public float leftmostX() {
		
		return frames.get(0).board.leftX();
		
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

	public void forAllArtboards(Consumer<Artboard> callback) {
		
		frames.stream().map(AnimationFrame::board).forEach(callback);
		
	}

	public <ThrowType extends Throwable> void forAllFrames(ThrowingConsumer<ThrowType , AnimationFrame> callback) throws ThrowType {
		
		for(int i = 0 ; i < frames.size() ; i++) callback.acceptOrThrow(frames.get(i));
		
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
	
	public int indexOf(Artboard artboard) {

		for(int i = 0 ; i < frames.size() ; i++) if(frames.get(i).board == artboard) return i;
		throw new IllegalArgumentException("Artboard not found in this animation.");
		
	}
	
	private AnimationFrame newFrame(Artboard frame) {
		
		return new AnimationFrame(frame , swapOnUpdates , swapTime , () -> defaultSwapType);
		
	}
	
	public FloatReference defaultSwapTime() {
		
		return swapTime;
		
	}

	public CSRefInt defaultUpdateAmount() {
		
		return swapOnUpdates;
		
	}

	public float leftU(float projectLeftMostX , float projectRightMostX) {
		
		if(numberFrames() == 0) return -1f;				
	 	AnimationFrame firstFrame = frames.get(0);
	 	float offset = firstFrame.board.leftX() - projectLeftMostX;
		return Math.abs(offset / (projectRightMostX - projectLeftMostX));
		
	}
	
	public float bottomV(float projectBottomY , float projectTopY) {
		
		if(numberFrames() == 0) return -1f;		
		AnimationFrame firstFrame = frames.get(0);		
		float offset = firstFrame.board.bottomY() - projectBottomY;
		return Math.abs(offset / (projectTopY - projectBottomY));
		
	}
	
	public float topV(float projectBottomY , float projectTopY) {
		
		if(numberFrames() == 0) return -1f;		
		AnimationFrame firstFrame = frames.get(0);
		float offset = firstFrame.board.topY() - projectBottomY;
		return Math.abs(offset / (projectTopY - projectBottomY));
		
	}
	
	public float widthU(float projectLeftMostX , float projectRightMostX) {

		if(numberFrames() == 0) return -1f;	
		float projectWidth = projectRightMostX - projectLeftMostX;
		return Math.abs(frameWidth() / projectWidth);
		
	}

	public boolean isEmpty() {
		
		return frames.isEmpty();
		
	}
	
}
