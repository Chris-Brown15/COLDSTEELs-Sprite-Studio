/**
 * 
 */
package cs.csss.ui.menus;

import static org.lwjgl.nuklear.Nuklear.NK_RGBA;
import static org.lwjgl.nuklear.Nuklear.NK_RGB;

import cs.csss.editor.ui.TwoChannelColorPicker;

import static sc.core.ui.SCUIConstants.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.joml.Vector4f;
import org.lwjgl.nuklear.NkPluginFilter;

import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.ui.elements.PublicFormatColorPicker;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUI.SCRow;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * Dialogue for selecting a color.
 */
public class ChooseColorDialogue extends Dialogue {
	
	private SCUserInterface ui;
	private SCNuklear nuklear;
	
	private PublicFormatColorPicker rgbPicker;
	private TwoChannelColorPicker grayPicker;
	
	private boolean inputsAreHex;
	
	/**
	 * Creates a new color chooser dialogue. {@code channelsPerPixel} should be a function that does not become invalid if the current project is 
	 * changed. For example prefer to use {@link cs.csss.editor.Editor#project() editor.project()}'s 
	 * {@link cs.csss.project.CSSSProject#channelsPerPixel() channelsPerPixel()}.
	 * 
	 * @param nuklear the nuklear
	 * @param inputsAreHex whether inputs to text fields are to be given as hex 
	 * @param title title for the UI element
	 * @param xRatio ratio of the window width for the top left corner of the dialogue
	 * @param yRatio ratio of the window height for the top left corner of the dialogue
	 * @param channelsPerPixel supplier for the current number of channels per pixel of the current project
	 * @param onAccept code to invoke when the "Accept" button is pressed 
	 * @param onCancel code to invoke when the "Cancel" button is pressed
	 * @throws NullPointerException if any parameter is <code>null</code>.
	 */
	public ChooseColorDialogue(
		SCNuklear nuklear , 
		boolean inputsAreHex ,
		String title , 
		float xRatio , 
		float yRatio , 
		IntSupplier channelsPerPixel , 
		Consumer<ColorPixel> onAccept , 
		Runnable onCancel
	) {

		Objects.requireNonNull(nuklear);
		Objects.requireNonNull(title);
		Objects.requireNonNull(channelsPerPixel);
		Objects.requireNonNull(onAccept);
		Objects.requireNonNull(onCancel);
		
		this.inputsAreHex = inputsAreHex;
		
		this.nuklear = nuklear;
		ui = new SCUserInterface(nuklear , title , xRatio , yRatio , 350 , 460);	
		ui.flags |= UI_TITLED|UI_BORDERED|UI_UNSCROLLABLE;
		
		SCDynamicRow rgbRow = ui.new SCDynamicRow(200) ; rgbRow.doLayout = () -> channelsPerPixel.getAsInt() >= 3;
		SCDynamicRow grayRow = ui.new SCDynamicRow(200) ; grayRow.doLayout = () -> channelsPerPixel.getAsInt() <= 2;
		
		rgbPicker = new PublicFormatColorPicker(nuklear.context() , rgbRow , channelsPerPixel.getAsInt() == 4 ? NK_RGBA : NK_RGB);
		grayPicker = new TwoChannelColorPicker(nuklear.context() , grayRow);
		
		int maxCharacters = inputsAreHex ? 3 : 4;
		NkPluginFilter filter = inputsAreHex ? SCNuklear.HEX_FILTER : SCNuklear.DECIMAL_FILTER;

		SCRow grayInputRow = ui.new SCRow(30).pushWidth(.15f).pushWidth(.85f) ; grayInputRow.doLayout = () -> channelsPerPixel.getAsInt() <= 2;
		grayInputRow.new SCText("Gray:" , TEXT_CENTERED|TEXT_LEFT);
		SCTextEditor grayEditor = grayInputRow.new SCTextEditor(maxCharacters , filter);

		SCRow redInputRow = ui.new SCRow(30).pushWidth(.15f).pushWidth(.85f) ; redInputRow.doLayout = () -> channelsPerPixel.getAsInt() >= 3;
		redInputRow.new SCText("Red:" , TEXT_CENTERED|TEXT_LEFT);
		SCTextEditor redEditor =  redInputRow.new SCTextEditor(maxCharacters , filter);

		SCRow greenInputRow = ui.new SCRow(30).pushWidth(.15f).pushWidth(.85f) ; greenInputRow.doLayout = () -> channelsPerPixel.getAsInt() >= 3;
		greenInputRow.new SCText("Green:" , TEXT_CENTERED|TEXT_LEFT);
		SCTextEditor greenEditor =  greenInputRow.new SCTextEditor(maxCharacters , filter);

		SCRow blueInputRow = ui.new SCRow(30).pushWidth(.15f).pushWidth(.85f) ; blueInputRow.doLayout = () -> channelsPerPixel.getAsInt() >= 3;
		blueInputRow.new SCText("Blue:" , TEXT_CENTERED|TEXT_LEFT);
		SCTextEditor blueEditor =  blueInputRow.new SCTextEditor(maxCharacters , filter);

		SCRow alphaInputRow = ui.new SCRow(30).pushWidth(.15f).pushWidth(.85f) ; alphaInputRow.doLayout = () -> {
			
			int channels = channelsPerPixel.getAsInt();
			return channels == 4 || channels == 2;
			
		};
		
		alphaInputRow.new SCText("Alpha:" , TEXT_CENTERED|TEXT_LEFT);
		SCTextEditor alphaEditor = alphaInputRow.new SCTextEditor(maxCharacters , filter);

		//handles immediate mode effects
		ui.attachedLayout((context) -> {
			
			int channels = channelsPerPixel.getAsInt();
			grayPicker.hasAlpha = channels == 2;
			if(channels == 4) rgbPicker.format(NK_RGBA);
			else rgbPicker.format(NK_RGB);
			
			String redString = redEditor.toString();
			String greenString = greenEditor.toString();
			String blueString = blueEditor.toString();
			String alphaString = alphaEditor.toString();
			String grayString = grayEditor.toString();
			
			Vector4f values = new Vector4f(rgbPicker.colorAsFloats());
			
			short red = (short)(values.x() * 0xff);
			short green = (short)(values.y() * 0xff);
			short blue = (short)(values.z() * 0xff);
			short alpha = channels == 4 ? (short)(values.w() * 0xff) : channels == 2 ? grayPicker.alpha : 0xff; 
			short gray = grayPicker.gray;
			
			String maxString = inputsAreHex ? "ff" : "255";
			
			if(canParse(redString)) { 
				
				red = parse(redString);
				if(red > 255) redEditor.setStringBuffer(maxString);
				
			}
			
			if(canParse(greenString)) { 
				
				green = parse(greenString);
				if(green > 255) greenEditor.setStringBuffer(maxString);
				
			}
			
			if(canParse(blueString)) { 
				
				blue = parse(blueString);
				if(blue > 255) blueEditor.setStringBuffer(maxString);
				
			}
			
			if(canParse(alphaString)) { 
				
				alpha = parse(alphaString);
				if(alpha > 255) alphaEditor.setStringBuffer(maxString);
				
			}
			
			if(canParse(grayString)) { 
				
				gray = parse(grayString);
				if(gray > 255) alphaEditor.setStringBuffer(maxString);
				
			}
			
			rgbPicker.setColor((byte)red, (byte)green, (byte)blue, (byte)alpha);
			grayPicker.gray = gray;
			grayPicker.alpha = alpha;
						
		});
		
		SCDynamicRow finishRow = ui.new SCDynamicRow();
		finishRow.new SCButton("Finish" , () -> {
			
			byte r = 0 , g = 0 , b = 0 , a = (byte)0xff;
			
			switch(channelsPerPixel.getAsInt()) {
				case 1 -> r = g = b = (byte) grayPicker.gray;
				case 2 -> {
					
					r = g = b = (byte) grayPicker.gray;
					a = (byte) grayPicker.alpha;
				}
								
				case 3 -> {
					
					Vector4f selected = new Vector4f(rgbPicker.colorAsFloats());
					r = (byte)(selected.x() * 0xff);
					g = (byte)(selected.y() * 0xff);
					b = (byte)(selected.z() * 0xff);
					
				}
					
				case 4 -> {
					
					Vector4f selected = new Vector4f(rgbPicker.colorAsFloats());
					r = (byte)(selected.x() * 0xff);
					g = (byte)(selected.y() * 0xff);
					b = (byte)(selected.z() * 0xff);
					a = (byte)(selected.w() * 0xff);
					
				}	
					
			}
			
			ChannelBuffer result = new ChannelBuffer();
			switch(channelsPerPixel.getAsInt()) {
				case 1 -> result.set((byte)r , (byte)r , (byte)r , (byte)0xff);
				case 2 -> result.set((byte)r , (byte)r , (byte)r , (byte)a);
				case 3 -> result.set((byte)r , (byte)g , (byte)b , (byte)0xff);
				case 4 -> result.set((byte)r , (byte)g , (byte)b , (byte)a);
			}
			
			onAccept.accept(result);
			onFinish();
			
		});
		
		finishRow.new SCButton("Cancel" , this::onFinish);
		
	}

	private boolean canParse(String source) {
		
		return !source.equals("") && !source.contains("-");
		
	}
	
	private short parse(String source) {
		
		return Short.parseShort(source, inputsAreHex ? 16 : 10);
		
	}
	
	@Override public void onFinish() {
		
		super.onFinish();
		ui.shutDown();
		nuklear.removeUserInterface(ui);
		
	}

}
