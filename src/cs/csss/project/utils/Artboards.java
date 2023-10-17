package cs.csss.project.utils;

import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.util.function.Consumer;

import org.lwjgl.system.MemoryStack;

import cs.csss.project.Artboard;
import cs.csss.utils.ByteBufferUtils;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Container for Artboard utilities.
 */
public final class Artboards {

	/**
	 * Invokes a callback for each pixel of the region specified by starting at {@code (x , y)} and extending {@code width} right and 
	 * {@code height} up.
	 * 
	 * @param x — left coordinate of the region
	 * @param y — bottom coordinate of the region
	 * @param width — width in pixels of the region
	 * @param height — height in pixels of the region
	 * @param callback — code to invoke on each pixel of the region
	 */	
	public static void forRegion(int x , int y , int width , int height , Consumer<int[]> callback) {
		
		RegionIterator iter = region(x , y , width , height);
		while(iter.hasNext()) callback.accept(iter.next());
		
	}
	
	/**
	 * Returns an iterator over a region for simple use cases.
	 * 
	 * @param x — left coordinate of the region
	 * @param y — bottom coordinate of the region
	 * @param width — width in pixels of the region
	 * @param height — height in pixels of the region
	 * @return {@code Iterator} over the specified region of {@code artboard}.
	 */
	public static RegionIterator region(int x , int y , int width , int height) {
		
		return new RegionIterator(x , y , width , height);
		
	}
	
	/**
	 * Receives some world coordinates and dimensions and ensures the returned container's parameters are valid for the given artboard.
	 * 
	 * @param artboard — an artboard to measure against
	 * @param leftX — left x coordinate of the region
	 * @param bottomY — bottom y coordinate of the region
	 * @param width — width of the region
	 * @param height — height of the region
	 * @return Container for corrected results.
	 */
	public static CorrectedResult worldCoordinatesToCorrectArtboardCoordinates(
		Artboard artboard , 
		int leftX , 
		int bottomY , 
		int width , 
		int height
	) {
		
		int[] asArtboardCoordinates = artboard.worldToPixelIndices(leftX, bottomY);
		return ByteBufferUtils.correctifyIndices(artboard, asArtboardCoordinates[0] , asArtboardCoordinates[1] , width, height);
				
	}

	/**
	 * Returns an allocation which will be on the given stack frame if the stack has enough memory to make the allocation of {@code size}
	 * bytes. If the stack does not have enough memory, the buffer is allocated from the heap.
	 * 
	 * <p>
	 * 	It may be wise to allocate all other stack memory before invoking this method because potentially all the stack memory is used on the
	 * 	allocation.
	 * </p>
	 * 
	 * @param stackFrame — already-pushed stack frame
	 * @param size — number of bytes to allocate
	 * @return Container of the allocated buffer and a {@code boolean} notating whether the buffer is stack allocated. If the boolean is 
	 * 		   {@code false}, the buffer must be freed. 
	 */
	public static StackOrHeapAllocation stackOrHeapBuffer(MemoryStack stackFrame , int size) { 
		
	 	boolean isStackAllocated = stackFrame.getPointer() > size;		
	 	return new StackOrHeapAllocation(isStackAllocated ? stackFrame.malloc(size) : memAlloc(size) , isStackAllocated);
	
	}
	
	private Artboards() {}
	
}
