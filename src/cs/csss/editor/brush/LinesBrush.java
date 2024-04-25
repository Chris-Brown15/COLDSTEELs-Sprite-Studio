/**
 * 
 */
package cs.csss.editor.brush;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import org.joml.Vector2f;

import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.NewControlPointEvent;
import cs.csss.editor.event.TransferLineEvent;
import cs.csss.editor.line.BezierLine;
import cs.csss.editor.line.Line;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;

/**
 * Brush for various lines.
 */
public class LinesBrush extends CSSSObjectBrush<Line> {

	private boolean canUse = false;
	private byte endpoint = -1;
	private int controlPoint = -1;
	
	public LinesBrush(Map<String, Supplier<Line>> initialCreatableObjects) {

		super("Brush for creating various types of lines." , true , initialCreatableObjects);

	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		canUse = false;
		TransferLineEvent event = new TransferLineEvent(artboard, editor.activeLine());
		return event;
		
	}

	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return canUse;
		
	}
	
	@Override public void update(Artboard artboard, Editor editor) {
		
		float[] cursor = editor.cursorCoords();

		Line activeLine = editor.activeLine();
		
		if(artboard == null || !editor.cursorInBoundsForBrush() || !artboard.isCursorInBounds(cursor)) {
			
			endpoint = -1;
			controlPoint = -1;
			return;
			
		}
		
		Control dragNewLine = Control.ARTBOARD_INTERACT;
		Control moveExistingLine = Control.MOVE_SELECTION_AREA;
		
		int[] cursorToArtboardCoords = artboard.worldToPixelIndices(cursor);
				
		//drag out a new line
		if(dragNewLine.pressed()) {
			
			if(Control.ARTBOARD_INTERACT.struck()) {
				
				editor.activeLine(activeLine = activeDescriptor().get());
				canUse = true;
				
				activeLine.setEndpoint1(artboard, cursorToArtboardCoords[0] , cursorToArtboardCoords[1]);
				activeLine.setEndpoint2(artboard, cursorToArtboardCoords[0] , cursorToArtboardCoords[1]);
				
			} else {
				
				activeLine.setEndpoint2(artboard, cursorToArtboardCoords[0] , cursorToArtboardCoords[1]);
				activeLine.reset(artboard);
				
			}
			
		} 
		//grab endpoints and move them around
		else if (moveExistingLine.struck()) {
			
			Iterator<Line> lines = artboard.lines();
			
			endpoint = -1;
			
			//search for cursor being over end points.
			while(lines.hasNext()) {
				
				Line x = lines.next();
				
				if(x.endpoint1X() == cursorToArtboardCoords[0] && x.endpoint1Y() == cursorToArtboardCoords[1]) { 
					
					editor.activeLine(x);
					endpoint = 1;
					break;
					
				} else if(x.endpoint2X() == cursorToArtboardCoords[0] && x.endpoint2Y() == cursorToArtboardCoords[1]) { 
					
					editor.activeLine(x);
					endpoint = 2;
					break;
					
				}
				else if (editor.activeLineBounder().inMiddleMover(cursor[0], cursor[1])) {
					
					endpoint = -2;
					break;
					
				}
				//find control points or make a new one
				else if (x instanceof BezierLine asBezier) {
					
					int i = 0;					
					for(Iterator<Vector2f> controlPoints = asBezier.controlPoints() ; controlPoints.hasNext() ; i++) {
						
						Vector2f next = controlPoints.next();
						if((int)next.x() == cursorToArtboardCoords[0] && (int)next.y() == cursorToArtboardCoords[1]) {
							
							editor.activeLine(x);
							controlPoint = i;
							//set to negative one when a control point was found
							i = -1;
							break;
							
						}
						
					}
					
					//if control point not found
					if(i != -1 && editor.activeLine() == x) {
						
						editor.eventPush(new NewControlPointEvent(artboard , asBezier , cursorToArtboardCoords[0], cursorToArtboardCoords[1]));
						controlPoint = i;
						
					}
										
				}
				
			}
			
		} else if (moveExistingLine.pressed()) {
			
			if(endpoint == 1) {
				
				activeLine.setEndpoint1(artboard, cursorToArtboardCoords[0] , cursorToArtboardCoords[1]);
				activeLine.reset(artboard);
				
			} else if(endpoint == 2) {
				
				activeLine.setEndpoint2(artboard, cursorToArtboardCoords[0] , cursorToArtboardCoords[1]);
				activeLine.reset(artboard);
				
			} else if (endpoint == -2) {
				
				editor.activeLine().moveTo(artboard, cursorToArtboardCoords[0] , cursorToArtboardCoords[1]);
				
			} else if(controlPoint != -1) {
				
				BezierLine asBezier = (BezierLine)activeLine;
				asBezier.controlPoint(controlPoint).set(cursorToArtboardCoords[0] , cursorToArtboardCoords[1]);
				asBezier.reset(artboard);
				
			}
			
		}

		if(artboard != null && activeLine != null) drawEndpointBoxes(artboard, editor, activeLine);

	}
	
	private void drawEndpointBoxes(Artboard artboard , Editor editor , Line active) {
		
		editor.pushNanoRenderCallback(nanoFrame -> {
			
			float worlde1x = artboard.artboardXToWorldX(active.endpoint1X());
			float worlde1y = artboard.artboardYToWorldY(active.endpoint1Y());
			float worlde2x = artboard.artboardXToWorldX(active.endpoint2X());
			float worlde2y = artboard.artboardYToWorldY(active.endpoint2Y());
			
			nanoFrame
				.newPath()
				.rectangle(worlde1x, worlde1y , 1, 1)
				.strokeColor(0xff00ff)
				.stroke()
				.closePath();

			nanoFrame
				.newPath()
				.rectangle(worlde2x, worlde2y, 1, 1)
				.strokeColor(0xff00ff)
				.stroke()
				.closePath();
			
			//draw control points
			if(active instanceof BezierLine asBezier) {
				
				for(Iterator<Vector2f> controls = asBezier.controlPoints() ; controls.hasNext() ; ) {
				
					Vector2f x = controls.next();
					
					nanoFrame
						.newPath()
						.rectangle(artboard.artboardXToWorldX((int)x.x()) , artboard.artboardYToWorldY((int)x.y()) , 1 , 1)						
						.strokeColor(0xff0000ff)
						.stroke()
						.closePath();
					
				}
								
			}
			
		});
		
	}
	
}