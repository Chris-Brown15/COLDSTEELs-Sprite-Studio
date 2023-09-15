package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.UI_BORDERED;
import static cs.core.ui.CSUIConstants.UI_TITLED;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap_colored;

import org.lwjgl.nuklear.NkColor;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;

/**
 * UI menu for creating a new animation.
 */
public class NewAnimationMenu {

	private volatile boolean isFinished = false;
	private boolean nameInUse = false;
	
	private String animationName;
	
	private final CSTextEditor textInput;
	private final CSUserInterface ui;
	private final Lambda finish;
			
	/**
	 * Creates a new animation menu.
	 * 
	 * @param currentProject — the current project to add an animation to
	 * @param nuklear — the Nuklear factory
	 */
	public NewAnimationMenu(CSSSProject currentProject , CSNuklear nuklear) {

		ui = nuklear.new CSUserInterface("New Animation" , 0.5f - (.33f / 2) , .5f - .125f , .33f , .25f);
		ui.options |= UI_TITLED|UI_BORDERED;
		
		finish = () -> { 
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			isFinished = true;
			
		};
	
		CSDynamicRow row = ui.new CSDynamicRow();
		row.new CSText("Animation Name:");
		textInput = row.new CSTextEditor(100);
		
		ui.attachedLayout((context , stack) -> {
			
			String input = textInput.toString();
			
			nameInUse = false;
			
			currentProject.forEachAnimation(anim -> {
				
				if(anim.name().equals(input)) {
					
					nk_layout_row_dynamic(context , 40 , 1);
					NkColor red = NkColor.malloc(stack).set((byte)-1 , (byte)0 , (byte)0 , (byte)-1);
					nk_text_wrap_colored(context , input + " already names an animation. Cannot use this name." , red);
					nameInUse = true;
				}
				
			});
						
		});		
		
		CSDynamicRow buttonRow = ui.new CSDynamicRow();
		buttonRow.new CSButton("Finish" , this::attemptFinish);
		buttonRow.new CSButton("Cancel" , finish);
		
	}
	
	private void attemptFinish() {
		
		String input = textInput.toString();
		if(!nameInUse) {
			
			animationName = input;
			finish.invoke();
			
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
