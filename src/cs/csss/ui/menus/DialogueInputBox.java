/**
 * 
 */
package cs.csss.ui.menus;

import java.util.function.Consumer;

import org.lwjgl.nuklear.NkPluginFilter;

import cs.core.ui.CSNuklear;
import cs.core.ui.prefabs.InputBox;

/**
 * 
 */
public class DialogueInputBox extends Dialogue {

	public DialogueInputBox(
		CSNuklear nuklear , 
		String displayName , 
		float xRatio , 
		float yRatio , 
		int maxCharacters , 
		NkPluginFilter filter , 
		Consumer<String> onEnter		
	) {
		
		new InputBox(nuklear , displayName , xRatio , yRatio , maxCharacters , filter , input -> {
			
			onFinish();
			onEnter.accept(input);
			
		});
		
	}

}
