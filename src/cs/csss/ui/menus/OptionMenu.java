package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.TEXT_CENTERED;
import static sc.core.ui.SCUIConstants.TEXT_LEFT;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.nuklear.NkPluginFilter;

import sc.core.ui.SCElements.SCUI.SCRow;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * Base class for option menus.
 */
public abstract class OptionMenu extends Dialogue {

	protected final SCUserInterface ui;
	
	/**
	 * Creates a new optin menu on the given {@code ui}.
	 * 
	 * @param ui a UI to put menus on
	 */
	public OptionMenu(SCUserInterface ui) {

		this.ui = ui;
		
	}

	protected void addOptionEntry(
		String optionName , 
		Supplier<String> stateGetter , 
		int inputSize , 
		NkPluginFilter filter , 
		Consumer<String> onSet
	) {
		
		SCRow row = ui.new SCRow(30).pushWidth(0.4f).pushWidth(0.2f).pushWidth(0.2f).pushWidth(0.15f);;
		row.new SCText(optionName , TEXT_CENTERED|TEXT_LEFT);
		row.new SCText(stateGetter , TEXT_CENTERED|TEXT_LEFT);
		var editor = row.new SCTextEditor(inputSize , filter != null ? filter : SCNuklear.NO_FILTER);
		row.new SCButton("Set" , () -> onSet.accept(editor.toString()));
		
	}
	
}
