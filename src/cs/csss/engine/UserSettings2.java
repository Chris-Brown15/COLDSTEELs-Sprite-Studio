package cs.csss.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cs.csss.editor.Editor;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.misc.textio.CSTextIO;
import cs.csss.project.CSSSProject;
import cs.csss.project.IndexTexture;
import cs.csss.project.io.CTSPFile;

/**
 * Class responsible for storing and retrieving user settings. This class is made for the purpose of scalability. It offers a single place to add and 
 * remove user settings.
 */
class UserSettings2 {

	private static final String fileName = "user settings2";	
	private static final CSFolder directory = CSFolder.getRoot("program");

	private static final String
		targetFPS 		= "TargetFPS",
		BGWidth 		= "BGWidth" , 
		BGHeight 		= "BGHeight", 
		stackSize 		= "StackSize", 
		wasFullScreen 	= "WasFullScreen" ,
		previousProject = "PreviousProject" ,
		windowWidth 	= "WindowWidth", 
		windowHeight 	= "WindowHeight", 
		windowX 		= "WindowX",
		windowY 		= "WindowY",
		cameraX 		= "CameraX",
		cameraY 		= "CameraY" ,
		cameraZoom		= "CameraZoom" ,
		currentTheme	= "Theme";
	
	
	UserSettings2() {}

	/**
	 * Writes the user setting file by binding the current state of all user setting fields and writes them to a file.
	 * 
	 * @param engine — the engine
	 * @param editor — the editor
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" }) void write(Engine engine , Editor editor) {

		try(CSTextIO writer = CSTextIO.createWriter(CSFile.makeFile(directory, fileName))) {

			CSSSProject currentProject = engine.currentProject();
			int[] windowSize = engine.windowSize();
			int[] windowPosition = engine.getWindowPosition();
			float[] cameraTranslation = engine.getCameraTranslation();
			UITheme theme = engine.currentTheme();
			String themeName;
			if(theme != null) themeName = theme.name();
			else themeName = "null";
				
			var values = Map.ofEntries(
				Map.entry(targetFPS, engine.realtimeTargetFPS()),
				Map.entry(BGWidth , IndexTexture.backgroundWidth),
				Map.entry(BGHeight, IndexTexture.backgroundHeight),
				Map.entry(stackSize, editor.undoCapacity()) ,
				Map.entry(wasFullScreen , engine.isFullscreen()) ,
				Map.entry(previousProject , currentProject != null ? currentProject.name() : "null") ,
				Map.entry(windowWidth, windowSize[0]) ,
				Map.entry(windowHeight, windowSize[1]) ,
				Map.entry(windowX, windowPosition[0]) ,
				Map.entry(windowY, windowPosition[1]) ,
				Map.entry(cameraX, cameraTranslation[0]) ,
				Map.entry(cameraY, cameraTranslation[1]) ,
				Map.entry(cameraZoom , engine.camera().zoom()) ,
				Map.entry(currentTheme, themeName)
			);
				
			writer.putMap("Stats", values, String::toString, String::valueOf);
			
			Object[] entries = Control.controls.stream()
				.map(control -> Map.entry(control.name, control.key() + (control.isKeyboard() ? "K" : "M")))
				.toArray();
			
			Entry[] cast = new Entry[entries.length];
			for(int i = 0 ; i < entries.length ; i++) cast[i] = (Entry)entries[i];
			
			writer.putMap("Controls", Map.ofEntries(cast), String::valueOf, String::valueOf);			
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Reads user setting data from the file and stores in the engine. 
	 * 
	 * @param engine — the engine
	 * @param editor — the editor
	 */
	void read(Engine engine , Editor editor) {
		
		if(!Files.exists(Paths.get(directory.getVirtualPath() + fileName))) return;
		
		try(CSTextIO reader = CSTextIO.createReader(new File(directory.getVirtualPath() + fileName))) {
			
			Map<String , String> stats = reader.getMap(HashMap::new, String::toString, String::valueOf).map();
			Map<String , String> controls = reader.getMap(HashMap::new, String::toString, String::valueOf).map();
			
			if(stats.containsKey(targetFPS)) engine.setRealtimeTargetFPS(Integer.parseInt(stats.get(targetFPS)));
			if(stats.containsKey(BGWidth)) IndexTexture.backgroundWidth = Integer.parseInt(stats.get(BGWidth));
			if(stats.containsKey(BGHeight)) IndexTexture.backgroundHeight = Integer.parseInt(stats.get(BGHeight));
			if(stats.containsKey(stackSize)) editor.setUndoAndRedoCapacity(Integer.parseInt(stats.get(stackSize)));
			if(stats.containsKey(wasFullScreen)) {			

				if(Boolean.parseBoolean(stats.get(wasFullScreen))) engine.toggleFullScreen();
				else {

					if(stats.containsKey(windowWidth)) {
						
						int width = Integer.parseInt(stats.get(windowWidth));
						int height = Integer.parseInt(stats.get(windowHeight));
						engine.setWindowSize(width , height);
						
					}
					
					if(stats.containsKey(windowX)) {

						int x = Integer.parseInt(stats.get(windowX));
						int y = Integer.parseInt(stats.get(windowY));
						engine.setWindowPosition(Math.max(x, 0) , Math.max(y , 0));
						
					}					

				}

			}
			
			if(stats.containsKey(previousProject)) {
				
				String previousProject = stats.get(UserSettings2.previousProject);
				if(!previousProject.equals("null")) {
					
					previousProject += CTSPFile.FILE_EXTENSION;				
					if(CTSPFile.projectExists(previousProject)) engine.loadProject(previousProject);
					
				}
				
			}
			
			if(stats.containsKey(cameraX)) {
				
				float x = Float.parseFloat(stats.get(cameraX));
				float y = Float.parseFloat(stats.get(cameraY));
				float zoom = Float.parseFloat(stats.get(cameraZoom));
				engine.camera().translate(x, y);
				engine.camera().zoom(zoom);
				
			}
			
			if(stats.containsKey(currentTheme)) {
				
				String theme = stats.get(currentTheme);
				if(!theme.equals("null")) engine.setTheme(theme);
								
			}
			
			Set<Entry<String , String>> entries = controls.entrySet();
			HashMap<String , Control> mapControls = new HashMap<>();
			
			for(Control x : Control.controls) mapControls.put(x.name, x);
			for(Entry<String , String> x : entries) {
				
				Control currentControl = mapControls.remove(x.getKey());
				String fileControlValue = x.getValue();
				currentControl.isKeyboard = fileControlValue.endsWith("K");
				currentControl.key = Integer.parseInt(fileControlValue, 0, fileControlValue.length() - 1, 10);
				
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
		} 
		
	}
		
}