/**
 * 
 */
package cs.csss.ui.menus;

/**
 * Used to flag an object as a UI dialogue. Some behavior of the program must be different if a dialogue is open, which this class helps track.
 */
public abstract class Dialogue {

	private static int openDialogues = 0;
	
	/**
	 * Returns the number of open dialogues.
	 * 
	 * @return Number of open dialogues.
	 */
	public static int numberOpenDialogues() {
		
		return openDialogues;
		
	}
	
	/**
	 * Creates a dialogue.
	 */
	public Dialogue() {
		
		openDialogues++;
				
	}

	/**
	 * Invoke when the UI element is to close.
	 */
	public void onFinish() {
		
		openDialogues--;
		
	}
	
}
