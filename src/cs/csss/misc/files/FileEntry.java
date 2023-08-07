package cs.csss.misc.files;

import static cs.core.utils.CSUtils.specify;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Container for individual data entries of the file.
 */
public class FileEntry {
	
	//object to compose 
	protected Object object;
	//name of this entry
	public final String name;
	//size of this entry in bytes
	protected int sizeBytes = -1;

	//read and write functions
	protected BiConsumer<Object , ByteBuffer> write;
	protected Function<ByteBuffer , Object> read;
	
	//getter of object. This is prioritized over the object member of this class.
	protected Supplier<Object> getter = null;
	
	protected FileEntry(FileEntry copyThis) {
		
		this.object = copyThis.object;
		this.name = copyThis.name;
		this.sizeBytes = copyThis.sizeBytes;
		this.write = copyThis.write;
		this.read = copyThis.read;
		this.getter = copyThis.getter;
		
	}

	protected FileEntry(
		Object object , 
		int sizeBytes ,
		final String name , 
		final BiConsumer<Object , ByteBuffer> write , 
		final Function<ByteBuffer , Object> read
	) {
		
		this.sizeBytes = sizeBytes;
		this.object = object;
		this.name = name;
		this.write = write;
		this.read = read;
		
	}

	protected FileEntry(
		int sizeBytes ,
		Supplier<Object> getter , 
		final String name , 
		final BiConsumer<Object , ByteBuffer> write , 
		final Function<ByteBuffer , Object> read
	) {
		
		this.sizeBytes = sizeBytes;
		this.getter = getter;
		this.name = name;
		this.write = write;
		this.read = read;
		
	}

	protected FileEntry(
		int sizeBytes ,
		final String name , 
		final BiConsumer<Object , ByteBuffer> write , 
		final Function<ByteBuffer , Object> read
	) {
		
		this.sizeBytes = sizeBytes;			
		this.name = name;
		this.write = write;
		this.read = read;
		
	}
	
	protected FileEntry(String name , BiConsumer<Object , ByteBuffer> write , Function<ByteBuffer , Object> read) {
		
		this.name = name;
		this.write = write;
		this.read = read;
		
	}
	
	protected FileEntry(String name , Composable composable) {
		
		this(composable , composable.sizeBytes() , name , (obj , buffer) -> composable.put(buffer) , composable::retrieve);
		
	}
	
	protected FileEntry(String name , Supplier<Composable> getter) {
		
		this(getter , getter.get().sizeBytes() , name , (obj , buffer) ->  getter.get().put(buffer) , getter.get()::retrieve);
		
	}
			
	/**
	 * Gets the object contained within this file entry. Note that the returned object will be the one gotten from this object's getter
	 * if the getter is not null and the mode's {@code WRITE} bit is set, otherwise it will return the object.
	 * 
	 * @return The object contained within this file entry.
	 */
	public Object object() {
		
		return getter != null ? getter.get() : object;
		
	}
	
	public void write(ByteBuffer buffer) {

		if(getter != null) object = getter.get();
		
		specify(object , "Data must be bound before writing a component of a File Composition.");
		
		int bufferInitialPosition = buffer.position();
		write.accept(object, buffer);
		
		specify(
			bufferInitialPosition + sizeBytes == buffer.position() , 
			"Invalid write operation: " + sizeBytes + " is " + name + "'s size in bytes, but " + 
			(buffer.position() - bufferInitialPosition) + " bytes were written by " + name + "'s write function."
		);
		
	}
	
	public void read(ByteBuffer buffer) {
	
		object = read.apply(buffer);
		
	}
	
	/**
	 * Attempts to return a string representation of the object this FileData contains as accurately as possible.
	 */
	@Override public String toString() {
		
		String objectString;
		
		if(object != null) {
			
			if(!object.getClass().isArray()) objectString = object.toString();
			else {
				
				if(object instanceof byte[]) objectString = Arrays.toString((byte[]) object);
				else if (object instanceof short[]) objectString = Arrays.toString((short[]) object);
				else if (object instanceof int[]) objectString = Arrays.toString((int[]) object);
				else if (object instanceof long[]) objectString = Arrays.toString((long[]) object);
				else if (object instanceof float[]) objectString = Arrays.toString((float[]) object);
				else if (object instanceof double[]) objectString = Arrays.toString((double[]) object);
				else if (object instanceof boolean[]) objectString = Arrays.toString((boolean[]) object);
				else if (object instanceof char[]) objectString = Arrays.toString((char[]) object);
				else objectString = Arrays.toString((Object[]) object);
				
			}
			
		} else objectString = "Null";
		
		return name + " => " + objectString;
		
	}
			
}
