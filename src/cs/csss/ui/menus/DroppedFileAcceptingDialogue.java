/**
 * 
 */
package cs.csss.ui.menus;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Class representing dialogues that accept files that have been dragged into Sprite Studio.
 */
public abstract class DroppedFileAcceptingDialogue extends Dialogue {

	private static final List<DroppedFileAcceptingDialogue> currentAcceptingDialogues = new LinkedList<>();
	
	/**
	 * Gets the most recently added {@code DroppedFileAcceptingDialogue} and returns to it the given file paths.
	 * 
	 * @param paths — paths to pass to the most recent {@code DroppedFileAcceptingDialogue}
	 */
	public static void acceptDroppedFilePaths(String...paths) {
		
		if(currentAcceptingDialogues.isEmpty()) return;
		currentAcceptingDialogues.get(currentAcceptingDialogues.size() - 1).acceptDroppedFilePath(paths);
		
	}
	
	/**
	 * Creates a new dropped file accepting dialogue.
	 */
	public DroppedFileAcceptingDialogue() {
		
		currentAcceptingDialogues.add(this);
		
	}

	@Override public void onFinish() {
		
		currentAcceptingDialogues.remove(this);
		super.onFinish();
		
	}
	
	/**
	 * Accepts file paths that have been dragged into the program.
	 * 
	 * @param filepaths — paths to the dropped files
	 */
	public abstract void acceptDroppedFilePath(String... filepaths);

	/**
	 * Default implementation of {@link DroppedFileAcceptingDialogue#acceptDroppedFilePath(String...) acceptDroppedFilePath(String...)} which just
	 * checks for validity of the {@code paths} parameter.
	 * 
	 * @param paths — paths passed to the menu
	 */
	public final void defaultAcceptDroppedFilePath(String...paths) {
		
		Objects.requireNonNull(paths);
		Objects.checkIndex(0, paths.length);
				
	}
	
}
