/**
 * 
 */
package cs.csss.project.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import cs.csss.engine.LookupPixel;
import cs.csss.project.Artboard;
import cs.csss.project.utils.Artboards;
import cs.csss.project.utils.RegionIterator;
import cs.csss.project.utils.RegionPosition;
import sc.core.SCShutDown;
import sc.core.binary.SCGraphic;
import sc.core.binary.SCPNG;

/**
 * Class responsible for importing PNGs, JPEGs, and BMPs and converting them into Artboards.
 */
public class ImageImporter implements SCShutDown {

	static final List<String> imports = new ArrayList<>();

	/**
	 * Clears any registered file paths for importing.
	 */
	public static void clearRegisteredImportFilePaths() {
		
		imports.clear();
		
	}
	
	/**
	 * Sets a list of strings to be file paths of images to export.
	 * 
	 * @param filepaths array of file paths to images to import
	 */
	public static void registerNewImportFilePaths(String... filepaths) {
		
		for(String x : filepaths) imports.add(x);
		
	}
	
	/**
	 * Invokes {@code callback} for each selected file path.
	 * 
	 * @param callback code to invoke for each selected file path
	 */
	public static void forEachImportPath(Consumer<String> callback) {
		
		for(String x : imports) callback.accept(x);
		
	}
	
	private SCGraphic source;
	
	/**
	 * Loads the image at the given file path.
	 * 
	 * @param filepath file path to an image
	 * @param channels channels of the project
	 */
	public ImageImporter(String filepath , int channels) {
	
		if(filepath.endsWith(".bmp")) source = new BMP(filepath , channels);
		else if (filepath.endsWith(".jpg")) source = new JPG(filepath , channels);
		else if (filepath.endsWith(".png")) source = new SCPNG(filepath , channels);
	
		Objects.requireNonNull(source.data());
		
	}
	
	/**
	 * Copies the contents of the image which was loaded by this class to the given artboard.
	 * 
	 * @param artboard an artboard to copy the image to.
	 */
	public void copyToArtboard(Artboard artboard) {
		
		assert artboard.width() >= source.width() && artboard.height() >= source.height() : "Artboard cannot be smaller than image.";
		
		ByteBuffer image = source.data();
		LookupPixel[][] imageColors = new LookupPixel[artboard.height()][artboard.width()];
				
		RegionPosition position;					
		for(RegionIterator iterator = Artboards.region(0, 0, artboard.width(), artboard.height()) ; image.hasRemaining() ; ) {
			
			position = iterator.next();
			imageColors[position.row()][position.col()] = artboard.putInPalette(artboard.createPalettePixel(image));
						
		}
		
		artboard.putColorsInImage(0, 0, artboard.width(), artboard.height(), imageColors);
		//must reset position for shut down.
		image.position(0);
		
	}

	/**
	 * Returns the width of the image loaded.
	 * 
	 * @return Width of the image loaded.
	 * 
	 * @see cs.core.utils.files.CSGraphic#width()
	 */
	public int width() {
		
		return source.width();
		
	}

	/**
	 * Returns the height of the image loaded.
	 * 
	 * @return Height of the image loaded.
	 * 
	 * @see cs.core.utils.files.CSGraphic#height()
	 */
	public int height() {
		
		return source.height();
		
	}

	/**
	 * @see cs.core.utils.ShutDown#shutDown()
	 */
	public void shutDown() {

		if(isFreed()) return;
		source.shutDown();
		
	}

	/**
	 * @return {@code true} if the image being imported with this {@code ImageImporter} is freed. 
	 * 
	 * @see cs.core.utils.ShutDown#isFreed()
	 */
	public boolean isFreed() {
		
		return source.isFreed();
		
	}
	
}
