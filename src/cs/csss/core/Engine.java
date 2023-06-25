package cs.csss.core;

import static cs.csss.core.Logging.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import static org.lwjgl.nuklear.Nuklear.nk_window_is_any_hovered;

import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL14C.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL14C.glBlendEquation;
import static org.lwjgl.opengl.GL30C.glClearColor;

import static cs.core.utils.CSUtils.wrapTry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.system.Configuration;

import cs.core.CSDisplay;
import cs.core.graphics.CSStandardRenderer;
import cs.core.ui.CSNuklear;
import cs.core.ui.prefabs.InputBox;
import cs.core.utils.LocalTemporal;
import cs.core.utils.ShutDown;
import cs.core.utils.threads.Await;
import cs.core.utils.threads.CSThreads;
import cs.coreext.python.CSJEP;
import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardTexture;
import cs.csss.artboard.NonVisualLayer;
import cs.csss.artboard.NonVisualLayerPrototype;
import cs.csss.artboard.VisualLayer;
import cs.csss.artboard.VisualLayerPrototype;
import cs.csss.editor.Editor;
import cs.csss.editor.GrayscaleShadeMenu;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.Directory;
import cs.csss.misc.files.FileComposition;
import cs.csss.misc.files.FileComposition.FileEntry;

/**
 * Engine is the driver of the application. It contains initialization, the main loop, and handles some program level behavior such as 
 * controls, settings, rendering, memory freeing, etc. 
 * 
 * The general structure of this application is:
 * Engine -> Editor -> Brush, Event, UI 
 *		  -> Project -> Artboard , Visual Layer, NonVisual Layer , Animation 								
 * 															
 * Anything to the right of an arrow is created by the thing to the left of the arrow. Anything to the right of the arrow has access to 
 * part of the capability of the thing to the left.
 * 
 * @author Chris Brown
 *
 */
public final class Engine implements ShutDown {

	public static final CSThreads THE_THREADS = new CSThreads(Runtime.getRuntime().availableProcessors() / 4);
	public static final LocalTemporal THE_TEMPORAL = new LocalTemporal();
	
	private static boolean isDebug = false;
	
	static FileComposition USER_SETTINGS = new FileComposition(FileComposition.MODE_READ_WRITE);
	
	static void preinitialize(final String[] programArgs) {
			
		try {
			
			Logging.initialize(OP_TO_STD);
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
		ArrayList<String> args = new ArrayList<>();
		for(String x : programArgs) args.add(x);
		
		if(args.contains("-d")) { 
			
			isDebug = true;
			
			sysDebug("[COLDSTEEL SPRITE STUDIO DEBUG ENABLED]");
			
			Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
			
			Path debugDir = Paths.get("debug/");
			
			/* The Debug Directory gets remade every launch */
			
			wrapTry(() -> {
				
				//does not work when the debug is empty, this doesnt work
				Files.delete(debugDir);
				Files.createDirectory(debugDir);					
				sysDebug("Debug Folder created.");

			});
			
		}
				
		THE_THREADS.async(Engine::loadSettingsFromDisk);
		
		CSJEP.initialize();
		
	}
	
	private static void createProgramFolder() {

		sysDebug("No program directory found, creating one.");
		
		try {
			
			Files.createDirectory(Paths.get("program/"));			
			sysDebug("Created program directory.");
			
		} catch (IOException e) {
			
			throw new IllegalStateException(
				"A Fatal Error has occured because the program directory was not found and could not be created."
			);
			
		}
	
	}
	
	private static void createSettingsFile() {

		Path settingsPath = Paths.get("program/settings");

		sysDebug("No settings file found, writing one.");
		
		try(FileOutputStream writer = new FileOutputStream(settingsPath.toFile())) {
			
			USER_SETTINGS.write(writer);				
			sysDebug("Created settings file.");
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	private static void loadSettingsFromDisk() {

		Iterator<Control> iter = Control.iterator();
		while(iter.hasNext()) {
			
			Control x = iter.next();			
			USER_SETTINGS.addShort(x.name , () -> (short) x.key()).addBoolean(x.name + " is keyboard" , () -> (boolean) x.isKeyboard());
						
		}
		
		USER_SETTINGS.addInt("Checkered Background Width", () -> ArtboardTexture.backgroundCheckerWidth);
		USER_SETTINGS.addInt("Checkered Background Height", () -> ArtboardTexture.backgroundCheckerHeight);
		
		if(!Files.exists(Paths.get("program/"))) createProgramFolder();
		
		Path settingsPath = Paths.get("program/settings");
		
		if(!Files.exists(settingsPath)) createSettingsFile(); 
		else {
			
			//create a file reader
			try(FileInputStream reader = new FileInputStream(settingsPath.toFile())) {
				
				//read the file according to the file reader.
				USER_SETTINGS.read(reader);
				
				//set the mode of the user settings to READ
				USER_SETTINGS.mode = FileComposition.MODE_READ;
				
				//iterate over controls and entries of the settings file
				Iterator<FileEntry> entries = USER_SETTINGS.iterator();
				Iterator<Control> controls = Control.iterator();
				
				//reads items out of the file entry and into each control.
				while(controls.hasNext()) { 
					
					Control current = controls.next();
										
					//grabs the key code and is keyboard variables from the file entry
					short keycode = (short) entries.next().object();
					boolean isKeyboard = (boolean) entries.next().object();
					//sets the control
					current.key(keycode , isKeyboard);
										
				}

				//load the checkered background dimensions
				ArtboardTexture.backgroundCheckerWidth = (int)entries.next().object();
				ArtboardTexture.backgroundCheckerHeight = (int)entries.next().object();
				
				//restore MODE
				USER_SETTINGS.mode = FileComposition.MODE_READ_WRITE;
				
				sysDebug("Loaded settings from file.");
				
			} catch (FileNotFoundException e) {

				throw new IllegalStateException();
				
			} catch (IOException e) {
				
				e.printStackTrace();
				throw new IllegalStateException();
				
			}
			
		}
		
	}
	
	private static void writeSettingsToDisk() {
		
		try(FileOutputStream writer = new FileOutputStream(Paths.get("program/settings").toFile())) {
			
			sysDebug("Writing settings to file.");
			USER_SETTINGS.write(writer);
			
		} catch (IOException e) {
			 
			throw new IllegalStateException("Setting file writing failed.");
			
		} 
		
	}
	
	public static boolean isDebug() {
		
		return isDebug;
		
	}
	
	static void finalShutDown() {
		
		Logging.shutDown();
		THE_THREADS.shutDown();
		CSDisplay.finalShutDown();
		
	}
	
	private int cameraMoveSpeed = 1;
	
	private boolean 
		isFullScreen = false ,
		//used to track when the mouse was pressed over a UI element
		wasMousePressedOverUI = false
	;
	
	private final CSDisplay display;
	private final Editor editor;
	private CSSSProject currentProject;
	
	private Await renderScene;
	
	private final Directory
		assetsRoot = Directory.establishRoot("assets") ,
		extensionDataRoot = Directory.establishRoot("core_extension_data") ,
		dataRoot = Directory.establishRoot("data") ,
		programRoot = Directory.establishRoot("program")
	;
	
	Engine() {
		
		THE_THREADS.async(() -> {
			
			assetsRoot.seekExistingFiles();
			extensionDataRoot.seekExistingFiles();
			dataRoot.seekExistingFiles();
			programRoot.seekExistingFiles();
			
		});
		
		display = new CSDisplay(true , "COLDSTEEL Sprite Studio" , 18 , "assets/fonts/FiraSansBold.ttf");
		
		//sets a callback to invoke for controls to determine whether they are pressed or not.
		Control.checkPressedCallback((keyCode , isKeyboard) -> isKeyboard ? 
			display.window.isKeyPressed(keyCode) : 
			display.window.isMouseButtonPressed(keyCode)
		);
		
		//initialize main thread interpreter
		CSJEP.interpreter().initializeCSPythonLibrary();
		
		editor = new Editor(this , display);
		
		//initialization of rendering
		display.renderer.post(() -> {

			Artboard.initializeTheArtboardShader();
			
			//initializes the render thread's python interpreter
			CSJEP.interpreter().initializeCSPythonLibrary();
			
			glEnable(GL_BLEND);
			glBlendEquation(GL_FUNC_ADD);
			glBlendFunc(GL_SRC_ALPHA , GL_ONE_MINUS_SRC_ALPHA);
			
		}).await();

		display.window.onScroll((xOffset , yOffset) -> {
			
			if(!isCursorHoveringUI()) display.camera.zoom(yOffset < 0);
			
		});
		
		display.window.onCursorMove((newX , newY) -> {
			
			if(currentProject == null) return;
			
			float[] cursorWorldCoords = getCursorWorldCoords();
			currentProject.setCurrentArtboardByCursorPosition(cursorWorldCoords[0] , cursorWorldCoords[1]);
			
		});
		
		//this sets the wasMousePressedOverUI boolean, which stops you from clicking on a UI element, holding down, and dragging onto an 
		//artboard, causing you to color it when you probably didnt want to.
		display.window.onMouseButtonInput((button , action , mods) -> {
			
			if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS && isCursorHoveringUI()) wasMousePressedOverUI = true;
			else if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) wasMousePressedOverUI = false;
			
		});
		
		enqueueRender();		

	}
	
	void run() {

		while(display.persist()) {
			
			display.waitInputs();
			
			controlEvents();
			
			editor.update();

			THE_TEMPORAL.updateAllEvents();
			
			renderScene();
						
		}

	}
	
	private void renderScene() {
		
		if(renderScene.isFinished()) enqueueRender();

	}
	
	private void enqueueRender() {

		display.layoutAllUserInterfaces();
		
		renderScene = display.renderer.post(() -> {
			
			glClearColor(0.15f , 0.15f , 0.15f , 1.0f);

			glClear(GL_COLOR_BUFFER_BIT);

			if(currentProject != null) {

				Artboard.theArtboardShader().updatePassVariables(
					display.camera , 	
					currentProject.getChannelsPerPixelOfCurrentLayer() , 
					currentProject.grayscaleShade()
				); 
				
				currentProject.renderAllArtboards(display.camera);
			
			}
			
			display.nuklear.render();
			
			display.window.swapBuffers();
			
		});
		
	}
	
	/**
	 * Checks the states of controls and invokes their actions.
	 */
	private void controlEvents() {
		
		Control.updateAllControls();
		
		if(!isCursorHoveringUI()) {
			
			if(Control.CAMERA_UP.struck()) display.camera.translate(0 , cameraMoveSpeed , 0);
			if(Control.CAMERA_DOWN.struck()) display.camera.translate(0 , -cameraMoveSpeed , 0);
			if(Control.CAMERA_LEFT.struck()) display.camera.translate(-cameraMoveSpeed , 0, 0);
			if(Control.CAMERA_RIGHT.struck()) display.camera.translate(cameraMoveSpeed , 0 , 0);

		}
		
		if(Control.TOGGLE_FULLSCREEN_HOTKEY.struck()) toggleFullScreen();
		
	}

	/**
	 * Gets the coordinates of the cursor as world space coordinates by converting their screen space coordinates to world space.
	 * 
	 * @return The world coordinates of the cursor, where {@code getCursorWorldCoords()[0]} is the X world coordinate, and 
	 * 		   {@code getCursorWorldCoords()[1]} is the Y world coordinate.
	 */
	public float[] getCursorWorldCoords() {
		
		double[] coords = display.window.cursorPosition();
		return getCursorWorldCoords(new float[] {(float) coords[0] , (float) coords[1]});
		
	}

	/**
	 * Gets the coordinates of the cursor as world space coordinates by converting their screen space coordinates to world space.
	 * 
	 * @return The world coordinates of the cursor, where {@code getCursorWorldCoords()[0]} is the X world coordinate, and 
	 * 		   {@code getCursorWorldCoords()[1]} is the Y world coordinate.
	 */
	public float[] getCursorWorldCoords(float[] screenCoords) {
		
		return new float[] {
			display.camera.XscreenCoordinateToWorldCoordinate(screenCoords[0]) ,
			display.camera.YscreenCoordinateToWorldCoordinate(screenCoords[1])				
		};
		
	}
	
	public int[] getCursorScreenCoords() {
		
		double[] cursorPos = display.window.cursorPosition();
		return new int[] {(int)cursorPos[0] , (int)cursorPos[1]};
		
	}
	
	public Artboard currentArtboard() {
		
		return currentProject.currentArtboard();
		
	}
	
	public CSStandardRenderer renderer() {
		
		return display.renderer;
		
	}
	
	public void cameraMoveRate(final int moveSpeed) {
		
		this.cameraMoveSpeed = moveSpeed;
		
	}

	public int cameraMoveRate() {
		
		return cameraMoveSpeed;
		
	}

	public void saveCurrentProject() {
		
		if(currentProject == null) return;

		renderer().post(() -> {
			
			try {
				
				currentProject.save();
				
			} catch (IOException e) {

				e.printStackTrace();
				
			}
			
		});		
		
	}
	
	public void startSaveProjectAs() {
		
		if(currentProject == null) return;		
		new InputBox(display.nuklear , "New Project Name" , 0.30f , 0.25f , 100 , CSNuklear.NO_FILTER , System.out::println);
		
	}
	
	public void startLoadProject() {
		
		LoadProjectMenu menu = new LoadProjectMenu(display.nuklear);
		
		THE_TEMPORAL.onTrue(menu::readyToFinish, () -> {
			
			String option = menu.get();
			
			if(option == null) return;
			
			//load project from selected project
			
			final String projectPath = "data" + File.separator + option + File.separator;
			
			ProjectMeta projectMeta = new ProjectMeta();
			
			try(FileInputStream reader = new FileInputStream(projectPath + option + ".csssmeta")) {
				
				projectMeta.read(reader);
				
			} catch (IOException e) {
				
				e.printStackTrace() ; throw new IllegalStateException();
				
			}
						
			CSSSProject project = new CSSSProject(projectMeta);
			
			renderer().post(project::initialize).await();			
			
			renderer().post(() -> project.loadArtboards(projectPath));
						
			project.forEachArtboard(artboard -> renderer().addRender(artboard.render()));
			
			currentProject = project;
			
		});
		
	}
	
	public CSSSProject currentProject() {
		
		return currentProject;
		
	}

	public void currentProject(CSSSProject project) {
		
		currentProject = project;
		
	}

	/**
	 * Returns {@code true} if the cursor is currently hovering a UI element, {@code false} otherwise.
	 * 
	 * @return {@code true} if the cursor is currently hovering a UI element, {@code false} otherwise.
	 */
	public boolean isCursorHoveringUI() {
		
		return nk_window_is_any_hovered(display.nuklear.context());
		
	}
	
	/* UI ELEMENTS */
	
	public void startNewProject() {
		
		NewProjectMenu newProjectMenu = new NewProjectMenu(display.nuklear);
		
		Engine.THE_TEMPORAL.onTrue(newProjectMenu::canFinish , () -> {
			
			if(newProjectMenu.get() == null) return;
			currentProject = display.renderer.make(() -> {
				
				CSSSProject project = new CSSSProject(
					newProjectMenu.get() , 
					newProjectMenu.channelsPerPixel() , 
					newProjectMenu.paletted() , 
					true
				);
				
				project.initialize();
				return project;
				
			}).get();
			
		});		
		
	}
	
	public void startNewAnimation() {
		
		if(currentProject == null) return;
		NewAnimationMenu newAnimationMenu = new NewAnimationMenu(currentProject , display.nuklear);
		
		Engine.THE_TEMPORAL.onTrue(newAnimationMenu::isFinished, () -> {
			
			String result = newAnimationMenu.get();
			if(result == null) return;
			currentProject.addAnimation(new Animation(result , currentProject));
			
		});
		
	}

	public void startNewVisualLayer() {
		
		if(currentProject == null) return;
		
		NewVisualLayerMenu newLayerMenu = new NewVisualLayerMenu(currentProject , display.nuklear);
		
		THE_TEMPORAL.onTrue(newLayerMenu::isFinished , () -> {
			
			VisualLayerPrototype newLayerPrototype = newLayerMenu.get();
			if(newLayerPrototype == null) return;
			currentProject.addVisualLayerPrototype(newLayerPrototype);
			currentProject.forEachArtboard(artboard -> {
				
				VisualLayer visualLayer = new VisualLayer(artboard , currentProject.palette() , newLayerPrototype);
				artboard.addVisualLayer(visualLayer);
				
			});
			
		});
		
	}
	
	public void startNewNonVisualLayer() {
		
		if(currentProject == null) return;
		NewNonVisualLayerMenu newLayerMenu = new NewNonVisualLayerMenu(currentProject , display.nuklear);
		
		THE_TEMPORAL.onTrue(newLayerMenu::isFinished , () -> {
			
			NonVisualLayerPrototype newNonVisualLayer = newLayerMenu.get();
			if(newNonVisualLayer == null) return;
			currentProject.addNonVisualLayerPrototype(newNonVisualLayer);
			currentProject.forEachArtboard(artboard -> {
				
				NonVisualLayer layer = new NonVisualLayer(
					artboard , 
					currentProject.getNonVisualPaletteBySize(newNonVisualLayer.sizeBytes()) , 
					newNonVisualLayer
				);
				
				artboard.addNonVisualLayer(layer);
				
			});
			
		});
		
	}
	
	public void startNewArtboard() {
		
		if(currentProject == null) return;
		
		NewArtboardMenu newArtboardMenu = new NewArtboardMenu(currentProject , display.nuklear);
		THE_TEMPORAL.onTrue(newArtboardMenu::isFinished , () -> {
			
			if(!newArtboardMenu.finishedValidly()) return;
			
			Artboard artboard = display.renderer.make(
				() -> new Artboard(newArtboardMenu.width() , newArtboardMenu.height())
			).get();
			
			currentProject.forEachVisualLayerPrototype(vlP -> {
			
				VisualLayer layer = new VisualLayer(artboard , currentProject.palette() , vlP);
				artboard.addVisualLayer(layer);
				
			});
			
			currentProject.forEachNonVisualLayerPrototype(nvlP -> {
			
				NonVisualLayer layer = new NonVisualLayer(artboard , currentProject.getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
				artboard.addNonVisualLayer(layer);
			
			});
			
			display.renderer.addRender(artboard.render());
			
			display.renderer.post(() -> currentProject.addArtboard(artboard));
			if(currentProject.currentArtboard() == null) currentProject.currentArtboard(artboard);
			
		});
		
	}
	
	public void startNewControlsEditor() {
		
		new ModifyControlsMenu(display.nuklear , this);
				
	}
	
	public void startTransparentBackgroundSettings() {
		
		new TransparentBackgroundSettingsMenu(editor , display.nuklear , currentProject);
		
	}
	
	public void startNewSetGrayscaleShadeMenu() {
		
		if(currentProject == null) return;
		
		GrayscaleShadeMenu grayscaleShadeMenu = new GrayscaleShadeMenu(display.nuklear);
		
		THE_TEMPORAL.onTrue(grayscaleShadeMenu::readyToFinish, () -> {
			
			if(grayscaleShadeMenu.option == GrayscaleShadeMenu.NO_OP) return;
			currentProject.grayscaleShade(grayscaleShadeMenu.option);			
			
		});
		
	}
	
	public void startSelectScriptMenu() {
		
		if(currentProject == null) return;
		
		SelectScriptMenu script = new SelectScriptMenu(display.nuklear);
		
		THE_TEMPORAL.onTrue(script::readyToFinish , () -> {
			
			CSFile selected;
			
			if((selected = script.selectedScript()) == null) return;
			
			editor.runScriptEvent(selected);
			
		});
		
	}
	
	public void exit() {
		
		display.window.close();
		
	}
	
	public void toggleFullScreen() { 
		
		isFullScreen = !isFullScreen;
		if(isFullScreen) display.window.goBorderlessFullScreen();
		else display.window.goNonFullScreen();
		
	}
	
	public boolean wasMousePressedOverUI() {
		
		return wasMousePressedOverUI;
		
	}
	
	@Override public void shutDown() {
		
		Await writeFiles = THE_THREADS.async(() -> writeSettingsToDisk());
		
		display.renderer.post(() -> CSJEP.interpreter().shutDown());
		CSJEP.interpreter().shutDown();
		
		if(!display.isFreed()) { 

			display.window.detachContext();			
			display.shutDown();
						
		}
		
		writeFiles.await();
				
	}

	@Override public boolean isFreed() {
		
		return display.isFreed();
		
	}
	
}