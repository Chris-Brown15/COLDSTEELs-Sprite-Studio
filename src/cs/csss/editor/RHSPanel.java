package cs.csss.editor;

import static cs.core.ui.CSUIConstants.*;

import static cs.csss.utils.UIUtils.*;

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

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.ui.prefabs.InputBox;
import cs.core.utils.CSRefInt;
import cs.core.utils.Lambda;
import cs.csss.core.CSSSProject;
import cs.csss.core.Engine;
import cs.csss.editor.events.HideLayerEvent;
import cs.csss.editor.events.MoveLayerRankEvent;
import cs.csss.editor.events.SwitchToNonVisualLayerEvent;
import cs.csss.editor.events.SwitchToVisualLayerEvent;
import cs.csss.utils.ConfirmationBox;
import cs.csss.utils.NotificationBox;

public class RHSPanel {

	/**
	 * Denotes the pixel offset from the left side of the UI for elements right below the project in tier.
	 */
	private static final int 
		TIER_ONE_PADDING = 30 ,
		TIER_TWO_PADDING = 60 ,
		TIER_THREE_PADDING = 90 ,
		TIER_FOUR_PADDING = 120 ,
		TIER_FIVE_PADDING = 150
	;
	
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
		expandNonVisualLayers = false
	;
	
	private CSSSProject current;
	private final CSUserInterface ui;
	
	public RHSPanel(Editor editor , CSNuklear nuklear) {

		ui = nuklear.new CSUserInterface("Project" , 0.80f , -1f , 0.199f , 0.90f);
		ui.setDimensions(ui.xPosition(), 77, ui.interfaceWidth(), ui.interfaceHeight());
		ui.options = UI_BORDERED|UI_TITLED;
		
		ui.specifyLayout((context , stack) -> {
			
			if((current = editor.currentProject()) == null) { 
				
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
				current.name() , 
				TEXT_MIDDLE , 
				toByte(stack , expandProject))
			) expandProject = !expandProject;
			
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
			
			if(expandAnimations) current.forEachAnimation(animation -> {
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 2);

				pad(context , TIER_TWO_PADDING);
				
				nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
				nk_text(context , animation.name() , dropdownTextOptions);
				
				nk_layout_row_end(context);
				
			});

			/*
			 * Visual Layer Section
			 */

			nk_layout_row_begin(context , NK_STATIC , 30 , 2);
			
			pad(context , TIER_ONE_PADDING);
			nk_layout_row_push(context , ui.interfaceWidth() - TIER_ONE_PADDING - 40);
			
			if(nk_selectable_symbol_text(
				context , 
				toMenuSymbol(expandVisual) , 
				"Visual Layers" , 
				TEXT_MIDDLE , 
				toByte(stack , expandVisual))
			) expandVisual = !expandVisual;
			
			if(expandVisual) {
				
				current.forEachVisualLayerPrototype(layer -> {
					
					nk_layout_row_begin(context , NK_STATIC , 30 , 2);

					pad(context , TIER_TWO_PADDING);
					
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
					nk_text(context , layer.toString() , dropdownTextOptions);
													
					nk_layout_row_end(context);				
					
					button(context , TIER_THREE_PADDING , "Delete" , () -> {
						
						if(current.visualLayerPrototypeSize() == 1) new NotificationBox(
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
							() -> editor.renderer().post(() -> editor.currentProject().deleteVisualLayer(layer)) , 
							() -> {}
						);
						
					});
					
				});
				
			}
			
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
			
			if(expandNonvisual) current.forEachNonVisualLayerPrototype(layer -> {
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 2);

				pad(context , TIER_TWO_PADDING);
				
				nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
				nk_text(context , layer.toString() , dropdownTextOptions);
				
				nk_layout_row_end(context);				
				
			});

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
			
			CSRefInt iter = new CSRefInt(-1);
			if(expandArtboards) current.forEachArtboard(artboard -> {

				iter.add(1);
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 2);

				pad(context , TIER_TWO_PADDING);
				
				boolean active = editor.currentArtboard() == artboard;

				nk_layout_row_push(context , ui.interfaceWidth() - TIER_TWO_PADDING - 40);
				nk_selectable_text(context , "Artboard " + iter.intValue() , dropdownTextOptions , toByte(stack , active));
				nk_layout_row_end(context);				
				
				//This part is for the active artboard. It displays the layer instances for that artboard and lets the user change which is
				//active.
				
				if(!active) return;
				
				//modify artboard buttons
				
				if(Engine.isDebug()) button(context , TIER_THREE_PADDING , "Write to File" , () -> {
					
					artboard.writeToFile(iter.intValue(), "debug/" + editor.currentProject().name() + "/");
					
				});
				
				
				//visual layers
				
				nk_layout_row_begin(context , NK_STATIC , 30 , 2);
				pad(context , TIER_THREE_PADDING);
				nk_layout_row_push(context , ui.interfaceWidth() - TIER_THREE_PADDING - 40);

				/* The iter is decremented because we incremented it above */
				if(nk_selectable_symbol_text(
					context , 
					toMenuSymbol(expandVisualLayers) , 
					"Visual Layers" , 
					TEXT_MIDDLE , 
					toByte(stack , expandVisualLayers)
				)) expandVisualLayers = !expandVisualLayers;

				nk_layout_row_end(context);

				if(expandVisualLayers) artboard.forEachVisualLayer(layer -> {

					int rank = artboard.getLayerRank(layer);
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 2);
					pad(context , TIER_FOUR_PADDING);
					
					boolean activeLayer = artboard.isActiveLayer(layer);
					
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_FOUR_PADDING - 50);
					if(nk_radio_text(context , layer.name + " -> Rank " + rank , toByte(stack , activeLayer))) { 
						
						editor.eventPush(new SwitchToVisualLayerEvent(artboard , layer));
						
					}
					
					nk_layout_row_end(context);
					
					//stuff to modify the active layer.
					
					if(activeLayer) {
						
						button(context , TIER_FIVE_PADDING , "Set Rank" , () -> {
						
							new InputBox(
								nuklear , 
								"Input New Rank for Layer " + rank , 
								0.4f , 
								0.4f , 
								4 , 
								CSNuklear.DECIMAL_FILTER , 
								result -> {
								
									if(result.equals("")) return;
									
									CSRefInt res = new CSRefInt(Integer.parseInt(result));
									if(res.intValue() >= artboard.numberVisualLayers()) res.set(artboard.numberVisualLayers());
									if(res.intValue() < 0) res.set(0);
										
									editor.eventPush(new MoveLayerRankEvent(artboard , res.intValue()));
																		
								}
							);
							
						});

						//hide button
						
						activeLayerCheckBox(context , "Hide" , layer.hiding() , () -> {
							
							editor.eventPush(new HideLayerEvent(artboard , artboard.getLayerRank(layer)));
							
						});
						
						activeLayerCheckBox(context , "Lock" , layer.locked() , () -> {
							
							layer.toggleLock();
							
						});
						
						//debug dump to file button
						
						if(!Engine.isDebug()) return;
						
						button(context , TIER_FIVE_PADDING , "Dump To File" , () -> layer.compressToFile("debug/layers/" + layer.name));
					
						button(context , TIER_FIVE_PADDING , "Load From File" , () -> {
							
							layer.decompressFromFile("debug/layers/" + layer.name + "/" + layer.name);
						
						});
						
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
					toByte(stack , expandNonVisualLayers)
				)) expandNonVisualLayers = !expandNonVisualLayers;

				nk_layout_row_end(context);
				
				if(expandNonVisualLayers) artboard.forEachNonVisualLayer(layer -> {
					
					nk_layout_row_begin(context , NK_STATIC , 20 , 2);
					pad(context , TIER_FOUR_PADDING);
					
					nk_layout_row_push(context , ui.interfaceWidth() - TIER_FOUR_PADDING - 40);
					if(nk_radio_text(context , layer.name , toByte(stack , artboard.isActiveLayer(layer)))) { 
						
						editor.eventPush(new SwitchToNonVisualLayerEvent(artboard , layer));
					
					}
					
					nk_layout_row_end(context);
					
					if(artboard.isActiveLayer(layer)) {
						
						activeLayerCheckBox(context , "Hide" , layer.hiding() , () -> editor.renderer().post(() -> {
							
							if(!layer.hiding()) layer.hide(artboard);
							else layer.show(artboard);
							
						}));
						
					}
					
				});
				
			});

		});	
		
	}
	
	private void pad(NkContext context , int pixels) {
		
		nk_layout_row_push(context , pixels);
		nk_text_wrap(context ,  "");
		
	}

	private void button(NkContext context , int padding , final String buttonText , final Lambda onPress) {

		final int rowSpace =  ui.interfaceWidth() - padding - 50;

		nk_layout_row_begin(context , NK_STATIC , 20 , 2);
		pad(context, padding);
		
		nk_layout_row_push(context , rowSpace);						
		if(nk_button_text(context , buttonText)) onPress.invoke();
		
		nk_layout_row_end(context);

	}
	
	private void activeLayerCheckBox(NkContext context , final String buttonText , final boolean state , final Lambda onPress) {

		final int rowSpace =  ui.interfaceWidth() - TIER_FIVE_PADDING - 50;

		nk_layout_row_begin(context , NK_STATIC , 20 , 2);
		pad(context, TIER_FIVE_PADDING);
		
		nk_layout_row_push(context , rowSpace);						
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			if(nk_checkbox_text(context , buttonText , toByte(stack , state))) onPress.invoke();
		}
		
		nk_layout_row_end(context);

	}
	
}
