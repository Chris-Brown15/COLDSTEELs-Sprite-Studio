package cs.csss.core;

import static cs.core.utils.CSUtils.specify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;

import cs.core.graphics.CSOrthographicCamera;
import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardMeta;
import cs.csss.artboard.ArtboardPalette;
import cs.csss.artboard.Layer;
import cs.csss.artboard.LayerMeta;
import cs.csss.artboard.LayerPixel;
import cs.csss.artboard.NonVisualLayer;
import cs.csss.artboard.NonVisualLayerPrototype;
import cs.csss.artboard.VisualLayer;
import cs.csss.artboard.VisualLayerPrototype;

/**
 * Contains information about the currently in-use project. 
 * 
 * @author Chris Brown
 *
 */
public class CSSSProject {  

	
	public static final String dataDir = new File("data/").getAbsolutePath() + "/";
	/**
	 * Grayscale shade selectors.
	 */
	public static final int 
		GRAYSCALE_GRAY 		= 0xffffffff ,
		GRAYSCALE_RED 		= 0xff0000ff ,
		GRAYSCALE_GREEN 	= 0x00ff00ff ,
		GRAYSCALE_BLUE 		= 0x0000ffff ,
		GRAYSCALE_YELLOW 	= 0xffff00ff ,
		GRAYSCALE_CYAN 		= 0x00ffffff ,
		GRAYSCALE_MAGENTA 	= 0xff00ffff 
	;
	
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

	private int channelsPerPixel = -1;
	private int grayscaleShade = GRAYSCALE_GRAY;
	
	private boolean paletted = false;
	
	private ArtboardPalette visualPalette;
	private ArrayList<ArtboardPalette> nonVisualPalettes = new ArrayList<>(NonVisualLayerPrototype.MAX_SIZE_BYTES);

	private final LinkedList<Artboard> artboards = new LinkedList<>();	
	private final LinkedList<Animation> animations = new LinkedList<>();
	private final LinkedList<VisualLayerPrototype> visualLayerPrototypes = new LinkedList<>();
	private final LinkedList<NonVisualLayerPrototype> nonVisualLayerPrototypes = new LinkedList<>();
	
	private Artboard currentArtboard;
	private Animation currentAnimation;
	
	public CSSSProject(final String name , int channelsPerPixel , final boolean paletted , boolean makeDefaultLayer) {
		
		specify(channelsPerPixel > 0 && channelsPerPixel <= 4 , channelsPerPixel + " is not a valid number of channels per pixel.");
		
		specify(isValidProjectName(name) , name + " is not a valid project name.");
		
		setName(name);
		this.channelsPerPixel = channelsPerPixel ; this.paletted = paletted;
		
		byte tff = (byte) 255;
		visualPalette = new ArtboardPalette(channelsPerPixel , tff , tff , tff , tff);

		for(int i = 1 ; i <= NonVisualLayerPrototype.MAX_SIZE_BYTES ; i++) { 
			
			nonVisualPalettes.add(new ArtboardPalette(i , tff , tff , tff , tff));
		
		}
		
	 	if(makeDefaultLayer) addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));
		
	}

	public CSSSProject(final String name) {
	
		specify(isValidProjectName(name) , name + " is not a valid project name.");
		setName(name);
		addVisualLayerPrototype(new VisualLayerPrototype("Default Layer"));		
				
	}
	
	CSSSProject(ProjectMeta meta) {

		this(meta.name() , meta.channelsPerPixel() , meta.paletted() , false);
		
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
		
		for(int i = 0 ; i < animations.length ; i++) addAnimation(new Animation(animations[i] , this));
	
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
		
		artboards.forEach(artboard -> {
			
		 	VisualLayer removeThis = artboard.getVisualLayer(layer);
			if(!removeThis.hiding()) removeThis.hide(artboard);
			
			artboard.removeLayer(removeThis);
		 	
		});
		
		//makes the remove take place at a safe point
		Engine.THE_TEMPORAL.onTrue(() -> true, () -> visualLayerPrototypes.remove(layer));
		
	}
	
	public void renderAllArtboards(CSOrthographicCamera camera) {
		
		forEachArtboard(artboard -> artboard.draw(camera));
		
	}
	
	public void addArtboard(Artboard newArtboard) {

		if(artboards.size() != 0) {
			
			Artboard last = artboards.getLast();
			float lastRightX = last.rightX();
			int translationX = (int) Math.ceil(lastRightX) + newArtboard.width();
			
			newArtboard.translate(translationX, 0);
			
		}
		
		artboards.add(newArtboard);
		
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
		
		artboards.forEach(callback);
		
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
		
		return artboards.indexOf(artboard);
		
	}
	
	public void grayscaleShade(int grayscaleShade) {
		
		specify(
			grayscaleShade == GRAYSCALE_GRAY ||
			grayscaleShade == GRAYSCALE_RED ||
			grayscaleShade == GRAYSCALE_GREEN ||
			grayscaleShade == GRAYSCALE_BLUE ||
			grayscaleShade == GRAYSCALE_YELLOW ||
			grayscaleShade == GRAYSCALE_CYAN ||
			grayscaleShade == GRAYSCALE_MAGENTA ,
			grayscaleShade + " is not a valid grayscale shade.");
		
		this.grayscaleShade = grayscaleShade;
		
	}
	
	public int grayscaleShade() {
		
		return grayscaleShade;
		
	}
	
	public void setCurrentArtboardByCursorPosition(float cursorWorldX , float cursorWorldY) {
		
		for(Artboard x : artboards) if(x.isCursorInBounds(cursorWorldX , cursorWorldY)) { 
			
			currentArtboard = x;
			return;
			
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
	
	/**
	 * Saves this project by writing it to the disk.
	 * 
	 * @param writeAbsPath — path of a directory into which this project will be written
	 * @throws IOException 
	 */
	void save() throws IOException {

		Path projectAbsPath = Paths.get(dataDir + name);		
		if(!Files.exists(projectAbsPath)) Files.createDirectory(projectAbsPath);
		
		writeMeta(projectAbsPath);
		
		//save palettes
		
		visualPalette.toPNG(projectAbsPath.toString() + File.separator + "visual");
		for(int i = 0 ; i < nonVisualPalettes.size() ; i++) { 
			
			nonVisualPalettes.get(i).toPNG(projectAbsPath.toString() + File.separator + "nonvisual " + i);
			
		}
		
		animations.forEach(animation -> {
			
			try {
				
				animation.save(projectAbsPath.toString());
				
			} catch (IOException e) {

				e.printStackTrace();					
				throw new IllegalStateException();
				
			}
			
		});
		
		int ID = 0;
		for(Artboard x : artboards) x.writeToFile(ID++ , projectAbsPath.toString());
		
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

	public void loadArtboards(final String projectPath) {
		
		File[] artboards = new File(projectPath).listFiles();
		
		//remove entries that aren't artboard folders.
		for(int i = 0 ; i < artboards.length ; i++) if(artboards[i].getName().endsWith(".csssmeta"))  {

			artboards[i] = null;
			break;			
			
		}
		
		for(File artboardDirectory : artboards) if(artboardDirectory != null) {

			File[] artboardDirectoryFiles = artboardDirectory.listFiles();

			File metaFile = new File(artboardDirectory.getAbsolutePath() + File.separator + "___meta");
			
			for(int i = 0 ; i < artboardDirectoryFiles.length ; i++) if(artboardDirectoryFiles[i].getName().equals("___meta")) {
				
				artboardDirectoryFiles[i] = null;
				break;
				
			}
			
			try(FileInputStream reader = new FileInputStream(metaFile)) {
				
				ArtboardMeta artboardMeta = new ArtboardMeta();
				artboardMeta.read(reader);
				
				Artboard artboard = new Artboard(artboardMeta.width() , artboardMeta.height());
				addArtboard(artboard);

				for(File y : artboardDirectoryFiles) if(y != null) {
					
					boolean isVisual = visualLayerPrototypes.stream().anyMatch(prototype -> prototype.name().equals(y.getName()));
					LayerMeta layerMeta = new LayerMeta();
					
					try(FileInputStream layerMetaReader = new FileInputStream(y)) {
						
						layerMeta.read(layerMetaReader);
													
						Layer instance;
						
						if(isVisual) {
						
							VisualLayerPrototype prototype = new VisualLayerPrototype(layerMeta.name());
							instance = new VisualLayer(artboard , visualPalette , prototype);
							
						} else {
						
							int size = 1;
							for(NonVisualLayerPrototype x : nonVisualLayerPrototypes) if(x.name().equals(layerMeta.name())) { 
								
								size = x.sizeBytes();
								break;
								
							}
							
							NonVisualLayerPrototype prototype = new NonVisualLayerPrototype(size , layerMeta.name());
							instance = new NonVisualLayer(artboard , getNonVisualPaletteBySize(size) , prototype);
														
						}	
						
						ByteBuffer asBuffer = ByteBuffer.wrap(layerMetaReader.readAllBytes());
						instance.decode(asBuffer);
						
						//parses the buffer into the layer
						while(asBuffer.hasRemaining()) {
							
							LayerPixel pixel = new LayerPixel(asBuffer.getInt() , asBuffer.getInt() , asBuffer.get() , asBuffer.get());
							instance.put(pixel);
							
						}
						
					}  catch (IOException e) {
						
						e.printStackTrace();
						
					}
					
				}
			
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		}
		
	}
	
	public ArtboardPalette palette() {
		
		return visualPalette;
		
	}
	
	public synchronized Artboard getArtboard(final int index) {
		
		return artboards.get(index);
		
	}
	
	public int numberArtboards() {
		
		return artboards.size();
		
	}
	
}
