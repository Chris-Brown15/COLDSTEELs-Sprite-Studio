package cs.csss.editor.ui;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_button_color;
import static org.lwjgl.nuklear.Nuklear.nk_property_int;

import java.nio.IntBuffer;
import java.util.function.BooleanSupplier;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

/**
 * Color picker used for two channel color pickers.
 */
class TwoChannelColorPicker {

	private final NkContext context;
	
	BooleanSupplier doLayout = () -> true;
	
	short gray = 0, alpha = 255;
	
	boolean hasAlpha = false;
	
	/**
	 * Creates a two channel color picker.
	 * 
	 * @param context — the nuklear context
	 */
	TwoChannelColorPicker(NkContext context) {
		
		this.context = context;
		
	}
	
	void set(short gray , short alpha) {
		
		this.gray = gray;
		this.alpha = alpha;
		
	}
	
	void layout() {
		
		if(!doLayout.getAsBoolean()) return;
		
		nk_layout_row_dynamic(context , 160 , 1);
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			NkColor color = NkColor.malloc(stack).set((byte)gray , (byte)gray , (byte)gray , (byte)alpha);
			nk_button_color(context , color);
			
			IntBuffer gray = stack.mallocInt(1).put(this.gray).rewind();
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_int(context, "Gray", 0, gray , 255, 1, 1);
			
			this.gray = (short) gray.get();
			
			if(hasAlpha) {

				IntBuffer alpha = stack.mallocInt(1).put(this.alpha).rewind();
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_property_int(context, "Alpha", 0, alpha , 255, 1, 1);
				
				this.alpha = (short) alpha.get();
				
			}
			
		}
		
	}

}