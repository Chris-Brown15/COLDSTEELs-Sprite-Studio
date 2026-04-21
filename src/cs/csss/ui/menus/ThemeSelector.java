package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import java.util.Collection;

import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI for selecting a theme for the user interface.
 */
public class ThemeSelector extends Dialogue {

	private static final float w = .2f , h = .15f , x = .5f - (w /2) , y = .5f - (h / 2);
	private final SCNuklear nuklear;
	
	private CSFile selected;
	private SCUserInterface ui;
	
	private boolean finished = false;
	
	/**
	 * Creates a new theme selector.
	 * 
	 * @param nuklear � the nuklear instance
	 */
	public ThemeSelector(Engine engine , SCNuklear nuklear) {

		this.nuklear = nuklear;
		ui = new SCUserInterface(nuklear , "Select Theme" , x , y , w , h);
		ui.flags = UI_TITLED|UI_BORDERED;
		
		CSFolder folder = new CSFolder("themes", CSFolder.getRoot("assets"));
		
		Collection<CSFile> files = folder.files();
		SCRadio[] radios = new SCRadio[files.size()];
		
		for(CSFile x : files) ui.new SCDynamicRow().new SCRadio(x.name() , false , () -> selected = x);
		
		SCRadio.groupAll(radios);
		
		ui.new SCDynamicRow().new SCButton("Reset to Default Theme" , engine::resetThemeToDefault);
		
		SCDynamicRow finishRow = ui.new SCDynamicRow();
		
		finishRow.new SCButton("Finish" , () -> {
		
			if(selected != null) onFinish();
			
		});
		
		finishRow.new SCButton("Cancel" , () -> {
			
			onFinish();
			selected = null;
			
		});
		
	}
	
	/**
	 * Returns the selected file, which will be <code>null</code> if none was selected.
	 * 
	 * @return the selected file, or <code>null</code>.
	 */
	public CSFile selected() {
		
		return selected;
		
	}

	/**
	 * Returns whether the UI is finished, either because it was cancelled or the user selected a theme and pressed finish. 
	 * 
	 * @return Whether this UI is finished.
	 */
	public boolean finished() {
		
		return finished;
		
	}
	
	@Override public void onFinish() {
		
		finished = true;
		super.onFinish();
		nuklear.removeUserInterface(ui);
		Engine.THE_TEMPORAL.onTrue(() -> true, ui::shutDown);
		
	}
	
}
