package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import java.util.Iterator;
import java.util.Objects;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.engine.Engine;
import cs.csss.engine.NamedNanoVGTypeface;
import cs.csss.project.CSSSProject;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;

/**
 * UI menu for creating vector text boxes.
 */
public class VectorTextMenu {

	/**
	 * Default option for vector text editor UI elements.
	 */
	public static final int TEXT_EDIT_OPTIONS = EDIT_FIELD|EDIT_MULTILINE|EDIT_TABBABLE;
	
	private final CSUserInterface ui;
	
	private NamedNanoVGTypeface selectedTypeface;
	private boolean finished = false;

	private final CSTextEditor textEditor;
	
	/**
	 * Creates a new vector text menu.
	 * 
	 * @param nuklear — the Nuklear factory
	 * @param engine — the engine
	 * @param currentProject — the current project
	 */
	public VectorTextMenu(CSNuklear nuklear , Engine engine , CSSSProject currentProject) {

		Objects.requireNonNull(currentProject);
		
		ui = nuklear.new CSUserInterface("Artboard Text Editor" , .3f , .2f , .4f , .4f);
		ui.options = UI_TITLED|UI_BORDERED;

		Lambda onFinish = () -> {

			finished = true;
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		};
		
		ui.new CSDynamicRow(30).new CSText("Select Typeface");
		
		//displays of fonts to choose from
		CSRadio[] radios = new CSRadio[engine.loadedFonts.size()];
		int nextRadio = 0;
		
		Iterator<NamedNanoVGTypeface> typefaceIterator = engine.loadedFonts.iterator();
		while(typefaceIterator.hasNext()) {

			NamedNanoVGTypeface typeface = typefaceIterator.next();
			radios[nextRadio++] = ui.new CSDynamicRow(20).new CSRadio(typeface.name() , false , () -> selectedTypeface = typeface);
		
		}
		
		CSRadio.groupAll(radios);

		ui.new CSDynamicRow(30).new CSText("Input text.");
		textEditor = ui.new CSDynamicRow(150).new CSTextEditor(999);
		textEditor.editorOptions = TEXT_EDIT_OPTIONS;

		CSDynamicRow finishRow = ui.new CSDynamicRow();
		finishRow.new CSButton("Finish" , () -> {
			
			if(canFinish()) onFinish.invoke();
			
		});		
		
		finishRow.new CSButton("Cancel" , onFinish);
		
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
