package cs.csss.project.utils;

import static org.lwjgl.system.MemoryUtil.memCalloc;

import java.util.Objects;
import java.util.function.Consumer;

import org.lwjgl.system.MemoryStack;

import cs.csss.engine.Pixel;
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
	public static void forRegion(int x , int y , int width , int height , Consumer<RegionPosition> callback) {
		
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
	 * Returns a {@link RegionIterator} whose positions are based on {@code correct}. {@code correct} can be <code>null</code>, in which case the 
	 * given iterator does not iterate over anything.
	 * 
	 * @param correct container for correct region values
	 * @return Iterator over a region.
	 */
	public static RegionIterator region(CorrectedResult correct) {
		
		if(correct == null) return new RegionIterator(0, 0, 0, 0);
		else return new RegionIterator(correct.leftX(), correct.bottomY(), correct.width(), correct.height());
		
	}
	
	/**
	 * Receives some world coordinates and dimensions and ensures the returned container's parameters are valid for the given artboard. If 
	 * <code>null</code> is returned, the the region cannot be corrected.
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
	 	return new StackOrHeapAllocation(isStackAllocated ? stackFrame.calloc(size) : memCalloc(size) , isStackAllocated);
	
	}
	
	/**
	 * Returns a string representation of {@code region}, whose width is {@code width} and whose height is {@code height}.
	 * 
	 * @param region region of pixels
	 * @param width width of the region
	 * @param height height of the region 
	 * @return String representation of the region.
	 * @throws NullPointerException if {@code region} is <code>null</code>.
	 * @throws IllegalArgumentException if {@code width} or {@code height} is not positive.
	 */
	public static String regionToString(Pixel[][] region , int width , int height) {
		
		Objects.requireNonNull(region);
		if(width <= 0) throw new IllegalArgumentException("Width invalid.");
		if(height <= 0) throw new IllegalArgumentException("Height invalid.");
		
		StringBuilder builder = new StringBuilder();
		for(int row = 0 ; row < height ; row++) {
			
			for(int col = 0 ; col < width - 1 ; col++) builder.append(String.valueOf(region[row][col])).append(", ");			
			builder.append(String.valueOf(region[row][width - 1])).append('\n');
			
		}
		
		return builder.toString();
		
	}
	
	private Artboards() {}
	
}

