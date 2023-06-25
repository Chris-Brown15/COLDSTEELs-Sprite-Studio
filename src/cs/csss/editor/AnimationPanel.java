package cs.csss.editor;

import static cs.core.ui.CSUIConstants.UI_BORDERED;
import static cs.core.ui.CSUIConstants.UI_TITLED;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;

public class AnimationPanel {

	private CSUserInterface ui;
	
	public AnimationPanel(Editor editor , CSNuklear nuklear) {

		ui = nuklear.new CSUserInterface("Animation Viewer" , 0.203f , 0.78f , 0.594f , 0.1925f);
		
		ui.options = UI_TITLED|UI_BORDERED;
		
//		CSDynamicRow nameRow = editor.currentAnimation().currentAnimation();
		
		
	}

}
