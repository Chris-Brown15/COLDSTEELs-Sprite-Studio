package cs.csss.misc.graphcs.framebuffer;

import static cs.core.utils.CSUtils.require;
import static org.lwjgl.opengl.GL11C.glGetIntegerv;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_MAX_COLOR_ATTACHMENTS;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30C.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30C.glDeleteFramebuffers;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import cs.core.utils.ShutDown;

/**
 * Implementation of an OpenGL Framebuffer. This frame buffer only supports color buffers, but can handle up to 
 * {@link Framebuffer#maxColorDestinations} different color attachments.
 * 
 * @author Chris Brown
 */
public class Framebuffer implements ShutDown {

	private static int maxColorDestinations = 0;
	
	private static Framebuffer theActiveFrameBuffer;
	
	/**
	 * Deactivates the current framebuffer.
	 */
	public static void deactivate() {
		
		glBindFramebuffer(GL_FRAMEBUFFER , 0);
		theActiveFrameBuffer = null;
		
	}

	/**
	 * Retrieves the max number of different color buffers that can be active at the same time.
	 */
	private static void getMaxColorDestinations() {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {

			IntBuffer max = stack.ints(1);
			glGetIntegerv(GL_MAX_COLOR_ATTACHMENTS , max);
			maxColorDestinations = max.get();	
			
		}
		
	}
	
 	private int framebufferID;
 	/**
 	 * Storage of all bound color attachments.
 	 */
 	private FramebufferAttachment[] colorAttachments;
	
 	/**
 	 * Initializes this frame buffer.
 	 */
	public void initialize() {
		
		if(maxColorDestinations == 0) getMaxColorDestinations();
		
		colorAttachments = new FramebufferAttachment[maxColorDestinations];
		
	 	framebufferID = glGenFramebuffers();
	 		 	
	}
	
	/**
	 * Adds the given attachment as a color attachment. Draw calls will subsequently affect {@code target}.
	 * 
	 * @param target — a color buffer this frame buffer will write to
	 */
	public void addColorAttachment(FramebufferAttachment target) { 
		
		activate();
		
		int nextColor = nextColorAttachment();
		target.attach(nextColor);
		colorAttachments[nextColor] = target; 
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			IntBuffer drawBuffers = stack.mallocInt(nextColor + 1);
			for(int i = 0 ; i < drawBuffers.limit() ; i++) drawBuffers.put(GL_COLOR_ATTACHMENT0 + i);
			drawBuffers.flip();
			glDrawBuffers(drawBuffers);
			
		}
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) System.err.println("Framebuffer failure.");
		
		deactivate();
		
	}
	
	/**
	 * Removes a color attachment at the given index.
	 * 
	 * @param index — index of a color attachment to remove
	 */
	public void removeColorAttachment(int index) {
		
		require(index < colorAttachments.length);
		require(index >= 0);
		
		activate();
		
		FramebufferAttachment colorAttachment = colorAttachments[index];
		colorAttachments[index] = null;
		//TODO make this not cause problems if the attachment is present in other framebuffers
		colorAttachment.shutDown();
		
		deactivate();
		
	}
	
	/**
	 * Activates this frame buffer.
	 */
	public void activate() {
		
		if(theActiveFrameBuffer != this) { 
			
			glBindFramebuffer(GL_FRAMEBUFFER , framebufferID);
			theActiveFrameBuffer = this;
			
		}
		
	}

	@Override public void shutDown() {
		
		if(framebufferID == -1) return;

		for(FramebufferAttachment x : colorAttachments) if(x != null) x.shutDown();
		
		glDeleteFramebuffers(framebufferID);
		framebufferID = -1;
		
		if(this == theActiveFrameBuffer) theActiveFrameBuffer = null;
		
	}

	@Override public boolean isFreed() {

		return framebufferID == -1;

	}

	private int nextColorAttachment() {
		
		int i = 0;
		while(i < colorAttachments.length && colorAttachments[i] != null) i++;
		if(i == colorAttachments.length) throw new IndexOutOfBoundsException("This framebuffer has all color attachments in use.");
		return i;
		
	}
	
}
