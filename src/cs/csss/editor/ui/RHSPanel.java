package cs.csss.editor.ui;

import static cs.core.ui.CSUIConstants.*;

import static cs.csss.ui.utils.UIUtils.*;

import static org.lwjgl.nuklear.Nuklear.NK_STATIC;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_text;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;
import static org.lwjgl.nuklear.Nuklear.nk_radio_text;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap_colored;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_text;
import static org.lwjgl.nuklear.Nuklear.nk_button_text;
import static org.lwjgl.nuklear.Nuklear.nk_spacer;
import static org.lwjgl.nuklear.Nuklear.nk_edit_string;
import static org.lwjgl.nuklear.Nuklear.nk_propertyi;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;
import static org.lwjgl.nuklear.Nuklear.nk_text_colored;

import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.util.Iterator;
import java.util.function.BooleanSupplier;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkColorf;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.coreext.nanovg.NanoVGTypeface;
import cs.csss.editor.Editor;
import cs.csss.editor.event.HideLayerEvent;
import cs.csss.editor.event.SwitchToNonVisualLayerEvent;
import cs.csss.editor.event.SwitchToVisualLayerEvent;
import cs.csss.engine.Engine;
import cs.csss.engine.NamedNanoVGTypeface;
import cs.csss.project.CSSSProject;
import cs.csss.ui.menus.ConfirmationBox;
import cs.csss.ui.menus.NotificationBox;
import cs.csss.ui.menus.VectorTextMenu;

/**
 * Class for the right hand side panel. This class contains a drop down tree of project components.
 */
@SuppressWarnings("unused") public class RHSPanel {
	
	/**
	 * Denotes the pixel offset from the left side of the UI for elements right below the project in tier.
	 */
	private static final int 
		TIER_ONE_PADDING = 30 ,
		TIER_TWO_PADDING = 60 ,
		TIER_THREE_PADDING = 90 ,
		TIER_FOUR_PADDING = 120 ,
		TIER_FIVE_PADDING = 150 ,
		OPTION_TEXT = TEXT_RIGHT|TEXT_MIDDLE;
	
	/**
	 * Denotes whether the user has expanded the specific part of the project heirarchy.
	 */
	private boolean 
		expandProject = false ,
		expandAnimations = false ,
		expandVisual = false ,
		expandNonvisual = false ,
		expandArtboards = false ,
		expandVisualLayers = false ,
		expandNonVisualLayers = false ,
		expandVectorTextBoxes = false ,
		expandFonts = false;
	
	private CSSSProject project;
	private final CSUserInterface ui;
	
	/**
	 * Creates a right hand side panel.
	 * 
	 * @param editor — the editor
	 * @param nuklear — Nuklear factory
	 * @param engine — the engine
	 */
	public RHSPanel(Editor editor , CSNuklear nuklear , Engine engine) {

		ui = nuklear.new CSUserInterface("Project" , 0.80f , -1f , 0.199f , 0.90f);
		ui.setDimensions(ui.xPosition(), 77, ui.interfaceWidth(), ui.interfaceHeight());
		ui.options = UI_BORDERED|UI_TITLED|UI_ICONIFYABLE;
		
		ui.specifyLayout((context , stack) -> {
			
			if((project = editor.project()) == null) { 
				
				nk_layout_row_dynamic(context , 20 , 1);
				nk_text_wrap_colored(context , "No project is active." , NkColor.malloc(stack).set(
					(byte)0xee , 
					(byte)0xee , 
					(byte)0 , 
					(byte)0xff
				));

				nk_layout_row_dynamic(context , 90 , 1);
				nk_text_wrap(context , "Create one with the \"Project\" button on the File Panel.");
				return;
				
			}
				
			final int dropdownTextOptions = TEXT_LEFT|TEXT_CENTERED;
			
			/*
			 * Project Section
			 */
			
			nk_layout_row_dynamic(context , 30 , 1);
			if(nk_selectable_symbol_text(
				context , 
				toMenuSymbol(expandProject) , 
				project.name() , 
				TEXT_MIDDLE , 
				toByte(stack , expandProject)
			)) expandProject = !expandProject;
			
			if(!expandProject) return;
			
			/*
			 * Animation Section
			 */
			
			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
			
			pad(context , TIER_ONE_PADDING);
			nk_layout_row_push(context , ui.interfaceWidth() - TIER_ONE_PADDING - 40);
			
			if(nk_selectable_symbol_text(
				context , 
				toMenuSymbol(expandAnimations) , 
				"Animations" , 
				TEXT_MIDDLE , 
				toByte(stack , expandAnimations))
			) expandAnimations = !expandAnimations;
			
			if(expandAnimations) {

				if(project.numberAnimations() == 0) {

					noItemPresentText(context , stack, "No Animations present.");
					addItemInProjectMenuText(context);
					
				}
				
				project.forEachAnimation(animation -> {
					
					nk_layout_row_begin(context , NK_STATIC , 30 , 2);

					pad(context , TIER_TWO_PADDING);
					
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
					if(nk_selectable_text(context , animation.name() , OPTION_TEXT , asByte(editor.isCurrentAnimation(animation)))) { 
						
						editor.project().currentAnimation(animation);
						
					}
					
					nk_layout_row_end(context);
					
					if(editor.isCurrentAnimation(animation)) {
						
						nk_layout_row_begin(context , NK_STATIC , 30 , 2);
						pad(context , TIER_THREE_PADDING);
						
						nk_layout_row_push(context , ui.interfaceWidth() - TIER_THREE_PADDING - 40);
						if(nk_button_text(context , "Delete")) Engine.THE_TEMPORAL.onTrue(() -> true, editor.project()::deleteAnimation);
											
					}
					
				});

			}
			
			/*
			 * Visual Layer Section
			 */

			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
			
			pad(context , TIER_ONE_PADDING);
			nk_layout_row_push(context , ui.interfaceWidth() - TIER_ONE_PADDING - 40);
			
			if(nk_selectable_symbol_text(context , toMenuSymbol(expandVisual) , "Visual Layers" , TEXT_MIDDLE , asByte(expandVisual))) { 
				
				expandVisual = !expandVisual;
				
			}
			
			if(expandVisual) project.forEachVisualLayerPrototype(layer -> {
					
				nk_layout_row_begin(context , NK_STATIC , 30 , 3);

				pad(context , TIER_TWO_PADDING);
					
				int totalSize = ui.interfaceWidth() - TIER_TWO_PADDING - 40;
				nk_layout_row_push(context , totalSize / 2);
				nk_text(context , layer.UIString() , dropdownTextOptions);
				
				nk_layout_row_push(context , totalSize / 2);
				if(nk_button_text(context , "Delete")) {
					
					if(project.numberVisualLayers() == 1) new NotificationBox(
						"Cannot Delete Layer" , 
						"You cannot delete this layer because at least one visual layer must always be present." ,
						nuklear
					); 
					else new ConfirmationBox(
						nuklear , 
						"Sure?" ,
						"Are You Sure You Want To Delete " + layer.name() + "? This will remove this layer from every artboard." , 
						0.4f , 
						0.4f , 
						() -> editor.rendererPost(() -> editor.project().deleteVisualLayer(layer)) , 
						() -> {}
					);
						
				}
					
			});				
			
			/*
			 * Nonvisual Layer Section
			 */

			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
			
			pad(context , TIER_ONE_PADDING);
			nk_layout_row_push(context , ui.interfaceWidth() - TIER_ONE_PADDING - 40);

			if(nk_selectable_symbol_text(
				context , 
				toMenuSymbol(expandNonvisual) , 
				"Nonvisual Layers" , 
				TEXT_MIDDLE , 
				toByte(stack , expandNonvisual))
			) expandNonvisual = !expandNonvisual;
			
			if(expandNonvisual) {

				if(project.numberNonVisualLayers() == 0) {
					
					noItemPresentText(context , stack, "No Nonvisual Layers present.");
					addItemInProjectMenuText(context);
					
				}
				
				project.forEachNonVisualLayerPrototype(layer -> {
					
					nk_layout_row_begin(context , NK_STATIC , 30 , 2);

					pad(context , TIER_TWO_PADDING);
					
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
					nk_text(context , layer.UIString() , dropdownTextOptions);
					
					nk_layout_row_end(context);
					
					button(context , TIER_THREE_PADDING , 30 , "Delete" , () -> {
						 new ConfirmationBox(
							nuklear , 
							"Sure?" ,
							"Are You Sure You Want To Delete " + layer.name() + "? This will remove this layer from every artboard." , 
							0.4f , 
							0.4f , 
							() -> editor.rendererPost(() -> editor.project().deleteNonVisualLayer(layer)) , 
							() -> {}
						);
					});
					
				});

			}
			/*
			 * Artboard Section
			 */

			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
			
			pad(context , TIER_ONE_PADDING);
			nk_layout_row_push(context , ui.interfaceWidth() - TIER_ONE_PADDING - 40);

			int symbol = toMenuSymbol(expandArtboards);
			
			if(nk_selectable_symbol_text(context , symbol , "Artboards" , TEXT_MIDDLE , toByte(stack , expandArtboards))) { 
				
				expandArtboards = !expandArtboards;
			
			}
			
			if(expandArtboards) {
				
				if(project.getNumberNonCopiedArtboards() == 0) {
					
					noItemPresentText(context , stack, "No Artboards present.");
					addItemInProjectMenuText(context);
					
				}
				
				 project.forEachArtboard(artboard -> {
					
					nk_layout_row_begin(context , NK_STATIC , 30 , 2);

					pad(context , TIER_TWO_PADDING);
						
					boolean active = editor.currentArtboard() == artboard;
						
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
					nk_selectable_text(context , editor.getArtboardUIName(artboard) , dropdownTextOptions , toByte(stack , active));
					nk_layout_row_end(context);				
						
					//This part is for the active artboard. It displays the layer instances for that artboard and lets the user change which 
					//layer is active.				
					if(!active) return;
					
					//modify artboard buttons
					
					nk_layout_row_begin(context , NK_STATIC , 30 , 4);
					
					pad(context , TIER_THREE_PADDING);
					int rowWidth = (ui.interfaceWidth() - TIER_THREE_PADDING - 54) / 2;
					nk_layout_row_push(context , rowWidth);
					if(nk_button_text(context , "Copy")) editor.rendererPost(() -> editor.addRender(project.deepCopy(artboard).render()));
					
					nk_layout_row_push(context , rowWidth);
					if(nk_button_text(context , "Remove")) Engine.THE_TEMPORAL.onTrue(() -> true , () -> editor.rendererPost(() -> {
						
						editor.project().removeArtboard(artboard);
						
					}));
					
					nk_layout_row_end(context);
					
					//visual layers
					
					nk_layout_row_begin(context , NK_STATIC , 30 , 2);
					pad(context , TIER_THREE_PADDING);
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_THREE_PADDING - 40);

					if(nk_selectable_symbol_text(context , toMenuSymbol(expandVisualLayers) , "Visual Layers" , TEXT_MIDDLE , asByte(
						expandVisualLayers
					))) { 
						
						expandVisualLayers = !expandVisualLayers;
						
					}

					nk_layout_row_end(context);

					if(expandVisualLayers) artboard.forEachVisualLayer(layer -> {

						int rank = artboard.getLayerRank(layer);
						
						nk_layout_row_begin(context , NK_STATIC , 20 , 2);
						pad(context , TIER_FOUR_PADDING);
						
						boolean activeLayer = artboard.isActiveLayer(layer);
						
						nk_layout_row_push(context , ui.interfaceWidth() - TIER_FOUR_PADDING - 50);
						if(nk_radio_text(context , layer.name + " -> Rank " + rank , toByte(stack , activeLayer))) { 
							
							editor.eventPush(new SwitchToVisualLayerEvent(artboard , editor.project() , layer));
							
						}
						
						nk_layout_row_end(context);
						
						//stuff to modify the active layer.
						
						if(activeLayer) {
							
							button(context , TIER_FIVE_PADDING , "Set Rank" , () -> editor.startMoveLayerRankEvent(layer));
							
							activeLayerCheckBox(context , "Hide" , layer.hiding() , () -> {
								
								editor.eventPush(new HideLayerEvent(artboard , artboard.getLayerRank(layer)));
								
							});
							
							activeLayerCheckBox(context , "Lock" , layer.locked() , () -> layer.toggleLock());
													
						}

					});
					
					//nonvisual layers
					
					nk_layout_row_begin(context , NK_STATIC , 30 , 2);
					pad(context , TIER_THREE_PADDING);
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_THREE_PADDING - 40);
					
					if(nk_selectable_symbol_text(
						context , 
						toMenuSymbol(expandNonVisualLayers) , 
						"Nonvisual Layers" , 
						TEXT_MIDDLE, 
						asByte(expandNonVisualLayers)
					)) { 
						
						expandNonVisualLayers = !expandNonVisualLayers;
						
					}

					nk_layout_row_end(context);
					
					if(expandNonVisualLayers) artboard.forEachNonVisualLayer(layer -> {
						
						nk_layout_row_begin(context , NK_STATIC , 20 , 2);
						pad(context , TIER_FOUR_PADDING);
						
						nk_layout_row_push(context , ui.interfaceWidth() - TIER_FOUR_PADDING - 40);
						if(nk_radio_text(context , layer.name , toByte(stack , artboard.isActiveLayer(layer)))) { 
							
							editor.eventPush(new SwitchToNonVisualLayerEvent(artboard , editor.project() , layer));
						
						}
						
						nk_layout_row_end(context);
						
						if(artboard.isActiveLayer(layer)) {
							
							activeLayerCheckBox(context , "Hide" , layer.hiding() , () -> editor.rendererPost(() -> {
								
								if(!layer.hiding()) layer.hide(artboard);
								else layer.show(artboard);
								
							}));
							
						}
						
					});

				});
						
			}
			
//			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
//			pad(context , TIER_ONE_PADDING);
//			nk_layout_row_push(context , ui.interfaceWidth() - TIER_ONE_PADDING - 40);
//			
//			if(nk_selectable_symbol_text(
//				context , 
//				toMenuSymbol(expandVectorTextBoxes) , 
//				"Text Boxes" , 
//				TEXT_MIDDLE, 
//				asByte(expandVectorTextBoxes)
//			)) { 
//				
//				expandVectorTextBoxes = !expandVectorTextBoxes;
//				
//			}
//
//			nk_layout_row_end(context);
//			
//			if(expandVectorTextBoxes)  {
//				
//				project.forEachVectorTextBox(textBox -> {
//					
//					boolean isActive = textBox == project.currentTextBox();
//					
//					int textDropDownSymbol = isActive ? SYMBOL_TRIANGLE_DOWN : SYMBOL_TRIANGLE_RIGHT;
//					
//					nk_layout_row_begin(context , NK_STATIC , 30 , 2);
//					pad(context , TIER_TWO_PADDING);
//					
//					nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
//					
//					/*
//					 * TODO:
//					 * For some unknown reason the program will crash if it is asked to display too much text in some of these nuklear 
//					 * functions, therefore we limit the length of strings in order to hopefully prevent that from happening.
//					 */
//					String text = memUTF8(textBox.text().get());
//					if(text.length() > 10) text = text.substring(0, 10);
//					
//					if(nk_selectable_symbol_text(context , textDropDownSymbol , text , TEXT_MIDDLE , toByte(stack , isActive))) {
//					
//						editor.setBrushTo(null);						
//						project.currentTextBox(textBox);
//						
//					}
//										
//					nk_layout_row_end(context);
//					
//					if(isActive) {
//
//						nk_layout_row_begin(context , NK_STATIC , 30 , 2);
//						pad(context , TIER_THREE_PADDING);
//						int fontSymbol = expandFonts ? SYMBOL_TRIANGLE_DOWN : SYMBOL_TRIANGLE_RIGHT; 
//						nk_layout_row_push(context , ui.interfaceWidth() - TIER_THREE_PADDING - 40);
//						if(nk_selectable_symbol_text(context , fontSymbol , "Fonts" , TEXT_MIDDLE , toByte(stack , expandFonts))) {
//							
//							expandFonts = !expandFonts;
//							
//						}
//						
//						nk_layout_row_end(context);
//						
//						if(expandFonts) {
//							
//							Iterator<NamedNanoVGTypeface> typefaceIterator = engine.loadedFonts.iterator();
//							
//							while(typefaceIterator.hasNext()) {
//								
//								NamedNanoVGTypeface typefaceContainer = typefaceIterator.next();
//								NanoVGTypeface typeface = typefaceContainer.typeface();
//								
//								radio(
//									context , 
//									typefaceContainer.name() , 
//									TIER_FOUR_PADDING , 
//									30 , 
//									() -> textBox.typeface(typeface) , 
//									() -> textBox.typeface() == typeface
//								);
//								
//							}
//						
//						}
//						
//						nk_layout_row_begin(context , NK_STATIC , 20 , 2);
//						pad(context , TIER_THREE_PADDING);						
//						nk_text(context , "Input Text: " , TEXT_LEFT);						
//						nk_layout_row_end(context);
//						
//						nk_layout_row_begin(context , NK_STATIC , 150 , 2);						
//						pad(context , TIER_THREE_PADDING);
//						nk_layout_row_push(context , ui.interfaceWidth() - TIER_THREE_PADDING - 40);						
//						nk_edit_string(
//							context , 
//							VectorTextMenu.TEXT_EDIT_OPTIONS , 
//							textBox.textEditorBuffer() ,
//							textBox.textLengthBuffer() , 
//							999 , 
//							CSNuklear.NO_FILTER
//						);
//						
//						nk_layout_row_end(context);
//
//						button(context , TIER_THREE_PADDING , 30 , "Accept" , textBox::setTextFromBuffers);
//						button(context , TIER_THREE_PADDING , 30 , "Remove" , () -> onTrue(() -> project.removeTextBox(textBox)));
//						textBox.charHeight(slider(context , TIER_THREE_PADDING , textBox.charHeight() , 1 , 9999 , 1 , 1f));	
//						textBox.rowHeight(slider(context , TIER_THREE_PADDING , textBox.rowHeight() , 1 , 9999 , 1 , 1f));
//						checkBox(context , "Move/Arrange" , TIER_THREE_PADDING , 30 , project::toggleMovingText , project::movingText);
//					
//						nk_layout_row_begin(context , NK_STATIC , 20 , 2);
//						pad(context , TIER_THREE_PADDING);
//						nk_text(context , "Text Color: " , TEXT_LEFT);					
//						nk_layout_row_end(context);
//						
//						nk_layout_row_begin(context , NK_STATIC , 150 , 2);						
//						pad(context , TIER_THREE_PADDING);
//						nk_layout_row_push(context , ui.interfaceWidth() - TIER_THREE_PADDING - 40);	
//						
//						NkColorf color = NkColorf.malloc(stack);
//						
//						int colors = textBox.color();
//						
//						float ured = Byte.toUnsignedInt((byte)(colors >> 24));
//						float ugreen = Byte.toUnsignedInt((byte)(colors >> 16));
//						float ublue = Byte.toUnsignedInt((byte)(colors >> 8));
//						float ualpha = Byte.toUnsignedInt((byte) colors);
//						
//						color.set(ured / 255f , ugreen / 255f , ublue / 255f , ualpha / 255f); 
//						
//						int format = project.channelsPerPixel() == 4 ? RGBA : RGB;
//						nk_color_pick(context , color , format);
//						nk_layout_row_end(context);					
//						
//						byte red = (byte)(color.r() * 255);
//						byte green = (byte)(color.g() * 255);
//						byte blue = (byte)(color.b() * 255);
//						byte alpha = (byte)(color.a() * 255);
//						
//						colors = (255 & red) << 24 | (255 & green) << 16 | (255 & blue) << 8 | 255 & alpha;
//						textBox.color(colors);
//						
//					}
//					
//				});
//				
//			}
			
		});	
		
	}
	
	private void pad(NkContext context , int pixels) {
		
		nk_layout_row_push(context , pixels);
		nk_spacer(context);
		
	}

	private void button(NkContext context , int padding , final String buttonText , final Lambda onPress) {

		button(context , padding , 20 , buttonText , onPress);
		
	}

	private void button(NkContext context , int padding , int rowWidth , final String buttonText , final Lambda onPress) {

		final int rowSpace =  ui.interfaceWidth() - padding - 40;

		nk_layout_row_begin(context , NK_STATIC , rowWidth , 2);
		pad(context, padding);
		
		nk_layout_row_push(context , rowSpace);						
		if(nk_button_text(context , buttonText)) onPress.invoke();
		
		nk_layout_row_end(context);

	}

	private void checkBox(NkContext context , String text , int padding , int rowWidth , Lambda onPress , BooleanSupplier state) {
		
		final int rowSpace =  ui.interfaceWidth() - padding - 40;

		nk_layout_row_begin(context , NK_STATIC , rowWidth , 2);
		pad(context, padding);
		
		nk_layout_row_push(context , rowSpace);
		if(nk_checkbox_text(context , text , toByte(MemoryStack.stackGet() , state.getAsBoolean()))) onPress.invoke();
		
		nk_layout_row_end(context);

	}

	private void radio(NkContext context , String text , int padding , int rowWidth , Lambda onPress , BooleanSupplier state) {
		
		final int rowSpace =  ui.interfaceWidth() - padding - 40;

		nk_layout_row_begin(context , NK_STATIC , rowWidth , 2);
		pad(context, padding);
		
		nk_layout_row_push(context , rowSpace);
		if(nk_radio_text(context , text , toByte(MemoryStack.stackGet() , state.getAsBoolean()))) onPress.invoke();
		
		nk_layout_row_end(context);

	}
	
	private void activeLayerCheckBox(NkContext context , final String buttonText , final boolean state , final Lambda onPress) {

		final int rowSpace =  ui.interfaceWidth() - TIER_FIVE_PADDING - 40;

		nk_layout_row_begin(context , NK_STATIC , 20 , 2);
		pad(context, TIER_FIVE_PADDING);
		
		nk_layout_row_push(context , rowSpace);						
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			if(nk_checkbox_text(context , buttonText , toByte(stack , state))) onPress.invoke();
			
		}
		
		nk_layout_row_end(context);

	}

	private int slider(NkContext context , int padding , int current , int min , int max , int increase , float increasePerPixel) {

		nk_layout_row_begin(context , NK_STATIC , 30 , 2);
		pad(context , padding);			
		nk_layout_row_push(context , ui.interfaceWidth() - padding - 40);						
		int result = nk_propertyi(context, "Row Height" , 1 , current , 9999, 1, increasePerPixel);			
		nk_layout_row_end(context);

		return result;
		
	}
	
	private void addItemInProjectMenuText(NkContext context) {

		nk_layout_row_begin(context , NK_STATIC , 20 , 2);
		pad(context , TIER_TWO_PADDING);
		nk_layout_row_push(context  ,ui.interfaceWidth() - TIER_TWO_PADDING - 40);
		nk_text(context , "Add one in the 'Project' drop down." , TEXT_LEFT|TEXT_CENTERED);
		nk_layout_row_end(context);
		
	}
	
	private void noItemPresentText(NkContext context , MemoryStack stack , String text) {

		nk_layout_row_begin(context , NK_STATIC , 20 , 2);
		pad(context , TIER_TWO_PADDING);
		NkColor color = NkColor.malloc(stack).set((byte)0xee , (byte)0xee , (byte)0x0 , (byte)0xff);
		nk_layout_row_push(context  ,ui.interfaceWidth() - TIER_TWO_PADDING - 40);
		nk_text_colored(context , text , TEXT_LEFT|TEXT_CENTERED , color);
		nk_layout_row_end(context);
			
	}	

	private void onTrue(Lambda onTrue) {
		
		Engine.THE_TEMPORAL.onTrue(() -> true , onTrue);
		
	}
	
}
