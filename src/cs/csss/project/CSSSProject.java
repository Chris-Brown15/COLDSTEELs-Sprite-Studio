package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

import cs.csss.core.Engine;

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
public class CSSSProject {  

	public static final String dataDir = new File("data/").getAbsolutePath() + "/";

	public static boolean isValidProjectName(final String prospectiveName) {
		
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

	private final Engine engine;
	
	private int channelsPerPixel = -1;
	
	private boolean paletted = false;
	
	private ArtboardPalette visualPalette;
	private ArrayList<ArtboardPalette> nonVisualPalettes = new ArrayList<>(NonVisualLayerPrototype.MAX_SIZE_BYTES);

	private ArtboardCopier copier = new ArtboardCopier();
	
	private final LinkedList<Artboard> 
		allArtboards = new LinkedList<>() ,
		looseArtboards = new LinkedList<>() 
	;
	
	private final LinkedList<Animation> animations = new LinkedList<>();
	private final LinkedList<VisualLayerPrototype> visualLayerPrototypes = new LinkedList<>();
	private final LinkedList<NonVisualLayerPrototype> nonVisualLayerPrototypes = new LinkedList<>();
	
	private Artboard currentArtboard;
	private Animation currentAnimation;
	
	public CSSSProject(Engine engine , final String name , int channelsPerPixel , final boolean paletted , boolean makeDefaultLayer) {
		
		specify(channelsPerPixel > 0 && channelsPerPixel <= 4 , channelsPerPixel + " is not a valid number of channels per pixel.");
		
		specify(isValidProjectName(name) , name + " is not a valid project name.");
		
		this.engine = engine;
		
		setName(name);
		this.channelsPerPixel = channelsPerPixel ; this.paletted = paletted;
		
		byte tff = (byte) 255;
		visualPalette = new ArtboardPalette(channelsPerPixel , tff , tff , tff , tff);

		for(int i = 1 ; i <= NonVisualLayerPrototype.MAX_SIZE_BYTES ; i++) { 
			
			nonVisualPalettes.add(new ArtboardPalette(i , tff , tff , tff , tff));
		
		}
		
	 	if(makeDefaultLayer) addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));
		
	}

	public CSSSProject(Engine engine , final String name) {
	
		specify(isValidProjectName(name) , name + " is not a valid project name.");
		
		this.engine = engine;
		
		setName(name);
		addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));		
				
	}
	
 	public CSSSProject(Engine engine , ProjectMeta meta) {

		this(engine , meta.name() , meta.channelsPerPixel() , meta.paletted() , false);
		
		byte[] nvlSizes = meta.nonVisualLayerSizes();
		
		String[] 
			nvlNames = meta.nonVisualLayerNames() ,
			vlNames = meta.visualLayerNames() ,
			animations = meta.animations()
		;
		
		for(int i = 0 ; i < nvlSizes.length ; i++) { 
			
			addNonVisualLayerPrototype(new NonVisualLayerPrototype(nvlSizes[i] , nvlNames[i]));
			
		}

		for(int i = 0 ; i < vlNames.length ; i++) addVisualLayerPrototype(new VisualLayerPrototype(vlNames[i]));
		
		for(int i = 0 ; i < animations.length ; i++) addAnimation(new Animation(animations[i]));
	
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
			
			//if the removed frame is a source of other shallow copies, we look through each animation until we find a shallow copy that was
			//made from the removed one. In that case, we replace the shallow copy with the original.
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
		
		forEachArtboard(Artboard::draw);
		
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
					arrangeAnimation(x , 0 , 2 , 0);
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
					arrangeAnimation(x , smallestXPosition + (x.frameWidth() / 2) , 2 , heightAccum);
					heightAccum += (height / 2) + 5;
					
				}
				
			});
		
		}
		
	}
	
	public void addArtboard(Artboard newArtboard) {

		synchronized(allArtboards) {
			
			allArtboards.add(newArtboard);
			
		}
		
		looseArtboards.add(newArtboard);

		arrangeArtboards();
		
	}
	
	public void addNonVisualLayerPrototype(NonVisualLayerPrototype newNonVisualLayerPrototype) {
		
		 nonVisualLayerPrototypes.add(newNonVisualLayerPrototype);
				
	}

	public void addVisualLayerPrototype(VisualLayerPrototype newVisualLayerPrototype) {
		
		visualLayerPrototypes.add(newVisualLayerPrototype);
	
	}
	
	public void addAnimation(Animation newAnimation) {
		
		animations.add(newAnimation);
		
	}
	
	public void forEachAnimation(Consumer<Animation> callback) {
		
		animations.forEach(callback);
		
	}
	
	public void forEachVisualLayerPrototype(Consumer<VisualLayerPrototype> callback) {
		
		visualLayerPrototypes.forEach(callback);
		
	}

	public void forEachNonVisualLayerPrototype(Consumer<NonVisualLayerPrototype> callback) {
		
		nonVisualLayerPrototypes.forEach(callback);
		
	}
	
	public void forEachArtboard(Consumer<Artboard> callback) {
		
		synchronized(allArtboards) {
			
			allArtboards.forEach(callback);
			
		}
		
	}

	public void forEachCopyOf(Artboard source , Consumer<Artboard> callback) {
		
		copier.forEachCopyOf(source , callback);
		
	}
	
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}
	
	public VisualLayerPrototype visualLayerPrototype(int index) {
		
		return visualLayerPrototypes.get(index);
		
	}
	
	public int visualLayerPrototypeSize() {
		
		return visualLayerPrototypes.size();
		
	}
	
	public int getArtboardIndex(Artboard artboard) {
		
		synchronized(allArtboards) {
			
			return allArtboards.indexOf(artboard);
		
		}
		
	}
	
	public void setCurrentArtboardByCursorPosition(float cursorWorldX , float cursorWorldY) {
		
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
	
	public boolean paletted() {
		
		return paletted;
		
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
	
	/**
	 * Saves this project by writing it to the disk.
	 * 
	 * @param writeAbsPath — path of a directory into which this project will be written
	 * @throws IOException 
	 */
	public void save() throws IOException {

		Path projectAbsPath = Paths.get(dataDir + name);		
		if(!Files.exists(projectAbsPath)) Files.createDirectory(projectAbsPath);
		
		writeMeta(projectAbsPath);
		
		//save palettes
		
		visualPalette.toPNG(projectAbsPath.toString() + File.separator + "visual");
		for(int i = 0 ; i < nonVisualPalettes.size() ; i++) { 
			
			nonVisualPalettes.get(i).toPNG(projectAbsPath.toString() + File.separator + "nonvisual " + i);
			
		}
		
		synchronized(allArtboards) {
			
			for(Artboard x : allArtboards) x.writeToFile(projectAbsPath.toString());
		
		}
		
	}
	
	private void writeMeta(Path projectPath) {

		//create array of nonvisual layer prototype data
		
		byte[] nvlSizes = new byte[nonVisualLayerPrototypes.size()];
		String[] nvlNames = new String[nonVisualLayerPrototypes.size()];
		
		int i = 0;
		
		for(var x : nonVisualLayerPrototypes) { 
			
			nvlSizes[i] = (byte) x.sizeBytes() ; nvlNames[i++] = x.name();
			
		}

		//setup arrays of visual layer prototype data
		
		i = 0;
		
		String[] vlNames = new String[visualLayerPrototypes.size()] ; for(var x : visualLayerPrototypes) vlNames[i++] = x.name();
		
		//same for animation names
		
		i = 0;
		
		String[] animationNames = new String[animations.size()] ; for(var x : animations) animationNames[i++] = x.name();
				
		//bind data to file composition

		ProjectMeta file = new ProjectMeta()		
			.bindName(name)
			.bindChannelsPerPixel((byte) channelsPerPixel)
			.bindPaletted(paletted)
			.bindNonVisualLayerSizes(nvlSizes)
			.bindNonVisualLayerNames(nvlNames)
			.bindVisualLayerNames(vlNames)
			.bindAnimations(animationNames)			
		;

		//write file
		
		try(FileOutputStream writer = new FileOutputStream(new File(projectPath.toString() + File.separator + name + ".csssmeta"))) {
			
			file.write(writer);
		
		} catch (IOException e) {
			
			e.printStackTrace();
			throw new IllegalStateException();
			
		}
		
	}

	public ArtboardPalette palette() {
		
		return visualPalette;
		
	}
	
	public synchronized Artboard getArtboard(final int index) {
		
		return allArtboards.get(index);
		
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
	
}
