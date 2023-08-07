package cs.csss.project;

import static cs.core.utils.CSUtils.specify;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import cs.core.utils.CSRefInt;
import cs.core.utils.ShutDown;
import cs.csss.engine.Engine;
import cs.csss.project.io.CTSPFile;
import cs.csss.project.io.CTSPFile.AnimationChunk;
import cs.csss.project.io.CTSPFile.AnimationFrameChunk;
import cs.csss.project.io.CTSPFile.ArtboardChunk;
import cs.csss.project.io.CTSPFile.NonVisualLayerChunk;
import cs.csss.project.io.CTSPFile.NonVisualLayerDataChunk;
import cs.csss.project.io.CTSPFile.PaletteChunk;
import cs.csss.project.io.CTSPFile.VisualLayerDataChunk;
import cs.csss.project.io.ProjectExporter;
import cs.csss.utils.FloatReference;

/**
 * Contains all data about the currently in-use project. 
 * 
 * <p>
 * 	The project is responsible for containing the artboards, layers, and animations the user is currently working with. Most structural 
 * 	modifications such as adding, deleting objects, and moving objects is handled by this class. 
 * </p>
 * 
 * <p>
 * 	Artboards are graphically placed next to one another in rows. Each animation is its own row, and the animation taking up the greatest
 * 	width in pixels is the highest row. If an artboard is in two or more animations, it is rerendered for each animation it's in.
 * </p>
 * 
 * @author Chris Brown
 *
 */
public class CSSSProject implements ShutDown {

	private static final PaletteShader thePaletteShader = new PaletteShader();
	private static final TextureShader theTextureShader = new TextureShader();
	private static CSSSShader currentShader = thePaletteShader;

	/**
	 * The shader for artboards stays the same over any artboard, so only one is created and used everywhere.
	 */
	public static void initializeArtboardShaders() {
		
		thePaletteShader.initialize();
		theTextureShader.initialize();

	}
	
	/**
	 * Returns the shader used to render paletted images.
	 * 
	 * @return The single palette shader used to render paletted images.
	 */
	public static PaletteShader thePaletteShader() {
		
		return thePaletteShader;
		
	}
	
	/**
	 * Returns the shader used to render textures directly.
	 * 
	 * @return The shader used to render textures.
	 */
	public static TextureShader theTextureShader() {
				
		return theTextureShader;
		
	}
	
	/**
	 * Returns the current shader.
	 * 
	 * @return The current shader for artboards.
	 */
	public static CSSSShader currentShader() {
		
		return currentShader;
		
	}
	
	/**
	 * Sets the current shader to the given shader.
	 * 
	 * @param newCurrent — new current shader
	 */
	public static void setTheCurrentShader(CSSSShader newCurrent) {
		
		currentShader = newCurrent;
		
	}
	
	private AtomicBoolean isFreed = new AtomicBoolean(false);
	
	private String name;

	private final Engine engine;
	
	private int channelsPerPixel = -1;
	
	private boolean 
		freemoveMode = false ,
		freemoveCheckCollisions = true ,
		padAnimationFrames = true
	;
	
	private ArtboardPalette visualPalette;
	private ArrayList<ArtboardPalette> nonVisualPalettes = new ArrayList<>(NonVisualLayerPrototype.MAX_SIZE_BYTES);

	private ArtboardCopier copier = new ArtboardCopier();
	
	private final LinkedList<Artboard> 
		allArtboards = new LinkedList<>() ,
		looseArtboards = new LinkedList<>();

	private final LinkedList<Animation> animations = new LinkedList<>();
	private final LinkedList<VisualLayerPrototype> visualLayerPrototypes = new LinkedList<>();
	private final LinkedList<NonVisualLayerPrototype> nonVisualLayerPrototypes = new LinkedList<>();
	
	private Artboard currentArtboard;
	private Animation currentAnimation;
	
	public CSSSProject(Engine engine , final String name , int channelsPerPixel , boolean makeDefaultLayer) {
		
		specify(channelsPerPixel > 0 && channelsPerPixel <= 4 , channelsPerPixel + " is not a valid number of channels per pixel.");

		this.engine = engine;
		
		setName(name);
		this.channelsPerPixel = channelsPerPixel;
		
		visualPalette = new ArtboardPalette(channelsPerPixel);

		for(int i = 1 ; i <= NonVisualLayerPrototype.MAX_SIZE_BYTES ; i++) nonVisualPalettes.add(new ArtboardPalette(i));
		
	 	if(makeDefaultLayer) addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));
		
	}

	public CSSSProject(Engine engine , final String name) {
	
		this.engine = engine;
		
		setName(name);
		addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));		
				
	}
	
 	public CSSSProject(Engine engine , CTSPFile ctsp) {

 		this.engine = engine;
 		setName(ctsp.name());
 		channelsPerPixel = ctsp.channelsPerPixel();
 		
 		//layers
 		for(String x : ctsp.visualLayerNames()) addVisualLayerPrototype(new VisualLayerPrototype(x));
 		for(NonVisualLayerChunk x : ctsp.nonVisualLayerChunks()) {
 			
 			addNonVisualLayerPrototype(new NonVisualLayerPrototype(x.size() , x.name()));
 			
 		}
 		
 		//palettes
 		visualPalette = loadPalette(ctsp.paletteChunks()[0]);
 		for(int i = 1 ; i < 5 ; i ++) nonVisualPalettes.add(loadPalette(ctsp.paletteChunks()[i]));
 		
 		//artboards
 		for(ArtboardChunk x : ctsp.artboardChunks()) loadArtboard(x);
 		
 		//animations
 		for(AnimationChunk x : ctsp.animationChunks()) loadAnimation(x);
 		
 	}
	
 	private ArtboardPalette loadPalette(PaletteChunk chunk) {

 		int width = chunk.width();
 		int height = chunk.height();
 		byte[] pixelData = chunk.pixelData();
 		
 		ArtboardPalette palette = new ArtboardPalette(chunk.channels());
 		palette.initialize();
 		palette.resizeAndCopy(width , height);
 		
 		//this skips the first two entries, the background pixel values
 		int i = 2 * chunk.channels();
 		
 		byte[] channelValues = new byte[chunk.channels()];
 		while(i < pixelData.length) {
 			
 			for(int j = 0 ; j < chunk.channels() ; j++) channelValues[j] = pixelData[i++];
 			palette.put(palette.new PalettePixel(channelValues));
 			
 		}
 		
 		return palette;
 		
 	}
 	
 	private Artboard loadArtboard(ArtboardChunk chunk) {
 		
 		Artboard newArtboard = createArtboard(chunk.name() , chunk.width() , chunk.height());
 		 		
 		//also set up visual layer ranks here. the order the chunks are found are the ranks the layers are supposed to be in
 		int i = 0;
 		for(VisualLayerDataChunk x : chunk.visualLayers()) { 
 			
 			VisualLayer layer = (VisualLayer) loadLayer(newArtboard , x);
 			int previousRank = newArtboard.getLayerRank(layer);
 			if(previousRank != i) newArtboard.moveVisualLayerRank(previousRank , i);
 			i++;

 		}
 		
 		//nonvisual
 		for(NonVisualLayerDataChunk x : chunk.nonVisualLayers()) loadLayer(newArtboard , x);
 		
 		//set active layer
 		if(chunk.isActiveLayerVisual()) { 
 			
 			newArtboard.setActiveLayer(newArtboard.getVisualLayer(chunk.activeLayerIndex()));
 			newArtboard.showAllNonHiddenVisualLayers();
 			
 		} else { 
 			
 			Layer active = newArtboard.getNonVisualLayer(chunk.activeLayerIndex());
 			newArtboard.setActiveLayer(active);
 			active.show(newArtboard); 
 			
 		}
 		
 		return newArtboard;
 		
 	}

 	private Layer loadLayer(Artboard artboard , VisualLayerDataChunk chunk) {
 		
 		return loadLayer(artboard , chunk.name() , chunk.hiding() , chunk.locked() , chunk.isCompressed() , chunk.pixelData());
 		
 	}

 	private Layer loadLayer(Artboard artboard , NonVisualLayerDataChunk chunk) {
 		
 		return loadLayer(artboard , chunk.name() , chunk.hiding() , chunk.locked() , chunk.isCompressed() , chunk.pixelData());
 		
 	}
 	
 	private void loadAnimation(AnimationChunk x) {
 		
 		Animation animation = createAnimation(x.name());
 		animation.defaultSwapType(AnimationSwapType.valueOf(x.defaultSwapType()));
 		animation.setFrameTime(x.defaultSwapTime());
 		animation.setUpdates(x.defaultUpdates());
 		
 		currentAnimation(animation);
 		
 		for(AnimationFrameChunk y : x.frames()) {
 			
 			appendArtboardToCurrentAnimation(getArtboard(y.artboardName()));
 			//most recent frame
 			AnimationFrame frame = animation.getFrame(animation.numberFrames() - 1); 			
 			AnimationSwapType swapType = AnimationSwapType.valueOf(y.swapType());
 			frame.swapType(() -> swapType);
 			
 			if(y.frameTime() == animation.getFrameTime.getAsFloat()) frame.time(animation.defaultSwapTime());
 			else frame.time(new FloatReference(y.frameTime()));
 			
 			if(y.frameUpdates() == animation.getUpdates.getAsInt()) frame.updates(animation.defaultUpdateAmount());
 			else frame.updates(new CSRefInt(y.frameUpdates()));
 			
 		}
 		
 	}
 	
	public void initialize() {
		
		visualPalette.initialize();
		for(ArtboardPalette x : nonVisualPalettes) x.initialize();
			
	}
	
	/**
	 * Deletes and removes all instances of the layer modeled by {@code layer} from the project.
	 * 
	 * @param layer — layer to delete
	 */
	public void deleteVisualLayer(VisualLayerPrototype layer) {
		
		synchronized(allArtboards) {
			
			allArtboards.forEach(artboard -> {
				
				//deleting the layer for the source will delete it for all shallow copies so only delete sources. 
				if(copier.isCopy(artboard)) return;
				
				VisualLayer removeThis = artboard.getVisualLayer(layer);
				if(!removeThis.hiding()) removeThis.hide(artboard);
				
				artboard.removeLayer(removeThis);
				
			});
			
			//makes the remove take place at a safe point
			Engine.THE_TEMPORAL.onTrue(() -> true, () -> visualLayerPrototypes.remove(layer));
			
		}
		
	}
	
	/* Animation Methods */
	
	public void appendArtboardToCurrentAnimation(Artboard artboard) {
		
		boolean wasLoose = looseArtboards.remove(artboard);
		
		if(wasLoose) {
			
			currentAnimation.appendArtboard(artboard);
			
		} else {
			
			Artboard shallowCopy = engine.renderer().make(() -> copier.copy(artboard)).get();
			currentAnimation.appendArtboard(shallowCopy);
			Engine.THE_TEMPORAL.onTrue(() -> true , () -> allArtboards.add(shallowCopy));
			
		}		
		
		arrangeArtboards();
		
	}	
	
	private Optional<Artboard> removeArtboardFromAnimation(Animation animation , int index) {

		AnimationFrame frame = currentAnimation.removeFrame(index);
		
		boolean wasShallow = copier.isCopy(frame.board);
		if(wasShallow) { 
			
			removeArtboard(frame.board);
			return Optional.empty();
			
		} else FindFirstShallowCopy: {
			
			//if the removed frame is a source of other shallow copies, we look through each animation until we find a shallow copy that 
			//was made from the removed one. In that case, we replace the shallow copy with the original.
			if(copier.isSource(frame.board)) for(Animation x : animations) for(int i = 0 ; i < x.frames.size() ; i++) {
				
				AnimationFrame iterFrame = x.frames.get(i);
				
				if(copier.isCopy(iterFrame.board) && copier.getSourceOf(iterFrame.board) == frame.board) { 
					
					x.replaceFrame(i , frame.board);
					removeArtboard(iterFrame.board);
					break FindFirstShallowCopy;
					
				}
				
			}
		
		}
			
		return Optional.of(frame.board);
		
	}
	
	public void removeArtboardFromCurrentAnimation(Artboard artboard) {
		
		removeArtboardFromCurrentAnimation(currentAnimation.indexOf(artboard));
		
	}
	
	public void removeArtboardFromCurrentAnimation(int frameIndex) {

		Optional<Artboard> removed = removeArtboardFromAnimation(currentAnimation , frameIndex);		
		if(removed.isPresent()) looseArtboards.add(removed.get());		
		arrangeArtboards();
		
	}
	
	public void deleteAnimation() {
		
		Optional<Artboard> results;
		
		while(currentAnimation.numberFrames() > 0) { 
			
			results = removeArtboardFromAnimation(currentAnimation , 0);
			if(results.isPresent()) looseArtboards.add(results.get());
			
		}
		
		animations.remove(currentAnimation);
		currentAnimation = null;
		
		arrangeArtboards();
		
	}
	
	public void renderAllArtboards() {
		
		renderAllArtboards(currentShader);
		
	}

	public void renderAllArtboards(CSSSShader shader) {
		
		forEachArtboard(artboard -> {

			if(artboard.isActiveLayerVisual()) visualPalette.activate();
			else nonVisualPalettes.get(artboard.activeLayerChannelsPerPixel() - 1).activate();
			
			shader.activate(artboard);
			shader.activate();
			artboard.draw();
			
		});
		
	}
	
	private void arrangeAnimation(Animation animation , int xOffset , int spaceBetweenBoards , int height) { 
		
		for(int j = 0 ; j < animation.numberFrames() ; j++) { 
			
			if(j == 0) animation.getFrame(j).board.moveTo(xOffset + animation.xPositionForFrameAt(j) , height);
			else animation.getFrame(j).board.moveTo(xOffset + animation.xPositionForFrameAt(j) + spaceBetweenBoards * j, height);
			
			
		}

	}
	
	/**
	 * Arranges loose artboards. The first loose artboard will be at (0 , 0) with subsequent ones extending rightward from it.
	 */
	private void arrangeLooseArtboards() {

		if(looseArtboards.size() > 0) {

			engine.renderer().post(() -> {

				looseArtboards.get(0).moveTo(0 , 0);
				for(int i = 1 ; i < looseArtboards.size() ; i++) {
							
					Artboard current = looseArtboards.get(i);
					Artboard last = looseArtboards.get(i - 1);
							
					float lastRightX = last.rightX();
					int translationX = (int) Math.ceil(lastRightX) + current.width();
					current.moveTo(translationX, 0);
							
				}
						
			});
				
		}
				
	}
	
	private int greatestHeightAmongLooseArtboards() {
		
		int greatestHeightOfLoose = 0;
		for(int i = 0 ; i < looseArtboards.size() ; i++) {
			
			Artboard current = looseArtboards.get(i);
			//check the heights for the purposes of offsetting them
			int height = current.height(); 
			if(height > greatestHeightOfLoose) greatestHeightOfLoose = height;
			
		}					
		
		return greatestHeightOfLoose;
		
	}
	
	/**
	 * Should be called any time an artboard is added or removed from the project, or when an artboard is added or removed from an 
	 * animation.
	 * 
	 * <p>
	 * 	This method moves all artboards into position and as such must be called from the render thread. Loose artboards, which are 
	 * 	artboards that are not in any animation are placed at the bottom of the scene sequentially. Then, artboards that are in animations
	 * 	are placed above loose artboards from bottom to top in order of the amount of horizontal space the animation's artboards take up. 	
	 * </p>
	 */
	public void arrangeArtboards() {

		//start off by arranging loose artboards, which are artboards that are not in any animations
		arrangeLooseArtboards();
		
		if(animations.size() > 0) {
			
			//this will sort the animations from lowest total width to highest total width
			Collections.sort(animations , (anim1 , anim2) -> anim1.getTotalWidth() - anim2.getTotalWidth());
			
			//iterate over the animation list and move artboards to their given position within the list at the heigth offset
			//This is done by keeping a height offset accumulator which increases for each animation by each animation's frame height
			//Half of the animation's height is added to the accumulator, then the artboards are moved into position, then the second half
			//of the animation's height is applied to the accumulator. This is to make the animations tightly next to one another.
			engine.renderer().post(() -> {
				
				int heightAccum = 0;
				int next = 0;
				
				//advances the iterator past any animations that are empty. empty animations are not represented on the image.
				while(next < animations.size() && animations.get(next).frameHeight() == 0) next++;

				if(next == animations.size()) return;
				
				Animation x = animations.get(next);
				int smallestXPosition = x.leftmostX();

				if(looseArtboards.size() == 0) {
					
					//the first animation is a special case where only half of its height is applied to the accum.
					arrangeAnimation(x , 0 , padAnimationFrames ? 2 : 0 , 0);
					heightAccum += (x.frameHeight() / 2) + 2;
					next++;
								
				}
				//if there are loose artboards (artboards that are not in any animation), they will be at the bottom of the image.
				//here we move them. we also arrange them so that 
				else heightAccum = greatestHeightAmongLooseArtboards() + 25;

				while(next < animations.size()) {
					
					x = animations.get(next++);
					
					int height = x.frameHeight();
					
					heightAccum += height / 2;
					//this moves the animation's artboards into position. the second parameter is the x offset to apply to the animation.
					//this is used to make all animations have their first artboard's left x positions align
					arrangeAnimation(x , smallestXPosition + (x.frameWidth() / 2) , padAnimationFrames ? 2 : 0 , heightAccum);
					heightAccum += (height / 2) + 5;
					
				}
				
			});
		
		}
		
	}
	
	
	public void forEachAnimation(Consumer<Animation> callback) {
		
		animations.forEach(callback);
		
	}
	
	public Iterator<Animation> animations() {
		
		return animations.iterator();
		
	}
	
	public Iterator<Artboard> allArtboards() {
		
		return allArtboards.iterator();
		
	}
	
	public void forEachVisualLayerPrototype(Consumer<VisualLayerPrototype> callback) {
		
		visualLayerPrototypes.forEach(callback);
		
	}

	public void forEachNonVisualLayerPrototype(Consumer<NonVisualLayerPrototype> callback) {
		
		nonVisualLayerPrototypes.forEach(callback);
		
	}
	
	public Iterator<NonVisualLayerPrototype> nonvisualLayers() {
		
		return nonVisualLayerPrototypes.iterator();
		
	}
	
	/**
	 * Invokes {@code callback} for all existing artboards, including shallow copies.
	 * 
	 * @param callback — consumer of an artboard
	 */
	public void forEachArtboard(Consumer<Artboard> callback) {
		
		synchronized(allArtboards) {
			
			allArtboards.forEach(callback);
			
		}
		
	}

	public void forEachCopyOf(Artboard source , Consumer<Artboard> callback) {
		
		copier.forEachCopyOf(source , callback);
		
	}
	
	public void forEachShallowCopy(Consumer<Artboard> callback) {
		
		copier.forEachCopy(callback);
		
	}
	
	public void forEachPalette(Consumer<ArtboardPalette> callback) {
		
		callback.accept(visualPalette);
		nonVisualPalettes.forEach(callback);
		
	}

	public void forEachNonVisualPalette(Consumer<ArtboardPalette> callback) {
		
		nonVisualPalettes.forEach(callback);
		
	}
	
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}
	
	public VisualLayerPrototype visualLayerPrototype(int index) {
		
		return visualLayerPrototypes.get(index);
		
	}
	
	public int numberVisualLayers() {
		
		return visualLayerPrototypes.size();
		
	}
	
	public int numberNonVisualLayers() {
		
		return nonVisualLayerPrototypes.size();
		
	}
	
	public int numberAnimations() {
		
		return animations.size();
		
	}
	
	public int getArtboardIndex(Artboard artboard) {
		
		synchronized(allArtboards) {
			
			return allArtboards.indexOf(artboard);
		
		}
		
	}
	
	public void setCurrentArtboardByMouse(float cursorWorldX , float cursorWorldY) {
		
		synchronized(allArtboards) {
			
			for(Artboard x : allArtboards) if(x.isCursorInBounds(cursorWorldX , cursorWorldY)) { 
				
				currentArtboard = x;
				return;
				
			}
		
		}
		
	}
	
	public Artboard currentArtboard() {
		
		return currentArtboard;
		
	}
	
	public void currentArtboard(Artboard currentArtboard) {
		
		this.currentArtboard = currentArtboard;
		
	}
	
	public Animation currentAnimation() {
		
		return currentAnimation;
		
	}

	public void currentAnimation(Animation newCurrent) {
		
		this.currentAnimation = newCurrent;
		
	}
	
	public void setName(String name) {
		
		this.name = name.strip();
		
	}
		
	public String name() {
		
		return name;
		
	}
	
	public int getChannelsPerPixelOfCurrentLayer() {
		
		if(currentArtboard != null) {
			
			if(currentArtboard.activeLayer() instanceof VisualLayer) return channelsPerPixel;
			else return ((NonVisualLayer) currentArtboard.activeLayer()).bytesPerPixel();
		
		} else return 4;
		
	}
	
	public ArtboardPalette getNonVisualPaletteBySize(int sizeBytes) {
		
		specify(sizeBytes > 0 && sizeBytes <= NonVisualLayerPrototype.MAX_SIZE_BYTES , "Invalid size in bytes for a nonvisual layer.");
		
		return nonVisualPalettes.get(sizeBytes - 1);
		
	}
	
	public void setCheckeredBackgroundSize() {
		
		forEachArtboard(artboard -> {
			
			for(int row = 0 ; row < artboard.height() ; row++) for(int col = 0 ; col < artboard.width() ; col++) {
				
				if(!artboard.isAnyLayerModifying(col, row)) { 
					
					artboard.writeToIndexTexture(col, row, 1, 1, artboard.getBackgroundColor(col, row));
					
				}
				
			}
		
		});
		
	}
	
	public void forEachLooseArtboard(Consumer<Artboard> callback) {
		
		looseArtboards.forEach(callback);
		
	}
	
	public Iterator<Artboard> looseArtboards() {
		
		return looseArtboards.iterator();
		
	}
	
	public void forEachNonShallowCopiedArtboard(Consumer<Artboard> callback) {
		
		synchronized(allArtboards) {
			
			allArtboards.stream().filter(artboard -> !copier.isCopy(artboard)).forEach(callback);
			
		}
		
	}	
	
	public void forValidArtboardsForAnimation(Animation animation , Consumer<Artboard> callback) {
		
		//valid artboards are those who aren't shallow copies and who dont have a shallow copy in the animation 
		forEachNonShallowCopiedArtboard(artboard -> {
			
			if(animation.frameWidth() == 0 || (!animation.hasArtboard(artboard) && animation.matchesDimensions(artboard))) { 
				
				//this part stops the callback from being called on an artboard whose alias is in this animation.
				for(AnimationFrame frame : animation.frames) {
					
					if(copier.isCopy(frame.board) && copier.getSourceOf(frame.board) == artboard) return;
					
				}
				
				callback.accept(artboard);
				
			}						
			
		});
		
	}
	
	public boolean isCopy(Artboard artboard) {
		
		return copier.isCopy(artboard);
		
	}
	
	public boolean isCopySource(Artboard isSource) {
		
		return copier.isSource(isSource);
		
	}
	
	public Artboard getSource(Artboard copy) {
		
		return copier.getSourceOf(copy);
		
	}
	
	public void removeArtboard(Artboard artboard) {
		
		engine.renderer().removeRender(artboard.render());
		artboard.render().shutDown();
		
		Engine.THE_TEMPORAL.onTrue(() -> true, () -> {
			
			synchronized(allArtboards) {
				
				allArtboards.remove(artboard);

			}
			
			looseArtboards.remove(artboard);
			copier.removeCopy(artboard);
			animations.forEach(animation -> animation.removeFrame(artboard));
			
			arrangeArtboards();
		
		});
		
	}	
	
	public ArtboardPalette palette() {
		
		return visualPalette;
		
	}
	
	public synchronized Artboard getArtboard(final int index) {
		
		return allArtboards.get(index);
		
	}
	
	public synchronized Artboard getArtboard(final String name) {
		
		for(Artboard x : allArtboards) if(x.name.equals(name)) return x;
		throw new IllegalArgumentException(name + " does not name an artboard");
		
	}
	
	/* Artboard Copy Methods */
	
	public Artboard shallowCopy(Artboard source) {
		
		Artboard copy = copier.copy(source);
		engine.renderer().addRender(copy.render());
		addArtboard(copy);
		return copy;
				
		
		
	}
	
	/**
	 * Returns whether the given artboard was created as a result of a shallow copy operation.
	 * 
	 * @param isShallowCopied — artboard which may have been shallow copied
	 * @return {@code true} if {@code isShallowCopied} is a shallow copy of some other artboard.
	 */
	public boolean isShallowCopy(Artboard isShallowCopied) {
		
		return copier.isCopy(isShallowCopied);
		
	}
	
	/**
	 * Returns whether {@code hasShallowCopies} has at least one Artboard alive as a shallow copy.
	 * 
	 * @param hasShallowCopies — an artboard which is being queried for whether any shallow copies were made from it that are still alive
	 * @return {@code true} if at least one artboard exists which is a shallow copy of {@code hasShallowCopies}.
	 */
	public boolean hasShallowCopies(Artboard hasShallowCopies) {
		
		return copier.isSource(hasShallowCopies);
		
	}
	
	public int numberArtboards() {
		
		return allArtboards.size();
		
	}
	
	public int numberNonCopiedArtboards() {
		
		int nonCopies = 0;		
		for(Iterator<Artboard> iter = allArtboards.iterator() ; iter.hasNext() ; ) if(!copier.isCopy(iter.next())) nonCopies++;
		return nonCopies;
		
	}
	
	/**
	 * Creates a new artboard.
	 * 
	 * @param name — name of the artboard
	 * @param width — width of the artboard
	 * @param height — height of the artboard
	 * @return The new Artboard
	 */
	public Artboard createArtboard(String name , int width , int height) {

		Artboard newArtboard = new Artboard(name , width , height);
		
		forEachVisualLayerPrototype(vlP -> {
			
			VisualLayer layer = new VisualLayer(newArtboard , visualPalette , vlP);
			newArtboard.addVisualLayer(layer);
			
		});
		
		forEachNonVisualLayerPrototype(nvlP -> {
			
			NonVisualLayer layer = new NonVisualLayer(newArtboard , getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
			newArtboard.addNonVisualLayer(layer);
		
		});
		
		addArtboard(newArtboard);
		return newArtboard;
		
	}
	
	/**
	 * Creates a new artboard with a default name.
	 * 
	 * @param name — name of the artboard
	 * @param width — width of the artboard
	 * @param height — height of the artboard
	 * @return The new Artboard.
	 */
	public Artboard createArtboard(int width , int height) {
		
		return createArtboard(String.valueOf(numberNonCopiedArtboards()) , width , height);
		
	}
	
	/**
	 * Deep copies the source artboard, naming the copy the given name.
	 * 
	 * @param source — an existing artboard to make a deep copy of
	 * @param newArtboardName — name of the copied artboard
	 * @return The result of the copy.
	 */
	public Artboard deepCopy(Artboard source , String newArtboardName) {
		
		Artboard result = Artboard.deepCopy(newArtboardName, source, this);
		addArtboard(result);
		return result;
		
	}

	/**
	 * Deep copies the source artboard, giving the result a default name.
	 * 
	 * @param source — an existing artboard to make a deep copy of
	 * @return The result of the deep copy.
	 */
	public Artboard deepCopy(Artboard source) {
		
		return deepCopy(source , String.valueOf(numberNonCopiedArtboards()));
		
	}
	
	/**
	 * Creates a new animation with the given name.
	 * 
	 * @param name — name of this animation 
	 * @return The new Animation.
	 */
	public Animation createAnimation(String name) {
		
		Animation newAnimation = new Animation(name , engine::realtimeFrameTime);
		addAnimation(newAnimation);
		return newAnimation;
		
	}
	
	/**
	 * Creates a new nonvisual layer from the given parameters and gives a new copy of it to each artboard.
	 * 
	 * @param name — name of the nonvisual layer
	 * @param sizeBytes — size in bytes of pixels of the nonvisual layer
	 * @return Instance of the nonvisual layer prototype.
	 */
	public NonVisualLayerPrototype createNonVisualLayer(String name , int sizeBytes) {
		
		NonVisualLayerPrototype newNVL = new NonVisualLayerPrototype(sizeBytes , name);
		
		addNonVisualLayerPrototype(newNVL);
		forEachNonShallowCopiedArtboard(artboard -> {
			
			NonVisualLayer layer = new NonVisualLayer(artboard , getNonVisualPaletteBySize(sizeBytes) , newNVL);			
			artboard.addNonVisualLayer(layer);
			
		});
		
		return newNVL;
		
	}
	
	public VisualLayerPrototype createVisualLayer(String name) {
		
		VisualLayerPrototype newVL = new VisualLayerPrototype(name);
		addVisualLayerPrototype(newVL);
		forEachNonShallowCopiedArtboard(artboard -> artboard.addVisualLayer(new VisualLayer(artboard , palette() , newVL)));
		
		return newVL;
		
	}

	private void addArtboard(Artboard newArtboard) {

		synchronized(allArtboards) {
			
			allArtboards.add(newArtboard);
			
		}
		
		looseArtboards.add(newArtboard);

		arrangeArtboards();
		
	}
	
	private void addNonVisualLayerPrototype(NonVisualLayerPrototype newNonVisualLayerPrototype) {
		
		 nonVisualLayerPrototypes.add(newNonVisualLayerPrototype);
				
	}

	public void addVisualLayerPrototype(VisualLayerPrototype newVisualLayerPrototype) {
		
		visualLayerPrototypes.add(newVisualLayerPrototype);
	
	}
	
	private void addAnimation(Animation newAnimation) {
		
		animations.add(newAnimation);
		
	}
	
	void resizeIndexTextures() {
		
		forEachNonShallowCopiedArtboard(artboard -> {});
		
	}

 	private Layer loadLayer(
 		Artboard artboard , 
 		String layerName , 
 		boolean hiding , 
 		boolean locked , 
 		boolean isCompressed , 
 		byte[] pixels
 	) {
		
		Layer layer = artboard.getLayer(layerName);
		ByteBuffer uncompressed;		
		byte[] pixelData = pixels;
				
		if(isCompressed) {
		
			ByteBuffer compressed = memAlloc(pixelData.length).put(pixelData).flip();
			uncompressed = layer.decode(compressed);
			memFree(compressed);
			
			
		} else uncompressed = memAlloc(pixelData.length).put(pixelData).flip();
		
		//do stuff with uncompressed
		//uncompressed is a buffer of ten byte regions containing x and y coordinates on the layer and lookup x and y to put there
		while(uncompressed.hasRemaining()) {
				
			layer.put(new LayerPixel(uncompressed.getInt() , uncompressed.getInt() , uncompressed.get() , uncompressed.get()));
				
		}
			
		memFree(uncompressed); 		
		layer.setHiding(hiding);
		layer.setLock(locked);
		
		return layer;
	
	}

 	public boolean freemoveMode() {
 		
 		return freemoveMode;
 		
 	}
 	
 	public void toggleFreemoveMode() {
 		
 		freemoveMode = !freemoveMode;
 		
 	}
 	
 	public void runFreemove(float[] cursorWorldCoords) {
		
 		if(freemoveMode && currentArtboard != null) {
 		
			cursorWorldCoords[0] = (int) Math.floor(cursorWorldCoords[0]);
			cursorWorldCoords[1] = (int) Math.floor(cursorWorldCoords[1]);
			
			if(isLoose(currentArtboard)) { 
			
				currentArtboard.moveTo((int)cursorWorldCoords[0], (int)cursorWorldCoords[1]);
				if(freemoveCheckCollisions) for(Artboard x : allArtboards) {
					
					if(x != currentArtboard && ProjectExporter.colliding(currentArtboard, x)) {
						
						int[] deltas = ProjectExporter.collisionDeltas(currentArtboard , x);
						resolveCollision(currentArtboard , deltas[0] , deltas[1]);

					}
					
				}
				
			} else for(Animation animation : animations) if(animation.hasArtboard(currentArtboard)) {
				
				int animationWidthDiv2 = (animation.frameWidth() * animation.numberFrames()) / 2;
				
				int animationMidX = (int) animation.getFrame(animation.numberFrames() - 1).board().rightX() - animationWidthDiv2;
				int animationMidY = (int) animation.getFrame(0).board().midY();
				
				int deltaX = (int) (cursorWorldCoords[0] - animationMidX);
				int deltaY = (int) (cursorWorldCoords[1] - animationMidY);
				
				animation.forAllFrames(artboard -> artboard.translate(deltaX, deltaY));
				
				/*
				 * Resolves collisions between animations and other artboards by iterating over the boards of the animation and all 
				 * artboards not in the animation, resolving individual collisions between them, and then moving all artboards of the 
				 * animation accordingly.
				 */
				
				if(freemoveCheckCollisions) ResolveCollisions: for(var iter = animation.frames.iterator() ; iter.hasNext() ;) {
					
					Artboard artboard = iter.next().board;

					for(Artboard other : allArtboards) if(!animation.hasArtboard(other)) if(ProjectExporter.colliding(artboard, other)) {

						int[] deltas = ProjectExporter.collisionDeltas(artboard , other);
						animation.forAllFrames(moveArtboard -> resolveCollision(moveArtboard , deltas[0] , deltas[1]));				
						continue ResolveCollisions;
						
					}
					
				}
				
			}		
			
		}
 		
 	}
 	
 	private void resolveCollision(Artboard x , int deltaX , int deltaY) {

		if(deltaX > deltaY) x.translate(deltaX , 0);
		else x.translate(0 , deltaY);
		
 	}
 	
 	public boolean padAnimationFrames() {
 	
 		return padAnimationFrames;
 		
 	}

 	public void padAnimationFrames(boolean doPad) {
 		
 		this.padAnimationFrames = doPad;
 		engine.renderer().post(this::arrangeArtboards);
 		
 	}
 	
 	public void togglePadAnimationFrames() {
 	
 		padAnimationFrames = !padAnimationFrames;
 		engine.renderer().post(this::arrangeArtboards);
 		
 	}
 	
 	public boolean isLoose(Artboard artboard) {
 		
 		return looseArtboards.contains(artboard);
 		
 	}
 	
 	public boolean freemoveCheckCollisions() {
 		
 		return freemoveCheckCollisions;
 		
 	}

 	public void toggleFreemoveCheckCollisions() {
 		
 		freemoveCheckCollisions = !freemoveCheckCollisions;
 		
 	}
 	
	@Override public void shutDown() {

		forEachArtboard(artboard -> engine.removeRender(artboard.render()));		
		isFreed.set(true);

	}

	@Override public boolean isFreed() {

		return isFreed.get();
		
	}
 	
}
