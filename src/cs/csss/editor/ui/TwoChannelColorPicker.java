package cs.csss.editor.ui;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_button_color;
import static org.lwjgl.nuklear.Nuklear.nk_propertyi;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;

/**
 * Color picker used for two channel color pickers.
 */
public class TwoChannelColorPicker extends CSElement {

	public short gray = 0, alpha = 255;
	
	public boolean hasAlpha = false;
	
	/**
	 * Creates a two channel color picker.
	 * 
	 * @param context the nuklear context
	 * @param source the owning layout
	 */
	public TwoChannelColorPicker(NkContext context , CSLayout source) {
		
		source.super();
		
		setCode(() -> {
			
			try(MemoryStack stack = MemoryStack.stackPush()) {
				
				NkColor color = NkColor.malloc(stack).set((byte)gray , (byte)gray , (byte)gray , (byte)alpha);
				nk_button_color(context , color);
				
				nk_layout_row_dynamic(context , 30 , 1);
				gray = (short) nk_propertyi(context, "Gray", 0, gray , 255, 1, 1);
				
				if(hasAlpha) {

					nk_layout_row_dynamic(context , 30 , 1);
					alpha = (short) nk_propertyi(context, "Alpha", 0, alpha , 255, 1, 1);
					
				} else alpha = 0xff;
				
			}
			
		});
		
	}
	
	@Override protected void onResize() {}

}