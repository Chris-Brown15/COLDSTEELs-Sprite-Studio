package cs.csss.misc.graphcs.framebuffer;

import java.nio.ByteBuffer;

import cs.core.utils.ShutDown;

/**
 * Interface for uniting APIs of objects that are attachable to an OpenGL Framebuffer.
 * 
 * @author Chris Brown
 */
public interface FramebufferAttachment extends ShutDown {
	
	/**
	 * Activates this OpenGL object.
	 */
	void activate();
	
	/**
	 * Deactivates this OpenGL object.
	 */
	void deactivate();
	
	/**
	 * Downloads the data associated with this {@code FramebufferAttachment} and returns it as a {@code ByteBuffer} which must be 
	 * freed using {@link org.lwjgl.system.MemoryUtil#memFree memFree}.
	 * 
	 * @param glFormat — the GL format enum associated with data elements of this attachment
	 * @param glType — type of components of data for this attachment
	 * @return {@code ByteBuffer} containing the downloaded content of this object.
	 */
	ByteBuffer download(int glFormat , int glType);
	
	/**
	 * Integer name of this OpenGL object.
	 * 
	 * @return Name of this object.
	 */
	int ID();
	
	/**
	 * Attaches this attachment to an attachment point some owning framebuffer.
	 * 
	 * @param index — index of this attachment for the given attachment type
	 */
	void attach(int index);
	
}
