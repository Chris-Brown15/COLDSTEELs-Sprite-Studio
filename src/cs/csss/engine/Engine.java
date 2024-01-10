package cs.csss.engine;

import static cs.csss.engine.Logging.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.nuklear.Nuklear.nk_window_is_any_hovered;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL14C.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14C.glBlendEquation;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.system.Configuration;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends.OverlayToWebPageMode;
import com.codedisaster.steamworks.SteamRemoteStorage.WorkshopFileType;
import com.codedisaster.steamworks.SteamUGC.MatchingUGCType;
import com.codedisaster.steamworks.SteamUGC.UserUGCList;
import com.codedisaster.steamworks.SteamUGC.UserUGCListSortOrder;

import cs.core.CSDisplay;
import cs.core.graphics.CSRender;
import cs.core.graphics.CSStandardRenderer;
import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSNuklearRender;
import cs.core.ui.prefabs.InputBox;
import cs.core.utils.CSRefInt;
import cs.core.utils.CSUtils;
import cs.core.utils.Lambda;
import cs.core.utils.ShutDown;
import cs.core.utils.Timer;
import cs.core.utils.exceptions.RequirementFailedException;
import cs.core.utils.exceptions.SpecificationBrokenException;
import cs.core.utils.files.TTF;
import cs.core.utils.threads.Await;
import cs.coreext.nanovg.CoordinateSpace;
import cs.coreext.nanovg.NanoVG;
import cs.coreext.nanovg.NanoVGFrame;
import cs.coreext.nanovg.NanoVGTypeface;
import cs.csss.annotation.InDevelopment;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.ScriptType;
import cs.csss.editor.brush.CSSSSelectingBrush;
import cs.csss.editor.event.MoveLayerRankEvent;
import cs.csss.editor.event.ShutDownProjectEvent;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.misc.graphcs.memory.GPUMemoryViewer;
import cs.csss.project.Animation;
import cs.csss.project.AnimationFrame;
import cs.csss.project.AnimationSwapType;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.project.VisualLayer;
import cs.csss.project.io.CTSPFile;
import cs.csss.project.io.ImageImporter;
import cs.csss.project.io.ProjectExporterUI;
import cs.csss.steamworks.SpriteStudioSteamFriendsCallbacks;
import cs.csss.steamworks.SpriteStudioSteamUserCallbacks;
import cs.csss.steamworks.SpriteStudioSteamWorkshopCallbacks;
import cs.csss.steamworks.WorkshopDownloadHelper;
import cs.csss.steamworks.WorkshopUploadHelper;
import cs.csss.ui.menus.ArtboardMenu;
import cs.csss.ui.menus.CheckeredBackgroundSettingsMenu;
import cs.csss.ui.menus.DetailedInputBox;
import cs.csss.ui.menus.Dialogue;
import cs.csss.ui.menus.DroppedFileAcceptingDialogue;
import cs.csss.ui.menus.LoadProjectMenu;
import cs.csss.ui.menus.ModifyControlsMenu;
import cs.csss.ui.menus.NewAnimationMenu;
import cs.csss.ui.menus.NewNonVisualLayerMenu;
import cs.csss.ui.menus.NewProjectMenu;
import cs.csss.ui.menus.NewScriptMenu;
import cs.csss.ui.menus.NewVisualLayerMenu;
import cs.csss.ui.menus.NotificationBox;
import cs.csss.ui.menus.SelectScriptMenu;
import cs.csss.ui.menus.SetAnimationFrameSwapTypeMenu;
import cs.csss.ui.menus.SteamWorkshopItemUpdateMenu;
import cs.csss.ui.menus.SteamWorkshopItemUploadMenu;
import cs.csss.ui.menus.VectorTextMenu;
import cs.csss.ui.utils.UIUtils;
import cs.csss.utils.FloatReference;
import cs.ext.steamworks.BadQueryException;
import cs.ext.steamworks.SteamLanguages;
import cs.ext.steamworks.SteamApplicationData;
import cs.ext.steamworks.Steamworks;
import cs.ext.steamworks.UGC;
import cs.ext.steamworks.UGCQuery;

/**
 * Engine is the driver of the application. It contains initialization, the main loop, and handles some program level behavior such as 
 * controls, settings, rendering, memory freeing, etc. 
 * <P>
 * 	The general structure of this application is:<br>
 * 	<ul>	
 * 		<li> 
 * 			Engine
 *		  	<ol>
 *		  		<li>
 *		  			Editor
 *		  			<ul>
 *		  				<li> Brush </li>
 *		  				<li> Event </li>
 *		  				<li> UI </li>
 *		  			</ul>
 *		  		</li>
 *		  		<li>
 *		  			Project
 *		  			<ul>
 *		  				<li> Artboard </li>
 *		  				<li> Visual Layer </li>
 *		  				<li> Nonvisual Layer </li>
 *		  				<li> Animation </li>
 *		  			</ul>
 *		  		</li>
 *		  	</ol>
 *		</li>
 * 	</ul>
 * </p>	
 * 
 * @author Chris Brown
 *
 */
public final class Engine implements ShutDown {

	/**
	 * Thread pool for the application.
	 */
	public static final ExecutorService THE_THREADS = Executors.newCachedThreadPool();
	
	/**
	 * Scheduler object that can receive code and execute it based on some predicate.
	 */
	public static final ConcurrentTemporal THE_TEMPORAL = new ConcurrentTemporal();
	
	/**
	 * Version of the current application distribution.
	 */
	public static final String VERSION_STRING = String.format("%s %d.%d%d" , "Beta" , 1 , 1 , 6);

	/**
	 * Containers for program files and assets.
	 */
	public static final CSFolder
		assetsRoot = CSFolder.establishRoot("assets") ,
		dataRoot = CSFolder.establishRoot("data") ,
		programRoot = CSFolder.establishRoot("program") ,
		exportsRoot = CSFolder.establishRoot("exports") ,
		debugRoot = CSFolder.establishRoot("debug");
	
	private static boolean isDebug = false , useSteam = true;
	
	/**
	 * Contains reserved strings that cannot be used as script names, and are script names that cannot be uploaded to the Workshop.
	 */
	public static final List<String> reservedScriptNames = List.of(
		"" , 
		".py" ,
		"__ExampleArtboardScript.py" ,
		"__DiagonalBrush.py" ,
		"__ExamplePalette.py" ,
		"__ExampleProjectScript.py" ,
		"__SimpleSelectorBrush.py" ,
		"__SimpleBrush.py"
	);
	
	/**
	 * Called from the main method to do any initialization that must occur before any other code from the program is invoked.
	 * 
	 * @param programArgs — arguments to the main method
	 */
	static void preinitialize(final String[] programArgs) {

		//initialize logging
		try {
			
			Logging.initialize(true);
						
		} catch(IOException e) {
			
			e.printStackTrace();
			System.exit(-1);
			
		}
		
		//parse arguments
		List<String> args = List.of(programArgs);
		if(!args.isEmpty()) syserr("args: " + args);
		if(args.contains("-d")) preinitializeDebug();
		if(args.contains("-ns")) useSteam = false;

		stbi_flip_vertically_on_write(true);
		
		//load the ControlChord class and Hotkey classes for the purposes of the user settings2
		Hotkey.COPY_REGION_HOTKEY.isKeyboard();
		ControlChord.NEW_PROJECT.isKeyboard();
		
	}
		
	/**
	 * Invoked when the {@code -d} argument is passed to the program, the enable debug argument.
	 */
	private static void preinitializeDebug() { 
		
		isDebug = true;		
		sysDebug("[COLDSTEEL SPRITE STUDIO DEBUG ENABLED]");		
		Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
		
	}

	/**
	 * Returns whether this runtime is in debug mode.  
	 * 
	 * @return {@code true} if this runtime is in debug mode.
	 */
	public static boolean isDebug() {
		
		return isDebug;
		
	}
	
	/**
	 * Returns whether the Steam API was initialized.
	 * 
	 * @return {@code true} if Steam was initialized.
	 */
	public boolean isSteamInitialized() {
		
		return useSteam && steam.initialized();
		
	}
	
	/**
	 * Returns whether the given string {@code equals} any elements of {@link Engine#reservedScriptNames}.
	 * 
	 * @param name — name of a script
	 * @return {@code true} if {@code name} equals one of the reserved script names.
	 */
	public static boolean isReservedScriptName(String name) {
		
		for(String x : reservedScriptNames) if(x.equals(name)) return true;
		for(String x : reservedScriptNames) if(x.length() > 3 && name.equals(x.substring(0 , x.length() - 3))) return true;		
		return false;
		
	}
	
	/**
	 * Frees static memory associated with COLDSTEEL Core and the program.
	 */
	static void finalShutDown() {
		
		THE_THREADS.shutdown();
		CSDisplay.finalShutDown();
		
		
	}
	
	/**
	 * Contains the renderer, window, and CSNuklear, a UI factory object. If this is assigned to null, the program will immediately close. 
	 * If this is null, its because Steam is going to restart the application.
	 */
	private final CSDisplay display;
	private final Editor editor;
	private CSSSCamera camera;
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
		realtimeMode = false ;

	private int realtimeTargetFPS = 60;
	private double realtimeFrameTime = 1000 / realtimeTargetFPS;

 	private Timer frameTimer = new Timer();
 	
	private final UserSettings2 settings2 = new UserSettings2();
	
	private final NanoVG nanoVG;
	
	public final List<NamedNanoVGTypeface> loadedFonts = Collections.synchronizedList(new ArrayList<>());

	private Steamworks steam;	
	
	/**
	 * Kept for the benefit of dragging the camera around.  
	 */
	private float previousCursorX , previousCursorY;
	
	private CursorDragState cursorDragState = CursorDragState.NOT_DRAGGING;
	
	/**
	 * Constructs the engine and initializes its members.
	 * <p>
	 * 	This constructor creates and initializes directories the program uses, creates a window, OpenGL context and UI factory, camera, 
	 * 	the control system, the Python interpreter, the editor, which provides tools to create art, and sets other peripheral callbacks. 
	 * </p>
	 */
	Engine() {
		
		if(useSteam && !initializeSteam()) { 
			
			//early exit because we need to restart for Steam
			nanoVG = null;
			display = null;
			editor = null;
			return;
						
		}
		
		initializeDirectories();
		
		display = new CSDisplay(true , "COLDSTEEL Sprite Studio" , 18 , "assets/fonts/FiraSansBold.ttf");
				
		CSNuklearRender.BUFFER_INITIAL_SIZE(12 * 1024);
		UIUtils.setFontWidthGetter(display.nuklear.font().width());
				
		initializeCamera();

		nanoVG = display.renderer.make(() -> new NanoVG(display.window , camera , true)).get();
		nanoVG.coordinateSpace(CoordinateSpace.WORLD_COORDINATE_SPACE);
		
		initializeNanoVGFonts();
		
		setControlCallback();
						
		openGLStateInitialize();
		
		editor = new Editor(this , display);

		settings2.read(this , editor);

		setOnScroll();	
		setOnMouseInput();		
		setOnFileDrop();
		setOnIconify();
		
		enqueueRender();	
		
		CSSSException.registerTheEngine(this);
		
		CSUtils.onRequirementFailed = message -> new CSSSException(message , new RequirementFailedException(message));
		CSUtils.onSpecificationBroken = message -> new CSSSException(message , new SpecificationBrokenException(message));
	
	}
	
	/**
	 * Contains the main loop of the application.
	 * 
	 * <p>
	 * 	Typically, Sprite Studio runs on a largely event driven architecture and will 'wait' for inputs to be sent from peripherals before
	 * 	running a main loop frame. It is also possible however to run in a more realtime mode, in which a fixed number of loop iterations
	 * 	occur every second.
	 * </p>
	 */
	void run() {

		if(display == null) return;

		while(display.persist()) {
			
			getInputs();
			
			controlEvents();
			
			editor.update();
			
			runProjectFreemove();
			
			THE_TEMPORAL.updateAllEvents();
			
			renderScene();
			
			updatePreviousCursorPositions();
			
			if(isSteamInitialized()) SteamAPI.runCallbacks();

			realtimeFrameLockup();
			
		}

	}
	
	private void renderScene() {
		
		if(renderScene.isFinished()) enqueueRender();

	}
	
	private void enqueueRender() {

		display.layoutAllUserInterfaces();
		
		renderScene = display.renderer.post(() -> {
			
			glClear(GL_COLOR_BUFFER_BIT);
			//NanoVG modifies blending
			glBlendFunc(GL_SRC_ALPHA , GL_ONE_MINUS_SRC_ALPHA);

			try(NanoVGFrame frame = nanoVG.frame()) {

				if(currentProject != null) {
	
					CSSSProject.currentShader().updatePassVariables(camera.projection() , camera.viewTranslation());				
					currentProject.renderAllArtboards();
					currentProject.renderAllVectorTextBoxes(frame);

					editor.renderSelectingBrushRender();				
					editor.renderSelectingBrushBounder(frame);
					editor.renderModifyingBrushBounder(frame);

				}

				//render UI
				display.nuklear.render();
				
				//renders the current palette as long as state allows
				editor.renderPalette();
				
				editor.renderPaletteBounder(frame);				
				
			}

			/*
			 * Here is where we render the current frame of the active animation within the animation editor menu. To do this, I rerender
			 * the artboard that is the current frame, applying a translation to it so it sits on top of the UI element, hence why this is
			 * done after the UI is rendered. 
			 */
			if(currentProject != null) {
				
				Animation current = currentProject.currentAnimation();
				if(current != null && editor.isAnimationPanelShowing()) {

					current.renderCurrentFrame(camera , editor.animationPanel() , display.window.size()[1]);
					
				}
				
			}
			
			windowSwapBuffers();
			
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
	 * Sets the previous cursor coordinates.
	 */
	private void updatePreviousCursorPositions() {

		float[] cursorCoords = getCursorWorldCoords();

		if(Control.TWO_D_CURSOR_DRAG.pressed() && cursorDragState == CursorDragState.NOT_DRAGGING) {
			
			float xDistance = Math.abs(previousCursorX - cursorCoords[0]);
			float yDistance = Math.abs(previousCursorY - cursorCoords[1]);
			cursorDragState = xDistance > yDistance ? CursorDragState.DRAGGING_HORIZONTAL : CursorDragState.DRAGGING_VERTICAL;
			
		} else if(!Control.TWO_D_CURSOR_DRAG.pressed()) cursorDragState = CursorDragState.NOT_DRAGGING;
		
		//only update the previous coordiante if the drag state is not locked into the other axis.
		if(cursorDragState != CursorDragState.DRAGGING_HORIZONTAL) previousCursorY = cursorCoords[1];		
		if(cursorDragState != CursorDragState.DRAGGING_VERTICAL) previousCursorX = cursorCoords[0];
		
	}
	
	/**
	 * Allows the user to move artboards and animations around freely.
	 */
	private void runProjectFreemove() {
		
		if(currentProject != null) { 
			
			float[] cursorWorldCoords = getCursorWorldCoords();
			display.renderer.post(() -> currentProject.runFreemove(cursorWorldCoords));
			
		}
		
	}
	
	/**
	 * Checks the states of controls and invokes their actions.
	 */
	private void controlEvents() {
		
		Control.updateAllControls();
		
		if(currentProject != null) {
			
			//not the best way to do this 
			if(ControlChord.NEW_ANIMATION.struck()) startNewAnimation();
			else if(ControlChord.NEW_ARTBOARD.struck()) startNewArtboard();
			if(ControlChord.NEW_NON_VISUAL_LAYER.struck()) startNewNonVisualLayer();
			if(ControlChord.NEW_VISUAL_LAYER.struck()) startNewVisualLayer();
			
		}
		
		int numberOpenDialogues = Dialogue.numberOpenDialogues();
		
		if(!isCursorHoveringUI() && numberOpenDialogues == 0) {
			
			if(Control.CAMERA_UP.pressed()) camera.translate(0 , cameraMoveSpeed);			
			if(Control.CAMERA_DOWN.pressed()) camera.translate(0 , -cameraMoveSpeed);			
			if(Control.CAMERA_LEFT.pressed()) camera.translate(-cameraMoveSpeed , 0);			
			if(Control.CAMERA_RIGHT.pressed()) camera.translate(cameraMoveSpeed , 0);

		} 
		//try to move the animation panel view of the current frame
		else if(
			currentProject != null && 
			editor.isAnimationPanelShowing() && 
			currentProject.currentAnimation() != null && 
			numberOpenDialogues == 0
		) {
			
			if(Control.CAMERA_UP.struck()) editor.animationPanel().translate(0 , cameraMoveSpeed); 		
			if(Control.CAMERA_DOWN.struck()) editor.animationPanel().translate(0 , -cameraMoveSpeed);
			if(Control.CAMERA_LEFT.struck()) editor.animationPanel().translate(-cameraMoveSpeed , 0);
			if(Control.CAMERA_RIGHT.struck()) editor.animationPanel().translate(cameraMoveSpeed , 0);
					
		}
		//try to move the palette UI
		else if (numberOpenDialogues == 0 && currentProject != null && currentProject.currentPalette() != null) {

			if(Control.CAMERA_UP.struck()) editor.artboardPaletteUI().translate(0 , cameraMoveSpeed); 		
			if(Control.CAMERA_DOWN.struck()) editor.artboardPaletteUI().translate(0 , -cameraMoveSpeed);
			if(Control.CAMERA_LEFT.struck()) editor.artboardPaletteUI().translate(-cameraMoveSpeed , 0);
			if(Control.CAMERA_RIGHT.struck()) editor.artboardPaletteUI().translate(cameraMoveSpeed , 0);
						
		}
		
		if(Control.TOGGLE_FULLSCREEN_HOTKEY.struck()) toggleFullScreen();
		if(numberOpenDialogues == 0) Hotkey.updateHotkeys(editor);
		if(ControlChord.NEW_PROJECT.struck()) startNewProject(); 
		
	}

	/**
	 * Returns the UGC instance used by this application for communicating with the SteamAPI, or {@code null} if 
	 * {@code !Engine.isSteamInitialized()}.
	 * 
	 * @return The {@link cs.ext.steamworks.UGC UGC}.
	 */
	public UGC UGC() { 
		
		return steam.UGCAPI();
		
	}
	
	/**
	 * Gets the coordinates of the cursor as world space coordinates by converting their screen space coordinates to world space.
	 * 
	 * @return The world coordinates of the cursor, where {@code getCursorWorldCoords()[0]} is the X world coordinate, and 
	 * 		   {@code getCursorWorldCoords()[1]} is the Y world coordinate.
	 */
	public float[] getCursorWorldCoords() {
		
		double[] coords = display.window.cursorPosition();
		float[] worldCoords = getCursorWorldCoords(new float[] {(float) coords[0] , (float) coords[1]});
		//set the direction we are not dragging to the previous cursor's position for that axis.
		if(cursorDragState == CursorDragState.DRAGGING_HORIZONTAL) worldCoords[1] = previousCursorY;
		else if(cursorDragState == CursorDragState.DRAGGING_VERTICAL) worldCoords[0] = previousCursorX;
		
		return worldCoords;
		
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
	
	/**
	 * Gets the screen coordinates of the cursor.
	 * 
	 * @return The screen coordinates of the cursor. 
	 */
	public int[] getCursorScreenCoords() {
		
		double[] cursorPos = display.window.cursorPosition();
		return new int[] {(int)cursorPos[0] , (int)cursorPos[1]};
		
	}
	
	/**
	 * Gets the current artboard.
	 * 
	 * @return The current artboard.
	 */
	public Artboard currentArtboard() {
		
		return currentProject.currentArtboard();
		
	}
	
	/**
	 * Returns the NanoVG.
	 * 
	 * @return The NanoVG.
	 */
	public NanoVG nanoVG() {
		
		return nanoVG;
		
	}
	
	/**
	 * Returns the standard renderer.
	 * 
	 * @return The standard renderer.
	 */
	public CSStandardRenderer renderer() {
		
		return display.renderer;
		
	}
	
	/**
	 * Sets the camera's move rate.
	 * 
	 * @param moveSpeed — a new move rate for the camera
	 */
	public void cameraMoveRate(final int moveSpeed) {
		
		this.cameraMoveSpeed = moveSpeed;
		
	}

	/**
	 * Returns the camera move rate.
	 * 
	 * @return The camera move rate.
	 */
	public int cameraMoveRate() {
		
		return cameraMoveSpeed;
		
	}

	/**
	 * Returns the current project.
	 * 
	 * @return The current project.
	 */
	public CSSSProject currentProject() {
		
		return currentProject;
		
	}

	/**
	 * Returns the current animation.
	 * 
	 * @return The current animation.
	 */
	public Animation currentAnimation() {
		
		if(currentProject == null || currentProject.currentAnimation() == null) return null;
		return currentProject.currentAnimation();
		
	}
	
	/**
	 * Sets the current project to {@link cs.csss.annotation.Nullable @Nullable} {@code project}. 
	 * 
	 * @param project — {@code @Nullable} new current project
	 */
	public void currentProject(CSSSProject project) {
		
	 	if(currentProject != null) editor.eventPush(new ShutDownProjectEvent(currentProject));		
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
	
	/**
	 * Returns the x position of the cursor in world space last frame.
	 * 
	 * @return X position of the cursor in world space last frame. 
	 */
	public float previousCursorX() {
		
		return previousCursorX;
		
	}

	/**
	 * Returns the y position of the cursor in world space last frame.
	 * 
	 * @return Y position of the cursor in world space last frame. 
	 */
	public float previousCursorY() {
		
		return previousCursorY;
		
	}
	
	/* UI ELEMENTS */
	
	/**
	 * Creates an UI element for creating a project.
	 */
	public void startNewProject() {
		
		NewProjectMenu newProjectMenu = new NewProjectMenu(display.nuklear);
		
		Engine.THE_TEMPORAL.onTrue(newProjectMenu::canFinish , () -> {
			
			if(newProjectMenu.get() == null) return;
			currentProject = display.renderer.make(() -> {
				
				CSSSProject project = new CSSSProject(this , newProjectMenu.get() , newProjectMenu.channelsPerPixel());				
				project.initialize();
				return project;
				
			}).get();
			
		});		
		
	}

	/**
	 * Creates an UI element for creating an animation.
	 */
	public void startNewAnimation() {
		
		if(currentProject == null) return;
		NewAnimationMenu newAnimationMenu = new NewAnimationMenu(currentProject , display.nuklear);		
		Engine.THE_TEMPORAL.onTrue(newAnimationMenu::isFinished, () -> currentProject.createAnimation(newAnimationMenu.get()));
		
	}

	/**
	 * Creates an UI element for creating a new visual layer.
	 */
	public void startNewVisualLayer() {
		
		if(currentProject == null) return;
		
		NewVisualLayerMenu newLayerMenu = new NewVisualLayerMenu(currentProject , display.nuklear);
		
		THE_TEMPORAL.onTrue(newLayerMenu::isFinished , () -> {
			
			if(newLayerMenu.canCreate()) currentProject.createVisualLayer(newLayerMenu.name()); 
			
		});
		
	}

	/**
	 * Creates an UI element for creating a new nonvisual layer.
	 */
	public void startNewNonVisualLayer() {
		
		if(currentProject == null) return;
		NewNonVisualLayerMenu newLayerMenu = new NewNonVisualLayerMenu(currentProject , display.nuklear);
		
		THE_TEMPORAL.onTrue(newLayerMenu::isFinished , () -> {
			
			if(newLayerMenu.canCreate()) currentProject.createNonVisualLayer(newLayerMenu.name(), newLayerMenu.channels());
			
		});
		
	}

	/**
	 * Creates an UI element for creating an artboard.
	 */
	public void startNewArtboard() {
		
		if(currentProject == null) return;
		
		ArtboardMenu artboardMenu = new ArtboardMenu(display.nuklear , "New Artboard");
		THE_TEMPORAL.onTrue(artboardMenu::finished , () -> {
			
			if(!artboardMenu.finishedValidly()) return;
			
			Artboard artboard = display.renderer.make(() -> {
				
				return currentProject.createArtboard(artboardMenu.width() , artboardMenu.height());
				
			}).get();
			
			
			display.renderer.addRender(artboard.render());			
			if(currentProject.currentArtboard() == null) currentProject.currentArtboard(artboard);
			
		});
		
	}

	/**
	 * Creates an UI element for editing controls.
	 */
	public void startNewControlsEditor() {
		
		new ModifyControlsMenu(display.nuklear , this);
				
	}

	/**
	 * Creates an UI element for setting stats for the checkered background.
	 */
	public void startCheckeredBackgroundSettings() {
		
		new CheckeredBackgroundSettingsMenu(editor , display.nuklear , currentProject);
		
	}

	/**
	 * Creates an UI element for selecing any type of script if a valid Python installation was found at startup. If none was found, an error
	 * notification appears which has a link to download Python.
	 */
	public void startSelectScriptMenu(String scriptSubdirectory , Consumer<File> onComplete) {
		
		if(currentProject == null) return;		
		SelectScriptMenu script = new SelectScriptMenu(display.nuklear , scriptSubdirectory);
		
		THE_TEMPORAL.onTrue(script::readyToFinish , () -> {
			
			File selected;			
			if((selected = script.selectedScript()) == null) return;			
			onComplete.accept(selected);
			
		});
			
	}
	
	/**
	 * Creates an UI element for selecing a custom time input.
	 * 
	 * @param animationFrameIndex — an animation frame index
	 */
	public void startAnimationFrameCustomTimeInput(int animationFrameIndex) {
		
		String title = "Frame " + animationFrameIndex + " custom speed";
		
		AnimationFrame frame = currentProject.currentAnimation().getFrame(animationFrameIndex);
		AnimationSwapType type = frame.swapType();
		
	 	NkPluginFilter filter = switch(type) {
		 	case SWAP_BY_TIME -> {

		 		title += " (millis)";
		 		yield CSNuklear.ONLY_FLOATS_FILTER;
		 		
		 	}
		 	case SWAP_BY_UPDATES -> {

		 		title += " (frames)";
		 		yield CSNuklear.DECIMAL_FILTER;
		 		
		 	}
	 	};
	 	
		new InputBox(display.nuklear , title , .4f , .4f , 10 , filter , result -> {
			
			if(result.equals("")) return;
			
			switch(type) {
				case SWAP_BY_TIME -> frame.time(new FloatReference(Float.parseFloat(result)));
				case SWAP_BY_UPDATES -> frame.updates(new CSRefInt(Integer.parseInt(result)));
			}
							
		});
		
	}

	/**
	 * Creates an UI element for moving an animation frame position.
	 * 
	 * @param originalIndex — the original index of the animation frame  
	 */
	public void startSetAnimationFramePosition(int originalIndex) {
		
		Animation current = currentAnimation();
		
		new DetailedInputBox(
			display.nuklear , 
			"New Position For Frame " + originalIndex , 
			"Set the new position of frame " + originalIndex + ", from 0 to " + current.numberFrames() ,
			.5f - (.15f / 2f) ,
			.5f - (.15f / 2f), 
			.15f ,
			.15f ,
			CSNuklear.DECIMAL_FILTER ,
			3 ,
			result -> {
				
				try {
					
					Integer asInt = Integer.parseInt(result);
					int size = current.numberFrames(); 
					if(asInt >= size) asInt = size - 1;
					else if (asInt < 0) asInt = 0;
					current.setFramePosition(originalIndex , asInt);

				} catch(NumberFormatException e) {
					
					sysDebug(e);
					
				}
								
			}
			
		);
		
	}
	
	/**
	 * Creates an UI element for setting the simulation frame rate during realtime mode.
	 */
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

	/**
	 * Creates an UI element for uploading a custom script to the Steam workshop.
	 */
	public void startSteamWorkshopItemUpload() {
		
		/*
		 * Item Uploading goes as follows: 
		 * 	Create a UI element to collect all the data a workshop item needs
		 * 	Call into the Steam API to create an item ID
		 * 	Set all the collected data from the UI for an update
		 * 	Update the item.
		 * 
		 * When we call into Steam to create a new workshop item, we need to wait until it returns the PublishedItemID, which we need to store.
		 * We use the ID to start an update, and all future updates, and once we are finished, we send the update to Steam. 
		 */
		
		if(SteamWorkshopItemUploadMenu.menuOpen()) return;
		else if(WorkshopUploadHelper.newItemUpdateInProgress()) {
			
			newNotificationBox("Item Uploading", "A Workshop item is uploading right now, please wait until it finishes to upload another item.");
			return;
			
		}
		
		SteamWorkshopItemUploadMenu upload = new SteamWorkshopItemUploadMenu(display.nuklear , steam.friendsAPI());
		
		THE_TEMPORAL.onTrue(upload::finished , () -> {
			
			if(!upload.finishedValidly()) return;
			UGC ugc = UGC();
			
			//create item will create a workshop item ID and invoke our bound callback.
			//we need to wait for the callback to be invoked and cache the ID it produces.
			//this call will trigger the WorkshopHelper::newItemUpdateInProgress to be true
			ugc.createItem(SteamApplicationData.steamAppID(), WorkshopFileType.Community);
			
			//pass the data from the UI element to the WorkshopItemData created for this item once it is ready.
			
			THE_TEMPORAL.onTrue(WorkshopUploadHelper::newItemUpdateInProgress , () -> WorkshopUploadHelper.updateNewItem(
				this ,
				ugc ,
				upload.name(),
				upload.description() ,
				upload.scriptType(),				
				upload.visibility() ,
				upload.tags() ,
				upload.previewFilePath() ,
				upload.scriptFilePath()			
			));
						
		});
		
	}

	/**
	 * Creates a menu for updating a workshop item and handles updating the selected item once finished.
	 */
	public void startSteamWorkshopItemUpdateMenu() {
		
		/*
		 * This operation goes as follows:
		 * 	Delete the old folder containing the selected item
		 *  Create a new folder for the item
		 *  Upload the updates to the workshop item
		 */
		
		SteamWorkshopItemUpdateMenu updateMenu = new SteamWorkshopItemUpdateMenu(display.nuklear, steam.friendsAPI() , this);
		
		THE_TEMPORAL.onTrue(updateMenu::readyToClose , () -> {
			
			if(!updateMenu.finishedValidly()) return;
			
			WorkshopUploadHelper.updateExistingItem(
				this , 
				steam.UGCAPI() , 
				updateMenu.item() , 
				updateMenu.name() , 
				updateMenu.description() ,
				updateMenu.visibility() ,
				updateMenu.tags() ,
				updateMenu.scriptFilePath() ,
				updateMenu.previewFilePath() ,
				updateMenu.changeLogInput()
			);
		
		});
		
	}
	
	/**
	 * Creates an UI element for moving the rank of a visual layer.
	 * 
	 * @param layer — a layer to rearrange
	 */
	public void startMoveLayerRank(VisualLayer layer) {
		
		int rank = currentProject.currentArtboard().getLayerRank(layer);
		
		new InputBox(display.nuklear , "Input New Rank for Layer " + rank , 0.4f , 0.4f , 4 , CSNuklear.DECIMAL_FILTER , result -> {
			
			if(result.equals("")) return;
			Artboard current = currentProject.currentArtboard();
			int res = Integer.parseInt(result);
			if(res >= current.numberVisualLayers()) res = current.numberVisualLayers() - 1;
			if(res < 0) res = 0;
			
			editor.eventPush(new MoveLayerRankEvent(current , res));
	
		});
			
	}
	
	/**
	 * Creates an UI element for setting the swap type of an animation frame.
	 * 
	 * @param frameIndex — index of an animation frame
	 */
	public void startSetAnimationFrameSwapType(int frameIndex) {
		
		SetAnimationFrameSwapTypeMenu animationFrameSwapTypeMenu = new SetAnimationFrameSwapTypeMenu(display.nuklear , frameIndex);
		
		THE_TEMPORAL.onTrue(animationFrameSwapTypeMenu::finished , () -> {
			
			if(animationFrameSwapTypeMenu.swapType() == null) return;
			currentAnimation().setFrameSwapType(frameIndex, animationFrameSwapTypeMenu.swapType());
			
		});
		
	}
	
	/**
	 * Creates an UI element for inputting arguments for scripts.
	 * 
	 * @param scriptName — name of a script
	 * @param popupMessage — optional string for the UI that 
	 * @param onFinish — code to invoke on completion of the UI element
	 */
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
	
	/**
	 * Creates an UI element for loading project.
	 */
	public void startLoadProject() {
		
		LoadProjectMenu menu = new LoadProjectMenu(display.nuklear);
		
		THE_TEMPORAL.onTrue(menu::readyToFinish, () -> {
			
			if(menu.get() == null || menu.get().equals("")) return;
			loadProject(menu.get());
			
		});
		
	}
	
	/**
	 * Attempts to load the {@code CTSP} file named {@code projectFileName}.
	 * 
	 * @param projectFileName — name of the project to load
	 */
	public void loadProject(String projectFileName) {

		CTSPFile file = new CTSPFile(projectFileName);
		
		try {
			
			file.read();
			display.renderer.post(() -> currentProject(new CSSSProject(this , file)));
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
	}
	
	/**
	 * Creates a UI element for exporting a project.
	 */
	public void startExport() {
		
		if(currentProject != null) new ProjectExporterUI(this , display.nuklear , currentProject);
		
	}
	
	/**
	 * Creates a UI element for adding text.
	 */
	public void startAddText() {
		
		if(currentProject == null) return;
		VectorTextMenu menu = new VectorTextMenu(display.nuklear , this , currentProject);
		
		THE_TEMPORAL.onTrue(menu::finished, () -> {
			
			NamedNanoVGTypeface selected = menu.selectedTypeface();
			if(selected == null) return;
			String typed = menu.inputString();
			
			currentProject.addVectorTextBox(selected.typeface() , typed);
			
		});
		
	}
	
	/**
	 * Creates an UI element for saving projects under a new name.
	 */
	public void startProjectSaveAs() {

		new InputBox(display.nuklear , "Save As" , .4f , .4f , 999 , CSNuklear.NO_FILTER , result -> {
			
			if(result.equals("")) return;
			saveProject(result);
							
		});
		
	}

	/**
	 * Creates an UI element for saving projects.
	 */
	public void saveProject() { 

		if(currentProject == null) return;
		saveProject(currentProject.name());		
		
	}
	
	/**
	 * Saves the current project under {@code name}.
	 * 
	 * @param name — name for a save file
	 */
	public void saveProject(String name) {

		if(currentProject == null) return;

		try {
			
			new CTSPFile(currentProject, name).write();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
	
	}

	public void startCreateNewScript() {
		
		NewScriptMenu newMenu = new NewScriptMenu(display.nuklear);
		THE_TEMPORAL.onTrue(newMenu::finished, () -> {
			
			ScriptType x = newMenu.type();
			if(x == null) return;
			
			String scriptName = newMenu.nameInput();
			if(isReservedScriptName(scriptName)) return;

			CSFolder scripts = programRoot.getSubdirectory("scripts").getSubdirectory(x.associatedFolderName);
			File newScript = new File(scripts.getRealPath() + CSFolder.separator + scriptName + ".py");

			try {
				
				newScript.createNewFile();
				if(Desktop.isDesktopSupported()) Desktop.getDesktop().open(newScript);
				
			} catch (IOException e) {

				e.printStackTrace();
				
			}			
			
		});		
		
	}
	
	/**
	 * Begins the exit process of Sprite Studio.
	 */
	public void exit() {
		
		display.window.close();
		
	}
	
	/**
	 * Toggles on or off fullscreen mode.
	 */
	public void toggleFullScreen() { 
		
		isFullScreen = !isFullScreen;
		if(isFullScreen) display.window.goBorderlessFullScreen();
		else display.window.goNonFullScreen();
		
	}

	/**
	 * Sets the realtime mode of the application to {@code mode}.
	 * 
	 * @param mode — {@code true} if the realtime mode is to be enabled
	 */
	public void realtimeMode(boolean mode) {
		
		this.realtimeMode = mode;
		
	}	
		
	/**
	 * Returns the realtime mode of the application.
	 * 
	 * @return Realtime mode of the application.
	 */
	public boolean realtimeMode() {
		
		return realtimeMode;
		
	}
	
	/**
	 * Returns whether the mouse was pressed down on a UI element.
	 * 
	 * @return Whether the mouse was pressed down on a UI element.
	 */
	public boolean wasMousePressedOverUI() {
		
		return wasMousePressedOverUI;
		
	}

	void setRealtimeTargetFPS(int newTarget) {
		
		realtimeTargetFPS = newTarget;
		realtimeFrameTime = 1000 / realtimeTargetFPS;
		
	}
	
	/**
	 * Returns the time a frame should take in realtime mode.
	 * 
	 * @return Time a frame should take in realtime mode.
	 */
	public double realtimeFrameTime() {
		
		return realtimeFrameTime;
		
	}
	
	/**
	 * Returns the amount of frames per second of the realtime mode.
	 * 
	 * @return Frames per second of the realtime mode.
	 */
	public int realtimeTargetFPS() {
		
		return realtimeTargetFPS;
		
	}
	
	/**
	 * Returns the camera.
	 * 
	 * @return The camera.
	 */
	public CSSSCamera camera() {
		
		return camera;
		
	}
	
	private void initializeDirectories() {

		assetsRoot.seekExistingFiles();
		dataRoot.seekExistingFiles();
		programRoot.seekExistingFiles();
		exportsRoot.seekExistingFiles();
		debugRoot.seekExistingFiles();
		
	}
	
	private void openGLStateInitialize() {

		//initialization of rendering
		display.renderer.post(() -> {

			GPUMemoryViewer.initialize();
			
			CSSSProject.initializeArtboardShaders();

			glClearColor(0.15f , 0.15f , 0.15f , 1.0f);

			glEnable(GL_BLEND);
			glBlendEquation(GL_FUNC_ADD);
			glBlendFunc(GL_SRC_ALPHA , GL_ONE_MINUS_SRC_ALPHA);

		}).await();

	}
	
	private void setOnScroll() {

		display.window.onScroll((xOffset , yOffset) -> {
			
			if(!isCursorHoveringUI()) camera.zoom(yOffset < 0);
			else if (editor.isAnimationPanelShowing() && currentProject.currentAnimation() != null) { 
				
				if(editor.cursorHoveringAnimationFramePanel()) editor.animationPanel().zoom(yOffset < 0);
								
			} else if (editor.cursorHoveringPaletteUI()) editor.artboardPaletteUI().zoom(yOffset < 0);
			
		});
		
	}
	
	private void setOnMouseInput() {

		//this sets the wasMousePressedOverUI boolean, which stops you from clicking on a UI element, holding down, and dragging onto an 
		//artboard, causing you to color it when you probably didnt want to.
		display.window.onMouseButtonInput((button , action , mods) -> {
			
			if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS && isCursorHoveringUI()) wasMousePressedOverUI = true;
			else if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) wasMousePressedOverUI = false;

		});
	
	}
	
	private void setOnFileDrop() {
		
		display.window.onFileDrop((files) -> {
			
			for(String x : files) {
				
				File asFile = new File(x);
				if(asFile.exists()) {
					
					if(asFile.isDirectory()) {
						
						THE_TEMPORAL.onTrue(() -> !currentlyRendering() , () -> ProjectExporterUI.registerExportLocation(x));
						
					} else {
						
						if(currentProject != null && (x.endsWith(".png") /* TODO: || x.endsWith(".jpg") */|| x.endsWith(".bmp"))) {
							
							ImageImporter.registerNewImportFilePaths(x);
							
						}
												
					}
					
				}
				
			}
			
			DroppedFileAcceptingDialogue.acceptDroppedFilePaths(files);
			
			if(currentProject != null) importSelectedFiles();
			
		});
		
	}
	
	private void setOnIconify() {
		
		display.window.onIconify(isIconified -> {
			
			if(isIconified) editor.animationPanel().hide();
			
		});
		
	}
	
	private void setControlCallback() {

		//sets a callback to invoke for controls to determine whether they are pressed or not.
		Control.checkPressedCallback((keyCode , isKeyboard) -> isKeyboard ? 
			display.window.isKeyPressed(keyCode) : 
			display.window.isMouseButtonPressed(keyCode)
		);

	}
	
	private void initializeCamera() {

		int[] windowSize = display.window.size();
		camera = new CSSSCamera(windowSize[0] , windowSize[1]);		
		display.window.onFramebufferResize(camera::resetProjection);
		
	}
	
	/**
	 * Removes a render object from the renderer.
	 * 
	 * @param render — render to remove
	 * @return {@code Await} object that will return finished once the object is freed.
	 */
	public Await removeRender(CSRender render) {
		
		return display.renderer.post(() -> {
			
			display.renderer.removeRender(render);
			render.shutDown();
			
		});
		
	}
	
	/**
	 * Swaps buffers.
	 */
	@RenderThreadOnly public void windowSwapBuffers() {
		
		display.window.swapBuffers();
		
	}
	
	/**
	 * Resets the viewport and background colors.
	 */
	@RenderThreadOnly public void resetViewport() {
		
		int[] framebufferSize = display.window.framebufferSize();
		glViewport(0 , 0 , framebufferSize[0] , framebufferSize[1]);
		glClearColor(0.15f , 0.15f , 0.15f , 1.0f);
		
	}
	
	/**
	 * Returns whether the renderer is currently rendering.
	 * 
	 * @return Whether the renderer is currently rendering.
	 */
	public boolean currentlyRendering() {
		
		return !renderScene.isFinished();
		
	}
	
	/**
	 * Returns the window size.
	 * 
	 * @return The window size.
	 */
	public int[] windowSize() {
		
		return display.window.size();
		
	}
	
	/**
	 * Sets the contents of {@code destination} to all the workshop items the user has created. 
	 */
	@InDevelopment public void getCreatedWorkshopItems() {
	
		THE_THREADS.submit(() -> {
			
		 	int accountID = steam.userAPI().getSteamID().getAccountID();
		 	int appID = SteamApplicationData.steamAppID(); 
			UGCQuery query = null;
			UGC ugc = steam.UGCAPI();
			try {
				
				query = ugc.createQueryUserUGCRequest(
					accountID , 
					UserUGCList.Published , 
					MatchingUGCType.Items , 
					UserUGCListSortOrder.CreationOrderDesc ,
					appID ,
					appID ,
					1
				);
								
//				query.setCloudFileNameFilter("");
				query.setReturnOnlyIDs(false);
//				query.setReturnKeyValueTags(false);
//				query.setReturnLongDescription(false);
//				query.setReturnMetadata(false);
//				query.setReturnChildren(false);
//				query.setReturnAdditionalPreviews(false);
//				query.setReturnTotalOnly(true);
				query.setLanguage(SteamLanguages.English);
				//not sure about this one
//				query.setAllowedCachedResponse(1);

				query.send();
				
			} catch (BadQueryException e) {
				
				e.printStackTrace();
				
			} finally {
				
				if(query != null) query.shutDown();
		
			}
			
		});
		
	}
	
	/**
	 * Carries out the process of importing files that have been dropped into the application.
	 */
	public void importSelectedFiles() {
		
		ImageImporter.forEachImportPath(x -> {
			
			File asFile = new File(x);
			String name = asFile.getName();
			ImageImporter importer = new ImageImporter(x , currentProject.channelsPerPixel());
			display.renderer.post(() -> {
				
				Artboard newArtboard = currentProject.createArtboard(name, importer.width(), importer.height() , false);
				importer.copyToArtboard(newArtboard);
				importer.shutDown();
				
			});
			
		});
		
		//will happen after all the render events are posted
		display.renderer.post(() -> ImageImporter.clearRegisteredImportFilePaths());
		
	}
	
	/**
	 * Creates and returns a new notification box with the given strings.
	 * 
	 * @param title — title for the resulting notification box
	 * @param message — message the resulting notification box will contain
	 * @return Newly created notification box.
	 */
	public NotificationBox newNotificationBox(String title , String message) {
		
		return new NotificationBox(title , message , display.nuklear);
		
	}
	
	/**
	 * Creates a notification box that forces the user to accept the steam workshop legal agreement.
	 */
	public void startWorkshopLegalAgreementPopup() {
		
		new NotificationBox(
			"Accept Workshop Legal Agreement" , 
			"You need to accept the workshop legal agreement to upload to the workshop" ,
			display.nuklear ,
			() -> steam.friendsAPI().activateGameOverlayToWebPage(
				"https://steamcommunity.com/sharedfiles/workshoplegalagreement" , 
				OverlayToWebPageMode.Default)			
		);
		
	}
	
	/**
	 * Returns whether the application is in fullscreen mode.
	 * 
	 * @return Whether the application is in fullscreen mode.
	 */
	public boolean isFullscreen() {
		
		return isFullScreen;
		
	}
	
	/**
	 * Sets the sizes of the window.
	 * 
	 * @param width — new width of the window
	 * @param height — new height of the window
	 */
	public void setWindowSize(int width , int height) {
		
		display.window.size(width, height);
		
	}
	
	/**
	 * Returns the position of the window in virtual monitor space.
	 * 
	 * @return Position array of the window.
	 */
	public int[] getWindowPosition() {
		
		return display.window.position();
		
	}
	
	/**
	 * Moves the window to the given x and y coordinates in virtual monitor space.
	 * 
	 * @param x — x coordiante for the top left corner of the monitor
	 * @param y — y coordiante for the top left corner of the monitor
	 */
	public void setWindowPosition(int x, int y) {
		
		display.window.moveTo(x, y);
		
	}
	
	/**
	 * Returns the translation of the camera. 
	 * 
	 * @return Translation of the camera.
	 */
	public float[] getCameraTranslation() {
		
		float[] cameraPosition = new float[2];
		Vector3f translation = new Vector3f();
		camera.viewTranslation().getTranslation(translation);
		cameraPosition[0] = translation.x;
		cameraPosition[1] = translation.y;
		return cameraPosition;
		
	}
	
	private boolean initializeSteam() {

		steam = new Steamworks(2616440);
		
		try {

			if(steam.restartIfNeeded()) return false;
			
		} catch (SteamException e) {

			throw new IllegalStateException(e);

		}

		if(steam.initialized()) { 
			
			SpriteStudioSteamWorkshopCallbacks ugcCallbacks = new SpriteStudioSteamWorkshopCallbacks(this);
			steam.initializeUGCAPI(ugcCallbacks);			
			steam.initializeFriendsAPI(new SpriteStudioSteamFriendsCallbacks());
			steam.initializeUserAPI(new SpriteStudioSteamUserCallbacks());
			
			ugcCallbacks.setUGC(steam.UGCAPI());
		
			sysDebug("Number Subscribed Items" , steam.UGCAPI().getNumSubscribedItems());
			
			THE_THREADS.submit(() -> WorkshopDownloadHelper.initializeDownloads(steam.UGCAPI()));
				
		}
		
		return true;
		
	}
	
	private void initializeNanoVGFonts() {
		
		CSFolder fonts = CSFolder.getRoot("assets").getOrCreateSubdirectory("fonts");
		Iterator<CSFile> files = fonts.filesIterator();
		while(files.hasNext()) {
			
			CSFile file = files.next();
			String filepath = file.getRealPath();
			THE_THREADS.submit(() -> {
				
				TTF font = new TTF(14 , filepath);
				NanoVGTypeface nanoFont = nanoVG.createFont(font);
				loadedFonts.add(new NamedNanoVGTypeface(file.name() , nanoFont));
				font.shutDown();
				
			});
			
		}
		
	}
		
	@Override public void shutDown() {
		
		if(display == null) return;
		
		if(isSteamInitialized()) THE_THREADS.submit(SteamAPI::shutdown);		
		settings2.write(this, editor);
		
		debugLoggedShutDown(nanoVG::shutDown, "NanoVG");
		debugLoggedShutDown(editor::shutDown, "Editor");
				
		display.window.detachContext();
		sysDebug("Detached render context.");
		display.window.attachContext();
		sysDebug("Attached render context.");

		if(CSSSSelectingBrush.render != null) debugLoggedShutDown(() -> CSSSSelectingBrush.render.shutDown(), "Selecting Brush Render");
		if(currentProject != null) debugLoggedShutDown(currentProject::shutDown, "Project");
		
		if(!display.isFreed()) { 

			display.window.detachContext();
			sysDebug("Detached context.");
			debugLoggedShutDown(display::shutDown, "Display");
						
		}
		
		if(isSteamInitialized()) steam.shutDown();
				
	}

	@Override public boolean isFreed() {
		
		return display.isFreed();
		
	}
	
	private void debugLoggedShutDown(Lambda code , String shutDownName) {
		
		sysDebug("Shutting down " + shutDownName + "...");
		code.invoke();
		sysDebug(shutDownName + " shut down.");
		
	}
	
	private enum CursorDragState {
		
		NOT_DRAGGING ,
		DRAGGING_HORIZONTAL ,
		DRAGGING_VERTICAL;
		
	}
	
} 