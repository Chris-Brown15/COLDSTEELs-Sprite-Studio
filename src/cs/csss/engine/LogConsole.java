package cs.csss.engine;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_begin;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_push;
import static org.lwjgl.nuklear.Nuklear.nk_text;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;

import static cs.csss.ui.utils.UIUtils.textLength;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.data.CSLinkedRingBuffer;

public class LogConsole {

	private CSLinkedRingBuffer<Entry> entries;
	
	LogConsole(CSNuklear nuklear , int entryCapacity) {
	
		entries = new CSLinkedRingBuffer<>(entryCapacity);
		
		CSUserInterface ui = nuklear.new CSUserInterface("Log" , 140 , 40 , 300 , 600);
		ui.options = UI_TITLED|UI_BORDERED|UI_MOVABLE|UI_SCALABLE;

		ui.attachedLayout((context , stack) -> {
			
			entries.forEach(entry -> {
				
				nk_layout_row_begin(context , NK_STATIC , 20 , 1);
				nk_layout_row_push(context , entry.width);
				nk_text(context , entry.text , TEXT_LEFT);
				nk_layout_row_end(context);
				
			});
			
		});
		
		
	}
	
	public synchronized void putEntry(String entry) {
		
		entries.put(new Entry(entry , textLength(entry)));
		
	}

	private record Entry(String text , int width) {}
	
}