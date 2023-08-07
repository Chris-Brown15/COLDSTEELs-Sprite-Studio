package cs.csss.project.io;

/**
 * Default file types for images.
 */
public enum ExportFileTypes {

	PNG(".png") ,
	BMP(".bmp") ,
	TGA(".tga") ,
	JPEG(".jpg")
	;
	
	public final String ending;
	
	ExportFileTypes(String type) {
		
		this.ending = type;
		
	}

	/**
	 * Returns the export callback corresponding to this {@code ExportFileTypes} corresponding export function.
	 * 
	 * @return {@link cs.csss.project.io.ExportCallback ExportCallback} corresponding to this {@code ExportFileTypes}.
	 */
	public final ExportCallback callbackOf() {
		
		return switch(this) {
			case BMP -> ExportCallback.stbBMP;
			case JPEG -> ExportCallback.stbJPG;
			case PNG -> ExportCallback.stbPNG;
			case TGA -> ExportCallback.stbTGA;
		
		};
		
	}
	
}
