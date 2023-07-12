package cs.csss.utils.debug;

import static cs.core.utils.CSUtils.require;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.csss.project.Artboard;
import cs.core.ui.CSNuklear.CSUserInterface;

public class ArtboardShaderReloaderUI {

	private CSTextEditor 
		vertexEditor , 
		fragmentEditor
	;
	
	public ArtboardShaderReloaderUI(CSNuklear nuklear) {
		
		CSUserInterface ui = nuklear.new CSUserInterface("Artboard Shader Reloader" , 210 , -1 , -1f, .9f);
		ui.options = UI_TITLED|UI_BORDERED;
		ui.setDimensions(ui.xPosition() , 77 , 275, ui.interfaceHeight());
		
		CSDynamicRow reloadButtonRow = ui.new CSDynamicRow();
		reloadButtonRow.new CSButton("Reload" , () -> {
			
			Artboard.theArtboardShader().reload(vertexEditor.toString() , fragmentEditor.toString());			
			
		});
		
		vertexEditor = addEditor(ui, "Vertex Shader");
		fragmentEditor = addEditor(ui, "Fragment Shader");
		
	}
	
	private CSTextEditor addEditor(CSUserInterface ui , final String text) {

		CSDynamicRow row = ui.new CSDynamicRow(30);
		row.new CSText(text , TEXT_MIDDLE|TEXT_CENTERED);
		row.new CSButton("Write" , () -> {});

		CSTextEditor editor = ui.new CSDynamicRow(400).new CSTextEditor(9999);
		editor.editorOptions |= EDIT_MULTILINE;
		
		return editor;
		
	}
	
	public void setShaderSources(String vertexSource, String fragmentSource) {
		
		require(vertexSource);
		require(fragmentSource);
		
		vertexEditor.setStringBuffer(vertexSource);
		fragmentEditor.setStringBuffer(fragmentSource);
				
	}

}
