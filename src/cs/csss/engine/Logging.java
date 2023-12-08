package cs.csss.engine;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Logging class providing utilities for logging and printing information about the running instance of Sprite Studio. Principally this class provides
 * the ability to print to multiple places at once.
 * 
 * @author Chris Brown
 *
 */
public class Logging {

	private static boolean printToFile;
	
	public static final String OBJECT_STRING_SEPARATOR = ", ";
	private static final String DEBUG_PRINT_PREFIX = "[DEBUG] " , LOG_FILE_TYPE = ".txt";
	private static final PrintStream stdOut = System.out , stdErr = System.err;
	private static PrintStream fileOut , fileErr;
	
	static void initialize(boolean printToFile) throws IOException {
		
		Logging.printToFile = printToFile;
		
		if(printToFile) { 
			
			fileOut = new PrintStream(new BufferedOutputStream(new FileOutputStream("debug/out" + LOG_FILE_TYPE)) , true);
			fileErr = new PrintStream(new BufferedOutputStream(new FileOutputStream("debug/err" + LOG_FILE_TYPE)) , true);
			
			sysout("Initialized Log Writer");
			
		}
		
		sysout(String.format("Version: %s" , Engine.VERSION_STRING));
		
	}
	
	/**
	 * Prints the given objects.
	 * 
	 * @param x — objects to print
	 */
	public static void sysout(Object... x) {
		
		sysoutInternal(x);
		
	}
	
	/**
	 * Prints the given objects in the error output.
	 * 
	 * @param x — objects to print
	 */
	public static void syserr(Object... x) {

		String print = stringFromVararg(x);
		
		System.err.println(print);
		if(printToFile) {
			
			System.setErr(fileErr);
			System.err.println(print);
			System.setErr(stdErr);
			
		}
				
	}
	
	/**
	 * Prints the given objects only if debug mode is on.
	 * 
	 * @param x — objects to print
	 */
	public static void sysDebug(Object... x) {
		
		if(Engine.isDebug()) syserr(x);
				
	}
	
	private static void sysoutInternal( Object... x) {

		String print = stringFromVararg(x);
		
		System.out.println(print);
		if(printToFile) {
			
			System.setOut(fileOut);
			System.out.println(print);
			System.setOut(stdOut);
			
		}
				
	}
		
	private static String stringFromVararg(Object... args) {

		StringBuilder string = new StringBuilder();
		
		if(Engine.isDebug()) string.append(DEBUG_PRINT_PREFIX);
		
		for(int i = 0 ; i < args.length - 1 ; i++) string.append(args[i]).append(OBJECT_STRING_SEPARATOR);
		string.append(args[args.length - 1]);
		
		return string.toString();
		
	}
	
	private Logging() {}
	
}
