/**
 * 
 */
package cs.csss.ui.menus;

import java.util.function.Consumer;

import org.lwjgl.nuklear.NkPluginFilter;

import sc.core.ui.SCNuklear;
import sc.core.ui.prefabs.SCBasicInputBox;

/**
 * 
 */
public class DialogueInputBox extends Dialogue {

	public DialogueInputBox(
		SCNuklear nuklear , 
		String displayName , 
		float xRatio , 
		float yRatio , 
		int maxCharacters , 
		NkPluginFilter filter , 
		Consumer<String> onEnter		
	) {
		
		SCBasicInputBox box = new SCBasicInputBox(nuklear , displayName , null, null , filter , input -> {
			
			onFinish();
			onEnter.accept(input);
			
		} , maxCharacters);
		
		box.ui.positioner.xRatio(xRatio).yRatio(yRatio);
		
	}

}
