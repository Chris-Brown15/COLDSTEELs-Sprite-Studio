package cs.csss.editor.ui;

import static cs.core.utils.CSUtils.require;

import static cs.core.ui.CSUIConstants.*;
import static cs.csss.ui.utils.UIUtils.toolTip;
import java.util.Iterator;
import java.util.function.BooleanSupplier;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_propertyi;
import org.joml.Vector4f;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSButton;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSColorPicker;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.csss.editor.DebugDisabledException;
import cs.csss.editor.Editor;
import cs.csss.editor.brush.CSSSBrush;
import cs.csss.editor.brush.CSSSModifyingBrush;
import cs.csss.engine.Engine;
import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.CSSSProject;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.ui.prefabs.InputBox;
import cs.core.utils.CSUtils;

/**
 * Left hand side panel. This panel contains buttons and UI elements for modifying artboards.
 */
public class LHSPanel {

	private final CSUserInterface ui;
	private final CSNuklear nuklear;
	
	private CSColorPicker color;
	private TwoChannelColorPicker twoDColor; 

	private final Editor editor;
	
	/**
	 * Creates a left hand side panel.
	 * 
	 * @param editor � the editor
	 * @param nuklear � Nuklear factory
	 */
	public LHSPanel(Editor editor , CSNuklear nuklear) {

		this.editor = editor;
		this.nuklear = nuklear;
		ui = nuklear.new CSUserInterface("Editor" , 0.001f , -1f , 0.199f , .90f);
		ui.setDimensions(ui.xPosition() , 77 , ui.interfaceWidth() , ui.interfaceHeight());
		ui.options = UI_BORDERED|UI_TITLED;
		
		Engine.THE_TEMPORAL.onTrue(() -> editor.project() != null , () -> {

			CSDynamicRow threeDColorRow = ui.new CSDynamicRow(200);
			color = threeDColorRow.new CSColorPicker(editor.project().channelsPerPixel() == 4 ? RGBA : RGB);
			threeDColorRow.doLayout = () -> channels() >= 3;			
			
			twoDColor = new TwoChannelColorPicker(nuklear.context());
			twoDColor.doLayout = () -> channels() <= 2;
			
			ui.attachedLayout((context , stack) -> {
				
				twoDColor.hasAlpha = channels() == 2;
				twoDColor.layout();
				
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
				
			});
				
			colorInput("Gray" , 0 , () -> channels() >= 1 && channels() <= 2);
			colorInput("Alpha" , 1 , () -> channels() == 2);
				
			colorInput("Red" , 0 , () -> channels() >= 3);
			colorInput("Green" , 1 , () -> channels() >= 3);
			colorInput("Blue" , 2 , () -> channels() >= 3);
			colorInput("Alpha" , 3 , () -> channels() == 4);
	
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
	
	private void colorInput(final String color , int index , BooleanSupplier doLayout) {
		
		CSDynamicRow row = ui.new CSDynamicRow();
		
		row.doLayout = doLayout;
		
		CSButton button = row.new CSButton("Input " + color , () -> {
			
			new InputBox(nuklear , "Input " + color + " Value As Hex" , .4f , 0.4f , 9 , CSNuklear.HEX_FILTER , res -> {
				
				if(res.length() == 0) return;
				
				int value = CSUtils.parseHexInt(res, 0 , res.length());
				
				byte[] colors = colors();
				colors[index] = (byte) value;
				this.color.color(colors[0] , colors[1] , colors[2] , colors[3]);
				
			});
			
		});
		
		if(!color.equals("Alpha")) toolTip(button , "Input a " + color + " value directly, in hex.");
		else toolTip(button , "Input an " + color + " value directly, in hex.");					
		
	}
	
	/**
	 * Creates and returns an array of colors.
	 * 
	 * @param colors � destination for color values
	 * @return {@code colors} after writing.
	 */
	public byte[] colors(byte[] colors) {
		
		require(colors.length == channels());
		
		if(channels() > 2) {
			
			Vector4f colorVector = color.color();
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
	 * @param pixel � a new color  
	 */
	public void setColor(PalettePixel pixel) {
		
		if(channels() <= 2) {

			twoDColor.gray = (short) Byte.toUnsignedInt(pixel.red());
			if(channels() == 2) twoDColor.alpha = (short) Byte.toUnsignedInt(pixel.green());
			
		} else color.color(pixel.red(), pixel.green(), pixel.blue(), pixel.alpha());
		
	}
	
	private String radioTextFromBrushName(CSSSBrush brush) {

		return brush.getClass().getSimpleName().replace("Brush", "").replace('_', ' ');
		
	}
	
	private int channels() {
		
		return editor.project().getChannelsPerPixelOfCurrentLayer();
		
	}

}