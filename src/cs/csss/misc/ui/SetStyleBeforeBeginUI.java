package cs.csss.misc.ui;

import static org.lwjgl.nuklear.Nuklear.nk_style_push_font;
import static org.lwjgl.nuklear.Nuklear.nk_style_pop_font;
import org.lwjgl.nuklear.NkStyle;
import org.lwjgl.nuklear.NkUserFont;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

class SetStyleBeforeBeginUI extends SCUserInterface {

	private final SCNuklear nuklear;
	private final NkStyle newStyle;
	private NkUserFont font;
	
	SetStyleBeforeBeginUI(
		SCNuklear nuklear , 
		String displayName, 
		int xPosition, 
		int yPosition, 
		int width, 
		int height , 
		NkStyle style
	) {
		
		super(nuklear , displayName, xPosition, yPosition, width, height);
		this.nuklear = nuklear;
		this.newStyle = style;
		
	}

	void setFont(NkUserFont font) {
		
		this.font = font;
		
	}
	
	@Override public void layout() {
		
		nuklear.context().style().set(newStyle);
		
		if(font != null) { 
			
			nk_style_push_font(nuklear.context() , font);
			super.layout();
			nk_style_pop_font(nuklear.context());
			
		} else {
			
			super.layout();
		
		}

	}
	
}
