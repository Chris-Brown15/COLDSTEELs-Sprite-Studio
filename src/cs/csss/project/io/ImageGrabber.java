package cs.csss.project.io;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;

import java.nio.ByteBuffer;
import cs.csss.annotation.RenderThreadOnly;
import sc.core.graphics.SCFramebuffer;
import sc.core.graphics.SCRenderBuffer;

/**
 * Class used for utilities of rendering world space images to a framebuffer and providing access to the result of the render as a 
 * {@code ByteBuffer}. Designed to be use case agnostic.
 */
public class ImageGrabber {

	private final SCFramebuffer framebuffer;
	private final Runnable renderCallback;
	
	private final SCRenderBuffer renderbuffer;
	private final int width , height;
	
	/**
	 * Creates an image grabber.
	 *  
	 * @param framebuffer {@link cs.csss.annotation.Nullable @Nullable} framebuffer for rendering
	 * @param renderbuffer {@code @Nullable} renderbuffer to receive render
	 * @param renderCallback code to invoke to render something to the framebuffer's attachments
	 * @param width width of the rendered region in world space
	 * @param height height of the rendered region in world space
	 */
	public ImageGrabber(SCFramebuffer framebuffer , SCRenderBuffer renderbuffer , Runnable renderCallback , int width , int height) {

		this.framebuffer = framebuffer;
		this.renderbuffer = renderbuffer;
		this.renderCallback = renderCallback;
		this.width = width;
		this.height = height;
		
	}
	
	/**
	 * Renders the project into the given renderbuffer and returns a {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} downloaded
	 * {@code ByteBuffer} of that image.
	 * 
	 * @return {@code @FreeAfterUse ByteBuffer} containing the resulting render, downloaded.
	 */
	@RenderThreadOnly public ByteBuffer renderImage() {

		int glFormat = GL_RGBA;

		SCFramebuffer useFramebuffer = framebuffer;
		
		if(framebuffer == null) { 
		
			useFramebuffer = new SCFramebuffer();
			useFramebuffer.initialize();
			
		}		

		//render the scene
		useFramebuffer.activate();
		
		SCRenderBuffer useRenderBuffer = renderbuffer != null ? renderbuffer : new SCRenderBuffer(width , height , glFormat);
		useFramebuffer.addColorAttachment(useRenderBuffer);
		
		glViewport(0 , 0 , width , height);
		glClearColor(0 , 0 , 0 , 0);
		glClear(GL_COLOR_BUFFER_BIT);

		renderCallback.run();
				
		//resulting data
		ByteBuffer download = useRenderBuffer.download(glFormat, GL_UNSIGNED_BYTE);
		
		if(renderbuffer == null) {
			
			useRenderBuffer.shutDown();
			useFramebuffer.removeColorAttachment(0);
			
		}
		
		if(framebuffer == null) useFramebuffer.shutDown();
		
		SCFramebuffer.deactivate();
		
		return download; 
		
	}

}
