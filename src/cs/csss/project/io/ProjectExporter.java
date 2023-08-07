package cs.csss.project.io;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import static cs.csss.utils.NumberUtils.nearestGreaterOrEqualPowerOfTwo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import cs.core.graphics.CSOrthographicCamera;
import cs.core.graphics.CSStandardRenderer;
import cs.core.utils.Lambda;
import cs.csss.engine.Engine;
import cs.csss.misc.graphcs.framebuffer.Framebuffer;
import cs.csss.misc.graphcs.framebuffer.RenderBuffer;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.project.CSSSShader;
import cs.csss.project.Layer;

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
 * 
 * 	<b>TODO:</b>
 * <p>
 * 	All downloaded frames four byte per pixel byte buffers which are converted into the cooresponding type. We should use different render 
 * 	buffers for each format of pixels.
 * </p>
 * 	
 * 
 * @author Chris Brown
 */
public class ProjectExporter {

	/**
	 * Returns whether {@code isBetween} is strictly greater than {@code lower} and strictly less than {@code higher}.
	 * 
	 * @param isBetween — a number whose status as being between {@code lower} and {@code higher} is being queried
	 * @param lower — a value {@code isBetween} must be greater than for {@code true} to be returned 
	 * @param higher — a value {@code isBetween} must be less than for {@code true} to be returned
	 * @return {@code true} if {@code isBetween} is strictly greater than {@code lower} and strictly less than {@code higher}.
	 */
	public static boolean between(int isBetween, int lower , int higher) {
		
		return isBetween > lower && isBetween < higher;
		
	}
	
	/**
	 * Returns whether {@code artboard1} and {@code artboard2} are colliding <b>horizontally</b>.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return {@code true} if the artboards are colliding horizontally.
	 */
	public static boolean collidingX(Artboard artboard1 , Artboard artboard2) {
		
		//two quads are colliding horizontally if one's horizontal positions are between the other's horizontal positions 
		int
			a1LX = (int) artboard1.leftX() ,
			a1RX = (int) artboard1.rightX() ,
			a2LX = (int) artboard2.leftX() ,
			a2RX = (int) artboard2.rightX();
		
		return between(a1LX , a2LX , a2RX) || between(a2LX , a1LX , a1RX);
		
	}

	/**
	 * Returns whether {@code artboard1} and {@code artboard2} are colliding <b>vertically</b>.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return {@code true} if the artboards are colliding vertically.
	 */
	public static boolean collidingY(Artboard artboard1 , Artboard artboard2) {
		
		int
			a1BY = (int) artboard1.bottomY() ,
			a1TY = (int) artboard1.topY() ,
			a2BY = (int) artboard2.bottomY() ,
			a2TY = (int) artboard2.topY();
		
		return between(a1BY , a2BY , a2TY) || between(a2BY , a1BY , a1TY);
		
	}
	
	/**
	 * Returns whether {@code artboard1} and {@code artboard2} are colliding. By colliding, we mean they overlap one another.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return {@code true} if the artboards are colliding both horizontally and vertically.
	 */
	public static boolean colliding(Artboard artboard1 , Artboard artboard2) {
	
		return collidingX(artboard1 , artboard2) && collidingY(artboard1 , artboard2);
		
	}
	
	/**
	 * Returns the distance to translate {@code artboard1} to stop it from colliding with {@code artboard2}. The resulting deltas are 
	 * invalid if the two artboards are not colliding.
	 * 
	 * @param artboard1 — an artboard
	 * @param artboard2 — a second artboard
	 * @return Array containing two integers representing the amount to translate {@code artboard1} to stop it from colliding with 
	 * 		   {@code artboard2}.
	 */
	public static int[] collisionDeltas(Artboard artboard1 , Artboard artboard2) {
		
		int[] deltas = new int[2];
		
		int
			a1LX = (int) artboard1.leftX() ,
			a1RX = (int) artboard1.rightX() ,
			a2LX = (int) artboard2.leftX() ,
			a2RX = (int) artboard2.rightX() ,
			a1BY = (int) artboard1.bottomY() ,
			a1TY = (int) artboard1.topY() ,
			a2BY = (int) artboard2.bottomY() ,
			a2TY = (int) artboard2.topY();
		
		//x delta
		if(between(a1LX , a2LX , a2RX)) deltas[0] = a2RX - a1LX;
		else deltas[0] = a2LX - a1RX;
		
		//y delta
		if(between(a1BY , a2BY , a2TY)) deltas[1] = a2TY - a1BY;
		//negate this to move the result down
		else deltas[1] = -(a1TY - a2BY);
				
		return deltas;
		
	}
	
	private final CSStandardRenderer renderer;
	private final Lambda swapBuffersCallback;
	private final CSSSProject project;
	
	private final Framebuffer framebuffer;	
	private final CSOrthographicCamera camera;
	
	private final String 
		exportFolderPath ,
		visualExportName
	;
	private final ArrayList<ExportCallbackAndName> exporters;
	
	private final int 
		exportWidth ,
		exportHeight ,
		visualChannels
	;
	
	private final boolean
		exportPalettes ,
		exportHiddenLayers ,		
		hideCheckeredBackground ,
		exportNonVisualLayers ,
		powerOfTwoSizes ,
		exportAsColor
	;
	
	ProjectExporter(
		CSStandardRenderer renderer , 
		Lambda swapBuffersCallback ,
		CSSSProject project , 
		ArrayList<ExportCallbackAndName> exporters ,
		String exportFolderPath ,
		String visualExportName ,
		boolean exportPalettes , 
		boolean exportHiddenLayers , 
		boolean hideCheckeredBackground , 
		boolean exportNonVisualLayers ,
		boolean powerOfTwoSizes ,
		boolean exportAsColor
	) {

		this.renderer = renderer;
		this.swapBuffersCallback = swapBuffersCallback;
		this.project = project;
		this.exporters = exporters;
		this.exportFolderPath = exportFolderPath;
		this.visualExportName = visualExportName;
		this.exportPalettes = exportPalettes;
		this.exportHiddenLayers = exportHiddenLayers;
		this.hideCheckeredBackground = hideCheckeredBackground;
		this.exportNonVisualLayers = exportNonVisualLayers;
		this.visualChannels = project.channelsPerPixel(); 
		this.powerOfTwoSizes = powerOfTwoSizes;
		this.exportAsColor = exportAsColor;
		
		framebuffer = renderer.make(() -> {
			
			Framebuffer newFramebuffer = new Framebuffer();
			newFramebuffer.initialize();
			return newFramebuffer;
			
		}).get();
	
		ExportSizeAndPositions exportInfo = getExportSizeAndPositions();

		exportWidth = exportInfo.width();
		exportHeight = exportInfo.height();
					
		camera = new CSOrthographicCamera(exportWidth / 2 , exportHeight / 2);
		camera.translate(-exportInfo.midpointX(), -exportInfo.midpointY() , 0);
		
	}
	
	public void export() {
		
		prepareForExport();
		
		exportVisual();
		if(exportNonVisualLayers) exportNonVisualLayers();
		if(exportPalettes) exportPalettes();
		
		restoreFromExport();
		
	}
	
	private void prepareForExport() {
		
		if((hideCheckeredBackground)) renderer.post(() -> {
			
			project.palette().hideCheckeredBackground();
			if(exportNonVisualLayers) project.forEachNonVisualPalette(ArtboardPalette::hideCheckeredBackground);
			
		});
		
	}
	
	private void restoreFromExport() {
		
		renderer.post(() -> {
			
			project.forEachPalette(ArtboardPalette::showCheckeredBackground);
			framebuffer.shutDown();
			Framebuffer.deactivate();
			
			project.arrangeArtboards();
			
		});
				
	}

	/**
	 * Renders the project into the given renderbuffer and returns a download of that image.
	 * 
	 * @param channels — number of channels to render 
	 * @return {@code ByteBuffer} containing the resulting render, downloaded.
	 */
	private ByteBuffer renderImage(int channels) {
		
		CSSSShader shader = exportAsColor ? CSSSProject.thePaletteShader() : CSSSProject.theTextureShader();
		
		shader.updatePassVariables(camera.projection(), camera.viewTranslation());
		
		int glFormat = GL_RGBA;
		
		//render the scene
		RenderBuffer renderbuffer = initializeRenderBuffer(glFormat);
		framebuffer.activate();
		
		glViewport(0 , 0 , exportWidth , exportHeight);
		glClearColor(0 , 0 , 0 , 0);
		glClear(GL_COLOR_BUFFER_BIT);
		
		project.renderAllArtboards(shader);
		
		swapBuffersCallback.invoke();
				
		//resulting data
		ByteBuffer download = renderbuffer.download(glFormat, GL_UNSIGNED_BYTE);
		
		renderbuffer.shutDown();
		framebuffer.removeColorAttachment(0);
		Framebuffer.deactivate();
		
		return download; 
		
	}

	private void exportVisual() {
		
		ByteBuffer download = renderer.make(() -> {

			project.forEachArtboard(artboard -> {
				
				artboard.setToCheckeredBackground();
				artboard.switchToVisualLayers();					
				artboard.showAllNonHiddenVisualLayers();
				
			});
			
			//users the option to export hidden layers or not. 
			if(exportHiddenLayers) project.forEachNonShallowCopiedArtboard(artboard -> artboard.forEachVisualLayer(layer -> {
				
				if(layer.hiding()) layer.show(artboard);
				
			}));
			
			//resulting data
			return renderImage(visualChannels);
			
		}).get();
		
		exportDownload(visualExportName , download , visualChannels);

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
			
				return renderImage(prototype.sizeBytes());
			
			}).get();
			
			exportDownload(visualExportName + " " + prototype.name() , download , prototype.sizeBytes());
			
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
		
		ByteBuffer exportBuffer = channels == 4 ? download : (download = reformatBuffer(download , channels)); 
		
		//list of export tasks
		Lambda[] exportTasks = constructExportTasks(name , download , channels , finishedExporters);
		
		//pass tasks to the thread pool
		Engine.THE_THREADS.fork(Engine.THE_THREADS.numberThreads() , exportTasks);		
		
		//once all tasks have completed, free the downloaded memory
		Engine.THE_TEMPORAL.onTrue(() -> finishedExporters.get() == exporters.size() ,  () -> memFree(exportBuffer));
		
		return new ExportFinishedAwait(finishedExporters , exporters.size());
		
	}

	/**
	 * Exports palettes if the user selects the check box for it.
	 */
	private void exportPalettes() {
		
		Engine.THE_THREADS.async(() -> {
			
			for(ExportCallbackAndName x : exporters) {
				
				exportPalette(project.palette() , x , exportFolderPath , " Visual Palette");
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
	 * Creates a list of {@code Lambda} to be passed to the thread pool.
	 * 
	 * @param exportName — name of the resulting file
	 * @param download — buffer downloaded from the GPU 
	 * @param channels — number of channels of the download 
	 * @param finishedExporters — atomic counter for completed exports 
	 * @return Array of functions to be passed to the thread pool
	 */
	private Lambda[] constructExportTasks(String exportName , ByteBuffer download , int channels , AtomicInteger finishedExporters) {
		
		Lambda[] tasks = new Lambda[exporters.size()];
		
		for(int i = 0 ; i < tasks.length ; i++) {
			
			int j = i;
			//set up each export task
			tasks[i] = () -> {
				
				ExportCallbackAndName iter = exporters.get(j); 
				iter.callback().export(exportFolderPath + exportName + iter.extension() , download , exportWidth , exportHeight , channels);
				finishedExporters.incrementAndGet();
				
			};
			
		}
		
		return tasks;
		
	}
	
	/**
	 * Computes size and position data needed for exporting.
	 * 
	 * @return Record storing width, height, and midpoint information.
	 */
	private ExportSizeAndPositions getExportSizeAndPositions() {

		//gather information about the state of the objects being saved
		
		//world coordinates notating the extreme points of the project
		int 	
			rightmostX = 0 ,
			leftmostX = Integer.MAX_VALUE ,
			uppermostY = 0 ,
			lowermostY = Integer.MAX_VALUE;
		
	 	Iterator<Artboard> artboards = project.allArtboards();
		
	 	while(artboards.hasNext()) {
			
			Artboard x = artboards.next();
			
			//dont use else if's here because if there is only one artboard, we wont set all values, which we need to do.
			if(x.rightX() > rightmostX) rightmostX = (int) x.rightX();
			if(x.leftX() < leftmostX) leftmostX = (int) x.leftX();
			if(x.topY() > uppermostY) uppermostY = (int) x.topY();
			if(x.bottomY() < lowermostY) lowermostY = (int) x.bottomY();
			
		}
		
		int
			width = rightmostX - leftmostX ,
			height = uppermostY - lowermostY ,				
			midpointX = rightmostX - (width / 2) ,
			midpointY = uppermostY - (height / 2);
		
		if(powerOfTwoSizes) { 
			
			width = nearestGreaterOrEqualPowerOfTwo(width);
			height = nearestGreaterOrEqualPowerOfTwo(height);
			
		} 
		
		return new ExportSizeAndPositions(width , height , midpointX , midpointY);
		
	}

	/**
	 * Creates and initializes a render buffer.
	 * 
	 * @param glFormat — OpenGL format of pixels for the render buffer
	 * @return Initialized render buffer.
	 */
	private RenderBuffer initializeRenderBuffer(int glFormat) {
		
		return renderer.make(() -> {
			
			RenderBuffer renderbuffer = new RenderBuffer(exportWidth , exportHeight , glFormat);			
			framebuffer.addColorAttachment(renderbuffer);
			return renderbuffer;
			
		}).get();
		
	}

	/**
	 * Since the render buffer is hard coded to export rgba pixels, we must reformat the download if we want to correctly export. This is
	 * subject to removal, because it should be possible to use render buffers of different pixel formats within the same shader which 
	 * would already contain correctly formatted pixels.
	 * 
	 * @param source — source {@code ByteBuffer}, assumed to be formatted as RGBA  
	 * @param newDesiredChannels — the number of channels the resulting buffer should contain
	 * @return New buffer containing correctly formatted pixel data.
	 */
	private ByteBuffer reformatBuffer(ByteBuffer source , int newDesiredChannels) {
		
		if(newDesiredChannels == 4) throw new IllegalArgumentException(newDesiredChannels + " is not a valid input.");
		
		ByteBuffer reformat = memAlloc((source.limit() / 4) * newDesiredChannels);
		
		//alpha is at byte 4, color is at byte 1, so the else block's implementation wont work for two byte per pixel
		if(newDesiredChannels == 2) while(reformat.hasRemaining()) {
			
			reformat.put(source.get());
			source.position(source.position() + 2);
			reformat.put(source.get());
			
		} else {
			
			int difference = 4 - newDesiredChannels;
			while(reformat.hasRemaining()) {
				
				for(int i = 0 ; i < newDesiredChannels ; i ++) reformat.put(source.get());
				source.position(source.position() + difference);
				
			}

		}
		
		memFree(source);
		
		return reformat.flip();
		
	}
	
}
