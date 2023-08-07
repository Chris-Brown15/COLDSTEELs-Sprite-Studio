package cs.csss.editor.brush;

import static cs.csss.ui.utils.UIUtils.textLength;

import static cs.core.ui.CSUIConstants.HOVERING;
import static cs.core.ui.CSUIConstants.MOUSE_PRESSED;
import static cs.core.ui.CSUIConstants.MOUSE_RIGHT;

import java.util.Iterator;
import java.util.LinkedList;

import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;
import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.project.Artboard;

/**
 * Base class for all brushes. Brushes are representations of tools used by artists to modify and introspect on the artboard. Brushes have
 * their own semantics and rules, and this class and {@linkplain cs.csss.editor.brushes.CSSSModifyingBrush ModifyingBrush} unify the brush.
 * API.
 * 
 * @author Chris Brown
 *
 */
public abstract class CSSSBrush {

	private static final LinkedList<CSSSBrush> allBrushes = new LinkedList<>();
	
	public static Iterator<CSSSBrush> allBrushes() {
	
		return allBrushes.iterator();
		
	}	
	
	public static int numberBrushes() {
		
		return allBrushes.size();
		
	}
	
	public final String toolTip;
	
	CSSSBrush(final String tooltip){
		
		allBrushes.add(this);
		this.toolTip = tooltip;
		
	}
	
	/**
	 * Given all needed data, this brush will do what it specifies. This method generates an event the Editor will handle.
	 * 
	 * @param artboard — the current artboard
	 * @param editor — the editor
	 * @param xIndex — x index of the pixel selected
	 * @param yIndex — y index of the pixel selected
	 * @return An event representing the operation of this brush.
	 */
	public abstract CSSSEvent use(Artboard artboard , Editor editor , int xIndex , int yIndex);
	
	/**
	 * Used to verify whether this brush should activate or not. This can be used to cull potential generation of events which would have
	 * no affect, such as trying to color a pixel who is already the selected color. If the required computations to avoid a useless event
	 * are more expensive than the required event, this method should just return true.
	 * 
	 * @param artboard — the current artboard
	 * @param editor — the editor
	 * @param xIndex — x index of the pixel selected
	 * @param yIndex — y index of the pixel selected
	 * @return {@code true} if it is wise to invoke {@code go} or if the implementor does not verify, {@code false} otherwise.
	 */
	public boolean canUse(Artboard artboard , Editor editor , int xIndex , int yIndex) {
		
		return true;
		
	}

	/**
	 * Sets up the toolTip associated with this Brush.
	 * 
	 * @param thisBrushsElement — a UI element representing this brush's selector
	 */
	public void setupToolTip(CSElement thisBrushsElement) {
		
		thisBrushsElement.initializeToolTip(HOVERING|MOUSE_PRESSED, MOUSE_RIGHT , 0 , textLength(toolTip));
		thisBrushsElement.toolTip.new CSDynamicRow(20).new CSText(toolTip);
		
	}
	
}
