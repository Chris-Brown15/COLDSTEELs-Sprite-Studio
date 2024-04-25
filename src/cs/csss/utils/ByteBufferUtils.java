package cs.csss.utils;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.util.Objects;

import cs.csss.project.Artboard;
import cs.csss.project.IndexPixel;
import cs.csss.project.utils.Artboards;
import cs.csss.project.utils.RegionIterator;
import cs.csss.project.utils.RegionPosition;

/**
 * Contains method(s) for modifying {@code ByteBuffers}, which are used extensively throughout Sprite Studio.
 */
public final class ByteBufferUtils {

	/**
	 * Converts the given source buffer, interpreted as an RGBA, 1 byte per channel pixel buffer, into a buffer of the given number of 
	 * channels, with each channel assumed to be one byte.
	 * 
	 * <p>
	 * 	The "RedAlpha" statement in the name of this method refers to what channel values are used when the {@code newDesiredChannels} 
	 * 	parameter is 2.
	 * </p>
	 * 
	 * @param source — source {@code ByteBuffer}, assumed to be formatted as RGBA  
	 * @param newDesiredChannels — the number of channels the resulting buffer should contain
	 * @return New buffer containing correctly formatted pixel data.
	 */
	public static ByteBuffer reformatBufferRedAlpha(ByteBuffer source , int newDesiredChannels) {
		
		ByteBuffer reformat = verifyAndPrepareReformat(source , newDesiredChannels);
		
		//alpha is at byte 4, color is at byte 1, so the else block's implementation wont work for two byte per pixel
		if(newDesiredChannels == 2) while(reformat.hasRemaining()) {
			
			reformat.put(source.get());
			source.position(source.position() + 2);
			reformat.put(source.get());
			
		} else reformat = reformatToOddChannels(source , reformat , newDesiredChannels);		
		
		memFree(source);
		
		return reformat.flip();
		
	}

	/**
	 * Converts the given source buffer, interpreted as an RGBA, 1 byte per channel pixel buffer, into a buffer of the given number of 
	 * channels, with each channel assumed to be one byte.
	 * 
	 * <p>
	 * 	The "RedGreen" statement in the name of this method refers to what channel values are used when the {@code newDesiredChannels} 
	 * 	parameter is 2.
	 * </p>
	 * 
	 * @param source — source {@code ByteBuffer}, assumed to be formatted as RGBA  
	 * @param newDesiredChannels — the number of channels the resulting buffer should contain
	 * @return New buffer containing correctly formatted pixel data.
	 */
	public static ByteBuffer reformatBufferRedGreen(ByteBuffer source,  int newDesiredChannels) {

		ByteBuffer reformat = verifyAndPrepareReformat(source , newDesiredChannels);

		if(newDesiredChannels == 2) while(reformat.hasRemaining()) {
			
			reformat.put(source.get());
			reformat.put(source.get());
			source.position(source.position() + 2);
			
		} else reformat = reformatToOddChannels(source , reformat , newDesiredChannels);		
		
		memFree(source);
		
		return reformat.flip();
		
	}
		
	private static ByteBuffer reformatToOddChannels(ByteBuffer source, ByteBuffer reformat , int newDesiredChannels) {

		int difference = 4 - newDesiredChannels;
		while(reformat.hasRemaining()) {
			
			for(int i = 0 ; i < newDesiredChannels ; i ++) reformat.put(source.get());
			source.position(source.position() + difference);
			
		}
		
		return reformat;
		
	}
	
	private static ByteBuffer verifyAndPrepareReformat(ByteBuffer source,  int newDesiredChannels) {

		if(newDesiredChannels >= 4 || newDesiredChannels <= 0) { 
			
			throw new IllegalArgumentException(newDesiredChannels + " is not a valid input.");
			
		}
		
		return memAlloc((source.limit() / 4) * newDesiredChannels);
		
	}
	
	/**
	 * Creates and returns a region of index pixels.
	 * 
	 * @param contents — source buffer
	 * @param width — width of the region
	 * @param height — height of the region
	 * @return 2D array of index pixels
	 */
	public static IndexPixel[][] bufferToIndices(ByteBuffer contents , int width , int height) {
		
		IndexPixel[][] pixels = new IndexPixel[height][width];		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) pixels[row][col] = new IndexPixel(contents);
		contents.rewind();
		return pixels;
		
	}

	/**
	 * Creates and returns a region of index pixels from the given buffer. Bytes read from the buffer will be tested by the {@code dropif} 
	 * predicate, and if the test returns true, the values are not written into the array, so {@code null} is left there instead.
	 * 
	 * @param contents — buffer to read from
	 * @param width — width of the region to read
	 * @param height — height of the region to read
	 * @param dropIf — test for if a pixel in the buffer should be ignored and not put in the resulting container
	 * @return 2D array of index pixels created from {@code source}.
	 */
	public static IndexPixel[][] bufferToIndices(ByteBuffer contents , int width , int height , BiBytePredicate dropIf) {
		
		IndexPixel[][] pixelRegion = new IndexPixel[height][width];
		RegionIterator iter = Artboards.region(0 , 0 , width , height);
		
		while(iter.hasNext()) { 
		
			RegionPosition position = iter.next();
			byte x = contents.get();
			byte y = contents.get();
			if(dropIf.test(x, y)) continue;
			
			pixelRegion[position.row()][position.col()] = new IndexPixel(x , y);
			
		}
		
		return pixelRegion;
		
	}
	
	/**
	 * Ensures the parameters given are not out of the bounds of the given artboard. The resulting object's left x, bottom y, and the space defined 
	 * by left x + width and bottom y + height are in bounds for the given artboard. The advance x and advance y values represent offsets from the 
	 * bottom left coordinate to begin indexing from in a 2D array of pixels.
	 * 
	 * @param artboard — some artboard
	 * @param leftX — a left x coordinate
	 * @param bottomY — a bottom y coordinate
	 * @param width — width of the region
	 * @param height — height of the region
	 * @return Container for the corrected bottom left coordinate and dimensions.
	 * @see CorrectedResult
	 */
	public static CorrectedResult correctifyIndices(Artboard artboard , int leftX , int bottomY , int width , int height) {

		int advanceX = 0;
		int advanceY = 0;	
		int operationalWidth = width;
		int operationalHeight = height;

		//santize the indices array and the width and height parameters so we dont go out of bounds
		//check extreme lows:
		if(leftX < 0) {
			
			// the right of the region is left of the left of the artboard, just return
			if(leftX + width < 0) return null;
			operationalWidth += leftX;
			//positive value representing the number of pixels we're removing
			advanceX -= leftX;
			leftX = 0;
			
		}
		
		if(bottomY < 0) {
			
			// the top of the region is below the bottom of the artboard
			if(bottomY + height < 0) return null;
			operationalHeight += bottomY;
			advanceY -= bottomY;
			bottomY = 0;
			
		}
		
		if(leftX + width > artboard.width()) {
			
			//the left of the region is past the right of the artboard
			if(leftX >= artboard.leftX() + artboard.width()) return null;
			operationalWidth = artboard.width() - leftX;
			
		}
		
		if(bottomY + height > artboard.height()) {
		
			//the bottom of the region is above the top of the artboard
			if(bottomY >= artboard.height()) return null;			
			operationalHeight = artboard.height() - bottomY;
			
		}
		
		return new CorrectedResult(leftX , bottomY , operationalWidth , operationalHeight , advanceX , advanceY);
		
	}
	
	/**
	 * Performs the same operations as {@link #correctifyIndices(Artboard, int, int, int, int)}, but this method stores the result in an array. The 
	 * array is laid out as
	 * <ol>
	 * 	<li> corrected left X </li>
	 * 	<li> corrected bottom Y </li>
	 * 	<li> corrected width </li>
	 * 	<li> corrected height </li>
	 * 	<li> X advancement </li>
	 * 	<li> Y advancement </li>
	 * </ol>
	 * 
	 * @param artboard artboard to correct for
	 * @param leftX left x coordinate of a region
	 * @param bottomY bottom y coordinate of a region
	 * @param width width of a region
	 * @param height height of a region 
	 * @param destination {@code int} array containing results 
	 * @return {@code destination}.
	 * @throws ArrayStoreException if {@code destination}'s length is less than six.
	 * @throws NullPointerException if {@code artboard} is <code>null</code>.
	 * @see #correctifyIndices(Artboard, int, int, int, int)
	 */
	public static int[] correctifyIndices(Artboard artboard , int leftX , int bottomY , int width , int height , int[] destination) {

		Objects.requireNonNull(artboard);
		if(destination == null) destination = new int[6];
		if(destination.length < 6) throw new ArrayStoreException("Destination length must be >= 6");
		
		int advanceX = 0;
		int advanceY = 0;	
		int operationalWidth = width;
		int operationalHeight = height;

		//santize the indices array and the width and height parameters so we dont go out of bounds
		//check extreme lows:
		if(leftX < 0) {
			
			// the right of the region is left of the left of the artboard, just return
			if(leftX + width < 0) return null;
			operationalWidth += leftX;
			//positive value representing the number of pixels we're removing
			advanceX -= leftX;
			leftX = 0;
			
		}
		
		if(bottomY < 0) {
			
			// the top of the region is below the bottom of the artboard
			if(bottomY + height < 0) return null;
			operationalHeight += bottomY;
			advanceY -= bottomY;
			bottomY = 0;
			
		}
		
		if(leftX + width > artboard.width()) {
			
			//the left of the region is past the right of the artboard
			if(leftX >= artboard.leftX() + artboard.width()) return null;
			operationalWidth = artboard.width() - leftX;
			
		}
		
		if(bottomY + height > artboard.height()) {
		
			//the bottom of the region is above the top of the artboard
			if(bottomY >= artboard.height()) return null;			
			operationalHeight = artboard.height() - bottomY;
			
		}
		
		destination[0] = leftX;
		destination[1] = bottomY;
		destination[2] = operationalWidth;
		destination[3] = operationalHeight;
		destination[4] = advanceX;
		destination[5] = advanceY;
		
		return destination;
		
	}
	
	private ByteBufferUtils() {}

	/**
	 * Container for modified indices and dimensions of regions of pixels
	 * <p>
	 * 	The {@code advance} integers are used to notate where the new bottom left of the region is. These will always be greater than or equal to the
	 * 	original bottom left coordinate of the region. 
	 * </p>
	 */
	public record CorrectedResult(int leftX , int bottomY , int width , int height , int advanceX , int advanceY) {}
	
}
