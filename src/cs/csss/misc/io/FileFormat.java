package cs.csss.misc.io;

import java.io.IOException;

/**
 * This interface defines the file format API. The file format API will allow users to define file formats in code and use standardized read
 * and write functions to easily write to disk and parse from disk their files.
 * 
 * <p>
 * 	This interface and its implementations attempt to make it easy to treat file contents as a series of chunks. Chunks can be either one
 * 	entry, or a series of entries.
 * </p>
 * 
 * @author Chris Brown
 */
public interface FileFormat extends ChunkCreator {

	public void write() throws IOException;
	
	public void read() throws IOException;
	
}