package cs.csss.editor.event;

import java.util.Objects;

import cs.csss.editor.shape.Shape;
import cs.csss.engine.LookupPixel;
import cs.csss.project.Artboard;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Event used for rasterizing shapes.
 */
public class RasterizeShapeEvent extends CSSSEvent {

	private final Artboard artboard;
	private final Shape shape;
	private final LookupPixel[][] previousRegion;
	private final CorrectedResult correct;
	
	/**
	 * Creates a new rasterize shape event.
	 *  
	 * @param destination arthboard to put {@code shape} in
	 * @param shape shape to rasterize
	 * @param correct corrected parameters for the resulting put operation of this event
	 */
	public RasterizeShapeEvent(Artboard destination , Shape shape , CorrectedResult correct) {
		
		super(true , false);
		
		this.artboard = Objects.requireNonNull(destination);
		this.shape = Objects.requireNonNull(shape);

		this.correct = correct; 
		if(correct != null) previousRegion = artboard.getRegionOfLayerPixels(correct);
		else { 
			
			previousRegion = null;
			shape.hide();
			
		}
		
	}

	@Override public void _do() {

		shape.reset();
		if(correct != null) shape.rasterize(artboard);
		
	}

	@Override public void undo() {

		if(correct != null) artboard.replace(correct, previousRegion);
		shape.hide(false);		
		
	}

}
