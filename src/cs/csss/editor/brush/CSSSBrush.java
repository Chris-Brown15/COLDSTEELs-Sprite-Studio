package cs.csss.editor.brush;

import static cs.csss.ui.utils.UIUtils.textLength;

import static cs.core.ui.CSUIConstants.HOVERING;
import static cs.core.ui.CSUIConstants.MOUSE_PRESSED;
import static cs.core.ui.CSUIConstants.MOUSE_RIGHT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;

/**
 * Base class for all brushes. Brushes are representations of tools used by artists to modify and introspect on the artboard. Brushes have
 * their own semantics and rules, and this class, {@linkplain cs.csss.editor.brush.CSSSModifyingBrush ModifyingBrush}, and
 * {@link cs.csss.editor.brush.CSSSSelectingBrush SelectingBrush} unify the brush API.
 * 
 * @author Chris Brown
 *
 */
@RenderThreadOnly public abstract class CSSSBrush {

	private static final List<CSSSBrush> allBrushes = new ArrayList<>();
	
	/**
	 * Returns an iterator over all the brushes that have been created.
	 * 
	 * @return � iterator over all the brushes that have been created
	 */
	public static Iterator<CSSSBrush> allBrushes() {
	
		return allBrushes.iterator();
		
	}	
	
	/**
	 * Returns the number of brushes that have been created.
	 * 
	 * @return Number of brushes that have been created.
	 */
	public static int numberBrushes() {
		
		return allBrushes.size();
		
	}
	
	/**
	 * String which is displayed as a tooltip when a brush button is hovered and the tooltip control is pressed.
	 */
	public final String toolTip;
	
	/**
	 * Whether this brush is stateful. Stateful brushes can implement {@link CSSSBrush#update(Artboard, Editor) update(Artboard, Editor)} to
	 * give them much more flexability and power.
	 */
	public final boolean stateful;
		
	/**
	 * Creates a new CSSSBrush which will have the given tooltip, and is stateful if {@code stateful} is <code>true</code>. 
	 * 
	 * @param tooltip � tooltip for this brush
	 * @param stateful � whether this brush is stateful
	 */
	public CSSSBrush(final String tooltip , boolean stateful) {
		
		allBrushes.add(this);
		this.toolTip = tooltip;
		this.stateful = stateful;
		
	}
	
	/**
	 * Given all needed data, this brush will do what it specifies. This method generates an event the Editor will handle.
	 * 
	 * @param artboard � the current artboard
	 * @param editor � the editor
	 * @param xIndex � x index of the pixel selected
	 * @param yIndex � y index of the pixel selected
	 * @return An event representing the operation of this brush.
	 */
	public abstract CSSSEvent use(Artboard artboard , Editor editor , int xIndex , int yIndex);
	
	/**
	 * Determines when this brush can be used. The default implementation will allow the brush to be used whenever the 
	 * {@link cs.csss.core.Control Control#ARTBOARD_INTERACT ARTBOARD_INTERACT} control is pressed, but any condition can be used, and it is
	 * valid for brushes to activate uner other circumstances according to the desired way to use the brush.
	 * 
	 * @param artboard � the current artboard
	 * @param editor � the editor
	 * @param xIndex � x index of the pixel selected
	 * @param yIndex � y index of the pixel selected
	 * @return {@code true} if it is valid to invoke {@code use}.
	 */
	public boolean canUse(Artboard artboard , Editor editor , int xIndex , int yIndex) {
		
		return Control.ARTBOARD_INTERACT.pressed(); 
		
	}

	/**
	 * Override this to implement stateful functionality to a brush.
	 * 
	 * @param artboard � the current artboard, or {@code null} if none is active
	 * @param editor � the editor
	 */
	public void update(Artboard artboard , Editor editor) {
		
		if(!stateful) throw new UnsupportedOperationException("Cannot update a non stateful brush.");
		
	}
	
	/**
	 * Sets up the toolTip associated with this Brush.
	 * 
	 * @param thisBrushsElement � a UI element representing this brush's selector
	 */
	public void setupToolTip(CSElement thisBrushsElement) {
		
		if(thisBrushsElement.toolTip != null) thisBrushsElement.toolTip.shutDown();
		thisBrushsElement.initializeToolTip(HOVERING|MOUSE_PRESSED, MOUSE_RIGHT , 0 , textLength(toolTip));
		thisBrushsElement.toolTip.new CSDynamicRow(20).new CSText(toolTip);
		
	}
	
}
