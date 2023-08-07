package cs.csss.project.io;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBImageWrite.stbi_write_jpg;
import static org.lwjgl.stb.STBImageWrite.stbi_write_tga;
import static org.lwjgl.stb.STBImageWrite.stbi_write_bmp;

import java.nio.ByteBuffer;

/**
 * Interface for exporting API. 
 * 
 * <p>
 * 	This interface defines an export function, which takes a variety of arguments and creates an image from them. The export function should
 * 	create a file at the given location and store in it image data.
 * </p>
 */
@FunctionalInterface
public interface ExportCallback {

	public static final ExportCallback
		stbPNG = (location , data , width , height , channels) -> stbi_write_png(location , width , height , channels , data , 0) ,
		stbBMP = (location , data , width , height , channels) -> stbi_write_bmp(location , width , height , channels , data) ,
		stbTGA = (location , data , width , height , channels) -> stbi_write_tga(location , width , height , channels , data) ,
		stbJPG = (location , data , width , height , channels) -> { 
			
			stbi_write_jpg(location , width , height , channels , data , ExportParameters.JPEGQuality());
			
		};
		
	
	public void export(String location , ByteBuffer data , int width , int height , int channels);
	
}
