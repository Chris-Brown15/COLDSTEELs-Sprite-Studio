package cs.csss.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.joml.Vector2f;

import cs.core.CSDisplay;
import cs.core.graphics.CSRender;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.utils.Lambda;
import cs.core.utils.ShutDown;
import cs.core.utils.threads.Await;
import cs.core.utils.threads.ConstructingAwait;
import cs.coreext.nanovg.NanoVG;
import cs.coreext.nanovg.NanoVGFrame;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.brush.CSSSBrush;
import cs.csss.editor.brush.CSSSModifyingBrush;
import cs.csss.editor.brush.CSSSSelectingBrush;
import cs.csss.editor.brush.Copy_RegionBrush;
import cs.csss.editor.brush.Delete_RegionBrush;
import cs.csss.editor.brush.EraserBrush;
import cs.csss.editor.brush.Eye_DropperBrush;
import cs.csss.editor.brush.Flood_FillBrush;
import cs.csss.editor.brush.Move_RegionBrush;
import cs.csss.editor.brush.PencilBrush;
import cs.csss.editor.brush.Replace_AllBrush;
import cs.csss.editor.brush.RotateBrush;
import cs.csss.editor.brush.Scale_RegionBrush;
import cs.csss.editor.brush.Select_ArtboardBrush;
import cs.csss.editor.event.AnonymousEvent;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.ModifyPaletteDirectEvent;
import cs.csss.editor.event.NOPEvent;
import cs.csss.editor.event.TogglePaletteReferenceModeEvent;
import cs.csss.editor.palette.AnalogousPalette;
import cs.csss.editor.palette.ComplementaryPalette;
import cs.csss.editor.palette.MonochromaticPalette;
import cs.csss.editor.ui.AnimationPanel;
import cs.csss.editor.ui.ArtboardPaletteUI;
import cs.csss.editor.ui.FilePanel;
import cs.csss.editor.ui.LHSPanel;
import cs.csss.editor.ui.RHSPanel;
import cs.csss.engine.CSSSCamera;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.Control;
import cs.csss.engine.Engine;
import cs.csss.engine.Logging;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.Pixel;
import cs.csss.engine.TransformPosition;
import cs.csss.project.Animation;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.project.IndexPixel;
import cs.csss.project.VisualLayer;
import cs.csss.ui.utils.UIUtils;

/**
 * Editor handles tasks relating to modifying artboards. It functions on a largely event-driven architecture where implementations of 
 * {@link cs.csss.editor.brush.CSSSBrush CSSSBrush} emit events which this class will receive and invoke. 
 * 
 * @author Chris Brown
 *
 */
public class Editor implements ShutDown {
		
	static final int DEFAULT_UNDO_REDO_STACK_SIZE = 1024;
		
	//create the brushes used by all editors
	public static final Select_ArtboardBrush theArtboardSelector = new Select_ArtboardBrush();
	public static final PencilBrush thePencilBrush = new PencilBrush();
	public static final EraserBrush theEraserBrush = new EraserBrush();
	public static final Eye_DropperBrush theEyeDropper = new Eye_DropperBrush();
	public static final Flood_FillBrush theFloodFill = new Flood_FillBrush();
	public static final Replace_AllBrush theReplaceAllBrush = new Replace_AllBrush();
	public static final Move_RegionBrush theRegionSelect = new Move_RegionBrush();
	public static final Delete_RegionBrush theDeleteRegion = new Delete_RegionBrush();
	public static final RotateBrush theRotateBrush = new RotateBrush();
	public static final Scale_RegionBrush theScaleBrush = new Scale_RegionBrush();
	public static final Copy_RegionBrush theCopyBrush = new Copy_RegionBrush();
	private static volatile CSSSBrush theScriptBrush2 = null;
	private static volatile CSSSModifyingBrush theModifyingScriptBrush2 = null;
	private static volatile CSSSSelectingBrush theSelectingScriptBrush2 = null;

	/**
	 * Returns the current script brush.
	 * 
	 * @return Current script brush.
	 */
	public static CSSSBrush theScriptBrush2() {
		
		return theScriptBrush2;
		
	}

	/**
	 * Returns the current modifying script brush.
	 * 
	 * @return Current modifying script brush.
	 */
	public static CSSSModifyingBrush theModifyingScriptBrush2() {
		
		return theModifyingScriptBrush2;
		
	}

	/**
	 * Returns the current modifying script brush.
	 * 
	 * @return Current selecting script brush.
	 */
	public static CSSSSelectingBrush theSelectingScriptBrush2() {
		
		return theSelectingScriptBrush2;
		
	}
	
	public final IntConsumer setCameraMoveRate;
	public final IntSupplier getCameraMoveRate;
	
	private final Engine engine;
	private final JythonScriptExecutor jython = new JythonScriptExecutor(Engine.isDebug());
		
	private volatile CSSSBrush currentBrush = thePencilBrush;
	
	private ConcurrentLinkedDeque<CSSSEvent> events = new ConcurrentLinkedDeque<>();
	private UndoRedoStack redos = new UndoRedoStack(DEFAULT_UNDO_REDO_STACK_SIZE) , undos = new UndoRedoStack(DEFAULT_UNDO_REDO_STACK_SIZE);
	
	private final AnimationPanel animationPanel;
	private final LHSPanel leftSidePanel;
	private final ArtboardPaletteUI paletteUI;
	
	private ChannelBuffer currentColor = new ChannelBuffer();
	
	private boolean colorInputsAreHex = false;
	
	//NanoVG rectangle used to approximately show where the user will modify with their next action if they are using a modification brush of some 
	//kind.
	private final SelectionAreaBounder modifyingBounder = new SelectionAreaBounder();
	
	private final SelectionAreaBounder paletteBounder = new SelectionAreaBounder();
	//Separate indices than the ones stored in artboard palettes. These are used by the editor for its modifications to the palette.
	private int paletteXIndex = 0 , paletteYIndex = 0;
	
	/*
	 * modifying palette directly refers to whether we are currently able to modify the palette image.
	 * 
	 * referencing palette directly refers to whether we are currently using paletteXIndex and paletteYIndex to choose what color's indices to put
	 * in the palette.
	 */
	private boolean referencingPaletteDirectly = false , modifyingPaletteDirectly = false;
	
	private LookupPixel currentColorIndices;
	
	/**
	 * Creates an editor object. 
	 * 
	 * @param engine — the engine of this application
	 * @param display — display for the application
	 */
	public Editor(Engine engine , CSDisplay display) {
		
		this.engine = engine;
		
		setCameraMoveRate = engine::cameraMoveRate;
		getCameraMoveRate = engine::cameraMoveRate;

		leftSidePanel = new LHSPanel(this , display.nuklear);
		paletteUI = new ArtboardPaletteUI(display.nuklear , this , leftSidePanel);
		new FilePanel(this , display.nuklear);
		new RHSPanel(this , display.nuklear , engine);
		animationPanel = new AnimationPanel(this , display.nuklear);

		new MonochromaticPalette(15);
		new AnalogousPalette(15);
		new ComplementaryPalette(6);
		
		display.window.onWindowResize((newWidth , newHeight) -> paletteUI.camera.resetProjection(newWidth, newHeight));
		
	}

	/**
	 * Called once per update to run the program's editor.
	 */
	public void update() {

		updateCurrentBrush();
		//add new events
		editArtboardOnControls();
		
		updateEditorControls();
		//updates the small rectangle that identifies which pixel in the palette panel we are currently modifying
		updatePaletteEdits();		
		//handle them
		handleEvents();	
		//update the current project's animation if running
		playAnimation();
	
	}
	
	/**
	 * Pushes an event on the undo queue and the list of all events.
	 * 
	 * @param event — any event
	 */
	public void eventPush(CSSSEvent event) {
		
		events.add(event);

		if(event.isTransientEvent) events.add(shutDownEventReflection(event)); 
		else {
			
			CSSSEvent removed = undos.push(event);
			if(removed == null) return;
			eventPush(shutDownEventReflection(removed));
			
		}

	}

	/**
	 * Handles any events associated with this editor.
	 */
	void handleEvents() {

		for(CSSSEvent x : events) {

			Lambda _do = Engine.isDebug() ? () -> {
				
				try {
					
					x._do();
				
				} catch(Exception e) {
					
					Logging.syserr("Event excepted " + x);
					e.printStackTrace();
					
				}
				
			} : x::_do;
			
			if(x.isRenderEvent) engine.renderer().post(_do);
			else x._do();
			
		}
		
		events.clear();
		
	}
	
	private void editArtboardOnControls() {

		CSSSProject project = project();
		
		if(project == null) return;
		
		boolean cursorInBoundsForBrush = cursorInBoundsForBrush();
		float[] cursor = engine.getCursorWorldCoords();			

		Artboard current = project.currentArtboard();
		
		if(project.freemoveMode() && Control.ARTBOARD_INTERACT.struck()) {
			
			if(current == null) project.setCurrentArtboardByMouse(cursor[0], cursor[1]); 
			else project.currentArtboard(null);
			return;		
			
		}
		
		if(!cursorInBoundsForBrush || currentBrush == null) return;
		
		if(Control.ARTBOARD_INTERACT.pressed()) current = setCurrentArtboard(cursor); 
		
		if(current != null && current.isCursorInBounds(cursor)) {
		
			int[] pixelIndex = current.worldToPixelIndices(cursor);
			
			Artboard finalCurrent = current;
			
			//TODO: make these only go to the renderer if needed
			engine.renderer().post(() -> {

				if(Engine.isDebug()) try {
					
					boolean canUse = currentBrush.canUse(finalCurrent , this , pixelIndex[0] , pixelIndex[1]);
					if(canUse) eventPush(currentBrush.use(finalCurrent , this , pixelIndex[0] , pixelIndex[1]));
					
				} catch(Exception e) {
					
					Logging.syserr("Brush _do failed for brush " + currentBrush);
					e.printStackTrace();
					
				} else {
					
					boolean canUse = currentBrush.canUse(finalCurrent , this , pixelIndex[0] , pixelIndex[1]);
					if(canUse) eventPush(currentBrush.use(finalCurrent , this , pixelIndex[0] , pixelIndex[1]));
				
				}
				
			}).await();
						
		}

	}

	/**
	 * Handles undo and redo based on the state of the undo and redo keys.
	 */
	private void updateEditorControls() {

		if(Control.PRELIM.pressed()) if(Control.PRELIM2.pressed()) {
			
			if(Control.UNDO.pressed()) undo();
			else if (Control.REDO.pressed()) redo();
		
		} else {
			
			if(Control.UNDO.struck()) undo();
			else if (Control.REDO.struck()) redo();
		
		}
		
		if(Control.TRANSLATE_CAMERA.pressed()) {

			float previousX = engine.previousCursorX();
			float previousY = engine.previousCursorY();
			float[] current = engine.getCursorWorldCoords();
			
			float xTranslation = current[0] - previousX;
			float yTranslation = current[1] - previousY;
			
			engine.camera().translate(xTranslation, yTranslation);
			//this code can be used to translate the views of the animation panel and palette panel, but its janky
//			if(!engine.isCursorHoveringUI()) 
//			else {
//			
//				if(cursorHoveringAnimationFramePanel()) animationPanel.translate(xTranslation, yTranslation);
//				else if (cursorHoveringPaletteUI()) paletteUI.translate(xTranslation, yTranslation);
//				
//			}
						
		}
		
		if(currentBrush instanceof CSSSModifyingBrush asModifying) {
			
			int radius = asModifying.radius();
			if(Control.DECREASE_BRUSH_SIZE.struck()) asModifying.radius(radius + 1);
			if(Control.INCREASE_BRUSH_SIZE.struck() && radius > 0) asModifying.radius(radius - 1);
			
		}
				
	}

	/**
	 * Updates the current brush if it is a stateful brush.
	 */
	private void updateCurrentBrush() {
		
		if(currentBrush == null) return;
		
		if(currentBrush.stateful) {
			
			CSSSProject project = project();
			rendererPost(() -> {
				
				if(Engine.isDebug()) {
					
					try {
						
						currentBrush.update(project == null ? null : project.currentArtboard() , this);
					
					} catch(Exception e) {
						
						Logging.syserr("Brush update failed for brush " + currentBrush);
						e.printStackTrace();
													
					}
					
				} else currentBrush.update(project == null ? null : project.currentArtboard() , this);
			
			}).await();
		
		}
		
		Artboard current = currentArtboard();
		if(currentBrush instanceof CSSSModifyingBrush modifingBrush && current != null) {
			
			//update the bounder
			float[] cursorPosition = engine.getCursorWorldCoords();
			int radius = modifingBrush.radius();

			modifyingBounder.LX(cursorPosition[0] - radius -.5f);
			modifyingBounder.RX(cursorPosition[0] + radius +.5f);
			modifyingBounder.BY(cursorPosition[1] - radius -.5f);
			modifyingBounder.TY(cursorPosition[1] + radius +.5f);
			
		}
			
	}
	
	private void updatePaletteEdits() {
		
		ArtboardPalette current = currentPalette();
		
		if(!paletteUI.showing() || current == null) return;
		
		//sets the position of the bounder that highlights the current pixel
		
		float untranslatedX = current.position().leftX() + paletteXIndex;
		float untranslatedY = current.position().bottomY() + paletteYIndex;
		
		paletteBounder.LX(untranslatedX);
		paletteBounder.RX(untranslatedX + 1);
		paletteBounder.BY(untranslatedY);
		paletteBounder.TY(untranslatedY + 1);
			
		if(!Control.ARTBOARD_INTERACT.pressed()) return;
		
		CSSSCamera camera = paletteUI.camera;
		
		//select a position on the palette.
		
		int[] cursorScreenCoordinates = engine.getCursorScreenCoords();
		
		if(!paletteUI.inBounds((float)cursorScreenCoordinates[0] , (float)cursorScreenCoordinates[1])) return;
		
		float[] asWorld = {
			camera.XscreenCoordinateToWorldCoordinate(cursorScreenCoordinates[0]) , 
			camera.YscreenCoordinateToWorldCoordinate(cursorScreenCoordinates[1])
		};
		
		int[] indices = current.worldCoordinateToPixelIndices(asWorld);
		//if the cursor is within the palette texture itself
		if(indices[0] >= 0 && indices[1] >= 0 && indices[0] < current.width() && indices[1] <= current.height()) {
			
			paletteXIndex = indices[0];
			paletteYIndex = indices[1];			
			
		}
		
		if(referencingPaletteDirectly) {
			
		 	if(modifyingPaletteDirectly) { 
		 		
		 		//logic is similar to using a brush, we check if we "can use" this event by seeing if the event will do anything else, then we 
		 		//propperly do the event.
		 		rendererPost(() -> {
		 		
		 			ColorPixel inPalette = current.get(paletteXIndex, paletteYIndex);
		 			if(inPalette.compareTo(currentColor) != 0) { 
		 				
		 				eventPush(new ModifyPaletteDirectEvent(current , paletteXIndex , paletteYIndex , currentColor));
		 				
		 			}

		 		});
		 		
		 	}
		 	
			currentColorIndices = new IndexPixel(paletteXIndex , paletteYIndex);
			
		}
		
	}
	
	/**
	 * Invokes the most recent event's undo code.
	 */
	public void undo() {
		
		undos.undo(engine.renderer() , redos);
		
	}
	
	/**
	 * Redoes the most recently undone event's code.
	 */
	public void redo() {
		
		redos.redo(engine.renderer() , undos);
		
	}

	/**
	 * Gets the project's current animation and attempts to play it, only happening if the animation is in the playing state.
	 */
	void playAnimation() {
	
		Animation current = engine.currentAnimation();
		if(current != null && current.playing()) { 
			
			engine.realtimeMode(true);
			current.update();
			
		} else engine.realtimeMode(false);
		
	}
	
	/**
	 * Renders the bounder of the current brush if the current brush is a selecting brush.
	 * 
	 * @param frame — a NanoVG frame to render with
	 */
	@RenderThreadOnly public void renderSelectingBrushBounder(NanoVGFrame frame) {
		
		if(currentBrush instanceof CSSSSelectingBrush x) x.renderBounder(frame); 
		
	}
	
	/**
	 * Draws the rendered region for the selection region.
	 */
	@RenderThreadOnly public void renderSelectingBrushRender() {
		
		if(currentBrush instanceof CSSSSelectingBrush) CSSSSelectingBrush.renderSelectionRegion();
		
	}
	
	/**
	 * Renders the frame of the modifying brush bounder iff the current brush is a modifying brush.
	 * 
	 * @param frame — the NanoVG frame
	 */
	@RenderThreadOnly public void renderModifyingBrushBounder(NanoVGFrame frame) {
		
		if(!engine.isCursorHoveringUI() && currentBrush instanceof CSSSModifyingBrush && currentArtboard() != null) modifyingBounder.render(frame);
		
	}
	
	/**
	 * Renders the palette bounder, the square that identifies the currently active palette pixel for when palette modification is current.
	 * 
	 * @param frame — the NanoVG frame
	 */
	@RenderThreadOnly public void renderPaletteBounder(NanoVGFrame frame) {
		
		ArtboardPalette currentPalette = currentPalette();
		if(!paletteUI.showing() || currentPalette == null) return;

		/*
		 * Here we render the small square that identifies what the current pixel is when we are rendering the palette/modifying it directly.
		 * We first apply the zoom of the UI element. 
		 * Then we find out whether the square is out of bounds of the UI element
		 * If it is, we undo the zoom and return, otherwise we render it and undo the zoom.
		 * 
		 * glScissor doesn't work at culling the box if it is out of bounds. I don't know why.
		 */
		
		TransformPosition palettePosition = currentPalette.position();
		
		NanoVG nano = engine.nanoVG();
		nano.converter().camera(paletteUI.camera);

		Vector2f bounderInScreenSpace = nano.worldToScreen(palettePosition.leftX() + paletteXIndex , palettePosition.bottomY() + paletteYIndex);
		
		if(!paletteUI.inBounds(bounderInScreenSpace.x, bounderInScreenSpace.y)) {
					
			nano.converter().camera(camera());
			return;
			
		}
		
		paletteBounder.render(frame);
		nano.converter().camera(camera());
				
	}
	
	/**
	 * Returns the current brush.
	 * 
	 * @return The current brush.
	 */
	public CSSSBrush currentBrush() {
		
		return currentBrush;
		
	}
		
	/**
	 * Sets the current brush to {@link cs.csss.annotation.Nullable @Nullable} {@code brush}.
	 * 
	 * @param brush — a {@code @Nullable} brush to make active
	 */
	public void setBrushTo(CSSSBrush brush) {
		
		this.currentBrush = brush;
		
	}
	
	/**
	 * Gets the {@link cs.csss.annotation.Nullable @Nullable} current artboard, which is {@code null} if no artboard is active. 
	 * 
	 * @return The {@code @Nullable} current artboard, which can be null.
	 */
	public Artboard currentArtboard() {
	
		CSSSProject currentProject = engine.currentProject();
		if(currentProject != null) return currentProject.currentArtboard();
		return null;
		
	}
	
	/**
	 * Returns a {@code Pixel} which contains <em>either</em> channel values or lookup values depending upon the current state of the editor. If you
	 * invoke this method, you need to use {@code instanceof} to check whether the received pixel is a lookup pixel or a color pixel.
	 *  
	 * @return Abstract {@code Pixe} containing values for the active color.
	 */
	public Pixel currentColor() {
		
		return referencingPaletteDirectly() ? selectedColorIndices() : selectedColorValues();
		
	}
	
	/**
	 * Returns a {@link cs.csss.project.ArtboardPalette.PalettePixel PalettePixel} containing the colors of the currently selected color in
	 * the color picker in the left hand side panel or the color chosen from.
	 * 
	 * @return A created palette pixel.
	 */
	public Pixel selectedColorValues() {
		
		return currentColor; 
		
	}

	/**
	 * Returns the lookup pixel containing the indices for the active color. 
	 * 
	 * @return A lookup pixel containing the indices of the active color.
	 */
	public Pixel selectedColorIndices() {
		
		return currentColorIndices;
		
	}
	
	/**
	 * Returns the undo stack's capacity.
	 * 
	 * @return The undo stack's capacity.
	 */
	public int undoCapacity() {
		
		return undos.capacity();
		
	}
	
	/**
	 * Returns the redo stack's capacity.
	 * 
	 * @return The redo stack's capacity.
	 */
	public int redoCapacity() {
		
		return redos.capacity();
		
	}
	
	/**
	 * Sets the capacity of both the undo and redo stack.
	 * 
	 * @param size — new capacity for the stacks
	 */
	public void setUndoAndRedoCapacity(int size) {
		
		redos = new UndoRedoStack(size);
		undos = new UndoRedoStack(size);
		
	}
	
	/**
	 * Sets the active colors of the color picker in the left hand side panel to {@code pixel}.
	 * 
	 * @param pixel — a new color to be selected in the left hand side panel
	 */
	public void setLHSSelectedColor(final ColorPixel pixel) {
		
		leftSidePanel.setColor(pixel);
	
	}

	/**
	 * Sets the active colors of the color picker in the left hand side panel to {@code pixel}.
	 * 
	 * @param r — red channel of the new color
	 * @param g — green channel of the new color
	 * @param b — blue channel of the new color
	 * @param a — alpha channel of the new color
	 */
	public void setLHSSelectedColor(byte r , byte g , byte b , byte a) {
		
		leftSidePanel.setColor(r , g , b,  a);
	
	}
	
	/**
	 * Sets the color the editor considers to be the current color value.
	 * 
	 * @param other — source for the channel values of the pixel now considered to be the selected color
	 */
	public void setSelectedColor(ColorPixel other) {
		
		byte zero = (byte)0;
		switch(engine.currentProject().getChannelsPerPixelOfCurrentLayer()) {
			case 1 -> setSelectedColor(other.r() , zero , zero , zero);
			case 2 -> setSelectedColor(other.r() , other.g() , zero , zero);
			case 3 -> setSelectedColor(other.r() , other.g() , other.b() , zero);
			case 4 -> setSelectedColor(other.r() , other.g() , other.b() , other.a());
		}
				
	}
	
	public void setSelectedColor2(Pixel pixel) {
		
		if(pixel instanceof ColorPixel asColor) {
			
			setSelectedColor(asColor.r() , asColor.g() , asColor.b() , asColor.a());
			leftSidePanel.setColor(asColor.r() , asColor.g() , asColor.b() , asColor.a());
			
		} else if (pixel instanceof LookupPixel asLookup) {
			
			ArtboardPalette current = currentPalette();
			if(current == null) return;
			
			ColorPixel color = current.get(asLookup.lookupX() , asLookup.lookupY());

			setSelectedColor(color.r() , color.g() , color.b() , color.a());
			leftSidePanel.setColor(color.r() , color.g() , color.b() , color.a());
		
			paletteXIndex = asLookup.lookupX() ; paletteYIndex = asLookup.lookupY();
			
		}
		
	}
	
	/**
	 * Sets the color the editor considers to be the current color value.
	 * 
	 * @param r — red channel for the new selected color
	 * @param g — green channel for the new selected color
	 * @param b — blue channel for the new selected color
	 * @param a — alpha channel for the new selected color
	 */
	public void setSelectedColor(byte r , byte g , byte b , byte a) {
		
		ArtboardPalette current = currentPalette();
		if(current == null) return;
		currentColor.set(r, g, b, a);
				
	}
	
	/**
	 * Returns the current project, which can be {@code null} is none is present.
	 * 
	 * @return The current project if one exists, or {@code null}.
	 */
	public CSSSProject project() {
	
		return engine.currentProject();
		
	}

	void setCurrentProject(CSSSProject project) {
		
		engine.currentProject(project);

	}
	
	/* OPERATIONS */
	
	/**
	 * Creates a project immediately, saving the time of going through menus.
	 * @throws DebugDisabledException 
	 */
	public void requestDebugProject() throws DebugDisabledException {

		if(!Engine.isDebug()) throw new DebugDisabledException(this);
		
		engine.renderer().post(() -> {
			
			CSSSProject project = new CSSSProject(engine , "debug proj" , 4);
			project.initialize();			
			project.createVisualLayer("Layer 1");
			project.createVisualLayer("Layer 2");
			project.createNonVisualLayer("Nonvisual" , 2);			
			project.createAnimation("Default Animation");			
			project.createArtboard(100 , 100);					
			setCurrentProject(project);
			
		});
	
	}

	/**
	 * Pushes some code to the render thread.
	 * 
	 * @param code — code to push to the render thread
	 * @return {@link cs.core.utils.threads.Await Await} for the task given.
	 */
	public Await rendererPost(Lambda code) {
		
		return engine.renderer().post(code);
		
	}
	
	/**
	 * Creates an object in the render thread, returning a {@link cs.core.utils.threads.ConstructingAwait ConstructingAwait} who will 
	 * contain the object when it is finished.
	 * 
	 * @param <T> — type of object to make 
	 * @param constructor — code who produces an instance of {@code T}.
	 * @return {@code ConstructingAwait} of {@code T}.
	 */
	public <T> ConstructingAwait<T> rendererMake(Supplier<T> constructor) {
		
		return engine.renderer().make(constructor);
		
	}

	/**
	 * Toggles full screen mode of the application of and on.
	 */
	public void toggleFullscreen() {
		
		engine.toggleFullScreen();
		
	}
	
	/* UI ELEMENTS */
	
	/**
	 * Creates a UI element for creating a new project.
	 */
	public void startNewProject() {

		engine.startNewProject();
		
	}

	/**
	 * Creates a UI element for creating a new animation.
	 */
	public void startNewAnimation() {
		
		engine.startNewAnimation();
		
	}

	/**
	 * Creates a UI element for creating a new visual layer.
	 */
	public void startNewVisualLayer() {
		
		engine.startNewVisualLayer();
		
	}

	/**
	 * Creates a UI element for creating a new nonvisual layer.
	 */
	public void startNewNonVisualLayer() {
		
		engine.startNewNonVisualLayer();
		
	}

	/**
	 * Creates a UI element for creating a new artboard.
	 */
	public void startNewArtboard() {
		
		engine.startNewArtboard();
		
	}

	/**
	 * Creates a UI element for editing controls.
	 */
	public void startEditingControls() {
		
		engine.startNewControlsEditor();
		
	}

	/**
	 * Creates a UI element for editing the settings of the checkered background.
	 */
	public void startCheckeredBackgroundSettings() {
		
		engine.startCheckeredBackgroundSettings();
		
	}
	
	/**
	 * Creates a UI element for setting a custom time for an animation frame.
	 * 
	 * @param index — index of an animation frame
	 */
	public void startAnimationFrameCustomTimeInput(int index) {
		
		engine.startAnimationFrameCustomTimeInput(index);
		
	}

	/**
	 * Creates an UI element for setting the simulation frame rate for real time mode.
	 * <p>
	 * 	This is used for animations that want to tie their animation frame time to game updates. The simulation frame rate decides how many
	 * 	frames the application allows to happen. The greater the number, the more frames will be produced, so animation frames may need 
	 * 	greater amounds of frames.
	 * </p>
	 */
	public void startSetSimulationFrameRate() {
	
		engine.startSetSimulationFrameRate();
		
	}	
	
	/**
	 * Creates an UI element for uploading an item to the Steam Workshop.
	 */
	public void startSteamWorkshopItemUpload() {
		
		engine.startSteamWorkshopItemUpload();
		
	}
	
	/**
	 * Creates an UI element for updating an item on the Steam Workshop.
	 */
	public void startSteamWorkshopItemUpdate() {
		
		engine.startSteamWorkshopItemUpdateMenu();
		
	}
	
	/**
	 * Creates an UI element for setting the swap type of a frame at the given index.
	 * 
	 * @param index — index of a frame of an animation whose swap time is being adjusted
	 */
	public void startSetAnimationFrameSwapType(int index) {
		
		engine.startSetAnimationFrameSwapType(index);
		
	}

	/**
	 * Creates an UI element for setting the rank of a visual layer.
	 * 
	 * @param layer — visual layer whose rank is being changed
	 */
	public void startMoveLayerRankEvent(VisualLayer layer) {
		
		engine.startMoveLayerRank(layer);
		
	}
	
	/**
	 * Creates an UI element for setting the index of a particular animation frame.
	 * 
	 * @param originalIndex — the index of the frame to move
	 */
	public void startSetAnimationFramePosition(int originalIndex) {
		
		engine.startSetAnimationFramePosition(originalIndex);
		
	}
	
	/**
	 * Creates an UI element for exporting the current project.
	 */
	public void startExport() {
		
		engine.startExport();
		
	}

	/**
	 * Creates an UI element for creating a text box.
	 */
	public void startAddText() {
		
		engine.startAddText();
		
	}
	
	/**
	 * Creates a UI for customizing the style of the UI.
	 */
	public void startUICustomizer() {
		
		engine.startUICustomizer();
		
	}
	
	/**
	 * Creates a UI for selecting a theme to set for the UI.
	 */
	public void startSelectUITheme() {
	
		engine.startSelectUITheme();
	
	}
	
	/**
	 * Returns the animation panel. 
	 * 
	 * @return The animation panel.
	 */
	public AnimationPanel animationPanel() {
				
		return animationPanel;
		
	}
	
	/**
	 * Returns whether the cursor's coordinates are within the animation frame panel.
	 * 
	 * @return {@code true} if the cursor given by the coordinates is within the animation panel's frame slot.
	 */
	public boolean cursorHoveringAnimationFramePanel() {

		int[] frameCorner = animationPanel.topLeftPointOfAnimationFrameSlot();
		int[] frameDimensions = animationPanel.dimensionsOfAnimationFrameSlot();
		
		return isCursorInBounds(frameCorner[0] , frameCorner[1] , frameDimensions[0] , frameDimensions[1]);
		
	}
	
	/**
	 * Returns whether the cursor is hovering the palette UI.
	 * 
	 * @return Whether the cursor is hovering the palette UI.
	 */
	public boolean cursorHoveringPaletteUI() {
		
		float[] frameCorner = paletteUI.positions();
		float[] frameDimensions = paletteUI.dimensions();
		
		return isCursorInBounds((int)frameCorner[0] , (int)frameCorner[1] , (int) frameDimensions[0] , (int) frameDimensions[1]);
		
	}
	
	private boolean isCursorInBounds(int boundLeftX , int boundTopY , int boundWidth , int boundHeight) {
		
		int[] cursor = engine.getCursorScreenCoords();
		int windowHeight = engine.windowSize()[1];
		//these two steps transform the cursor Y and the frame corner Y so that 0 would be the bottom of the window rather than the top. 
		boundTopY = windowHeight - boundTopY - boundHeight;
		cursor[1] = windowHeight - cursor[1];
				
		return boundLeftX <= cursor[0] && boundLeftX + boundWidth > cursor[0] && boundTopY <= cursor[1] && boundTopY + boundHeight > cursor[1];
				
	}
	
	/**
	 * Returns the max radius a brush can have.
	 * 
	 * @return Max radius a brush can have.
	 */
	public int maxBrushRadius() {
		
		return currentArtboard() != null ? (currentArtboard().height() / 2) - 1 : 999;
		
	}
	
	/**
	 * Toggles on or off the animation panel.
	 */
	public void toggleAnimationPanel() {
		
		animationPanel.toggleShow();
		
	}
	
	/**
	 * Adds a render object to the renderer.
	 * 
	 * @param render — a fully initialized render object
	 */
	public void addRender(CSRender render) {
		
		engine.renderer().addRender(render);
		
	}
	
	/**
	 * Returns whether the animation panel is showing.
	 * 
	 * @return Whether the animation panel is showing.
	 */
	public boolean isAnimationPanelShowing() {
		
		return animationPanel.showing();
		
	}
	
	/**
	 * Returns whether the given animation is the current animation.
	 * 
	 * @param animation — an animation
	 * @return {@code true} if the current animation is {@code animation}.
	 */
	public boolean isCurrentAnimation(Animation animation) {
		
		return engine.currentProject().currentAnimation() == animation;
		
	}
	
	/**
	 * Returns whether the Steam API was initialized.
	 * 
	 * @return Whether the Steam API was initialized.
	 */
	public boolean isSteamInitialized() {
		
		return engine.isSteamInitialized();
		
	}
	
	/**
	 * Creates a name for an artboard to display in UIs.
	 * 
	 * @param artboard — an artboard
	 * @return A string containing the name of the artboard and some other contents depending on the nature of the artboard.
	 */
	public String getArtboardUIName(Artboard artboard) {
		
		if(project().isCopy(artboard)) return "Artboard " + artboard.name + " Alias";
		else return "Artboard " + artboard.name;
		
	}
	
	private Artboard setCurrentArtboard(float[] cursor) {
		
		//grant that the cursor is in a valid position
		
		CSSSProject project = project();
		if(project == null) return null;
		
		Artboard oldCurrent = project.currentArtboard();
		
		project.setCurrentArtboardByMouse(cursor[0] , cursor[1]);
		
		Artboard current = project.currentArtboard();
		
		if(project.freemoveMode()) {
			
			boolean sameAnimation = false;
			
			for(Iterator<Animation> animations = project.animations() ; animations.hasNext() ; ) { 
				
				Animation x = animations.next();
				
				if(x.hasArtboard(oldCurrent) && x.hasArtboard(current)) { 
					
					sameAnimation = true;
					break;
										
				}
				
			}			
			
			if(current == oldCurrent || sameAnimation) {
				
				project.currentArtboard(null);
				current = null;
				
			}
		
		}
		
		return current;
		
	}
	
	/**
	 * Instructs the engine to begin shut down.
	 */
	public void exit() {
		
		engine.exit();
		
	}
	
	/**
	 * Saves the project to disk.
	 */
	public void saveProject() {
		
		engine.saveProject();
		
	}
	
	/**
	 * Starts saving the project as a new file.
	 */
	public void startProjectSaveAs() {
		
		engine.startProjectSaveAs();
		
	}

	/**
	 * Starts loading a project from disk.
	 */
	public void startLoadProject() {
		
		engine.startLoadProject();
		
	}

	/**
	 * Gets and returns the world coordinates of the cursor.
	 * 
	 * @return World coordinates of the cursor.
	 */
	public float[] cursorCoords() {
		
		return engine.getCursorWorldCoords();
		
	}
	
	/**
	 * Returns the current palette of the current artboard, that is, the palette used for coloring the current layer.
	 * 
	 * @return Palette for the current artboard, or {@code null} if it cannot be retrieved.
	 */
	public ArtboardPalette currentPalette() {
		
		CSSSProject project = project();
		if(project == null) return null;
		return project.currentPalette();
		
	}
	
	/**
	 * Returns whether the cursor is in a state such that it actually can be used. This can be used in brush {@code update} methods to make
	 * sure it is actually OK to use the brush.
	 * 
	 * @return {@code true} if the conditions are met to use a brush.
	 */
	public boolean cursorInBoundsForBrush() {
		
		return !engine.wasMousePressedOverUI() && !engine.isCursorHoveringUI();
		
	}
	
	/**
	 * Returns the camera of the program.
	 * 
	 * @return Camera of the progrma.
	 */
	public CSSSCamera camera() {
		
		return engine.camera();
		
	}
	
	/**
	 * Swaps the current framebuffer.
	 */
	public void swapBuffers() {
		
		engine.windowSwapBuffers();
		
	}
	
	/**
	 * Resets the viewport and clear color to their defaults.
	 */
	public void resetViewport() {
		
		engine.resetViewport();
		
	}

	/**
	 * Starts a new menu to select and run an artboard script. Once one is selected, it is pushed as an event.
	 */
	public void startRunArtboardScript2() {
		
		if(currentArtboard() == null) return;
		engine.startSelectScriptMenu("artboards", file -> {
			
			jython.registerArtboardScript(file);			
			jython.runArtboardScript(this, file.getName());
			
		});
		
	}
	
	/**
	 * Starts a new menu to select and run a project script. Once one is selected, it is pushed as an event.
	 */
	public void startRunProjectScript2() {
		
		if(project() == null) return;
		engine.startSelectScriptMenu("projects", file -> {
			
			jython.registerProjectScript(file);
			jython.runProjectScript(this , file.getName());
			
		});
		
	}
	
	/**
	 * Starts a new menu to select a palette script. Once one is selected, it is run, creating a new palette.
	 */
	public void startRunPaletteScript2() {
	
		engine.startSelectScriptMenu("palettes", file -> {
			
			jython.registerPaletteScript(file);
			jython.getPalette(this, file);
			
		});
		
	}
	
	/**
	 * Starts a menu to select the current simple script brush.
	 * 
	 * @param radio — the radio button whose tooltip will be updated to the selected brush's tooltip
	 */
	public void startSelectSimpleScriptBrush2(CSRadio radio) {
		
		engine.startSelectScriptMenu("simple brushes", file -> {
			
			jython.registerSimpleBrushScript(file);
			theScriptBrush2 = jython.getSimpleBrush(this , file);
			if(theScriptBrush2 != null) UIUtils.toolTip(radio, jython.getSimpleBrushInfo(file.getName()).tooltip());
			
		});
		
	}

	/**
	 * Starts a menu to select the current modifying script brush.
	 * 
	 * @param radio — the radio button whose tooltip will be updated to the selected brush's tooltip
	 */
	public void startSelectModifyingScriptBrush2(CSRadio radio) {
		
		engine.startSelectScriptMenu("modifying brushes", file -> {
			
			jython.registerModifyingBrushScript(file);
			theModifyingScriptBrush2 = jython.getModifyingBrush(this , file);
			if(theModifyingScriptBrush2 != null) UIUtils.toolTip(radio, jython.getModifyingBrushInfo(file.getName()).tooltip());
			
		});
		
	}

	/**
	 * Starts a menu to select the current selecting script brush.
	 * 
	 * @param radio — the radio button whose tooltip will be updated to the selected brush's tooltip
	 */
	public void startSelectSelectingScriptBrush2(CSRadio radio) {
		
		engine.startSelectScriptMenu("selecting brushes", file -> {
			
			jython.registerSelectingBrushScript(file);
			theSelectingScriptBrush2 = jython.getSelectingBrush(this , file);
			if(theSelectingScriptBrush2 != null) UIUtils.toolTip(radio, jython.getSelectingBrushInfo(file.getName()).tooltip());
			
		});
		
	}
	
	/**
	 * Creates a UI menu for creating a new script.
	 */
	public void startCreateNewScript() {
		
		engine.startCreateNewScript();
		
	}
	
	/**
	 * Renders the artboard palette in the UI element if possible.
	 */
	public void renderPalette() {
		
		if(!paletteUI.showing()) return;
		ArtboardPalette currentPalette = currentPalette();
		if(currentPalette == null) return;
		paletteUI.renderPaletteInUI(currentPalette, engine.windowSize()[1]);
		
	}
		
	void startScriptArgumentInput(String scriptName, Optional<String> dialogueText , Consumer<String> onFinish) {
		
		engine.startScriptArgumentInput(scriptName, dialogueText, onFinish);
		
	}
	
	String asScriptName(EventScriptMeta eventScriptMeta) {
		
		return asScriptName(eventScriptMeta.scriptName());
		
	}
	
	String asScriptName(BrushScriptMeta brushScriptMeta) {
		
		return asScriptName(brushScriptMeta.scriptName());
		
	}
	
	String asScriptName(String string) {
		
		return string.substring(0, string.length() - 3);
		
	}
	
	/* DEBUG */
	
	/**
	 * Toggles real time mode on or off
	 * 
	 * @throws DebugDisabledException if the application is not in debug mode.
	 */
	public void toggleRealtime() throws DebugDisabledException {
		
		if(!Engine.isDebug()) throw new DebugDisabledException(this);
		
		engine.realtimeMode(!engine.realtimeMode());
		
	}

	/**
	 * Returns whether color inputs are as hex or decimal 
	 *  
	 * @return Whether color inputs are as hex or decimal.
	 */
	public boolean colorInputsAreHex() {
		
		return colorInputsAreHex;
		
	}
	
	/**
	 * Toggles the state of whethers color inputs are as hex or decimal.
	 */
	public void toggleColorInputsAreHex() {
		
		colorInputsAreHex = !colorInputsAreHex; 
		
	}
	
	/**
	 * Gets the current pallete and returns its contents as a list. However, if no palette is active for any reason, <code>null</code> is returned.
	 * 
	 * @return A {@link List} containing the colors stored in the current palette, or <code>null</code> if none is active.
	 */
	public List<ColorPixel> currentPaletteColorsAsList() {
		
		CSSSProject project = engine.currentProject();
		if(project != null) {
			
			ArtboardPalette currentPalette = project.currentPalette();
			if(currentPalette != null) return currentPalette.getColorsAsList(15);
			
		}
		
		return null;
		
	}

	/**
	 * Returns the artboard palette UI.
	 * 
	 * @return The artboard palette UI.
	 */
	public ArtboardPaletteUI artboardPaletteUI() {
		
		return paletteUI;
		
	}
	
	/**
	 * Toggles whether the palette is being referenced directly.
	 */
	public void togglePaletteReferenceMode() {
		
		eventPush(new TogglePaletteReferenceModeEvent(this));
		
		
	}
	
	/**
	 * Returns whether direct palette access is enabled.
	 * 
	 * @return Whether direct palette access is enabled.
	 */
	public boolean referencingPaletteDirectly() {
		
		return referencingPaletteDirectly;
		
	}
	
	/**
	 * Returns whether the palette is being modified directly.
	 *  
	 * @return {@code true} if the palette modification mode is currently enabled.
	 */
	public boolean modifyingPaletteDirectly() {
		
		return modifyingPaletteDirectly;
		
	}
	
	/**
	 * Toggles the state of whether direct access to the palette is enabled.
	 */
	public void toggleDirectPaletteAccess() {
		
		setReferencingPaletteDirectly(!referencingPaletteDirectly);
		
	}
	
	/**
	 * Toggles the state of the direct palette access mode.
	 */
	public void toggleDirectPaletteModification() {
		
		setModifyingPaletteDirectly(!modifyingPaletteDirectly);
		
	}

	/**
	 * Sets whether the editor is in direct palette access mode.
	 * 
	 * @param nowReferencing — whether the UI is now being modified
	 */
	public void setReferencingPaletteDirectly(boolean nowReferencing) {
		
		this.referencingPaletteDirectly = nowReferencing;
		ArtboardPalette current = currentPalette();
		if(current != null && nowReferencing) {
			
			paletteXIndex = current.currentCol();
			paletteYIndex = current.currentRow();
			
		}
		
	}
	
	/**
	 * Sets the state of whether the palette is being modified directly.
	 * 
	 * @param state — whether the palette is being modified directly
	 */
	public void setModifyingPaletteDirectly(boolean state) {
		
		modifyingPaletteDirectly = state;
				
	}
	
	/**
	 * Returns the dimensions of the window.
	 *  
	 * @return Dimensions of the window.
	 */
	public int[] windowDimensions() {
		
		return engine.windowSize();
		
	}
		
	/**
	 * Uses reflection to detect whether a shutdown method is present on the given event. This is for the advantage of Jython implementations of 
	 * {@code CSSSEvent}. 
	 * 
	 * @param event — the event to shut down
	 * @return An event that will shutdown the given event.
	 */
	private CSSSEvent shutDownEventReflection(CSSSEvent event) {
		
		Method shutDownMethod;
		try {
			
			shutDownMethod = event.getClass().getMethod("shutDown");
			return new AnonymousEvent(event.isRenderEvent , () -> {
				
				try {
					
					shutDownMethod.invoke(event);
					
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
				
			});
			
		} catch (NoSuchMethodException | SecurityException e) {}
				
		return new NOPEvent();
			
	}
	
	@Override public void shutDown() {

		paletteUI.shutDown();
		animationPanel.shutDown();
		undos.shutDown(engine.renderer());
		redos.shutDown(engine.renderer());
		
	}

	@Override public boolean isFreed() {

		return animationPanel.isFreed();
		
	}
	
}
