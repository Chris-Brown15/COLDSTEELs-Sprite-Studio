package cs.csss.project;

import static cs.core.graphics.StandardRendererConstants.POSITION_2D;
import static cs.core.graphics.StandardRendererConstants.STATIC_VAO;
import static cs.core.graphics.StandardRendererConstants.UINT;
import static cs.core.graphics.StandardRendererConstants.UV;

import static cs.core.utils.CSUtils.specify;
import static org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11C.glPixelStorei;
import static cs.core.utils.CSUtils.require;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

import org.joml.Random;
import org.lwjgl.system.MemoryStack;

import cs.core.graphics.CSRender;
import cs.core.graphics.CSVAO;
import cs.core.graphics.utils.VertexBufferBuilder;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.VAOPosition;
import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.utils.Artboards;
import cs.csss.project.utils.RegionIterator;
import cs.csss.project.utils.StackOrHeapAllocation;

/**
 * Artboard contains the data needed to display and edit a graphic in CSSS and has methods to operate on pixels and layers.
 * 
 * @author Chris Brown
 *
 */
public class Artboard {
	
	/**
	 * Deep copies the visual layers of {@code source} into {@code destination}. They will be independent of one another for the purposes
	 * of layer modification.
	 * 
	 * @param project — the project 
	 * @param source — the source artboard 
	 * @param destination — the destination artboard
	 */
	private static void deepCopyVisualLayers(CSSSProject project , Artboard source , Artboard destination) {

		//creates all visual layers
		project.forEachVisualLayerPrototype(prototype -> {
			
			VisualLayer instance = new VisualLayer(destination , project.visualPalette() , prototype);
			destination.addVisualLayer(instance);
			
			VisualLayer sourceLayer = source.getVisualLayer(prototype);
			instance.hiding(sourceLayer.hiding);			
			sourceLayer.copy(instance);

			if(sourceLayer == source.activeLayer()) destination.setActiveLayer(instance);
			
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
			destination.addNonVisualLayer(instance);
			
			NonVisualLayer sourceLayer = source.getNonVisualLayer(proto);
			instance.hiding(sourceLayer.hiding);
			sourceLayer.copy(instance);
			
			if(sourceLayer == source.activeLayer()) destination.setActiveLayer(instance);
			
		});
		
	}

	/**
	 * Creates a deep copy of the source artboard, using the source artboard and the project.
	 * 
	 * @param source — source artboard
	 * @param project — owning project
	 * @return Copied project.
	 */
	static Artboard deepCopy(String newArtboardName , Artboard source , CSSSProject project) {
		
		Artboard newArtboard = new Artboard(newArtboardName , source.width() , source.height());
		
		deepCopyVisualLayers(project , source , newArtboard);	
		deepCopyNonVisualLayers(project , source , newArtboard);
		
		//copies the source artboard's image data to the next one		
		ByteBuffer sourceTexelBuffer = source.indexTexture.allTexelData();

		glPixelStorei(GL_UNPACK_ALIGNMENT , source.visualLayers.get(0).palette.channelsPerPixel);
		
		newArtboard.indexTexture.put(0 , 0 , source.width() , source.height() , sourceTexelBuffer);		
		source.indexTexture.freeTexelBuffer(sourceTexelBuffer);
		
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
	static Artboard shallowCopy(String newArtboardName , Artboard source) {

		Artboard newArtboard = new Artboard(newArtboardName , source.indexTexture , source.width() , source.height());

		newArtboard.visualLayers = source.visualLayers;
		newArtboard.nonVisualLayers = source.nonVisualLayers;
		newArtboard.setActiveLayer(source.activeLayer);
		
		return newArtboard;
		
	}
	
	//used to track whether the abstract Layer instance activeLayer below is a nonvisual or visual layer.
	private boolean isActiveLayerVisual = true;
	
	private CSVAO vao = new CSVAO();
	public final VAOPosition positions;
	private CSRender render;

	private IndexTexture indexTexture;
	
	/**
	 * Container of visual layers. Layers are stored in this list such that the layer with the lowest index is said to be the highest rank,
	 * meaning it is the topmost layer. The greater some layer's index, the lower its rank.
	 */
	private ArrayList<VisualLayer> visualLayers = new ArrayList<>();
	
	private ArrayList<NonVisualLayer> nonVisualLayers = new ArrayList<>();
	
	private Layer activeLayer;
	
	public final String name;
	
	/**
	 * Initializes a new artboard.
	 * 
	 * @param name — name of this artboard
	 * @param width — width of this artboard
	 * @param height — height of this artboard
	 */
	Artboard(String name , int width , int height) {
 		
		this(name , new IndexTexture() , width , height);

	}
	
	/**
	 * Initializes a new artboard.
	 * 
	 * @param name — name of this artboard
	 * @param texture — an index texture this artboard will use
	 * @param width — width of this artboard
	 * @param height — height of this artboard
	 */
	Artboard(String name , IndexTexture texture , int width , int height) {
		
		this.name = name;

		VertexBufferBuilder vertexBuffer = new VertexBufferBuilder(POSITION_2D|UV);
		vertexBuffer.size(width , height); 
		
		vao.initialize(vertexBuffer.attributes, STATIC_VAO, vertexBuffer.get());
		vao.drawAsElements(6, UINT);
		
		positions = new VAOPosition(vao , vertexBuffer.attribute(POSITION_2D));

		this.indexTexture = texture;
		if(!indexTexture.isInitialized()) indexTexture.initialize(width , height);
		
		render = new CSRender(vao);
				
	}
	
	/**
	 * Renders this artboard on the GPU.
	 * 
	 */
	@RenderThreadOnly public void draw() {
		
		vao.activate();
		vao.draw(); 
		vao.deactivate();
				
	}
	
	/**
	 * Moves this artboard by {@code x} along the x axis and {@code y} along the y axis.
	 * 
	 * @param x — amount to move this artboard horizontally
	 * @param y — amount to move this artboard vertically
	 */
	@RenderThreadOnly public void translate(int x , int y) {
		
		positions.translate(x, y);
		
	} 
	
	/**
	 * Applies a translation to this artboard with floating point parameters. Useful for subpixel positining.
	 * 
	 * @param x — amount to translate horizontally
	 * @param y — amount to translate vertically 
	 */
	@RenderThreadOnly public void translateByFloats(float x , float y) {
		
		positions.translate(x, y);
		
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
	public void worldToPixelIndices(float worldX , float worldY , int[] destination) {
		
		destination[0] = (int) (worldX - leftX());
		destination[1] = (int) (worldY - bottomY());
		
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
	 * Places the color given by the channel values in {@code values} into the next position in the palette texture and returns the indices
	 * to which it is stored, or, if {@code values} is already present in the palette, returns its indices.
	 * 
	 * @param values — array of channel values for a color to put in the palette.
	 * @return Array of indices into the palette where {@code values} is located.
	 */
	@RenderThreadOnly public LookupPixel putInPalette(PalettePixel pixel) {

		short[] indices = activeLayer().palette.putOrGetColors(pixel);
		return new IndexPixel(indices[0] , indices[1]);
		
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
	@RenderThreadOnly public void putColorInImage(int xIndex , int yIndex , int width , int height , final PalettePixel values) {

		if(activeLayer().hiding() || activeLayer().locked()) return;
		
		LookupPixel indexPixel = putInPalette(values);

		bulkPutInActiveLayer(indexPixel , xIndex , yIndex , width , height);

		if(isActiveLayerVisual) {
			
			int activeLayerIndex = visualLayers.indexOf(activeLayer());
			
			//canBulkWrite will be true if there is no layer above the current one modifying any of the pixels of the region.
			boolean canBulkWrite = !bulkIsUpperRankLayerModifying(activeLayerIndex , xIndex , yIndex , width , height);
			
			if(canBulkWrite) indexTexture.put(xIndex , yIndex , width , height , indexPixel);
			else for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
				
				boolean isUpperLayerModifying = isUpperRankLayerModifying(activeLayerIndex , xIndex + col , yIndex + row);
				if(!isUpperLayerModifying) indexTexture.put(xIndex + col , yIndex + row , 1 , 1 , indexPixel);
				
			}
			
		} else indexTexture.put(xIndex , yIndex , width , height , indexPixel);
				
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
	 * @param values — index pixel whose values provide a lookup into the palette
	 */
	@RenderThreadOnly public void putColorInImage(int bottomX , int bottomY , int width , int height , LookupPixel values) {
		
		putColorInImage(bottomX , bottomY , width , height , getColorPointedToBy(values));		
		
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
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			StackOrHeapAllocation allocation = Artboards.stackOrHeapBuffer(stack, width * height * IndexTexture.pixelSizeBytes);
		 	ByteBuffer bufferedContents = allocation.buffer();
		 	
		 	storePixelRegionInBuffer(values, bufferedContents, leftX , bottomY , width, height);
		 	
		 	indexTexture.put(leftX, bottomY, width, height, bufferedContents);		 	
		 	if(!allocation.stackAllocated()) memFree(bufferedContents);
		 	
		}
		
		storeLookupPixelsInLayer(values , leftX , bottomY , width , height);
		
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
		PalettePixel pixel
	) {
		
		short[] colorValueLookup = palette.putOrGetColors(pixel);
		IndexPixel indexPixel = new IndexPixel(colorValueLookup[0], colorValueLookup[1]);
		indexTexture.put(xIndex, yIndex, width, height, indexPixel);
		
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
	@RenderThreadOnly public void writeToIndexTexture(int xIndex , int yIndex , int width , int height , PalettePixel pixel) {

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
			
			storePixelRegionInBuffer(pixels, buffer, xIndex, yIndex, width, height);			
			indexTexture.put(xIndex, yIndex, width, height, buffer);
			
			if(!allocation.stackAllocated()) memFree(buffer);
						
		}
		
	}
	
	/**
	 * Gets an index pixel directly from the index texture using the given {@code xIndex} and {@code yIndex}.
	 * 
	 * @param xIndex — x index into texture of the pixel to get
	 * @param yIndex — y index into texture of the pixel to get
	 * @return Pixel located at the given coordinates.
	 */
	@RenderThreadOnly public IndexPixel getIndexPixelAtIndices(int xIndex , int yIndex) {
		
		return indexTexture.getPixelByIndex(xIndex, yIndex);
		
	}

	/**
	 * Fills the returned 3D array with values of the image texture, that is, the indices of the region given, where {@code xIndex} and 
	 * {@code yIndex} are the coordinates of the bottom left pixel of the region, and the region extends {@code width} and {@code height}
	 * pixels from the given coordinates. 
	 * 
	 * <b>NOTE:</b> This method must be called from the render thread.
	 * 
	 * @param xIndex — index texture x index of the bottom left corner of the region 
 	 * @param yIndex — index texture y index of the bottom left corner of the region
	 * @param width — number of pixels to extend from {@code xIndex}
	 * @param height — number of pixels to extend from {@code yIndex}
	 * @return 2D array containing all the pixels of the region specified.
	 */
	@RenderThreadOnly public IndexPixel[][] getRegionOfIndexPixels(int xIndex , int yIndex , int width , int height) {
		
		ByteBuffer texelBuffer = indexTexture.texelBufferWithReformat(width , height , xIndex , yIndex);		
		IndexPixel[][] region = new IndexPixel[height][width];
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) { 
			
			region[row][col] = indexTexture.getPixel(texelBuffer);
			
		}
		
		indexTexture.freeTexelBuffer(texelBuffer);
		
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
		
		IndexPixel indicesOfPixelAtIndex = indexTexture.getPixelByIndex(indexImageXIndex, indexImageYIndex);		
		return getColorFromIndicesOfPalette(indicesOfPixelAtIndex.xIndex , indicesOfPixelAtIndex.yIndex);
		
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
	 * @param channelValues — Color channel values for the palette pixel
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
		
		ByteBuffer texels = indexTexture.texelBuffer(width, height, x, y);
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
	@RenderThreadOnly public void replacePalettePixelAtIndex(int paletteXIndex , int paletteYIndex , PalettePixel replaceWithThis) {
		
		activeLayer().palette.put(paletteXIndex , paletteYIndex , replaceWithThis);
		
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
		
		return new Iterator<>() {

			Iterator<VisualLayer> source = visualLayers.iterator();
			
			@Override public boolean hasNext() {

				return source.hasNext();
				
			}

			@Override public VisualLayer next() {

				return source.next();
				
			}
			
		};
		
	}

	/**
	 * Creates and returns an iterator over the nonvisual layers of this artboard. Supports only 
	 * {@link java.util.Iterator#hasNext() hasNext} and {@link java.util.Iterator#next() next}.
	 * 
	 * @return Iterator over the nonvisual layers of this artboard.
	 */
	public Iterator<NonVisualLayer> nonVisualLayers () {
		
		return new Iterator<>() {

			Iterator<NonVisualLayer> source = nonVisualLayers.iterator();
			
			@Override public boolean hasNext() {

				return source.hasNext();
				
			}

			@Override public NonVisualLayer next() {

				return source.next();
				
			}
			
		};
		
	}
	
	/**
	 * Retrieves a region of pixels of the current layer. The region starts at {@code (xIndex , yIndex)} and extends {@code width} upward
	 * and {@code height} upward. 
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
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
			
			putInActiveLayer(source , xPosition + col , yPosition + row);
			
		}
		
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
		
		if(inferiorToThis != visualLayers.size() - 1) specify(
			stopAt > inferiorToThis , 
			stopAt + " must be greater than " + inferiorToThis
		);
		
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
	 * Moves the active layer to {@code newRank} rank.
	 * 
	 * @param newRank — new rank for the active layer
	 */
	public void moveVisualLayerRank(final int newRank) {
		
		if(!isActiveLayerVisual) return;
		
		moveVisualLayerRank(visualLayers.indexOf((VisualLayer)activeLayer()) , newRank);
		
	}
	
	/**
	 * Moves the layer of rank {@code moveThisRank} to {@code toThisRank} rank.
	 * 
	 * @param moveThisRank — rank of a layer to move
	 * @param toThisRank — new rank for the layer at {@code moveThisRank} 
	 */ 
	public void moveVisualLayerRank(final int moveThisRank , final int toThisRank) {
		 
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
	public VisualLayer getVisualLayer(final int rank) {
		
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
	public VisualLayer getVisualLayer(final VisualLayerPrototype prototype) {

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
		 * Divide the indices by 8 to get their region 
		 * The regions' oddness or evenness tells us whether to pick the darker or lighter color to put in the image. 
		 * However, if the region of the y index is even, we need to choose the opposite of what the color was. 
		 *
		 * Basically, the columns alternate between the dark and light colors. We want the rows to also alternate, giving a checkered 
		 * look. To do this, switch the starting color at the start of every row.
		 */ 

		int 
			xRegion = xIndex / IndexTexture.backgroundWidth ,
			yRegion = yIndex / IndexTexture.backgroundHeight;
		
		//odd exponent, use the darker background otherwise use the lighter one
		boolean darker = (xRegion & 1) == 1;
		//flip darker if the y region is even
		if((yRegion & 1) == 0) darker = !darker;
				
		return getColorPointedToBy(
			darker ? indexTexture.darkerTransparentBackground : indexTexture.lighterTransparentBackground
		);
		
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
	 * @return — width of this artboard.
	 */
	public int width() {
		
		return (int) (rightX() - leftX());
		
	}

	/**
	 * Returns the height of this artboard.
	 * 
	 * @return — height of this artboard.
	 */
	public int height() {
		
		return (int) (topY() - bottomY());
		
	}

	/**
	 * Returns the top Y coordinate (in world space) of this artboard.
	 * 
	 * @return — top Y coordinate of this artboard.
	 */
	public float topY() {
		
		return positions.topY();
		
	}

	/**
	 * Returns the bottom Y coordinate (in world space) of this artboard.
	 * 
	 * @return — bottom Y coordinate of this artboard.
	 */
	public float bottomY() {
		
		return positions.bottomY();
		
	}

	/**
	 * Returns the left X coordinate (in world space) of this artboard.
	 * 
	 * @return — left X coordinate of this artboard.
	 */
	public float leftX() {
		
		return positions.leftX();
		
	}

	/**
	 * Returns the right X coordinate (in world space) of this artboard.
	 * 
	 * @return — right X coordinate of this artboard.
	 */
	public float rightX() {
		
		return positions.rightX();
		
	}

	/**
	 * Returns the midpoint Y coordinate (in world space) of this artboard.
	 * 
	 * @return — midpoint X coordinate of this artboard.
	 */
	public float midX() {
		
		return leftX() + (width() / 2);
		
	}

	/**
	 * Returns the midpoint Y coordinate (in world space) of this artboard.
	 * 
	 * @return — midpoint Y coordinate of this artboard.
	 */
	public float midY() {
		
		return bottomY() + (height() / 2);
		
	}
	
	/**
	 * Returns this artboard's render object, which contains its VAO.
	 * 
	 * @return Render of this artboard.
	 */
	public CSRender render() { 
		
		return render;
		
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
		int[] x;
		while(iter.hasNext()) {
			
			x = iter.next();
			removePixel(x[0] , x[1]);
			
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
		
		int[] next;
		while(regionIter.hasNext()) {

			next = regionIter.next();
			LookupPixel pixel = region[next[1]][next[0]];
			if(pixel == null) continue;
			
			if(pixel instanceof LayerPixel asLayerPixel) active.put(asLayerPixel);
			else active.put(new LayerPixel(leftX + next[0] , bottomY + next[1] , pixel));
						
		}
		
	}
	
	private void storePixelRegionInBuffer(
		LookupPixel[][] region , 
		ByteBuffer destination , 
		int leftX , 
		int bottomY , 
		int width , 
		int height
	) {

	 	RegionIterator regionIter = Artboards.region(leftX, bottomY, width, height);
	 	RegionIterator valuesIter = Artboards.region(0 , 0 , width, height);
	 		
	 	if(isActiveLayerVisual) {

	 		int activeRank = getLayerRank((VisualLayer)activeLayer);
	 		
		 	while(regionIter.hasNext()) {
		 				 		
		 		int[] currentRegionIndices = regionIter.next();
		 		int[] currentLookup = valuesIter.next();
		 		
		 		LookupPixel x = region[currentLookup[1]][currentLookup[0]];		 		
		 		//if there is no layer pixel at the given indices, or a higher ranking layer modifies the same positions, retrieve whatever
		 		//is already in the texture.
		 		if(x == null || isUpperRankLayerModifying(activeRank , currentRegionIndices[0] , currentRegionIndices[1])) { 
		 			
		 			x = getIndexPixelAtIndices(currentRegionIndices[0], currentRegionIndices[1]);
		 			
		 		}
		 			 		
		 		destination.put(x.lookupX()).put(x.lookupY());
		 		
		 	}
		 	
	 	} else {

		 	while(regionIter.hasNext()) {
		 				 		
		 		int[] currentRegionIndices = regionIter.next();
		 		int[] currentLookup = valuesIter.next();
		 		
		 		LookupPixel x = region[currentLookup[1]][currentLookup[0]];	
		 		if(x == null) x = getIndexPixelAtIndices(currentRegionIndices[0], currentRegionIndices[1]);
		 			 		
		 		destination.put(x.lookupX()).put(x.lookupY());
		 	}
		 	
	 	}
	 	
		destination.flip();
	 	
	}
	
}