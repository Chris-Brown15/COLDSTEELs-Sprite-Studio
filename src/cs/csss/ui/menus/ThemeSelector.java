package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import java.util.Collection;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;

/**
 * UI for selecting a theme for the user interface.
 */
public class ThemeSelector extends Dialogue {

	private static final float w = .2f , h = .15f , x = .5f - (w /2) , y = .5f - (h / 2);
	private final CSNuklear nuklear;
	
	private CSFile selected;
	private CSUserInterface ui;
	
	private boolean finished = false;
	
	/**
	 * Creates a new theme selector.
	 * 
	 * @param nuklear — the nuklear instance
	 */
	public ThemeSelector(Engine engine , CSNuklear nuklear) {

		this.nuklear = nuklear;
		ui = nuklear.new CSUserInterface("Select Theme" , x , y , w , h);
		ui.options = UI_TITLED|UI_BORDERED;
		
		CSFolder folder = new CSFolder("themes", CSFolder.getRoot("assets"));
		
		Collection<CSFile> files = folder.files();
		CSRadio[] radios = new CSRadio[files.size()];
		
		for(CSFile x : files) ui.new CSDynamicRow().new CSRadio(x.name() , false , () -> selected = x);
		
		CSRadio.groupAll(radios);
		
		ui.new CSDynamicRow().new CSButton("Reset to Default Theme" , engine::resetThemeToDefault);
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		
		finishRow.new CSButton("Finish" , () -> {
		
			if(selected != null) onFinish();
			
		});
		
		finishRow.new CSButton("Cancel" , () -> {
			
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
		ui.shutDown();
		
	}
	
}
