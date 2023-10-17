package cs.csss.editor.ui;

import static cs.core.ui.CSUIConstants.*;
import static cs.csss.ui.utils.UIUtils.toolTip;

import java.util.Arrays;
import java.util.Iterator;

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
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.ui.prefabs.InputBox;

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
	private boolean equals;
	
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
					
					nk_layout_row_dynamic(context , 20 , 1);
					nk_text(context , next.name + ":", TEXT_LEFT|TEXT_CENTERED);
					
					nk_layout_row_dynamic(context , 30 , 15);					
					ColorPixel currentColor = new ChannelBuffer(
						currentColorBuffer[0] , 
						currentColorBuffer[1] , 
						currentColorBuffer[2] , 
						currentColorBuffer[3]
					);
					
					ColorPixel[] pixels;
					if(!equals) pixels = next.generate(currentColor , editor.project().getChannelsPerPixelOfCurrentLayer());
					else pixels = next.get();
					
					for(ColorPixel x : pixels) if(nk_button_color(context , colorForCurrentChannels(x , stack))) editor.setSelectedColor(x);
				
				}
				
				nk_layout_row_dynamic(context , 30 , 2);
				nk_text(context , "Selected Color: " , TEXT_CENTERED|TEXT_LEFT);
				ColorPixel currentColor = editor.selectedColors();
				nk_button_color(context , colorForCurrentChannels(currentColor , stack));	
				
			});
			
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
			CSRadio scriptBrushRadio = scriptBrushRow.new CSRadio(
				"Script Brush" , 
				() -> editor.currentBrush() == Editor.theScriptBrush() , 
				() -> editor.setBrushTo(Editor.theScriptBrush()));
			
			scriptBrushRow.new CSButton("Select" , () -> editor.startSelectSimpleScriptBrush(scriptBrushRadio));
			
			CSDynamicRow modifyingScriptBrushRow = ui.new CSDynamicRow(30);
			CSRadio scriptModifyingBrushRadio = modifyingScriptBrushRow.new CSRadio(
				"Modifying Script Brush" , 
				() -> editor.currentBrush() == Editor.theModifyingScriptBrush() , 
				() -> editor.setBrushTo(Editor.theModifyingScriptBrush()));

			modifyingScriptBrushRow.new CSButton("Select" , () -> editor.startSelectModifyingScriptBrush(scriptModifyingBrushRadio));
			
			CSDynamicRow selectingBrushRow = ui.new CSDynamicRow(30);
			CSRadio scriptSelectingBrushRadio = selectingBrushRow.new CSRadio(
				"Selecting Script Brush" , 
				() -> editor.currentBrush() == Editor.theSelectingScriptBrush() , 
				() -> editor.setBrushTo(Editor.theSelectingScriptBrush()));

			selectingBrushRow.new CSButton("Select" , () -> editor.startSelectSelectingScriptBrush(scriptSelectingBrushRadio));
			
			//this is a slider that lets you modify the brush radius if the brush is a modifying brush
			ui.attachedLayout((context , stack) -> {
							
				if(editor.currentBrush() instanceof CSSSModifyingBrush) {
				
					CSSSModifyingBrush asModifying = (CSSSModifyingBrush) editor.currentBrush();					
					nk_layout_row_dynamic(context , 30 , 1);			 	
					int maxWidth = editor.currentArtboard() != null ? (editor.currentArtboard().height() / 2) - 1 : 999;				
					asModifying.radius(nk_propertyi(context , "Brush Radius" , 1 , asModifying.radius() + 1 , maxWidth , 1 , 1 ) - 1);
					
				}
				
				//set the last frame color value and update the editor if the last frame color doesnt match the current 
				equals = Arrays.equals(previousFrameColorBuffer, currentColorBuffer);
				if(!equals) editor.setSelectedColor(currentColorBuffer[0], currentColorBuffer[1], currentColorBuffer[2], currentColorBuffer[3]);
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
			
			new InputBox(nuklear , "Input " + color + " Value" , .4f , 0.4f , 4 , CSNuklear.DECIMAL_FILTER , res -> {
				
				if(res.length() == 0) return;
				
				int value = Integer.parseInt(res);
				
				if(channels() < 3) { 
					
					if(index == 0) twoDColor.gray = (short)value;
					else twoDColor.alpha = (short)value;
					
				} else {
					
					byte[] colors = colors();
					colors[index] = (byte) value;					
					this.rgbPicker().color(colors[0], colors[1], colors[2], channels() == 4 ? colors[3] : (byte)0xff);
					
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
		
		if(channels() <= 2) {

			twoDColor.gray = (short) Byte.toUnsignedInt(pixel.r());
			if(channels() == 2) twoDColor.alpha = (short) Byte.toUnsignedInt(pixel.g());
			
		} else rgbPicker().color(pixel.r(), pixel.g(), pixel.b(), pixel.a());
		
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

}