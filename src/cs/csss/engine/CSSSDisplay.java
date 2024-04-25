package cs.csss.engine;

import static cs.core.graphics.StandardRendererConstants.MAG_FILTER_LINEAR;
import static cs.core.graphics.StandardRendererConstants.MIN_FILTER_LINEAR;

import cs.core.CSWindow;
import cs.core.graphics.CSOrthographicCamera;
import cs.core.graphics.CSTexture;
import cs.core.ui.CSNuklear;
import cs.core.utils.ShutDown;
import cs.core.utils.files.TTF;
import cs.core.utils.threads.Await;

public class CSSSDisplay implements ShutDown {

	public final boolean isMultiThreaded;
	
	public CSWindow window;
	public CSSSRenderer renderer;
	public CSOrthographicCamera camera;
	public CSNuklear nuklear;
	public TTF defaultFont;
	
	CSSSDisplay(
		boolean shouldMultiThread , 
		String windowName , 
		int defaultFontHeight , 
		String fontAbsFilePath
		) {

		window = new CSWindow(windowName);		
		renderer = new CSSSRenderer(window);
		
		if(isMultiThreaded = shouldMultiThread) renderer.threadStart();

		Await rendererInit = renderer.post(() -> {
			
			renderer.initialize();
			if(fontAbsFilePath != null) { 
				
				defaultFont = new TTF(defaultFontHeight , fontAbsFilePath);
				CSTexture fontTexture = new CSTexture();
				fontTexture.initialize(defaultFont.asGraphic(), MAG_FILTER_LINEAR|MIN_FILTER_LINEAR);
				
				CSNuklear.CSNuklearRender.initializeTheUIShader();
				nuklear = new CSNuklear(window, defaultFont, fontTexture);
				renderer.addRender(nuklear.uiRender());

			}
			
		});

		window.onFramebufferResize((newWidth , newHeight) -> {
			
			camera.resetProjection(newWidth, newHeight);
			renderer.post(() -> renderer.setViewport(newWidth , newHeight));
			
		});
		
		int[] windowDims = window.size();
		camera = new CSOrthographicCamera(windowDims[0], windowDims[1]);		
				
		rendererInit.await();
		
	}

	/**
	 * Standard way to get inputs from the system and propogate them to the UI if it is available. Does not block the caller.
	 */
	public void pollInputs() {
		
		if(nuklear != null) { 
			
			nuklear.beginInput();
			CSWindow.pollEvents();
			nuklear.endInput();

		} else CSWindow.pollEvents();
		
	}

	/**
	 * Standard way to get inputs from the system and propogate them to the UI if it is available. Does block the caller until an event
	 * is posted.
	 */
	public void waitInputs() {
		
		if(nuklear != null) { 
			
			nuklear.beginInput();
			CSWindow.waitEvents();
			nuklear.endInput();

		} else CSWindow.pollEvents();
		
	}

	@Override public void shutDown() {

		if(nuklear != null) { 
			
			nuklear.shutDown();
			defaultFont.shutDown();
			
		}
		
		window.shutDown();
		if(!isMultiThreaded) renderer.shutDown();
		
	}
	
	@Override public boolean isFreed() {
		
		return window.isFreed() && renderer.isFreed();
		
	}
	
}
