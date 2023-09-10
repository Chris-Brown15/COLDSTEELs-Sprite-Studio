package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.DeleteRegionEvent;
import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;

public class Delete_RegionBrush extends CSSSSelectingBrush {

	private LayerPixel[][] region;
	
	public Delete_RegionBrush() {
		
		super("Deletes the selected region under the current layer.");
		selectionBounder.color = 0xee0000ff;
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		int[] asArtboardCoords = artboard.worldToPixelIndices(selectionBounder.LX() , selectionBounder.BY());
		
		return new DeleteRegionEvent(
			artboard , 
			asArtboardCoords[0] , 
			asArtboardCoords[1] , 
			selectionBounder.width() , 
			selectionBounder.height() ,
			region
		);
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		if(!super.canUse(artboard, editor, xIndex, yIndex)) return false;

		int[] asArtboardCoords = artboard.worldToPixelIndices(selectionBounder.LX() , selectionBounder.BY());
		
		//find out if the selected region has any mods at all
		region = artboard.getRegionOfLayerPixels(
			asArtboardCoords[0] , 
			asArtboardCoords[1] , 
			selectionBounder.width() , 
			selectionBounder.height()
		);
		
		for(LayerPixel[] row : region) for(LayerPixel x : row) if(x != null) return true; 
		
		return false;
		
	}
	
	@Override public void update(Artboard current , Editor editor) {
		
		if(!editor.cursorInBoundsForBrush()) return;
		
		float[] cursorCoords = editor.cursorCoords();
		defaultUpdateBounder((int)cursorCoords[0] , (int)cursorCoords[1]);

		if(current == null) return;
		
		selectionBounder.snapBounderToCoordinates((int)current.leftX(), (int)current.rightX(), (int)current.bottomY(), (int)current.topY());
				
	}

}