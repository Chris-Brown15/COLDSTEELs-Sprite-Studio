package cs.csss.project;

import static cs.core.utils.CSUtils.specify;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import cs.core.utils.CSRefInt;
import cs.core.utils.ShutDown;
import cs.coreext.nanovg.NanoVGFrame;
import cs.coreext.nanovg.NanoVGTypeface;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.Control;
import cs.csss.engine.Engine;
import cs.csss.engine.Logging;
import cs.csss.project.io.CTSPFile;
import cs.csss.project.io.CTSPFile.AnimationChunk;
import cs.csss.project.io.CTSPFile.AnimationFrameChunk;
import cs.csss.project.io.CTSPFile.ArtboardChunk;
import cs.csss.project.io.CTSPFile.NonVisualLayerChunk;
import cs.csss.project.io.CTSPFile.NonVisualLayerDataChunk;
import cs.csss.project.io.CTSPFile.PaletteChunk;
import cs.csss.project.io.CTSPFile.VisualLayerDataChunk;
import cs.csss.project.io.ProjectSizeAndPositions;
import cs.csss.utils.CollisionUtils;
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
	@RenderThreadOnly public static void initializeArtboardShaders() {
		
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
		freemoveText = false;
	
	private ArtboardPalette visualPalette;
	private ArrayList<ArtboardPalette> nonVisualPalettes = new ArrayList<>(NonVisualLayerPrototype.MAX_SIZE_BYTES);

	private ArtboardCopier copier = new ArtboardCopier();
	
	private final List<Artboard> 
		allArtboards = new ArrayList<>() ,
		looseArtboards = new ArrayList<>();

	private final List<Animation> animations = new ArrayList<>();
	private final List<VisualLayerPrototype> visualLayerPrototypes = new ArrayList<>();
	private final List<NonVisualLayerPrototype> nonVisualLayerPrototypes = new ArrayList<>();
	private final List<VectorText> vectorTextBoxes = new ArrayList<>();
	
	private Artboard currentArtboard;
	private Animation currentAnimation;
	private VectorText currentText;	
	
	/**
	 * Creates a project.
	 * 
	 * @param engine — the engine
	 * @param name — the name of this project
	 * @param channelsPerPixel — the channels per pixel of this project
	 */
	@RenderThreadOnly public CSSSProject(Engine engine , final String name , int channelsPerPixel) {
		
		specify(channelsPerPixel > 0 && channelsPerPixel <= 4 , channelsPerPixel + " is not a valid number of channels per pixel.");

		this.engine = engine;
		
		setName(name);
		this.channelsPerPixel = channelsPerPixel;
		
		visualPalette = new ArtboardPalette(channelsPerPixel);

		for(int i = 1 ; i <= NonVisualLayerPrototype.MAX_SIZE_BYTES ; i++) nonVisualPalettes.add(new ArtboardPalette(i));
		
	 	addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));
		
	}

	/**
	 * Creates a nwe projcet without specifying its channels per pixel.
	 * 
	 * @param engine — the engine
	 * @param name — the name of this project
	 */
	public CSSSProject(Engine engine , final String name) {
	
		this.engine = engine;
		
		setName(name);
		addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));		
				
	}
	
	/**
	 * Creates a new project by loading from the given {@link cs.csss.project.io.CTSPFile CTSPFile}.
	 * 
	 * @param engine — the engine
	 * @param ctsp — a loaded CTSP file
	 */
 	@RenderThreadOnly public CSSSProject(Engine engine , CTSPFile ctsp) {

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
 		
 		Logging.sysDebug("Constructed Palettes");
 		
 		//artboards
 		for(ArtboardChunk x : ctsp.artboardChunks()) loadArtboard(x);
 		
 		Logging.sysDebug("Constructed Artboards");
 		
 		//animations
 		for(AnimationChunk x : ctsp.animationChunks()) loadAnimation(x);
 		
 		arrangeArtboards();
 		arrangeArtboards();
 		
 	}
	
 	/**
 	 * Initializes this project.
 	 */
	@RenderThreadOnly public void initialize() {
		
		visualPalette.initialize();
		for(ArtboardPalette x : nonVisualPalettes) x.initialize();
			
	}
	
	/**
	 * Deletes and removes all instances of the layer modeled by {@code layer} from the project.
	 * 
	 * @param layer — layer to delete
	 */
	@RenderThreadOnly public void deleteVisualLayer(VisualLayerPrototype layer) {
		
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
	
	@RenderThreadOnly public void deleteNonVisualLayer(NonVisualLayerPrototype layer) {
		
		synchronized(allArtboards) {
			
			allArtboards.forEach(artboard -> {
				
				if(copier.isCopy(artboard)) return;
				
				NonVisualLayer removed = artboard.getNonVisualLayer(layer);
				if(!removed.hiding) removed.hide(artboard);
				
				artboard.removeLayer(removed);
				
			});
			
			Engine.THE_TEMPORAL.onTrue(() -> true, () -> nonVisualLayerPrototypes.remove(layer));
			
		}
		
	}
	
	/* Animation Methods */
	
	/**
	 * Appends the given artboard to the current animation
	 * 
	 * @param artboard — an artboard
	 */
	@RenderThreadOnly public void appendArtboardToCurrentAnimation(Artboard artboard) {
		
		appendArtboardToCurrentAnimationDontArrange(artboard);
		arrangeArtboards();
		
	}	
	
	private void appendArtboardToCurrentAnimationDontArrange(Artboard artboard) {

		boolean wasLoose = looseArtboards.remove(artboard);
		
		if(wasLoose) {
			
			currentAnimation.appendArtboard(artboard);
			
		} else {
			
			Artboard shallowCopy = engine.renderer().make(() -> copier.copy(artboard)).get();
			currentAnimation.appendArtboard(shallowCopy);
			Engine.THE_TEMPORAL.onTrue(() -> true , () -> allArtboards.add(shallowCopy));
			
		}		
		
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
	
	/**
	 * Removes the given artboard from the current animation.
	 * 
	 * @param artboard — an artboard
	 */
	@RenderThreadOnly public void removeArtboardFromCurrentAnimation(Artboard artboard) {
		
		removeArtboardFromCurrentAnimation(currentAnimation.indexOf(artboard));
		
	}
	
	/**
	 * Removes the animation frame at the given index from the current animation.
	 * 
	 * @param frameIndex — index of an animation frame for the current animation
	 */
	@RenderThreadOnly public void removeArtboardFromCurrentAnimation(int frameIndex) {

		Optional<Artboard> removed = removeArtboardFromAnimation(currentAnimation , frameIndex);		
		if(removed.isPresent()) looseArtboards.add(removed.get());		
		arrangeArtboards();
		
	}
	
	/**
	 * Deletes the current animation from the application. No artboards are deleted, only the animation object itself.
	 */
	@RenderThreadOnly public void deleteAnimation() {
		
		Optional<Artboard> results;
		
		while(currentAnimation.numberFrames() > 0) { 
			
			results = removeArtboardFromAnimation(currentAnimation , 0);
			if(results.isPresent()) looseArtboards.add(results.get());
			
		}
		
		animations.remove(currentAnimation);
		currentAnimation = null;
		
		arrangeArtboards();
		
	}
	
	/**
	 * Draws all artboards in this project.
	 */
	@RenderThreadOnly public void renderAllArtboards() {
		
		renderAllArtboards(currentShader);
		
	}

	/**
	 * Draws all vector text boxes in this project
	 *  
	 * @param frame — NanoVG Frame for the current application frame
	 */
	@RenderThreadOnly public void renderAllVectorTextBoxes(NanoVGFrame frame) {
		
		vectorTextBoxes.forEach(textBox -> textBox.renderBoxAndText(frame));
		
	}
		
	/**
	 * Renders all artboards with the given shader.
	 * 
	 * @param shader — a shader to use for rendering
	 */
	@RenderThreadOnly public void renderAllArtboards(CSSSShader shader) {
		
		forEachArtboard(artboard -> {

			if(artboard.isActiveLayerVisual()) visualPalette.activate();
			else nonVisualPalettes.get(artboard.activeLayerChannelsPerPixel() - 1).activate();
			
			shader.updateTextures(artboard.activeLayer().palette , artboard.indexTexture());
			shader.activate();			
			artboard.draw();
			
		});
		
	}
	
	/**
	 * Draws both all artboards and all vector text boxes. 
	 * 
	 * @param shader — a shader for the artboards
	 * @param frame — the nano VG frame for the current program frame
	 */
	@RenderThreadOnly public void renderEverything(CSSSShader shader , NanoVGFrame frame) {
		
		renderAllArtboards(shader);
		renderAllVectorTextBoxes(frame);
		
	}
	
	/**
	 * Arranges loose artboards. The first loose artboard will be at (0 , 0) with subsequent ones extending rightward from it.
	 */
	private int arrangeLooseArtboardsNew() {

		if(looseArtboards.size() == 0) return 0;
		
		return engine.renderer().make(() -> {

			Artboard previous = looseArtboards.get(0);
			int greatestHeight = previous.height(); 
			previous.moveTo(0 , 0);
			
			for(int i = 1 ; i < looseArtboards.size() ; i++) {
						
				Artboard current = looseArtboards.get(i);
				previous = looseArtboards.get(i - 1);
								
				float lastRightX = previous.rightX();
				int translationX = (int) Math.ceil(lastRightX) + current.width();
				current.moveTo(translationX, 0);
				if(greatestHeight < current.height()) greatestHeight = current.height();				
				
			}
					
			return greatestHeight;
			
		}).get();
	  
	}
	
	/**
	 * Arranges all artboards in the applicaton. Artboards are arranged so that animations who take up the most horizontal space are placed
	 * higher, with descending horizontal space animations right below them. At the bottom are the loose artboards.
	 */
	@RenderThreadOnly public void arrangeArtboards() {
		
	 	engine.renderer().post(() -> {

	 		CSRefInt rowHeightAccum = new CSRefInt(arrangeLooseArtboardsNew() + 25);	 		
	 		animations.sort((animation1 , animation2) -> animation1.getTotalWidth() - animation2.getTotalWidth()); 		
	 		animations.stream().filter(animation -> !animation.isEmpty()).forEach(x -> {

		 		Iterator<Artboard> iter = x.frames.stream().map(frame -> frame.board).iterator();
		 		Artboard previous = iter.next() , current;
		 		
		 		rowHeightAccum.add(x.frameHeight() / 2);
		 		
		 		previous.moveTo(0, rowHeightAccum.intValue());
		 			 		
		 		for( ; iter.hasNext() ; previous = current) {
		 			
		 			current = iter.next();
		 			current.moveTo((int)previous.rightX() + (current.width() / 2), rowHeightAccum.intValue());
		 				 			
		 		}
		 		
		 		rowHeightAccum.add(x.frameHeight());
		 				 	
	 		});
	 			 		
	 	});
		
	}
	
	/**
	 * Invokes {@code callback} on each animation of this project
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachAnimation(Consumer<Animation> callback) {
		
		animations.forEach(callback);
		
	}
	
	/**
	 * Returns an iterator over the animations of this project.
	 * 
	 * @return Iterator over the animations of this project.
	 */
	public Iterator<Animation> animations() {
		
		return animations.iterator();
		
	}

	/**
	 * Returns an iterator over all artboards of this project.
	 * 
	 * @return Iterator over all artboards of this project.
	 */
	public Iterator<Artboard> allArtboards() {
		
		return allArtboards.iterator();
		
	}

	/**
	 * Invokes {@code callback} on each visual layer prototype of this project
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachVisualLayerPrototype(Consumer<VisualLayerPrototype> callback) {
		
		visualLayerPrototypes.forEach(callback);
		
	}

	/**
	 * Invokes {@code callback} on each nonvisual layer prototype of this project
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachNonVisualLayerPrototype(Consumer<NonVisualLayerPrototype> callback) {
		
		nonVisualLayerPrototypes.forEach(callback);
		
	}

	/**
	 * Returns an iterator over all nonvisual layer prototypes of this project.
	 * 
	 * @return Iterator over all nonvisual layer prototypes of this project.
	 */
	public Iterator<NonVisualLayerPrototype> nonvisualLayers() {
		
		return nonVisualLayerPrototypes.iterator();
		
	}
	
	/**
	 * Returns an iterator over all visual layer prototypes of this project.
	 * 
	 * @return Iterator over all visual layer prototypes of this project.
	 */
	public Iterator<VisualLayerPrototype> visualLayers() {
		
		return visualLayerPrototypes.iterator();
		
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

	/**
	 * Invokes {@code callback} for each artboard that is a shallow copy of {@code source}. 
	 * 
	 * <p>
	 * 	A shallow copy of an artboard is produced when an existing artboard is added to an animation while it is already in a different
	 * 	one. Shallow copies share graphics resources with their sources, so changes made to a source or shallow copy reflect on all the
	 * 	other shallow copies and source.
	 * </p>
	 * 
	 * @param source — a source artboard
	 * @param callback — code to invoke for each shallow copy
	 */
	public void forEachCopyOf(Artboard source , Consumer<Artboard> callback) {
		
		copier.forEachCopyOf(source , callback);
		
	}
	
	/**
	 * Invokes {@code callback} for all artboards that are shallow copies.
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachShallowCopy(Consumer<Artboard> callback) {
		
		copier.forEachCopy(callback);
		
	}
	
	/**
	 * Invokes {@code callback} for each palette, of which there are 5 total.
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachPalette(Consumer<ArtboardPalette> callback) {
		
		callback.accept(visualPalette);
		nonVisualPalettes.forEach(callback);
		
	}

	/**
	 * Invokes {@code callback} for each nonvisual palette, of which there are 4, one for each channel size.
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachNonVisualPalette(Consumer<ArtboardPalette> callback) {
		
		nonVisualPalettes.forEach(callback);
		
	}

	/**
	 * Invokes {@code callback} for each vector text box.
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachVectorTextBox(Consumer<VectorText> callback) {
		
		vectorTextBoxes.forEach(callback);
		
	}

	/**
	 * Returns an iterator over all vector text boxes of this project.
	 * 
	 * @return Iterator over all vector text boxes of this project.
	 */
	public Iterator<VectorText> textBoxes() {
		
		return vectorTextBoxes.iterator();
		
	}
	
	/**
	 * Returns the number of channels per pixel for visual layers for this project.
	 * 
	 * @return Number of channels per pixel for visual layers.
	 */
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}
	
	/**
	 * Gets a visual layer prototype at the given index.
	 * 
	 * @param index — index of a visual layer prototype within the list of visual layer prototypes
	 * @return VisualLayerPrototype at the given index.
	 */
	public VisualLayerPrototype visualLayerPrototype(int index) {
		
		return visualLayerPrototypes.get(index);
		
	}
	
	/**
	 * Returns the number of visual layer prototypes in this project.
	 * 
	 * @return Number of visual layer prototypes.
	 */
	public int numberVisualLayers() {
		
		return visualLayerPrototypes.size();
		
	}

	/**
	 * Returns the number of nonvisual layer prototypes in this project.
	 * 
	 * @return Number of nonvisual layer prototypes.
	 */
	public int numberNonVisualLayers() {
		
		return nonVisualLayerPrototypes.size();
		
	}

	/**
	 * Returns the number of animations in this project.
	 * 
	 * @return Number of animations.
	 */
	public int numberAnimations() {
		
		return animations.size();
		
	}
	
	/**
	 * Gets the index of the given artboard in the list of all artboards for this project.
	 * 
	 * @param artboard — an artboard whose index is being queried 
	 * @return Index of {@code artboard} in this project.
	 */
	public int getArtboardIndex(Artboard artboard) {
		
		synchronized(allArtboards) {
			
			return allArtboards.indexOf(artboard);
		
		}
		
	}
	
	/**
	 * Sets this project's current artboard to whichever artboard the given cursor world coordinates fall into. This will not change the 
	 * current artboard if none is under the cursor.
	 * 
	 * @param cursorWorldX — x world coordinate of the cursor 
	 * @param cursorWorldY — y world coordinate of the cursor 
	 */
	public void setCurrentArtboardByMouse(float cursorWorldX , float cursorWorldY) {
		
		synchronized(allArtboards) {
			
			for(Artboard x : allArtboards) if(x.isCursorInBounds(cursorWorldX , cursorWorldY)) { 
				
				currentArtboard = x;
				return;
				
			}
		
		}
		
	}
	
	/**
	 * Returns the current artboard.
	 * 
	 * @return The current artboard.
	 */
	public Artboard currentArtboard() {
		
		return currentArtboard;
		
	}
	
	/**
	 * Sets the current artboard.
	 * 
	 * @param currentArtboard — new current artboard
	 */
	public void currentArtboard(Artboard currentArtboard) {
		
		this.currentArtboard = currentArtboard;
		
	}

	/**
	 * Returns the current animation.
	 * 
	 * @return The current animation.
	 */
	public Animation currentAnimation() {
		
		return currentAnimation;
		
	}

	/**
	 * Sets the current animation.
	 * 
	 * @param newCurrent — new current animation
	 */
	public void currentAnimation(Animation newCurrent) {
		
		this.currentAnimation = newCurrent;
		
	}
	
	/**
	 * Sets the name of this project.
	 * 
	 * @param name — new name for this project
	 */
	public void setName(String name) {
		
		this.name = name.strip();
		
	}
		
	/**
	 * Gets the name of this project.
	 * 
	 * @return Name of this project.
	 */
	public String name() {
		
		return name;
		
	}
	
	/**
	 * Gets the channels per pixel of the current layer for the current artboard. If no artboard is active, returns 4.
	 * 
	 * @return The number of channels per pixel of the current layer for the current artboard, or 4 if none is active.
	 */
	public int getChannelsPerPixelOfCurrentLayer() {
		
		if(currentArtboard != null) {
			
			if(currentArtboard.activeLayer() instanceof VisualLayer) return channelsPerPixel;
			else return ((NonVisualLayer) currentArtboard.activeLayer()).bytesPerPixel();
		
		} else return channelsPerPixel;
		
	}
	
	/**
	 * Gets a nonvisual palette by {@code sizeBytes}, which corresponds to the number of channels per pixel for the returned palette. 
	 * 
	 * @param channels — the channels per pixel of the palette being queried
	 * @return Nonvisual artboard palette used for nonvisual layers whose channel per pixel is {@code sizeBytes}.
	 */
	public ArtboardPalette getNonVisualPaletteBySize(int channels) {
		
		specify(channels > 0 && channels <= NonVisualLayerPrototype.MAX_SIZE_BYTES , "Invalid size in bytes for a nonvisual layer.");
		
		return nonVisualPalettes.get(channels - 1);
		
	}
	
	/**
	 * Invokes {@code callback} for each loose artboard.
	 * <p>
	 * 	A loose artboard is an artboard that is not in any animation. they would be the artboards at the bottom of the project, below any
	 * 	animations.
	 * </p>
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachLooseArtboard(Consumer<Artboard> callback) {
		
		looseArtboards.forEach(callback);
		
	}
	
	/**
	 * Returns an iterator over the loose artboards of this project.
	 * 
	 * @return Iterator over the loose artboards
	 */
	public Iterator<Artboard> looseArtboards() {
		
		return looseArtboards.iterator();
		
	}
	
	/**
	 * Invokes{@code callback} for each artboard in this project that is not a shallow copy of another artboard. These could be loose or
	 * nonloose artboards, but for nonloose artboards, they are artboards which are not in more than one animation.
	 * 
	 * @param callback — code to invoke
	 */
	public void forEachNonShallowCopiedArtboard(Consumer<Artboard> callback) {
		
		synchronized(allArtboards) {
			
			allArtboards.stream().filter(artboard -> !copier.isCopy(artboard)).forEach(callback);
			
		}
		
	}	
	
	/**
	 * Given some animation, {@code callback} is invoked for each artboard of the project which is a valid candidate for being added to the
	 * animation. An artboard is valid if it is the same dimensions as artboards already in the animation, and it is not already in the 
	 * animation. If no artboard is in the animation, all nonshallow artboards are valid.
	 * 
	 * @param animation — an animation
	 * @param callback — code to invoke for each valid artboard for {@code animation}
	 */
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
	
	/**
	 * Returns whether the given artboard is a shallow copy of another.
	 * 
	 * @param artboard — an artboard
	 * @return {@code true} if {@code artboard} is a shallow copy of another artboard.
	 */
	public boolean isCopy(Artboard artboard) {
		
		return copier.isCopy(artboard);
		
	}

	/**
	 * Returns whether the given artboard is a source of other shallow artboards. Artboards that are themselves shallow and artboards that
	 * have not been used to create other artboards will return {@code false}.
	 * 
	 * @param artboard — an artboard
	 * @return {@code true} if {@code artboard} is a source for shallow copy of another artboard.
	 */
	public boolean isCopySource(Artboard isSource) {
		
		return copier.isSource(isSource);
		
	}
	
	/**
	 * Given some shallow copied artboard, returns it's source. 
	 * 
	 * @param copy — a shallow copied artboard
	 * @return The source artboard for the copy.
	 */
	public Artboard getSource(Artboard copy) {
		
		return copier.getSourceOf(copy);
		
	}
	
	/**
	 * Removes the given artboard from this project.
	 * 
	 * @param artboard — an artboard to remove
	 */
	@RenderThreadOnly public void removeArtboard(Artboard artboard) {
		
		engine.renderer().removeRender(artboard.render());
		artboard.render().shutDown();
		
		if(artboard == currentArtboard) currentArtboard = null;
		
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
	
	/**
	 * Returns the current palette.
	 * 
	 * @return The current palette for the the active layer of the active artboard.
	 */
	public ArtboardPalette currentPalette() {
		
		if(currentArtboard == null) return null;
		if(currentArtboard.isActiveLayerVisual()) return visualPalette;
		else return getNonVisualPaletteBySize(currentArtboard.activeLayerChannelsPerPixel());
		
	}
	
	/**
	 * Returns the visual palette.
	 * 
	 * @return The single visual palette for this project.
	 */
	public ArtboardPalette visualPalette() {
		
		return visualPalette;
		
	}
	
	/**
	 * Gets an artboard by the given name. 
	 *  
	 * @param name — name of an artboard
	 * @return An artboard of the given name.
	 * @throws IllegalArgumentException if the given name does not belong to any artboard
	 */
	public synchronized Artboard getArtboard(final String name) throws IllegalArgumentException {
		
		for(Artboard x : allArtboards) if(x.name.equals(name)) return x;
		throw new IllegalArgumentException(name + " does not name an artboard");
		
	}
	
	/* Artboard Copy Methods */
	
	/**
	 * Takes a source artboard and returns a shallow copy of it by the semantics of 
	 * {@link cs.csss.project.Artboard#shallowCopy(String, Artboard) shallowCopy(String , Artboard)}.
	 * 
	 * @param source — source artboard
	 * @return Shallow copy of {@code source}.
	 */
	@RenderThreadOnly public Artboard shallowCopy(Artboard source) {
		
		Artboard copy = copier.copy(source);
		engine.renderer().addRender(copy.render());
		addArtboard(copy);
		return copy;
				
		
		
	}
	
	/**
	 * Calculates and returns the number of artboards that are not shallow copies. 
	 * 
	 * @return Number of artboards that are not shallow copies.
	 */
	public int getNumberNonCopiedArtboards() {
		
		CSRefInt counter = new CSRefInt(0);
		allArtboards.stream().filter(artboard -> !copier.isCopy(artboard)).forEach(artboard -> counter.inc());
		return counter.intValue();
		
	}
	
	/**
	 * Creates a new artboard.
	 * 
	 * @param name — name of the artboard
	 * @param width — width of the artboard
	 * @param height — height of the artboard
	 * @return The new Artboard.
	 */
	@RenderThreadOnly public Artboard createArtboard(String name , int width , int height) {

		return createArtboard(name , width, height , true);
		
	}
	
	/**
	 * Creates a new artboard.
	 * 
	 * @param name — name of the artboard
	 * @param width — width of the artboard
	 * @param height — height of the artboard
	 * @param setCheckeredBackground — whether to set the checkered background
	 * @return The new Artboard.
	 */
	@RenderThreadOnly public Artboard createArtboard(String name , int width , int height , boolean setCheckeredBackground) {

		Artboard artboard = createArtboardDontArrange(name , width , height , setCheckeredBackground);		
		arrangeArtboards();
		return artboard;
		
	}
	
	/**
	 * Creates a new artboard but does not rearrange artboards.
	 * 
	 * @param name — name of the artboard
	 * @param width — width of the artboard
	 * @param height — height of the artboard
	 * @param setCheckeredBackground — whether to set the texture to a checkered background
	 * @return The new Artboard
	 */
	private Artboard createArtboardDontArrange(String name , int width , int height , boolean setCheckeredBackground) {

		Artboard newArtboard = new Artboard(name , width , height , setCheckeredBackground);
		
		forEachVisualLayerPrototype(vlP -> {
			
			VisualLayer layer = new VisualLayer(newArtboard , visualPalette , vlP);
			newArtboard.addVisualLayer(layer);
			
		});
		
		forEachNonVisualLayerPrototype(nvlP -> {
			
			NonVisualLayer layer = new NonVisualLayer(newArtboard , getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
			newArtboard.addNonVisualLayer(layer);
		
		});
		
		addArtboardDontArrange(newArtboard);
		
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
	@RenderThreadOnly public Artboard createArtboard(int width , int height) {
		
		return createArtboard(String.valueOf(getNumberNonCopiedArtboards()) , width , height);
		
	}
	
	/**
	 * Deep copies the source artboard, naming the copy the given name.
	 * 
	 * @param source — an existing artboard to make a deep copy of
	 * @param newArtboardName — name of the copied artboard
	 * @return The result of the copy.
	 */
	@RenderThreadOnly public Artboard deepCopy(Artboard source , String newArtboardName) {
		
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
	@RenderThreadOnly public Artboard deepCopy(Artboard source) {
		
		return deepCopy(source , String.valueOf(getNumberNonCopiedArtboards()));
		
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
	 * Creates a new nonvisual layer prototype from the given parameters and gives a new copy of it to each artboard.
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
	
	/**
	 * Creates a new visual layer prototype from the given parameters and gives a new copy of it to each artboard.
	 * 
	 * @param name — name of the visual layer prototype
	 * @return New visual layer prototype.
	 */
	public VisualLayerPrototype createVisualLayer(String name) {
		
		VisualLayerPrototype newVL = new VisualLayerPrototype(name);
		addVisualLayerPrototype(newVL);
		forEachNonShallowCopiedArtboard(artboard -> artboard.addVisualLayer(new VisualLayer(artboard , visualPalette() , newVL)));
		
		return newVL;
		
	}

	private void addArtboard(Artboard newArtboard) {

		synchronized(allArtboards) {
			
			allArtboards.add(newArtboard);
			
		}
		
		looseArtboards.add(newArtboard);

		arrangeArtboards();
		
	}
	
	private void addArtboardDontArrange(Artboard newArtboard) {

		synchronized(allArtboards) {
			
			allArtboards.add(newArtboard);
			
		}
		
		looseArtboards.add(newArtboard);

	}
	
	private void addNonVisualLayerPrototype(NonVisualLayerPrototype newNonVisualLayerPrototype) {
		
		 nonVisualLayerPrototypes.add(newNonVisualLayerPrototype);
				
	}

	/**
	 * Adds the given visual layer prototype to this project's list of visual layer prototypes.
	 * 
	 * @param newVisualLayerPrototype — visual layer prototype to add
	 */
	public void addVisualLayerPrototype(VisualLayerPrototype newVisualLayerPrototype) {
		
		visualLayerPrototypes.add(newVisualLayerPrototype);
	
	}
	
	/**
	 * Adds the given animation to this project's list of animations.
	 * 
	 * @param newAnimation — animation to add
	 */
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

 	/**
 	 * Returns whether freemove mode is enabled.
 	 * 
 	 * @return {@code true} if freemove mode is enabled.
 	 */
 	public boolean freemoveMode() {
 		
 		return freemoveMode;
 		
 	}
 	
 	/**
 	 * Toggles on or off freemove mode.
 	 */
 	public void toggleFreemoveMode() {
 		
 		freemoveMode = !freemoveMode;
 		
 	}
 	
 	/**
 	 * Adds a vector text box to the project which will use the given typeface.
 	 * 
 	 * @param typeface — a nanoVG typeface
 	 */
 	public void addVectorTextBox(NanoVGTypeface typeface) {
 		
 		vectorTextBoxes.add(new VectorText(typeface));
 		
 	}

 	/**
 	 * Adds a vector text box containing the given text to the project which will use the given typeface.
 	 * 
 	 * @param typeface — a nanoVG typeface
 	 * @param sourceText — text for the textbox to contain
 	 */
 	public void addVectorTextBox(NanoVGTypeface typeface , String sourceText) {
 		
 		vectorTextBoxes.add(new VectorText(typeface , sourceText));
 		
 	}
 	
 	/**
 	 * Returns the current vector text box.
 	 * 
 	 * @return Current vector text box.
 	 */
 	public VectorText currentTextBox() {
 		
 		return currentText;
 		
 	}
 	
 	/**
 	 * Sets the current vector text box to {@code newCurrent}.
 	 * 
 	 * @param newCurrent — vector text box to make current
 	 */
 	public void currentTextBox(VectorText newCurrent) {
 		
 		if(newCurrent == currentText) currentText = null;
 		else this.currentText = newCurrent;
 		
 	}
 	
 	/**
 	 * Handles freemove mode.
 	 * 
 	 * @param cursorWorldCoords — cursor world coordinates
 	 */
 	@RenderThreadOnly public void runFreemove(float[] cursorWorldCoords) {
		
 		if(freemoveMode && currentArtboard != null) freemoveArtboard(cursorWorldCoords);
 		if(freemoveText && currentText != null) freemoveTextBox(cursorWorldCoords);
 		else if (!freemoveText && currentText != null) dragTextBox(cursorWorldCoords); 		
 		
 	}
 	
 	private void freemoveArtboard(float[] cursorWorldCoords) {

		cursorWorldCoords[0] = (int) Math.floor(cursorWorldCoords[0]);
		cursorWorldCoords[1] = (int) Math.floor(cursorWorldCoords[1]);
		
		if(isLoose(currentArtboard)) { 
		
			currentArtboard.moveTo((int)cursorWorldCoords[0], (int)cursorWorldCoords[1]);
			if(freemoveCheckCollisions) for(Artboard x : allArtboards) {
				
				if(x != currentArtboard && CollisionUtils.colliding(currentArtboard.positions, x.positions)) {
					
					int[] deltas = CollisionUtils.collisionDeltas(currentArtboard.positions , x.positions);
					resolveCollision(currentArtboard , deltas[0] , deltas[1]);

				}
				
			}
			
		} else for(Animation animation : animations) if(animation.hasArtboard(currentArtboard)) {
			
			int animationWidthDiv2 = (animation.frameWidth() * animation.numberFrames()) / 2;
			
			int animationMidX = (int) animation.getFrame(animation.numberFrames() - 1).board().rightX() - animationWidthDiv2;
			int animationMidY = (int) animation.getFrame(0).board().midY();
			
			int deltaX = (int) (cursorWorldCoords[0] - animationMidX);
			int deltaY = (int) (cursorWorldCoords[1] - animationMidY);
			
			animation.forAllArtboards(artboard -> artboard.translate(deltaX, deltaY));
			
			/*
			 * Resolves collisions between animations and other artboards by iterating over the boards of the animation and all 
			 * artboards not in the animation, resolving individual collisions between them, and then moving all artboards of the 
			 * animation accordingly.
			 */
			
			if(freemoveCheckCollisions) ResolveCollisions: for(var iter = animation.frames.iterator() ; iter.hasNext() ;) {
				
				Artboard artboard = iter.next().board;

				for(Artboard other : allArtboards) if(!animation.hasArtboard(other)) {
					
					if(CollisionUtils.colliding(artboard.positions, other.positions)) {

						int[] deltas = CollisionUtils.collisionDeltas(artboard.positions , other.positions);
						animation.forAllArtboards(moveArtboard -> resolveCollision(moveArtboard , deltas[0] , deltas[1]));
						continue ResolveCollisions;
						
					}
					
				}
				
			}
			
		}		
	
 	}
 	
 	private void freemoveTextBox(float[] cursorCoords) {
 		
 		currentText.moveTo(cursorCoords[0] , cursorCoords[1]);
 		if(Control.ARTBOARD_INTERACT.pressed()) freemoveText = false;
 		
 	}
 	
 	private void dragTextBox(float[] cursorCoords) {
 		
 		if(Control.MOVE_SELECTION_AREA.pressed()) currentText.moveCorner(cursorCoords[0] , cursorCoords[1]);
 		
 	}
 	
 	private void resolveCollision(Artboard x , int deltaX , int deltaY) {

		if(deltaX > deltaY) x.translate(deltaX , 0);
		else x.translate(0 , deltaY);
		
 	}
 	
 	/**
 	 * Returns whether {@code artboard} is a loose artboard.
 	 * 
 	 * @param artboard — an artboard
 	 * @return {@code true} if {@code artboard} is loose.
 	 */
 	public boolean isLoose(Artboard artboard) {
 		
 		return looseArtboards.contains(artboard);
 		
 	}
 	
 	/**
 	 * Returns whether to check collisions during freemove mode.
 	 * 
 	 * @return {@code true} if collisions are being checked during freemove mode.
 	 */
 	public boolean freemoveCheckCollisions() {
 		
 		return freemoveCheckCollisions;
 		
 	}

 	/**
 	 * Toggles on or off collision checking during freemove mode.
 	 */
 	public void toggleFreemoveCheckCollisions() {
 		
 		freemoveCheckCollisions = !freemoveCheckCollisions;
 		
 	}
 	
 	/**
 	 * Removes the given text box from this project.
 	 * 
 	 * @param text — text bxo to remove
 	 */
 	public void removeTextBox(VectorText text) {
 		
 		vectorTextBoxes.remove(text);
 		text.shutDown();
 		
 	}
 	
 	/**
 	 * Toggles on or off whether currently moving text boxes.
 	 */
 	public void toggleMovingText() {
 		
 		freemoveText = !freemoveText;
 		
 	}
 	
	/**
	 * Returns whether currently moving text.
	 * 
	 * @return Whether currently moving text.
	 */
	public boolean movingText() {
		
		return freemoveText;
		
	}

	/**
	 *  Sets the state of moving text.
	 * 
	 * @param movingText — whether to move text around
	 */
	public void movingText(boolean movingText) {
		
		this.freemoveText = movingText;
		
	}

	/**
	 * Computes size and position data needed for exporting.
	 * 
	 * @return Record storing width, height, and midpoint information.
	 */
	public ProjectSizeAndPositions getProjectSizeAndPositions() {

		//gather information about the state of the objects being saved
		
		//world coordinates notating the extreme points of the project
		float 	
			rightmostX = 0 ,
			leftmostX = Integer.MAX_VALUE ,
			uppermostY = 0 ,
			lowermostY = Integer.MAX_VALUE;
		
	 	Iterator<Artboard> artboards = allArtboards();
		
	 	while(artboards.hasNext()) {
			
			Artboard x = artboards.next();
			
			//dont use else if's here because if there is only one artboard, we wont set all values, which we need to do.
			if(x.rightX() > rightmostX) rightmostX = x.rightX();
			if(x.leftX() < leftmostX) leftmostX = x.leftX();
			if(x.topY() > uppermostY) uppermostY = x.topY();
			if(x.bottomY() < lowermostY) lowermostY = x.bottomY();
			
		}
		
		float
			width = rightmostX - leftmostX ,
			height = uppermostY - lowermostY ,				
			midpointX = rightmostX - (width / 2) ,
			midpointY = uppermostY - (height / 2);
		
		return new ProjectSizeAndPositions(leftmostX , rightmostX , lowermostY , uppermostY , width , height , midpointX , midpointY);
		
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
 		
 		Artboard newArtboard = createArtboardDontArrange(chunk.name() , chunk.width() , chunk.height() , true);
 		 		
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
 			
 			appendArtboardToCurrentAnimationDontArrange(getArtboard(y.artboardName()));
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
 	
	@Override public void shutDown() {

		forEachArtboard(artboard -> engine.removeRender(artboard.render()));		
		vectorTextBoxes.forEach(VectorText::shutDown);
		isFreed.set(true);
		
	}
	
	@Override public boolean isFreed() {

		return isFreed.get();
		
	}
 	
}
