package cs.csss.editor;

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
import cs.csss.core.Control;
import cs.csss.core.Engine;
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
import cs.csss.misc.files.CSFile;
import cs.csss.project.Animation;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.project.NonVisualLayer;
import cs.csss.project.NonVisualLayerPrototype;
import cs.csss.project.VisualLayer;
import cs.csss.project.VisualLayerPrototype;
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
	private UndoRedoQueue redos = new UndoRedoQueue();
	private UndoRedoQueue undos = new UndoRedoQueue();
	
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
		undos.push(event);
	
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

		if(!engine.wasMousePressedOverUI() && Control.ARTBOARD_INTERACT.pressed() && currentBrush != null && !engine.isCursorHoveringUI()) {
			
			float[] cursor = engine.getCursorWorldCoords();
			
			Artboard current = engine.currentArtboard();
			
			if(current != null && current.isCursorInBounds(cursor)) {
				
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

		if(Control.PRELIM.pressed()) {
			
			if(Control.PRELIM2.pressed()) {
				
				if(Control.UNDO.pressed()) undo();
				else if (Control.REDO.pressed()) redo();
			
			} else {
				
				if(Control.UNDO.struck()) undo();
				else if (Control.REDO.struck()) redo();
			
			}
			
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
	
	public void saveCurrentProject() {
		
		engine.saveCurrentProject();
		
	}

	public void startSaveAs() {
		
		engine.startSaveProjectAs();
		
	}
	
	public void startProjectLoad() {
		
		engine.startLoadProject();
		
	}
	
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
			
			CSSSProject project = new CSSSProject(engine , "debug proj" , 4 , true , true);
			project.initialize();
			project.addVisualLayerPrototype(new VisualLayerPrototype("Layer 1"));
			project.addVisualLayerPrototype(new VisualLayerPrototype("Layer 2"));
			project.addNonVisualLayerPrototype(new NonVisualLayerPrototype(2 , "Nonvisual"));
			project.addAnimation(new Animation("Default Animation"));
			project.addAnimation(new Animation("Animation One"));
			project.addAnimation(new Animation("Animation Two"));
			
			Artboard artboard = new Artboard("1" , 100 , 100);
			Artboard artboard2 = new Artboard("2" , 200 , 200);
			Artboard artboard3 = new Artboard("3" , 300 , 300);
			project.addArtboard(artboard);
			project.addArtboard(artboard2);
			project.addArtboard(artboard3);
			
			project.forEachVisualLayerPrototype(vlP -> {
				
				VisualLayer layer = new VisualLayer(artboard , project.palette() , vlP);
				artboard.addVisualLayer(layer);

				VisualLayer layer2 = new VisualLayer(artboard2 , project.palette() , vlP);
				artboard2.addVisualLayer(layer2);

				VisualLayer layer3 = new VisualLayer(artboard3 , project.palette() , vlP);
				artboard3.addVisualLayer(layer3);
				
			});
			
			project.forEachNonVisualLayerPrototype(nvlP -> {
				
				NonVisualLayer layer = new NonVisualLayer(artboard , project.getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
				artboard.addNonVisualLayer(layer);			

				NonVisualLayer layer2 = new NonVisualLayer(artboard2 , project.getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
				artboard2.addNonVisualLayer(layer2);			

				NonVisualLayer layer3 = new NonVisualLayer(artboard3 , project.getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
				artboard3.addNonVisualLayer(layer3);			
				
			});

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
	
	public void exit() {
		
		engine.exit();
		
	}

	/* DEBUG */
	
	public void toggleRealtime() throws DebugDisabledException {
		
		if(!Engine.isDebug()) throw new DebugDisabledException(this);
		
		engine.realtimeMode(!engine.realtimeMode());
		
	}
	
	@Override public void shutDown() {

		animationPanel.shutDown();
		
	}

	@Override public boolean isFreed() {

		return animationPanel.isFreed();
		
	}
	
}
