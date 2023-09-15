package cs.csss.editor.brush;

import cs.coreext.nanovg.NanoVGFrame;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.SelectionAreaBounder;
import cs.csss.editor.SelectionAreaRender;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.project.LayerPixel;

/**
 * Brush unifying the API for all brushes who utilize {@link cs.csss.editor.SelectionAreaBounder SelectionAreaBounder} and 
 * {@link cs.csss.editor.SelectionAreaRender SelectionAreaRender} to provide functionality for brushes that modify regions of pixels in
 * various ways.
 * <p>
 * 	Such brushes are inherently stateful so they are always marked as such.
 * </p>
 */
@RenderThreadOnly public abstract class CSSSSelectingBrush extends CSSSBrush { 

	/**
	 * Object that is used to visualize a region of an artboard apart from the artboard itself.
	 */
	public static volatile SelectionAreaRender render;

	/**
	 * Renders an extender of {@code CSRender} used to display the contents of an artboard that are within the selection area bounder.
	 */
	public static void renderSelectionRegion() {
		
		if(render != null) render.draw();
		
	}
	
	protected final SelectionAreaBounder selectionBounder = new SelectionAreaBounder();
	
	/**
	 * Creates a new selecting brush with the given tooltip.
	 * 
	 * @param tooltip — a tooltip for this brush
	 */
	public CSSSSelectingBrush(String tooltip) {
		
		super(tooltip , true);
		
	}

	/**
	 * Moves a corner of the bounder of this selector by dragging whatever corner is closest to the given coordinates
	 * 
	 * @param cursorWorldX — x coordinate of the cursor
	 * @param cursorWorldY — y coordinate of the cursor
	 */
	public final void updateBounder(int cursorWorldX , int cursorWorldY) {
		
		selectionBounder.moveCorner(cursorWorldX, cursorWorldY);
				
	}

	/**
	 * Renders the bounder of the selection bruhs.
	 * 
	 * @param frame — the NanoVG frame for rendering
	 */
	public final void renderBounder(NanoVGFrame frame) {
		
		selectionBounder.render(frame);
		
	}

	/**
	 * Creates a new render for the selected region.
	 * 
	 * @param current — the current artboard
	 * @param editor — the editor
	 * @param cursor — int array containing the cursor's positions
	 */
	protected void newRender(Artboard current , Editor editor , int[] cursor) {
		
		int leftX = selectionBounder.LX(); 
	 	int bottomY = selectionBounder.BY();
		int width = selectionBounder.width();
		int height = selectionBounder.height();
		
		//creates the render for the subregion. A better solution would be to create a render from current's index texture directly 
		//and scissor out the unneeded parts. Then, we would download only the region of the buffer we want much like we do here
		int[] texelCoords = current.worldToPixelIndices(leftX, bottomY);

		LayerPixel[][] regionContents = current.activeLayer().get(texelCoords[0], texelCoords[1], width, height);

		if(render != null) render.shutDown();

		var shader = CSSSProject.thePaletteShader(); 
		var palette = editor.currentPalette();
		
		render = new SelectionAreaRender(
			shader , 
			leftX , 
			bottomY , 
			width , 
			height , 
			cursor[0] , 
			cursor[1] , 
			regionContents , 
			palette , 
			editor.camera()
		);
		
	}

	/**
	 * Creates a new render for the selected region.
	 * 
	 * @param current — the current artboard
	 * @param editor — the editor
	 */
	protected void newRender(Artboard current , Editor editor) {
		
		float[] midpoint = selectionBounder.midpoint();
		int[] asInts = {(int)midpoint[0] , (int)midpoint[1]};
		newRender(current , editor , asInts);		
		
	}

	/**
	 * Default implementation for moving the selection bounder. 
	 *  
	 * @param x — world x coordinate
	 * @param y — world y coordinate
	 */
	public final void defaultUpdateBounder(int x , int y) {
		
		if(Control.MOVE_SELECTION_AREA.pressed()) updateBounder(x, y);
		
	}
	
}
