package cs.csss.artboard;

import static cs.csss.core.Logging.*;

import static cs.core.graphics.StandardRendererConstants.POSITION_2D;
import static cs.core.graphics.StandardRendererConstants.STATIC_VAO;
import static cs.core.graphics.StandardRendererConstants.UINT;
import static cs.core.graphics.StandardRendererConstants.UV;

import static cs.core.utils.CSUtils.specify;
import static cs.core.utils.CSUtils.require;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

import cs.core.graphics.CSOrthographicCamera;
import cs.core.graphics.CSRender;
import cs.core.graphics.CSVAO;
import cs.core.graphics.CSVAO.VertexBufferAccess;
import cs.core.graphics.utils.VertexBufferBuilder;
import cs.core.utils.ShutDown;
import cs.csss.artboard.ArtboardPalette.PalettePixel;
import cs.csss.artboard.ArtboardTexture.IndexPixel;

/**
 * Artboard contains the data needed to display and edit a graphic in CSSS and has methods to operate on pixels and artboards.
 * 
 * @author Chris Brown
 *
 */
public class Artboard implements ShutDown {
	
	private static final ArtboardShader theArtboardShader = new ArtboardShader();

	/**
	 * The shader for artboards stays the same over any artboard, so only one is created and used everywhere.
	 */
	public static void initializeTheArtboardShader() {
		
		theArtboardShader.initialize();

	}
	
	public static ArtboardShader theArtboardShader() {
		
		return theArtboardShader;
		
	}
	
	private final float[] positions;
	//used to track whether the abstract Layer instance activeLayer below is a nonvisual or visual layer.
	private boolean isActiveLayerVisual = true;
	
	private CSVAO vao = new CSVAO();
	private CSRender render;	

	private ArtboardTexture indexTexture = new ArtboardTexture();
	
	//Container of visual layers. Layers are stored in this list such that the layer with the lowest index is said to be the highest rank,
	//meaning it is the topmost layer. The greater some layer's index, the lower its rank.
	private LinkedList<VisualLayer> visualLayers = new LinkedList<>();
	
	private LinkedList<NonVisualLayer> nonVisualLayers = new LinkedList<>();
	
	private Layer activeLayer;
	
	public Artboard(int width , int height) {
 		
		VertexBufferBuilder vertexBuffer = new VertexBufferBuilder(POSITION_2D|UV);
		vertexBuffer.size(width , height); 
		vao.initialize(vertexBuffer.attributes, STATIC_VAO, vertexBuffer.get());
		vao.drawAsElements(6, UINT);
		
		positions = vertexBuffer.attribute(POSITION_2D);
		
		indexTexture.initialize(width , height);
		render = new CSRender(vao , theArtboardShader);
		
	}
	
	/**
	 * Renders this artboard on the GPU.
	 * 
	 * @param camera — a camera to use for transformations to position the object correctly
	 */
	public void draw(CSOrthographicCamera camera) {

		theArtboardShader.activate(this);
		render.draw();
				
	}
	
	/**
	 * Moves this artboard by {@code x} along the x axis and {@code y} along the y axis.
	 * 
	 * @param x — amount to move this artboard horizontally
	 * @param y — amount to move this artboard vertically
	 */
	public void translate(int x , int y) {
		
		vao.activate();
		
		try(VertexBufferAccess vertices = vao.new VertexBufferAccess()) {
			
			vertices.putFloat(0 , vertices.getFloat(0) + x).putFloat(1 << 2 , vertices.getFloat(1 << 2) + y);
			vertices.putFloat(4 << 2 , vertices.getFloat(4 << 2) + x).putFloat(5 << 2 , vertices.getFloat(5 << 2) + y);
			vertices.putFloat(8 << 2 , vertices.getFloat(8 << 2) + x).putFloat(9 << 2 , vertices.getFloat(9 << 2) + y);
			vertices.putFloat(12 << 2 , vertices.getFloat(12 << 2) + x).putFloat(13 << 2 ,vertices.getFloat(13 << 2) + y);
			
		}
		
		vao.deactivate();
		
		positions[0] += x;
		positions[2] += x;
		positions[4] += x;
		positions[6] += x;		
		positions[1] += y;
		positions[3] += y;
		positions[5] += y;
		positions[7] += y;
		
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
	 * @param cursorWorldCoords — cursor world coords
	 * @return Array whose contents are indices into the image texture of the pixel the cursor is hovering.
	 */
	public int[] cursorToPixelIndex(float[] cursorWorldCoords) {
		
		return new int[] {
			(int) (cursorWorldCoords[0] - leftX()) ,
			(int) (cursorWorldCoords[1] - bottomY()) ,
		};
		
	}
	
	/**
	 * Places the color given by the channel values in {@code values} into the next position in the palette texture and returns the indices
	 * to which it is stored, or, if {@code values} is already present in the palette, returns its indices.
	 * 
	 * @param values — array of channel values for a color to put in the palette.
	 * @return Array of indices into the palette where {@code values} is located.
	 */
	public short[] putInPalette(PalettePixel pixel) {
		
		return activeLayer.palette.putOrGetColors(pixel);
		
	}
	
	/**
	 * Puts a single color at a region of the image. This method does logic for layers as well, only writing to the artboard when logic to
	 * do so permits. 
	 * <br><br>
	 * A pixel is only updated if 
	 * <ol>
	 * <li> the current layer is the layer of the highest priority, or </li>
	 * <li> no layers of a higher priority than the current one also modify the given pixel. </li>
	 * </ol>
	 * 
	 * @param xIndex — x index of the bottom left corner of the region to put the color
	 * @param yIndex — y index of the bottom left corner of the region to put the color
	 * @param width — number of pixels to extend rightward from {@code xIndex} to put the color in
	 * @param height — number of pixels to extend upward from {@code yIndex} to put the color in
	 * @param values — channel values to put in each pixel of the region.
	 */
	public void putColorInImage(int xIndex , int yIndex , int width , int height , final PalettePixel values) {

		if(activeLayer.hiding() || activeLayer.locked()) return;
		
		short[] colorValueLookup = putInPalette(values);
		IndexPixel indexPixel = indexTexture.new IndexPixel(colorValueLookup[0] , colorValueLookup[1]);

		bulkPutInActiveLayer(indexPixel , xIndex , yIndex , width , height);

		if(isActiveLayerVisual) {
			
			int activeLayerIndex = visualLayers.indexOf(activeLayer);
			
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
	public void writeToIndexTexture(final int xIndex , final int yIndex , final int width , final int height , final PalettePixel pixel) {

		writeToIndexTexture(xIndex , yIndex , width , height , activeLayersPalette() , pixel);
		
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
	void writeToIndexTexture(
		final int xIndex , 
		final int yIndex , 
		final int width , 
		final int height , 
		ArtboardPalette palette , 
		final PalettePixel pixel
	) {
		
		short[] colorValueLookup = palette.putOrGetColors(pixel);
		IndexPixel indexPixel = indexTexture.new IndexPixel(colorValueLookup[0], colorValueLookup[1]);
		indexTexture.put(xIndex, yIndex, width, height, indexPixel);
		
	}
	
	/**
	 * Gets an index pixel directly from the index texture using the given {@code xIndex} and {@code yIndex}.
	 * 
	 * @param xIndex — x index into texture of the pixel to get
	 * @param yIndex — y index into texture of the pixel to get
	 * @return Pixel located at the given coordinates.
	 */
	public IndexPixel getIndexPixelAtIndices(int xIndex , int yIndex) {
		
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
	public IndexPixel[][] getRegionOfIndexPixels(int xIndex , int yIndex , int width , int height) {
		
		ByteBuffer texelBuffer = indexTexture.texelBuffer(width , height , xIndex , yIndex);		
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
	public PalettePixel getColorFromIndicesOfPalette(int paletteXIndex , int paletteYIndex) {
		
		paletteXIndex = Byte.toUnsignedInt((byte)paletteXIndex);
		paletteYIndex = Byte.toUnsignedInt((byte)paletteYIndex);

		return activeLayer.palette.getColorByIndices(paletteXIndex , paletteYIndex);
		
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
	public PalettePixel getColorFromIndicesOfPalette(ByteBuffer texelData , int paletteXIndex , int paletteYIndex) {

		paletteXIndex = Byte.toUnsignedInt((byte)paletteXIndex);
		paletteYIndex = Byte.toUnsignedInt((byte)paletteYIndex);
		
		return activeLayer.palette.getColorByIndicesFromTexelBuffer(texelData , paletteXIndex , paletteYIndex);
		
	}
	
	/**
	 * Gets a color in the palette from the values of {@code pixel}.
	 * 
	 * @param pixel — an index pixel from the index texture
	 * @return Color value stored at the indices of the palette referenced by {@code pixel}.
	 */
	public PalettePixel getColorPointedToByIndexPixel(IndexPixel pixel) {
		
		return getColorFromIndicesOfPalette(pixel.xIndex, pixel.yIndex);
		
	}
	
	/**
	 * Gets the index pixel located at {@code (indexImageXIndex , indexImageYIndex)}, and uses it to get the color in the color palette
	 * pointed to by the pixel's {@code xIndex} and {@code yIndex}. 
	 * 
	 * @param indexImageXIndex — x index of a pixel in the index image
	 * @param indexImageYIndex — y index of a pixel in the index image
	 * @return Color in the palette who the index pixel at {@code (indexImageXIndex , indexImageYIndex)} points to.
	 */
	public PalettePixel getColorPointedToByIndexPixel(int indexImageXIndex , int indexImageYIndex) {
		
		IndexPixel indicesOfPixelAtIndex = indexTexture.getPixelByIndex(indexImageXIndex, indexImageYIndex);		
		return getColorFromIndicesOfPalette(indicesOfPixelAtIndex.xIndex , indicesOfPixelAtIndex.yIndex);
		
	}

	
	
	/**
	 * Gets the index pixel located at {@code (indexTextureXIndex , indexTextureYIndex)}, and uses it to get the color in the color palette
	 * pointed to by the pixel's {@code xIndex} and {@code yIndex}.
	 * 
	 * @param indexTexelBuffer — container of texel data for the index texture
	 * @param paletteTexelBuffer — container of texel data for the palette texture
	 * @param indexTextureXIndex — x index of a pixel in the index image
	 * @param indexTextureYIndex — y index of a pixel in the index image
	 * @return Color in the palette who the index pixel at {@code (indexImageXIndex , indexImageYIndex)} points to.
	 */
	public PalettePixel getColorPointedToByIndexPixel(
		ByteBuffer indexTexelBuffer , 
		ByteBuffer paletteTexelBuffer , 
		int indexTextureXIndex , 
		int indexTextureYIndex
	) {
		
		IndexPixel indices = indexTexture.getPixelByIndex(indexTexelBuffer , indexTextureXIndex , indexTextureYIndex);
		return getColorFromIndicesOfPalette(paletteTexelBuffer , indices.xIndex , indices.yIndex);
		
	}
	
	/**
	 * Constructs a {@code PalettePixel} from the settings of this artboard's palette texture, returning the result. The resulting pixel is
	 * <b>not</b> stored in this artboard's palette. 
	 * 
	 * @param channelValues — Color channel values for the palette pixel
	 * @return {@code PalettePixel} resulting from creating a pixel whose contents are {@code channelValues}.
	 */
	public PalettePixel createPalettePixel(final byte[] channelValues) {
		
		specify(
			channelValues.length == activeLayer.pixelSizeBytes() , 
			channelValues.length + " is not a valid number of channel values. " + activeLayer.pixelSizeBytes() + " expected."
		);
		
		return activeLayer.palette.new PalettePixel(channelValues);
				
	}
	
	/**
	 * Returns a read-only {@code ByteBuffer} containing the texel data of the index texture.
	 *  
	 * @return Read-only {@code ByteBuffer} containing the texel data of the index texture.
	 */
	public ByteBuffer indexTextureTexelBuffer() {
		
		ByteBuffer 
			texelBuffer = indexTexture.allTexelData() ,
			texelBufferReadOnly = texelBuffer.asReadOnlyBuffer()
		;
				
		return texelBufferReadOnly;
		
	}
	
	/**
	 * Returns a read-only {@code ByteBuffer} containing the texel data of the palette texture.
	 * 
	 * @return Read-only {@code ByteBuffer} containing the texel data of the palette texture.
	 */
	public ByteBuffer paletteTexelBuffer() {
		
		return activeLayer.palette.texelData();
		
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
	public void replacePalettePixelAtIndex(final int paletteXIndex , final int paletteYIndex , PalettePixel replaceWithThis) {
		
		activeLayer.palette.put(paletteXIndex , paletteYIndex , replaceWithThis);
		
	}
	
	/**
	 * Creates a visual layer for this artboard from the given prototype.
	 * 
	 * @param prototype — prototype layer
	 */
	public void addVisualLayer(VisualLayer layer) {
		
		visualLayers.add(layer);
		if(activeLayer == null) setActiveLayer(layer);
				
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

	public boolean isActiveLayer(Layer layer) {
		
		return this.activeLayer == layer;
		
	}
	
	public void forEachVisualLayer(Consumer<VisualLayer> callback) {
		
		visualLayers.forEach(callback);
		
	}
	
	public void forEachNonVisualLayer(Consumer<NonVisualLayer> callback) {
		
		nonVisualLayers.forEach(callback);
		
	}
	
	/**
	 * Stores the values of the given index pixel in the active layer at the given position. 
	 * 
	 * @param source — a pixel to copy
	 * @param xPosition — x position of the pixel to copy
	 * @param yPosition — y position of the pixel to copy
	 */
	public void putInActiveLayer(final IndexPixel source , final int xPosition , final int yPosition) {
	
		if(activeLayer == null) return;
		
		LayerPixel pixel = new LayerPixel(xPosition , yPosition , source.xIndex , source.yIndex);		
		activeLayer.put(pixel);
		
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
	public void bulkPutInActiveLayer(
		final IndexPixel source , 
		final int xPosition , 
		final int yPosition , 
		final int width , 
		final int height
	) {
		
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
	 * @return {@code true} if the given position is modified by any layer of a lower rank to the one located at {@code inferiorToThis} up 
	 * 		   to and including the end of the list.
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
	 * @return {@code true} if the given position is modified by any layer of a lower rank to the one located at {@code inferiorToThis} up 
	 * 		   to and including the layer at {@code inferiorToThis}.
	 */
	public boolean isLowerRankLayerModifying(int inferiorToThis , int toRank , int xIndex , int yIndex) {
		
		specify(inferiorToThis > toRank , toRank + " must be greater than " + inferiorToThis + ".");
		
		
		if(!isActiveLayerVisual) return false;

		for(int i = inferiorToThis + 1 ; i <= toRank ; i++) if(visualLayers.get(i).isModifying(xIndex, yIndex)) return true;		
		return false;
		
	}
	
	/**
	 * Returns the closest layer to {@code inferiorToThis} who modifies the pixel at {@code (xIndex , yIndex)}.
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
		
		moveVisualLayerRank(visualLayers.indexOf((VisualLayer)activeLayer) , newRank);
		
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
	public void toggleHideLayer(VisualLayer layer) {
		
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
	 * Gets the visual layer whose prototype is {@code prototype}.
	 * 
	 * @param prototype — prototype of a visual layer from which an instance is derived and contained within this artboard 
	 * @return Instance of visual layer derived from {@code prototype}.
	 */
	public VisualLayer getVisualLayer(final VisualLayerPrototype prototype) {

		for(int i = 0 ; i < visualLayers.size() ; i++) if(visualLayers.get(i).isInstanceOfPrototype(prototype)) { 
			
			return visualLayers.get(i);
			
		}
		
		throw new IllegalStateException("Layer not found!");
		
	}
	
	/**
	 * Removes the abstract layer {@code layer} from whatever list contains its implementation type.
	 * 
	 * @param layer — a layer to remove
	 */
	public void removeLayer(Layer layer) {
		
		if(layer instanceof VisualLayer) visualLayers.remove(layer);
		else nonVisualLayers.remove(layer);
		
	}
	
	/**
	 * Hides all layers.
	 */
	public void hideAllVisualLayers() {
		
		setToCheckeredBackground();
		forEachVisualLayer(layer -> layer.hiding(true));
		
	}
	
	/**
	 * Shows all visual layers.
	 * 
	 */
	public void showAllVisualLayers() {
		
		Iterator<VisualLayer> iter = visualLayers.descendingIterator();
		while(iter.hasNext()) iter.next().show(this);
		
	}
	
	/**
	 * Determines the background color to display at {@code (xIndex , yIndex)}. Will return one of the transparent background checker 
	 * colors.
	 * 
	 * @param xIndex — x index of a pixel whose background is being checked
	 * @param yIndex — y index of a pixel whose background is being checked
	 * @return Palette pixel containing the values of a transparent background checker color.
	 */
	public PalettePixel getBackgroundColor(int xIndex , int yIndex) {

		/*
		 * Determine what quadrant of the checkered background the pixel at the given indices is in.
		 * Divide the indices by 8 to get their region 
		 * The regions' oddness or evenness tells us whether to pick the darker or lighter color to put in the image. 
		 * However, if the region of the y index is even, we need to choose the opposite of what the color was. 
		 *
		 * Basically, the columns alternate between the dark and light colors. We want the rows to also alternate, giving a checkered look.
		 * To do this, switch the starting color at the start of every row.
		 *
		 */ 

		int 
			xRegion = xIndex / ArtboardTexture.backgroundCheckerWidth ,
			yRegion = yIndex / ArtboardTexture.backgroundCheckerHeight
		;
		
		//odd exponent, use the darker background otherwise use the lighter one
		boolean darker = (xRegion & 1) == 1;
		//flip darker if the y region is even
		if((yRegion & 1) == 0) darker = !darker;
				
		return getColorPointedToByIndexPixel(
			darker ? indexTexture.darkerTransparentBackground : indexTexture.lighterTransparentBackground
		);
		
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
	public PalettePixel getHighestRankingLayerModification(int xIndex , int yIndex) {
		
		LayerPixel mod;

		if(isActiveLayerVisual) {
			
			for(VisualLayer x : visualLayers) {
				
				if(x.hiding) continue;
				
				if((mod = x.get(xIndex, yIndex)) != null) return getColorFromIndicesOfPalette(mod.lookupX, mod.lookupY);
				
			}
		
		} else if(activeLayer.hiding() && (mod = activeLayer.get(xIndex, yIndex)) != null) { 
			
			return getColorFromIndicesOfPalette(mod.lookupX, mod.lookupY);
			
		}
		
		return null;
		
	}
	
	/**
	 * Overwrites the entire artboard image to the transparent checkered background. No layers are modified. This method should only be
	 * called in conjuction with layer show methods.
	 */
	public void setToCheckeredBackground() {
		
		indexTexture.setCheckerBackground();
		
	}
	
	public void writeToFile(int artboardID , final String projectFolderPath) {
		
		createDirectory(artboardID , projectFolderPath);
		
		ArtboardMeta meta = new ArtboardMeta()
			.bindWidth(indexTexture.width)
			.bindHeight(indexTexture.height)
			.bindPosition(positions)
		;
		
		try(FileOutputStream writer = new FileOutputStream(
			projectFolderPath + File.separator + artboardID + File.separator + "___meta"
		)) {
			
			meta.write(writer);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
			
		visualLayers.forEach(layer -> writeLayer(layer , artboardID , projectFolderPath));
		nonVisualLayers.forEach(layer -> writeLayer(layer , artboardID , projectFolderPath));		
		
	}

	private void createDirectory(int artboardID , final String projectFolderPath) {

		Path artboardFolderPath = Paths.get(projectFolderPath + File.separator + artboardID);
		
		if(!Files.exists(artboardFolderPath)) {
		
			sysDebug("Creating Artboard directory.");
			
			try {
				
				Files.createDirectories(artboardFolderPath);				
				sysDebug("Created Artboard directory.");
				
			} catch (IOException e) {}

		}
			
	}
	
	private void writeLayer(Layer layer , int artboardID , String projectFolderPath) {

		try(
			FileOutputStream writer = new FileOutputStream(projectFolderPath + File.separator + artboardID + File.separator + layer.name);
			FileChannel channel = writer.getChannel()
		) {
			
			ByteBuffer compressed = layer.encode();
			layer.meta().write(writer);
			channel.write(compressed);
			memFree(compressed);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public int numberVisualLayers() {
		
		return visualLayers.size();
		
	}
	
	public int activeLayerChannelsPerPixel() {
		
		return activeLayer.pixelSizeBytes();
		
	}
	
	public int width() {
		
		return (int) (rightX() - leftX());
		
	}

	public int height() {
		
		return (int) (topY() - bottomY());
		
	}
	
	private float topY() {
		
		return positions[1];
		
	}
	
	private float bottomY() {
		
		return positions[3];
		
	}
	
	private float leftX() {
		
		return positions[4];
		
	}
	
	public float rightX() {
		
		return positions[0];
		
	}
	
	public float midX() {
		
		return leftX() + (width() / 2);
		
	}

	public float midY() {
		
		return bottomY() + (height() / 2);
		
	}
	
	public CSRender render() { 
		
		return render;
		
	}
	
	public Layer activeLayer() {
		
		return activeLayer;
		
	}
	
	public boolean isActiveLayerVisual() {
		
		return activeLayer instanceof VisualLayer;
		
	}
			
	ArtboardTexture indexTexture() {
		
		return indexTexture;
		
	}
	
	ArtboardPalette activeLayersPalette() {
		
		return activeLayer.palette;
		
	}
	
	ArtboardPalette visualPalette() {
		
		return visualLayers.get(0).palette;
		
	}

	@Override public void shutDown() {
		
		
		
	}

	@Override public boolean isFreed() {

		return false;
		
	}
		
}