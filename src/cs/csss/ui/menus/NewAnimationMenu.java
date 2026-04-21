package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap_colored;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.system.MemoryStack;

import cs.csss.project.CSSSProject;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu for creating a new animation.
 */
public class NewAnimationMenu extends Dialogue {

	private volatile boolean isFinished = false;
	private boolean nameInUse = false;
	
	private String animationName;
	
	private final SCTextEditor textInput;
	private final SCUserInterface ui;
	private final Runnable finish;
			
	/**
	 * Creates a new animation menu.
	 * 
	 * @param currentProject the current project to add an animation to
	 * @param nuklear the Nuklear factory
	 */
	public NewAnimationMenu(CSSSProject currentProject , SCNuklear nuklear) {

		ui = new SCUserInterface(nuklear, "New Animation" , 0.5f - (.33f / 2) , .5f - (.12f / 2) , .33f , .12f);
		ui.flags |= UI_TITLED|UI_BORDERED;
		
		finish = () -> { 
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			isFinished = true;
			super.onFinish();
			
		};
	
		SCDynamicRow row = ui.new SCDynamicRow();
		row.new SCText("Animation Name:" , TEXT_CENTERED|TEXT_MIDDLE);
		textInput = row.new SCTextEditor(100);
		
		ui.attachedLayout((context) -> {
			
			String input = textInput.toString();
			
			nameInUse = false;
			
			currentProject.forEachAnimation(anim -> {
				
				if(anim.name().equals(input)) {
				
					try(MemoryStack stack = MemoryStack.stackPush()) {
						
						nk_layout_row_dynamic(context , 40 , 1);
						NkColor red = NkColor.malloc(stack).set((byte)-1 , (byte)0 , (byte)0 , (byte)-1);
						nk_text_wrap_colored(context , input + " already names an animation. Cannot use this name." , red);
						nameInUse = true;
						
					}
					
				}
				
			});
						
		});		
		
		SCDynamicRow buttonRow = ui.new SCDynamicRow();
		buttonRow.new SCButton("Finish" , this::attemptFinish);
		buttonRow.new SCButton("Cancel" , finish);
		
	}
	
	private void attemptFinish() {
		
		String input = textInput.toString();
		if(!nameInUse) {
			
			animationName = input;
			finish.run();
			
		}		
		
	}
	
	/**
	 * Returns whether this UI is ready to finish.
	 * 
	 * @return Whether this UI is ready to finish.
	 */
	public boolean isFinished() {
		
		return isFinished;
		
	}
	
	/**
	 * Returns the name the user input for the animation.
	 * 
	 * @return the name the user input
	 */
	public String get() { 
		
		return animationName;
		
	}

}
