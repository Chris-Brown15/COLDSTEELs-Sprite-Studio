/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;
import java.util.Stack;

import cs.csss.project.CSSSProject;
import cs.csss.project.utils.Artboards;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * 
 */
public class RasterizeAllShapesEvent extends CSSSEvent {

	private Stack<RasterizeShapeEvent> allEnqueuedEvents = new Stack<>();
	private Stack<RasterizeShapeEvent> allDequedEvents = new Stack<>();
	
	public RasterizeAllShapesEvent(CSSSProject project) {
		
		super(true , false);
	
		Objects.requireNonNull(project);
		project.forEachNonShallowCopiedArtboard(artboard -> artboard.visualLayers().forEachRemaining(layer -> layer.shapesIterator().forEachRemaining(shape -> {

			CorrectedResult correct = Artboards.worldCoordinatesToCorrectArtboardCoordinates(
				artboard ,
				(int)shape.leftX() ,                               
				(int)shape.bottomY(),                              
				shape.textureWidth() ,
				shape.textureHeight()                                     
			);                                                     
				
			allEnqueuedEvents.push(new RasterizeShapeEvent(artboard , shape , correct));
		
		})));
		
	}

	@Override public void _do() {

		swapStackEvents(allEnqueuedEvents, allDequedEvents , true);
		
	}

	@Override public void undo() {

		swapStackEvents(allDequedEvents , allEnqueuedEvents , false);
		
	}

	private void swapStackEvents(Stack<RasterizeShapeEvent> popFrom , Stack<RasterizeShapeEvent> pushTo , boolean _do) {

		while(!popFrom.isEmpty()) {
			
			RasterizeShapeEvent popped = popFrom.pop();
			if(_do) popped._do();
			else popped.undo();
			pushTo.push(popped);
			
		}
	
	}
	
}
