/**
 * 
 */
package cs.csss.editor.brush;

import java.util.Map;
import java.util.function.Supplier;

import cs.csss.editor.ActiveItemBounder;
import cs.csss.editor.Editor;
import cs.csss.editor.SelectionAreaBounder2;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.TransferShapeEvent;
import cs.csss.editor.shape.Rectangle;
import cs.csss.editor.shape.Shape;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;

/**
 * Brush used to create shapes. This brush does not rasterize shapes and does not push any event.
 */
public class ShapesBrush extends CSSSObjectBrush<Shape> {

	private final SelectionAreaBounder2 bounder = new SelectionAreaBounder2();
	private boolean readyToUse = false , isPopupOn = false;
	
	private Shape active = null;
		
	private ClickedSide clickedSide = ClickedSide.NONE;
	
	/**
	 * Creates a new shape brush from the given map of creatable objects.
	 * 
	 * @param initialShapes map of {@code String} identifiers and {@link Supplier}s of {@link Shape} used to create shapes.
	 */
	public ShapesBrush(Map<String , Supplier<Shape>> initialShapes) {

		super("Draws the currently selected shape an artboard" , true , initialShapes);
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		readyToUse = false;
		Shape transfer = active;
		active = null;		
		bounder.reset();
		return new TransferShapeEvent(artboard, transfer);
		
	}

	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
			
		return readyToUse;
		
	}
	
	@Override public void update(Artboard artboard , Editor editor) {
				
		if(artboard == null || !editor.cursorInBoundsForBrush()) { 
			
			bounder.reset();
			return;
			
		}
		
		float[] cursor = editor.cursorCoords();
		
		Control dragNewShape = Control.ARTBOARD_INTERACT , moveExistingShape = Control.MOVE_SELECTION_AREA;
		
		//make a new shape
		if(dragNewShape.pressed() && !dragNewShape.struck()) { 
			
		 	if(artboard.isShallowCopy()) {
		 		
		 		if(!isPopupOn) editor.startDetailedNotification(
		 			"Cannot Create Shape" , 
		 			"This artboard is an alias, which means it is a copy of another artboard.\n" + 
		 			"Shapes can only be created on nonalias artboards.\nPlace the shape on the artboard this artboard aliases." 		 			
		 			, () -> isPopupOn = false
		 		);
		 		
		 		isPopupOn = true;
		 		
		 		return;
		 		
		 		
		 	} else updateDragNewShape(editor, cursor);
			
		}
		//if the user struck and released in a two frame window, active will be null.
		else if(dragNewShape.released() && active != null) readyToUse = true;
		
		//move an existing shape
		if(moveExistingShape.struck()) selectClickedSide(editor , cursor); 
		else if(moveExistingShape.pressed() && clickedSide != ClickedSide.NONE) {
			
			Shape editorActive = editor.activeShape();
			if(editorActive == null) return;

			switch(clickedSide) {
				case LEFT -> moveLeftSide(editor , editorActive , cursor); 
				case TOP -> moveTopSide(editor , editorActive , cursor);
				case RIGHT -> moveRightSide(editor , editorActive , cursor); 
				case BOTTOM -> moveBottomSide(editor , editorActive , cursor);
				case MIDDLE -> middleMover(editor, editorActive, cursor);
				case NONE -> throw new IllegalStateException();			
			}
			
		} else if (moveExistingShape.released()) clickedSide = ClickedSide.NONE;
		
	}
	
	private void updateDragNewShape(Editor editor , float[] cursor) {

		bounder.update(cursor[0] , cursor[1]);
		bounder.castToInts();
		
		int bounderWidth = bounder.width();
		int bounderHeight = bounder.height();
		
		if(bounderWidth > 0 || bounderHeight > 0) { 
			
			if(active == null) active = activeDescriptor().get();
			
			if(bounderWidth > 0) active.shapeWidth(bounderWidth);
			else active.shapeWidth(1);
			
			if(bounderHeight > 0) active.shapeHeight(bounderHeight);
			else active.shapeHeight(1);
			
			int[] mid = bounder.mid();
			active.moveTo(mid[0], mid[1]);
			editor.rendererPost(active::reset);
			
		}
		
		editor.pushNanoRenderCallback(bounder::render);

	}

	private void selectClickedSide(Editor editor , float[] cursor) {

		Shape editorActive = editor.activeShape();
		if(editorActive == null) return;

		ActiveItemBounder bounder = editor.activeShapeBounder();

		if(bounder.inLeftSideMover(cursor[0], cursor[1])) clickedSide = ClickedSide.LEFT;
		else if(bounder.inTopSideMover(cursor[0], cursor[1])) clickedSide = ClickedSide.TOP;
		else if(bounder.inRightSideMover(cursor[0], cursor[1])) clickedSide = ClickedSide.RIGHT;
		else if(bounder.inBottomSideMover(cursor[0], cursor[1])) clickedSide = ClickedSide.BOTTOM;
		else if(bounder.inMiddleMover(cursor[0], cursor[1])) clickedSide = ClickedSide.MIDDLE;
		
	
	}
	
	private void moveLeftSide(Editor editor , Shape editorActive , float[] cursor) {

		ActiveItemBounder bounder = editor.activeShapeBounder();

	 	int cursorX = (int)cursor[0];
	 	int lx = (int)bounder.lx() - 1;
	 	//to the right of lx
		if(cursorX > lx) { 
	 		
	 		editorActive.shapeWidth(editorActive.shapeWidth() - 1);
	 		editor.rendererPost(editorActive::reset);
	 		
	 	} 
		else if (cursorX < lx) { 
	 		
			if(editorActive instanceof Rectangle) {
				
				float midX = editorActive.midX();
				float midY = editorActive.midY();
				editorActive.moveTo(midX - 1, midY);
				editorActive.shapeWidth(editorActive.shapeWidth() + 1);
				editor.rendererPost(editorActive::reset);
			
			} else {
				//2 is added instead of one because of the nature of ellipse radii
				editorActive.shapeWidth(editorActive.shapeWidth() + 2);
				editor.rendererPost(editorActive::reset);
										
			}
	 		
	 	}
	
	}
	
	private void moveTopSide(Editor editor , Shape editorActive , float[] cursor) {

		ActiveItemBounder bounder = editor.activeShapeBounder();
		int ty = (int)bounder.ty();
		
		//cursor is above top y
		if(cursor[1] > ty + 1) {
			
			int addend = editorActive instanceof Rectangle ? 1 : 2;
			editorActive.shapeHeight(editorActive.shapeHeight() + addend);
			editor.rendererPost(editorActive::reset);
			
		} else if (cursor[1] < ty) {

			if(editorActive instanceof Rectangle) {
				
				float midX = editorActive.midX();
				float midY = editorActive.midY();
				editorActive.moveTo(midX , midY - 1);				
				editorActive.shapeHeight(editorActive.shapeHeight() - 1);
				editor.rendererPost(editorActive::reset);
				
			} else {
							
				editorActive.shapeHeight(editorActive.shapeHeight() - 2);
				editor.rendererPost(editorActive::reset);
				
			}
			
		}
		
	}
	
	private void moveRightSide(Editor editor , Shape editorActive , float[] cursor) {

		ActiveItemBounder bounder = editor.activeShapeBounder();

	 	int cursorX = (int)cursor[0];
	 	int rx = (int)bounder.rx();
	 	//to the right of rx
		if(cursorX > rx) { 
			
			int addend = editorActive instanceof Rectangle ? 1 : 2;
	 		
	 		editorActive.shapeWidth(editorActive.shapeWidth() + addend);
	 		editor.rendererPost(editorActive::reset);
	 		
	 	} 
		else if (cursorX < rx) { 

			if(editorActive instanceof Rectangle) {
				
				float midX = editorActive.midX();
				float midY = editorActive.midY();
				editorActive.moveTo(midX - 1, midY);
				editorActive.shapeWidth(editorActive.shapeWidth() - 1);
				editor.rendererPost(editorActive::reset);
			
			} else {
				//2 is added instead of one because of the nature of ellipse radii
				editorActive.shapeWidth(editorActive.shapeWidth() - 2);
				editor.rendererPost(editorActive::reset);
										
			}
	 		
	 	}
	 	
	}
	
	private void moveBottomSide(Editor editor , Shape editorActive , float[] cursor) {

		ActiveItemBounder bounder = editor.activeShapeBounder();

		int by = (int)bounder.by();
		int addend = editorActive instanceof Rectangle ? 1 : 2;

		//moving up
		if(cursor[1] > by ) {

			editorActive.shapeHeight(editorActive.shapeHeight() - addend);
			editor.rendererPost(editorActive::reset);
			
		} 
		else if(cursor[1] < by - 1) {

			if(editorActive instanceof Rectangle) {
				
				float midX = editorActive.midX();
				float midY = editorActive.midY();
				editorActive.moveTo(midX , midY - 1);				
				
			} 
			editorActive.shapeHeight(editorActive.shapeHeight() + addend);
			editor.rendererPost(editorActive::reset);
			
		}
	
	}
	
	private void middleMover(Editor editor , Shape editorActive , float[] cursor) {
		
		if(editorActive instanceof Rectangle) {
			
			editorActive.moveTo((int)cursor[0], (int)cursor[1]);
		
		} else editorActive.moveTo((int)cursor[0], (int)cursor[1] - 2);

		editor.rendererPost(editorActive::reset);
		
	}
	
	private enum ClickedSide {
		
		LEFT , TOP , RIGHT , BOTTOM , MIDDLE , NONE;
		
	}
	
}
