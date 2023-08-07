package cs.csss.editor;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import cs.core.CSDisplay;
import cs.core.graphics.CSRender;
import cs.core.utils.Lambda;
import cs.core.utils.ShutDown;
import cs.core.utils.threads.Await;
import cs.coreext.python.CSJEP;
import cs.csss.editor.brush.BlenderBrush;
import cs.csss.editor.brush.CSSSBrush;
import cs.csss.editor.brush.EraserBrush;
import cs.csss.editor.brush.Eye_DropperBrush;
import cs.csss.editor.brush.Flood_FillBrush;
import cs.csss.editor.brush.PencilBrush;
import cs.csss.editor.brush.Replace_AllBrush;
import cs.csss.editor.brush.ScriptBrush;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.RunArtboardScriptEvent;
import cs.csss.editor.events.RunProjectScriptEvent;
import cs.csss.editor.ui.AnimationPanel;
import cs.csss.editor.ui.FilePanel;
import cs.csss.editor.ui.LHSPanel;
import cs.csss.editor.ui.RHSPanel;
import cs.csss.engine.Control;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFile;
import cs.csss.project.Animation;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.project.VisualLayer;
import cs.csss.utils.StringUtils;
import jep.JepException;
import cs.csss.project.ArtboardPalette.PalettePixel;

/**
 * Editor handles tasks relating to modifying artboards. It functions on a largely event-driven architecture where extenders of 
 * {@link cs.csss.editor.brush.CSSSBrush CSSSBrush} emit events which this class will receive and invoke. These events are to include 
 * {@code _do} and {@code undo} methods which execute the action of the event, or undo it.
 * 
 * @author Chris Brown
 *
 */
public class Editor implements ShutDown {
	
	//create the brushes used by all editors
	static final PencilBrush thePencilBrush = new PencilBrush();
	static final EraserBrush theEraserBrush = new EraserBrush();
	static final Eye_DropperBrush theEyeDropper = new Eye_DropperBrush();
	static final Flood_FillBrush theFloodFill = new Flood_FillBrush();
	static final Replace_AllBrush theReplaceAllBrush = new Replace_AllBrush();
	static final BlenderBrush theBlenderBrush = new BlenderBrush();
	static final ScriptBrush theScriptBrush = new ScriptBrush();
		
	public final IntConsumer setCameraMoveRate;
	public final IntSupplier getCameraMoveRate;
	
	private final Engine engine;	
	private final LHSPanel leftSidePanel;

	private volatile CSSSBrush currentBrush;
	
	private ConcurrentLinkedDeque<CSSSEvent> events = new ConcurrentLinkedDeque<>();
	private UndoRedoQueue redos = new UndoRedoQueue(1000);
	private UndoRedoQueue undos = new UndoRedoQueue(1000);
	
	private final AnimationPanel animationPanel;
	
	public Editor(Engine engine , CSDisplay display) {
		
		this.engine = engine;

		setCameraMoveRate = engine::cameraMoveRate;
		getCameraMoveRate = engine::cameraMoveRate;

		leftSidePanel = new LHSPanel(this , display.nuklear);
		new FilePanel(this , display.nuklear);
		new RHSPanel(this , display.nuklear);
		animationPanel = new AnimationPanel(this , display.nuklear);
				
	}

	/**
	 * Called once per update to run the program's editor.
	 */
	public void update() {

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
		if(!event.isTransientEvent) undos.push(event);
	
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

		if(!engine.wasMousePressedOverUI() && Control.ARTBOARD_INTERACT.pressed() && !engine.isCursorHoveringUI()) {
			
			float[] cursor = engine.getCursorWorldCoords();
			
			Artboard current = setCurrentArtboard(cursor);
			
			if(current != null && current.isCursorInBounds(cursor) && currentBrush != null && !project().freemoveMode()) {
				
				int[] pixelIndex = current.cursorToPixelIndex(cursor);				
		
				//TODO: make these only go to the renderer if needed

				engine.renderer().post(() -> {
					
					boolean canUse = currentBrush.canUse(current , this , pixelIndex[0] , pixelIndex[1]);
					if(canUse) eventPush(currentBrush.use(current , this , pixelIndex[0] , pixelIndex[1]));
									
				}).await();
				
			}
			
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
	
	public CSSSBrush currentBrush() {
		
		return currentBrush;
		
	}
	
	public void setBrushTo(CSSSBrush brush) {
		
		this.currentBrush = brush;
		
	}
	
	public Artboard currentArtboard() {
	
		return engine.currentArtboard();
		
	}

	public PalettePixel selectedColors(Artboard artboard) {
		
		return artboard.createPalettePixel(leftSidePanel.colors());
		
	}
	
	public int undoCapacity() {
		
		return undos.capacity();
		
	}
	
	public int redoCapacity() {
		
		return redos.capacity();
		
	}
	
	public void setUndoAndRedoCapacity(int size) {
		
		redos = new UndoRedoQueue(size);
		undos = new UndoRedoQueue(size);
		
	}
	
	public void setSelectedColor(final PalettePixel pixel) {
		
		leftSidePanel.setColor(pixel);
	
	}
	
	public CSSSProject project() {
	
		return engine.currentProject();
		
	}

	void setCurrentProject(CSSSProject project) {
		
		engine.currentProject(project);

	}
	
	/* OPERATIONS */
	
	/**
	 * Invokes the script pointed to by {@code script}.
	 * 
	 * @param script — a script file
	 */ 
	public void runScriptEvent(CSFile script) {
		
		try(CSJEP python = CSJEP.interpreter()) {
			
			python.initializeCSPythonLibrary();
			
			python.run(script.getRealPath());
			
			boolean isRenderEvent = python.get("isRenderEvent");
			boolean receiveArguments = false;
			
			String popupMessage = null;
			
			try {
				
				receiveArguments = python.get("receiveArguments");
				popupMessage = python.get("argumentPopupMessage");
				
			} catch (JepException e) {}
			
			if(receiveArguments) engine.startScriptArgumentInput(script.name() , Optional.ofNullable(popupMessage) , args -> {
				
				String sanitized = StringUtils.removeMultiSpaces(args);			
				eventPush(new RunArtboardScriptEvent(isRenderEvent , script , currentArtboard() , this , List.of(sanitized.split(" "))));
				
			}); 
			else eventPush(new RunArtboardScriptEvent(isRenderEvent , script , currentArtboard() , this , List.of()));
		
		}
			
	}
		
	public void requestDebugProject() {

		engine.renderer().post(() -> {
			
			CSSSProject project = new CSSSProject(engine , "debug proj" , 4 , true);
			project.initialize();
			
			project.createVisualLayer("Layer 1");
			project.createVisualLayer("Layer 2");

			project.createNonVisualLayer("Nonvisual" , 2);
			
			project.createAnimation("Default Animation");
			
			project.createArtboard(100 , 100);		
			
			setCurrentProject(project);
			
		});
	
	}
	
	/* UI ELEMENTS */
	
	public void startNewProject() {

		engine.startNewProject();
		
	}
	
	public void startNewAnimation() {
		
		engine.startNewAnimation();
		
	}

	public void startNewVisualLayer() {
		
		engine.startNewVisualLayer();
		
	}

	public void startNewNonVisualLayer() {
		
		engine.startNewNonVisualLayer();
		
	}
	
	public void startNewArtboard() {
		
		engine.startNewArtboard();
		
	}
	
	public void startEditingControls() {
		
		engine.startNewControlsEditor();
		
	}
	
	public void startTransparentBackgroundSettings() {
		
		engine.startTransparentBackgroundSettings();
		
	}
	
	public void toggleFullscreen() {
		
		engine.toggleFullScreen();
		
	}
	
	public Await rendererPost(Lambda code) {
		
		return engine.renderer().post(code);
		
	}
	
	public void startRunScript() {
		
		engine.startSelectScriptMenu("events" , this::runScriptEvent);
		
	}
	
	public void startAnimationFrameCustomTimeInput(int index) {
		
		engine.startAnimationFrameCustomTimeInput(index);
		
	}
	
	public void startSetSimulationFrameRate() {
	
		engine.startSetSimulationFrameRate();
		
	}	
	
	public void startSetAnimationFrameSwapType(int index) {
		
		engine.startSetAnimationFrameSwapType(index);
		
	}

	public void startMoveLayerRankEvent(VisualLayer layer) {
		
		engine.startMoveLayerRank(layer);
		
	}
	
	public void startScriptBrushScriptSelect() {
		
		engine.startSelectScriptMenu("brushes", this::setScriptBrush);
		
	}
	
	public void startProjectScript() {
		
		engine.startSelectScriptMenu("project", this::runProjectScript);
		
	}
	
	public void startExport() {
		
		engine.startExport();
		
	}
	
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
			   frameCorner[1] <= cursorScreenY && frameCorner[1] + frameDimensions[1] > cursorScreenY	   
		;
		
	}
	
	public void toggleAnimationPanel() {
		
		animationPanel.toggleShow();
		
	}
	
	public void addRender(CSRender render) {
		
		engine.renderer().addRender(render);
		
	}
	
	public boolean isAnimationPanelShowing() {
		
		return animationPanel.showing();
		
	}
	
	public boolean isCurrentAnimation(Animation animation) {
		
		return engine.currentProject().currentAnimation() == animation;
		
	}
	
	public String getArtboardUIName(Artboard artboard) {
		
		if(project().isCopy(artboard)) return "Artboard " + artboard.name + " Alias";
		else return "Artboard " + artboard.name;
		
	}
	
	private void setScriptBrush(CSFile newScript) {
		
		theScriptBrush.setUseScript(newScript.getRealPath());
		
	}
	
	private void runProjectScript(CSFile file) { 

		try(CSJEP python = CSJEP.interpreter()) {
			
			python.initializeCSPythonLibrary();
			
			python.run(file.getRealPath());
			
			boolean isRenderEvent = python.get("isRenderEvent");
			boolean receiveArguments = false;
			
			String popupMessage = null;
			
			try {
				
				receiveArguments = python.get("receiveArguments");
				popupMessage = python.get("argumentPopupMessage");
				
			} catch (JepException e) {}
			
			if(receiveArguments) engine.startScriptArgumentInput(file.name() , Optional.ofNullable(popupMessage) , args -> {
				
				String sanitized = StringUtils.removeMultiSpaces(args);			
				eventPush(new RunProjectScriptEvent(isRenderEvent , file , engine.currentProject() , this , List.of(sanitized.split(" "))));
				
			}); 
			else eventPush(new RunProjectScriptEvent(isRenderEvent , file , engine.currentProject() , this , List.of()));
			
		}
		
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
	
	public void exit() {
		
		engine.exit();
		
	}
	
	public void saveProject() {
		
		engine.saveProject();
		
	}
	
	public void startProjectSaveAs() {
		
		engine.startProjectSaveAs();
		
	}

	public void startLoadProject() {
		
		engine.startLoadProject();
		
	}
	
	/* DEBUG */
	
	public void toggleRealtime() throws DebugDisabledException {
		
		if(!Engine.isDebug()) throw new DebugDisabledException(this);
		
		engine.realtimeMode(!engine.realtimeMode());
		
	}
	
	public float[] cursorCoords() throws DebugDisabledException {
		
		if(!Engine.isDebug()) throw new DebugDisabledException(this);
		
		return engine.getCursorWorldCoords();
		
	}
	
	@Override public void shutDown() {

		animationPanel.shutDown();
		
	}

	@Override public boolean isFreed() {

		return animationPanel.isFreed();
		
	}
	
}
