package cs.csss.engine;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Logging class providing utilities for logging and printing information about the running instance of Sprite Studio. Principally this class 
 * provides the ability to print to multiple places at once.
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
			
			sysoutln("Initialized Log Writer");
			
		}
		
		sysoutln(String.format("Version: %s" , Engine.VERSION_STRING));
		
	}
	
	/**
	 * Prints the given objects followed by a line feed.
	 * 
	 * @param x — objects to print
	 */
	public static void sysoutln(Object... x) {
		
		sysoutInternal(true , x);
		
	}
	
	/**
	 * Prints the given objects without any line feed.
	 * 
	 * @param x objects to print
	 */
	public static void sysout(Object...x) {
		
		sysoutInternal(false, x);
		
	}
	
	/**
	 * Prints the given objects in the error output followed by a line feed.
	 * 
	 * @param x — objects to print
	 */
	public static void syserrln(Object... x) {

		syserrInternal(true , x);
				
	}
	
	/**
	 * Prints the given objects to the error output without a line feed.
	 * 
	 * @param x objects to print
	 */
	public static void syserr(Object... x) {
		
		syserrInternal(false , x);
		
	}
	
	/**
	 * If {@link Engine#isDebug() Engine.isDebug()}, the given objects are printed as error with a line feed at the end. Otherwise, nothing happens.
	 * 
	 * @param x — objects to print
	 */
	public static void sysDebugln(Object... x) {
		
		if(Engine.isDebug()) syserrln(x);
				
	}

	/**
	 * If {@link Engine#isDebug() Engine.isDebug()}, the given objects are printed as error without a line feed at the end. Otherwise, nothing 
	 * happens.
	 * 
	 * @param x — objects to print
	 */
	public static void sysDebug(Object...x) {
		
		if(Engine.isDebug()) syserr(x);
		
	}
	
	private static void sysoutInternal(boolean lineFeed , Object... x) {

		String print = stringFromVararg(x);
		
		System.out.print(print);
		if(lineFeed) System.out.print('\n');
		if(printToFile) {
			
			System.setOut(fileOut);
			System.out.print(print);
			if(lineFeed) System.out.print('\n');
			System.setOut(stdOut);
			
		}
				
	}
		
	private static void syserrInternal(boolean lineFeed , Object...x) {

		String print = stringFromVararg(x);
		
		System.err.print(print);
		if(lineFeed) System.err.print('\n');
		if(printToFile) {
			
			System.setErr(fileErr);
			System.err.print(print);
			if(lineFeed) System.err.print('\n');
			System.setErr(stdErr);
			
		}
				
	}
	
	private static String stringFromVararg(Object... args) {

		StringBuilder string = new StringBuilder();
		
		if(args.length == 0) return string.toString();
		
		if(Engine.isDebug()) string.append(DEBUG_PRINT_PREFIX);
		
		for(int i = 0 ; i < args.length - 1 ; i++) string.append(args[i]).append(OBJECT_STRING_SEPARATOR);
		string.append(args[args.length - 1]);
		
		return string.toString();
		
	}
	
	private Logging() {}
	
}
