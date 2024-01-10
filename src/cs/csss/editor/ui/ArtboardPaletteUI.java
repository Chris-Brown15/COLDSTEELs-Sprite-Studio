/**
 * 
 */
package cs.csss.editor.ui;

import static cs.core.ui.CSUIConstants.*;
import static org.lwjgl.nuklear.Nuklear.nk_window_get_content_region;
import static org.lwjgl.opengl.GL11C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glScissor;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSGroup;
import cs.core.utils.ShutDown;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.engine.CSSSCamera;
import cs.csss.project.ArtboardPalette;
import cs.csss.ui.elements.FullAccessCSUI;

import org.lwjgl.nuklear.NkRect;

/**
 * UI menu for containing the artboard palette when it is being rendered.
 */
public class ArtboardPaletteUI implements ShutDown {

	private final CSNuklear nuklear;
	private FullAccessCSUI ui;
	
	private float[] positions = new float[2];
	private float[] dimensions = new float[2];
	private float zoom = 1.0f , xTransform , yTransform;
	private boolean hiding = true;
	
	/**
	 * The camera used to render the palette in this UI.
	 */
	public CSSSCamera camera;
	
	/**
	 * Creates a new artboard palette UI.
	 * 
	 * @param nuklear — the nuklear factory
	 * @param left — the left hand side panel
	 */
	public ArtboardPaletteUI(CSNuklear nuklear , Editor editor , LHSPanel left) {
		
		ui = new FullAccessCSUI(this.nuklear = nuklear, "Current Palette" , 0.203f , -1f , 300 , 300);
		nuklear.removeUserInterface(ui);
		
		int[] windowDims = editor.windowDimensions();
		camera = new CSSSCamera(windowDims[0] , windowDims[1]);
		
		ui.setDimensions(ui.xPosition(), left.topY(), ui.interfaceWidth(), ui.interfaceHeight());
		ui.options = UI_TITLED|UI_BORDERED|UI_MOVABLE|UI_SCALABLE|UI_UNSCROLLABLE;

		CSDynamicRow groupRow = ui.new CSDynamicRow(0.75f);
		CSGroup paletteGroup = groupRow.new CSGroup("UNSEEN");
		paletteGroup.ui.options |= UI_BORDERED|UI_UNSCROLLABLE;
		
		//used to resize the group the palette is rendered in when the window is changed.
		ui.attachedLayout((context , stack) -> {

			NkRect result = NkRect.malloc(stack);
			nk_window_get_content_region(context , result);
			groupRow.rowHeight = (int)(result.h() - 76);
						
		});
		
		paletteGroup.ui.attachedLayout((context , stack) -> {
			
			NkRect result = NkRect.malloc(stack);
			nk_window_get_content_region(context , result);
			positions[0] = result.x();
			positions[1] = result.y();
			dimensions[0] = result.w();
			dimensions[1] = result.h();
			
		});
		
		CSDynamicRow buttonsRow = ui.new CSDynamicRow();
		buttonsRow.new CSButton("Recenter" , () -> {
			
			zoom = 1.0f;
			xTransform = 0;
			yTransform = 0;
			
		});

		buttonsRow.new CSCheckBox("Modifying Palette Directly" , editor::modifyingPaletteDirectly , editor::toggleDirectPaletteModification);
		
		CSDynamicRow slidersRow = ui.new CSDynamicRow();
		slidersRow.new CSFloatProperty("Horizontal" , 1.0f , 1.0f , -333f , 333f , sliderValue -> xTransform = sliderValue , () -> xTransform);
		slidersRow.new CSFloatProperty("Vertical" , 1.0f , 1.0f , -222f , 222f , sliderValue -> yTransform = sliderValue , () -> yTransform);
		
	}
	
	/**
	 * Renders {@code palette} in this UI element, applying a scissor operation so the palette appears inside the UI.
	 * 
	 * @param palette — the palette to render
	 * @param screenHeight — the height of the screen
	 * @param camera — the camera
	 */
	@RenderThreadOnly public void renderPaletteInUI(ArtboardPalette palette , int screenHeight) {

		float midX = camera.XscreenCoordinateToWorldCoordinate(midX());
		float midY = camera.YscreenCoordinateToWorldCoordinate(midY());
		
		scissor(screenHeight);
		palette.render(camera, midX + xTransform, midY + yTransform);
		
		glDisable(GL_SCISSOR_TEST);
		
	}
	
	/**
	 * Enables GL_SCISSOR_TEST and sets the scissor values for this UI element.
	 * 
	 * @param screenHeight — screen height
	 */
	@RenderThreadOnly public void scissor(int screenHeight) {

		glEnable(GL_SCISSOR_TEST);
		glScissor((int)positions[0] , (int)(screenHeight - positions[1] - dimensions[1] - 1), (int)dimensions[0] , (int)dimensions[1] - 1);
		
	}
	
	/**
	 * Returns the dimensions of the artboard palette UI.
	 * 
	 * @return Dimensions of the artboard palette UI.
	 */
	public float[] dimensions() {
		
		return dimensions;
		
	}
	
	/**
	 * Returns the positions of the group that contains the palette.
	 * 
	 * @return The position in screen space of the palette group.
	 */
	public float[] positions() {
		
		return positions;
		
	}
	
	/**
	 * Returns the zoom of the artbaord palette UI.
	 * 
	 * @return Zoom of the UI.
	 */
	public float zoom() {
		
		return zoom;
		
	}
	
	/**
	 * Zooms the panel in or out based on whether {@code out} is <code>true</code>. The rate it is zoomed out is equal to the default value the 
	 * camera will zoom in or out, {@code 0.1f * currentZoom}.  
	 * 
	 * @param out — whether to zoom out or in
	 */
	public void zoom(boolean out) {
		
		camera.zoom(out);
		
	}
	
	/**
	 * Returns the X midpoint of the UI.
	 * 
	 * @return X midpoint of the UI.
	 */
	public float midX() {
		
		return positions[0] + (dimensions[0] / 2);
		
	}

	/**
	 * Returns the Y midpoint of the UI.
	 * 
	 * @return Y midpoint of the UI.
	 */
	public float midY() {
		
		return positions[1] + (dimensions[1] / 2);
		
	}
	
	/**
	 * Translates the palette rendered within the menu by the given translations.
	 * 
	 * @param x — x translation
	 * @param y — y translation
	 */
	public void translate(float x , float y) {
		
		xTransform += x;
		yTransform += y;
		
	}

	/**
	 * Toggles the visibility of this UI menu.
	 */
	public void toggleVisible() {

		hiding = !hiding;
		if(hiding) nuklear.removeUserInterface(ui);
		else nuklear.addUserInterface(ui);
		
	}
	
	/**
	 * Returns whether this UI is currently visible or hiding.
	 * 
	 * @return Whether this UI is currently visible or hiding.
	 */
	public boolean showing() {
		
		return !hiding;
		
	}

	/**
	 * Returns whether the given screen space coordinates are in bounds for the group item containing the render of the palette.
	 *  
	 * @param x — x coordinate to check for being inside this UI
	 * @param y — y coordinate to check for being inside this UI
	 * @return Whether the coordinate described by {@code x, y} is within this UI. 
	 */
	public boolean inBounds(float x , float y) {
		
		return x >= positions[0] && x < positions[0] + dimensions[0] && y >= positions[1] && y < positions[1] + dimensions[1];
		
	}
	
	@Override public void shutDown() {

		if(isFreed()) return;
		
		ui.shutDown();
		nuklear.removeUserInterface(ui);
		ui = null;
		
	}

	@Override public boolean isFreed() {

		return ui == null;
		
	}
	
}