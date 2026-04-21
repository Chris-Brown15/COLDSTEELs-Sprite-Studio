package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import cs.csss.project.AnimationSwapType;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu for setting the swap type of an animation frame.
 */
public class SetAnimationFrameSwapTypeMenu extends Dialogue {

	private AnimationSwapType swapType;
	private final SCUserInterface ui;
	
	/**
	 * Creates a set swap type for animation frame menu. 
	 * 
	 * @param nuklear the Nuklear factory
	 * @param frame index of an animation frame
	 */
	public SetAnimationFrameSwapTypeMenu(SCNuklear nuklear , int frame) {

		ui = new SCUserInterface(nuklear , "Set Animation Frame Swap Type" , .5f - (.22f / 2) , .5f - (.22f / 2) , .22f , .2f );

		ui.flags = UI_TITLED|UI_BORDERED;
		
		Runnable close = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			super.onFinish();
			
		};
		
		ui.new SCDynamicRow(40).new SCText("Select Animation Frame " + frame + "'s animation swap type");
				
		AnimationSwapType[] types = AnimationSwapType.values();
		
		SCRadio[] radios = new SCRadio[types.length];
		for(int i = 0 ; i < types.length ; i++) {
			
			int j = i;
			radios[i] = ui.new SCDynamicRow().new SCRadio(types[i].formattedName() , false , () -> swapType = types[j]);
					
		}
		
		SCRadio.groupAll(radios);
		
		SCDynamicRow finishRow = ui.new SCDynamicRow();
		finishRow.new SCButton("Finish" , () -> {
		
			if(swapType == null) return;
			close.run();
			
		});
		
		finishRow.new SCButton("Cancel" , () -> {
			
			swapType = null;
			close.run();
			
		});
		
	}
	
	/**
	 * Returns whether this UI is finished.
	 * 
	 * @return Whether this UI is finished.
	 */
	public boolean finished() {
		
		return ui.isFreed();
		
	}

	/**
	 * Returns what swap type is selected.
	 * 
	 * @return What swap type is selected.
	 */
	public AnimationSwapType swapType() {
		 
		return swapType;
		
	}
	
}
