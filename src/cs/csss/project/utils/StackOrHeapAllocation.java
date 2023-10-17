package cs.csss.project.utils;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

/**
 * Container for a {@code ByteBuffer} which may or may not be heap allocated. 
 * 
 */
public record StackOrHeapAllocation(ByteBuffer buffer , boolean stackAllocated) {
	
	/**
	 * Frees the buffer contained within this allocation only if it is not stack allocated.
	 */
	public void free() {
		
		if(!stackAllocated) memFree(buffer);
		
	}
	
}
