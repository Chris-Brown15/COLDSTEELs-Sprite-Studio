package cs.csss.editor.ui;

import static cs.core.ui.CSUIConstants.*;
import static cs.csss.ui.utils.UIUtils.toolTip;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_propertyi;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_button_color;
import org.joml.Vector4f;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSButton;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSColorPicker;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.csss.editor.DebugDisabledException;
import cs.csss.editor.Editor;
import cs.csss.editor.brush.CSSSBrush;
import cs.csss.editor.brush.CSSSModifyingBrush;
import cs.csss.editor.palette.ColorPalette;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.Engine;
import cs.csss.engine.Logging;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.ui.menus.DialogueInputBox;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.ui.prefabs.InputBox;
import cs.core.utils.CSUtils;

/**
 * Left hand side panel. This panel contains buttons and UI elements for modifying artboards.
 */
public class LHSPanel {

	private final CSUserInterface ui;
	private final CSNuklear nuklear;
	
	private CSColorPicker rgbaChooser , rgbChooser;
	private TwoChannelColorPicker twoDColor; 

	private final Editor editor;
	private byte[] previousFrameColorBuffer = new byte[4] , currentColorBuffer = new byte[4];
	private boolean previousColorEqualsCurrent;
	
	/**
	 * Creates a left hand side panel.
	 * 
	 * @param editor — the editor
	 * @param nuklear — Nuklear factory
	 */
	public LHSPanel(Editor editor , CSNuklear nuklear) {

		this.editor = editor;
		this.nuklear = nuklear;
		ui = nuklear.new CSUserInterface("Editor" , 0.001f , -1f , 0.199f , .90f);
		ui.setDimensions(ui.xPosition() , 77 , ui.interfaceWidth() , ui.interfaceHeight());
		ui.options = UI_BORDERED|UI_TITLED|UI_ICONIFYABLE;
		
		CSDynamicRow addProjectRow = ui.new CSDynamicRow();
		addProjectRow.new CSButton("New Project" , editor::startNewProject);
		
		addProjectRow.doLayout = () -> editor.project() == null;
		
		Engine.THE_TEMPORAL.onTrue(() -> editor.project() != null , () -> {

			CSDynamicRow rgbaColorRow = ui.new CSDynamicRow(200);
			CSDynamicRow rgbColorRow = ui.new CSDynamicRow(200);
			
			rgbaChooser = rgbaColorRow.new CSColorPicker(RGBA);
			rgbChooser = rgbColorRow.new CSColorPicker(RGB);
			twoDColor = new TwoChannelColorPicker(nuklear.context());
			
			rgbaColorRow.doLayout = () -> channels() == 4;
			rgbColorRow.doLayout = () -> channels() == 3;
			twoDColor.doLayout = () -> channels() <= 2;
			
			ui.attachedLayout((context , stack) -> {
				
				int channels = channels();
				
				twoDColor.hasAlpha = channels == 2;
				twoDColor.layout();
				colors(currentColorBuffer);
				
				if(channels >= 3) {

					nk_layout_row_dynamic(context , 20 , channels);
					nk_text(context , String.format("Red: %d", (short)Byte.toUnsignedInt(currentColorBuffer[0])) , TEXT_LEFT);
					nk_text(context , String.format("Green: %d", (short)Byte.toUnsignedInt(currentColorBuffer[1])) , TEXT_LEFT);
					nk_text(context , String.format("Blue: %d", (short)Byte.toUnsignedInt(currentColorBuffer[2])) , TEXT_LEFT);
					if(channels == 4) nk_text(context , String.format("Alpha: %d", (short)Byte.toUnsignedInt(currentColorBuffer[3])) , TEXT_LEFT);
				
				}
				
			});
			
			CSDynamicRow grayRow = ui.new CSDynamicRow(30) ; grayRow.doLayout = () -> channels() == 1 || channels() == 2;
			CSDynamicRow rgbRow = ui.new CSDynamicRow(30) ; rgbRow.doLayout = () -> channels() >= 3; 
			CSDynamicRow alphaRow1 = ui.new CSDynamicRow(30) ; alphaRow1.doLayout = () -> channels() == 4;
			CSDynamicRow alphaRow2 = ui.new CSDynamicRow(30) ; alphaRow2.doLayout = () -> channels() == 2;
			colorInput("Gray" , 0 , grayRow);			
			colorInput("Alpha" , 1 , alphaRow2);
			colorInput("Red" , 0 , rgbRow);
			colorInput("Green" , 1 , rgbRow);
			colorInput("Blue" , 2 , rgbRow);
			colorInput("Alpha" , 3 , alphaRow1);
			
			CSDynamicRow wholeColorButtonRow = ui.new CSDynamicRow(30) ; wholeColorButtonRow.doLayout = () -> channels() > 1;
			wholeColorButtonRow.new CSButton("Input Whole Color" , this::startWholeColorInput);
			
			/*
			 * Auto Palette
			 */
			ui.attachedLayout((context , stack) -> {
				
				List<ColorPixel> currentPalette = editor.currentPaletteColorsAsList();
				
				if(currentPalette != null) {
					
					int sublistSize = currentPalette.size();
					if(sublistSize == 0) return;
										
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , "Recent Colors:" , TEXT_LEFT|TEXT_CENTERED);
					
					nk_layout_row_dynamic(context , 30 , 15);
										
					for(ListIterator<ColorPixel> iter = currentPalette.listIterator(0) ; iter.hasNext() ; ) {
						
						ColorPixel x = iter.next();
						if(nk_button_color(context , colorForCurrentChannels(x , stack))) editor.setSelectedColor(x);
						
					}
					
				}
				
			});
			
			/*
			 * Color Palettes
			 */
	
			ui.attachedLayout((context , stack) -> {
				
				Artboard currentArtboard = editor.currentArtboard();
				if(currentArtboard == null) return;
				
				Iterator<ColorPalette> generators = ColorPalette.palettes();
				while(generators.hasNext()) {
					
					ColorPalette next = generators.next();
					
					if(!next.show()) continue;
					
					ColorPixel currentColor = new ChannelBuffer(
						currentColorBuffer[0] , 
						currentColorBuffer[1] , 
						currentColorBuffer[2] , 
						currentColorBuffer[3]
					);
					
					ColorPixel[] pixels;
					if(!previousColorEqualsCurrent) {
						
						if(Engine.isDebug()) try {
							
							pixels = next.generate(currentColor , editor.project().getChannelsPerPixelOfCurrentLayer());

						} catch(Exception e) {
							
							Logging.sysDebug("Color Palette generate colors failed");
							e.printStackTrace();
							continue;
							
						} else pixels = next.generate(currentColor , editor.project().getChannelsPerPixelOfCurrentLayer());
					
					} else pixels = next.get();

					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , next.name + ":", TEXT_LEFT|TEXT_CENTERED);
					
					nk_layout_row_dynamic(context , 30 , 15);
					for(ColorPixel x : pixels) if(nk_button_color(context , colorForCurrentChannels(x , stack))) editor.setSelectedColor(x);
				
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				nk_text(context , "Selected Color: " , TEXT_CENTERED|TEXT_LEFT);
				ColorPixel currentColor = (ColorPixel)editor.selectedColorValues();
				nk_button_color(context , colorForCurrentChannels(currentColor , stack));	
				
			});
			
			/*
			 * Cursor Artboard Positions
			 */
			
			ui.attachedLayout((context , stack) -> {
				
				CSSSProject project = editor.project(); 
				Artboard current = project != null ? project.currentArtboard() : null;
				if(current == null) return;
				nk_layout_row_dynamic(context , 20 , 2);
				
				int[] indices = current.worldToPixelIndices(editor.cursorCoords());
				if(indices[0] < 0 || indices[0] >= current.width()) indices[0] = -1;
				if(indices[1] < 0 || indices[1] >= current.height()) indices[1] = -1;
				String xString = indices[0] == -1 ? "X: OoB" : "X: " + indices[0];
				String yString = indices[1] == -1 ? "Y: OoB" : "Y: " + indices[1];
				nk_text(context , xString , TEXT_LEFT|TEXT_CENTERED);
				nk_text(context , yString , TEXT_LEFT|TEXT_CENTERED);
				
			});
			
			/*
			 * Brushes 
			 */
			
			Iterator<CSSSBrush> brushes = CSSSBrush.allBrushes();
			CSSSBrush iter;
			
			while(brushes.hasNext()) {
				
				iter = brushes.next();
				
				CSSSBrush finalIter = iter;
				
				String uiText = radioTextFromBrushName(iter);
				CSDynamicRow row = ui.new CSDynamicRow(25);
				CSRadio newRadio = row.new CSRadio(uiText , () -> editor.currentBrush() == finalIter , () -> editor.setBrushTo(finalIter));	
				toolTip(newRadio , iter.toolTip);
				
			}
			
			//script brushes next
			CSDynamicRow scriptBrushRow = ui.new CSDynamicRow(30);						
			CSRadio simpleBrushRadio = scriptBrushRow.new CSRadio(
				"Script Brush" , 
				() -> editor.currentBrush() != null && editor.currentBrush() == Editor.theScriptBrush2() , 
				() -> editor.setBrushTo(Editor.theScriptBrush2()));
			
			scriptBrushRow.new CSButton("Select" , () -> editor.startSelectSimpleScriptBrush2(simpleBrushRadio));
			
			CSDynamicRow modifyingScriptBrushRow = ui.new CSDynamicRow(30);
			CSRadio scriptModifyingBrushRadio = modifyingScriptBrushRow.new CSRadio(
				"Modifying Script Brush" , 
				() -> editor.currentBrush() != null && editor.currentBrush() == Editor.theModifyingScriptBrush2() , 
				() -> editor.setBrushTo(Editor.theModifyingScriptBrush2()));

			modifyingScriptBrushRow.new CSButton("Select" , () -> editor.startSelectModifyingScriptBrush2(scriptModifyingBrushRadio));
			
			CSDynamicRow selectingBrushRow = ui.new CSDynamicRow(30);
			CSRadio scriptSelectingBrushRadio = selectingBrushRow.new CSRadio(
				"Selecting Script Brush" , 
				() -> editor.currentBrush() != null && editor.currentBrush() == Editor.theSelectingScriptBrush2() , 
				() -> editor.setBrushTo(Editor.theSelectingScriptBrush2()));

			selectingBrushRow.new CSButton("Select" , () -> editor.startSelectSelectingScriptBrush2(scriptSelectingBrushRadio));
			
			//this is a slider that lets you modify the brush radius if the brush is a modifying brush
			ui.attachedLayout((context , stack) -> {
							
				if(editor.currentBrush() instanceof CSSSModifyingBrush) {
				
					CSSSModifyingBrush asModifying = (CSSSModifyingBrush) editor.currentBrush();					
					nk_layout_row_dynamic(context , 30 , 1);									
					asModifying.radius(nk_propertyi(context , "Brush Radius" , 1 , asModifying.radius() + 1 , editor.maxBrushRadius() , 1 , 1) - 1);
					
				}
				
				//set the last frame color value and update the editor if the last frame color doesnt match the current 
				previousColorEqualsCurrent = Arrays.equals(previousFrameColorBuffer, currentColorBuffer);
				if(!previousColorEqualsCurrent) { 
					
					editor.setSelectedColor(currentColorBuffer[0], currentColorBuffer[1], currentColorBuffer[2], currentColorBuffer[3]);
				
				}

				for(int i = 0 ; i < previousFrameColorBuffer.length ; i++) previousFrameColorBuffer[i] = currentColorBuffer[i];
				
			});
				
		});

		if(Engine.isDebug()) {
			
			CSDynamicRow debugProjectRow = ui.new CSDynamicRow();
			
			debugProjectRow.new CSButton("add debug proj" , () -> {

				try {

					editor.requestDebugProject();
					
				} catch (DebugDisabledException e) {
					
					throw new IllegalStateException(e);
				}
				
			});
		
			debugProjectRow.doLayout = () -> editor.project() == null;
			
			ui.new CSDynamicRow().new CSButton("Arrange Animations" , () -> {
				
				CSSSProject project = editor.project();				
				if(project != null) project.arrangeArtboards();
				
			});
			
		}
		
	}	
	
	private void colorInput(final String color , int index , CSLayout layout) {
		
		CSButton button = layout.new CSButton("Input " + color , () -> {
			
			boolean hexWhenStarted = editor.colorInputsAreHex();
			
			new InputBox(
				nuklear , 
				"Input " + color + " Value" , 
				.4f , 
				0.4f , 
				hexWhenStarted ? 3 : 4 , 
				hexWhenStarted ? CSNuklear.HEX_FILTER : CSNuklear.DECIMAL_FILTER , 
				res -> {
				
				if(res.length() == 0) return;
				
				int value = hexWhenStarted ? CSUtils.parseHexInt(res, 0, res.length()) : Integer.parseInt(res);
				
				if(channels() < 3) { 
					
					if(index == 0) twoDColor.gray = (short)value;
					else twoDColor.alpha = (short)value;
					
				} else {
					
					byte[] colors = colors();
					colors[index] = (byte) value;					
					rgbPicker().color(colors[0], colors[1], colors[2], channels() == 4 ? colors[3] : (byte)0xff);
					
				}				
				
			});
			
		});
		
		if(!color.equals("Alpha")) toolTip(button , "Input a " + color + " value directly, in hex.");
		else toolTip(button , "Input an " + color + " value directly, in hex.");					
		
	}
	
	/**
	 * Creates and returns an array of colors.
	 * 
	 * @param colors — destination for color values
	 * @return {@code colors} after writing.
	 */
	public byte[] colors(byte[] colors) {
				
		if(channels() > 2) {
			
			Vector4f colorVector = rgbPicker().color();
			for(int i = 0 ; i < colors.length ; i++) colors[i] = (byte) (colorVector.get(i) * 255);
			return colors;
			
		} else {
			
			colors[0] = (byte) twoDColor.gray;
			if(channels() == 2) colors[1] = (byte) twoDColor.alpha;
			else colors[1] = 0;
			return colors;
			
		}
		
	}
	
	/**
	 * Gets and returns the active color.
	 * 
	 * @return The color selected by the color picker.
	 */
	public byte[] colors() {
		
		return colors(new byte[channels()]);
		
	}
	
	/**
	 * Sets the color of the color picker.
	 * 
	 * @param pixel — a new color  
	 */
	public void setColor(ColorPixel pixel) {
		
		setColor(pixel.r() , pixel.g() , pixel.b() , pixel.a());
		
	}

	/**
	 * Sets the color of the color picker.
	 * 
	 * @param pixel — a new color  
	 */
	public void setColor(byte r , byte g, byte b, byte a) {
		
		if(channels() <= 2) {

			twoDColor.gray = (short) Byte.toUnsignedInt(r);
			if(channels() == 2) twoDColor.alpha = (short) Byte.toUnsignedInt(g);
			
		} else rgbPicker().color(r , g, b, a);
		
	}

	/**
	 * Returns the left X coordinate of the LHS.
	 * 
	 * @return Left x coordinate of the LHS.
	 */
	public int leftX() {
		
		return ui.xPosition();
		
	}
	
	/**
	 * Returns the top Y coordinate of the LHS.
	 * 
	 * @return Top y coordinate of the LHS.
	 */
	public int topY() {
		
		return ui.yPosition();
		
	}
	
	/**
	 * Returns the width of the LHS.
	 * 
	 * @return Width of the LHS.
	 */
	public int width() {
		
		return ui.interfaceWidth();
		
	}
	
	/**
	 * Returns the height of the LHS.
	 * 
	 * @return Height of the LHS.
	 */
	public int height() {
		
		return ui.interfaceHeight();
		
	}
	
	/**
	 * Returns the right x coordinate of the LHS.
	 * 
	 * @return Right X coordinate.
	 */
	public int rightX() {
		
		return leftX() + width();
		
	}
	
	/**
	 * Returns the bottom y coordinate of the LHS.
	 * 
	 * @return Bottom Y coordinate.
	 */
	public int bottomY() {
		
		return topY() - height();
		
	}
	
	private CSColorPicker rgbPicker() {
		
		return channels() == 4 ? rgbaChooser : rgbChooser;
		
	}
	
	private String radioTextFromBrushName(CSSSBrush brush) {

		return brush.getClass().getSimpleName().replace("Brush", "").replace('_', ' ');
		
	}
	
	private int channels() {
		
		return editor.project().getChannelsPerPixelOfCurrentLayer();
		
	}
	
	private NkColor colorForCurrentChannels(ColorPixel source , MemoryStack stack) {
		
		if(source == null) { 
			
			byte zero = (byte)0;
			return NkColor.malloc(stack).set(zero , zero , zero , zero);
			
		}
		
		int channels = channels();
		byte max = (byte) 0xff;
		return switch(channels) {
			case 1 -> NkColor.malloc(stack).set(source.r() , source.r() , source.r() , max);
			case 2 -> NkColor.malloc(stack).set(source.r() , source.r() , source.r() , source.a());
			case 3 -> NkColor.malloc(stack).set(source.r() , source.g() , source.b() , max);
			case 4 -> NkColor.malloc(stack).set(source.r() , source.g() , source.b() , source.a());
			default -> throw new IllegalArgumentException("Unexpected value: " + channels);
		
		};
				
	}
	
	private void startWholeColorInput() {

		boolean hexWhenStarted = editor.colorInputsAreHex();
		int channels = editor.project().getChannelsPerPixelOfCurrentLayer();
		int charsPerChannel = hexWhenStarted ? 2 : 3;
		
		int totalChars = channels * charsPerChannel;
		new DialogueInputBox(
			nuklear , 
			"Input Color Code" , 
			.4f , 
			.4f , 
			totalChars + 1 , 
			hexWhenStarted ? CSNuklear.HEX_FILTER : CSNuklear.DECIMAL_FILTER , 
			res -> {
			
				if(res.length() == 0) return;
				
				int red , green , blue , alpha;
	
				if(res.length() < 4 * charsPerChannel) {
					
					StringBuilder append = new StringBuilder(res);
					while(append.length() < totalChars) append.append('0');
					res = append.toString();
					
				}
				
				Function<String , Integer> parse = hexWhenStarted ? string -> CSUtils.parseHexInt(string, 0, charsPerChannel) : Integer::parseInt;
				red = parse.apply(res.substring(0, charsPerChannel));
				green = parse.apply(res.substring(charsPerChannel, 2 * charsPerChannel));
				blue = parse.apply(res.substring(2 * charsPerChannel, 3 * charsPerChannel));
				alpha = parse.apply(res.substring(3 * charsPerChannel, 4 * charsPerChannel));
				if(channels == 3) alpha = 255;	
				
				switch(channels) {
					case 1 -> twoDColor.gray = (short) red;
					case 2 -> twoDColor.set((short)red , (short) green);
					default -> rgbPicker().color(red / 255f, green / 255f , blue / 255f , alpha / 255f);					
				}
				
		});
		
	}

}