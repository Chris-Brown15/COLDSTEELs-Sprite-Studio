package cs.csss.utils;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

import cs.csss.project.Artboard;
import cs.csss.project.IndexPixel;
import cs.csss.project.IndexTexture;
import cs.csss.project.LayerPixel;
import cs.csss.project.utils.Artboards;
import cs.csss.project.utils.RegionIterator;

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
		int[] values;
		while(iter.hasNext()) { 
		
			values = iter.next();
			byte x = contents.get();
			byte y = contents.get();
			if(dropIf.test(x, y)) continue;
			
			pixelRegion[values[1]][values[0]] = new IndexPixel(x , y);
			
		}
		
		return pixelRegion;
		
	}
	
	/**
	 * Ensures the parameters given are not out of the bounds of the given artboard.
	 * 
	 * @param artboard — some artboard
	 * @param leftX — a left x coordinate
	 * @param bottomY — a bottom y coordinate
	 * @param width — width of the region
	 * @param height — height of the region
	 * @return Container for the corrected bottom left coordinate and dimensions.
	 */
	public static CorrectedResult correctifyIndices(Artboard artboard , int leftX , int bottomY , int width , int height) {

		int advanceX = 0;
		int advanceY = 0;		
		int limitX = 0;
		int limitY = 0;		
		int operationalWidth = width;
		int operationalHeight = height;

		//santize the indices array and the width and height parameters so we dont go out of bounds
		//check extreme lows:
		if(leftX < 0) {
			
			// the right of the render is left of the left of the artboard, just return
			if(leftX + width < 0) return null;
			operationalWidth += leftX;
			//positive value representing the number of pixels we're removing
			advanceX -= leftX;
			leftX = 0;
			
		}
		
		if(bottomY < 0) {
			
			// the top of the render is below the bottom of the artboard
			if(bottomY + height < 0) return null;
			operationalHeight += bottomY;
			advanceY -= bottomY;
			bottomY = 0;
			
		}
		
		if(leftX + width > artboard.width()) {
			
			//the left of the render is past the right of the artboard
			if(leftX >= artboard.width()) return null;			
			limitX = (leftX + width) - artboard.width(); 
			operationalWidth -= limitX;
			
		}
		
		if(bottomY + height > artboard.height()) {
		
			//the bottom of the render is above the top of the artboard
			if(bottomY >= artboard.height()) return null;
			limitY = (bottomY + height) - artboard.height();			
			operationalHeight -= limitY;
			
		}
		
		return new CorrectedResult(
			new CorrectedParameters(leftX , bottomY , operationalWidth , operationalHeight) , 
			new CorrectedParameterOffsets(advanceX , advanceY , limitX, limitY, operationalWidth, operationalHeight)
		);
		
	}
	
	/**
	 * Puts the given buffer of pixels into the given artboard starting from the {@code (leftX , bottomY)} coordinate and going right and 
	 * up.
	 * 
	 * @param current — artboard to write to
	 * @param leftX — left x coordinate of the region
	 * @param bottomY — bottom y coordinate of the region
	 * @param width — width of the region to write to
	 * @param height — width of the region to write to
	 * @param contents — buffer containing lookup data to write to the artboard
	 * @param dropIf — condition for dropping a pixel.
	 */
	public static void putBufferInArtboard(
		Artboard artboard , 
		int leftX , 
		int bottomY , 
		int width , 
		int height , 
		ByteBuffer contents ,
		BiBytePredicate dropIf
	) {

		int advanceX = 0;
		int advanceY = 0;		
		int limitX = 0;
		int limitY = 0;		
		int operationalWidth = width;
		int operationalHeight = height;

		//santize the indices array and the width and height parameters so we dont go out of bounds
		//check extreme lows:
		if(leftX < 0) {
			
			// the right of the render is left of the left of the artboard, just return
			if(leftX + width < 0) return;
			operationalWidth += leftX;
			//positive value representing the number of pixels we're removing
			advanceX -= leftX;
			leftX = 0;
			
		}
		
		if(bottomY < 0) {
			
			// the top of the render is below the bottom of the artboard
			if(bottomY + height < 0) return;
			operationalHeight += bottomY;
			advanceY -= bottomY;
			bottomY = 0;
			
		}
		
		if(leftX + width > artboard.width()) {
			
			//the left of the render is past the right of the artboard
			if(leftX >= artboard.width()) return;			
			limitX = (leftX + width) - artboard.width(); 
			operationalWidth -= limitX;
			
		}
		
		if(bottomY + height > artboard.height()) {
		
			//the bottom of the render is above the top of the artboard
			if(bottomY >= artboard.height()) return;
			limitY = (bottomY + height) - artboard.height();			
			operationalHeight -= limitY;
			
		}
		
		int maxRow = bottomY + operationalHeight;
		int maxCol = leftX + operationalWidth;

		contents.position(advanceY * width * IndexTexture.pixelSizeBytes);
		contents.limit(contents.limit() - (limitY * width * IndexTexture.pixelSizeBytes));
		
		for(int row = bottomY ; row < maxRow ; row++) {
	
			contents.position(contents.position() + (advanceX * IndexTexture.pixelSizeBytes));
			
			 for(int col = leftX ; col < maxCol ; col++) {
					
				byte lookupX = contents.get();
				byte lookupY = contents.get();
				
				if(dropIf.test(lookupX, lookupY)) continue;
				
				LayerPixel pixel = new LayerPixel(col , row , lookupX , lookupY);
				artboard.putColorInImage(col, row, 1, 1, pixel);
				
			}

			contents.position(contents.position() + (limitX * IndexTexture.pixelSizeBytes));			
			 
		}

		memFree(contents);
	
	}
	
	private ByteBufferUtils() {}

	/**
	 * Container for modified indices and dimensions of regions of pixels
	 */
	public record CorrectedParameters(int leftX , int bottomY , int width , int height) {}
	
	/**
	 * Container for offsets for use when working with regins of pixels.
	 * <p>
	 * 	The {@code advance} integers are used to notate where the new bottom left of the region is. These will always be greater than the
	 * 	original bottom left coordinate of the region. The {@code limit} integers notate where to stop when working with a region. And the
	 * 	{@code operational} dimensions are the new dimensions of the region.
	 * </p>
	 */
	public record CorrectedParameterOffsets(
		int advanceX , 
		int advanceY , 
		int limitX , 
		int limitY , 
		int operationalWidth , 
		int operationalHeight
	) {}

	/**
	 * Container for corrected parameters and corrected offsets.
	 */
	public record CorrectedResult(CorrectedParameters params , CorrectedParameterOffsets offsets) {}
	
}
