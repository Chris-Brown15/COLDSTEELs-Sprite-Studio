package cs.csss.editor.event;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

import cs.core.utils.Lambda;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.SelectionAreaBounder;
import cs.csss.editor.SelectionAreaRender;
import cs.csss.project.Artboard;
import cs.csss.project.IndexPixel;
import cs.csss.project.LayerPixel;
import cs.csss.utils.ByteBufferUtils;
import cs.csss.utils.ByteBufferUtils.CorrectedParameters;

/**
 * Rotates a given region along its midpoint. 
 * <p>
 * 	The current implementation of this class uses a framebuffer and renders a rotated quad of the region, then downloads the render and 
 * 	places it in the artboard. This is ok for large objects but for small ones many artifacts arise. This implementation is subject to 
 * 	change.
 * </p>
 */
@RenderThreadOnly public class RotateRegionEvent extends CSSSEvent {

	private final Artboard artboard;
	private final Editor editor;
	private final Lambda swapBuffersCallback;
	private SelectionAreaRender render;
	private IndexPixel[][] rotatedRegion;
	private LayerPixel[][] 
		//region of pixels that are inside the original bounder
		previousOriginalRegion ,
		//region of pixels that are inside the rotated region
		previousRotatedRegion;
	
	private final int 
		renderWidth , 
		renderHeight ,
		leftX , 
		bottomY ,
		originalLeftX ,
		originalBottomY ,
		originalBounderWidth ,
		originalBounderHeight;
	
	/**
	 * Creates a rotate region event.
	 * 
	 * @param artboard — an artboard to contain the rotation
	 * @param editor — the editor
	 * @param render — render object who will be rendered to a framebuffer and put into {@code artboard}
	 * @param bounder — the selection bounder for the original region
	 * @param swapBuffersCallback — callback containing code used to swap buffers
	 */
	public RotateRegionEvent(
		Artboard artboard , 
		Editor editor , 
		SelectionAreaRender render , 
		SelectionAreaBounder bounder , 
		Lambda swapBuffersCallback
	) {
		
		super(true , false);
		
		this.artboard = artboard;
		this.editor = editor;
		this.render = render;
		this.swapBuffersCallback = swapBuffersCallback;

		float[] dims = dimensionsOfBoundingBoxOverRotation(render , bounder); 
		int renderWidth = (int)Math.round(dims[0]);
		int renderHeight = (int)Math.round(dims[1]);
				
		int prelimLeftX = (int) (dims[2] - (bounder.width() / 2));
		int prelimBottomY = (int) (dims[3] - (bounder.height() / 2));
		int[] indices = artboard.worldToPixelIndices(prelimLeftX , prelimBottomY);
		int leftX = indices[0];
		int bottomY = indices[1];
		
		CorrectedParameters corrected = ByteBufferUtils.correctifyIndices(artboard, leftX, bottomY, renderWidth, renderHeight).params();
		this.leftX = corrected.leftX();
		this.bottomY = corrected.bottomY();
		this.renderWidth = corrected.width();
		this.renderHeight = corrected.height();
		
		indices = artboard.worldToPixelIndices(bounder.LX() , bounder.BY());
		originalLeftX = indices[0];
		originalBottomY = indices[1];
		originalBounderWidth = bounder.width();
		originalBounderHeight = bounder.height();

		previousOriginalRegion = artboard.getRegionOfLayerPixels(
			originalLeftX, 
			originalBottomY, 
			originalBounderWidth , 
			originalBounderHeight
		);
	
		previousRotatedRegion = artboard.getRegionOfLayerPixels(this.leftX, this.bottomY, this.renderWidth, this.renderHeight);
		
	}

	@Override public void _do() {
		
		artboard.removePixels(originalLeftX, originalBottomY, originalBounderWidth , originalBounderHeight);
		
		//only happens the first time the event is invoked.
		if(rotatedRegion == null) {
				
			ByteBuffer renderContents = render.renderAndDownload(
				swapBuffersCallback , 
				renderWidth , 
				renderHeight , 
				render.positions.midX() , 
				render.positions.midY()
			);
			
			renderContents = ByteBufferUtils.reformatBufferRedGreen(renderContents, 2);			
			rotatedRegion = ByteBufferUtils.bufferToIndices(renderContents , renderWidth , renderHeight , (x , y) -> x == 0 && y == 0);
			memFree(renderContents);
			render.shutDown();
			render = null;
			
			editor.resetViewport();
						
		}
		
		artboard.putColorsInImage(leftX, bottomY, renderWidth , renderHeight, rotatedRegion);
		
	}

	@Override public void undo() {

		artboard.removePixels(leftX, bottomY , renderWidth , renderHeight);
		artboard.putColorsInImage(originalLeftX, originalBottomY, originalBounderWidth , originalBounderHeight, previousOriginalRegion);
		artboard.putColorsInImage(leftX, bottomY, renderWidth, renderHeight, previousRotatedRegion);
				
	}

	/**
	 * Gets the width and height of the rectangle who bounds the non axis aligned region.
	 * <p>
	 * 	Implementation based on: 
	 * 		<a href="https://stackoverflow.com/questions/622140/calculate-bounding-box-coordinates-from-a-rotated-rectangle">
	 * 			Stack Overflow answer by Ryan Peschel
	 * 		</a>
	 * </p>
	 * 
	 * @param render — selection area render which should be rotated
	 * @param bounder — selection bounder containing the render
	 * @return Dimensions of the bounding box who bounds the non axis aligned region.
	 */
	static float[] dimensionsOfBoundingBoxOverRotation(SelectionAreaRender render , SelectionAreaBounder bounder) {
		
		float angle = render.rotationAngle();
		
		float cosAngle = (float) Math.abs(Math.cos(angle));
		float sinAngle = (float) Math.abs(Math.sin(angle));
		
		float width = (render.width * cosAngle) + (render.height * sinAngle);
		float height = (render.width * sinAngle) + (render.height * cosAngle);
	
		float originalMidX = render.positions.midX();
		float originalMidY = render.positions.midY();
		float newMidX = originalMidX - (width - bounder.width()) / 2;
		float newMidY = originalMidY - (height - bounder.height()) / 2;
		
		return new float[] {width , height , newMidX , newMidY};
		
	}
	
}
