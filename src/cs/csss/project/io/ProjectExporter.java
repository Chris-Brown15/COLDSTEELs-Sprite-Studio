package cs.csss.project.io;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryUtil.memFree;
import static cs.csss.utils.NumberUtils.nearestGreaterOrEqualPowerOfTwo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import cs.core.graphics.CSOrthographicCamera;
import cs.core.graphics.CSStandardRenderer;
import cs.core.utils.Lambda;
import cs.coreext.nanovg.NanoVG;
import cs.coreext.nanovg.NanoVGFrame;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.Engine;
import cs.csss.engine.Logging;
import cs.csss.misc.graphcs.framebuffer.Framebuffer;
import cs.csss.misc.graphcs.framebuffer.RenderBuffer;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.project.CSSSShader;
import cs.csss.project.Layer;
import cs.csss.utils.ByteBufferUtils;

/**
 * Class responsible for exporting projects as images.
 * 
 * <p>
 * 	This class handles everything needed to produce an image from a project. There are a few discrete steps for exporting:
 * 	<ol>
 * 		<li>
 * 			Begin exporting by opening an export widget
 * 		</li>
 * 		<li>
 * 			Arrange animations and loose artboards however the user wants
 * 		</li>
 * 		<li>
 * 			Render the current entire project to a render buffer using an alternate framebuffer
 * 		</li>
 * 		<li>
 * 			Download the render buffer to CPU
 * 		</li>
 * 		<li>
 * 			Send the downloaded frame to an exporter
 * 		</li>
 * 	</ol> 
 * 	This approach uses a framebuffer and render buffer to 'screenshot' the project, AKA rendering the project to a render buffer, then 
 * 	downloading the resulting image from VRAM and exporting it as is. This approach has some advantages and disadvantages.
 * 	<br>
 * 	<b>Advantages:</b>
 * 	<ul>
 * 		<li>
 * 			It is simple. The entire process of exporting is very simple to conceptualize and implement.
 * 		</li>
 * 		<li>
 * 			It gives the user complete control of how their image will look. Users can arrange artboards and animations however they like.
 * 		</li>
 * 		<li>
 * 			It is arguably faster than alternatives. The main overhead of this appraoch (outside of actually writing the image, which is 
 * 			currently handled by the stbi library), is downloading the image, but this is a single download, rather than the alternative
 * 			which is to download each artboard separately.
 * 		</li>
 * 	</ul>
 * 	<b>Disadvantages:</b>
 * 	<ul>
 * 		<li>
 * 			The main disadvantage of this appraoch is the fact that a render buffer must be allocated on VRAM. For large projects, this 
 * 			additional usage of VRAM could exceed the amount the user has remaining. One mitigation of this is to unload artboards from VRAM
 * 			during export until it is time to render them to the render buffer, at which point they could be loaded again, but this could
 * 			dramatically increase export times.
 * 		</li>
 * 		<li>
 * 			An additional disadvantage is the requirement to rerender the entire project. This is not required in alternate approaches, but
 * 			cannot be avoided in this one.
 * 		</li>
 * 	</ul>
 * </p>
 * <p>
 * 	The actual image rendering implementation of the exporter has been moved to {@link cs.csss.project.io.ImageGrabber ImageGrabber}.  
 * </p>
 * 
 * 	<b>TODO:</b>
 * <p>
 * 	Any project, no matter its number of channels, is rendered as a four byte per pixel image. Once it's downloaded, its converted into a byte 
 * 	buffer of the appropriate type for the number of channels per pixel of the project. This is clearly inferior to rendering the image as a propper
 * 	number of channels per pixel the first time.
 * </p>
 * 	
 * @author Chris Brown
 */
@RenderThreadOnly public class ProjectExporter {

	private final CSStandardRenderer renderer;
	private final CSSSProject project;
	
	private final Framebuffer framebuffer;	
	
	private final Lambda swapBuffersCallback;
	
	private final String 
		exportFolderPath ,
		exportName;
	
	private final ArrayList<ExportCallbackAndName> exporters;
	
	private final float
		exportMidX ,
		exportMidY;
	
	private final int 
		exportWidth ,
		exportHeight ,
		visualChannels;
	
	private final boolean
		exportPalettes ,
		exportHiddenLayers ,		
		hideCheckeredBackground ,
		exportNonVisualLayers ,
		exportAnimations ,
		exportColor;
	
	private final int[] windowSize;
	
	private final ImageGrabber imager;
	
	private final NanoVG nanoVG;
	
	/**
	 * Creates a project exporter which will export a project by the given parameters.
	 * 
	 * @param renderer — the standard renderer of the application 
	 * @param swapBuffersCallback — a callback to swap buffers after rendering
	 * @param project — a project to export
	 * @param exporters — a list of exporters for the project; each will be used when the project is exported
	 * @param exportFolderPath — absolute file path of the folder to export to
	 * @param exportName — name given to exported files
	 * @param nanoVG — the NanoVG object
	 * @param windowSize — size in pixels of the window
	 * @param exportPalettes — if {@code true}, the palettes will be exported alongside images
	 * @param exportHiddenLayers — if {@code true}, layers that are currently hidden will also be exported
	 * @param hideCheckeredBackground — if {@code true}, the checkered backgrounds will be hidden where possible.
	 * @param exportNonVisualLayers — if {@code true}, nonvisual layers will be exported alongside other exported files
	 * @param powerOfTwoSizes — if {@code true}, the width and height of the exported project will be powers of two
	 * @param exportAsColor — if {@code true}, the resulting image is colors, not indices
	 * @param exportAnimations — if {@code true}, the animations of the project will be exported as {@code .ctsa} files
	 */
	public ProjectExporter(
		CSStandardRenderer renderer , 
		Lambda swapBuffersCallback ,
		CSSSProject project , 
		ArrayList<ExportCallbackAndName> exporters ,
		String exportFolderPath ,
		String exportName ,
		NanoVG nanoVG ,
		int[] windowSize ,
		boolean exportPalettes , 
		boolean exportHiddenLayers , 
		boolean hideCheckeredBackground , 
		boolean exportNonVisualLayers ,
		boolean powerOfTwoSizes ,
		boolean exportAsColor ,
		boolean exportAnimations
	) {

		this.renderer = renderer;
		this.project = project;
		this.exporters = exporters;
		this.exportFolderPath = exportFolderPath;
		this.exportName = exportName;
		this.exportPalettes = exportPalettes;
		this.exportHiddenLayers = exportHiddenLayers;
		this.hideCheckeredBackground = hideCheckeredBackground;
		this.exportNonVisualLayers = exportNonVisualLayers;
		this.visualChannels = project.channelsPerPixel();
		this.exportColor = exportAsColor;
		
		this.windowSize = windowSize;
		this.swapBuffersCallback = swapBuffersCallback;
		this.exportAnimations = exportAnimations;
		this.nanoVG = nanoVG;		
		
		framebuffer = renderer.make(() -> {
			
			Framebuffer newFramebuffer = new Framebuffer();
			newFramebuffer.initialize();
			return newFramebuffer;
			
		}).get();
	
		ProjectSizeAndPositions exportInfo = project.getProjectSizeAndPositions();

		if(powerOfTwoSizes) { 
			
			exportWidth = nearestGreaterOrEqualPowerOfTwo(exportInfo.width());
			exportHeight = nearestGreaterOrEqualPowerOfTwo(exportInfo.height());
			
		} else { 
			
			exportWidth = (int) Math.ceil(exportInfo.width());
			exportHeight = (int) Math.ceil(exportInfo.height());
		
		}
					
		exportMidX = exportInfo.midpointX();
		exportMidY = exportInfo.midpointY();
		
		RenderBuffer renderbuffer = renderer.make(() -> {
			
			RenderBuffer newRenderbuffer = new RenderBuffer(exportWidth , exportHeight , GL_RGBA);
			return newRenderbuffer;
			
		}).get();

		imager = new ImageGrabber(framebuffer , renderbuffer , this::render , exportWidth , exportHeight);
		
	}
	
	/**
	 * Exports the project by the parameters given in the constructor.
	 */
	public void export() {
		
		prepareForExport();
		
		exportVisual();
		if(exportNonVisualLayers) exportNonVisualLayers();
		if(exportPalettes) exportPalettes();
		if(exportAnimations) exportAnimations();
		
		restoreFromExport();
		
	}
	
	private void prepareForExport() {
		
		if((hideCheckeredBackground)) renderer.post(() -> {
			
			project.visualPalette().hideCheckeredBackground();
			if(exportNonVisualLayers) project.forEachNonVisualPalette(ArtboardPalette::hideCheckeredBackground);
			
		});
		
	}
	
	private void restoreFromExport() {
		
		renderer.post(() -> {
			
			project.forEachPalette(ArtboardPalette::showCheckeredBackground);
			framebuffer.shutDown();
			Framebuffer.deactivate();
			
			project.arrangeArtboards();

			glViewport(0 , 0 , windowSize[0] , windowSize[1]);
			glClearColor(0.15f , 0.15f , 0.15f , 1.0f);
			
		});
				
	}

	private void exportVisual() {
		
		ByteBuffer download = renderer.make(() -> {

			project.forEachArtboard(artboard -> {
				
				artboard.setToCheckeredBackground();
				artboard.switchToVisualLayers();					
				artboard.showAllNonHiddenVisualLayers();
				
			});
			 
			if(exportHiddenLayers) project.forEachNonShallowCopiedArtboard(artboard -> artboard.forEachVisualLayer(layer -> {
				
				if(layer.hiding()) layer.show(artboard);
				
			}));
			
			//resulting data
			return imager.renderImage();
			
		}).get();
		
		exportDownload(exportName , download , exportColor ? visualChannels : 2);

	}

	private void exportNonVisualLayers() {
		
		project.forEachNonVisualLayerPrototype(prototype -> {
			
			ByteBuffer download = renderer.make(() -> {
							
				//set each artboard to show the current nonvisual layer
				project.forEachNonShallowCopiedArtboard(artboard -> {
					
					artboard.setToCheckeredBackground();
					
					Layer thisLayer = artboard.getLayer(prototype.name());
					artboard.setActiveLayer(thisLayer);
					thisLayer.show(artboard);
					
				});
			
				return imager.renderImage();
			
			}).get();
			
			exportDownload(exportName + " " + prototype.name() , download , exportColor ? prototype.sizeBytes() : 2);
			
		});
		
	}
	
	/**
	 * Exports a single downloaded frame. 
	 * 
	 * @param name — name of the resulting file
	 * @param download — data to export
	 * @param channels — channels per pixel of the download
	 */
	private ExportFinishedAwait exportDownload(String name , ByteBuffer download , int channels) {

		AtomicInteger finishedExporters = new AtomicInteger(0);						
		
		ByteBuffer exportBuffer = channels == 4 ? download : (download = ByteBufferUtils.reformatBufferRedAlpha(download , channels)); 
		
		//pass tasks to the thread pool
		try {

			Engine.THE_THREADS.invokeAll(Set.of(constructExportTasks(name , download , channels , finishedExporters)));
			
		} catch (InterruptedException e) {

			e.printStackTrace();
			
		}		
		
		//once all tasks have completed, free the downloaded memory
		Engine.THE_TEMPORAL.onTrue(() -> finishedExporters.get() == exporters.size() , () -> {
		
			Logging.sysDebug("Finished exporting");
			memFree(exportBuffer);
		
		});
		
		return new ExportFinishedAwait(finishedExporters , exporters.size());
		
	}

	/**
	 * Exports palettes if the user selects the check box for it.
	 */
	private void exportPalettes() {
		
		Engine.THE_THREADS.submit(() -> {
			
			for(ExportCallbackAndName x : exporters) {
				
				exportPalette(project.visualPalette() , x , exportFolderPath , " Visual Palette");
				exportPalette(project.getNonVisualPaletteBySize(1) , x , exportFolderPath , " Nonvisual Palette 1");
				exportPalette(project.getNonVisualPaletteBySize(2) , x , exportFolderPath , " Nonvisual Palette 2");
				exportPalette(project.getNonVisualPaletteBySize(3) , x , exportFolderPath , " Nonvisual Palette 3");
				exportPalette(project.getNonVisualPaletteBySize(4) , x , exportFolderPath , " Nonvisual Palette 4");
								 
			}
			
		});
		
	}

	/**
	 * Exports the given palette with the given exporter.
	 * 
	 * @param palette — palette to export
	 * @param exporter — exporter to use
	 * @param filePath — filepath to export to
	 * @param paletteName — palette name
	 */
	private void exportPalette(ArtboardPalette palette , ExportCallbackAndName exporter , String filePath , String paletteName) {
		
		String totalPath = filePath + paletteName + exporter.extension();		
		exporter.callback().export(totalPath , palette.texelData(), palette.width(), palette.height() , palette.channelsPerPixel());
		
	}
	
	/**
	 * Exports all animations of the project in the same directory as all other exported files. The resulting file format, {@code .ctsa},
	 * is an uncompressed animation file format.
	 */
	private void exportAnimations() {
		
		project.forEachAnimation(animation -> {
			
			CTSAFile ctsaFile = new CTSAFile(animation , project);
			try {
				
				ctsaFile.write(exportFolderPath);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		});
		
	}

	private void render() {
	
		CSOrthographicCamera camera = new CSOrthographicCamera(exportWidth / 2 , exportHeight / 2);
		camera.translate(-exportMidX, -exportMidY , 0);
		CSSSShader shader = exportColor ? CSSSProject.thePaletteShader() : CSSSProject.theTextureShader();
		shader.updatePassVariables(camera.projection(), camera.viewTranslation());

		try(NanoVGFrame frame = nanoVG.frame()) {
			
			project.renderEverything(shader , frame);
		
		}
		
		swapBuffersCallback.invoke();
	
	}
	
	/**
	 * Creates a list of {@code Lambda} to be passed to the thread pool.
	 * 
	 * @param exportName — name of the resulting file
	 * @param download — buffer downloaded from the GPU 
	 * @param channels — number of channels of the download 
	 * @param finishedExporters — atomic counter for completed exports 
	 * @return Array of functions to be passed to the thread pool
	 */
	private Callable<Object>[] constructExportTasks(String exportName , ByteBuffer download , int channels , AtomicInteger finishedExporters) {
		
		@SuppressWarnings("unchecked")
		Callable<Object>[] tasks = new Callable[exporters.size()];
		
		for(int i = 0 ; i < tasks.length ; i++) {
			
			int j = i;
			//set up each export task
			tasks[i] = () -> {
				
				ExportCallbackAndName iter = exporters.get(j); 
				iter.callback().export(exportFolderPath + exportName + iter.extension() , download , exportWidth , exportHeight , channels);
				finishedExporters.incrementAndGet();
				return null;
				
			};
			
		}
		
		return tasks;
		
	}
	
}
