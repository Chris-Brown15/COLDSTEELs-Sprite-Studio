/**
 * 
 */
package cs.csss.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Attempts to find out whether the user has a valid Python installation so scripts can be invoked.
 */
class PythonChecker {

	private ProcessBuilder pythonFinderProcessBuilder = new ProcessBuilder();
	private final Process process;
	
	PythonChecker() throws IOException {
		
		pythonFinderProcessBuilder.command(List.of("python" , "--version"));
		process = pythonFinderProcessBuilder.start();
		
	}
	
	/**
	 * To find the result of whether the user has Python installed, we get the version of Python from the process, and then check that it is greater
	 * or equal to Python 3.5. 
	 * 
	 * @return {@code true} if a propper version of Python was found on the running computer.
	 * @throws IOException if an exception occured when getting the input from the version finder process.
	 */
	boolean findResult() throws IOException {
		
		InputStream input = process.getInputStream();
		String version = new String(input.readAllBytes());
		if(!version.startsWith("Python ")) return false;
		//7 is used here because thats the number of character "Python " uses, so we jump to the first character after the space.
		String versionNumber = version.substring(7);
		char[] chars = versionNumber.toCharArray();
		if(chars[0] < '3') return false;
		int foundDots = 0;
		int dotOneIndex = 0;
		int dotTwoIndex = 0;
		for(int i = 1 ; i < chars.length ; i++) if(chars[i] == '.') {
			
			foundDots++;
			if(foundDots == 1) dotOneIndex = i;
			else if(foundDots == 2) {
			
				dotTwoIndex = i;
				int midVersion = Integer.parseInt(new String(chars , dotOneIndex + 1, dotTwoIndex - (dotOneIndex + 1)));
				if(midVersion >= 5) return true;
					
			}

		}
		
		return false;
		
	}
	
}