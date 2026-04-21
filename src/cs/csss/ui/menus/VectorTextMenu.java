package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import java.util.Iterator;
import java.util.Objects;
import cs.csss.engine.Engine;
import cs.csss.engine.NamedNanoVGTypeface;
import cs.csss.project.CSSSProject;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu for creating vector text boxes.
 */
public class VectorTextMenu extends Dialogue {

	/**
	 * Default option for vector text editor UI elements.
	 */
	public static final int TEXT_EDIT_OPTIONS = EDIT_FIELD|EDIT_MULTILINE|EDIT_TABBABLE;
	
	private final SCUserInterface ui;
	
	private NamedNanoVGTypeface selectedTypeface;
	private boolean finished = false;

	private final SCTextEditor textEditor;
	
	/**
	 * Creates a new vector text menu.
	 * 
	 * @param nuklear the Nuklear factory
	 * @param engine the engine
	 * @param currentProject the current project
	 */
	public VectorTextMenu(SCNuklear nuklear , Engine engine , CSSSProject currentProject) {

		Objects.requireNonNull(currentProject);
		
		ui = new SCUserInterface(nuklear , "Artboard Text Editor" , .3f , .2f , .4f , .4f);
		ui.flags = UI_TITLED|UI_BORDERED;

		Runnable onFinish = () -> {

			finished = true;
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			super.onFinish();
			
		};
		
		ui.new SCDynamicRow(30).new SCText("Select Typeface");
		
		//displays of fonts to choose from
		SCRadio[] radios = new SCRadio[engine.loadedFonts.size()];
		int nextRadio = 0;
		
		Iterator<NamedNanoVGTypeface> typefaceIterator = engine.loadedFonts.iterator();
		while(typefaceIterator.hasNext()) {

			NamedNanoVGTypeface typeface = typefaceIterator.next();
			radios[nextRadio++] = ui.new SCDynamicRow(20).new SCRadio(typeface.name() , false , () -> selectedTypeface = typeface);
		
		}
		
		SCRadio.groupAll(radios);

		ui.new SCDynamicRow(30).new SCText("Input text.");
		textEditor = ui.new SCDynamicRow(150).new SCTextEditor(999);
		textEditor.flags = TEXT_EDIT_OPTIONS;

		SCDynamicRow finishRow = ui.new SCDynamicRow();
		finishRow.new SCButton("Finish" , () -> {
			
			if(canFinish()) onFinish.run();
			
		});		
		
		finishRow.new SCButton("Cancel" , onFinish);
		
	}
	
	private boolean canFinish() {
		
		return selectedTypeface != null;
		
	}
	
	/**
	 * Returns the selected typeface.
	 * 
	 * @return Selected typeface.
	 */
	public NamedNanoVGTypeface selectedTypeface() {
		
		return selectedTypeface;
		
	}
	
	/**
	 * Returns the source text that was input.
	 * 
	 * @return Source text.
	 */
	public String inputString() {
		
		return textEditor.toString();
		
	}
	
	/**
	 * Returns whether this UI is finished.
	 * 
	 * @return {@code true} if this UI is finished.
	 */
	public boolean finished() {
		
		return finished;
		
	}

}
