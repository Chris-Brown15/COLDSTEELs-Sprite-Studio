package cs.csss.editor;

import static cs.core.ui.CSUIConstants.*;

import java.util.LinkedList;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSExtendedText;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;

public class GrayscaleShadeMenu {

	public static final int NO_OP = 1;
	
	public int option;
	
	private boolean finished = false;
			
	private Lambda onFinish;
	
	public GrayscaleShadeMenu(CSNuklear nuklear) {

		CSUserInterface ui = nuklear.new CSUserInterface(
			"Grayscale Shade Changer" , 
			0.5f - (0.33f / 2) , 
			0.5f - (0.35f / 2) , 
			0.33f , 
			0.35f
		);
		
		ui.options = UI_TITLED|UI_BORDERED;
		
		LinkedList<CSRadio> radios = new LinkedList<>();
		
		onFinish = () -> {
			
			finished = true;
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		};
		
		radios.add(option(ui , "Grayscale" , 	0xffffffff));
		radios.add(option(ui , "Red" , 			0xff0000ff));
		radios.add(option(ui , "Green" , 		0x00ff00ff));
		radios.add(option(ui , "Blue" , 		0x0000ffff));
		radios.add(option(ui , "Yellow" , 		0xffff00ff));
		radios.add(option(ui , "Cyan" , 		0x00ffffff));
		radios.add(option(ui , "Magenta" , 		0xff00ffff));
	 	
		CSRadio.groupAll(radios.toArray(CSRadio[]::new));

		CSDynamicRow row = ui.new CSDynamicRow();
		row.new CSButton("Finish" , onFinish);
		row.new CSButton("Cancel" , () -> { 
			
			onFinish.invoke();
			option = NO_OP;
			
		});
		
	}
	
	private CSRadio option(CSUserInterface ui , String name , int color) {

		CSRow row = ui.new CSRow(20).pushWidth(20).pushWidth(ui.interfaceWidth() - 60);
		CSRadio radio = row.new CSRadio("" , false , () -> option = color);
	 	CSExtendedText text = row.new CSExtendedText(name);
	 	text.colorFirst(name, color);
	 	
	 	return radio;
	 	
	}
	
	public boolean readyToFinish() {
		
		return finished;
		
	}
	
	public int option() {
		
		return option;
		
	}

}
