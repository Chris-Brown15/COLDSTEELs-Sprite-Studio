package cs.csss.editor.events;

import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.joml.Vector3f;

import cs.core.utils.Lambda;
import cs.csss.editor.Editor;
import cs.csss.editor.SelectionAreaRender;
import cs.csss.project.Artboard;
import cs.csss.project.IndexPixel;
import cs.csss.project.LayerPixel;
import cs.csss.project.utils.Artboards;
import cs.csss.utils.ByteBufferUtils;
import cs.csss.utils.ByteBufferUtils.CorrectedParameters;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Scales a selected region by the amount the render has been scaled
 */
public class ScaleRegionEvent extends CSSSEvent {

	private final Artboard artboard;
	private final Lambda swapBuffers;
	private final LayerPixel[][] previousRegion;
	private final Editor editor;
	private SelectionAreaRender render;
	private IndexPixel[][] scaledRegion;
	
	private final boolean scaledUp;
	
	private final int
		originalLeftX ,
		originalBottomY ,
		originalWidth ,
		originalHeight ,
		scaledLeftX ,
		scaledBottomY ,
		scaledWidth ,
		scaledHeight;
	
	public ScaleRegionEvent(Artboard artboard , Editor editor , SelectionAreaRender render , Lambda swapBuffers) {
	
		super(true , false);
	
		this.artboard = artboard;
		this.render = render;
		this.swapBuffers = swapBuffers;
		this.editor = editor;
		
		Vector3f scaleVector = new Vector3f();
		render.positions.translation.getScale(scaleVector);
		
		int[] artboardCoords = artboard.worldToPixelIndices( render.positions.leftX() , render.positions.bottomY());
		
		originalLeftX = artboardCoords[0];
		originalBottomY = artboardCoords[1];
		originalWidth = render.width;
		originalHeight = render.height;
		
		int scaledWorldWidth = (int)Math.ceil(render.width * scaleVector.x);
		int scaledWorldHeight = (int)Math.ceil(render.height * scaleVector.y);
		
		int scaledWorldLeftX = (int)render.positions.midX() - (scaledWorldWidth / 2);
		int scaledWorldBottomY = (int) render.positions.midY() - (scaledWorldHeight) / 2;
		
		CorrectedResult corrected = Artboards.worldCoordinatesToCorrectArtboardCoordinates(
			artboard, 
			scaledWorldLeftX ,
			scaledWorldBottomY, 
			scaledWorldWidth ,
			scaledWorldHeight			
		);
	
		Objects.requireNonNull(corrected) ; CorrectedParameters params = corrected.params();
		
		this.scaledLeftX = params.leftX();
		this.scaledBottomY = params.bottomY();
		this.scaledWidth = params.width();
		this.scaledHeight = params.height();
		
		scaledUp = scaledWorldWidth > originalWidth;

		if(scaledUp) previousRegion = artboard.getRegionOfLayerPixels(scaledLeftX , scaledBottomY , scaledWidth, scaledHeight);
		else previousRegion = artboard.getRegionOfLayerPixels(originalLeftX, originalBottomY , originalWidth, originalHeight);
		
	}

	@Override public void _do() {

		if(scaledRegion == null) {
			
			ByteBuffer renderedRegion = render.renderAndDownload(swapBuffers , scaledWidth , scaledHeight , render.positions);
			renderedRegion = ByteBufferUtils.reformatBufferRedGreen(renderedRegion, 2);
			scaledRegion = ByteBufferUtils.bufferToIndices(renderedRegion, scaledWidth, scaledHeight , (x , y) -> x == y && x == 0);
			
			memFree(renderedRegion);
			render.shutDown();
			render = null;
			
			editor.resetViewport();
			
		}
		
		artboard.removePixels(originalLeftX , originalBottomY , originalWidth , originalHeight);
		artboard.putColorsInImage(scaledLeftX , scaledBottomY , scaledWidth , scaledHeight , scaledRegion);

	}

	@Override public void undo() {

		artboard.removePixels(scaledLeftX, scaledBottomY, scaledWidth, scaledHeight);
		if(scaledUp) artboard.putColorsInImage(scaledLeftX, scaledBottomY, scaledWidth, scaledHeight, previousRegion);
		else artboard.putColorsInImage(originalLeftX, originalBottomY, originalWidth, originalHeight, previousRegion);
		
	}

}
