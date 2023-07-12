package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.TEXT_CENTERED;
import static cs.core.ui.CSUIConstants.TEXT_LEFT;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkPluginFilter;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;

public abstract class OptionMenu {

	protected final CSUserInterface ui;
	
	public OptionMenu(CSUserInterface ui) {

		this.ui = ui;
		
	}

	protected void addOptionEntry(
		String optionName , 
		Supplier<String> stateGetter , 
		int inputSize , 
		NkPluginFilter filter , 
		Consumer<String> onSet
	) {
		
		CSRow row = ui.new CSRow(30).pushWidth(0.4f).pushWidth(0.2f).pushWidth(0.2f).pushWidth(0.15f);;
		row.new CSText(optionName , TEXT_CENTERED|TEXT_LEFT);
		row.new CSText(stateGetter , TEXT_CENTERED|TEXT_LEFT);
		var editor = row.new CSTextEditor(inputSize , filter != null ? filter : CSNuklear.NO_FILTER);
		row.new CSButton("Set" , () -> onSet.accept(editor.toString()));
		
	}
	
}
