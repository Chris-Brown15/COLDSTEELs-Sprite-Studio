package cs.csss.project;

import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memCalloc;
import static org.lwjgl.system.MemoryUtil.memCallocInt;
import static org.lwjgl.system.MemoryUtil.memSet;
import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import cs.core.utils.ShutDown;
import cs.coreext.nanovg.NanoVGFrame;
import cs.coreext.nanovg.NanoVGTypeface;
import cs.coreext.nanovg.utils.OffHeapText;
import cs.csss.editor.SelectionAreaBounder;

public class VectorText implements ShutDown {

	private final SelectionAreaBounder bounder = new SelectionAreaBounder();	
	private OffHeapText source;
	private NanoVGTypeface typeface;
	private int 
		rowHeight ,
		charHeight;
	
	/*
	 * These buffers are here for the benefit of the UI elements that allow for editing the text of this vector text. 
	 */
	private ByteBuffer textEditorBuffer = memCalloc(999);
	private IntBuffer textLengthBuffer = memCallocInt(1);
	
	public VectorText(NanoVGTypeface typeface) {

		typeface(typeface);
		rowHeight = typeface.charHeight();
		charHeight = typeface.charHeight();
		
	}

	public VectorText(NanoVGTypeface typeface , String sourceText) {
		
		this(typeface);
		setSource(sourceText);
	
	}
	
	public void setSource(String text) {
		
		if(source != null) source.shutDown();
		if(text == null) text = "null";
			
		source = new OffHeapText(text);
				
		updateBuffers();
		
	}
	
	public void typeface(NanoVGTypeface typeface) {
		
		Objects.requireNonNull(typeface);
		
		this.typeface = typeface;
		
	}

	public void rowHeight(int rowHeight) {
		
		this.rowHeight = rowHeight;
		
	}
	
	public NanoVGTypeface typeface() {
		
		return typeface;
		
	}
	
	public OffHeapText text() {
		
		return source;
		
	}
	
	public int rowHeight() {	
		
		return rowHeight;
		
	}
	
	public void renderBoxAndText(NanoVGFrame frame) {
		
		renderBox(frame);
		renderText(frame); 
		
	}
	
	public void renderBox(NanoVGFrame frame) {
		
		bounder.render(frame);
		
	}
	
	public void renderText(NanoVGFrame frame) {
		
		int charHeightAddend = charHeight - typeface.charHeight(); 
		frame.setCurrentFontAndResize(typeface, charHeightAddend);
		if(source != null) { 
			
			frame
				.fillColor(bounder.color)
				.textBox(bounder.LX(), bounder.TY() - (charHeight / 2) , bounder.width(), rowHeight, source);
			
		}
	
	}
	
	public ByteBuffer textEditorBuffer() {
		
		return textEditorBuffer;
		
	}

	/**
	 * @return The length of the input buffer.
	 */
	public IntBuffer textLengthBuffer() {
		
		return textLengthBuffer;
	
	}

	public void setTextFromBuffers() {
		
		String text = memUTF8(textEditorBuffer.slice(0, textLengthBuffer.get(0)));
		setSource(text);
		
	}
	
	private void updateBuffers() {
		
		memSet(textLengthBuffer , 0);
		memSet(textEditorBuffer , 0);		
		
	}

	public int charHeight() {
		
		return charHeight;
		
	}

	public void charHeight(int charHeight) {
		
		this.charHeight = charHeight;
				
	}
	
	/**
	 * Moves the bounder of this text to the given coordinates.
	 * 
	 * @param moveToX — world x coordinate
	 * @param moveToY — world y coordinate
	 * @see cs.csss.editor.SelectionAreaBounder#moveTo(float, float)
	 */
	public void moveTo(float moveToX, float moveToY) {
		
		bounder.moveTo(moveToX, moveToY);
		
	}

	/**
	 * Moves a corner of the text bounder.
	 * 
	 * @param cursorX — x coordinate of the cursor in world space
	 * @param cursorY — y coordinate of the cursor in world space
	 * @see cs.csss.editor.SelectionAreaBounder#moveCorner(int, int)
	 */
	public void moveCorner(float cursorX, float cursorY) {
		
		bounder.moveCorner(cursorX, cursorY);
		
	}

	public int color() {
		
		return bounder.color;
		
	}
	
	public void color(int color) {
		
		bounder.color = color;
		
	}
	
	@Override public void shutDown() {

		if(isFreed()) return;
		source.shutDown();
		memFree(textLengthBuffer);
		memFree(textEditorBuffer);
		
	}

	@Override public boolean isFreed() {

		return source.isFreed();
		
	}

}
