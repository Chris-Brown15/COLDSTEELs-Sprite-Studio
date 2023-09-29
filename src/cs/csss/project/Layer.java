package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import static org.lwjgl.util.lz4.LZ4.LZ4_compress_default;
import static org.lwjgl.util.lz4.LZ4.LZ4_decompress_safe;

import static cs.core.utils.CSUtils.require;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

import cs.csss.annotation.FreeAfterUse;
import cs.csss.annotation.Invalidated;

/**
 * 
 * Layers are containers of modifications of the Artboard. This class is the base class for all layer types.
 * 
 * <p>
 * 	All layers in Sprite Studio are purely-CPU, Java objects. They do not interact with native memory or VRAM at all. One extender of 
 * 	{@code Layer} can be 'active' at a time. When any attempt to modify the current artboard is made, the active layer stores the 
 * 	modification. From there, if by the semantics of the layer, a visual change of the artboard should be made, the textures of the artboard
 * 	are updated.
 * </p>
 * 
 * @author Chris Brown
 *
 */
public abstract class Layer {

	static final LinkedList<String> invalidNameContents = new LinkedList<>();
	
	/**
	 * Writes the contents of {@code source} into {@code destination}. Each pixel will occupy two bytes in the destination and for any 
	 * pixels that are {@code null}, the values {@code (0 , 0)} are written.  
	 * 
	 * @param source — a 2D array of layer pixels
	 * @param destination — a destination for the layer pixels
	 */
	public static void toByteBuffer(LayerPixel[][] source , ByteBuffer destination) {
		
		int destPosition = destination.position();
		
		for(LayerPixel[] row : source) for(LayerPixel x : row) {
			
			if(x != null) destination.put((byte)x.lookupX).put((byte)x.lookupY);
			else destination.put((byte)0).put((byte)0);
			
		}
	
		destination.position(destPosition);
		
	}

	/**
	 * Writes the contents of {@code source} to a newly allocated {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer}, 
	 * returning the byte buffer after writing. 
	 * 
	 * @param source — a 2D array of layer pixels
	 * @return {@code @FreeAfterUse ByteBuffer} destination for layer pixel data.
	 */
	public static @FreeAfterUse ByteBuffer toByteBuffer(LayerPixel[][] source) {
		
		ByteBuffer contents = memAlloc(source[0].length * source.length * IndexTexture.pixelSizeBytes);
		toByteBuffer(source , contents);
		return contents;
				
	}
	
	public final int 
		width ,
		height;
	
	//if true, this layer cannot be modified.
	protected boolean 
		locked = false ,
		hiding = false;
	
	public final String name;
	protected final ArtboardPalette palette;
	
	protected volatile LayerDataStore layerDataStore;
		
	/**
	 * Constructs a layer with the given name, palette, width and height
	 * 
	 * @param name — name of this layer
	 * @param palette — palette pixels of this layer draw from
	 * @param width — width of this layer
	 * @param height — height of this layer
	 */
	public Layer(final String name , ArtboardPalette palette , final int width , final int height) {
		
		this.width = width;
		this.height = height;
		this.name = name;
		this.palette = palette;
		
		layerDataStore = new AtomicLayerDataStore(width , height);
		
	}
	
	/**
	 * Puts the given pixel in this layer.
	 * 
	 * @param pixel — a pixel to add
	 */
	public void put(LayerPixel pixel) {
		
		if(locked) return;		
		layerDataStore.put(pixel);		

	}
	
	/**
	 * Removes the pixel at the given indices from this layer. The layer must contain a modification at the given indices. 
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 */
	public void remove(int xIndex , int yIndex) {
		
		specify(containsModificationTo(xIndex, yIndex) , "This layer does not modify (" + xIndex + ", " + yIndex + ")");
		
		layerDataStore.remove(xIndex, yIndex);
		
	}
	
	/**
	 * Removes the region whose bottom left coordinate is {@code (xIndex , yIndex)} and whose dimensions are {@code width} and 
	 * {@code height}.
	 * 
	 * @param xIndex — x index of the bottom left corner of the region
	 * @param yIndex — y index of the bottom left corner of the region
	 * @param width — width of the region
	 * @param height — height of the region
	 */
	public void remove(int xIndex , int yIndex , int width , int height) {
		
		layerDataStore.remove(xIndex, yIndex, width, height);
		
	}
	
	/**
	 * Returns whether this layer's modification at {@code (xIndex , yIndex)} is actively visible in the artboard.
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return {@code true} if this layer modifies the pixel at {@code (xIndex , yIndex)}.
	 */
	public boolean isModifying(int xIndex , int yIndex) {
		
		return !hiding && layerDataStore.modifiesAtIndex(xIndex, yIndex);
		
	}
		
	/**
	 * Returns whether this layer contains the modification at {@code (xIndex , yIndex)}.
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return {@code true} if this layer contains a modification at {@code (xIndex , yIndex)}.
	 */
	public final boolean containsModificationTo(int xIndex , int yIndex) {
		
		return layerDataStore.modifiesAtIndex(xIndex, yIndex);
		
	}
	
	/**
	 * Returns the pixel this layer modifies at {@code (xIndex , yIndex)}, or {@code null} if this layer does not modify that pixel.
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @return Pixel at {@code (xIndex , yIndex)}, or {@code null} if this layer does not modify that pixel.
	 */
	public LayerPixel get(int xIndex , int yIndex) {

		specify(xIndex >= 0 , xIndex + " is out of bounds.");
		specify(xIndex < width , xIndex + " is out of bounds."); 
		specify(yIndex >= 0 , xIndex + " is out of bounds.");
		specify(yIndex < height , xIndex + " is out of bounds.");
		return layerDataStore.get(xIndex, yIndex);
		
	}

	/**
	 * Gets a region of layer pixels, where {@code (xIndex , yIndex)} is the bottom left pixel coordinate, and the region extends 
	 * {@code width} pixels 'rightward' and {@code height} pixels 'upward.' 
	 * 
	 * @param xIndex — x index of a pixel
	 * @param yIndex — y index of a pixel
	 * @param width — width of the region
	 * @param height — height of the region
	 * @return 2D array of layer pixels representing this layer's contents at the specified region.
	 */
	public LayerPixel[][] get(int xIndex , int yIndex , int width , int height) {
		
		return layerDataStore.get(xIndex, yIndex, width, height);
		
	}
	
	/**
	 * Locks this layer, preventing {@linkplain Layer#put(LayerPixel) put()} from doing anything.
	 */
	public void lock() {
		
		locked = true;
		
	}

	/**
	 * Unlocks this layer, allowing {@linkplain Layer#put(LayerPixel) put()} to operate.
	 */
	public void unlock() {
		
		locked = false;
		
	}
	
	/**
	 * Switches the lock from {@code false} to {@code true} or {@code true} to {@code false}.
	 */
	public void toggleLock() {
		
		locked = !locked;
		
	}
	
	/**
	 * Sets the locked value of this layer directly, doing nothing else.
	 * 
	 * @param locked — whether this layer is locked
	 */
	void setLock(boolean locked) {
		
		this.locked = locked;
		
	}
	
	/**
	 * Sets the hiding value of this layer directly, doing nothing else.
	 * 
	 * @param hiding — whether this layer is hiding
	 */
	void setHiding(boolean hiding) {
		
		this.hiding = hiding;
		
	}
	
	/**
	 * Hides this layer, making all modifications it makes which are currently visible invisible. 
	 * 
	 * @param artboard — the owning artboard
	 */
	public abstract void hide(Artboard artboard);
	
	/**
	 * Shows this layer, making any modifications that would be visible visible.
	 * 
	 * @param artboard — the owning artboard
	 */
	public abstract void show(Artboard artboard);

	/**
	 * Iterates over each modification of this layer, accepting {@code callback} on each.
	 * 
	 * @param callback — function to invoke for each modified pixel.
	 */
	public void forEachModification(Consumer<LayerPixel> callback) {
		
		layerDataStore.forEach(callback);
		
	}
	
	/**
	 * Abstract method for getting the size of individual elements of the layer. If this layer is a nonvisual layer, this method will return
	 * the size in bytes of elements of the layer. If the layer is a visual layer, this will return the number of channels of the pixel.
	 * 
	 * @return Abstract size of a pixel of this layer.
	 */
	public abstract int pixelSizeBytes();
	
	/**
	 * Returns {@code true} if this layer is currently hidden.
	 * 
	 * @return {@code true} if this layer is currently hidden.
	 */
	public abstract boolean hiding();

	/**
	 * Returns the palette used by this layer
	 * 
	 * @return This layer's palette.
	 */
	public final ArtboardPalette palette() {
		
		return palette;
		
	}

	/**
	 * Returns whether this layer is locked.
	 * 
	 * @return Whether this layer is locked.
	 */
	public boolean locked() {
		
		return locked;
		
	}
	
	/**
	 * Returns the number of positions this layer modifies. 
	 * 
	 * @return The number of positions this layer modifies.
	 */
	public int mods() {
		
		return layerDataStore.mods();
		
	}
	
	/**
	 * Copies this layer into {@code otherLayer}.
	 *  
	 * @param <T> — type of another layer
	 * @param otherLayer — a destination for copying
	 */
	public abstract  <T extends Layer> void copy(T otherLayer);
	
	/**
	 * Converts the contents of this layer to a {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer}. Unlike 
	 * {@link Layer#toByteBuffer(LayerPixel[][])}, this method copies both the positions in the layer of the layer pixels, and the lookup
	 * values of said pixels.  
	 * 
	 * @return {@code @FreeAfterUse ByteBuffer} containing the layer data of this layer.
	 */
	public final @FreeAfterUse ByteBuffer toByteBuffer() {
		
		ByteBuffer buffer = memAlloc(mods() * 10);
		forEachModification(px -> buffer.putInt(px.textureX).putInt(px.textureY).put((byte) px.lookupX).put((byte) px.lookupY));
		buffer.flip();
		return buffer;
		
	}
	
	/**
	 * Converts a region of this layer to a {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer}. The region starts
	 * at the given {@code (startingX , startingY)} position within the layer, and writes a region of {@code width} width and {@code height}
	 * height to a byte buffer which is returned.
	 * 
	 * @param startingX — starting x coordinate for copy  
	 * @param startingY — starting y coordinate for copy
	 * @param width — width of the copied region
	 * @param height — height of the copied region
	 * @return {@code @FreeAfterUse ByteBuffer} containing the specified region of layer data of this layer.
	 */
	public final @FreeAfterUse ByteBuffer toByteBuffer(int startingX , int startingY , int width , int height) {
		
		Objects.checkIndex(startingX, this.width);
		Objects.checkIndex(startingY, this.height);
		Objects.checkFromIndexSize(startingX, width, this.width);
		Objects.checkFromIndexSize(startingY, height, this.height);
		
		ByteBuffer buffer = memAlloc(width * height * IndexTexture.pixelSizeBytes);
		LayerPixel[][] contents = layerDataStore.get(startingX, startingY, width, height);
		toByteBuffer(contents , buffer);
		
		return buffer.rewind();
		
	}
	
	/**
	 * Converts the entire contents of this layer to the returned {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer}
	 * and encodes that buffer using the LZ4 library.
	 * 
	 * @return Compressed contents of this layer.
	 */
	public final @FreeAfterUse ByteBuffer encode() {
		
		//the buffer sizes are based on the fact that layer pixels are 10 bytes, 8 for position, and 2 for lookup
		ByteBuffer 	
			buffer = toByteBuffer() ,
			compress = memAlloc(mods() * 10);

		int bytes = LZ4_compress_default(buffer , compress);
		compress.limit(bytes);
		
		memFree(buffer);
		
		return compress;
		
	}
	
	/**
	 * Given a byte buffer that was prevously encoded using {@link Layer#encode() encode()}, this method decodes it and returns the result.
	 * The buffer passed to this method is {@link cs.csss.annotation.Invalidated @Invalidated}, do not use it again.
	 * 
	 * @param compressed {@code @Invalidated ByteBuffer} used to decode the contents of a layer.
	 * @return {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} {@code ByteBuffer} containing the decoded contents of this layer.
	 */
	public final @FreeAfterUse ByteBuffer decode(@Invalidated ByteBuffer compressed) {

		ByteBuffer decompressed = memAlloc((width * height) * 10);
		
		int bytes = LZ4_decompress_safe(compressed , decompressed);
				
		require(bytes > 0);
		
		decompressed.limit(bytes);
				
		return decompressed;
				
	}
		
}
