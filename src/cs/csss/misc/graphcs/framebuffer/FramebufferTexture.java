package cs.csss.misc.graphcs.framebuffer;

import static cs.core.graphics.StandardRendererConstants.MAG_FILTER_LINEAR;
import static cs.core.graphics.StandardRendererConstants.MAG_FILTER_NEAREST;
import static cs.core.graphics.StandardRendererConstants.MIN_FILTER_LINEAR;
import static cs.core.graphics.StandardRendererConstants.MIN_FILTER_NEAREST;
import static cs.core.graphics.StandardRendererConstants.S_WRAP_BORDER;
import static cs.core.graphics.StandardRendererConstants.S_WRAP_EDGE;
import static cs.core.graphics.StandardRendererConstants.S_WRAP_MIRRORED;
import static cs.core.graphics.StandardRendererConstants.S_WRAP_REPEAT;
import static cs.core.graphics.StandardRendererConstants.T_WRAP_BORDER;
import static cs.core.graphics.StandardRendererConstants.T_WRAP_EDGE;
import static cs.core.graphics.StandardRendererConstants.T_WRAP_MIRRORED;
import static cs.core.graphics.StandardRendererConstants.T_WRAP_REPEAT;
import static cs.core.graphics.StandardRendererConstants.present;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14C.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.nio.ByteBuffer;

import cs.core.graphics.CSTexture;

/**
 * Texture implementation for use with {@link cs.csss.misc.graphics.framebuffer.Framebuffer Framebuffer}. Use this class's initialize method to 
 * allocate an uninitialized texture memory for the framebuffer to write to.
 * 
 * @author Chris Brown
 */
public class FramebufferTexture extends CSTexture implements FramebufferAttachment {
	
	private int width , height , channels;
	
	/**
	 * Initializes this texture so it allocates memory to write and read from but does not initialize the memory itself.
	 * 
	 * @param width — width of this texture
	 * @param height — height of this texture
	 * @param channels — number of channels of this texture
	 * @param glFormat — the OpenGL format type of this texture
 	 * @param glChannelType — the OpenGL channel component type of this texture
	 * @param textureOptions — texture options for this texture
	 */
	public void initialize(int width , int height , int channels , int glFormat , int glChannelType , int textureOptions) {
		
		this.width = width;
		this.height = height;
		this.channels = channels;
		
		textureID = glGenTextures();
		activate();

		if(present(MIN_FILTER_NEAREST , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_MIN_FILTER , GL_NEAREST);
		else if(present(MIN_FILTER_LINEAR , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_MIN_FILTER , GL_LINEAR);
		
		if(present(MAG_FILTER_NEAREST , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_MAG_FILTER , GL_NEAREST);
		else if(present(MAG_FILTER_LINEAR , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_MAG_FILTER , GL_NEAREST);

		if(present(S_WRAP_REPEAT , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_S , GL_REPEAT);
		else if(present(S_WRAP_MIRRORED , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_S , GL_MIRRORED_REPEAT);
		else if(present(S_WRAP_EDGE , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_S , GL_CLAMP_TO_EDGE);
		else if(present(S_WRAP_BORDER , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_S , GL_CLAMP_TO_BORDER);

		if(present(T_WRAP_REPEAT , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_T , GL_REPEAT);
		else if(present(T_WRAP_MIRRORED , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_T , GL_MIRRORED_REPEAT);
		else if(present(T_WRAP_EDGE , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_T , GL_CLAMP_TO_EDGE);
		else if(present(T_WRAP_BORDER , textureOptions)) glTexParameteri(GL_TEXTURE_2D , GL_TEXTURE_WRAP_T , GL_CLAMP_TO_BORDER);
					
		ByteBuffer none = null;
		glTexImage2D(GL_TEXTURE_2D , 0 , glFormat , width , height , 0 , glFormat , glChannelType , none);
		
		deactivate();
		
	}
	
	@Override public int ID() { 
		
		return textureID();			
		
	}
	
	@Override public ByteBuffer download(int glFormat , int glType) {
		
		ByteBuffer result = memAlloc(width * height * channels);
		activate();
		glGetTexImage(GL_TEXTURE_2D , 0 , glFormat , glType , result);
		deactivate();
		return result;
		
	}

	/**
	 * Attaches this texture as a color destination occupying the given index.
	 */
	@Override public void attach(int index) { 
		
		activate();
		glFramebufferTexture(GL_FRAMEBUFFER , GL_COLOR_ATTACHMENT0 + index, ID() , 0);
		deactivate();
		
	}
	
}
