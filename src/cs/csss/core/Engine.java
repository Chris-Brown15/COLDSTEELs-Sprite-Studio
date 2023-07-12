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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.lwjgl.system.Configuration;

import cs.core.CSDisplay;
import cs.core.graphics.CSStandardRenderer;
import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSNuklearRender;
import cs.core.ui.prefabs.InputBox;
import cs.core.utils.LocalTemporal;
import cs.core.utils.ShutDown;
import cs.core.utils.Timer;
import cs.core.utils.threads.Await;
import cs.core.utils.threads.CSThreads;
import cs.coreext.python.CSJEP;
import cs.csss.editor.Editor;
import cs.csss.editor.events.MoveLayerRankEvent;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.Directory;
import cs.csss.misc.files.FileComposition;
import cs.csss.misc.files.FileComposition.FileEntry;
import cs.csss.project.Animation;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardTexture;
import cs.csss.project.CSSSProject;
import cs.csss.project.NonVisualLayer;
import cs.csss.project.NonVisualLayerPrototype;
import cs.csss.project.ProjectMeta;
import cs.csss.project.VisualLayer;
import cs.csss.project.VisualLayerPrototype;
import cs.csss.ui.menus.LoadProjectMenu;
import cs.csss.ui.menus.ModifyControlsMenu;
import cs.csss.ui.menus.NewAnimationMenu;
import cs.csss.ui.menus.NewArtboardMenu;
import cs.csss.ui.menus.NewNonVisualLayerMenu;
import cs.csss.ui.menus.NewProjectMenu;
import cs.csss.ui.menus.NewVisualLayerMenu;
import cs.csss.ui.menus.SelectScriptMenu;
import cs.csss.ui.menus.SetAnimationFrameSwapTypeMenu;
import cs.csss.ui.menus.TransparentBackgroundSettingsMenu;
import cs.csss.ui.prefabs.DetailedInputBox;
import cs.csss.utils.FloatReference;

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

	private static int realtimeTargetFPS = 60;
	private static double realtimeFrameTime = 1000 / realtimeTargetFPS;

	private static void setRealtimeTargetFPS(int newTarget) {
		
		realtimeTargetFPS = newTarget;
		realtimeFrameTime = 1000 / realtimeTargetFPS;
		
	}
	
	public static double realtimeFrameTime() {
		
		return realtimeFrameTime;
		
	}
	
	static void preinitialize(final String[] programArgs) {
			
		try {
			
			Logging.initialize(OP_TO_STD);
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
		List<String> args = new ArrayList<>();
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
	
	private final CSDisplay display;
	private final Editor editor;
	private final CSSSCamera camera;
	private Await renderScene;
	
	private CSSSProject currentProject;

	private int cameraMoveSpeed = 1;

	private boolean 
		isFullScreen = false ,
		/**
		 * Used to track when the mouse was pressed over a UI element
		 */
		wasMousePressedOverUI = false ,
		/**
		 * Used to track whether we should be in real time mode or event driven mode.
		 */
		realtimeMode = false
	;
		
 	private Timer frameTimer = new Timer();
	
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
		
		CSNuklearRender.BUFFER_INITIAL_SIZE(8 * 1024);
		
		int[] windowSize = display.window.size();
		camera = new CSSSCamera(windowSize[0] , windowSize[1]);		
		display.window.onFramebufferResize(camera::resetProjection);
		
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
			
			if(!isCursorHoveringUI()) camera.zoom(yOffset < 0);
			else if (editor.isAnimationPanelShowing() && currentProject.currentAnimation() != null) { 
				
				int[] cursorScreenCoords = getCursorScreenCoords();
				int windowHeight = display.window.size()[1]; 
				boolean hovering = editor.cursorHoveringAnimationFramePanel(cursorScreenCoords[0], cursorScreenCoords[1] , windowHeight);
				if(hovering) editor.animationPanel().zoom(yOffset < 0);
				
			}
			
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
			
			getInputs();
			
			controlEvents();
			
			editor.update();

			THE_TEMPORAL.updateAllEvents();
			
			renderScene();
			
			realtimeFrameLockup();
			
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
					camera.projection() , 
					camera.viewTranslation() , 
					currentProject.getChannelsPerPixelOfCurrentLayer()
				);
				
				currentProject.renderAllArtboards();
			
			}
			
			//render UI
			display.nuklear.render();
			
			/*
			 * Here is where we render the current frame of the active animation within the animation editor menu. To do this, I rerender
			 * the artboard that is the current frame, applying a translation to it so it sits on top of the UI element, hence why this is
			 * done after the UI is rendered. 
			 */
			if(currentProject != null) {
				
				Animation current = currentProject.currentAnimation();
				if(current != null && editor.isAnimationPanelShowing()) {

					current.renderCurrentFrame(
						camera , 
						editor.animationPanel() ,
						display.window.size()[1] ,
						currentProject.getChannelsPerPixelOfCurrentLayer()
					);
					
				}
				
			}
			
			display.window.swapBuffers();
			
		});
		
	}
	
	/**
	 * Gets inputs to the program based on the state of {@link Engine#realtimeMode realtimeMode}.
	 */
	private void getInputs() {

		if(realtimeMode) { 
			
			display.pollInputs();
			frameTimer.start();
			
		} else display.waitInputs();
			
	}
	
	/**
	 * Locks the application at the framerate given by {@link Engine#realtimeTargetFPS} by making the main thread wait an appropriate
	 * amount of time.
	 */
	private void realtimeFrameLockup() {

		if(realtimeMode) {
			
			long waitFor = (long) (realtimeFrameTime - frameTimer.getElapsedTimeMillis());
			//this will be true if the frame took longer than the ideal frame time, REALTIME_FRAME_TIME, which is no big deal, just dont
			//make the thread wait.
			if(waitFor <= 0) return;
			
			Thread current = Thread.currentThread();
			synchronized(current) {
				
				try {
					
					current.wait(waitFor);
					
				} catch (InterruptedException e) {}
				
			}
			
		}
		
	}
	
	/**
	 * Checks the states of controls and invokes their actions.
	 */
	private void controlEvents() {
		
		Control.updateAllControls();
		
		if(!isCursorHoveringUI()) {
			
			if(Control.CAMERA_UP.struck()) camera.translate(0 , cameraMoveSpeed , 0);
			if(Control.CAMERA_DOWN.struck()) camera.translate(0 , -cameraMoveSpeed , 0);
			if(Control.CAMERA_LEFT.struck()) camera.translate(-cameraMoveSpeed , 0, 0);
			if(Control.CAMERA_RIGHT.struck()) camera.translate(cameraMoveSpeed , 0 , 0);

		} 
		//try to move the animation panel view of the current frame
		else if(currentProject != null && editor.isAnimationPanelShowing() && currentProject.currentAnimation() != null ) {
			
			if(Control.CAMERA_UP.struck()) editor.animationPanel().translate(0 , cameraMoveSpeed); 		
			if(Control.CAMERA_DOWN.struck()) editor.animationPanel().translate(0 , -cameraMoveSpeed);
			if(Control.CAMERA_LEFT.struck()) editor.animationPanel().translate(-cameraMoveSpeed , 0);
			if(Control.CAMERA_RIGHT.struck()) editor.animationPanel().translate(cameraMoveSpeed , 0);
					
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
			camera.XscreenCoordinateToWorldCoordinate(screenCoords[0]) ,
			camera.YscreenCoordinateToWorldCoordinate(screenCoords[1])				
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
						
			CSSSProject project = new CSSSProject(this , projectMeta);
			
			renderer().post(project::initialize).await();
			
			project.forEachArtboard(artboard -> renderer().addRender(artboard.render()));
			
			currentProject = project;
			
		});
		
	}
	
	public CSSSProject currentProject() {
		
		return currentProject;
		
	}

	public Animation currentAnimation() {
		
		if(currentProject == null || currentProject.currentAnimation() == null) return null;
		return currentProject.currentAnimation();
		
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
					this ,
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
			currentProject.addAnimation(new Animation(result));
			
		});
		
	}

	public void startNewVisualLayer() {
		
		if(currentProject == null) return;
		
		NewVisualLayerMenu newLayerMenu = new NewVisualLayerMenu(currentProject , display.nuklear);
		
		THE_TEMPORAL.onTrue(newLayerMenu::isFinished , () -> {
			
			VisualLayerPrototype newLayerPrototype = newLayerMenu.get();
			if(newLayerPrototype == null) return;
			currentProject.addVisualLayerPrototype(newLayerPrototype);
			currentProject.forEachNonShallowCopiedArtboard(artboard -> {
				
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
			currentProject.forEachNonShallowCopiedArtboard(artboard -> {
				
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
			
			Artboard artboard = display.renderer.make(() -> {
				
				Artboard newArtboard = new Artboard(
					String.valueOf(currentProject.numberArtboards()) , 
					newArtboardMenu.width() , 
					newArtboardMenu.height()
				);
				 
				currentProject.addArtboard(newArtboard);
				return newArtboard;
				
			}).get();
			
			currentProject.forEachVisualLayerPrototype(vlP -> {
			
				VisualLayer layer = new VisualLayer(artboard , currentProject.palette() , vlP);
				artboard.addVisualLayer(layer);
				
			});
			
			currentProject.forEachNonVisualLayerPrototype(nvlP -> {
			
				NonVisualLayer layer = new NonVisualLayer(artboard , currentProject.getNonVisualPaletteBySize(nvlP.sizeBytes()) , nvlP);
				artboard.addNonVisualLayer(layer);
			
			});
			
			display.renderer.addRender(artboard.render());			
			if(currentProject.currentArtboard() == null) currentProject.currentArtboard(artboard);
			
		});
		
	}
	
	public void startNewControlsEditor() {
		
		new ModifyControlsMenu(display.nuklear , this);
				
	}
	
	public void startTransparentBackgroundSettings() {
		
		new TransparentBackgroundSettingsMenu(editor , display.nuklear , currentProject);
		
	}
	
	public void startSelectScriptMenu(String scriptSubdirectory , Consumer<CSFile> onComplete) {
		
		if(currentProject == null) return;
		
		SelectScriptMenu script = new SelectScriptMenu(display.nuklear , scriptSubdirectory);
		
		THE_TEMPORAL.onTrue(script::readyToFinish , () -> {
			
			CSFile selected;
			
			if((selected = script.selectedScript()) == null) return;
			
			onComplete.accept(selected);
			
		});
		
	}
	
	public void startAnimationFrameCustomTimeInput(int animationFrameIndex) {
		
		String title = "Frame " + animationFrameIndex + " custom speed (millis)";
		
		new InputBox(display.nuklear , title , .4f , .4f , 10 , CSNuklear.DECIMAL_FILTER , result -> {
			
			if(result.equals("")) return;
			currentProject.currentAnimation().getFrame(animationFrameIndex).time(new FloatReference(Float.parseFloat(result)));
				
		});
		
	}
	
	public void startSetSimulationFrameRate() {
		
		new DetailedInputBox(
			display.nuklear , 
			"Set Simulation Frame Rate" ,
			"Currently " + realtimeTargetFPS + ". This is used for viewing animations. Set this value to the number of frames per "
			+ "second your application is targeting." ,
			.5f - (.15f / 2f) ,
			.5f - (.15f / 2f), 
			.15f ,
			.15f ,
			CSNuklear.DECIMAL_FILTER ,
			5 ,
			result -> {
			
				try {
					
					Integer asInt = Integer.parseInt(result);
					setRealtimeTargetFPS(asInt);

				} catch(NumberFormatException e) {
					
					sysDebug(e);
					
				}
								
			}
			
		);	
		
	}
	
	public void startMoveLayerRank(VisualLayer layer) {
		
		int rank = currentProject.currentArtboard().getLayerRank(layer);
		
		new InputBox(display.nuklear , "Input New Rank for Layer " + rank , 0.4f , 0.4f , 4 , CSNuklear.DECIMAL_FILTER , result -> {
			
			if(result.equals("")) return;
			Artboard current = currentProject.currentArtboard();
			int res = Integer.parseInt(result);
			if(res >= current.numberVisualLayers()) res = current.numberVisualLayers();
			if(res < 0) res = 0;
			
			editor.eventPush(new MoveLayerRankEvent(current , res));
	
		});
			
	}
	
	public void startSetAnimationFrameSwapType(int frameIndex) {
		
		SetAnimationFrameSwapTypeMenu animationFrameSwapTypeMenu = new SetAnimationFrameSwapTypeMenu(display.nuklear , frameIndex);
		
		THE_TEMPORAL.onTrue(animationFrameSwapTypeMenu::finished , () -> {
			
			if(animationFrameSwapTypeMenu.swapType() == null) return;
			currentAnimation().setFrameSwapType(frameIndex, animationFrameSwapTypeMenu.swapType());
			
		});
		
	}
	
	public void startScriptArgumentInput(String scriptName , Optional<String> popupMessage , Consumer<String> onFinish) {
	
		String description = "Input arguments to " + scriptName + ". Leave spaces between arguments. ";
		if(popupMessage.isPresent()) description += popupMessage.get();
		
		new DetailedInputBox(
			display.nuklear , 
			scriptName + " Arguments" , 
			description,
			.5f - (.33f / 2) ,
			.5f - (.22f / 2) ,
			.33f ,
			.22f ,
			CSNuklear.NO_FILTER ,
			999 ,
			onFinish
		);
		
	}
	
	public void exit() {
		
		display.window.close();
		
	}
	
	public void toggleFullScreen() { 
		
		isFullScreen = !isFullScreen;
		if(isFullScreen) display.window.goBorderlessFullScreen();
		else display.window.goNonFullScreen();
		
	}

	public void realtimeMode(boolean mode) {
		
		this.realtimeMode = mode;
		
	}	
		
	public boolean realtimeMode() {
		
		return realtimeMode;
		
	}
	
	public boolean wasMousePressedOverUI() {
		
		return wasMousePressedOverUI;
		
	}
		
	@Override public void shutDown() {
		
		Await writeFiles = THE_THREADS.async(() -> writeSettingsToDisk());
		
		display.renderer.post(() -> CSJEP.interpreter().shutDown());
		CSJEP.interpreter().shutDown();
		
		editor.shutDown();
		
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