package cs.csss.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;
import org.python.util.PythonInterpreter;

import cs.csss.editor.brush.CSSSBrush;
import cs.csss.editor.brush.CSSSModifyingBrush;
import cs.csss.editor.brush.CSSSSelectingBrush;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.NOPEvent;
import cs.csss.editor.event.RunScriptEvent2;
import cs.csss.editor.palette.ColorPalette;
import cs.csss.editor.palette.PaletteScriptMeta;
import cs.csss.engine.Engine;
import cs.csss.engine.Logging;
import cs.csss.misc.utils.MiscUtils;

/**
 * Class that uses Jython to invoke scripts. Instances of this class handle registering, compilation, execution, and hot-reloading of scripts 
 * implemented in Python for Sprite Studio. In general, a script must be registered by a file it is implemented in. Then it can provide either a 
 * {@link CSSSEvent} implementation or a {@link CSSSBrush} implementation. 
 */
public class JythonScriptExecutor {

	//hashmaps for each type of script
	private final HashMap<String , EventAndCode> 
		artboardScripts = new HashMap<>() ,
		projectScripts = new HashMap<>() ;
	
	private final HashMap<String , BrushAndCode>
		simpleBrushes = new HashMap<>() ,
		modifyingBrushes = new HashMap<>() ,
		selectingBrushes = new HashMap<>() ;
	
	private final HashMap<String , PaletteAndCode> palettes = new HashMap<>();
	
	private boolean hotReload = false;
	
	/**
	 * Creates a new Jython script executor which will hot reload scripts if {@code hotReload} is <code>true</code>.
	 * 
	 * @param hotReload — whether to hot reload scripts
	 */
	public JythonScriptExecutor(boolean hotReload) {

		this.hotReload = hotReload;
		
	}
	
	/* REGISTER METHODS */

	/**
	 * Registers the Artboard script at the given path, returning whether it was sucessfully registered.
	 * 
	 * @param scriptName — the file containing a script to register
	 * @return Whether the script was successfully registered.
	 */
	public boolean registerArtboardScript(File file) {
		
		return registerEventScript(file , artboardScripts);
		
	}

	/**
	 * Registers the Project script at the given path, returning whether it was sucessfully registered.
	 * 
	 * @param scriptName — the file containing a script to register
	 * @return Whether the script was successfully registered.
	 */
	public boolean registerProjectScript(File file) {
		
		return registerEventScript(file , projectScripts);
		
	}
	
	/**
	 * Registers the brush script in the given file, returning whether the registry was successful.
	 * 
	 * @param file — the file to register
	 * @return Whether the script was successfully registered.
	 */
	public boolean registerSimpleBrushScript(File file) {
		
		return registerBrushScript(file , simpleBrushes);
		
	}

	/**
	 * Registers the brush script in the given file, returning whether the registry was successful.
	 * 
	 * @param file — the file to register
	 * @return Whether the script was successfully registered.
	 */
	public boolean registerModifyingBrushScript(File file) {
		
		return registerBrushScript(file , modifyingBrushes);
		
	}

	/**
	 * Registers the brush script in the given file, returning whether the registry was successful.
	 * 
	 * @param file — the file to register
	 * @return Whether the script was successfully registered.
	 */
	public boolean registerSelectingBrushScript(File file) {
	
		return registerBrushScript(file , selectingBrushes);
	
	}

	/**
	 * Registers the paette script in the given file, returning whether the registry was successful.
	 * 
	 * @param file — the file to register
	 * @return Whether the script was successfully registered.
	 */
	public boolean registerPaletteScript(File file) {
		
		return compileCode(file , palettes , (interpreter , code) -> new PaletteAndCode(
			new PaletteScriptMeta(
				getOrDefault(interpreter , "name" , file.getName() + " Palette" , String.class) , 
				getOrDefault(interpreter, "valueScale", 15, Integer.TYPE)
			) , 
			code
		));
		
	}
	
	/* INVOKE METHODS */
	
	/**
	 * Runs the artboard script registered under {@code name}.
	 * 
	 * @param editor — the editor
	 * @param name — the name of the script to execute
	 */
	public void runArtboardScript(Editor editor , String name) {
		
		pushEventScriptEvent(name, artboardScripts , editor , MiscUtils.modifyableOf(editor.currentArtboard() , editor));

	}

	/**
	 * Runs the project script registered under {@code name}.
	 * 
	 * @param editor — the editor
	 * @param name — the name of the script to execute
	 */
	public void runProjectScript(Editor editor , String name) {
		
		pushEventScriptEvent(name, projectScripts, editor , MiscUtils.modifyableOf(editor.project() , editor));
				
	}
	
	/* GET BRUSH METHODS */
	
	/**
	 * Returns a {@link cs.csss.editor.brush.CSSSBrush} implementation from the given script file.
	 * 
	 * @param editor — the editor
	 * @param file — the file the script is registered under
	 * @return Brush implementation frmo the given script file.
	 */
	public CSSSBrush getSimpleBrush(Editor editor , File file) {
	
		String fileName = file.getName();
		BrushAndCode info = simpleBrushes.get(fileName);		
		if(info == null) return null;
		String scriptName = editor.asScriptName(fileName);
		PyObject brush = runScriptNameFunction(scriptName, info.code, new Object[] {info.meta.tooltip() , info.meta.isStateful()});
		if(brush == null) return null;
		return (CSSSBrush)brush.__tojava__(CSSSBrush.class);
		
	}
	
	/**
	 * Returns a {@link cs.csss.editor.brush.CSSSModifyingBrush} implementation from the given script file.
	 * 
	 * @param editor — the editor
	 * @param file — the file the script is registered under
	 * @return Modifying brush implementation frmo the given script file.
	 */
	public CSSSModifyingBrush getModifyingBrush(Editor editor , File file) {
		
		String fileName = file.getName();
		BrushAndCode info = modifyingBrushes.get(fileName);
		if(info == null) return null;
		String scriptName = editor.asScriptName(fileName);
		PyObject brush = runScriptNameFunction(scriptName, info.code, new Object[] {info.meta.tooltip() , info.meta.isStateful()});
		if(brush == null) return null;
		return (CSSSModifyingBrush)brush.__tojava__(CSSSModifyingBrush.class);
		
	}

	/**
	 * Returns a {@link cs.csss.editor.brush.CSSSSelectingBrush} implementation from the given script file.
	 * 
	 * @param editor — the editor
	 * @param file — the file the script is registered under
	 * @return Selecting brush implementation frmo the given script file.
	 */
	public CSSSSelectingBrush getSelectingBrush(Editor editor , File file) {
		
		String fileName = file.getName() , scriptName = editor.asScriptName(fileName);
		BrushAndCode info = selectingBrushes.get(fileName);
		if(info == null) return null;
		PyObject brush = runScriptNameFunction(scriptName, info.code, new Object[] {info.meta.tooltip()});
		if(brush == null) return null;
		return (CSSSSelectingBrush)brush.__tojava__(CSSSSelectingBrush.class);
		
	}
	
	/* GET PALETTE */
	
	public ColorPalette getPalette(Editor editor , File file) {
		
		String fileName = file.getName() , scriptName = editor.asScriptName(fileName);
		PaletteAndCode info = palettes.get(fileName);
		if(info == null) return null;
		PyObject palette = runScriptNameFunction(scriptName, info.code, new Object[] {info.meta.name() , info.meta.defaultValueScale()});
		if(palette == null) return null;
		return (ColorPalette)palette.__tojava__(ColorPalette.class);
		
	}
	
	/* GET METADATA METHODS */
	
	/**
	 * Returns a metadata container for the brush registered as {@code scriptName}.
	 * 
	 * @param scriptName — name of the script whose meta is being queried 
	 * @return The Metadata attached to the registered script.
	 */
	public BrushScriptMeta getSimpleBrushInfo(String scriptName) {
		
		return simpleBrushes.get(scriptName).meta;
		
	}

	/**
	 * Returns a metadata container for the modifying brush registered as {@code scriptName}.
	 * 
	 * @param scriptName — name of the script whose meta is being queried 
	 * @return The Metadata attached to the registered script.
	 */
	public BrushScriptMeta getModifyingBrushInfo(String scriptName) {
		
		return modifyingBrushes.get(scriptName).meta;
		
	}

	/**
	 * Returns a metadata container for the selecting brush registered as {@code scriptName}.
	 * 
	 * @param scriptName — name of the script whose meta is being queried 
	 * @return The Metadata attached to the registered script.
	 */
	public BrushScriptMeta getSelectingBrushInfo(String scriptName) {
		
		return selectingBrushes.get(scriptName).meta;
		
	}
	
	/**
	 * Registers a script in the given map if it was not previously, or if hot reloading is enabled. 
	 * 
	 * @param file — file to register
	 * @param map — map to store the script
	 * @return {@code true} if the script was successfully registered, equal to if a new entry was put in {@code map}.
	 */
	private boolean registerEventScript(File file , HashMap<String , EventAndCode> map) {
		
		return compileCode(file , map , (interpreter , code) -> new EventAndCode(
			new EventScriptMeta(
				(boolean)interpreter.get("isRenderEvent").__tojava__(Boolean.TYPE) ,
				(boolean)getOrDefault(interpreter , "isTransientEvent", false, Boolean.TYPE) ,
				(boolean)getOrDefault(interpreter , "takesArguments" , false , Boolean.TYPE) ,
				getOrDefault(interpreter , "argumentDialogueText" , null , String.class) ,
				file.getName()
			) , 
			code
		));
		
	}
	
	/**
	 * Registers the script at {@code file} in the map {@code map}.
	 * 
	 * @param file — a file to register
	 * @param map — the map to store the registery in
	 * @return {@code true} if {@code file} was registered.
	 */
	private boolean registerBrushScript(File file , HashMap<String , BrushAndCode> map) {
		
		return compileCode(file , map , (interpreter , code) -> {
			return new BrushAndCode(
				new BrushScriptMeta(
					getOrDefault(interpreter , "tooltip" , "" , String.class) , 
					getOrDefault(interpreter , "stateful" , false , Boolean.TYPE) ,
					file.getName() ,
					(boolean)interpreter.get("isRenderEvent").__tojava__(Boolean.TYPE) ,
					(boolean)interpreter.get("isTransientEvent").__tojava__(Boolean.TYPE)
				) ,	
				code
			);
		});
		
	}
	
	/**
	 * Handles most of the boilerplate for compiling python code and generating a metadata item.
	 * 
	 * @param <T> — type of metadata item
	 * @param file — script file to compile
	 * @param map  — map to store the item in
	 * @param infoGetter — {@link BiFunction} responsible for creating the metadata-and-code object stored in the map
	 * @return {@code true} if a new item was registered into the given map, {@code false} otherwise.
	 */
	private <T> boolean compileCode(File file , HashMap<String , T> map , BiFunction<PythonInterpreter , PyCode , T> infoGetter) {
		
		Objects.requireNonNull(file);
		boolean result = false;
		try(PythonInterpreter interpreter = new PythonInterpreter()) {

			String name = file.getName();
			T entry = map.get(name);
			if(entry != null) result = true;
			
			if(hotReload || entry == null) {
				
				map.remove(name);
				PyCode code;
				try {

					Logging.sysDebug("Compiling " + name);
					code = interpreter.compile(new FileReader(file));
					
				} catch (FileNotFoundException e) {

					e.printStackTrace();
					Logging.syserr("Failed to compile " + name);
					return false;
					
				}
				
				interpreter.exec(code);
				T info = infoGetter.apply(interpreter , code);
				map.put(name, info);
				result = true;
				
			}
			
		}
		
		return result;
		
	}

	/**
	 * Executes the script of the given name, which is to be found in {@code map}.
	 * 
	 * @param name — name of the script to execute
	 * @param map — the map to find it in
	 * @param editor — the editor
	 * @param arguments — the arguments to pass to the script's name function
	 */
	private void pushEventScriptEvent(String name , HashMap<String , EventAndCode> map , Editor editor , List<Object> arguments) {
		
		EventAndCode script = Objects.requireNonNull(map.get(name));
		String functionName = editor.asScriptName(name);		
		
		if(script.meta.takesArguments()) editor.startScriptArgumentInput(name, Optional.ofNullable(script.meta.argumentDialogueText()), result -> {
			
			List<String> args = List.of(result.split(" "));
			arguments.add(args);			
			editor.eventPush(runScriptNameFunction(functionName, script, arguments.toArray()));
			
		});
		else editor.eventPush(runScriptNameFunction(functionName, script, arguments.toArray()));
		
	}
	
	/**
	 * Executes the function of the name {@code name} from the {@code PyCode} contained within {@code info}, passing {@code arguments} as the 
	 * function's arguments.
	 *  
	 * @param name — name of a function to invoke
	 * @param info — container for metadata and compiled code
	 * @param arguments — arguments to pass
	 * @return Event to push containing the given code.
	 */
	private CSSSEvent runScriptNameFunction(String name , EventAndCode info , Object[] arguments) {

		PyObject event = runScriptNameFunction(name, info.code, arguments);
		if(event == null) return new NOPEvent();
		RunScriptEvent2 scriptEvent = new RunScriptEvent2(info.meta , event);
		return scriptEvent;
		
	}
	
	/**
	 * Executes the function named {@code name}, which will receive {@code arguments}. {@code code} is the source compilation containing the function
	 * of the given name. 
	 * 
	 * @param name — name of a function to execute
	 * @param code — code containing the function
	 * @param arguments — arguments to pass to the function
	 * @return Result of the function call.
	 */
	private PyObject runScriptNameFunction(String name , PyCode code,  Object[] arguments) {

		Logging.sysDebug("Running " + name);
		try(PythonInterpreter interpreter = new PythonInterpreter()) {
			
			Logging.sysDebug("Starting execution of code.");
			interpreter.exec(code);
			Logging.sysDebug("Executed code.");
			PyObject nameFunction = interpreter.get(name);
			PyObject[] argsAsPyObjects = new PyObject[arguments.length];
			ClassicPyObjectAdapter adapter = new ClassicPyObjectAdapter();
			for(int i = 0 ; i < argsAsPyObjects.length ; i++) argsAsPyObjects[i] = adapter.adapt(arguments[i]);
			Logging.sysDebug(String.format("Starting function call (%d args).", argsAsPyObjects.length));
			PyObject result = null;
			//if in debug mode, catch an exception if an error occurs
			if(Engine.isDebug()) try {
				
				result = nameFunction.__call__(argsAsPyObjects);

			} catch(Exception e) {
				
				e.printStackTrace();
			
			} else result = nameFunction.__call__(argsAsPyObjects);
			
			Logging.sysDebug("Finished call to function " + name + ".");
			return result;
			
		}
		
	}
	
	/**
	 * Gets a global variable from the given interpreter named {@code name}, if one is present. If not, {@code _default} is returned.
	 * 
	 * @param <T> — type of data sought after and returned
	 * @param interpreter — interpreter to get the variable from
	 * @param itemName — name of the variable to get
	 * @param _default — a variable to return if none was found in {@code interpreter}
	 * @param Tclass — {@code Class<T>} for the variable being queried for
	 * @return The variable in {@code interpreter} named {@code itemName}, if it is found, {@code _default} otherwise.
	 */
	@SuppressWarnings("unchecked") private <T> T getOrDefault(PythonInterpreter interpreter , String itemName , T _default , Class<T> Tclass) {
		
		try {
			
			var item = interpreter.get(itemName);
			if(item == null) return _default;
			return (T) item.__tojava__(Tclass);
			
		} catch(Exception e) {
			
			if(Engine.isDebug()) e.printStackTrace();
			
			return _default;
			
		}
		
	}

	private record EventAndCode(EventScriptMeta meta , PyCode code) {}
	private record BrushAndCode(BrushScriptMeta meta , PyCode code) {}
	private record PaletteAndCode(PaletteScriptMeta meta , PyCode code) {}
	
}