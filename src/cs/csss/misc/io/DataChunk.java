package cs.csss.misc.io;

public abstract class DataChunk extends Chunk implements Cloneable {

	protected DataChunk(String name) {
		
		super(name);

	}

	protected abstract Object get();
	
	protected abstract void set(Object value);
	
	protected abstract DataChunk clone();
	
}
