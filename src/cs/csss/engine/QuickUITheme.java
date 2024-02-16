package cs.csss.engine;

import static org.lwjgl.opengl.GL30C.glClearColor;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import cs.core.utils.ShutDown;
import cs.csss.misc.utils.MiscUtils;

import static org.lwjgl.nuklear.Nuklear.*;

import java.util.Objects;

class QuickUITheme implements ShutDown {

	private NkContext context;
	private NkColor.Buffer colors;
	private NkColor background;
	
	public QuickUITheme(
		NkContext context ,
		NkColor background ,
		NkColor text,
		NkColor window,
		NkColor header,
		NkColor border ,
		NkColor button ,
		NkColor buttonHover ,
		NkColor buttonActive ,
		NkColor toggle ,
		NkColor toggleHover, 
		NkColor toggleCursor ,
		NkColor select ,
		NkColor selectActive ,
		NkColor slider ,
		NkColor sliderCursor ,
		NkColor sliderCursorHover,
		NkColor sliderCursorActive ,
		NkColor property, 
		NkColor edit,
		NkColor editCursor,
		NkColor combo,
		NkColor chart,
		NkColor chartColor,
		NkColor chartColorHighlight,
		NkColor scrollbar,
		NkColor scrollbarCursor,
		NkColor scrollbarCursorHover,
		NkColor scrollbarCursorActive,
		NkColor tabHeader
	) {
	
		this.context = Objects.requireNonNull(context);
		this.background = NkColor.malloc().set(background);
		
		int nonnull = MiscUtils.numberNonNull(
			text,                 
			window,               
			header,               
			border ,              
			button ,              
			buttonHover ,         
			buttonActive ,        
			toggle ,              
			toggleHover,          
			toggleCursor ,        
			select ,              
			selectActive ,        
			slider ,              
			sliderCursor ,        
			sliderCursorHover,    
			sliderCursorActive ,  
			property,             
			edit,                 
			editCursor,           
			combo,                
			chart,                
			chartColor,           
			chartColorHighlight,  
			scrollbar,            
			scrollbarCursor,      
			scrollbarCursorHover, 
			scrollbarCursorActive,
			tabHeader
		);
		
		colors = NkColor.calloc(nonnull);
		
		colors.put(NK_COLOR_TEXT, text);
        colors.put(NK_COLOR_WINDOW, window);
        colors.put(NK_COLOR_HEADER, header);
        colors.put(NK_COLOR_BORDER, border);
        colors.put(NK_COLOR_BUTTON, button);
        colors.put(NK_COLOR_BUTTON_HOVER, buttonHover);
        colors.put(NK_COLOR_BUTTON_ACTIVE, buttonActive);
        colors.put(NK_COLOR_TOGGLE, toggle);
        colors.put(NK_COLOR_TOGGLE_HOVER, toggleHover);
        colors.put(NK_COLOR_TOGGLE_CURSOR, toggleCursor);
        colors.put(NK_COLOR_SELECT, select);
        colors.put(NK_COLOR_SELECT_ACTIVE, selectActive);
        colors.put(NK_COLOR_SLIDER, slider);
        colors.put(NK_COLOR_SLIDER_CURSOR, sliderCursor);
        colors.put(NK_COLOR_SLIDER_CURSOR_HOVER, sliderCursorHover);
        colors.put(NK_COLOR_SLIDER_CURSOR_ACTIVE, sliderCursorActive);
        colors.put(NK_COLOR_PROPERTY, property);
        colors.put(NK_COLOR_EDIT, edit);
        colors.put(NK_COLOR_EDIT_CURSOR, editCursor);
        colors.put(NK_COLOR_COMBO, combo);
        colors.put(NK_COLOR_CHART, chart);
        colors.put(NK_COLOR_CHART_COLOR, chartColor);
        colors.put(NK_COLOR_CHART_COLOR_HIGHLIGHT, chartColorHighlight);
        colors.put(NK_COLOR_SCROLLBAR, scrollbar);
        colors.put(NK_COLOR_SCROLLBAR_CURSOR, scrollbarCursor);
        colors.put(NK_COLOR_SCROLLBAR_CURSOR_HOVER, scrollbarCursorHover);
        colors.put(NK_COLOR_SCROLLBAR_CURSOR_ACTIVE, scrollbarCursorActive);
        colors.put(NK_COLOR_TAB_HEADER, tabHeader);
		
	}
	
	public void set() {

        nk_style_from_table(context , colors);
        if(background != null) { 
        
        	glClearColor(background.r() / 255f, background.g() / 255, background.b() / 255, background.a() / 255);
        	
        }
        
	}

	@Override public void shutDown() {

		if(isFreed()) return;
		colors.free();
		colors = null;
		
	}

	@Override public boolean isFreed() {

		return colors == null;
		
	}

}
