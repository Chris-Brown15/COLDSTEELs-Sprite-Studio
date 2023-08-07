package cs.csss.misc.io;

public interface ChunkCreator {

	/**
	 * Creates a chunk with the given name, or returns the chunk previously created with that name.
	 * 
	 * @param name — name of a chunk
	 * @return Chunk present in this {@code FileFormat}.
	 */
	public ContainerChunk chunk(String name);
			
	public void addChunk(ContainerChunk chunk);
	
	public void removeChunk(ContainerChunk chunk);
		
}
