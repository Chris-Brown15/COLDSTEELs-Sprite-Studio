package cs.csss.editor;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import cs.core.CSDisplay;
import cs.core.graphics.CSStandardRenderer;
import cs.core.utils.Lambda;
import cs.coreext.python.CSJEP;
import cs.csss.artboard.Artboard;
import cs.csss.artboard.VisualLayerPrototype;
import cs.csss.artboard.ArtboardPalette.PalettePixel;
import cs.csss.artboard.NonVisualLayer;
import cs.csss.artboard.NonVisualLayerPrototype;
import cs.csss.artboard.VisualLayer;
import cs.csss.core.CSSSProject;
import cs.csss.core.Control;
import cs.csss.core.Engine;
import cs.csss.editor.brush.BlenderBrush;
import cs.csss.editor.brush.CSSSBrush;
import cs.csss.editor.brush.Eye_DropperBrush;
import cs.csss.editor.brush.Flood_FillBrush;
import cs.csss.editor.brush.PencilBrush;
import cs.csss.editor.brush.Replace_AllBrush;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.RunScriptEvent;
import cs.csss.misc.files.CSFile;

/**
 * Editor handles tasks relating to modifying artboards. It functions on a largely event-driven architecture where extenders of 
 * {@link cs.csss.editor.brush.CSSSBrush CSSSBrush} emit events which this class will receive and invoke. These events are to include 
 * {@code _do} and {@code undo} methods which execute the action of the event, or undo it.
 * 
 * @author Chris Brown
 *
 */
public class Editor {
	
	//create the brushes used by all editors	 
	static final PencilBrush thePencilBrush = new PencilBrush();
	static final Eye_DropperBrush theEyeDropper = new Eye_DropperBrush();
	static final Replace_AllBrush theReplaceAllBrush = new Replace_AllBrush();
	static final Flood_FillBrush theFloodFill = new Flood_FillBrush();
	static final BlenderBrush theBlenderBrush = new BlenderBrush();
		
	public final IntConsumer setCameraMoveRate;
	public final IntSupplier getCameraMoveRate;
	
	private final Engine engine;	
	private final LHSPanel leftSidePanel;

	private volatile CSSSBrush currentBrush;
	
	private ConcurrentLinkedDeque<CSSSEvent> events = new ConcurrentLinkedDeque<>();
	private UndoRedoQueue redos = new UndoRedoQueue();
	private UndoRedoQueue undos = new UndoRedoQueue();
	
	public Editor(Engine engine , CSDisplay display) {
		
		this.engine = engine;

		setCameraMoveRate = engine::cameraMoveRate;
		getCameraMoveRate = () -> engine.cameraMoveRate();

		leftSidePanel = new LHSPanel(this , display.nuklear);
		new FilePanel(this , display.nuklear);
		new RHSPanel(this , display.nuklear);
		new AnimationPanel(this , display.nuklear);
		
	}

	public void update() {

		//add new events
		editArtboardOnControls();
		undoRedoOnControls();
		//handle them
		handleEvents();
		
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
									
				});
				
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
	
	CSSSProject currentProject() {
	
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
	
	public void runScriptEvent(CSFile script) {
		
		CSJEP python = CSJEP.interpreter();
		
		python.run(script.getRealPath());
		
		boolean isRenderEvent = python.get("isRenderEvent");
		
		eventPush(new RunScriptEvent(isRenderEvent , script , currentArtboard() , this));
			
	}
		
	void requestDebugProject() {

		engine.renderer().post(() -> {
			
			CSSSProject project = new CSSSProject("debug proj" , 4 , true , true);
			project.initialize();
			project.addVisualLayerPrototype(new VisualLayerPrototype("Layer 1"));
			project.addVisualLayerPrototype(new VisualLayerPrototype("Layer 2"));
			project.addNonVisualLayerPrototype(new NonVisualLayerPrototype(2 , "Nonvisual"));
							
			Artboard artboard = new Artboard(100 , 100);
			project.addArtboard(artboard);
			
			project.forEachVisualLayerPrototype(vlP -> {
				
				VisualLayer layer = new VisualLayer(artboard , project.palette() , vlP);
				artboard.addVisualLayer(layer);
				
			});
			
			project.forEachNonVisualLayerPrototype(nvlP -> {
				
				NonVisualLayer layer = new NonVisualLayer(artboard , project.getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
				artboard.addNonVisualLayer(layer);			
				
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
	
	public void startNewGrayscaleShadeMenu() {
		
		engine.startNewSetGrayscaleShadeMenu();
		
	}
	
	public void toggleFullscreen() {
		
		engine.toggleFullScreen();
		
	}
	
	public void rendererPost(Lambda code) {
		
		renderer().post(code);
		
	}
	
	public void startRunScript() {
		
		engine.startSelectScriptMenu();
		
	}
	
	CSStandardRenderer renderer() {
		
		return engine.renderer();
		
	}
	
	public void exit() {
		
		engine.exit();
		
	}
	
}