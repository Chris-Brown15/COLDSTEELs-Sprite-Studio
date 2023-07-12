package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import static org.lwjgl.util.lz4.LZ4.LZ4_compress_default;
import static org.lwjgl.util.lz4.LZ4.LZ4_decompress_safe;

import static cs.core.utils.CSUtils.require;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

import cs.core.utils.data.CSCHashMap;
import cs.csss.core.Engine;

/**
 * 
 * Layers are containers of modifications of the Artboard. This class is the base class for all layer types.
 * 
 * @author Chris Brown
 *
 */
public abstract class Layer {

	private static final CSCHashMap<String , String> reservedNames = new CSCHashMap<>(13);

	static final LinkedList<String> invalidNameContents = new LinkedList<>();
	
	static {
		
		invalidNameContents.add(",");
		reservedNames.put("___meta", "___meta");
		
	}
		
	/**
	 * Some words and characters are used internally and cannot be layer names. This method checks to make sure a potential layer name is 
	 * not one of the reserved words and does not contain reserved characters.
	 * 
	 * @param name — a potential layer name
	 * @return {@code true} if the potential name is a valid name.
	 */
	public static final boolean isValidName(String name) {

		boolean valid = !reservedNames.hasKey(name);;
		for(Iterator<String> iter = invalidNameContents.iterator() ; iter.hasNext() && valid ; ) valid = !name.contains(iter.next());
		return valid; 
		
	}
	
	public final int 
		width ,
		height
	;
	
	//if true, this layer cannot be modified.
	protected boolean 
		locked = false ,
		hiding = false
	;
	
	public final String name;
	protected final ArtboardPalette palette;
	
	protected volatile StaticLayerDataStore layerDataStore;
		
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
		
		layerDataStore = new StaticLayerDataStore(width , height);
		
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
	protected int mods() {
		
		return layerDataStore.mods();
		
	}
	
	public abstract  <T extends Layer> void copy(T otherLayer);
	
	public final ByteBuffer encode() {
		
		//the buffer sizes are based on the fact that layer pixels are 10 bytes, 8 for position, and 2 for lookup
		ByteBuffer 	
			buffer = memAlloc(mods() * 10) ,
			compress = memAlloc(mods() * 10)
		;

		forEachModification(px -> buffer.putInt(px.textureX).putInt(px.textureY).put((byte) px.lookupX).put((byte) px.lookupY));
		
		buffer.flip();
		
		int bytes = LZ4_compress_default(buffer , compress);
		compress.limit(bytes);

		memFree(buffer);
		
		return compress;
		
	}
	
	public final ByteBuffer decode(ByteBuffer compressed) {

		ByteBuffer decompressed = memAlloc(mods() * 10);
		
		int bytes = LZ4_decompress_safe(compressed , decompressed);

		require(bytes > 0);
		
		decompressed.limit(bytes);
		
		return decompressed;
				
	}
		
	public LayerMeta meta() {
		
		return new LayerMeta().bindLocked(locked).bindHiding(hiding).bindName(name);
		
	}
	
	public void compressToFile(final String filepathToLayerFolder) {
				
		Engine.THE_THREADS.async(() -> {

			if(!Files.exists(Paths.get(filepathToLayerFolder))) try {
			
				Files.createDirectories(Paths.get(filepathToLayerFolder));
						
			} catch (IOException e1) {
			
				e1.printStackTrace();
				throw new IllegalStateException();
				
			}
		
			ByteBuffer 
				buffer = memAlloc(width * height * 2) ,
				compress = memAlloc(width * height * 2)
			;
			
			layerDataStore.forEach(pixel -> buffer.put((byte)pixel.lookupX).put((byte)pixel.lookupY));
			
			buffer.flip();
			
			int bytes = LZ4_compress_default(buffer , compress);
			compress.limit(bytes);

			//I use channels because we can write byte buffers directly with them, including offheap ones
			try(FileChannel writer = new FileOutputStream(filepathToLayerFolder + "/" + name).getChannel()) {
								
				writer.write(compress);
				
			} catch (FileNotFoundException e) {} catch (IOException e) {
				
				e.printStackTrace();
				throw new IllegalStateException();
				
			} finally {
				
				memFree(buffer);
				memFree(compress);
				
			}
			
		});
		
	}
	
	public void decompressFromFile(String layerFilePath) {
		
		require(Files.exists(Paths.get(layerFilePath)));
		
		ByteBuffer 
			compressed = null ,
			decompressed = null 
		;
		
		//I use channels because we can write byte buffers directly with them, including offheap ones
		try(FileChannel reader = new FileInputStream(layerFilePath).getChannel()) {
			
			compressed = memAlloc((int)reader.size());
			decompressed = memAlloc(width * height * 2);
			
			reader.read(compressed);
			
			compressed.flip();
			
			int bytes = LZ4_decompress_safe(compressed , decompressed);

			require(bytes > 0);
			
			decompressed.limit(bytes);
			
		} catch (FileNotFoundException e) {} catch (IOException e) {

			e.printStackTrace();
			throw new IllegalStateException();
			
		} finally {
			
			memFree(compressed);
			memFree(decompressed);
			
		}
		
	}
	
}
