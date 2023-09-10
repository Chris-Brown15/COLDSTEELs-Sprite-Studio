package cs.csss.project.utils;

import java.nio.ByteBuffer;

/**
 * Container for a {@code ByteBuffer} which may or may not be heap allocated. 
 * 
 */
public record StackOrHeapAllocation(ByteBuffer buffer , boolean stackAllocated) {}
