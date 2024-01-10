package cs.csss.engine;

/**
 * Launcher class of the application. 
 * 
 * <p>
 * 	The main method is the only method of this class and it follows the structure of 
 * 		<ol>
 * 			<li> preinitialization, </li> 
 * 			<li> initialization, </li>
 *  		<li> runtime, and </li>
 * 			<li> shut down (and static shut down) </li>
 * 		</ol>
 * 	Preinitialization consists of parsing the program arguments and executing any must-happen-first code. Initialization creates the engine
 * 	which creates and manages all resources of the application. and finally shut down, which frees resources the program is using and writes
 * 	data to files (the user settings2 file). And finally is static shut down, which frees static memory allocated by the COLDSTEEL Core 
 * 	library.
 * </p>
 */
public class Launch {

	private Launch() {}
	
	public static void main(String[] args) {
	
		Engine.preinitialize(args);
		
		Engine spriteStudio = new Engine();
		spriteStudio.run();
		spriteStudio.shutDown();
		
		Engine.finalShutDown();
		
		System.out.println("Closing Sprite Studio.");
		
	}

}
