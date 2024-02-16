package cs.csss.misc.ui;

import static org.lwjgl.opengl.GL30C.glClearColor;

import static cs.core.ui.CSUIConstants.*;

import static cs.csss.misc.utils.SCBits.*;

import static org.lwjgl.nuklear.Nuklear.*;

import static org.lwjgl.system.MemoryUtil.memCallocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memCalloc;
import static org.lwjgl.system.MemoryUtil.memCallocInt;
import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkColorf;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkPluginFilterI;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkStyle;
import org.lwjgl.nuklear.NkStyleButton;
import org.lwjgl.nuklear.NkStyleChart;
import org.lwjgl.nuklear.NkStyleCombo;
import org.lwjgl.nuklear.NkStyleEdit;
import org.lwjgl.nuklear.NkStyleItem;
import org.lwjgl.nuklear.NkStyleItemData;
import org.lwjgl.nuklear.NkStyleProgress;
import org.lwjgl.nuklear.NkStyleProperty;
import org.lwjgl.nuklear.NkStyleScrollbar;
import org.lwjgl.nuklear.NkStyleSelectable;
import org.lwjgl.nuklear.NkStyleSlider;
import org.lwjgl.nuklear.NkStyleToggle;
import org.lwjgl.nuklear.NkStyleWindow;
import org.lwjgl.nuklear.NkStyleWindowHeader;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.CSUtils;
import cs.core.utils.ShutDown;
import cs.csss.editor.Editor;
import cs.csss.engine.UITheme;
import cs.csss.ui.menus.Dialogue;

public class UICustomizer extends Dialogue implements ShutDown {

	private static boolean isOpen = false;
	private static UICustomizer openCustomizer = null;
	
	/**
	 * Used to free the UI Customizer if it is open currently.
	 */
	public static void finalShutDown() {
		
		if(openCustomizer != null) openCustomizer.shutDown();
		
	}
	
	private final Editor editor;
	
	private final CSNuklear nuklear;
	private final CSUserInterface modifierUI;
	private final SetStyleBeforeBeginUI viewChangesUI;
	
	private boolean isFreed = false;
	
	private FloatBuffer sliderValue = memCallocFloat(1);
	private long progressValue = 50;
	private FloatBuffer propertyValue = memCallocFloat(1);
	
	private ByteBuffer stringEditorMemory = memCalloc(1024);
	private IntBuffer stringEditorMemoryLength = memCallocInt(1);

	private final NkStyle originalStyle = NkStyle.malloc();
	private NkStyle customStyle = NkStyle.malloc();
	
	private NkColorf colorPicker = NkColorf.malloc();
	private NkColor windowColor;// = NkColor.malloc().set((byte)(255 * .15f) , (byte)(255 * .15f) , (byte)(255 * .15f) , (byte)-1);
	
	private ByteBuffer redChannelText = memCalloc(4);
	private IntBuffer redChannelTextLength = memCallocInt(1);

	private ByteBuffer greenChannelText = memCalloc(4);
	private IntBuffer greenChannelTextLength = memCallocInt(1);

	private ByteBuffer blueChannelText = memCalloc(4);
	private IntBuffer blueChannelTextLength = memCallocInt(1);

	private ByteBuffer alphaChannelText = memCalloc(4);
	private IntBuffer alphaChannelTextLength = memCallocInt(1);
	
	private LinkedList<NkColor> UIPaletteColors;
	
	private boolean finished = false;
	private boolean showingWindowBackgroundColor = false;
	private ByteBuffer radioButton = memCalloc(1);
	private ByteBuffer checkbox = memCalloc(1);
	
	public UICustomizer(CSNuklear nuklear , Editor editor , LinkedList<NkColor> uiPaletteColors , NkColor windowBackgroundDefaultColor) {

		if(isOpen) throw new IllegalStateException("Cannot have more than one styler open at a time.");
		isOpen = true;
		openCustomizer = this;
		
		this.editor = Objects.requireNonNull(editor);
		this.nuklear = Objects.requireNonNull(nuklear);
		
		originalStyle.set(nuklear.context().style());		
		customStyle.set(originalStyle);
		
		UIPaletteColors = copyList(uiPaletteColors); 
		
		this.windowColor = NkColor.malloc();
		if(windowBackgroundDefaultColor != null) this.windowColor.set(windowBackgroundDefaultColor);
		else windowColor.set((byte)(255 * .15f) , (byte)(255 * .15f) , (byte)(255 * .15f) , (byte)-1);
		
		modifierUI = nuklear.new CSUserInterface("UI Customizer" , 550 , 80 , 400 , 730);
		modifierUI.options = UI_TITLED|UI_BORDERED;
		modifierUI.setDimensions(550 , 80 , 400 , 730);
				
		viewChangesUI = new SetStyleBeforeBeginUI(nuklear , "Change Viewer" , 955 , 80 , 400 , 730 , customStyle);
		viewChangesUI.setDimensions(955 , 80 , 400 , 730);
		viewChangesUI.options = UI_TITLED|UI_BORDERED;

		modifierUI.attachedLayout((context , stack) -> {
			
			nk_layout_row_dynamic(context , 40 , 1);
			nk_text(context , "UI Color Palette Creator" , TEXT_MIDDLE|TEXT_CENTERED);
			
			nk_layout_row_dynamic(context , 100 , 1);
			nk_color_pick(context , colorPicker , NK_RGBA);
			
			NkPluginFilterI filter = editor.colorInputsAreHex() ? CSNuklear.HEX_FILTER : CSNuklear.DECIMAL_FILTER;
			
			nk_layout_row_begin(context , NK_DYNAMIC , 30 , 2);
			
			nk_layout_row_push(context , 0.3f);
			nk_text(context , "Red Channel" , TEXT_LEFT|TEXT_MIDDLE);			
			nk_layout_row_push(context , 0.7f);			
			nk_edit_string(context , EDIT_FIELD , redChannelText , redChannelTextLength , 4 , filter);		
			nk_layout_row_end(context);
			
			nk_layout_row_begin(context , NK_DYNAMIC , 30 , 2);			
			nk_layout_row_push(context , 0.3f);
			nk_text(context , "Green Channel" , TEXT_LEFT|TEXT_MIDDLE);			
			nk_layout_row_push(context , 0.7f);			
			nk_edit_string(context , EDIT_FIELD , greenChannelText , greenChannelTextLength , 4 , filter);		
			nk_layout_row_end(context);
			
			nk_layout_row_begin(context , NK_DYNAMIC , 30 , 2);			
			nk_layout_row_push(context , 0.3f);
			nk_text(context , "Blue Channel" , TEXT_LEFT|TEXT_MIDDLE);			
			nk_layout_row_push(context , 0.7f);			
			nk_edit_string(context , EDIT_FIELD , blueChannelText , blueChannelTextLength , 4 , filter);		
			nk_layout_row_end(context);
			
			nk_layout_row_begin(context , NK_DYNAMIC , 30 , 2);			
			nk_layout_row_push(context , 0.3f);
			nk_text(context , "Alpha Channel" , TEXT_LEFT|TEXT_MIDDLE);			
			nk_layout_row_push(context , 0.7f);			
			nk_edit_string(context , EDIT_FIELD , alphaChannelText , alphaChannelTextLength , 4 , filter);		
			nk_layout_row_end(context);
				
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_text(context , "Add Inputs to UI Color Palette")) {
				
				if(
					redChannelTextLength.get(0) > 0 && 
					greenChannelTextLength.get(0) > 0 && 
					blueChannelTextLength.get(0) > 0 && 
					alphaChannelTextLength.get(0) > 0
				) { 
					
					Function<String , Integer> parser = editor.colorInputsAreHex() ? 
						string -> CSUtils.parseHexInt(string, 0, string.length()) : 
						Integer::parseInt;
					
					addColorToPalette(
						parser.apply(memUTF8(redChannelText , redChannelTextLength.get(0))).byteValue(), 
						parser.apply(memUTF8(greenChannelText , greenChannelTextLength.get(0))).byteValue(), 
						parser.apply(memUTF8(blueChannelText , blueChannelTextLength.get(0))).byteValue(), 
						parser.apply(memUTF8(alphaChannelText , alphaChannelTextLength.get(0))).byteValue()
					);
				
				}
				
			}
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_button_text(context , "Add Picker Color to UI Color Palette")) {
			
				addColorToPalette(
					(byte)(colorPicker.r() * 255) ,
					(byte)(colorPicker.g() * 255) ,
					(byte)(colorPicker.b() * 255) ,
					(byte)(colorPicker.a() * 255)  
				);
				
			}				
			
			NkColor pressed = layoutUIPalette(context , null);
			if(pressed != null) {
				  
				UIPaletteColors.remove(pressed);
				pressed.free();
				
			}
			
//			/* WINDOW COLOR AND FONT */
			/* WINDOW BACKGROUND */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Window Background" , NkVec2.malloc(stack).set(modifierUI.interfaceWidth() , 300))) {
				
				boolean changed = colorPick(
					context, 
					windowColor, 
					NkColor.malloc(stack).set((byte)(255 * 0.15f) , (byte)(255 * 0.15f) , (byte)(255 * 0.15f) , (byte)(255 * 1.0f)) ,
					"Window Background Color"
				);
				
				if(changed && showingWindowBackgroundColor) setClearColorToSelected();
									
				nk_combo_end(context);
				
			}
			
			/* TEXT STYLE */
			
			nk_layout_row_dynamic(context , 30 , 1);			
			if(nk_combo_begin_text(context , "Text" , NkVec2.malloc(stack).set(modifierUI.interfaceWidth() , 300))) {
				
				colorPick(context , customStyle.text().color() , originalStyle.text().color() , "Text Color");	
				
				nk_layout_row_dynamic(context , 30 , 1);
				vec2(context , customStyle.text().padding() , "Font X Padding" , "Font Y Padding");

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) customStyle.text().set(originalStyle.text());
				
				nk_combo_end(context);
				
			}
			
			/* BUTTON STYLE */

			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Button" , NkVec2.malloc(stack).set(modifierUI.interfaceWidth() , 300))) {
				
				buttonDesigner(context, customStyle.button(), originalStyle.button());

				nk_combo_end(context);
				
			}
			
			/* MENU BUTTON STYLE */

			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Menu Button" , NkVec2.malloc(stack).set(modifierUI.interfaceWidth() , 300))) {
				
				buttonDesigner(context , customStyle.menu_button() , originalStyle.menu_button());
				
				nk_combo_end(context);
				
			}
			
			/* TOGGLE */
			
			toggleDesigner(context , customStyle.option() , originalStyle.option() , "Toggle");

			/* CHECKBOX */
			
			toggleDesigner(context , customStyle.checkbox() , originalStyle.checkbox() , "Checkbox" );
			
			/* SELECTABLE */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Selectable" , NkVec2.malloc(stack).set(modifierUI.interfaceWidth() , 300))) {
								
				NkStyleSelectable selectableStyle = customStyle.selectable();
				NkStyleSelectable original = originalStyle.selectable();
				styleItem(context , selectableStyle.normal() 		 , original.normal() 				, "Normal");
				styleItem(context , selectableStyle.hover() 		 , original.hover() 				, "Hover");
				styleItem(context , selectableStyle.pressed() 		 , original.pressed() 				, "Pressed");
				styleItem(context , selectableStyle.normal_active()  , original.normal_active() 		, "Active Normal");
				styleItem(context , selectableStyle.hover_active() 	 , original.hover_active() 			, "Active Hover");
				styleItem(context , selectableStyle.pressed_active() , original.pressed_active() 	, "Active Pressed");
				
				colorPick(context , selectableStyle.text_normal() , original.text_normal() , "Text Normal");
				colorPick(context , selectableStyle.text_hover() , original.text_hover() , "Text Hover"); 
		
				colorPick(context, selectableStyle.text_pressed() , original.text_pressed() , "Text Pressed");     
				colorPick(context, selectableStyle.text_normal_active() , original.text_normal_active() , "Text Active Normal");
	
				colorPick(context, selectableStyle.text_hover_active() , original.text_hover_active() , "Text Active Hover");  
				colorPick(context, selectableStyle.text_pressed_active() , original.text_pressed_active() , "Text Active Pressed");		
				
				selectableStyle.text_alignment(textFlags(context, selectableStyle.text_alignment()));
				selectableStyle.rounding(setFloat(context, selectableStyle.rounding(), "Rounding"));
				
				vec2(context, selectableStyle.padding(), "X Padding", "Y Padding");
				vec2(context, selectableStyle.touch_padding(), "X Touch Padding", "Y Touch Padding");
				vec2(context, selectableStyle.image_padding(), "X Image Padding", "Y Image Padding");
				
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) selectableStyle.set(original);
				
				nk_combo_end(context);
				
			}
			
			/* SLIDER */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Slider" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				NkStyleSlider sliderStyle = customStyle.slider();
				NkStyleSlider original = originalStyle.slider();
				
				styleItem(context , sliderStyle.normal() , original.normal() , "Normal");
				styleItem(context , sliderStyle.hover() , original.hover() , "Hover");
				styleItem(context , sliderStyle.active() , original.active() , "Active");
					
				colorPick(context, sliderStyle.border_color() , original.border_color() , "Border Color");    
				colorPick(context, sliderStyle.bar_normal() , original.bar_normal() , "Bar Normal"); 
				
				colorPick(context, sliderStyle.bar_hover() , original.bar_hover() , "Bar Hovered"); 
				colorPick(context, sliderStyle.bar_active() , original.bar_active() , "Bar Active");
				colorPick(context , sliderStyle.bar_filled() , original.bar_filled() , "Bar Filled");
				
				styleItem(context , sliderStyle.cursor_normal() , original.cursor_normal() , "Cursor Normal");
				styleItem(context , sliderStyle.cursor_hover() , original.cursor_hover() , "Cursor Hover");
				styleItem(context , sliderStyle.cursor_active() , original.cursor_active() , "Cursor Active");
				
				sliderStyle.border(setFloat(context, sliderStyle.border(), "Border"));
				sliderStyle.rounding(setFloat(context, sliderStyle.rounding(), "Rounding"));
				sliderStyle.bar_height(setFloat(context, sliderStyle.bar_height(), "Bar Height"));
				
				vec2(context , sliderStyle.padding() , "X Padding" , "Y Padding");
				vec2(context , sliderStyle.spacing() , "X Spacing" , "Y Spacing");
				vec2(context , sliderStyle.cursor_size() , "Cursor Radius" , "UNUSED");
				
				sliderStyle.show_buttons(setInt(context , "Show Buttons" , sliderStyle.show_buttons()));
				
				nk_layout_row_dynamic(context , 40 , 1);
				nk_text(context , "Increase Button" , TEXT_CENTERED|TEXT_MIDDLE);
				
				buttonDesigner(context, sliderStyle.inc_button(), original.inc_button());

				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Decrease Button" , TEXT_CENTERED|TEXT_MIDDLE);
				
				buttonDesigner(context, sliderStyle.dec_button(), original.dec_button());
								
				sliderStyle.inc_symbol(symbol(context , "Increase Symbol" , sliderStyle.inc_symbol()));
				sliderStyle.dec_symbol(symbol(context , "Decrease Symbol" , sliderStyle.dec_symbol()));

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) sliderStyle.set(original);
				
				nk_combo_end(context);
				
			}
			
			/* PROGRESS */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Progress" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				progressDesigner(context , customStyle.progress() , originalStyle.progress());
				
				nk_combo_end(context);
				
			}

			/* PROPERTY */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Property" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				NkStyleProperty property = customStyle.property();
				NkStyleProperty original = originalStyle.property();
				styleItem(context , property.normal()  , original.normal(), "Normal");
				styleItem(context , property.hover()   , original.hover() , "Hover");
				styleItem(context , property.active()  , original.active(), "Active");
				
				colorPick(context , property.border_color() , original.border_color() , "Property Border");      
				colorPick(context , property.label_normal() , original.label_normal() , "Property Label Normal"); 

				colorPick(context , property.label_hover() , original.label_hover() , "Property Label Hover"); 
				colorPick(context , property.label_active() , original.label_active() , "Property Label Active");
				
				property.sym_left(symbol(context , "Left Symbol" , property.sym_left()));
				property.sym_right(symbol(context , "Right Symbol" , property.sym_right()));
				
				property.border(setFloat(context , property.border() , "Border"));
				property.rounding(setFloat(context , property.rounding() , "Rounding"));
				
				vec2(context , property.padding() , "X Padding" , "Y Padding");
				
				nk_layout_row_dynamic(context , 40 , 1);
				nk_text(context , "Text Input" , TEXT_CENTERED|TEXT_MIDDLE);
				
				editDesigner(context , property.edit() , original.edit() , false);
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Increase Button" , TEXT_MIDDLE);				
				buttonDesigner(context, property.inc_button() , original.inc_button());

				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Decrease Button" , TEXT_MIDDLE);	
				buttonDesigner(context, property.dec_button(), original.dec_button());

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) property.set(original);
				
				nk_combo_end(context);
				
			}
			
			/* TEXT EDITOR */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Text Editor" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				editDesigner(context , customStyle.edit() , originalStyle.edit() , true);				
				nk_combo_end(context);
				
			}
			
			/* CHART */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Chart" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				NkStyleChart chart = customStyle.chart();
				NkStyleChart original = originalStyle.chart();
				
				styleItem(context , chart.background() , original.background() , "Background");				
				colorPick(context , chart.border_color() , original.border_color() , "Border");				
				colorPick(context , chart.selected_color() , original.selected_color() , "Selected Color");				
				colorPick(context , chart.color() , original.color() , "Color");
				
				chart.border(setFloat(context , chart.border() , "Border"));
				chart.rounding(setFloat(context , chart.rounding() , "Rounding"));
				
				vec2(context , chart.padding() , "X Padding" , "Y Padding");

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) chart.set(original);
					
				nk_combo_end(context);
				
			}
			
			/* HORIZONTAL SCROLL */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Horizontal Scroll" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				NkStyleScrollbar horizontal = customStyle.scrollh();
				scrollbarDesigner(context , horizontal , originalStyle.scrollh());
				
				nk_combo_end(context);
				
			}

			/* VERTICAL SCROLL */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Vertical Scroll" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				NkStyleScrollbar vertical = customStyle.scrollv();
				scrollbarDesigner(context , vertical , originalStyle.scrollv());
				
				nk_combo_end(context);
				
			}
			
			/* TAB */
			//I Dont know what Element this applies to
//			nk_layout_row_dynamic(context , 30 , 1);
//			if(nk_combo_begin_text(context , "Tab" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
//				
//				NkStyleTab tab = customStyle.tab();
//				NkStyleTab original = originalStyle.tab();
//				
//				styleItem(context , tab.background() , original.background() , "Background");				
//				colorPick(context , tab.border_color() , original.border_color() , "Border");
//				
//				colorPick(context , tab.text() , original.text() , "Text");
//				
//				nk_layout_row_dynamic(context , 40 , 1);
//				nk_text(context , "Maxmimize Button" , TEXT_MIDDLE|TEXT_CENTERED);
//				
//				buttonDesigner(context, tab.tab_maximize_button(), original.tab_maximize_button());
//
//				nk_layout_row_dynamic(context , 40 , 1);
//				nk_text(context , "Minimize Button" , TEXT_MIDDLE|TEXT_CENTERED);
//				
//				buttonDesigner(context, tab.tab_minimize_button(), original.tab_minimize_button());
//
//				nk_layout_row_dynamic(context , 40 , 1);
//				nk_text(context , "Node Maxmimize Button" , TEXT_MIDDLE|TEXT_CENTERED);
//				
//				buttonDesigner(context, tab.node_maximize_button(), original.node_maximize_button());
//
//				nk_layout_row_dynamic(context , 40 , 1);
//				nk_text(context , "Node Minimize Button" , TEXT_MIDDLE|TEXT_CENTERED);
//				
//				buttonDesigner(context, tab.node_minimize_button(), original.node_minimize_button());
//				
//				tab.sym_maximize(symbol(context , "Maximize Symbol" , tab.sym_maximize()));
//				tab.sym_minimize(symbol(context , "Minimize Symbol" , tab.sym_minimize()));
//				
//				tab.border(setFloat(context , tab.border() , "Border"));
//				tab.rounding(setFloat(context , tab.rounding() , "Rounding"));
//				tab.indent(setFloat(context , tab.indent() , "Indent"));
//				
//				vec2(context , tab.padding() , "X Padding" , "Y Padding");
//				vec2(context , tab.spacing() , "X Spacing" , "Y Spacing");
//
//				nk_layout_row_dynamic(context , 30 , 1);
//				if(nk_button_text(context , "Reset to Defaults")) tab.set(original);
//			
//				nk_combo_end(context);
//				
//			}
			
			/* COMBO */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Combo" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				NkStyleCombo combo = customStyle.combo();
				NkStyleCombo original = originalStyle.combo();
				
				styleItem(context , combo.normal()  , original.normal(), "Normal");
				styleItem(context , combo.hover()   , original.hover() , "Hover");
				styleItem(context , combo.active()  , original.active(), "Active");			
				
				colorPick(context , combo.label_normal()  , original.label_normal()  , "Label Normal");
				colorPick(context , combo.label_hover()   , original.label_hover()   , "Label Hover");
				colorPick(context , combo.label_active()  , original.label_active()  , "Label Active");
				colorPick(context , combo.symbol_normal() , original.symbol_normal() , "Symbol Normal");
				colorPick(context , combo.symbol_hover()  , original.symbol_hover()  , "Symbol Hover");
				colorPick(context , combo.symbol_active() , original.symbol_active() , "Symbol Active");
				
				nk_layout_row_dynamic(context , 40 , 1);
				nk_text(context , "Combo Button" , TEXT_MIDDLE|TEXT_CENTERED);
				
				buttonDesigner(context, combo.button(), original.button());
				
				combo.sym_normal(symbol(context , "Symbol Normal" , combo.sym_normal()));
				combo.sym_hover(symbol(context , "Symbol Hover" , combo.sym_hover()));
				combo.sym_active(symbol(context , "Symbol Active" , combo.sym_active()));
				
				combo.border(setFloat(context , combo.border() , "Border"));
				combo.rounding(setFloat(context , combo.rounding() , "Rounding"));
				
				vec2(context , combo.content_padding() , "X Content Padding" , "Y Content Padding");
				vec2(context , combo.button_padding() , "X Button Padding" , "Y Button Padding");
				vec2(context , combo.spacing() , "X Spacing" , "Y Spacing");

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) combo.set(original);
			
				nk_combo_end(context);
				
			}

			/* WINDOW */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_text(context , "Window" , nk_vec2(modifierUI.interfaceWidth() , 300 , NkVec2.malloc(stack)))) {
				
				NkStyleWindow window = customStyle.window();
				NkStyleWindow original = originalStyle.window();
				NkStyleWindowHeader header = window.header();
				NkStyleWindowHeader originalHeader = original.header();
				
				styleItem(context , header.normal()  , originalHeader.normal(), "Window Header Normal");
				styleItem(context , header.hover()   , originalHeader.hover() , "Window Header Hover");
				styleItem(context , header.active()  , originalHeader.active(), "Window Header Active");
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Close Button" , TEXT_MIDDLE);
				buttonDesigner(context, header.close_button(), originalHeader.close_button());

				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Minimize Button" , TEXT_MIDDLE);
				buttonDesigner(context, header.minimize_button(), originalHeader.minimize_button());
				
				header.close_symbol(symbol(context , "Close Symbol" , header.close_symbol()));			
				header.minimize_symbol(symbol(context , "Minimize Symbol" , header.minimize_symbol()));
				header.maximize_symbol(symbol(context , "Maximize Symbol" , header.maximize_symbol()));
				
				colorPick(context , header.label_normal() , originalHeader.label_normal() , "Text Normal");
				colorPick(context , header.label_hover()  , originalHeader.label_hover()  , "Text Hover" );
				colorPick(context , header.label_active() , originalHeader.label_active() , "Text Active");
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text(context , "Header Align" , TEXT_MIDDLE);
				nk_layout_row_dynamic(context , 30 , 2);
				if(nk_checkbox_text(context , "Header Left" , stack.bytes(header.align() == NK_HEADER_LEFT ? (byte)1 : 0))) {
					
					header.align(NK_HEADER_LEFT);
					
				}

				if(nk_checkbox_text(context , "Header Right" , stack.bytes(header.align() == NK_HEADER_RIGHT ? (byte)1 : 0))) {
					
					header.align(NK_HEADER_RIGHT);
					
				}
				
				vec2(context , header.padding() , "Header X Padding" , "Header Y Padding");
				vec2(context , header.label_padding() , "Header X Text Padding" , "Header Y Text Padding");
				vec2(context , header.spacing() , "Header X Spacing" , "Header Y Spacing");
				
				styleItem(context , window.fixed_background() , original.fixed_background() , "Fixed Background");
				
				colorPick(context , window.background() , original.background() , "Background"); 
				colorPick(context , window.border_color() , original.border_color() , "Border"); 
			
				colorPick(context , window.popup_border_color() , original.popup_border_color() , "Popup Border"); 
				colorPick(context , window.combo_border_color() , original.combo_border_color() , "Combo Border"); 

				colorPick(context , window.contextual_border_color() , original.contextual_border_color() , "Contextual Border"); 
				colorPick(context , window.menu_border_color() , original.menu_border_color() , "Menu Border");       

				colorPick(context , window.group_border_color() , original.group_border_color() , "Group Border");  
				colorPick(context , window.tooltip_border_color() , original.tooltip_border_color() , "Tooltip Border");

				styleItem(context , window.scaler() , original.scaler() , "Scale Tool");
				
				window.border(setFloat(context , window.border() , "Window Border"));
				window.combo_border(setFloat(context , window.combo_border() , "Combo Border"));
				window.contextual_border(setFloat(context , window.contextual_border() , "Contextual Border"));
				window.menu_border(setFloat(context , window.menu_border() , "Menu Border"));
				window.group_border(setFloat(context , window.group_border() , "Group Border"));
				window.tooltip_border(setFloat(context , window.tooltip_border() , "Tooltip Border"));
				window.popup_border(setFloat(context , window.popup_border() , "Popup Border"));
				window.min_row_height_padding(setFloat(context , window.min_row_height_padding() , "Minimum Row Height Padding"));
				window.rounding(setFloat(context , window.rounding() , "Rounding"));
				
				vec2(context , window.spacing() , "X Spacing" , "Y Spacing");
				vec2(context , window.scrollbar_size() , "X Scrollbar Size" , "Y Scrollbar Size");
				vec2(context , window.min_size() , "X Minimum Size" , "Y Minimum Size");
				vec2(context , window.padding() , "X Padding" , "Y Padding");
				vec2(context , window.group_padding() , "X Group Padding" , "Y Group Padding");
				vec2(context , window.popup_padding() , "X Popup Padding" , "Y Popup Padding");
				vec2(context , window.combo_padding() , "X Combo Padding" , "Y Combo Padding");
				vec2(context , window.contextual_padding() , "X Contextual Padding" , "Y Contextual Padding");
				vec2(context , window.menu_padding() , "X Menu Padding" , "Y Menu Padding");
				vec2(context , window.tooltip_padding() , "X Tooltip Padding" , "Y Tooltip Padding");

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) window.set(original);
			
				nk_combo_end(context);
				
			}

		});
		
		viewChangesUI.attachedLayout((context , stack) -> {

			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_checkbox_text(context , "Preview Window Background Color" , stack.bytes(showingWindowBackgroundColor ? (byte) 1 : 0))) {

				showingWindowBackgroundColor = !showingWindowBackgroundColor;
				
				if(showingWindowBackgroundColor) setClearColorToSelected();
				else editor.rendererPost(() -> glClearColor(.15f , .15f , .15f , 1.0f));
				
			}
			
			nk_layout_row_dynamic(context , 24 , 1);
			nk_text_wrap(context , "Lorem Ipsum Dolor Sit Amet.");

			nk_layout_row_dynamic(context , 30 , 1);
			nk_button_text(context , "Lorem Ipsum Dolor Sit Amet.");
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_contextual_begin(
				context , 
				UI_TITLED|UI_BORDERED , 
				nk_vec2(1000, 1000, NkVec2.malloc(stack)) , 
				nk_rect(0, 0, 100, 100, NkRect.malloc(stack)))
			) {
	
				nk_layout_row_dynamic(context , 30 , 1);
				nk_contextual_item_text(context , "Lorem Ipsum Dolor Sit Amet." , TEXT_MIDDLE);
				
				nk_contextual_end(context);
				
			}

			nk_layout_row_dynamic(context , 30 , 1);
			nk_radio_text(context , "Lorem Ipsum Dolor Sit Amet." , radioButton);

			nk_layout_row_dynamic(context , 30 , 1);
			nk_checkbox_text(context , "Lorem Ipsum Dolor Sit Amet." , checkbox);
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_selectable_text(context , "Lorem Ipsum Dolor Sit Amet." , TEXT_LEFT , stack.bytes((byte)0));
			
			nk_layout_row_dynamic(context , 30 , 1);
			nk_slider_float(context , 0f , sliderValue , 100f , 1.0f);

			nk_layout_row_dynamic(context , 30 , 1);
			progressValue =  nk_prog(context , progressValue , 100 , false);

			nk_layout_row_dynamic(context , 30 , 1);
			nk_property_float(context , "Lorem Ipsum Dolor Sit Amet." , 0F , propertyValue , 100f , 1.0f , 2.0f);

			nk_layout_row_dynamic(context , 60 , 1);
			nk_edit_string(context , EDIT_FIELD|EDIT_MULTILINE , stringEditorMemory , stringEditorMemoryLength , 1024 , CSNuklear.NO_FILTER);

			nk_layout_row_dynamic(context , 60 , 1);
			nk_chart_begin(context , CHART_COLUMNS , 7 , 0f , 30f);
			
			nk_chart_push(context , 5f);
			nk_chart_push(context , 12f);
			nk_chart_push(context , 1f);
			nk_chart_push(context , 20f);
			nk_chart_push(context , 6f);
			nk_chart_push(context , 17f);
			nk_chart_push(context , 30f);
			
			nk_chart_end(context);
			
			nk_layout_row_dynamic(context , 120 , 1);
			if(nk_group_begin(context , "Sliders" , UI_TITLED)) {
				
				nk_layout_row_begin(context , STATIC , 300 , 1);
				nk_layout_row_push(context , 1000);
				nk_spacer(context);
				nk_layout_row_end(context);
				nk_group_end(context);
				
			}

//			nk_layout_row_dynamic(context , 60 , 1);
//			if(nk_group_begin(context , "Group Tab" , UI_TITLED|UI_BORDERED)) nk_group_end(context);
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_combo_begin_symbol_text(context , "Combo" , SYMBOL_TRIANGLE_DOWN , nk_vec2(300 , 200 , NkVec2.malloc(stack)))) {
			
				nk_layout_row_dynamic(context , 30 , 1);
				nk_combo_item_text(context , "Option 1" , TEXT_MIDDLE);
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_combo_item_symbol_text(context , SYMBOL_CIRCLE_OUTLINE , "Option 2" , TEXT_MIDDLE);
				
				nk_combo_end(context);
				
			}
			
			nk_layout_row_dynamic(context , 30 , 2);
			nk_menubar_begin(context);
			
			if(nk_menu_begin_text(context , "Menu Option" , TEXT_LEFT , NkVec2.malloc(stack).set(200 , 200))) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "LOREM IPSUM");
				nk_menu_end(context);
				
			}

			if(nk_menu_begin_text(context , "Menu Option 2" , TEXT_LEFT , NkVec2.malloc(stack).set(200 , 200))) {
				
				nk_layout_row_dynamic(context , 30 , 1);
				nk_text_wrap(context , "LOREM IPSUM");
				nk_menu_end(context);
				
			}
			
			nk_menubar_end(context);
						
			context.style().set(originalStyle);

		});
				
		CSDynamicRow finishRow = modifierUI.new CSDynamicRow();
		finishRow.new CSButton("Finish" , () -> {
			
			onFinish();
			finished = true;
			isOpen = false;
			openCustomizer = null;
			
		});
		
		finishRow.new CSButton("Cancel" , () -> {
			
			shutDown();
			onFinish();
			finished = true;
			isOpen = false;
			openCustomizer = null;			
			customStyle = null;
			
		});

	}

	public UITheme resultingTheme() {
		
		if(customStyle == null) return null;
		NkStyle styleCopy = NkStyle.malloc().set(customStyle);
		LinkedList<NkColor> paletteCopy = copyList(UIPaletteColors);
		NkColor windowColorCopy = NkColor.malloc().set(windowColor.r() , windowColor.g() , windowColor.b() , windowColor.a());
		return new UITheme(null , styleCopy , paletteCopy , windowColorCopy);
		
	}
	
	private LinkedList<NkColor> copyList(LinkedList<NkColor> source) {
		
		LinkedList<NkColor> destination = new LinkedList<>();
	 	if(source != null) for(NkColor x : source) destination.add(NkColor.malloc().set(x.r() , x.g() , x.b() , x.a()));	
		return destination;
	 	
	}
	
	private void buttonDesigner(NkContext context , NkStyleButton button , NkStyleButton original) {
		
		styleItem(context , button.normal() , original.normal() , "Normal");
		styleItem(context , button.hover() , original.hover() , "Hover");
		styleItem(context , button.active() , original.active() , "Active");
		
		colorPick(context , button.border_color() 	 , original.border_color() 		, "Button Border Color");
		colorPick(context , button.text_background() , original.text_background() 	, "Button Text Background Color");	
		colorPick(context , button.text_normal() 	 , original.text_normal() 		, "Button Text Normal Color");	
		colorPick(context , button.text_hover() 	 , original.text_hover() 		, "Button Text Hover Color");		
		colorPick(context , button.text_active() 	 , original.text_active() 		, "Button Text Active Color");		
		
		button.text_alignment(textFlags(context , button.text_alignment()));		
		button.border(setFloat(context , button.border() , "Border"));		
		button.rounding(setFloat(context , button.rounding() , "Rounding"));
		
		nk_layout_row_dynamic(context , 30 , 1);
		vec2(context, button.padding(), "X Padding", "Y Padding");
						
		nk_layout_row_dynamic(context , 30 , 1);
		vec2(context , button.touch_padding() , "X Touch Padding" , "Y Touch Padding");				
		
		nk_layout_row_dynamic(context , 30 , 1);
		if(nk_button_text(context , "Reset to Defaults")) button.set(original);
		
	}
	
	private void toggleDesigner(NkContext context , NkStyleToggle toggle , NkStyleToggle original , String dropDownText) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {

			if(nk_combo_begin_text(context , dropDownText , NkVec2.malloc(stack).set(modifierUI.interfaceWidth() , 300))) {

				styleItem(context, toggle.normal()		, original.normal()		, "Normal");				
				styleItem(context, toggle.hover()		, original.hover()		, "Hover");
				styleItem(context, toggle.active()		, original.active()		, "Active");
				colorPick(context, toggle.border_color()	, original.border_color()	, "Border");						
				
				styleItem(context, toggle.cursor_normal() , original.cursor_normal() , "Cursor Normal");
				styleItem(context, toggle.cursor_hover() , original.cursor_hover() , "Cursor Hover");
				
				colorPick(context, toggle.text_normal()	, original.text_normal(), "Text Normal");
				colorPick(context, toggle.text_hover()	, original.text_hover(), "Text Hover");
				colorPick(context, toggle.text_active()	, original.text_active(), "Text Active");
				colorPick(context, toggle.text_background()	, original.text_background(), "Text Background");

				toggle.text_alignment(textFlags(context, toggle.text_alignment()));
				
				vec2(context , toggle.padding() , "X Padding" , "Y Padding");
				vec2(context , toggle.touch_padding() , "X Touch Padding" , "Y Touch Padding");
				
				toggle.spacing(setFloat(context, toggle.spacing(), "Spacing"));
				toggle.border(setFloat(context, toggle.border(), "border"));

				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_button_text(context , "Reset to Defaults")) toggle.set(original);
						
				nk_combo_end(context);
				
			}
			
		}
		
	}
	
	//returns whether dest was changed as a result of this call
	private boolean colorPick(NkContext context , NkColor dest , NkColor def , String heading) {
		
		boolean changed = false;
		
		//check if previous color is absent
		if(!UIPaletteColors.stream().anyMatch(x -> x.r() == dest.r() && x.g() == dest.g() && x.b() == dest.b() && x.a() == dest.a())) { 
			
			dest.set(def);
			changed = true;
			
		}

		nk_layout_row_dynamic(context , 24 , 1);
		nk_text(context , heading + ":", TEXT_MIDDLE);
		NkColor result = layoutUIPalette(context , dest);		
		if(result != null) { 
			
			dest.set(result);
			changed = true;
			
		}
				
		return changed;
		
	}

	private void vec2(NkContext context , NkVec2 dest , String xComponentString , String yComponentString) {
		
		MemoryStack stack = MemoryStack.stackPush();
				
		FloatBuffer current = stack.floats(dest.x());
		nk_property_float(context , xComponentString , -100 , current , 100 , 0.1f , 0.2f);
		dest.x(current.get(0));
		
		current = stack.floats(dest.y());
		nk_property_float(context , yComponentString , -100 , current , 100 , 0.1f , 0.2f);
		dest.y(current.get(0));
						
		MemoryStack.stackPop();
		
	}
	
	private int textFlags(NkContext context , int currentFlags) {
		
		int flags = currentFlags;
		
		try(MemoryStack stack = MemoryStack.stackPush()) { 
			
			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_checkbox_text(context , "LEFT" , stack.bytes(has(flags , TEXT_LEFT) ? (byte) 1 : 0))) flags = toggle(flags , TEXT_LEFT);
			if(nk_checkbox_text(context , "RIGHT" , stack.bytes(has(flags , TEXT_RIGHT) ? (byte) 1 : 0))) flags = toggle(flags , TEXT_RIGHT);

			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_checkbox_text(context , "MIDDLE" , stack.bytes(has(flags , TEXT_MIDDLE) ? (byte) 1 : 0))) flags = toggle(flags , TEXT_MIDDLE);
			if(nk_checkbox_text(context , "CENTERED" , stack.bytes(has(flags , TEXT_CENTERED) ? (byte) 1 : 0))) { 
				
				flags = toggle(flags , TEXT_CENTERED);
				
			}

			nk_layout_row_dynamic(context , 30 , 2);
			if(nk_checkbox_text(context , "TOP" , stack.bytes(has(flags , TEXT_TOP) ? (byte) 1 : 0))) flags = toggle(flags , TEXT_TOP);
			if(nk_checkbox_text(context , "BOTTOM" , stack.bytes(has(flags , TEXT_BOTTOM) ? (byte) 1 : 0))) flags = toggle(flags , TEXT_BOTTOM);
			
		}		
		
		return flags;
		
	}
	
	private float setFloat(NkContext context , float currentFloat , String text) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			nk_layout_row_dynamic(context , 30 , 1);
			return nk_propertyf(context , text , -100 , currentFloat , 100 , 0.1f , 0.2f);
			
		}
		
	}
	
	private void styleItem(NkContext context , NkStyleItem item , NkStyleItem original , String colorPickerText) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			//TODO
//			nk_layout_row_dynamic(context , 30 , 3);
//			if(nk_checkbox_text(context , "Type = Color" , stack.bytes(item.type() == NK_STYLE_ITEM_COLOR ? (byte)1 : 0)));
//			if(nk_checkbox_text(context , "Type = Image" , stack.bytes(item.type() == NK_STYLE_ITEM_IMAGE ? (byte)1 : 0)));
//			if(nk_checkbox_text(context , "Type = Nine Slice" , stack.bytes(item.type() == NK_STYLE_ITEM_NINE_SLICE ? (byte)1 : 0)));

			item.type(NK_STYLE_ITEM_COLOR);
			styleItemData(context , item.data() , original.data() , colorPickerText);
			
		}
		
	}
	
	private void styleItemData(NkContext context , NkStyleItemData itemData , NkStyleItemData original , String colorPickerText) {
		
		colorPick(context, itemData.color() , original.color() , colorPickerText);
		
		//TODO image and 9slice
		
	}
	
	private int setInt(NkContext context , String text , int originalValue) {
		
		nk_layout_row_dynamic(context , 30 , 1);
		return nk_propertyi(context , text , -100 , originalValue , 100 , 1 , 1f);
		
	}
	
	private int symbol(NkContext context , String text , int originalValue) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			nk_layout_row_dynamic(context , 24 , 1);
			nk_text(context , text , TEXT_MIDDLE|TEXT_CENTERED);
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_checkbox_label(context , "NONE" , stack.bytes(originalValue == NK_SYMBOL_NONE ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_NONE;
				
			}
			
			if(nk_checkbox_label(context , "X" , stack.bytes(originalValue == NK_SYMBOL_X ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_X;
				
			}
			
			if(nk_checkbox_label(context , "UNDERSCORE" , stack.bytes(originalValue == NK_SYMBOL_UNDERSCORE ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_UNDERSCORE;
				
			}

			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_checkbox_label(context , "SOLID CIRCLE" , stack.bytes(originalValue == NK_SYMBOL_CIRCLE_SOLID ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_CIRCLE_SOLID;
				
			}
			
			if(nk_checkbox_label(context , "OUTLINE CIRCLE" , stack.bytes(originalValue == NK_SYMBOL_CIRCLE_OUTLINE ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_CIRCLE_OUTLINE;
				
			}
			
			if(nk_checkbox_label(context , "SOLID RECT" , stack.bytes(originalValue == NK_SYMBOL_RECT_SOLID ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_RECT_SOLID;
				
			}
		
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_checkbox_label(context , "OUTLINE RECT" , stack.bytes(originalValue == NK_SYMBOL_RECT_OUTLINE ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_RECT_OUTLINE;
				
			}
			
			if(nk_checkbox_label(context , "TRIANGLE UP" , stack.bytes(originalValue == NK_SYMBOL_TRIANGLE_UP ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_TRIANGLE_UP;
				
			}
			
			if(nk_checkbox_label(context , "TRIANGLE DOWN" , stack.bytes(originalValue == NK_SYMBOL_TRIANGLE_DOWN ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_TRIANGLE_DOWN;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_checkbox_label(context , "TRIANGLE LEFT" , stack.bytes(originalValue == NK_SYMBOL_TRIANGLE_LEFT ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_TRIANGLE_LEFT;
				
			}
			
			if(nk_checkbox_label(context , "TRIANGLE RIGHT" , stack.bytes(originalValue == NK_SYMBOL_TRIANGLE_RIGHT ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_TRIANGLE_RIGHT;
				
			}
			
			if(nk_checkbox_label(context , "PLUS" , stack.bytes(originalValue == NK_SYMBOL_PLUS ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_PLUS;
				
			}
			
			nk_layout_row_dynamic(context , 30 , 3);
			if(nk_checkbox_label(context , "MINUS" , stack.bytes(originalValue == NK_SYMBOL_MINUS ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_MINUS;
				
			}
			
			if(nk_checkbox_label(context , "MAX" , stack.bytes(originalValue == NK_SYMBOL_MAX ? (byte)1 : 0))) { 
				
				originalValue = NK_SYMBOL_MAX;
				
			}
			
			
		}
		
		return originalValue;
		
	}
	
	private void progressDesigner(NkContext context , NkStyleProgress progressStyle , NkStyleProgress original) {

		styleItem(context, progressStyle.normal() , original.normal() , "Normal");
		styleItem(context, progressStyle.hover()  , original.hover()  , "Hover");
		styleItem(context, progressStyle.active() , original.active() , "Active");
		
		colorPick(context , progressStyle.border_color() , original.border_color() , "Border Color");
		
		styleItem(context, progressStyle.cursor_normal() , original.cursor_normal(), "Cursor Normal");
		styleItem(context, progressStyle.cursor_hover()  , original.cursor_hover() ,  "Cursor Hover");
		styleItem(context, progressStyle.cursor_active() , original.cursor_active(), "Cursor Active");

		colorPick(context , progressStyle.cursor_border_color() , original.cursor_border_color() , "Cursor Border Color");
		
		progressStyle.rounding(setFloat(context , progressStyle.rounding() , "Rounding"));
		progressStyle.border(setFloat(context , progressStyle.border() , "Border"));
		progressStyle.cursor_border(setFloat(context , progressStyle.cursor_border() , "Cursor Border"));
		progressStyle.cursor_rounding(setFloat(context , progressStyle.cursor_rounding() , "Cursor Rounding"));
		
		vec2(context , progressStyle.padding() , "X Padding" , "Y Padding");

		nk_layout_row_dynamic(context , 30 , 1);
		if(nk_button_text(context , "Reset to Defaults")) progressStyle.set(original);
		
	}
	
	private void editDesigner(NkContext context , NkStyleEdit edit , NkStyleEdit original , boolean includeScrollbars) {
		
		styleItem(context , edit.normal()  , original.normal() , "Normal");
		styleItem(context , edit.hover()   , original.hover()  , "Hover");
		styleItem(context , edit.active()  , original.active() , "Active");
		
		colorPick(context , edit.border_color() , original.border_color() , "Border Color");
		
		if(includeScrollbars) scrollbarDesigner(context , edit.scrollbar() , original.scrollbar());

		colorPick(context , edit.cursor_normal() 		 , original.cursor_normal() 		, "Cursor Normal");
		colorPick(context , edit.cursor_hover() 		 , original.cursor_hover() 			, "Cursor Hover");
		colorPick(context , edit.cursor_text_normal() 	 , original.cursor_text_normal() 	, "Cursor Text Normal");
		colorPick(context , edit.cursor_text_hover() 	 , original.cursor_text_hover() 	, "Cursor Text Hover");
		colorPick(context , edit.text_normal() 			 , original.text_normal() 			, "Text Normal");
		colorPick(context , edit.text_hover() 			 , original.text_hover() 			, "Text Hover");
		colorPick(context , edit.text_active() 			 , original.text_active() 			, "Text Active");
		colorPick(context , edit.selected_normal() 		 , original.selected_normal() 		, "Selected Normal");
		colorPick(context , edit.selected_hover() 		 , original.selected_hover() 		, "Selected Hover");
		colorPick(context , edit.selected_text_normal()  , original.selected_text_normal()	, "Selected Text Normal");
		colorPick(context , edit.selected_text_hover()   , original.selected_text_hover() 	, "Selected Text Hover");

		edit.border(setFloat(context , edit.border() , "Border"));
		edit.rounding(setFloat(context , edit.rounding() , "Rounding"));
		edit.cursor_size(setFloat(context , edit.cursor_size() , "Cursor Size"));
		
		if(includeScrollbars) {
			
			vec2(context , edit.scrollbar_size() , "X Scrollbar Size" , "Y Scrollbar Size");
			vec2(context , edit.padding() , "X Padding" , "Y Padding");
			
		}
		
		edit.row_padding(setFloat(context , edit.row_padding() , "Row Padding"));

		nk_layout_row_dynamic(context , 30 , 1);
		if(nk_button_text(context , "Reset to Defaults")) edit.set(original);
		
	}
	
	private void scrollbarDesigner(NkContext context , NkStyleScrollbar scrollbar , NkStyleScrollbar original) {
		
		styleItem(context , scrollbar.normal()  , original.normal() , "Scrollbar Normal");
		styleItem(context , scrollbar.hover()   , original.hover()  , "Scrollbar Hover");
		styleItem(context , scrollbar.active()  , original.active() , "Scrollbar Active");		
		
		colorPick(context , scrollbar.border_color() , original.border_color() , "Scrollbar Border");

		styleItem(context , scrollbar.cursor_normal()  , original.cursor_normal(), "Scrollbar Cursor Normal");
		styleItem(context , scrollbar.cursor_hover()   , original.cursor_hover() , "Scrollbar Cursor Hover");
		styleItem(context , scrollbar.cursor_active()  , original.cursor_active(), "Scrollbar Cursor Active");
		
		colorPick(context , scrollbar.cursor_border_color() , original.cursor_border_color() , "Scrollbar Cursor Border");
				
		scrollbar.border(setFloat(context , scrollbar.border() , "Scrollbar Border"));
		scrollbar.rounding(setFloat(context , scrollbar.rounding() , "Scrollbar Rounding"));
		scrollbar.border_cursor(setFloat(context , scrollbar.border_cursor() , "Scrollbar Cursor Border"));
		scrollbar.rounding_cursor(setFloat(context , scrollbar.rounding_cursor() , "Scrollbar Cursor Rounding"));
		
		vec2(context , scrollbar.padding() , "Scrollbar X Padding" , "Scrollbar Y Padding");
		
		scrollbar.show_buttons(setInt(context , "Scrollbar Show Buttons" , scrollbar.show_buttons()));
		
		nk_layout_row_dynamic(context , 40 , 1);
		nk_text(context , "Scrollbar Increase Button" , TEXT_MIDDLE|TEXT_CENTERED);
		
		buttonDesigner(context, scrollbar.inc_button(), original.inc_button());		

		nk_layout_row_dynamic(context , 20 , 1);
		nk_text(context , "Scrollbar Decrease Button" , TEXT_MIDDLE|TEXT_CENTERED);

		buttonDesigner(context, scrollbar.inc_button(), original.inc_button());		

		scrollbar.inc_symbol(symbol(context , "Increase Symbol" , scrollbar.inc_symbol()));
		scrollbar.dec_symbol(symbol(context , "Decrease Symbol" , scrollbar.dec_symbol()));

		nk_layout_row_dynamic(context , 30 , 1);
		if(nk_button_text(context , "Reset to Defaults")) scrollbar.set(original);
			
	}
	
	private NkColor layoutUIPalette(NkContext context , NkColor current) {
		
		NkColor result = null;
		
		Iterator<NkColor> colors = UIPaletteColors.iterator();
		//display 16 colors per row
		for(int i = 0 ; i < UIPaletteColors.size() ; i++) {
			
			if(i % 16 == 0) {
				
				if(i > 0) nk_layout_row_end(context);				
				nk_layout_row_begin(context , NK_STATIC , 30 , 16);
								
			}
			
			nk_layout_row_push(context , 20);
			NkColor x = colors.next();
			
			//we set up the current color to be more distinct
			boolean currentIsX = current != null ? fieldWiseEqual(x, current) : false;
			if(currentIsX) {
				
				try(MemoryStack stack = MemoryStack.stackPush()) {
					
					NkColor green = NkColor.malloc(stack).set((byte)0x0 , (byte)0xff , (byte)0x0 , (byte)0xff);
					nk_style_push_color(context , context.style().button().border_color() , green);	
					float border = context.style().button().border();
					context.style().button().border(3);

					if(nk_button_color(context , x)) result = x;
					
					context.style().button().border(border);
					nk_style_pop_color(context);

				}
				
			} else if(nk_button_color(context , x)) result = x;
			
		}
		
		return result;
		
	}
	
	/**
	 * Adds the given color to the UI palette if the color is not already in the palette.
	 * 
	 * @param r — red channel for the color
	 * @param g — green channel for the color
	 * @param b — blue channel for the color
	 * @param a — alpha channel for the color
	 */
	public void addColorToPalette(byte r , byte g, byte b , byte a) { 

		//try to find duplicate in the list
		for(NkColor x : UIPaletteColors) {
			
			//exact same color
			if(x.r() == r && x.g() == g && x.b() == b && x.a() == a) return;
			
		}
		
		UIPaletteColors.add(NkColor.malloc().set(r , g , b , a));
		
	}	
	
	/**
	 * Adds the given color to the UI palette if it is not already present.
	 * 
	 * @param color — {@link NkColor} struct to add
	 */
	public void addColorToPalette(NkColor color) {
		
		addColorToPalette(color.r() , color.g() , color.b() , color.a());
		
	}
	
	/**
	 * Returns whether the UI has been closed yet.
	 */
	public boolean finished() {
		
		return finished;
		
	}
	
	private void setClearColorToSelected() {
		
		editor.rendererPost(() -> glClearColor(
			(float)(Byte.toUnsignedInt(windowColor.r()) / 255f) , 
			(float)(Byte.toUnsignedInt(windowColor.g()) / 255f) ,
			(float)(Byte.toUnsignedInt(windowColor.b()) / 255f) , 
			(float)(Byte.toUnsignedInt(windowColor.a()) / 255f)					
		));
		
	}

	private boolean fieldWiseEqual(NkColor x , NkColor y) {
		
		return x.r() == y.r() && x.g() == y.g() && x.b() == y.b() && x.a() == y.a();
		
	}
	
	@Override public void shutDown() {

		if(isFreed()) return;
		
		setClearColorToSelected();
		nuklear.context().style().set(originalStyle);
		isFreed = true;
		nuklear.removeUserInterface(modifierUI);
		nuklear.removeUserInterface(viewChangesUI);
		modifierUI.shutDown();
		viewChangesUI.shutDown();
		memFree(sliderValue);
		memFree(propertyValue);
		memFree(stringEditorMemory);
		memFree(stringEditorMemoryLength);
		memFree(redChannelText);
		memFree(redChannelTextLength);
		memFree(greenChannelText);
		memFree(greenChannelTextLength);
		memFree(blueChannelText);
		memFree(blueChannelTextLength);
		memFree(alphaChannelText);
		memFree(alphaChannelTextLength);
		memFree(radioButton);
		memFree(checkbox);
		colorPicker.free();
		originalStyle.free();
		this.UIPaletteColors.forEach(NkColor::free);
		windowColor.free();
		customStyle.free();
		
	}

	@Override public boolean isFreed() {

		return isFreed;
	}
	
}
