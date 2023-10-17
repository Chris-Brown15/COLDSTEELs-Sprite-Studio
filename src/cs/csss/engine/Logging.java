package cs.csss.engine;

import static cs.core.utils.CSUtils.require;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;

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
		OP_TO_FILE = 0b10;

	private static final String 
		DEBUG_PRINT_PREFIX = "[DEBUG] " ,
		LOG_FILE_TYPE = ".txt";
	
	private static final int linefeed = System.lineSeparator().getBytes()[0];
	
	public static final String OBJECT_STRING_SEPARATOR = ", ";
	
	public static int operations = OP_TO_STD;

	private static volatile FileOutputStream logWriter;
	
	private static LoggingThread loggingThread = new LoggingThread();
		
	private static final LocalDateTime launchTime = LocalDateTime.now();
	
	static void initialize(int _operations) throws IOException {
		
		operations = _operations;
		
		if(has(OP_TO_FILE)) { 
			
			CSFolder logs = Engine.debugRoot.getOrCreateSubdirectory("log");
			String nameString = String.format(
				"Y %dD %dM %dS %d" , 
				launchTime.getYear() , 
				launchTime.getDayOfYear(), 
				launchTime.getMinute() , 
				launchTime.getSecond()
			);
			
			File log = CSFile.makeFile(logs, nameString + LOG_FILE_TYPE);
			logWriter = new FileOutputStream(log);	
			log.deleteOnExit();			
			sysout("Initialized Log Writer");
			
		}
		
		loggingThread.start();
		
		sysout(String.format("Version: %s" , Engine.VERSION_STRING));
		
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
	 * Prints the given objects only if debug mode is on.
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
				
				logWriter.write(result.getBytes()) ; logWriter.write(linefeed);
					
			} catch (IOException e) {}
			
		});
			
		loggingThread.awaken();
		
	}
	
	private static boolean has(int someOperation) {
		
		require(someOperation >= OP_TO_STD && someOperation <= OP_TO_FILE);
		
		return (operations & someOperation) == someOperation;
		
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

	/**
	 * Deletes any log 7 days old or older. 
	 */
	static void deleteOldLogs() {
		
		CSFolder debug = CSFolder.getRoot("debug");
		Iterator<CSFile> files = debug.files();
		final int day = launchTime.getDayOfYear();
		
		FindOldFiles: while(files.hasNext()) {
			
			CSFile next = files.next();
			String name = next.name();			
			char[] chars = name.toCharArray();
			for(int i = 0 ; i < chars.length ; i++) if(chars[i] == 'D') {
								
				//go the first number
				i += 2;
				
				int j = i;
				while(chars[j] != 'M') j++;
				String asString = new String(chars , i , j - i);
				int logDay = Integer.parseInt(asString);				
				if(day >= logDay + 7) { 
					
					files.remove();
					next = CSFile.delete(next);
					continue FindOldFiles;
					
				}
				
			}
			
		}
		
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
