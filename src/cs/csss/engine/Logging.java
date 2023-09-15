package cs.csss.engine;

import static cs.core.utils.CSUtils.require;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Logging class providing utilities for logging and printing information about the running instance of Sprite Studio in an asynchronous
 * way.
 * 
 * @author Chris Brown
 *
 */
public class Logging {

	public static final int
		OP_TO_STD = 0b1,
		OP_TO_FILE = 0b10
	;

	private static final String DEBUG_PRINT_PREFIX = "[DEBUG] ";	

	public static final String OBJECT_STRING_SEPARATOR = ", ";
	
	public static int operations = OP_TO_STD;

	private static FileOutputStream logWriter;
	
	private static LoggingThread loggingThread = new LoggingThread();
		
	static void initialize(int _operations) throws FileNotFoundException {
		
		operations = _operations;
		
		if(has(OP_TO_FILE)) { 
			
			sysout("Initialized Log Writer");
			logWriter = new FileOutputStream("debug/log " + LocalDateTime.now());
			
		}
		
		loggingThread.start();
		
	}
	
	/**
	 * Prints the given objects.
	 * 
	 * @param x — objects to print
	 */
	public static void sysout(Object... x) {
		
		sysoutInternal(System.out::println , x);
		
	}
	
	/**
	 * Prints the given objects in the error output.
	 * 
	 * @param x — objects to print
	 */
	public static void syserr(Object... x) {
		
		sysoutInternal(System.err::println , x);
		
	}
	
	/**
	 * Prints the given objects only if debug mode is off.
	 * 
	 * @param x — objects to print
	 */
	public static void sysDebug(Object... x) {
		
		if(Engine.isDebug()) syserr(x);
				
	}
	
	private static void sysoutInternal(Consumer<Object> printer , Object... x) {

		loggingThread.enqueue(() -> {

			StringBuilder string = new StringBuilder();
			
			if(Engine.isDebug()) if(has(OP_TO_STD)) string.append(DEBUG_PRINT_PREFIX);
			
			for(int i = 0 ; i < x.length - 1 ; i++) string.append(x[i]).append(OBJECT_STRING_SEPARATOR);
			string.append(x[x.length - 1]);
			
			String result = string.toString();
			
			if(has(OP_TO_STD)) printer.accept(result);
			
			if(has(OP_TO_FILE)) try {
				
				logWriter.write(result.getBytes());
					
			} catch (IOException e) {}
			
		});
			
		loggingThread.awaken();
		
	}
	
	private static boolean has(int someOperation) {
		
		require(someOperation >= OP_TO_STD && someOperation <= OP_TO_FILE);
		
		return (operations & someOperation) == operations;
		
	}
	
	private Logging() {}

	static void shutDown() {

		loggingThread.endPersist();
		
		if(!has(OP_TO_FILE)) return;
		
		try {
			
			logWriter.close();
			
		} catch (IOException e) {}
		
		logWriter = null;
		operations &= ~OP_TO_FILE;
		
	}

	private static class LoggingThread extends Thread {
		
		private final AtomicBoolean persist = new AtomicBoolean(true);
		private final ConcurrentLinkedDeque<Runnable> prints = new ConcurrentLinkedDeque<>();
		
		LoggingThread() {
			
			setDaemon(true);
			setName("Logging Thread");
		
		}
		
		@Override public void run() {
			
			while(persist.get()) {
				
				try {
				
					synchronized(this) {
						
						wait();
						
					}
					
					while(!prints.isEmpty()) prints.poll().run();
					
				} catch (InterruptedException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
		}
		
		private void awaken() {
			
			synchronized(this) {
				
				notify();
				
			}
			
		}
		
		private void enqueue(Runnable code) {
		
			prints.add(code);

		}
		
		private void endPersist() {
		
			persist.set(false);
			awaken();
			
		}
		
	}
	
}
