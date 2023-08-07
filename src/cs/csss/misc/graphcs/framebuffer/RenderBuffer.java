package cs.csss.misc.graphcs.framebuffer;

import static cs.core.utils.CSUtils.require;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11C.glReadPixels;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.GL_RG;
import static org.lwjgl.opengl.GL30C.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30C.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL30C.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30C.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30C.glRenderbufferStorageMultisample;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.nio.ByteBuffer;

/**
 * Basic implementation of an OpenGL Render Buffer for color storage. 
 * 
 * @author Chris Brown 
 */
public class RenderBuffer implements FramebufferAttachment {
	
	private int renderbufferID = 0;
	
	private int
		width ,
		height ,
		channels;	
	
	public RenderBuffer() {}
	
	/**
	 * @see {@link RenderBuffer#initialize(int, int, int) initialize}.
	 */
	public RenderBuffer(int width , int height , int channelType) {
		
		initialize(width , height , channelType);
		
	}
	
	/**
	 * @see {@link RenderBuffer#initialize(int, int, int, int) initialize}.
	 */
	public RenderBuffer(int width , int height , int channelType , int multisamples) {

		initialize(width , height , channelType , multisamples);
		
	}

	/**
	 * Initializes this render buffer with the given parameters.
	 * 
	 * @param width — width of this render buffer
	 * @param height — height of this render buffer
	 * @param channelType — OpenGL channel type for color components of this render buffer
	 */
	public void initialize(int width , int height , int channelType) {
		
		initialize(width , height , channelType , 0);
		
	}
	
	/**
	 * Initializes this render buffer with the given parameters, allowing for multisampling.
	 * 
	 * @param width — width of this render buffer
	 * @param height — height of this render buffer
	 * @param channelType — OpenGL channel type for color components of this render buffer
	 * @param multisamples — number of samples to perform
	 */
	public void initialize(int width , int height , int channelType , int multisamples) {

		//dont allow reinitialization because it isn't recommended
		require(renderbufferID == 0);
		
		this.width = width;
		this.height = height;
		
		channels = switch(channelType) {
			case GL_RGBA -> 4;
			case GL_RGB -> 3;
			case GL_RG -> 2;
			case GL_RED -> 1;
			default -> throw new IllegalArgumentException("Unexpected value: " + channelType);
		};
		
		renderbufferID = glGenRenderbuffers();
		activate();		
		glRenderbufferStorageMultisample(GL_RENDERBUFFER , multisamples , channelType , width , height);		
		deactivate();
		
	}
	
	@Override public ByteBuffer download(int glFormat , int glType) {
		
		ByteBuffer imageData = memAlloc(width * height * channels);
		activate();
		glReadPixels(0 , 0 , width , height , glFormat , glType , imageData);
		deactivate();
		return imageData;
		
	}
	
	@Override public void activate() {
		
		glBindRenderbuffer(GL_RENDERBUFFER , renderbufferID);
		
	}

	@Override public void deactivate() {
		
		glBindRenderbuffer(GL_RENDERBUFFER , 0);
		
	}

	public int renderbufferID() {
		
		return renderbufferID;
		
	}
	
	@Override public int ID() {
		
		return renderbufferID();
		
	}
	
	@Override public void shutDown() {

		glDeleteRenderbuffers(renderbufferID);
		renderbufferID = -1;
		
	}

	@Override public boolean isFreed() {

		return renderbufferID == -1;
	
	}

	@Override public void attach(int index) {

		activate();
		glFramebufferRenderbuffer(GL_FRAMEBUFFER , GL_COLOR_ATTACHMENT0 + index , GL_RENDERBUFFER , renderbufferID());
		deactivate();
		
	}
	
}
