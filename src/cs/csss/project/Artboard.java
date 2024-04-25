package cs.csss.project;

import static cs.core.graphics.StandardRendererConstants.POSITION_2D;
import static cs.core.graphics.StandardRendererConstants.STATIC_VAO;
import static cs.core.graphics.StandardRendererConstants.UINT;
import static cs.core.graphics.StandardRendererConstants.UV;

import static cs.core.utils.CSUtils.specify;
import static cs.core.utils.CSUtils.require;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import org.joml.Random;
import org.lwjgl.system.MemoryStack;

import cs.core.graphics.utils.VertexBufferBuilder;
import cs.core.utils.ShutDown;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.line.BezierLine;
import cs.csss.editor.line.Line;
import cs.csss.editor.line.LinearLine;
import cs.csss.editor.shape.Ellipse;
import cs.csss.editor.shape.Rectangle;
import cs.csss.editor.shape.Shape;
import cs.csss.engine.CSSSCamera;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.Logging;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.Pixel;
import cs.csss.engine.VAOPosition;
import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.utils.Artboards;
import cs.csss.project.utils.RegionIterator;
import cs.csss.project.utils.RegionPosition;
import cs.csss.project.utils.StackOrHeapAllocation;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Artboard contains the data needed to display and edit a graphic in CSSS and have methods to operate on pixels and layers.
 * <p>
 * 	One or many artboards may be created for a CSSS project. The {@link CSSSProject} is responsible for creating and managing artboards. One 
 * 	artboard is active at a time and this is the one that is modified by brushes and scripts.
 * </p>
 * <p>
 * 	Artboards are GPU textures. The textures are 2-channel, 1 byte per channel textures. Thus, the total number of bytes needed for an artboard is
 * 	{@code 2 * width * height}. Each pixel is referred to as a {@link LookupPixel}. Lookup pixels store one byte x and y coordinates. These coordinates 
 * 	are used to index into a palette of colors. Whichever color is located at the given coordinate is what is rendered in the place of the lookup pixel.
 * </p>
 * 
 * @author Chris Brown
 *
 */
public class Artboard implements ShutDown {
	
	/**
	 * Deep copies the visual layers of {@code source} into {@code destination}. They will be independent of one another for the purposes
	 * of layer modification.
	 * 
	 * @param project — the project 
	 * @param source — the source artboard 
	 * @param destination — the destination artboard
	 */
	@RenderThreadOnly private static void deepCopyVisualLayers(CSSSProject project , Artboard source , Artboard destination) {

		//creates all visual layers
		project.forEachVisualLayerPrototype(prototype -> {
			
			VisualLayer sourceLayer = source.getVisualLayer(prototype);
			VisualLayer destinationLayer = new VisualLayer(destination , project.visualPalette() , prototype);
			copyLayer(sourceLayer, destinationLayer, source, destination);
			
		});
	
		//arranges them
		for(int i = 0 ; i < source.numberVisualLayers() ; i++) {
			
			VisualLayer sourceLayerForIndex = source.getVisualLayer(i);
			VisualLayer destinationLayerForIndex = destination.getVisualLayer(i);
			if(!sourceLayerForIndex.name.equals(destinationLayerForIndex.name)) {
				
				destination.moveVisualLayerRank(destination.getLayerRank(destination.getVisualLayer(sourceLayerForIndex.name)), i);
				
			}
			
		}
		
	}
	
	/**
	 * Deep copies the nonvisual layers of {@code source} into {@code destination}. They will be independent of one another for the 
	 * purposes of layer modification.
	 * 
	 * @param project — the project 
	 * @param source — the source artboard 
	 * @param destination — the destination artboard
	 */
	private static void deepCopyNonVisualLayers(CSSSProject project , Artboard source , Artboard destination) {

		project.forEachNonVisualLayerPrototype(proto -> {
			
			NonVisualLayer instance = new NonVisualLayer(destination , project.getNonVisualPaletteBySize(proto.sizeBytes()) , proto);
			NonVisualLayer sourceLayer = source.getNonVisualLayer(proto);

			copyLayer(sourceLayer, instance, source, destination);
			
		});
		
	}

	@RenderThreadOnly private static void copyLayer(Layer source , Layer destination , Artboard sourceArtboard , Artboard destinationArtboard) {
	
		if(destination instanceof VisualLayer asVisual) destinationArtboard.addVisualLayer(asVisual);
		else destinationArtboard.addNonVisualLayer((NonVisualLayer)destination);
		
		destination.hiding(source.hiding);
		source.copy(destination);
		source.shapesIterator().forEachRemaining(shape -> {
			
			//find the offset from the middle of the artboard that the source's shape is, and move the copy the same amount.
			float offsetX = shape.xOffsetFrom(sourceArtboard);
			float offsetY = shape.yOffsetFrom(sourceArtboard);
			
			Shape copy = shape.copy();			
			copy.moveTo(0 , 0);
			copy.translate((int)offsetX, (int)offsetY);
			destination.addShape(copy);
						
		});
		
		source.linesIterator().forEachRemaining(line -> {
			
			Line copy = line.copy();
			destination.addLine(copy);
						
		});
		
		if(source == sourceArtboard.activeLayer()) destinationArtboard.setActiveLayer(destination);
				
	}
	
	/**
	 * Creates a deep copy of the source artboard, using the source artboard and the project.
	 * 
	 * @param source — source artboard
	 * @param project — owning project
	 * @return Copied project.
	 */
	@RenderThreadOnly static Artboard deepCopy(String newArtboardName , Artboard source , CSSSProject project) {
		
		Artboard newArtboard = new Artboard(newArtboardName , source.width() , source.height());
		
		//remove mods from lines from the source artboard so they won't carry over when it is copied.		
		source.undoAllLines();
								
		deepCopyVisualLayers(project , source , newArtboard);	
		deepCopyNonVisualLayers(project , source , newArtboard);
		
		//copies the image of source onto the new artboard
		if(newArtboard.isActiveLayerVisual) newArtboard.showAllNonHiddenVisualLayers();
		else newArtboard.activeLayer.show(newArtboard);
		
		//reset lines again
		source.showAllLines();
//		newArtboard.showAllLines();
		
		return newArtboard;
		
	}
	
	/**
	 * Shallow copies {@code source} artboard, returning a new artboard which is a shallow copy. 
	 * 
	 * <p>
	 * In the context of artboards, a shallow copy is an artboard who has the same textures and layers as a source, but unique VAO. Any 
	 * changes which occur to the source occur to the original and vise versa. Shallow copies are created from the 
	 * {@link cs.csss.project.ArtboardCopier ArtboardCopier}, a package level class and member of the 
	 * {@link cs.csss.project.CSSSProject CSSSProject}. 
	 * </p>
	 * 
	 * @param newArtboardName — name of the copy
	 * @param source — a source artboard for copy
	 * @return Shallow copy artboard of {@code source}.
	 */
	@RenderThreadOnly static Artboard shallowCopy(String newArtboardName , Artboard source) {

		Artboard newArtboard = new Artboard(newArtboardName , source.indexTexture , source.width() , source.height() , false , true);

		newArtboard.visualLayers = source.visualLayers;
		newArtboard.nonVisualLayers = source.nonVisualLayers;
		newArtboard.setActiveLayer(source.activeLayer);
		
		return newArtboard;
		
	}

	/**
	 * Returns a lookup pixel pointing to the background color that would be placed at {@code (xIndex , yIndex)}. 
	 * 
	 * @param xIndex x index of a pixel 
	 * @param yIndex y index of a pixel
	 * @return {@link LookupPixel} representing the background color that would be found at that position.
	 */
	public static LookupPixel backgroundColorIndexForPixelIndex(int xIndex , int yIndex) {

		int xRegion = xIndex / IndexTexture.backgroundWidth;
		int yRegion = yIndex / IndexTexture.backgroundHeight;
		
		//odd exponent, use the darker background otherwise use the lighter one
		boolean darker = (xRegion & 1) == 1;
		//flip darker if the y region is even
		if((yRegion & 1) == 0) darker = !darker;
			
		return darker ? new IndexPixel(0 , 0) : new IndexPixel(1 , 0);
		
	}
	
	private CSSSVAO vao = new CSSSVAO();
	
	private IndexTexture indexTexture;	
	
	/**
	 * Position controller for this artboard.
	 */
	public final VAOPosition positions;
	
	//used to track whether the abstract Layer instance activeLayer below is a nonvisual or visual layer.
	private boolean isActiveLayerVisual = true;

	private ArrayList<VisualLayer> visualLayers = new ArrayList<>();
	
	private ArrayList<NonVisualLayer> nonVisualLayers = new ArrayList<>();
	
	private Layer activeLayer;
	
	/**
	 * Name of this artboard.
	 */
	public final String name;
	
	private final boolean isShallowCopy;
	
	/**
	 * Initializes a new artboard.
	 * 
	 * @param name — name of this artboard
	 * @param width — width of this artboard
	 * @param height — height of this artboard
	 */
	@RenderThreadOnly Artboard(String name , int width , int height) {
 		
		this(name , null , width , height , true , false);

	}

	/**
	 * Initializes a new artboard.
	 * 
	 * @param name — name of this artboard
	 * @param width — width of this artboard
	 * @param height — height of this artboard
	 * @param setCheckeredBackground — whether to set the texture to a checkered background 
	 */
	@RenderThreadOnly Artboard(String name , int width , int height , boolean setCheckeredBackground) {
 		
		this(name , null , width , height , setCheckeredBackground , false);

	}
	
	/**
	 * Initializes a new artboard.
	 * 
	 * @param name — name of this artboard
	 * @param texture — an index texture this artboard will use, if <code>null</code>, a new one is created
	 * @param width — width of this artboard
	 * @param height — height of this artboard
	 * @param setCheckeredBackground — if {@code true}, the index texture will be set to a checkered background
	 */
  	@RenderThreadOnly Artboard(String name , IndexTexture texture , int width , int height , boolean setCheckeredBackground , boolean isShallowCopy) {
		
		this.name = name;
		this.isShallowCopy = isShallowCopy;
		
		VertexBufferBuilder vertexBuffer = new VertexBufferBuilder(POSITION_2D|UV);
		vertexBuffer.size(width , height); 
		
		vao.initialize(vertexBuffer.attributes, STATIC_VAO, vertexBuffer.get());
		vao.drawAsElements(6, UINT);
		
		positions = new VAOPosition(vao , vertexBuffer.attribute(POSITION_2D));

		if(texture == null) {
			
			indexTexture = new IndexTexture();
			
		} else {
			
			indexTexture = texture;
			indexTexture.incrementOwners();

		}
		
		if(!indexTexture.isInitialized()) indexTexture.initialize(width , height , setCheckeredBackground);
		
	}
	
	/**
	 * Renders this artboard on the GPU.
	 * @param camera {@link cs.csss.annotation.Nullable @Nullable} camera which will be used to render the shapes of this artboard, or 
	 * 				 <code>null</code> to not render shapes
	 */
	@RenderThreadOnly public void draw(CSSSCamera camera) {
		
		vao.activate();
		vao.draw(); 
		vao.deactivate();
		
		if(camera != null) {
		
			//render shapes			
			if(isActiveLayerVisual) visualLayers.stream().filter(layer -> !layer.hiding()).forEach(layer -> layer.shapes.renderShapes(camera));
			else if(!activeLayer.hiding()) activeLayer.shapes.renderShapes(camera);
		
		}
		
	}
	
	/**
	 * Moves this artboard by {@code x} along the x axis and {@code y} along the y axis.
	 * 
	 * @param x — amount to move this artboard horizontally
	 * @param y — amount to move this artboard vertically
	 */
	@RenderThreadOnly public void translate(int x , int y) {
		
		positions.translate(x, y);		
		if(!isShallowCopy) forAllLayers(layer -> layer.shapesStream().forEach(shape -> shape.translate(x, y)));
				
	} 
	
	/**
	 * Moves this artboard such that its midpoint will be {@code (x , y)} after transformation.
	 * 
	 * @param x — x position to move to
	 * @param y — y position to move to
	 */
	@RenderThreadOnly public void moveTo(int x , int y) {
		
		translate((int)-midX() + x, (int)-midY() + y);
		
	}			

	/**
	 * Returns {@code true} if the coordinates within {@code cursorWorldCoords} are within the bounds of this artboard, {@code false} 
	 * otherwise.
	 * 
	 * @param curorWorldCoords — array containing cursor world coordinates where {@code cursorWorldCoords[0]} is x and 
	 * 							 {@code cursorWorldCoords[1]} is y
	 * @return {@code true} if the coordinates within {@code cursorWorldCoords} are within the bounds of this artboard, {@code false} 
	 * 		   otherwise.
	 */
	public boolean isCursorInBounds(float...cursorWorldCoords) {
		
		return (
			cursorWorldCoords[0] <= rightX() && cursorWorldCoords[0] >= leftX() &&
			cursorWorldCoords[1] <= topY() && cursorWorldCoords[1] >= bottomY()
		);
		
	}
	
	/**
	 * Returns the x and y coordinates in the image texture of the pixel located at {@code cursorWorldCoords}.
	 * 
	 * @param worldCoords — cursor world coords
	 * @return Array whose contents are indices into the image texture of the pixel the cursor is hovering.
	 */
	public int[] worldToPixelIndices(float[] worldCoords) {
		
		return new int[] {
			(int) (worldCoords[0] - leftX()) ,
			(int) (worldCoords[1] - bottomY()) ,
		};
		
	}
	
	/**
	 * Converts the given values as world coordinates to indices of this artboard.
	 * 
	 * @param xWorldCoord — an x coordinate in world space
	 * @param yWorldCoord — a y coordinate in world space
	 * @return Array containing converted x and y coordinates.
	 */
	public int[] worldToPixelIndices(float xWorldCoord , float yWorldCoord) {
		
		int[] destination = new int[2];
		worldToPixelIndices(xWorldCoord , yWorldCoord , destination);
		return destination;
		
	}

	/**
	 * Converst the given world coordinates to indices of this artboard, storing the results in {@code destination}.
	 * 
	 * @param worldX — x world coordinate 
	 * @param worldY — y world coordinate
	 * @param destination — destination for values
	 */
	public int[] worldToPixelIndices(float worldX , float worldY , int[] destination) {
		
		destination[0] = (int) (worldX - leftX());
		destination[1] = (int) (worldY - bottomY());
		return destination;
		
	}
	
	/**
	 * Converts the given X artboard coordinate to world space, returning the result. The resulting value is the world space coordinate of the pixel
	 * column indexed by {@code artboardX}.
	 * 
	 * @param artboardX X artboard coordinate
	 * @return World space coordinate of {@code artboardX}.
	 */
	public float artboardXToWorldX(int artboardX) {
		
		return leftX() + artboardX;
		
	}

	/**
	 * Converts the given Y artboard coordinate to world space, returning the result. The resulting value is the world space coordinate of the pixel
	 * row indexed by {@code artboardY}.
	 * 
	 * @param artboardX Y artboard coordinate
	 * @return World space coordinate of {@code artboardY}.
	 */
	public float artboardYToWorldY(int artboardY) {
		
		return bottomY() + artboardY;
		
	}
	
	/**
	 * Converts the given values as world coordinates to indices of this artboard.
	 * 
	 * @param xWorldCoord — an x coordinate in world space
	 * @param yWorldCoord — a y coordinate in world space
	 * @return Array containing converted x and y coordinates.
	 */
	public int[] worldToPixelIndices(int xWorldCoord , int yWorldCoord) {
		
		int[] destination = new int[2];
		worldToPixelIndices(xWorldCoord , yWorldCoord , destination);
		return destination;
			
	}
	
	/**
	 * Places the color given by the channel values in {@code pixel} into the next position in the palette texture and returns the indices
	 * to which it is stored, or, if {@code pixels} is already present in the palette, returns its indices.
	 * 
	 * @param values — pixel values for a color to put in the palette.
	 * @return Array of indices into the palette where {@code values} is located.
	 */
	@RenderThreadOnly public LookupPixel putInPalette(ColorPixel pixel) {

		return activeLayer().palette.putOrGetColors(pixel);
		
	}
	
	/**
	 * Puts a single color at a region of the image. This method does logic for layers as well, only writing to the artboard when logic to
	 * do so permits.  
	 * <p>
	 * 	A pixel is only updated if 
	 * 	<ol>
	 * 		<li> the current layer is the layer of the highest priority, or </li>
	 * 		<li> no layers of a higher priority than the current one also modify the given pixel. </li>
	 * 	</ol>
	 * </p>
	 * 
	 * @param x — x coordinate of the bottom left corner of the region
	 * @param y — y coordinate of the bottom left corner of the region
	 * @param width — the width of the region
	 * @param height — the height of the region
	 * @param value — pixel instance
	 */
	@RenderThreadOnly public void putColorInImage2(int x , int y , int width , int height , Pixel value) {
		
		if(value instanceof LookupPixel asLookup) putColorInImage(x , y , width , height , asLookup);
		else if(value instanceof ColorPixel asColor) putColorInImage(x , y , width , height , asColor);
		
	}
	
	/**
	 * Puts a single color at a region of the artboard. This method does logic for layers as well.
	 * 
	 * @param corrected corrected indices for this artboard
	 * @param value pixel value to put
	 * @throws NullPointerException if either parameter is < code>null</code>.
	 */
	@RenderThreadOnly public void putColorInImage2(CorrectedResult corrected , Pixel value) {
		
		Objects.requireNonNull(value);
		Objects.requireNonNull(corrected);
		putColorInImage2(corrected.leftX() , corrected.bottomY() , corrected.width() , corrected.height() , value);
		
	}
	
	/**
	 * Puts a single color at a region of the image. This method does logic for layers as well, only writing to the artboard when logic to
	 * do so permits. 
	 * <br><br>
	 * A pixel is only updated if 
	 * <ol>
	 * 	<li> 
	 * 		the current layer is the layer of the highest priority, or 
	 * 	</li>
	 * 	<li> 
	 * 		no layers of a higher priority than the current one also modify the given pixel. 
	 * 	</li>
	 * </ol>
	 * 
	 * @param xIndex — x index of the bottom left corner of the region to put the color
	 * @param yIndex — y index of the bottom left corner of the region to put the color
	 * @param width — number of pixels to extend rightward from {@code xIndex} to put the color in
	 * @param height — number of pixels to extend upward from {@code yIndex} to put the color in
	 * @param values — channel values to put in each pixel of the region.
	 */
	@RenderThreadOnly public void putColorInImage(int xIndex , int yIndex , int width , int height , ColorPixel values) {

		if(activeLayer().hiding() || activeLayer().locked()) return;		
		
		putColorInImage(xIndex, yIndex, width, height, putInPalette(values));

	}

	/**
	 * Override of {@link #putColorInImage(int, int, int, int, LookupPixel)} using the values stored in {@code color} as parameters.
	 * 
	 * @param color color to store
	 * @throws NullPointerException if {@code color} is <code>null</code>.
	 */
	@RenderThreadOnly public void putColorInImage(LayerPixel color) {
		
		Objects.requireNonNull(color);
		
		if(activeLayer().hiding() || activeLayer().locked()) return;
		
		putColorInImage(color.textureX() , color.textureY() , 1 , 1 , color);
		
	}
	
	/**
	 * Puts a single color at a region of the image. This method does logic for layers as well, only writing to the artboard when logic to
	 * do so permits. 
	 * <br><br>
	 * A pixel is only updated if 
	 * <ol>
	 * 	<li> the current layer is the layer of the highest priority, or </li>
	 * 	<li> no layers of a higher priority than the current one also modify the given pixel. </li>
	 * </ol>
	 * 
	 * @param xIndex — x index of the bottom left corner of the region to put the color
	 * @param yIndex — y index of the bottom left corner of the region to put the color
	 * @param width — number of pixels to extend rightward from {@code xIndex} to put the color in
	 * @param height — number of pixels to extend upward from {@code yIndex} to put the color in
	 * @param values — index pixel whose values provide a lookup into the palette
	 */
	@RenderThreadOnly public void putColorInImage(int xIndex , int yIndex , int width , int height , LookupPixel values) {

		if(activeLayer().hiding() || activeLayer().locked()) return;
		
		bulkPutInActiveLayer(values , xIndex , yIndex , width , height);

		if(isActiveLayerVisual) {
			
			int activeLayerIndex = visualLayers.indexOf(activeLayer());
			
			//canBulkWrite will be true if there is no layer above the current one modifying any of the pixels of the region.
			boolean canBulkWrite = !bulkIsUpperRankLayerModifying(activeLayerIndex , xIndex , yIndex , width , height);
			
			if(canBulkWrite) indexTexture.putSubImage(xIndex , yIndex , width , height , values);
			else for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
				
				boolean isUpperLayerModifying = isUpperRankLayerModifying(activeLayerIndex , xIndex + col , yIndex + row);
				if(!isUpperLayerModifying) indexTexture.putSubImage(xIndex + col , yIndex + row , 1 , 1 , values);
				
			}
			
		} else indexTexture.putSubImage(xIndex , yIndex , width , height , values);
				
	}
	
	/**
	 * Puts a 2D array of values into this artboard.
	 * 
	 * @param leftX — left x coordinate 
	 * @param bottomY — bottom y coordinate
	 * @param width — width of the region
	 * @param height — height of the region
	 * @param values — 2D array of pixel values
	 */
	@RenderThreadOnly public void putColorsInImage(int leftX , int bottomY , int width , int height , LookupPixel[][] values) {
		
		specify(leftX >= 0 && leftX < width() , leftX + " is an invalid x index");
		specify(bottomY >= 0 && bottomY < height() , bottomY + " is an invalid y index");

		if(activeLayer().hiding() || activeLayer().locked()) return;
		
		storeLookupPixelsInLayer(values , leftX , bottomY , width , height);
		
		if(isActiveLayerVisual) {
		
			LookupPixel[][] copy = new LookupPixel[height][width];
			
			RegionIterator region = Artboards.region(leftX, bottomY, width, height);
			RegionIterator valuesIter = Artboards.region(0 , 0 , width, height);
			while(region.hasNext()) {
				
				RegionPosition next = region.next();
				RegionPosition nextValues = valuesIter.next();
				LookupPixel highest = getHighestRankingLookupForLayerModification(next.col(), next.row());
				LookupPixel current = values[nextValues.row()][nextValues.col()];
				//fill out the copy array with highest ranking modifications for each position.
				copy[valuesIter.row()][valuesIter.col()] = highest != null && highest.compareTo(current) != 0 ? highest : current;
								
			}
			
			indexTexture.putSubImage(leftX, bottomY, width, height, copy);
			
		} else indexTexture.putSubImage(leftX, bottomY, width, height, values);

	}
	
	/**
	 * Puts a 2D array of values in this artboard. {@code values} is trimmed down according to {@code result.offsets().advanceX()} and
	 * {@code result.offsets().advanceY()} if needed.
	 * 
	 * @param result {@link CorrectedResult} containing corrected index and size values
	 * @param values array of pixels to put in this artboard
	 */
	@RenderThreadOnly public void putColorsInImage(CorrectedResult result , LookupPixel[][] values) {
		
		putColorsInImage(result.leftX() , result.bottomY() , result.width() , result.height() , subRegionSubRegion(result , values));
		
	}
	
	/**
	 * Returns a sub region of {@code values} offset from {@code values} based on the values of {@code result.offsets()}.
	 * 
	 * @param result {@code CorrectedResult}
	 * @param values 2D array of pixels
	 * @return Subregion of {@code values}.
	 */
	private LookupPixel[][] subRegionSubRegion(CorrectedResult result , LookupPixel[][] values) {

		int advanceX = result.advanceX();
		int advanceY = result.advanceY();
		
		if(advanceX == 0 && advanceY == 0) return values;
				
		int width = result.width();
		int height = result.height();
		LookupPixel[][] correctedRegion = new LookupPixel[height][width];
		
		RegionIterator valuesIter = Artboards.region(advanceX , advanceY , width, height);
		RegionIterator correctRegionIter = Artboards.region(0 , 0 , width, height);
		
		while(valuesIter.hasNext() && correctRegionIter.hasNext()) {
			
			RegionPosition valuesIndices = valuesIter.next();
			RegionPosition correctRegionIndices = correctRegionIter.next();
			
			LookupPixel lookupPixel = values[valuesIndices.row()][valuesIndices.col()];
			if(lookupPixel == null) continue;			
			correctedRegion[correctRegionIndices.row()][correctRegionIndices.col()] = (LookupPixel) lookupPixel.clone();
						
		}
		
		return correctedRegion;
		
	}
	
	/**
	 * Extended version of {@link #putColorInImage2(int, int, int, int, Pixel)} in which contents of the specified region prior to this call are 
	 * removed and {@code pixel} is filled in. If {@code pixel} is <code>null</code>, the specified region is removed.
	 * 
	 * @param leftX left x coordinate of the region to modify in this artboard
	 * @param bottomY bottom y coordinate of the region to modify in this artboard
	 * @param width width of the region to modify
	 * @param height height of the region to modify
	 * @param pixel a pixel to fill in the region
	 * @throws IllegalArgumentException if {@code width} or {@code height} is not positive.
	 * @throws IndexOutOfBoundsException if {@code leftX} or {@code bottomY} or {@code leftX + width} or {@code bottomY + height} is out of bounds
	 * 									 as an index for this artboard.
	 */
	public void replace(int leftX , int bottomY , int width , int height , Pixel pixel) {

		if(width <= 0) throw new IllegalArgumentException("Width is negative.");
		if(height <= 0) throw new IllegalArgumentException("Height is negative.");
		Objects.checkIndex(leftX, this.width());
		Objects.checkIndex(bottomY, this.height());
		if(leftX + width > this.width()) throw new IndexOutOfBoundsException("Region out of bounds horizontally.");
		if(bottomY + height > this.height()) throw new IndexOutOfBoundsException("Region out of bounds vertically.");
		
		if(pixel == null) {
			
			removePixels(leftX , bottomY , width , height);
			return;
			
		}

		putColorInImage2(leftX , bottomY , width , height , pixel);
		
	}
	
	/**
	 * Extended version of {@link #putColorsInImage(int, int, int, int, LookupPixel[][])} in which {@code pixels} is placed in this artboard at
	 * the specified positions. This method has the additional quality that any position in {@code pixels} that is <code>null</code> is removed from
	 * this artboard automatically. Therefore, there is no need for {@link #removePixels(int, int, int, int)} to precede this method. This method
	 * also modifies layers.
	 * 
	 * @param leftX left x coordinate of the region to modify in this artboard
	 * @param bottomY bottom y coordinate of the region to modify in this artboard
	 * @param width width of the region to modify
	 * @param height height of the region to modify
	 * @param pixels 2D array of pixels to replace this artboard's current contents with at the given positions
	 * @throws NullPointerException if {@code pixels} is <code>null</code>.
	 * @throws IllegalArgumentException if {@code width} or {@code height} is not positive.
	 * @throws IndexOutOfBoundsException if {@code leftX} or {@code bottomY} or {@code leftX + width} or {@code bottomY + height} is out of bounds
	 * 									 as an index for this artboard.
	 */
	public void replace(int leftX , int bottomY , int width , int height , LookupPixel[][] pixels) {
		
		Objects.requireNonNull(pixels);		
		checkParameters(leftX, bottomY, width, height);
		
		try(MemoryStack stack = MemoryStack.stackPush()) {

			StackOrHeapAllocation allocation = Artboards.stackOrHeapBuffer(stack, width * IndexTexture.pixelSizeBytes);
			ByteBuffer buffer = allocation.buffer();
		
			for(int row = 0 ; row < height ; row++) {
				
				int artboardY = bottomY + row;
								
				for(int col = 0 ; col < width ; col++) {
				
					int artboardX = leftX + col;
					
					//first handle layer logic
					
					LookupPixel x = pixels[row][col];
					if(x == null) { 
						
						if(activeLayer.containsModificationTo(artboardX, artboardY)) activeLayer.remove(artboardX , artboardY);
						
					} else putInActiveLayer(x, artboardX , artboardY);
					
					//then handle pixel buffer
					
					if(isActiveLayerVisual) {
						
						VisualLayer next = getHighestRankLayerModifying(artboardX, artboardY);
						if(next != null) x = next.get(artboardX , artboardY);
						else x = getBackgroundColorIndices(artboardX , artboardY);
						
					} else x = getBackgroundColorIndices(artboardX , artboardY);
							
				 	buffer.put(x.lookupX()).put(x.lookupY());					
										
				}
				
				indexTexture.putSubImage(leftX, artboardY , width, 1 , buffer.flip());

			}
		
			allocation.free();
			
		}
		
	}
	
	/**
	 * Pass through for the values of {@code correct} into {@link #replace(int, int, int, int, LookupPixel[][])}.
	 * 
	 * @param correct set of correct parameter values for a region
	 * @param pixels 2D array of pixel values.
	 */
	public void replace(CorrectedResult correct , LookupPixel[][] pixels) {
		
		Objects.requireNonNull(correct);
		Objects.requireNonNull(pixels);
		
		replace(correct.leftX() , correct.bottomY() , correct.width() , correct.height() , pixels);
		
	}
	
	/**
	 * Writes an index pixel to a region of the artboard texture. This method uses the given palette for this operation. This is meant for 
	 * internal write operations and isn't public.
	 * 
	 * @param xIndex — x index of the bottom left corner of the region to put the color
	 * @param yIndex — y index of the bottom left corner of the region to put the color
	 * @param width — number of pixels to extend rightward from {@code xIndex} to put the color in
	 * @param height — number of pixels to extend upward from {@code yIndex} to put the color in
	 * @param palette — a palette of the user's choosing
	 * @param pixel — A {@code PalettePixel} containing the pixel values the index texture's pixel will point to
	 */
	@RenderThreadOnly void writeToIndexTexture(
		int xIndex , 
		int yIndex , 
		int width , 
		int height , 
		ArtboardPalette palette , 
		ColorPixel pixel
	) {
		
		indexTexture.putSubImage(xIndex, yIndex, width, height, palette.putOrGetColors(pixel));
		
	}
	
	/**
	 * Works similarly to {@link #writeToIndexTexture(int, int, int, int, ColorPixel)} and 
	 * {@link #writeToIndexTexture(int, int, int, int, LookupPixel)}.
	 * 
	 * @param leftX leftmost x index to write to 
	 * @param bottomY bottom y index to write to
	 * @param width width of the region to write
	 * @param height height  of the region to write
	 * @param pixel singular pixel to write at the region
	 */
	@RenderThreadOnly public void writeToIndexTexture2(int leftX , int bottomY , int width , int height , Pixel pixel) {
		
		if(pixel instanceof ColorPixel asColor) writeToIndexTexture(leftX , bottomY , width , height, asColor);
		else if(pixel instanceof LookupPixel asLookup) writeToIndexTexture(leftX , bottomY , width , height, asLookup);
		
	}
	
	/**
	 * Writes an index pixel to a region of the artboard texture. The difference between this and 
	 * {@linkplain Artboard#putColorInImage(int, int, int, int, PalettePixel) putColorInImage(int, int, int , int, PalettePixel} is that 
	 * this does no logic regarding layers. It simply writes to the texture. That method does layer-related logic and only writes to the 
	 * index texture when it can.
	 * 
	 * @param xIndex — x index of the bottom left corner of the region to put the color
	 * @param yIndex — y index of the bottom left corner of the region to put the color
	 * @param width — number of pixels to extend rightward from {@code xIndex} to put the color in
	 * @param height — number of pixels to extend upward from {@code yIndex} to put the color in
	 * @param pixel — A {@code PalettePixel} containing the pixel values the index texture's pixel will point to
	 */
	@RenderThreadOnly public void writeToIndexTexture(int xIndex , int yIndex , int width , int height , ColorPixel pixel) {

		writeToIndexTexture(xIndex , yIndex , width , height , activeLayersPalette() , pixel);
		
	}

	/**
	 * Writes an index pixel to a region of the artboard texture. The difference between this and 
	 * {@linkplain Artboard#putColorInImage(int, int, int, int, PalettePixel) putColorInImage(int, int, int , int, PalettePixel} is that 
	 * this does no logic regarding layers. It simply writes to the texture. That method does layer-related logic and only writes to the 
	 * index texture when it can.
	 * 
	 * @param xIndex — x index of the bottom left corner of the region to put the color
	 * @param yIndex — y index of the bottom left corner of the region to put the color
	 * @param width — number of pixels to extend rightward from {@code xIndex} to put the color in
	 * @param height — number of pixels to extend upward from {@code yIndex} to put the color in
	 * @param pixel — A {@code LayerPixel} containing the pixel values the index texture's pixel will point to
	 */
	@RenderThreadOnly public void writeToIndexTexture(int xIndex , int yIndex , int width , int height , LookupPixel pixel) {
		
		writeToIndexTexture(xIndex, yIndex, width, height, getColorPointedToBy(pixel));
		
	}
	
	/**
	 * Writes a region of lookup pixels into the texture of this artboard, disregarding all logic for layers.
	 * 
	 * @param xIndex — left x coordinate of the region
	 * @param yIndex — bottom y coordinate of the region
	 * @param width — width of the region
	 * @param height — height of the region
	 * @param pixels — 2D array of pixels representing a region of pixels
	 */
	@RenderThreadOnly public void writeToIndexTexture(int xIndex , int yIndex , int width , int height , LookupPixel[][] pixels) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			StackOrHeapAllocation allocation = Artboards.stackOrHeapBuffer(stack, width * height * IndexTexture.pixelSizeBytes);
			ByteBuffer buffer = allocation.buffer();
			
//			storePixelRegionInBuffer(pixels, buffer, xIndex, yIndex, width, height);			
			indexTexture.putSubImage(xIndex, yIndex, width, height, pixels);
			
			if(!allocation.stackAllocated()) memFree(buffer);
						
		}
		
	}
	
	/**
	 * Writes a region of lookup pixels into the texture of this artboard, disregarding all logic for layers.
	 * 
	 * @param region — correct specifier for a region of this artboard.
	 * @param pixels — 2D array of pixel values
	 */
	@RenderThreadOnly public void writeToIndexTexture(CorrectedResult region , LookupPixel[][] pixels) {
		
		writeToIndexTexture(region.leftX(), region.bottomY(), region.width(), region.height(), pixels);
		
	}
	
	/**
	 * Writes directly to the index texture of this artboard <em>only if</em> no layer of a greater rank than the current one modifies that 
	 * position. If a write is performed, the active layer is <em>not</em> modified. If the active layer is nonvisual, a write is performed without
	 * modification to the active layer.
	 * 
	 * @param leftX left x coordinate of the region to modify
	 * @param bottomY bottom y coordinate of the region to modify 
	 * @param width width of the region to modify
	 * @param height height of the region to modify 
	 * @param value pixel to write to the index texture where applicable
	 * @throws NullPointerException if {@code value} is <code>null</code>.
	 * @throws IndexOutOfBoundsException if {@code leftX}, {@code bottomY}, {@code width}, or {@code height} are out of bounds for this artboard.
	 */
	@RenderThreadOnly public void writeToTextureIfLayersAllow(int leftX , int bottomY , int width , int height , Pixel value) {
		
		Objects.requireNonNull(value);
		checkParameters(leftX, bottomY, width, height);		
				
		try(MemoryStack stack = MemoryStack.stackPush()) {

			StackOrHeapAllocation allocation = Artboards.stackOrHeapBuffer(stack, width * height * IndexTexture.pixelSizeBytes);
			ByteBuffer buffer = allocation.buffer();
			LookupPixel asLookup = toLookup(value);
			
			if(isActiveLayerVisual) {
				
				int activeLayerRank = getLayerRank((VisualLayer)activeLayer);
				
				RegionIterator iter = Artboards.region(leftX, bottomY, width, height);
				while(iter.hasNext()) {
					
				 	RegionPosition next = iter.next();
					
					VisualLayer highestModifier = getHighestRankLayerModifying(next.col() , next.row());
					int highestModifierRank = getLayerRank(highestModifier);
					
					//can modify
					//active layers rank is less or equal to the highest
					if(highestModifierRank == -1 || activeLayerRank <= highestModifierRank) asLookup.buffer(buffer);
					else if(highestModifierRank > activeLayerRank) highestModifier.get(next.col(), next.row()).buffer(buffer);
					else toLookup(getBackgroundColor(next.col(), next.row())).buffer(buffer);
				
				}
				
			} else asLookup.buffer(buffer);
			
			indexTexture.putSubImage(leftX, bottomY, width, height, buffer.flip());
			
			allocation.free();
			
		}
				
	}
	
	@RenderThreadOnly public void writeToTextureIfLayersAllow(int leftX , int bottomY , int width , int height , Pixel[][] values) {
		
		Objects.requireNonNull(values);
		checkParameters(leftX, bottomY, width, height);		
		
		if(isActiveLayerVisual) {
			
			int activeLayerRank = getLayerRank((VisualLayer)activeLayer);

			RegionIterator regionIterator = Artboards.region(leftX, bottomY, width, height);
			RegionIterator valuesIterator = Artboards.region(0 , 0 , width , height);
			while(regionIterator.hasNext()) {
				
				RegionPosition regionPosition = regionIterator.next() , valuesPosition = valuesIterator.next();
				Pixel currentPixel = values[valuesPosition.row()][valuesPosition.col()];
				if(currentPixel == null) continue;
				
				int highestRankingMod = getLayerRank(getHighestRankLayerModifying(regionPosition.col() , regionPosition.row()));
				if(highestRankingMod == -1 || activeLayerRank <= highestRankingMod) { 
					
					writeToIndexTexture2(regionPosition.col() , regionPosition.row() , 1 , 1 , currentPixel);
					
				}
				
			}
			
		} else { 

			RegionIterator iter = Artboards.region(leftX , bottomY , width , height);
			
			for(Pixel[] row : values) for(Pixel col : row) {
				
				if(col == null) continue;				
				RegionPosition position = iter.next();				
				writeToIndexTexture2(position.col() , position.row() , 1 , 1, col);
			
			}
			
		}
		
	}
	
	@RenderThreadOnly public void writeToTextureIfLayersAllow(CorrectedResult corrected , Pixel value) {
		
		Objects.requireNonNull(corrected);
		writeToTextureIfLayersAllow(corrected.leftX() , corrected.bottomY() , corrected.width() , corrected.height() , value);
		
	}
	
	/**
	 * Gets an index pixel directly from the index texture using the given {@code xIndex} and {@code yIndex}.
	 * 
	 * @param xIndex — x index into texture of the pixel to get
	 * @param yIndex — y index into texture of the pixel to get
	 * @return Pixel located at the given coordinates.
	 */
	public LookupPixel getIndexPixelAtIndices(int xIndex , int yIndex) {
		
		return getPixelByIndex(xIndex, yIndex);
		
	}

	/**
	 * Fills the returned 2D array with values of the image texture, that is, the indices of the region given, where {@code xIndex} and 
	 * {@code yIndex} are the coordinates of the bottom left pixel of the region, and the region extends {@code width} and {@code height}
	 * pixels from the given coordinates. 
	 * 
	 * @param xIndex — index texture x index of the bottom left corner of the region 
 	 * @param yIndex — index texture y index of the bottom left corner of the region
	 * @param width — number of pixels to extend from {@code xIndex}
	 * @param height — number of pixels to extend from {@code yIndex}
	 * @return 2D array containing all the pixels of the region specified.
	 */
	@RenderThreadOnly public IndexPixel[][] getRegionOfIndexPixels(int xIndex , int yIndex , int width , int height) {
		
		ByteBuffer texelBuffer = indexTexture.texelBufferWithReformat(width , height , xIndex , yIndex);		
		return getRegionOfIndexPixelsInternal(texelBuffer, xIndex, yIndex, width, height);
		
	}
	
	/**
	 * Gets a region of index pixels from this artboard from the region specified by 
	 * {@link cs.csss.utils.ByteBufferUtils.CorrectedParameters region}.
	 * @see {@link Artboard#getRegionOfIndexPixels(int, int, int, int) getRegionOfIndexPixels(int, int, int, int)}.
	 * 
	 * @param region — correct indices and dimensions for this artboard
	 * @return 2D array containing all the pixels of the region specified.
	 */
	@RenderThreadOnly public IndexPixel[][] getRegionOfIndexPixels(CorrectedResult region) {
	
		return getRegionOfIndexPixels(region.leftX(), region.bottomY(), region.width(), region.height());
		
	}
	
	public LookupPixel[][] getRegionOfIndexPixels2(int leftX , int bottomY , int width , int height) {
		
		checkParameters(leftX, bottomY, width, height);
		LookupPixel[][] region = new LookupPixel[height][width];
		
		RegionIterator artboardRegion = Artboards.region(leftX, bottomY, width, height);
		RegionIterator resultRegion = Artboards.region(0 , 0 , width, height);
		while(artboardRegion.hasNext()) {
			
			RegionPosition artboardPosition = artboardRegion.next();
			RegionPosition resultPosition = resultRegion.next();
			
			if(isActiveLayerVisual) {
				
				VisualLayer highestRanking = getHighestRankLayerModifying(artboardPosition.col(), artboardPosition.row());
				if(highestRanking != null) {
					
					region[resultPosition.row()][resultPosition.col()] = highestRanking.get(artboardPosition.col(), artboardPosition.row());	

				} else region[resultPosition.row()][resultPosition.col()] = getBackgroundColorIndices(artboardPosition.col(), artboardPosition.row());
				
			} else {
				
				if(activeLayer.containsModificationTo(artboardPosition.col(), artboardPosition.row())) {
					
					region[resultPosition.row()][resultPosition.col()] = activeLayer.get(artboardPosition.col(), artboardPosition.row());
					
				} else region[resultPosition.row()][resultPosition.col()] = getBackgroundColorIndices(artboardPosition.col(), artboardPosition.row());
				
			}
			
		}
		
		return region;
		
	}
	
	/**
	 * Fills the returned 2D array with values of the image texture, that is, the indices of the region given, where {@code xIndex} and 
	 * {@code yIndex} are the coordinates of the bottom left pixel of the region, and the region extends {@code width} and {@code height}
	 * pixels from the given coordinates. 
	 * 
	 * <p>
	 * 	If {@link Artboard#getRegionOfIndexPixels(int, int, int, int) getRegionOfIndexPixels} is not working correctly, try this one.
	 * </p>
	 * 
	 * @param xIndex — index texture x index of the bottom left corner of the region 
 	 * @param yIndex — index texture y index of the bottom left corner of the region
	 * @param width — number of pixels to extend from {@code xIndex}
	 * @param height — number of pixels to extend from {@code yIndex}
	 * @return 2D array containing all the pixels of the region specified.
	 */
	@RenderThreadOnly public IndexPixel[][] getRegionOfIndexPixelsAlternate(int xIndex , int yIndex , int width , int height){
		
		ByteBuffer texelBuffer = indexTexture.texelBuffer(xIndex, yIndex, width, height);
		if(texelBuffer == null) return null;
		return getRegionOfIndexPixelsInternal(texelBuffer, xIndex, yIndex, width, height);
		
	}
	
	/**
	 * Gets a region of index pixels from this artboard from the region specified by 
	 * {@link cs.csss.utils.ByteBufferUtils.CorrectedParameters region}.
	 * @see {@link Artboard#getRegionOfIndexPixelsAlternate(int, int, int, int) getRegionOfIndexPixelsAlternate(int, int, int, int)}.
	 * 
	 * @param region — correct indices and dimensions for this artboard
	 * @return 2D array containing all the pixels of the region specified.
	 */
	@RenderThreadOnly public IndexPixel[][] getRegionOfIndexPixelsAlternate(CorrectedResult region) {
		
		return getRegionOfIndexPixelsAlternate(region.leftX() , region.bottomY() , region.width() , region.height());
		
	}
	
	private IndexPixel[][] getRegionOfIndexPixelsInternal(ByteBuffer indexTexels , int leftX , int bottomY , int width , int height) {

		IndexPixel[][] region = new IndexPixel[height][width];		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) region[row][col] = indexTexture.getPixel(indexTexels);
		indexTexture.freeTexelBuffer(indexTexels);		
		return region;
		
	}
	
	/**
	 * Gets a palette pixel at a given set of indices within the palette. The given index values are to be indices into the palette 
	 * directly. They are <b>not</b> indices of the image texture.
	 * 
	 * @param paletteXIndex — x index into the palette to retrieve a color
	 * @param paletteYIndex — y index into the palette to retrieve a color
	 * @return {@code PalettePixel} containing channel values for the pixel at the given indices.
	 */
	@RenderThreadOnly public PalettePixel getColorFromIndicesOfPalette(int paletteXIndex , int paletteYIndex) {
		
		paletteXIndex = Byte.toUnsignedInt((byte)paletteXIndex);
		paletteYIndex = Byte.toUnsignedInt((byte)paletteYIndex);

		return activeLayer().palette.getColorByIndices(paletteXIndex , paletteYIndex);
		
	}

	/**
	 * Gets a palette pixel at a given set of indices within the palette. The given index values are to be indices into the palette 
	 * directly. They are <b>not</b> indices of the image texture.
	 * 
	 * @param texelData — previously gotten texel buffer
	 * @param paletteXIndex — x index into the palette to retrieve a color
	 * @param paletteYIndex — y index into the palette to retrieve a color
	 * @return Array of numbers containing channel values for the pixel at the given indices.
	 */
	@RenderThreadOnly public PalettePixel getColorFromIndicesOfPalette(ByteBuffer texelData , int paletteXIndex , int paletteYIndex) {

		paletteXIndex = Byte.toUnsignedInt((byte)paletteXIndex);
		paletteYIndex = Byte.toUnsignedInt((byte)paletteYIndex);
		
		return activeLayer().palette.getColorByIndicesFromTexelBuffer(texelData , paletteXIndex , paletteYIndex);
		
	}
	
	/**
	 * Gets a color in the palette from the values of {@code pixel}.
	 * 
	 * @param pixel — an index pixel from the index texture
	 * @return Color value stored at the indices of the palette referenced by {@code pixel}.
	 */
	@RenderThreadOnly public PalettePixel getColorPointedToBy(LookupPixel pixel) {
		
		return getColorFromIndicesOfPalette(pixel.lookupX(), pixel.lookupY());
		
	}
		
	/**
	 * Gets the index pixel located at {@code (indexImageXIndex , indexImageYIndex)}, and uses it to get the color in the color palette
	 * pointed to by the pixel's {@code xIndex} and {@code yIndex}. 
	 * 
	 * @param indexImageXIndex — x index of a pixel in the index image
	 * @param indexImageYIndex — y index of a pixel in the index image
	 * @return Color in the palette who the index pixel at {@code (indexImageXIndex , indexImageYIndex)} points to.
	 */
	@RenderThreadOnly public PalettePixel getColorPointedToByIndexPixel(int indexImageXIndex , int indexImageYIndex) {
		
		LookupPixel indicesOfPixelAtIndex = getPixelByIndex(indexImageXIndex, indexImageYIndex);		
		return getColorFromIndicesOfPalette(indicesOfPixelAtIndex.lookupX() , indicesOfPixelAtIndex.lookupY());
		
	}

	/**
	 * Gets the index pixel located at {@code (indexTextureXIndex , indexTextureYIndex)}, and uses it to get the color in the color 
	 * palette pointed to by the pixel's {@code xIndex} and {@code yIndex}.
	 * 
	 * @param indexTexelBuffer — container of texel data for the index texture
	 * @param paletteTexelBuffer — container of texel data for the palette texture
	 * @param indexTextureXIndex — x index of a pixel in the index image
	 * @param indexTextureYIndex — y index of a pixel in the index image
	 * @return Color in the palette who the index pixel at {@code (indexImageXIndex , indexImageYIndex)} points to.
	 */
	@RenderThreadOnly public PalettePixel getColorPointedToByIndexPixel(
		ByteBuffer indexTexelBuffer , 
		ByteBuffer paletteTexelBuffer , 
		int indexTextureXIndex , 
		int indexTextureYIndex
	) {
		
		IndexPixel indices = indexTexture.getPixelByIndex(indexTexelBuffer , indexTextureXIndex , indexTextureYIndex);
		return getColorFromIndicesOfPalette(paletteTexelBuffer , indices.xIndex , indices.yIndex);
		
	}
	
	/**
	 * Constructs a {@code PalettePixel} from the settings of this artboard's palette texture, returning the result. The resulting pixel 
	 * is <b>not</b> stored in this artboard's palette. 
	 * 
	 * @param channelValues — color channel values for the palette pixel
	 * @return {@code PalettePixel} resulting from creating a pixel whose contents are {@code channelValues}.
	 */
	public PalettePixel createPalettePixel(final byte[] channelValues) {
		
		specify(
			channelValues.length == activeLayer().pixelSizeBytes() , 
			channelValues.length + " is not a valid number of channel values. " + activeLayer().pixelSizeBytes() + " expected."
		);
		
		return activeLayer().palette.new PalettePixel(channelValues);
				
	}
	
	/**
	 * Constructs a {@code PalettePixel} from the given buffer whose contents are assumed to be pixel channel values. The given buffer's position
	 * is updated by this method.
	 * 
	 * @param source — a container of channel values.
	 * @return Newly created palette pixel.
	 */
	public PalettePixel createPalettePixel(ByteBuffer source) {
		
		specify(source.remaining() >= activeLayer().palette.channelsPerPixel , "Not enough data remaining for creation of color pixel.");
		return activeLayer.palette.new PalettePixel(source);
		
	}
	
	/**
	 * Returns a read-only {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer} containing the texel data of the index
	 * texture.
	 *  
	 * @return Read-only {@code @FreeAfterUse ByteBuffer} containing the texel data of the index texture.
	 */
	@RenderThreadOnly public ByteBuffer indexTextureTexelBuffer() {
		
		return indexTexture.allTexelData().asReadOnlyBuffer();
		
	}
	
	/**
	 * Returns a read-only {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer} containing the texel data of the index
	 * texture at the given offsets and of the given dimensions. The returned buffer's contents are exactly as expected.
	 * 
	 * @param x — left x index of the region in texel coordinates
	 * @param y — bottom y index of the region in texel coordinates 
	 * @param width — width of the region in texel coordinates
	 * @param height — height of the region in texel coordinates
	 * @return Read-only {@code @FreeAfterUse ByteBuffer} containing the texel data of the index texture.
	 */
	@RenderThreadOnly public ByteBuffer indexTextureTexelBufferFormatted(int x , int y , int width , int height) {
		
		return indexTexture.texelBufferWithReformat(width , height , x , y).asReadOnlyBuffer();
		
	}
	
	/**
	 * Returns a read only {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer} containing the texel data of the index
	 * texture at the given offsets and of the given dimensions. The returned buffer is not formatted at all and its contents may not be
	 * exactly as expected.
	 * 
	 * @param x — left x index of the region in texel coordinates
	 * @param y — bottom y index of the region in texel coordinates 
	 * @param width — width of the region in texel coordinates
	 * @param height — height of the region in texel coordinates
	 * @return Read-only {@code @FreeAfterUse ByteBuffer} containing the texel data of the index texture.
	 */
	@RenderThreadOnly public ByteBuffer indexTextureTexelBufferUnformatted(int x , int y , int width , int height) {
		
		ByteBuffer texels = indexTexture.texelBuffer(x, y, width, height);
		return texels != null ? texels.asReadOnlyBuffer() : null;
		
	}
	
	/**
	 * Returns a read-only {@code ByteBuffer} containing the texel data of the palette texture.
	 * 
	 * @return Read-only {@code ByteBuffer} containing the texel data of the palette texture.
	 */
	@RenderThreadOnly public ByteBuffer paletteTexelBuffer() {
		
		return activeLayer().palette.texelData();
		
	}
	
	/**
	 * Frees a texel buffer representing the memory of the index texture. 
	 * 
	 * @param texelBuffer — a {@code ByteBuffer} containing texel data
	 */
	public void freeIndexTextureTexelBuffer(ByteBuffer texelBuffer) {
		
		indexTexture.freeTexelBuffer(texelBuffer);
		
	}

	/**
	 * Puts a new value at the pixel in the palette texture located at {@code (paletteXIndex , paletteYIndex)}.
	 * 
	 * @param paletteXIndex — x index into the palette texture to replace
	 * @param paletteYIndex — y index into the palette texture to replace
	 * @param replaceWithThis — pixel containing color data to put in the palette texture
	 */
	@RenderThreadOnly public void replacePalettePixelAtIndex(int paletteXIndex , int paletteYIndex , ColorPixel replaceWithThis) {
		
		PalettePixel asPalettePixel;
		
		if(replaceWithThis instanceof PalettePixel palettePixel) asPalettePixel = palettePixel;
		else asPalettePixel = activeLayer().palette.new PalettePixel(replaceWithThis);
		
		activeLayer().palette.put(paletteXIndex , paletteYIndex , asPalettePixel);
		
	}
	
	/**
	 * Creates a visual layer for this artboard from the given prototype.
	 * 
	 * @param prototype — prototype layer
	 */
	public void addVisualLayer(VisualLayer layer) {
		
		visualLayers.add(layer);
		if(activeLayer() == null) setActiveLayer(layer);
				
	}

	/**
	 * Adds a nonvisual layer for this artboard.
	 * 
	 * @param layer — nonvisual layer to add
	 */
	public void addNonVisualLayer(NonVisualLayer layer) {
		
		nonVisualLayers.add(layer);
				
	}
	
	/**
	 * Sets the active layer of this artboard to {@code layer}. This method accepts either {@linkplain NonVisualLayer} or 
	 * {@linkplain VisualLayer}, handling logic regarding the type of the layer.
	 * 
	 * @param layer — the new active layer
	 */
	public void setActiveLayer(Layer layer) {
		
		this.activeLayer = layer;
		isActiveLayerVisual = activeLayer instanceof VisualLayer;
	
	}

	/**
	 * Returns whether the given layer is the active layer.
	 * 
	 * @param layer — a layer
	 * @return {@code true} if {@code layer} is this artboard's current layer.
	 */
	public boolean isActiveLayer(Layer layer) {
		
		return this.activeLayer() == layer;
		
	}
	
	/**
	 * Invokes {@code callback} on each visual layer.
	 * 
	 * @param callback — code to execute on each visual layer.
	 */
	public void forEachVisualLayer(Consumer<VisualLayer> callback) {
		
		visualLayers.forEach(callback);
		
	}
	
	/**
	 * Invokes {@code callback} on each nonvisual layer.
	 * 
	 * @param callback — code to execute on each visual layer.
	 */
	public void forEachNonVisualLayer(Consumer<NonVisualLayer> callback) {
		
		nonVisualLayers.forEach(callback);
		
	}
	
	/**
	 * Creates and returns an iterator over the visual layers of this artboard. Supports only {@link java.util.Iterator#hasNext() hasNext}
	 * and {@link java.util.Iterator#next() next}.
	 * 
	 * @return Iterator over the visual layers of this artboard.
	 */
	public Iterator<VisualLayer> visualLayers () {
		
		return visualLayers.iterator();
		
	}

	/**
	 * Creates and returns an iterator over the nonvisual layers of this artboard. Supports only 
	 * {@link java.util.Iterator#hasNext() hasNext} and {@link java.util.Iterator#next() next}.
	 * 
	 * @return Iterator over the nonvisual layers of this artboard.
	 */
	public Iterator<NonVisualLayer> nonVisualLayers () {
		
		return nonVisualLayers.iterator();
		
	}
	
	/**
	 * Retrieves a region of pixels of the current layer. The region starts at {@code (xIndex , yIndex)} and extends {@code width} rightward and 
	 * {@code height} upward. 
	 * 
	 * @param xIndex — x index of a pixel, left coordinate of the region
	 * @param yIndex — y index of a pixle, bottom coordinate of the region
	 * @param width — width of the region
	 * @param height — height of the region
	 * @return 2D array containing the active layer's modifications to the given region.
	 */
	public LayerPixel[][] getRegionOfLayerPixels(int xIndex , int yIndex , int width , int height) {
		
		return activeLayer.get(xIndex, yIndex , width , height);
		
	}
	
	/**
	 * Retrieves a region of pixels from the current layer from the given 
	 * {@link cs.csss.utils.ByteBufferUtils.CorrectedParameters CorrectedParameters}.
	 * 
	 * @param params — correct indices and dimensions for this artboard 
	 * @return 2D array containing the active layer's modifications to the given region.
	 */
	public LayerPixel[][] getRegionOfLayerPixels(CorrectedResult params) {
		
		return getRegionOfLayerPixels(params.leftX() , params.bottomY() , params.width() , params.height());
		
	}
	
	/**
	 * Stores the values of the given index pixel in the active layer at the given position. 
	 * 
	 * @param source — a pixel to copy
	 * @param xPosition — x position of the pixel to copy
	 * @param yPosition — y position of the pixel to copy
	 */
	public void putInActiveLayer(LookupPixel source , int xPosition , int yPosition) {
		
		if(activeLayer() == null) return;
		
		LayerPixel pixel = new LayerPixel(xPosition , yPosition , source.lookupX() , source.lookupY());		
		activeLayer().put(pixel);
		
	}
	
	/**
	 * Stores the values of the given index pixel in a block of the active layer, where the {@code xPosition} and {@code yPosition} are 
	 * indices of the bottom left corner of the region and the region extends {@code width} and {@code height} positions out from the 
	 * bottom left corner.
	 * 
	 * @param source — a pixel to copy 
	 * @param xPosition — x position of the bottom left corner of the region to copy to
	 * @param yPosition — y position of the bottom left corner of the region to copy to
	 * @param width — width of the region to copy
	 * @param height — height of the region to copy
	 */
	public void bulkPutInActiveLayer(LookupPixel source , int xPosition , int yPosition , int width , int height) {
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) putInActiveLayer(source , xPosition + col , yPosition + row);
		
	}
	
	/**
	 * Puts the given region of lookup pixels in the layer, starting at the given bottom left corner and extending {@code width} and {@code height}
	 * pixels outward.
	 *   
	 * @param source — region of pixels
	 * @param leftX — left x coordinate to begin writing to the layer
	 * @param bottomY — bottom y coordinate to begin writing to the layer
	 * @param width — width of the region to write 
	 * @param height — height of the region to write
	 */
	public void bulkPutInActiveLayer(LookupPixel[][] source , int leftX , int bottomY , int width , int height) {
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) { 
			
			if(source[row][col] != null) putInActiveLayer(source[row][col] , leftX + col , bottomY + row);
			
		}
		
	}
	
	/**
	 * Performs a bulk replace operation on the active layer. For each index of {@code source}, if the pixel is <code>null</code>, and modification the 
	 * active layer contains at that index is removed. If the pixel at any index is nonnull, a new modification is made at that position.
	 * 
	 * @param leftX left x coordinate in artboard coordinates of the region to bulk replace
	 * @param bottomY bottom y coordinate in artboard coordinates of the region to bulk replace
	 * @param width width of the region to replace
	 * @param height height of the region to replace
	 * @param source 2D array of pixels to replace to
	 * @throws NullPointerException if {@code source} is <code>null</code>.
	 * @throws IllegalArgumentException if either {@code width} or {@code height} is not positive.
	 * @throws IndexOutOfBoundsException if any of {@code leftX , bottomY , leftX + width , bottomY + height} is out of bounds for this artboard.
	 */
	public void bulkReplaceInActiveLayer(int leftX , int bottomY , int width , int height, LookupPixel[][] source) {
	
		Objects.requireNonNull(source);
		checkParameters(leftX, bottomY, width, height);
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
			
			int artboardX = leftX + col;
			int artboardY = bottomY + row;
			
			if(source[col][row] == null) { 
				
				if(activeLayer.containsModificationTo(artboardX , artboardY)) activeLayer.remove(artboardX , artboardY);
								
			} else activeLayer.put(new LayerPixel(artboardX , artboardY , source[col][row].lookupX() , source[col][row].lookupY()));
			
		}
		
	}
	
	/**
	 * Stores the given pixel in the active layer but does not write to this artboard's index texture.
	 * 
	 * @param source pixel to put 
	 * @param xPosition x index to put {@code source} at
	 * @param yPosition y index to put {@code source} at
	 * @throws NullPointerException if {@code source} is <code>null</code>.
	 * @throws IndexOutOfBoundsException if either {@code xPosition} or {@code yPosition} is out of bounds for this artboard.
	 */
	public void putInActiveLayer2(Pixel source , int xPosition , int yPosition) {
		
		Objects.requireNonNull(source);
		Objects.checkIndex(xPosition, width());
		Objects.checkIndex(yPosition, height());
		
		LookupPixel lookup;
		
		if(source instanceof ColorPixel asColor) lookup = activeLayer.palette.putOrGetColors(asColor);
		else lookup = (LookupPixel)source;
		
		activeLayer.put(new LayerPixel(xPosition , yPosition , lookup.lookupX() , lookup.lookupY()));
		
	}
	
	/**
	 * Returns the highest ranked layer that modifies the pixel at the given indices. If no layer does so, <code>null</code> is returned.
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return The {@link VisualLayer} that modifies the pixel at {@code (xIndex , yIndex)}, or <code>null</code> if none exists.
	 */
	public VisualLayer getHighestRankLayerModifying(int xIndex , int yIndex) {
		
		for(VisualLayer x : visualLayers) if(!x.hiding && x.containsModificationTo(xIndex, yIndex)) return x;
		return null;
		
	}
	
	/**
	 * Returns whether a layer of a greater rank than that of {@code index} modifies the given pixel position.
	 * 
	 * @param superiorToThis — index in the visual layer list of the layer whose superior layers are being queried
	 * @param xIndex — x index of a position of a pixel
	 * @param yIndex — y index of a position of a pixel
	 * @return {@code true} if the given position is modified by any layer of a greater rank to the one located at {@code index}.
	 */
	public boolean isUpperRankLayerModifying(int superiorToThis , int xIndex , int yIndex) {
		
		return isUpperRankLayerModifying(superiorToThis, 0, xIndex, yIndex);
		
	}
	
	/**
	 * 
	 * Returns whether a layer of a greater rank than that of {@code index} modifies the given pixel position.
	 * 
	 * @param superiorToThis — index in the visual layer list of the layer whose superior layers are being queried
	 * @param toRank — index of a last layer to check 
	 * @param xIndex — x index of a position of a pixel
	 * @param yIndex — y index of a position of a pixel
	 * @return {@code true} if the given position is modified by any layer of a greater rank to the one located at {@code index} up to and
	 * 		   including the layer at {@code toRank}.
	 */
	public boolean isUpperRankLayerModifying(int superiorToThis , final int toRank , int xIndex , int yIndex) {
		
		if(!isActiveLayerVisual) return false;
		
		require(superiorToThis < visualLayers.size());
		require(toRank >= 0);
				
		for(int i = toRank ; i < superiorToThis ; i++) if(visualLayers.get(i).isModifying(xIndex, yIndex)) return true;		
		return false;
		
	}
	
	/**
	 * Returns whether a layer of a lower rank than {@code inferiorToThis} modifies the pixel at the given indices.
	 * 
	 * @param inferiorToThis — index of a visual layer whose inferior layers are queried
	 * @param xIndex — x index of a layer to check
	 * @param yIndex — y index of a layer to check
	 * @return {@code true} if the given position is modified by any layer of a lower rank to the one located at {@code inferiorToThis} 
	 * 		   up to and including the end of the list.
	 */
	public boolean isLowerRankLayerModifying(int inferiorToThis , int xIndex , int yIndex) {
		
		return isLowerRankLayerModifying(inferiorToThis, visualLayers.size() - 1 , xIndex , yIndex);
		
	}
	
	/**
	 * Returns whether any layer of a lower rank to {@code inferiorToThis} modify the given pixel indices. This method checks all layers
	 * between {@code inferiorToThis} exclusive and {@code toRank} inclusive.
	 * 
	 * @param inferiorToThis — index of a visual layer whose inferior layers are queried
	 * @param toRank — layer of a inferior rank to {@code inferiorToThis} that will be the upper bound of this method's operation
	 * @param xIndex — x index of a layer to check
	 * @param yIndex — y index of a layer to check
	 * @return {@code true} if the given position is modified by any layer of a lower rank to the one located at {@code inferiorToThis} 
	 * 		   up to and including the layer at {@code inferiorToThis}.
	 */
	public boolean isLowerRankLayerModifying(int inferiorToThis , int toRank , int xIndex , int yIndex) {
		
		specify(inferiorToThis > toRank , toRank + " must be greater than " + inferiorToThis + ".");
		
		
		if(!isActiveLayerVisual) return false;

		for(int i = inferiorToThis + 1 ; i <= toRank ; i++) if(visualLayers.get(i).isModifying(xIndex, yIndex)) return true;		
		return false;
		
	}
	
	/**
	 * Returns the closest layer to {@code inferiorToThis} who modifies the pixel at {@code (xIndex , yIndex)}. If no such layer exists,
	 * {@code null} is returned.
	 * 
	 * @param inferiorToThis — index of a visual layer whose inferior layers are queried
	 * @param xIndex — x index of a layer to check
	 * @param yIndex — y index of a layer to check
	 * @return Visual layer who modifies the given pixel indices.
	 */
	public VisualLayer getHighestLowerRankLayerModifying(int inferiorToThis , int xIndex , int yIndex) {
		
		return getHighestLowerRankLayerModifying(inferiorToThis, visualLayers.size() - 1 , xIndex , yIndex);
		
	}
	
	/**
	 * Returns the closest layer to {@code inferiorToThis} who modifies the pixel at {@code (xIndex , yIndex)}. This method stops its 
	 * checking at {@code stopAt} inclusive.
	 * 
	 * @param inferiorToThis — index of a visual layer whose inferior layers are queried
	 * @param stopAt — index of a layer to stop at.
	 * @param xIndex — x index of a layer to check
	 * @param yIndex — y index of a layer to check
	 * @return Visual layer closest to {@code inferiorToThis} in rank that modifies {@code (xIndex , yIndex)}.
	 */
	public VisualLayer getHighestLowerRankLayerModifying(int inferiorToThis , int stopAt , int xIndex , int yIndex) {
		
		if(!isActiveLayerVisual) return null;
		
		if(inferiorToThis != visualLayers.size() - 1) specify(stopAt > inferiorToThis , stopAt + " must be greater than " + inferiorToThis);
		
		specify(inferiorToThis >= 0 , inferiorToThis + " is an invalid layer index.");
		
		for(int i = inferiorToThis + 1 ; i <= stopAt ; i++) {
			
			VisualLayer iter = visualLayers.get(i);			
			if(iter.isModifying(xIndex, yIndex)) return iter;
			
		}
		
		return null;
		
	}
	
	/**
	 * Returns whether at least one pixel of the pixels in the region specified by the parameters is modified by a layer of a greater rank
	 * to the one at {@code index}.
	 * 
	 * @param index — index in the visual layer list of the layer whose superior layers are being queried
	 * @param xIndex — x index of the bottom left corner of the region to query
	 * @param yIndex — y index of the bottom left corner of the region to query
	 * @param width — width of the region to query
	 * @param height — height of the region to query
	 * @return {@code true} if at least one pixel of the region is modified by a layer of a greater rank than xIndex.
	 */
	public boolean bulkIsUpperRankLayerModifying(int index , int xIndex , int yIndex , int width , int height) {
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
			
			if(isUpperRankLayerModifying(index , xIndex + col , yIndex + row)) return true;
			
		}
		
		return false;
		
	}
	
	/**
	 * Returns whether the pixel at the given indices in the active layer is the same as {@code someColor}. 
	 * 
	 * @param someColor — any pixel implementation
	 * @param xIndex — x coordinate of a pixel on this artboard
	 * @param yIndex — y coordinate of a pixel on this artboard
	 * @return <code>true</code> if someColor matches the color given at the indices. 
	 */
	public boolean doColorsMatch(Pixel someColor , int xIndex , int yIndex) {
		
		LayerPixel pixel = activeLayer.get(xIndex, yIndex);
		if(pixel == null) return false;
		if(someColor instanceof LookupPixel asLookup) return someColor.compareTo(asLookup) == 0;
		else if(someColor instanceof ColorPixel asColor) {
			
			LookupPixel indices = activeLayer.palette.getIndicesOfColor(asColor);			
			return indices != null && pixel.compareTo(indices) == 0;
			
		}
	
		return false;
		
	}
	
	/**
	 * Moves the active layer to {@code newRank} rank.
	 * 
	 * @param newRank — new rank for the active layer
	 */
	public void moveVisualLayerRank(int newRank) {
		
		if(!isActiveLayerVisual) return;
		
		moveVisualLayerRank(visualLayers.indexOf((VisualLayer)activeLayer()) , newRank);
		
	}
	
	/**
	 * Moves the layer of rank {@code moveThisRank} to {@code toThisRank} rank.
	 * 
	 * @param moveThisRank — rank of a layer to move
	 * @param toThisRank — new rank for the layer at {@code moveThisRank} 
	 */ 
	public void moveVisualLayerRank(int moveThisRank , int toThisRank) {
		 
		specify(moveThisRank >= 0 && moveThisRank < visualLayers.size() , moveThisRank + " is invalid as a layer rank.");
		specify(toThisRank >= 0 && toThisRank < visualLayers.size() , toThisRank + " is invalid as a layer rank.");
		
		VisualLayer swap = visualLayers.remove(moveThisRank);
		visualLayers.add(toThisRank , swap);
		
	}
	
	/**
	 * Finds {@code layer} in the list of visual layers, returning its rank.
	 * 
	 * @param layer — a layer whose rank is being queried
	 * @return Integer representing rank.
	 */
	public int getLayerRank(VisualLayer layer) {
		
		return visualLayers.indexOf(layer);
		
	}
	
	/**
	 * Returns whether the layer at {@code layerRank} modifies the pixel at {@code (xIndex , yIndex)}.
	 * 
	 * @param layerRank — rank of a visual layer
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return {@code true} if the layer of rank {@code layerRank} modifies {@code (xIndex , yIndex)}.
	 */
	public boolean isLayerModifying(int layerRank , int xIndex , int yIndex) {
		
		if(!isActiveLayerVisual) return false;
		
		require(layerRank <= visualLayers.size() - 1);
		require(xIndex >= 0 && xIndex < indexTexture.width);
		require(yIndex >= 0 && yIndex < indexTexture.height);
		
		return visualLayers.get(layerRank).isModifying(xIndex, yIndex);
		
	}
	
	/**
	 * Toggles visibility of {@code layer}.
	 * 
	 * @param layer — a layer whose visibility is to be toggled
	 */
	@RenderThreadOnly public void toggleHideLayer(VisualLayer layer) {
		
		if(!layer.hiding()) layer.hide(this);
		else layer.show(this);
				
	}
	
	/**
	 * Gets the layer of the given rank.
	 * 
	 * @param rank — rank of a layer
	 * @return Visual layer at that rank.
	 */
	public VisualLayer getVisualLayer(int rank) {
		
		return visualLayers.get(rank);
		
	}
	
	/**
	 * Returns a nonvisual layer at the given index.
	 * 
	 * @param index — index of a nonvisual layer
	 * @return Nonvisual layer at the given index.
	 */
	public NonVisualLayer getNonVisualLayer(int index) {
		
		return nonVisualLayers.get(index);
		
	}
	
	/**
	 * Gets the visual layer whose prototype is {@code prototype}.
	 * 
	 * @param prototype — prototype of a visual layer from which an instance is derived and contained within this artboard 
	 * @return Instance of visual layer derived from {@code prototype}.
	 */
	public VisualLayer getVisualLayer(VisualLayerPrototype prototype) {

		for(int i = 0 ; i < visualLayers.size() ; i++) if(visualLayers.get(i).isInstanceOfPrototype(prototype)) return visualLayers.get(i);
		throw new IllegalStateException("Layer not found!");
		
	}

	/**
	 * Gets the nonvisual layer whose prototype is {@code prototype}.
	 * 
	 * @param prototype — prototype of a nonvisual layer from which an instance is derived and contained within this artboard 
	 * @return Instance of nonvisual layer derived from {@code prototype}.
	 */
	public NonVisualLayer getNonVisualLayer(NonVisualLayerPrototype prototype) {
		
		for(int i = 0 ; i < nonVisualLayers.size() ; i ++) if(nonVisualLayers.get(i).isInstanceOfPrototype(prototype)) {
			
			return nonVisualLayers.get(i);
			
		}
		
		throw new IllegalStateException("Layer not found");
		
	}
		
	/**
	 * Removes the abstract layer {@code layer} from whatever list contains its implementation type.
	 * 
	 * @param layer — a layer to remove
	 */
	@RenderThreadOnly public void removeLayer(Layer layer) {
		
		if(layer instanceof VisualLayer) visualLayers.remove(layer);
		else nonVisualLayers.remove(layer);
		
	}
	
	/**
	 * Hides all layers.
	 */
	@RenderThreadOnly public void hideAllVisualLayers() {
		
		setToCheckeredBackground();
		forEachVisualLayer(layer -> layer.hiding(true));
		
	}
	
	/**
	 * Shows all visual layers.
	 * 
	 */
	@RenderThreadOnly public void showAllVisualLayers() {
		
		for(int i = visualLayers.size() - 1 ; i  >= 0 ; i--) visualLayers.get(i).show(this);
		
	}
	
	/**
	 * Invokes {@link cs.csss.project.VisualLayer#show(Artboard) show} on all layers of this artboard that are not marked as hidden.
	 */
	@RenderThreadOnly public void showAllNonHiddenVisualLayers() {
		
		for(int i = visualLayers.size() - 1 ; i  >= 0 ; i--) { 
			
			VisualLayer x = visualLayers.get(i);
			if(!x.hiding) x.show(this);
			
		}
		
	}
	
	/**
	 * Determines the background color to display at {@code (xIndex , yIndex)}. Will return one of the transparent background checker 
	 * colors.
	 * 
	 * @param xIndex — x index of a pixel whose background is being checked
	 * @param yIndex — y index of a pixel whose background is being checked
	 * @return Palette pixel containing the values of a transparent background checker color.
	 */
	@RenderThreadOnly public PalettePixel getBackgroundColor(int xIndex , int yIndex) {

		/*
		 * Determine what quadrant of the checkered background the pixel at the given indices is in.
		 * Divide the indices by the corresponding dimension to get their region 
		 * The regions' oddness or evenness tells us whether to pick the darker or lighter color to put in the image. 
		 * However, if the region of the y index is even, we need to choose the opposite of what the color was. 
		 *
		 * Basically, the columns alternate between the dark and light colors. We want the rows to also alternate, giving a checkered 
		 * look. To do this, switch the starting color at the start of every row.
		 */ 

		int xRegion = xIndex / IndexTexture.backgroundWidth;
		int yRegion = yIndex / IndexTexture.backgroundHeight;
		
		//odd exponent, use the darker background otherwise use the lighter one
		boolean darker = (xRegion & 1) == 1;
		//flip darker if the y region is even
		if((yRegion & 1) == 0) darker = !darker;
				
		return getColorPointedToBy(darker ? indexTexture.darkerTransparentBackground : indexTexture.lighterTransparentBackground);
		
	}

	/**
	 * Gets the lookup pixel for the background color located at the given indices.
	 * 
	 * @param xIndex — x index of a background color to look up
	 * @param yIndex — y index of a background color to look up
	 * @return Lookup pixel containing the lookups for the background color that corresponds to the given indices.
	 */
	@RenderThreadOnly public LookupPixel getBackgroundColorIndices(int xIndex , int yIndex) {
		
		PalettePixel color = getBackgroundColor(xIndex , yIndex);
		return putInPalette(color);
		
	}
	
	/**
	 * Returns whether any layer at all (both visual and nonvisual) modifies the given pixel position.
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return {@code true} if any layer, either visual or nonvisual modifies the pixel at {@code (xIndex , yIndex)}.
	 */
	public boolean isAnyLayerModifying(int xIndex , int yIndex) {
		
		for(VisualLayer x : visualLayers) if(x.isModifying(xIndex, yIndex)) return true;		
		for(NonVisualLayer x : nonVisualLayers) if(x.isModifying(xIndex, yIndex)) return true;		
		return false;
		
	}

	/**
	 * Gets the color of the highest ranking layer's modification or {@code null} if no visible layer modifies the given indices.
	 * 
	 * <p>
	 * 	This method returns the color at the given indices but unlike similar methods, this does not use the artboard to determine what 
	 * 	color is at the given indices. It looks at layers to determine the correct color. If the current layer is visual, this method will
	 * 	return the value in the highest ranking layer that modifies the given indices. If the current layer is nonvisual, this method 
	 * 	returns the active layer's modification to the given indices. In the case that no relevent layers modify the given indices, this
	 * 	method returns {@code null}.
	 * </p>
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return Color stored by the highest ranking layer that modifies the given indices, or the color stored at the given indices if the
	 * 		   active layer is nonvisual, or null if any 
	 */
	@RenderThreadOnly public PalettePixel getHighestRankingColorForLayerModification(int xIndex , int yIndex) {
		
		LayerPixel mod;

		if(isActiveLayerVisual) {
			
			for(VisualLayer x : visualLayers) {
				
				if(x.hiding) continue;
				
				if((mod = x.get(xIndex, yIndex)) != null) return getColorFromIndicesOfPalette(mod.lookupX, mod.lookupY);
				
			}
		
		} else if(activeLayer().hiding() && (mod = activeLayer().get(xIndex, yIndex)) != null) { 
			
			return getColorFromIndicesOfPalette(mod.lookupX, mod.lookupY);
			
		}
		
		return null;
		
	}
	
	/**
	 * Returns a lookup pixel representing the highest ranked layer modification to the given pixel index. If no <em>visible</em> layer modifies 
	 * {@code (xIndex , yIndex)}, or if a nonvisual layer is active, {@code null} is returned.
	 * 
	 * @param xIndex x index of a pixel 
	 * @param yIndex y index of a pixel
	 * @return {@link LookupPixel} for the color currently visible at the given pixel position, or <code>null</code>.
	 */
	public LookupPixel getHighestRankingLookupForLayerModification(int xIndex , int yIndex) {

		if(isActiveLayerVisual) for(VisualLayer x : visualLayers) if(x.isModifying(xIndex, yIndex)) return x.get(xIndex, yIndex);				
		return null;
		
	}
	
	/**
	 * Gets a visual layer by the given name.
	 * 
	 * @param layerName — name of a visual layer
	 * @return Resulting layer.
	 */
	public VisualLayer getVisualLayer(String layerName) {
		
		for(VisualLayer x : visualLayers) if(x.name.equals(layerName)) return x;
		throw new IllegalArgumentException(layerName + " does not name a layer.");
		
	}

	/**
	 * Gets a nonvisual layer by the given name.
	 * 
	 * @param layerName — name of a nonvisual layer
	 * @return Resulting layer.
	 */
	public NonVisualLayer getNonVisualLayer(String layerName) {
		
		for(NonVisualLayer x : nonVisualLayers) if(x.name.equals(layerName)) return x;
		throw new IllegalArgumentException(layerName + " does not name a layer.");
		
	}

	/**
	 * Gets a layer by the given name. The list of visual layer is searched first, then the nonvisual.
	 * 
	 * @param layerName — name of a layer
	 * @return Resulting layer.
	 */
	public Layer getLayer(String name) {
		
		for(VisualLayer x : visualLayers) if(x.name.equals(name)) return x;
		for(NonVisualLayer x : nonVisualLayers) if(x.name.equals(name)) return x;
		throw new IllegalArgumentException(name + " does not name a layer.");
		
	}
	
	/**
	 * Overwrites the entire artboard image to the checkered background. No layers are modified. This method should only be called in 
	 * conjuction with layer show methods.
	 */
	@RenderThreadOnly public void setToCheckeredBackground() {
		
		indexTexture.setCheckerBackground();
		
	}
	
	/**
	 * Returns the number of visual layers.
	 * 
	 * @return The number of visual layers.
	 */
	public int numberVisualLayers() {
		
		return visualLayers.size();
		
	}
	
	/**
	 * Returns the number of channels of the active layer.
	 * 
	 * @return Number of channels per pixel of the active layer.
	 */
	public int activeLayerChannelsPerPixel() {
		
		return activeLayer().pixelSizeBytes();
		
	}
	
	/**
	 * Returns the width of this artboard.
	 * 
	 * @return Width of this artboard.
	 */
	public int width() {
		
		return (int) (rightX() - leftX());
		
	}

	/**
	 * Returns the height of this artboard.
	 * 
	 * @return Height of this artboard.
	 */
	public int height() {
		
		return (int) (topY() - bottomY());
		
	}

	/**
	 * Returns the top Y coordinate (in world space) of this artboard.
	 * 
	 * @return Top Y coordinate of this artboard.
	 */
	public float topY() {
		
		return positions.topY();
		
	}

	/**
	 * Returns the bottom Y coordinate (in world space) of this artboard.
	 * 
	 * @return Bottom Y coordinate of this artboard.
	 */
	public float bottomY() {
		
		return positions.bottomY();
		
	}

	/**
	 * Returns the left X coordinate (in world space) of this artboard.
	 * 
	 * @return Left X coordinate of this artboard.
	 */
	public float leftX() {
		
		return positions.leftX();
		
	}

	/**
	 * Returns the right X coordinate (in world space) of this artboard.
	 * 
	 * @return Right X coordinate of this artboard.
	 */
	public float rightX() {
		
		return positions.rightX();
		
	}

	/**
	 * Returns the midpoint X coordinate (in world space) of this artboard.
	 * 
	 * @return Midpoint X coordinate of this artboard.
	 */
	public float midX() {
		
		return leftX() + (width() / 2);
		
	}

	/**
	 * Returns the midpoint Y coordinate (in world space) of this artboard.
	 * 
	 * @return Midpoint Y coordinate of this artboard.
	 */
	public float midY() {
		
		return bottomY() + (height() / 2);
		
	}

	/**
	 * Returns the active layer of this artboard.
	 * 
	 * @return Active layer of this artboard.
	 */
	public Layer activeLayer() {
		
		return activeLayer;
		
	}
	
	/**
	 * Randomly generates a palette pixel by selecting random values within the bounds of the channel values for each channel value.
	 * 
	 * @return Randomly selected channel values into a pixel.
	 */
	@RenderThreadOnly public PalettePixel randomColor() {
		
		byte[] channelValues = new byte[activeLayerChannelsPerPixel()];
		Random random = new Random();
		for(int i = 0 ; i < channelValues.length ; i++) channelValues[i] = (byte) random.nextInt(256);		
		return createPalettePixel(channelValues);
		
	}

	/**
	 * Sets the active layer to a visual layer.
	 */
	public void switchToVisualLayers() {
		
		//there will always be one visual layer
		setActiveLayer(visualLayers.get(0));
		
	}
	
	/**
	 * Returns whether the active layer of this artbaord is a visual layer.
	 * 
	 * @return {@code true} if the active layer of this artbaord is a visual layer.
	 */
	public boolean isActiveLayerVisual() {
		
		return activeLayer() instanceof VisualLayer;
		
	}
	
	/**
	 * Removes a pixel from the artboard. This method will remove the pixel at the given indices from the active layer, and if necessary, 
	 * update the artboard image.
	 * 
	 * @param xIndex — x index of the pixel to remove
	 * @param yIndex — y index of the pixel to remove
	 */
	@RenderThreadOnly public void removePixel(int xIndex , int yIndex) {
		
		Layer active = activeLayer;
		if(!active.containsModificationTo(xIndex, yIndex)) return;
		
		active.remove(xIndex, yIndex);
		
		if(active instanceof VisualLayer visual) {
			
			int visualRank = getLayerRank(visual);			
			if(isUpperRankLayerModifying(visualRank, xIndex, yIndex)) return;
			
			VisualLayer lowerRanking = getHighestLowerRankLayerModifying(visualRank, xIndex , yIndex);
			if(lowerRanking == null) writeToIndexTexture(xIndex , yIndex , 1, 1, getBackgroundColor(xIndex , yIndex));
			else writeToIndexTexture(xIndex , yIndex, 1, 1, lowerRanking.get(xIndex , yIndex));
			
		} else writeToIndexTexture(xIndex , yIndex , 1, 1, getBackgroundColor(xIndex , yIndex));
		
	}
	
	/**
	 * Bulk pixel remove operation. Removes all pixels in the region given by {@code correct} in the current layer. 
	 * 
	 * @param correct corrected indices
	 */
	@RenderThreadOnly public void removePixels(CorrectedResult correct) {
		
		if(correct == null) return;
		removePixels(correct.leftX() , correct.bottomY() , correct.width() , correct.height());
		
	}
	
	/**
	 * Bulk pixel remove operation. Removes all pixels starting from {@code (leftX , bottomY)} and extending {@code width} right and 
	 * {@code height} up.
	 * 
	 * @param leftX — left x coordinate of the region
	 * @param bottomY — bottom y coordinate of the region
	 * @param width — width of the region
	 * @param height — height of the region
	 */
	@RenderThreadOnly public void removePixels(int leftX , int bottomY , int width , int height) {
		
		RegionIterator iter = Artboards.region(leftX, bottomY, width, height);
		RegionPosition x;
		while(iter.hasNext()) {
			
			x = iter.next();
			removePixel(x.col() , x.row());
			
		}
		
	}
	
	/**
	 * Hides visibility of the pixel at the given indices. Whatever pixel is currently present will be removed from the image, but not from any 
	 * layer. If a layer lower than the highest layer modifying the given position also modifies that position, its pixel is shown instead.
	 * 
	 * @param xIndex — x index of the pixel to hide
	 * @param yIndex — y index of the pixel to hide
	 */
	public void hidePixel(int xIndex , int yIndex) {
		
		if(isActiveLayerVisual) {
			
			VisualLayer highest = getHighestRankLayerModifying(xIndex, yIndex);
			if(highest != null) {
				
				VisualLayer lower = getHighestLowerRankLayerModifying(getLayerRank(highest), xIndex, yIndex);
				if(lower != null) writeToIndexTexture(xIndex, yIndex, 1, 1, lower.get(xIndex, yIndex));
				else writeToIndexTexture(xIndex , yIndex , 1 , 1 , getBackgroundColor(xIndex, yIndex));
				
			} else writeToIndexTexture(xIndex , yIndex , 1 , 1 , getBackgroundColor(xIndex, yIndex));
			
		} else writeToIndexTexture(xIndex , yIndex , 1 , 1 , getBackgroundColor(xIndex , yIndex));
		
	}
	
	/**
	 * Hides visibility of the pixels in the given region. Whatever pixel is currently present at each position in the region will be removed from 
	 * the image but not its layer. If a layer lower than the highest layer modifying any given position also modifies that position, its pixel is 
	 * shown instead.
	 * 
	 * @param leftX — left x coordinate of the region to modify
	 * @param bottomY — bottom y coordinate of the region to modify
	 * @param width — width of the region to modify
	 * @param height — height of the region to modify
	 */
	public void hidePixels(int leftX , int bottomY , int width , int height) {

		RegionIterator iter = Artboards.region(leftX, bottomY, width, height);
		RegionPosition x;
		while(iter.hasNext()) {
			
			x = iter.next();
			hidePixel(x.col() , x.row());
			
		}
		
	}
	
	/**
	 * Returns a lookup for the highest ranking modification to the given indices. If the current layer is nonvisual, a lookup to the pixel
	 * modifying the given indices is returned, or a background color lookup is returned.
	 * 
	 * @param xIndex — x index of a pixel whose highest lookup is being queried
	 * @param yIndex — y index of a pixel whose highest lookup is being queried
	 * @return {@code LookupPixel} containing lookups for the color at the given indices. 
	 */
	@RenderThreadOnly public LookupPixel highestPixelForIndex(int xIndex , int yIndex) {
		
		if(isActiveLayerVisual) {
			
 			PalettePixel color = getHighestRankingColorForLayerModification(xIndex, yIndex);
			if(color == null) color = getBackgroundColor(xIndex , yIndex);
			return putInPalette(color);
 			
		} else {
			
  			LookupPixel mod = activeLayer.get(xIndex, yIndex);
			if(mod == null) mod = getBackgroundColorIndices(xIndex, yIndex);
			return mod;
		}
		
	}
	
	/**
	 * Returns the name of this artboard.
	 * 
	 * @return Name of this artboard.
	 */
	public String name() {
		
		return name;
		
	}
	
	/*
	 *
	 *	SHAPES
	 *
	 */

 	/**
 	 * Creates a new ellipse with default parameters.
 	 * 
 	 * @return The newly created ellipse.
 	 */
   @RenderThreadOnly public Ellipse newEllipse() {
 		
 		byte z = (byte)0;
		ChannelBuffer color = new ChannelBuffer(z , z , z , (byte) 0xff);
		return activeLayer.newEllipse(this, 10, 10, color , color , false, true);
 		
 	}
   
   /**
    * Creates a new rectangle with default parameters.
    * 
    * @return The newly created rectangle.
    */
   @RenderThreadOnly public Rectangle newRectangle() {
	   
	   byte z = (byte)0;
	   ChannelBuffer color = new ChannelBuffer(z , z , z , (byte)0xff);
	   return activeLayer.newRectangle(this , 20 , 20 , color , color , false, true);
	   
   }
   
   /**
    * Removes  the given shape. It is removed from this artboard, but is <em>not</em> shut down.
    * 
    * @param remove shape to remove
    * @throws NullPointerException if {@code remove} is <code>null</code>.
    */
   public void removeShape(Shape remove) {
	   
	   Objects.requireNonNull(remove);
	   
	   //cannot make any assumptions about which layer it belongs to
	   
	   for(VisualLayer x : visualLayers) if(x.shapes.remove(remove)) return;
	   for(NonVisualLayer x : nonVisualLayers) if(x.shapes.remove(remove)) return;

	   throw new NoSuchElementException(remove + " not found in this artboard.");
	   
   }
 	
   /**
    * Returns an iterator over all ellipses in the active layer.
    * 
    * @return Iterator over ellipses in the active layer of this artboard.
    */
   public Iterator<Ellipse> activeLayerEllipses() {
	   
	   return activeLayer.ellipsesIterator();
	   
   }
   
   /**
    * Returns an iterator over all rectangles in the active layer.
    * 
    * @return Iterator over rectangles in the active layer of this artboard.
    */
   public Iterator<Rectangle> activeLayerRectangles() {
	   
	   return activeLayer.rectanglesIterator();
	   
   }
   
   /**
    * Returns the layer on this artboard that contains {@code shape}, or <code>null</code> if none does.
    * 
    * @param shape a shape to check for presence in this artboard's layers
    * @return The layer containing {@code shape}, or <code>null</code> if none does.
    * @throws NullPointerException if {@code shape} is <code>null</code>.
    */
   public Layer layerOwningShape(Shape shape) {
	   
	   Objects.requireNonNull(shape);
	   for(Layer x : visualLayers) if(x.containsShape(shape)) return x;
	   for(Layer x : nonVisualLayers) if(x.containsShape(shape)) return x;
	   return null;
	   
   }
   
   /**
    * Returns the number of ellipses in this artboard including both visual and nonvisual layers.
    * 
    * @return Total number of ellipses in this artboard.
    */
   public int numberEllipses() {
	 
	   int number = 0;
	   for(VisualLayer x : visualLayers) number += x.ellipsesStream().count();
	   for(NonVisualLayer x : nonVisualLayers) number += x.ellipsesStream().count();
	   return number;
	   
   }

   /**
    * Returns the number of rectangles in this artboard including both visual and nonvisual layers.
    * 
    * @return Total number of rectangles in this artboard.
    */
   public int numberRectangles() {
	 
	   int number = 0;
	   for(VisualLayer x : visualLayers) number += x.rectanglesStream().count();
	   for(NonVisualLayer x : nonVisualLayers) number += x.rectanglesStream().count();
	   return number;
	   
   }
   
   /*
    *
    * LINES
    * 
    */
   
   /**
    * Creates a new linear line in the active layer of this artboard.
    *  
    * @return Newly created line.
    */
    public LinearLine newLinearLine(ColorPixel color) {
   		
   		return activeLayer.lines.newLinearLine(color);
   		
   	}
   
    /**
     * Creates a new bezier line with the given color.
     * 
     * @param color color to make the bezier line
     * @return Resulting Bezier curve.
     */
    public BezierLine newBezierLine(ColorPixel color) {
    	
    	return activeLayer.lines.newBezierLine(color);
    	
    }

    /**
     * Returns an iterator over the linear lines of the current layer.
     * 
     * @return {@link Iterator} of {@link LinearLine}s for the current layer.
     */
    public Iterator<LinearLine> linearLines() {
    	
    	return activeLayer.linearLinesIterator();
    	
    }
    
    /**
     * Returns an iterator over the bezier lines of the current layer.
     * 
     * @return {@link Iterator} of {@link BezierLine}s for the current layer.
     */
    public Iterator<BezierLine> bezierLines() {
    	
    	return activeLayer.bezierLinesIterator();
    	
    }
    
    /**
     * Returns an iterator over all the lines of the current layer.
     * 
     * @return {@link Iterator} of {@link Line}s for the current layer.
     */
    public Iterator<Line> lines() {
    	
    	return activeLayer.linesIterator();
    	
    }
    
    /**
     * Removes the given line from whichever layer owns it.
     * 
     * @param line line to remove
     * @throws NullPointerException if {@code line} is <code>null</code>.
     * @throws IllegalArgumentException if {@code line} is not in any layer of this artboard.
     */
    public void removeLine(Line line) {
    	
    	Objects.requireNonNull(line);
    	
    	for(Layer x : visualLayers) if(x.containsLine(line)) { 
    		
    		x.removeLine(line);
    		return;
    		
    	}
    	
    	for(Layer x : nonVisualLayers) if(x.containsLine(line)) { 
    		
    		x.removeLine(line);
    		return;
    		
    	}
    	
    	throw new IllegalArgumentException("Given line is not in any layer of this artboard.");
    	
    }
    
    /**
     * Adds {@code line} to the active layer of this artboard. 
     * 
     * @param line a line to add
	 * @throws NullPointerException if {@code line} is <code>null</code>.
	 * @throws IllegalArgumentException if {@code line} is already in the active layer.
     */
    public void addLine(Line line) {
    	
    	activeLayer.addLine(line);
    	
    }
    
    public int numberLinearLines() {
    	
    	int number = 0;
    	for(VisualLayer x : visualLayers) number += x.linearLinesStream().count();
    	for(NonVisualLayer x : nonVisualLayers) number += x.linearLinesStream().count();    	
    	return number;
    	
    }

    public int numberBezierLines() {
    	
    	int number = 0;
    	for(VisualLayer x : visualLayers) number += x.bezierLinesStream().count();
    	for(NonVisualLayer x : nonVisualLayers) number += x.bezierLinesStream().count();    	
    	return number;
    	
    }
    
    /**
     * Returns the layer owning the given line, or <code>null</code> if no layer of this artboard owns {@code line}.
     * 
     * @param line a line to look for.
     * @return Layer owning {@code line}, or <code>null</code> if none does. 
     */
    public Layer layerOwningLine(Line line) {
    	
    	Iterator<Layer> layers = layers();
    	while(layers.hasNext()) {
    		
    		Layer x = layers.next();    		
    		if(x.containsLine(line)) return x;
    		
    	}
    	
    	return null;
    	
    }
    
    /**
     * Returns an iterator over all layers of this artboard.
     * 
     * @return Iterator over all layers of this artboard.
     */
    public Iterator<Layer> layers() {
    	
    	return new Iterator<>() {

    		Iterator<VisualLayer> visual = visualLayers();
    		Iterator<NonVisualLayer> nonvisual = nonVisualLayers();
    		
			@Override public boolean hasNext() {

				return visual.hasNext() || nonvisual.hasNext();
				
			}

			@Override public Layer next() {
				
				if(!hasNext()) throw new NoSuchElementException("No remaining layers for this iterator.");
				
				if(visual.hasNext()) return visual.next();
				else return nonvisual.next();
								
			}
    		
    	};
    	
    }
    
	IndexTexture indexTexture() {
		
		return indexTexture;
		
	}
	
	ArtboardPalette activeLayersPalette() {
		
		return activeLayer().palette;
		
	}
	
	ArtboardPalette visualPalette() {
		
		return visualLayers.get(0).palette;
		
	}

	private void storeLookupPixelsInLayer(LookupPixel[][] region , int leftX, int bottomY, int width, int height) {

		RegionIterator regionIter = Artboards.region(0 , 0 , width, height);
		Layer active = activeLayer;
		
		RegionPosition next;
		while(regionIter.hasNext()) {

			next = regionIter.next();
			LookupPixel pixel = region[next.row()][next.col()];
			if(pixel == null) continue;			
			active.put(new LayerPixel(leftX + next.col() , bottomY + next.row() , pixel));
						
		}
		
	}
	
	private LookupPixel getPixelByIndex(int x , int y) {
		
		Objects.checkIndex(x, width());
		Objects.checkIndex(y, height());
		
		if(isActiveLayerVisual) {
			
			VisualLayer highest = getHighestRankLayerModifying(x, y);
			if(highest != null) return highest.get(x, y);
			else return getBackgroundColorIndices(x, y);
			
		} else return activeLayer.isModifying(x, y) ? activeLayer.get(x, y) : getBackgroundColorIndices(x, y);
		
	}

	/**
	 * Invokes the given callback for each layer of this artboard.
	 * 
	 * @param callback callback to invoke for each layer
	 */
	public void forAllLayers(Consumer<Layer> callback) {
		
		visualLayers.forEach(callback);
		nonVisualLayers.forEach(callback);
		
	}
	
	/**
	 * Returns an iterator over all shapes in this artboard.
	 * 
	 * @return Iterator over all shapes in this artboard.
	 */
	public Iterator<Shape> activeLayerShapes() {
		
		return new Iterator<>() {

			Iterator<Ellipse> ellipses = activeLayerEllipses();
			Iterator<Rectangle> rectangles = activeLayerRectangles();
			
			@Override public boolean hasNext() {

				return ellipses.hasNext() || rectangles.hasNext();
				
			}

			@Override public Shape next() {

				if(!hasNext()) throw new NoSuchElementException("This iterator has no remaining shapes.");
				if(ellipses.hasNext()) return ellipses.next();
				else return rectangles.next();
				
			}
			
		};
		
	}

	/**
	 * Checks to ensure the given region is within bounds for this artboard. 
	 * 
	 * @param leftX left x coordinate of a region
	 * @param bottomY bottom y coordinate of a region
	 * @param width width of a region
	 * @param height height of a region
	 * @throws IndexOutOfBoundsException if either {@code leftX}, {@code leftX + width}, {@code bottomY}, or {@code bottomY + height} is out of bounds.
	 * 
	 */
	public void checkParameters(int leftX , int bottomY , int width , int height) {
		
		if(width <= 0) throw new IllegalArgumentException("Width is not positive: " + width);
		if(height <= 0) throw new IllegalArgumentException("Height is not positive: " + height);
		int artboardWidth = width();
		int artboardHeight = height();
		Objects.checkIndex(leftX, artboardWidth);
		Objects.checkIndex(bottomY, artboardHeight);
		Objects.checkIndex(leftX + width - 1, artboardWidth);
		Objects.checkIndex(bottomY + height - 1, artboardHeight);
		
	}
	
	/**
	 * Returns a lookup pixel representing {@code value}. If {@code value} is a lookup pixel, it is returned. If it is a color pixel, it is located
	 * in the palette of the active layer, and the resulting lookup pixel is returned.
	 * 
	 * @param value pixel to get as a lookup
	 * @return {@link LookupPixel} form of {@code value}
	 * @throws NullPointerException if {@code value} is <code>null</code>.
	 */
	public LookupPixel toLookup(Pixel value) {
		
		Objects.requireNonNull(value);
		if(value instanceof LookupPixel asLookup) return asLookup;
		else return activeLayer.palette.putOrGetColors((ColorPixel)value);
		
		
	}
	
	/**
	 * Returns whether this artboard is a shallow copy of some other artboard.
	 * 
	 * @return Whether this artboard is a shallow copy of some other artboard.
	 */
	public boolean isShallowCopy() {
		
		return isShallowCopy;
		
	}

	/**
	 * Shows all lines in this artboard that are in visual layers if the active layer is visual, or shows the lines in the active layer only if it is 
	 * 
	 * nonvisual.
	 */
	@RenderThreadOnly public void showAllLines() {
	
		if(isActiveLayerVisual) {
			
			visualLayers.stream()
				.filter(layer -> !layer.hiding())
				.forEach(layer -> layer.linesIterator().forEachRemaining(line -> line.reset(this, layer)));			
			
		} else {
			
			if(!activeLayer.hiding()) activeLayer.linesIterator().forEachRemaining(line -> line.reset(this, activeLayer));
						
		}
		
	}
	
	/**
	 * Puts all mods from all lines in all layers back in the artboard, even if those layers or lines are hidden.
	 */
	@RenderThreadOnly public void undoAllLines() {
		
		forAllLayers(layer -> layer.forEachLine(line -> line.putModsInArtboard(this , layer)));
				
	}

 	@RenderThreadOnly @Override public void shutDown() {

 		if(isFreed()) return; 
 		
 		Logging.sysDebugln("Shutting down artboard " + name);
 		
		vao.shutDown();
		indexTexture.shutDown();
		if(!isShallowCopy()) forAllLayers(layer -> layer.shutDown());
		
		Logging.sysDebugln("\tVAO Freed" , vao.isFreed() , "Texture Freed" , indexTexture.isFreed());
		 		
	}

 	@RenderThreadOnly @Override public boolean isFreed() {

		return vao.isFreed();
		
	}

}