package cs.csss.project.io;

/**
 * Class containing data for exporters.
 */
public final class ExportParameters {

	/**
	 * Integer between 0 and 100 notating the quality of the exported JPEG. 
	 * <p>
	 * 	<q>Higher quality looks better but results in a bigger image.</q>
	 * </p>
	 */
	private static int JPEGQuality = 100;
	
	/**
	 * Sets JPEG export quality, a value from 1 , 100, where larger values result in better quality, at higher file sizes.
	 * 
	 * @param quality a JPEG export quality value
	 */
	public static void JPEGQuality(int quality) {
		
		assert quality > 0 && quality <= 100 : quality + " out of bounds for [1 , 100]";
		
		JPEGQuality = quality;
		
	}
	
	/**
	 * Returns the current JPEG export quality value.
	 * 
	 * @return The current JPEG export quality value.
	 */
	public static int JPEGQuality() {
		
		return JPEGQuality;
		
	}
	
	private ExportParameters() {
		
	}

}
