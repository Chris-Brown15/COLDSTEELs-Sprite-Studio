package cs.csss.project.io;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;

import java.nio.ByteBuffer;
import cs.core.utils.Lambda;
import cs.csss.misc.graphcs.framebuffer.Framebuffer;
import cs.csss.misc.graphcs.framebuffer.RenderBuffer;

/**
 * Class used for utilities of rendering world space images to a framebuffer and providing access to the result of the render as a 
 * {@code ByteBuffer}. Designed to be use case agnostic.
 */
public class ImageGrabber {

	private final Framebuffer framebuffer;
	private final Lambda renderCallback;
	
	private final RenderBuffer renderbuffer;
	private final int 
		width ,
		height;
	
	public ImageGrabber(Framebuffer framebuffer , RenderBuffer renderbuffer , Lambda renderCallback , int width , int height) {

		this.framebuffer = framebuffer;
		this.renderbuffer = renderbuffer;
		this.renderCallback = renderCallback;
		this.width = width;
		this.height = height;
		
	}
	
	/**
	 * Renders the project into the given renderbuffer and returns a download of that image.
	 * 
	 * @return {@code ByteBuffer} containing the resulting render, downloaded.
	 */
	public ByteBuffer renderImage() {

		int glFormat = GL_RGBA;

		Framebuffer useFramebuffer = framebuffer;
		
		if(framebuffer == null) { 
		
			useFramebuffer = new Framebuffer();
			useFramebuffer.initialize();
			
		}		
		
		RenderBuffer useRenderBuffer = renderbuffer != null ? renderbuffer : new RenderBuffer(width , height , glFormat);
		useFramebuffer.addColorAttachment(useRenderBuffer);
		
		//render the scene
		useFramebuffer.activate();
		
		glViewport(0 , 0 , width , height);
		glClearColor(0 , 0 , 0 , 0);
		glClear(GL_COLOR_BUFFER_BIT);

		renderCallback.invoke();
				
		//resulting data
		ByteBuffer download = useRenderBuffer.download(glFormat, GL_UNSIGNED_BYTE);
		
		if(renderbuffer == null) {
			
			useRenderBuffer.shutDown();
			useFramebuffer.removeColorAttachment(0);
			
		}
		
		if(framebuffer == null) useFramebuffer.shutDown();
		
		Framebuffer.deactivate();
		
		return download; 
		
	}

}
