package cs.csss.editor.ui;

import static org.lwjgl.nuklear.Nuklear.NK_DYNAMIC;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_window_get_content_region;
import static org.lwjgl.nuklear.Nuklear.nk_button_text;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_propertyf;
import static org.lwjgl.nuklear.Nuklear.nk_propertyi;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_symbol_text;
import static org.lwjgl.nuklear.Nuklear.nk_spacer;

import static cs.csss.ui.utils.UIUtils.toByte;
import static cs.core.ui.CSUIConstants.*;

import java.util.function.BooleanSupplier;

import org.joml.Matrix4f;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSButton;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSGroup;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.utils.CSRefInt;
import cs.core.utils.ShutDown;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.csss.editor.Editor;
import cs.csss.editor.event.ModifyArtboardInAnimationStatusEvent;
import cs.csss.engine.Engine;
import cs.csss.project.Animation;
import cs.csss.project.AnimationFrame;
import cs.csss.project.AnimationSwapType;
import cs.csss.project.CSSSProject;
import cs.csss.ui.elements.ProgressBar;
import cs.csss.ui.elements.UIAttachedElement;
import cs.csss.ui.utils.UIUtils;

/**
 * This panel is used to modify animations. It has more state than others because it also facilitates rendering the active frame of the 
 * animation in the middle group of the top part, a collection of three groups.
 * 
 * @author Chris Brown
 *
 */
public class AnimationPanel implements ShutDown {

	private static final int textOptions = TEXT_LEFT|TEXT_CENTERED , toolTipShow = HOVERING|MOUSE_PRESSED;
	
	private final CSNuklear nuklear;	
	private CSUserInterface ui;
	
	private CSSSProject project;
	
	private boolean show = false;
	
	private CSRow topPartSections;	
	private CSDynamicRow frameTimeRow , frameUpdatesRow;

	private AnimationSwapType[] swapTypes = AnimationSwapType.values();
	
	private CSDynamicRow[] swapTypeRows = new CSDynamicRow[swapTypes.length];
	
	private int framePanelX , framePanelY , framePanelWidth , framePanelHeight;

	//controls the zoom and translations of the object within the ui panel
	private float zoom = 0.3f , xTranslation = 0, yTranslation = 0;
	
	private final Matrix4f moveToPoint = new Matrix4f().identity();
	
	private BooleanSupplier doLayout = () -> project != null && animation() != null;
	
	/**
	 * Creates the animation panel.
	 * 
	 * @param editor — the editor
	 * @param nuklear — the Nuklear factory
	 */
	public AnimationPanel(Editor editor , CSNuklear nuklear) {

		ui = nuklear.new CSUserInterface("Animation Viewer" , 0.203f , 0.64f , 0.594f , 0.3320f);
		
		nuklear.removeUserInterface(ui);
		
		ui.options = UI_TITLED|UI_BORDERED|UI_UNSCROLLABLE;
		
		this.nuklear = nuklear;
		
		project = editor.project();
		
		//hack to set current project every frame 
		ui.attachedLayout((context , stack) -> project = editor.project());
		
		Engine.THE_TEMPORAL.onTrue(() -> animation() != null , () -> {

			String timeSliderText = "Average Frame Speed (Millis)";
			String updateSliderText = "Average Frame Speed (Frames)";
					
			UIAttachedElement averageFrameTimeSlider = new UIAttachedElement(frameTimeRow , () -> {
				
				float current = animation().getFrameTime.getAsFloat();
				animation().setFrameTime(nk_propertyf(nuklear.context() , timeSliderText , 0 , current , 999999f , .1f , .1f));
				
			});
			
			UIAttachedElement averageFrameUpdatesSlider = new UIAttachedElement(frameUpdatesRow , () -> {
				
				int current = animation().getUpdates.getAsInt();
				animation().setUpdates(nk_propertyi(nuklear.context() , updateSliderText , 0 , current , 999999 , 1 , 1f));
				
			});
			
			String timeSliderTooltip = "Used to set the amount of milliseconds frames of this animation will take before going to "
				+ "the next one (except for frames that have their own times and frames who swap by program frames).";
			
			String updateSliderTooltip = "Used to set the amount of program frames frames of this animation will take before going to the "
				+ "next one (except for animation frames that have their own frame amounts and animation frames that swap by time).";
			
			averageFrameTimeSlider.initializeToolTip(toolTipShow , MOUSE_RIGHT , 0 , UIUtils.textLength(timeSliderTooltip));
			averageFrameTimeSlider.toolTip.new CSDynamicRow(20).new CSText(timeSliderTooltip);
			
			averageFrameUpdatesSlider.initializeToolTip(toolTipShow , MOUSE_RIGHT , 0 , UIUtils.textLength(updateSliderTooltip));
			averageFrameUpdatesSlider.toolTip.new CSDynamicRow(20).new CSText(updateSliderTooltip);

			CSRadio[] swapTypeRadios = new CSRadio[swapTypeRows.length];
			
			for(int i = 0 ; i < swapTypes.length ; i++) {
				
				int j = i;				
			  	swapTypeRadios[i] = swapTypeRows[i].new CSRadio(
			  		swapTypes[i].formattedName() , 
			  		swapTypes[i] == animation().defaultSwapType() , 
			  		() -> animation().defaultSwapType(swapTypes[j])
			  	);
			  	
			}
			
			CSRadio.groupAll(swapTypeRadios);
			
		});
		
		topPartSections = ui.new CSRow(.80f);
		topPartSections.pushWidth(0.34f);
		topPartSections.pushWidth(0.33f);
		topPartSections.pushWidth(0.33f);
		
		CSGroup optionGroup = topPartSections.new CSGroup("Options");
		optionGroup.ui.options = UI_TITLED|UI_BORDERED;

		CSRow topRow = optionGroup.ui.new CSRow(30);
		topRow.pushWidth(30);
		CSButton 
			playButton = topRow.new CSButton(SYMBOL_TRIANGLE_RIGHT , this::togglePlay) ,
			stopButton = topRow.new CSButton(SYMBOL_RECT_SOLID , this::togglePlay);
		
		topRow.doLayout = doLayout; 
		playButton.doLayout = () -> animation() != null && !animation().playing();
		stopButton.doLayout = () -> animation() != null && animation().playing();
				
		frameTimeRow = optionGroup.ui.new CSDynamicRow();		
		frameTimeRow.doLayout = doLayout;
		
		frameUpdatesRow = optionGroup.ui.new CSDynamicRow();
		frameUpdatesRow.doLayout = doLayout;
				
		CSDynamicRow statsRow = optionGroup.ui.new CSDynamicRow();
		statsRow.doLayout = doLayout;
		
		for(int i = 0 ; i < swapTypes.length ; i++) (swapTypeRows[i] = optionGroup.ui.new CSDynamicRow()).doLayout = () -> animation() != null;
		
		statsRow.new CSText(() -> "Dimensions (WxH): " + animation().frameWidth() + ", " + animation().frameHeight() , textOptions);
		
		CSDynamicRow buttonsRow = optionGroup.ui.new CSDynamicRow(30);
		buttonsRow.doLayout = doLayout;
		buttonsRow.new CSButton("Reset Frame View" , () -> {
			
			zoom = 0.3f ; xTranslation = 0 ; yTranslation = 0;
			
		});
		
		optionGroup.ui.new CSDynamicRow(20).new CSText("Frames" , textOptions).doLayout = doLayout;
		
		optionGroup.ui.attachedLayout((context , stack) -> {

			if(!doLayout.getAsBoolean()) return;
			
			CSRefInt currentAnimationFrameIndex = new CSRefInt(0);
			Animation animation = animation();		
			
			animation.forAllArtboards(artboard -> {
				
				int current = currentAnimationFrameIndex.intValue();
				currentAnimationFrameIndex.inc();
				
				boolean isActiveFrame = animation().currentFrameIndex() == current;
				
				int dropdownSymbol = isActiveFrame ? SYMBOL_TRIANGLE_DOWN : SYMBOL_TRIANGLE_RIGHT;
				
				nk_layout_row_begin(context , NK_DYNAMIC , 20 , 1);
				nk_layout_row_push(context , .33f);
				if(nk_selectable_symbol_text(
					context , 
					dropdownSymbol , 
					"Artboard " + artboard.name , 
					TEXT_RIGHT , 
					toByte(stack , isActiveFrame))
				) animation().currentFrameIndex(current);
				
				nk_layout_row_end(context);
				
				if(!isActiveFrame) return;
				
				nk_layout_row_begin(context , NK_DYNAMIC , 30 , 3);
				nk_layout_row_push(context , .2f);
				nk_spacer(context);
				nk_layout_row_push(context , .4f);
				AnimationFrame frame = animation().getFrame(current);
				AnimationSwapType swap = frame.swapType();
				nk_text(context , "Swaps by: " + swap.shortenedName() , TEXT_LEFT|TEXT_CENTERED);
				nk_layout_row_push(context , .4f);				
				String rate;
				if(swap == AnimationSwapType.SWAP_BY_TIME) rate = "Rate (Millis): " + frame.time();
				else rate = "Rate: (Frames): " + frame.updates();
				nk_text(context , rate , TEXT_LEFT|TEXT_CENTERED);
				nk_layout_row_end(context);			
				
				nk_layout_row_begin(context , NK_DYNAMIC , 30 , 3);
				nk_layout_row_push(context , .2f);
				nk_spacer(context);
				nk_layout_row_push(context , .4f);
				if(nk_button_text(context , "Custom Time")) editor.startAnimationFrameCustomTimeInput(current);				
				nk_layout_row_push(context , .4f);
				if(nk_button_text(context , "Swap Type")) editor.startSetAnimationFrameSwapType(current);				
				nk_layout_row_end(context);
				
				nk_layout_row_begin(context , NK_DYNAMIC , 30 , 3);
				nk_layout_row_push(context , .2f);
				nk_spacer(context);
				nk_layout_row_push(context , .4f);
				if(nk_button_text(context , "Set Position")) editor.startSetAnimationFramePosition(current);
				nk_layout_row_push(context , .4f);
				if(nk_button_text(context , "Remove")) editor.eventPush(new ModifyArtboardInAnimationStatusEvent(project, artboard));
				nk_layout_row_end(context);
				
				//if this is a nondefault frame. The float comparison is how Java recommends to compare floats for equality
				if(
					(swap == AnimationSwapType.SWAP_BY_TIME && Float.compare(frame.time(), animation.defaultSwapTime().get()) != 0) ||
					(swap == AnimationSwapType.SWAP_BY_UPDATES && frame.updates() != animation.defaultUpdateAmount().intValue())
				) {
					
					nk_layout_row_begin(context , NK_DYNAMIC , 30 , 2);
					nk_layout_row_push(context , .2f);
					nk_spacer(context);
					nk_layout_row_push(context , .8f);
					if(nk_button_text(context , "Remove Custom Time")) {
					
						frame.time(animation.defaultSwapTime());
						frame.updates(animation.defaultUpdateAmount());
						
					}
					
					nk_layout_row_push(context , .4f);
				}				
			
			});
			
		});
		
		//this code attaches the code within the lambda to the element that will be placed next on thet top part section row. This is an
		//empty group that will contain the frame of the image.
		new UIAttachedElement(topPartSections , () -> {
			
			if(!nk_group_begin(nuklear.context() , "UNSEEN IDENTIFIER FOR SAFETY" , UI_BORDERED)) return;
			
			try(MemoryStack stack = MemoryStack.stackPush()) {
				
				//this part updates the variables tracking the position and size of the animation frame panel
				NkRect result = nk_window_get_content_region(nuklear.context() , NkRect.malloc(stack));
				
				framePanelX = (int) result.x();
				framePanelY = (int) result.y();
				framePanelWidth = (int) result.w();
				framePanelHeight = (int) result.h();
				
			}
			
			nk_group_end(nuklear.context());
						
		});
		
		CSGroup secondOptionGroup = topPartSections.new CSGroup("Select");
		secondOptionGroup.ui.options = UI_TITLED|UI_BORDERED;
		
		secondOptionGroup.ui.attachedLayout((context , stack) -> {
		
			if(!doLayout.getAsBoolean()) return;
			
			CSRefInt counter = new CSRefInt(0);
			
			//get each artboard and allow it to be part of this animation
			//If no artboards are currently part of this animation, we will allow any artboard to be selected. Once one is selected, this
			//part should only display artboards of an equal width and height.
			project.forValidArtboardsForAnimation(animation() , artboard -> {
							
				nk_layout_row_begin(context , NK_DYNAMIC , 30 , 2);
				nk_layout_row_push(context , 0.75f);
				nk_text(context , "Artboard " + artboard.name , textOptions);
				nk_layout_row_push(context , 0.25f);
				if(nk_button_text(context , "Add")) editor.eventPush(new ModifyArtboardInAnimationStatusEvent(project , artboard));
				
				nk_layout_row_end(context);
				
				counter.inc();
				
			});
			
		});
		
		CSDynamicRow playbackBarRow =  ui.new CSDynamicRow(0.25f);
		playbackBarRow.doLayout = doLayout;
		
		//this is the playback bar, used to visualize the progress through the animation when it is playing
		new ProgressBar(
			nuklear , 
			playbackBarRow , 
			() -> (long) animation().elapsedTime() << 10 , 
			() -> (long) animation().totalTime() << 10 , 
			true
		);
		
	}

	/**
	 * Toggles on or off the animation panel.
	 */
	public void toggleShow() {
		
		show = !show;
		
		if(!show) nuklear.removeUserInterface(ui);
		else { 
			
			nuklear.addUserInterface(ui);
			//Needed because if this UI is not in the nuklear's list of UI elements it wont be resized when the window resizes.
			ui.computePositionsAndDimensions();
			topPartSections.computeValues();
			frameTimeRow.computeValues();
			
		}
		
	}
	
	/**
	 * Shows the UI element.
	 */
	public void show() {
		
		show = true;
		
	}
	
	/**
	 * Hides the UI element.
	 */
	public void hide() {
		
		show = false;
		
	}
	
	/**
	 * Returns an array containing the top left point of the frame panel.
	 * 
	 * @return Array containing the top left coordinates of the animation frame panel.
	 */
	public int[] topLeftPointOfAnimationFrameSlot() {
		
		return new int[] {framePanelX , framePanelY};
		
	}
	
	/**
	 * Returns the very middle of the UI element, which will be the appropriate point to move the Artboard to.
	 * 
	 * @return Midpoint of the animation frame slot.
	 */
	public int[] midpointToAnimationFrameSlot() {

		return new int[] {framePanelX + (framePanelWidth / 2) , framePanelY + (framePanelHeight / 2)};
		
	}
	
	/**
	 * Returns the dimensions of the animation frame slot, i.e., the scissor dimensions for the slot.
	 * 
	 * @return Dimensions of the scissor space.
	 */
	public int[] dimensionsOfAnimationFrameSlot() {

		return new int[] {framePanelWidth , framePanelHeight};
		
	}
	
	/**
	 * Returns whether this animation panel is showing. 
	 * 
	 * @return {@code true} if this animation panel is showing.
	 */
	public boolean showing() {
		
		return show;
		
	}
	
	/**
	 * Returns the zoom value of the animation panel.
	 * 
	 * @return Zoom of the animation panel.
	 */
	public float zoom() {
		
		return zoom;
		
	}
	
	/**
	 * Zooms in or out the animation panel.
	 * 
	 * @param out — {@code true} if the panel should zoom out
	 */
	public void zoom(boolean out) {

		//0.1f is the default zoom factor for CSOrthographic camera, so we use it here
		float zoomMod = zoom * 0.1f; 
		if(!out) zoomMod = -zoomMod;
		zoom += zoomMod;
		
	}
	
	/**
	 * Returns the matrix used for the translation of the animation panel.
	 * 
	 * @return Matrix for translating an animation panel.
	 */
	public Matrix4f moveToMatrix() {
		
		//make it identity so the user can move it into position
		return moveToPoint.identity();
		
	}
	
	/**
	 * Returns the x translation of the animation panel frame.
	 * 
	 * @return X translation of the animation panel frame.
	 */
	public float xTranslation() {
		
		return xTranslation;
		
	}

	/**
	 * Returns the y translation of the animation panel frame.
	 * 
	 * @return Y translation of the animation panel frame.
	 */
	public float yTranslation() {
		
		return yTranslation;
		
	}
	
	/**
	 * Translates the animation panel frame. 
	 * 
	 * @param x — x translation value
	 * @param y — y translation value
	 */
	public void translate(float x , float y) {
		
		xTranslation += x;
		yTranslation += y;
		
	}

	private Animation animation() {
		
		if(project == null) return null;
		return project.currentAnimation();
		
	}
	
	private void togglePlay() {
		
		if(animation() != null && animation().numberFrames() > 0) animation().togglePlaying();
		
	}

	/**
	 * Returns the x position of the animation panel itself.
	 * 
	 * @return X position of the animation panel.
	 */
	public int xPosition() {
		
		return ui.xPosition();
				
	}
	
	/**
	 * Returns the y position of the animation panel itself.
	 * 
	 * @return Y position of the animation panel.
	 */
	public int yPosition() {
		
		return ui.yPosition();
		
	}
	
	@Override public void shutDown() {

		if(!show) toggleShow();
		
	}

	@Override public boolean isFreed() {

		return ui.isFreed();
		
	}
	
}
