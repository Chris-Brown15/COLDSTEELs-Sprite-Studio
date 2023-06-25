package cs.csss.core;

import static cs.core.ui.CSUIConstants.*;

import java.io.File;
import java.util.LinkedList;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;

public class LoadProjectMenu {

	private String selected = null;
	private String[] entries;
	
	private boolean readyToFinish = false;
	
	
	public LoadProjectMenu(CSNuklear nuklear) {

		CSUserInterface ui = nuklear.new CSUserInterface("Load Project" , 0.5f - (0.33f / 2) , 0.5f - (0.35f / 2) , 0.33f , 0.35f);
		
		ui.options = UI_TITLED|UI_BORDERED;
		
		// load names of projects
		
		entries = new File("data/").list();
		
		Lambda onFinish = () -> {
	 		
			nuklear.removeUserInterface(ui);
	 		ui.shutDown();
	 		readyToFinish = true;
	 		
	 	};
		
		LinkedList<CSRadio> radios = new LinkedList<>();
		
		for(String x : entries) radios.add(ui.new CSDynamicRow(20).new CSRadio(x , false , () -> selected = x));
		
		CSRadio.groupAll(radios.toArray(CSRadio[]::new));
		
	 	CSDynamicRow bottomRow = ui.new CSDynamicRow();
		
	 	bottomRow.new CSButton("Finish" , () -> {
	 	
	 		if(selected == null) return;
	 		onFinish.invoke();
	 		
	 	});
	 	
	 	bottomRow.new CSButton("Cancel" , () -> {
	 		
	 		selected = null;
	 		onFinish.invoke();
	 		
	 	});
	 	
	}
	
	public String get() {
		
		return selected;
		
	}
	
	public boolean readyToFinish() {
		
		return readyToFinish;
		
	}

}
