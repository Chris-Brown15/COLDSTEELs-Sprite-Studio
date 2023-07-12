package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.utils.Lambda;
import cs.csss.project.AnimationSwapType;
import cs.core.ui.CSNuklear.CSUserInterface;

public class SetAnimationFrameSwapTypeMenu {

	private AnimationSwapType swapType;
	private final CSUserInterface ui;
	
	public SetAnimationFrameSwapTypeMenu(CSNuklear nuklear , int frame) {

		ui = nuklear.new CSUserInterface("Set Animation Frame Swap Type" , .5f - (.22f / 2) , .5f - (.22f / 2) , .22f , .2f );

		ui.options = UI_TITLED|UI_BORDERED;
		
		final Lambda close = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		};
		
		ui.new CSDynamicRow(40).new CSText("Select Animation Frame " + frame + "'s animation swap type");
				
		AnimationSwapType[] types = AnimationSwapType.values();
		
		CSRadio[] radios = new CSRadio[types.length];
		for(int i = 0 ; i < types.length ; i++) {
			
			int j = i;
			radios[i] = ui.new CSDynamicRow().new CSRadio(types[i].formattedName() , false , () -> swapType = types[j]);
					
		}
		
		CSRadio.groupAll(radios);
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		finishRow.new CSButton("Finish" , () -> {
		
			if(swapType == null) return;
			close.invoke();
			
		});
		
		finishRow.new CSButton("Cancel" , () -> {
			
			swapType = null;
			close.invoke();
			
		});
		
	}
	
	public boolean finished() {
		
		return ui.isFreed();
		
	}

	public AnimationSwapType swapType() {
		 
		return swapType;
		
	}
	
}
