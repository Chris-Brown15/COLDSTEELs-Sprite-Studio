package cs.csss.editor;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import cs.core.CSDisplay;
import cs.core.graphics.CSRender;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.utils.Lambda;
import cs.core.utils.ShutDown;
import cs.core.utils.data.CSCHashMap;
import cs.core.utils.data.CSHashMapEntry;
import cs.core.utils.threads.Await;
import cs.core.utils.threads.ConstructingAwait;
import cs.coreext.nanovg.NanoVGFrame;
import cs.coreext.python.CSJEP;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.brush.CSSSBrush;
import cs.csss.editor.brush.CSSSSelectingBrush;
import cs.csss.editor.brush.Delete_RegionBrush;
import cs.csss.editor.brush.EraserBrush;
import cs.csss.editor.brush.Eye_DropperBrush;
import cs.csss.editor.brush.Flood_Fill_WIPBrush;
import cs.csss.editor.brush.ModifyingScriptBrush;
import cs.csss.editor.brush.PencilBrush;
import cs.csss.editor.brush.Move_RegionBrush;
import cs.csss.editor.brush.Replace_AllBrush;
import cs.csss.editor.brush.RotateBrush;
import cs.csss.editor.brush.Scale_RegionBrush;
import cs.csss.editor.brush.ScriptBrush;
import cs.csss.editor.brush.SelectingScriptBrush;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.ShutDownEventEvent;
import cs.csss.editor.event.RunScriptEvent;
import cs.csss.editor.ui.AnimationPanel;
import cs.csss.editor.ui.FilePanel;
import cs.csss.editor.ui.LHSPanel;
import cs.csss.editor.ui.RHSPanel;
import cs.csss.engine.CSSSCamera;
import cs.csss.engine.Control;
import cs.csss.engine.Engine;
import cs.csss.engine.Logging;
import cs.csss.misc.files.CSFile;
import cs.csss.project.Animation;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.CSSSProject;
import cs.csss.project.VisualLayer;
import jep.JepException;
import jep.python.PyObject;

/**
 * Editor handles tasks relating to modifying artboards. It functions on a largely event-driven architecture where implementations of 
 * {@link cs.csss.editor.brush.CSSSBrush CSSSBrush} emit events which this class will receive and invoke. 
 * 
 * @author Chris Brown
 *
 */
public class Editor implements ShutDown {
	
	//create the brushes used by all editors
	static final PencilBrush thePencilBrush = new PencilBrush();
	static final EraserBrush theEraserBrush = new EraserBrush();
	static final Eye_DropperBrush theEyeDropper = new Eye_DropperBrush();
	static final Flood_Fill_WIPBrush theFloodFill = new Flood_Fill_WIPBrush();
	static final Replace_AllBrush theReplaceAllBrush = new Replace_AllBrush();
	static final Move_RegionBrush theRegionSelect = new Move_RegionBrush();
	static final Delete_RegionBrush theDeleteRegion = new Delete_RegionBrush();
	static final RotateBrush theRotateBrush = new RotateBrush();
	static final Scale_RegionBrush theScaleBrush = new Scale_RegionBrush();
	static volatile ScriptBrush theScriptBrush = null;
	static volatile ModifyingScriptBrush theModifyingScriptBrush = null;
	static volatile SelectingScriptBrush theSelectingScriptBrush = null;
	
	/**
	 * Returns the current script brush.
	 * 
	 * @return Current script brush.
	 */
	public static ScriptBrush theScriptBrush() {
		
		return theScriptBrush;
		
	}

	/**
	 * Sets the script brush to {@code theScriptBrush}
	 * 
	 * @param theScriptBrush — new script brush
	 */
	public static void theScriptBrush(ScriptBrush theScriptBrush) {
		
		Editor.theScriptBrush = theScriptBrush;
		
	}

	/**
	 * Returns the current modifying script brush.
	 * 
	 * @return Current modifying script brush.
	 */
	public static ModifyingScriptBrush theModifyingScriptBrush() {
		
		return theModifyingScriptBrush;
		
	}

	/**
	 * Sets the modifying script brush to {@code theModifyingScriptBrush}
	 * 
	 * @param theModifyingScriptBrush — new modifying script brush
	 */
	public static void theModifyingScriptBrush(ModifyingScriptBrush theModifyingScriptBrush) {
		
		Editor.theModifyingScriptBrush = theModifyingScriptBrush;
		
	}

	/**
	 * Returns the current selecting script brush.
	 * 
	 * @return the theSelectingScriptBrush
	 */
	public static SelectingScriptBrush theSelectingScriptBrush() {
		
		return theSelectingScriptBrush;
		
	}

	/**
	 * Sets the selecting script brush to {@code theSelectingScriptBrush}
	 * 
	 * @param theSelectingScriptBrush — new selecting script brush
	 */
	public static void theSelectingScriptBrush(SelectingScriptBrush theSelectingScriptBrush) {
		
		Editor.theSelectingScriptBrush = theSelectingScriptBrush;
		
	}

	public final IntConsumer setCameraMoveRate;
	public final IntSupplier getCameraMoveRate;
	
	private final Engine engine;	
	private final LHSPanel leftSidePanel;

	private volatile CSSSBrush currentBrush = thePencilBrush;
	
	private ConcurrentLinkedDeque<CSSSEvent> events = new ConcurrentLinkedDeque<>();
	private UndoRedoStack redos = new UndoRedoStack(100);
	private UndoRedoStack undos = new UndoRedoStack(100);
	
	private volatile CSCHashMap<EventScriptMeta , String> 
		loadedArtboardScripts = new CSCHashMap<>(13) ,
		loadedProjectScripts = new CSCHashMap<>(13);
	
	private volatile CSCHashMap<BrushScriptMeta , String>
		loadedSimpleBrushScripts = new CSCHashMap<>(13) ,
		loadedModifyingBrushScripts = new CSCHashMap<>(13) ,
		loadedSelectingBrushScripts = new CSCHashMap<>(13);
	
	private final AnimationPanel animationPanel;
	
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
		new FilePanel(this , display.nuklear);
		new RHSPanel(this , display.nuklear , engine);
		animationPanel = new AnimationPanel(this , display.nuklear);
	
	}

	/**
	 * Called once per update to run the program's editor.
	 */
	public void update() {

		updateCurrentBrush();
		//add new events
		editArtboardOnControls();
		undoRedoOnControls();
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
		//only shut down event right away if it is transient. Otherwise, it wont get added to the undo queue and therefore it will be lost
		if(event.isTransientEvent && event instanceof ShutDown asShutDown) { 
			
			eventPush(new ShutDownEventEvent(event.isRenderEvent , asShutDown));
			
		}

		CSSSEvent removed = undos.push(event);
		if(removed != null && removed instanceof ShutDown asShutDown) { 
			
			events.add(new ShutDownEventEvent(removed.isRenderEvent , asShutDown));
			
		}
	
	}

	/**
	 * Handles any events associated with this editor.
	 * 
	 * @param renderer — the renderer, needed in case an event must be executed by the renderer
	 */
	void handleEvents() {

		for(CSSSEvent x : events) {
			
			if(x.isRenderEvent) engine.renderer().post(x::_do);
			else x._do();
		
		}
		
		events.clear();
		
	}
	
	private void editArtboardOnControls() {

		float[] cursor = engine.getCursorWorldCoords();			
		Artboard current = setCurrentArtboard(cursor);

		if(!cursorInBoundsForBrush() || currentBrush == null || project() == null || project().freemoveMode()) return;
		
		if(current != null && current.isCursorInBounds(cursor)) {
		
			int[] pixelIndex = current.worldToPixelIndices(cursor);
			
			//TODO: make these only go to the renderer if needed

			engine.renderer().post(() -> {

				boolean canUse = currentBrush.canUse(current , this , pixelIndex[0] , pixelIndex[1]);				
				if(canUse) eventPush(currentBrush.use(current , this , pixelIndex[0] , pixelIndex[1]));				
				
			}).await();
						
		}

	}

	/**
	 * Handles undo and redo based on the state of the undo and redo keys.
	 */
	private void undoRedoOnControls() {

		if(Control.PRELIM.pressed()) if(Control.PRELIM2.pressed()) {
			
			if(Control.UNDO.pressed()) undo();
			else if (Control.REDO.pressed()) redo();
		
		} else {
			
			if(Control.UNDO.struck()) undo();
			else if (Control.REDO.struck()) redo();
		
		}
		
	}

	/**
	 * Updates the current brush if it is a stateful brush.
	 */
	private void updateCurrentBrush() {
		
		if(currentBrush != null && currentBrush.stateful) { 
		
			CSSSProject project = project();
			rendererPost(() -> currentBrush.update(project == null ? null : project.currentArtboard() , this)).await();
						
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
	
		return engine.currentArtboard();
		
	}
	
	/**
	 * Returns a {@link cs.csss.project.ArtboardPalette.PalettePixel PalettePixel} containing the colors of the currently selected color in
	 * the color picker in the left hand side panel.
	 * 
	 * @param artboard — some artboard
	 * @return A created palette pixel.
	 */
	public PalettePixel selectedColors(Artboard artboard) {
		
		return artboard.createPalettePixel(leftSidePanel.colors());
		
	}

	/**
	 * Returns a {@link cs.csss.project.ArtboardPalette.PalettePixel PalettePixel} containing the colors of the currently selected color in
	 * the color picker in the left hand side panel.
	 * 
	 * @return A created palette pixel.
	 * @throws NullPointerException if the current artboard is null.
	 */
	public PalettePixel selectedColors() throws NullPointerException { 
		
		return currentArtboard().createPalettePixel(leftSidePanel.colors());
		
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
	public void setSelectedColor(final PalettePixel pixel) {
		
		leftSidePanel.setColor(pixel);
	
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
	 * @param cursorScreenX — x coordinate of the cursor in screen space 
	 * @param cursorScreenY — y coordinate of the cursor in screen space
	 * @param windowHeight — height of the window, needed for some calculations
	 * @return {@code true} if the cursor given by the coordinates is within the animation panel's frame slot.
	 */
	public boolean cursorHoveringAnimationFramePanel(int cursorScreenX , int cursorScreenY , int windowHeight) {

		int[] 
			frameCorner = animationPanel.topLeftPointOfAnimationFrameSlot() ,
			frameDimensions = animationPanel.dimensionsOfAnimationFrameSlot()
		;
		
		//these two steps transform the cursor Y and the frame corner Y so that 0 would be the bottom of the window rather than the top. 
		frameCorner[1] = windowHeight - frameCorner[1] - frameDimensions[1];
		cursorScreenY = windowHeight - cursorScreenY;
		
		return frameCorner[0] <= cursorScreenX && frameCorner[0] + frameDimensions[0] > cursorScreenX &&
			   frameCorner[1] <= cursorScreenY && frameCorner[1] + frameDimensions[1] > cursorScreenY;
		
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
	 * Creates an UI element to select a script to run on the current project.
	 */
	public void startRunProjectScript() {
		
		engine.startSelectScriptMenu("projects", file -> {
			
			EventScriptMeta eventScriptMeta = initializeOrGetEventScript(file , loadedProjectScripts);
			if(eventScriptMeta == null) { 
				
				Logging.syserr("Failed to run script due to an error, aborting operation.");
				return;
				
			}
			
			Consumer<List<String>> code = (args) -> {
				
				CSJEP jep = CSJEP.interpreter();
				try {

					PyObject asPyObject = invokeScriptFunction(jep, asScriptName(eventScriptMeta), args, project() , this);
					eventPush(new RunScriptEvent(eventScriptMeta.isRenderEvent() , eventScriptMeta.isTransientEvent() , asPyObject));
					
				} catch(JepException e) {
					
					e.printStackTrace();
					return;
					
				}

			};
				
			pushScriptCode(eventScriptMeta , file , code);
			
		});
		
	}
	
	/**
	 * Invokes the script pointed to by {@code script}.
	 * 
	 * @param script — a script file
	 */ 
	public void startRunArtboardScript() {
		
		engine.startSelectScriptMenu("artboards" , file -> {
			
			EventScriptMeta eventScriptMeta = initializeOrGetEventScript(file , loadedArtboardScripts);
			if(eventScriptMeta == null) { 
				
				Logging.syserr("Failed to run script due to an error, aborting operation.");
				return;
				
			}
			
			Consumer<List<String>> code = (args) -> {

				CSJEP jep = CSJEP.interpreter();				
				try {

					String functionName = asScriptName(eventScriptMeta);					
					PyObject asPyObject = invokeScriptFunction(jep, functionName, args, currentArtboard() , this);
					eventPush(new RunScriptEvent(eventScriptMeta.isRenderEvent() , eventScriptMeta.isTransientEvent() , asPyObject));
					
				} catch(JepException e) {
					
					e.printStackTrace();
					return;
					
				}
							
			};
			
			pushScriptCode(eventScriptMeta , file , code);
			
		});
		
	}

	/**
	 * Creates an UI element for selecting a script for the script brush to use.
	 * 
	 * @param radioButton — a button whose tooltip will be set to the tooltip of the brush script selected
	 */
	public void startSelectSimpleScriptBrush(CSRadio radioButton) {
		
		engine.startSelectScriptMenu("simple brushes", file -> {
			
			//must happen in the main thread
			BrushScriptMeta meta = initializeOrGetBrushScript(file, loadedSimpleBrushScripts);
			if(meta == null) {
				
				Logging.sysDebug("Failed to compile brush script, aborting operation.");
				return;
				
			}
			
			//do this in the main thread, catching an exception along the way to try to ensure the render thread wont throw an exception
			try {
				
				invokeScriptFunction(CSJEP.interpreter(), asScriptName(meta), null , new ScriptBrush(meta) , null);
								
			} catch(JepException e) {
				
				e.printStackTrace();
				return;
				
			}
			
			//must happen in the render thread
			rendererPost(() -> {
				
				CSJEP rendererJep = CSJEP.interpreter();
				ScriptBrush newScriptBrush = new ScriptBrush(meta);
				PyObject object = invokeScriptFunction(rendererJep, asScriptName(meta), null , newScriptBrush , null);
				
				newScriptBrush.setScriptBrush(object, meta);
				boolean isActiveBrushThis = currentBrush == theScriptBrush;
				if(theScriptBrush != null) theScriptBrush.shutDown();
				theScriptBrush = newScriptBrush;
				if(isActiveBrushThis) setBrushTo(theScriptBrush);
				theScriptBrush.setupToolTip(radioButton);
				
			});
			
		});
		
	}

	/**
	 * Creates an UI element for selecting a script for the modifying script brush to use.
	 * 
	 * @param radioButton — a button whose tooltip will be set to the tooltip of the modifying brush script selected
	 */
	public void startSelectModifyingScriptBrush(CSRadio radioButton) {
		
		engine.startSelectScriptMenu("modifying brushes", file -> {
			
			//must happen in the main thread
			BrushScriptMeta meta = initializeOrGetBrushScript(file, loadedModifyingBrushScripts);
			if(meta == null) {
				
				Logging.sysDebug("Failed to compile brush script, aborting operation.");
				return;
				
			}
			
			try {
				
				invokeScriptFunction(CSJEP.interpreter(), asScriptName(meta), null , new ScriptBrush(meta) , null);
								
			} catch(JepException e) {
				
				e.printStackTrace();
				return;
				
			}		
			//must happen in the render thread
			rendererPost(() -> {
			
				try {

					CSJEP rendererJep = CSJEP.interpreter();
					ModifyingScriptBrush newScriptBrush = new ModifyingScriptBrush(meta);
					PyObject object = invokeScriptFunction(rendererJep, asScriptName(meta), null , newScriptBrush , null);
					
					newScriptBrush.setScriptBrush(object, meta);
					boolean isActiveBrushThis = currentBrush == theModifyingScriptBrush;
					if(theModifyingScriptBrush != null) theModifyingScriptBrush.shutDown();
					theModifyingScriptBrush = newScriptBrush;
					if(isActiveBrushThis) setBrushTo(theModifyingScriptBrush);
					theModifyingScriptBrush.setupToolTip(radioButton);
					
				} catch(Exception e) {
					
					e.printStackTrace();
					return;
					
				}
				
			});
			
		});
		
	}

	/**
	 * Creates an UI element for selecting a script for the selecting script brush to use.
	 * 
	 * @param radioButton — a button whose tooltip will be set to the tooltip of the selecting brush script selected
	 */
	public void startSelectSelectingScriptBrush(CSRadio radioButton) {

		engine.startSelectScriptMenu("selecting brushes", file -> {

			BrushScriptMeta meta = initializeOrGetBrushScript(file, loadedSelectingBrushScripts);
			if(meta == null) {
				
				Logging.sysDebug("Failed to compile brush script, aborting operation.");
				return;
				
			}

			try {
				
				invokeScriptFunction(CSJEP.interpreter(), asScriptName(meta), null , new ScriptBrush(meta) , null);
								
			} catch(JepException e) {
				
				e.printStackTrace();
				return;
				
			}
			rendererPost(() -> {
				
				CSJEP rendererJep = CSJEP.interpreter();
				SelectingScriptBrush newScriptBrush = new SelectingScriptBrush(meta);
				PyObject object = invokeScriptFunction(rendererJep, asScriptName(meta), null , newScriptBrush , null);
				
				newScriptBrush.setScriptBrush(object, meta);
				boolean isActiveBrushThis = currentBrush == theSelectingScriptBrush;
				if(theSelectingScriptBrush != null) theSelectingScriptBrush.shutDown();
				theSelectingScriptBrush = newScriptBrush;
				if(isActiveBrushThis) setBrushTo(theSelectingScriptBrush);
				theSelectingScriptBrush.setupToolTip(radioButton);
				
			});	
			
		});			
			
	}
	
	private void pushScriptCode(EventScriptMeta eventScriptMeta , CSFile file , Consumer<List<String>> code) {

		//get user arguments, then procede.
		if(eventScriptMeta.takesArguments()) {
			
			engine.startScriptArgumentInput(file.name(), Optional.ofNullable(eventScriptMeta.argumentDialogueText()), result -> {
				
				List<String> args = List.of(result.split(" "));
				
				if(eventScriptMeta.isRenderEvent()) rendererPost(() -> code.accept(args));
				else code.accept(args);
				
			});		
			
		}
		//invoke the script without arguments
		else {

			if(eventScriptMeta.isRenderEvent()) rendererPost(() -> code.accept(null));
			else code.accept(null);
				
		}
		
	}
	
	/**
	 * Used to invoke the function sharing its name with the python file, returning the result of the function call.
	 * 
	 * @param jep — python interpreter to use
	 * @param functionName — name of the function
	 * @param args — additional, user given arguments to the function to call
	 * @param argument1 — some argument to pass to the called function
	 * @param argument2 — some argument to pass to the called function
	 * @return Result of the called function.
	 * @throws JepException if an error occurs in the python function.
	 */
	private PyObject invokeScriptFunction(
		CSJEP jep , 
		String functionName , 
		List<String> args , 
		Object argument1 , 
		Object argument2
	) throws JepException { 
		
		//set up argument array to pass to Python
		int argumentsInfo = 0;
		if(args != null && args.size() > 0) argumentsInfo++;
		if(argument1 != null) argumentsInfo++;
		if(argument2 != null) argumentsInfo++;
		Object[] arguments = new Object[argumentsInfo];
		argumentsInfo = 0;
		if(argument1 != null) arguments[argumentsInfo++] = argument1;
		if(argument2 != null) arguments[argumentsInfo++] = argument2;
		if(args != null && args.size() > 0) arguments[argumentsInfo++] = args;
		
		//use arguments to invoke function
		Object event = jep.invoke(functionName , arguments);
		Objects.requireNonNull(event);
		
		if(!(event instanceof PyObject)) throw new NotEventTypeException(event);
		return (PyObject) event;
		
	}
	
	/**
	 * This method takes a script file and a hash map and caches information about the script which helps subsequent executions be faster.
	 * The hash map stores the script meta returned by this method as a value, hashed by the name of the script. This means there should be
	 * different hash maps for different types of script.
	 * 
	 * @param scriptFile — a file pointing to a .py file 
	 * @param targetMap — a hash map to store the cached metadata in
	 * @return Script metadata, or null in the case the script cannot be run.
	 */
	private EventScriptMeta initializeOrGetEventScript(CSFile scriptFile , CSCHashMap<EventScriptMeta , String> targetMap) {

		synchronized(targetMap) {
			
			CSHashMapEntry<EventScriptMeta , String> scriptContainer = targetMap.getEntry(scriptFile.name());
						
			if(scriptContainer != null) return scriptContainer.value();

			CSJEP localJep = CSJEP.interpreter();
			boolean[] scriptData;
			String dialogueText;
			
			//this try block will run the script and attempt to gather its metadata.
			try {
				
				localJep.run(scriptFile.getRealPath());
				scriptData = new boolean[] {
					(boolean) localJep.get("isRenderEvent") , 
					getOrDefault(localJep , "isTransientEvent" , false) ,
					getOrDefault(localJep , "takesArguments" , false)						
				};

				dialogueText = getOrDefault(localJep , "argumentDialogueText" , null);
				
			} catch(JepException e) {
				
				e.printStackTrace();
				//returning null means the script cannot be executed as it currently exists.
				return null;
				
			}

			EventScriptMeta scriptMetadata = new EventScriptMeta(
				scriptData[0] , 
				scriptData[1] , 
				scriptData[2] , 
				dialogueText , 
				scriptFile.name()
			);
			
			/*
			 * If in debug, rerun the script every time to verify whether its a renderer event or not.
			 *  
			 * Basically, script writers can launch the application in debug mode while they're developing their script. This will allow
			 * them to constantly recompute whether the script is renderer only or not.
			 * 
			 * By not putting the renderEvent boolean in the hash map, we ensure we reinitialize the script every time it's invoked.
			 * 
			 */				
			if(!Engine.isDebug()) targetMap.put(scriptMetadata , scriptFile.name());
			
			/*
			 * If this is a render event, run the script in the render thread.                                                          
			 * TODO: swap this. make the render thread the first one to run the script, then do it in the main thread only if the event 
			 * is actually a nonrender event. This scenario will occur much less often, so we would usually half the amount of time this
			 * method takes. We dont do it this way because if any exception occurs in the render thread, the application doesn't handle
			 * it and it freezes.                                                                                                       
			 */
			if(scriptData[0]) rendererPost(() -> {
				
				CSJEP jep = CSJEP.interpreter();
				jep.run(scriptFile.getRealPath());
				
			}).await();
			
			return scriptMetadata;
		
		
		}
		
	}

	private BrushScriptMeta initializeOrGetBrushScript(CSFile file , CSCHashMap<BrushScriptMeta , String> targetMap) {
		
		synchronized(targetMap) {

			String title = file.name();
			CSHashMapEntry<BrushScriptMeta , String> meta = targetMap.getEntry(title);
			if(meta != null) return meta.value();

			//for some reason, if running the python code for the first time causes an error in the render thread, the application dies.
			//so we have to run it twice, once in the main thread to make sure it won't throw an exception in the render thread, then in 
			//the render thread.
			try {
				
				CSJEP.interpreter().run(file.getRealPath());
				
			} catch(Exception e) {
				
				e.printStackTrace();
				return null;
				
			}
			
			BrushScriptMeta scriptMeta = rendererMake(() -> {

				CSJEP jep = CSJEP.interpreter();
				jep.run(file.getRealPath());
							
				return new BrushScriptMeta(
					getOrDefault(jep , "tooltip" , "") , 
					getOrDefault(jep , "stateful" , false) , 
					title , 
					jep.get("isRenderEvent") , 
					jep.get("isTransientEvent")
				);
					
			}).get();
			
			if(!Engine.isDebug()) targetMap.put(scriptMeta, title);
			
			return scriptMeta;
			
		}
		
	}
	
	private String asScriptName(EventScriptMeta eventScriptMeta) {
		
		return asScriptName(eventScriptMeta.scriptName());
		
	}
	
	private String asScriptName(BrushScriptMeta brushScriptMeta) {
		
		return asScriptName(brushScriptMeta.scriptName());
		
	}
	
	private String asScriptName(String string) {
		
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

	@Override public void shutDown() {

		animationPanel.shutDown();
		undos.shutDown(engine.renderer());
		redos.shutDown(engine.renderer());
		
	}

	@Override public boolean isFreed() {

		return animationPanel.isFreed();
		
	}

	private <T> T getOrDefault(CSJEP jep , String variableName , T _default) {
		
		T result;
		
		try {
			
			result = jep.get(variableName);
			
		} catch(JepException e) {
			
			Logging.sysDebug(variableName + " was not found, defaulting to " + _default);
			result = _default;
			
		}
		
		return result;		
		
	}
	
}
