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
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.SelectionAreaBounder;

/**
 * Vector text powered by NanoVG. This class is not currently usable because it is unfinished.
 */
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
	
	/**
	 * Creates a vector text box from the given nanoVG typeface.
	 * 
	 * @param typeface — typeface for this text box
	 */
	public VectorText(NanoVGTypeface typeface) {

		typeface(typeface);
		rowHeight = typeface.charHeight();
		charHeight = typeface.charHeight();
		
	}

	/**
	 * Creates a vector text box from the given nanoVG typeface that will contain the given text.
	 * 
	 * @param typeface — typeface for this text box
	 * @param sourceText — text this text box will contain
	 */
	public VectorText(NanoVGTypeface typeface , String sourceText) {
		
		this(typeface);
		setSource(sourceText);
	
	}
	
	/**
	 * Sets the source text of this text box to {@code text}.
	 * 
	 * @param text — text this text box will dislpay
	 */
	public void setSource(String text) {
		
		if(source != null) source.shutDown();
		if(text == null) text = "null";
			
		source = new OffHeapText(text);
				
		updateBuffers();
		
	}
	
	/**
	 * Sets the typeface text of this text box will display as.
	 * 
	 * @param typeface — typeface text of this text box will display as 
	 */
	public void typeface(NanoVGTypeface typeface) {
		
		Objects.requireNonNull(typeface);
		
		this.typeface = typeface;
		
	}

	/**
	 * Sets the distance between rows of this text box.
	 * 
	 * @param rowHeight — height between rows of this text box
	 */
	public void rowHeight(int rowHeight) {
		
		this.rowHeight = rowHeight;
		
	}
	
	/**
	 * Returns the typeface of this text box.
	 * 
	 * @return Typeface of this text box.
	 */
	public NanoVGTypeface typeface() {
		
		return typeface;
		
	}
	
	/**
	 * Returns the offheap allocated text of this text box.
	 *  
	 * @return Offheap allocated text of this text box.
	 */
	public OffHeapText text() {
		
		return source;
		
	}
	
	/**
	 * Returns the distance between rows of this text box.
	 * 
	 * @return Distance between rows of this text box.
	 */
	public int rowHeight() {	
		
		return rowHeight;
		
	}
	
	/**
	 * Renders the text of this text box, and its bounder.
	 * 
	 * @param frame — the nanoVG frame for this application frame
	 */
	@RenderThreadOnly public void renderBoxAndText(NanoVGFrame frame) {
		
		renderBox(frame);
		renderText(frame); 
		
	}

	/**
	 * Renders just the bounder of this text box.
	 * 
	 * @param frame — the nanoVG frame for this application frame
	 */
	@RenderThreadOnly public void renderBox(NanoVGFrame frame) {
		
		bounder.render(frame);
		
	}
	
	/**
	 * Renders just the text of this text box.
	 * 
	 * @param frame — the nanoVG frame for this application frame
	 */
	@RenderThreadOnly public void renderText(NanoVGFrame frame) {
		
		int charHeightAddend = charHeight - typeface.charHeight(); 
		frame.setCurrentFontAndResize(typeface, charHeightAddend);
		if(source != null) { 
			
			frame
				.fillColor(bounder.color)
				.textBox(bounder.LX(), bounder.TY() - (charHeight / 2) , bounder.width(), rowHeight, source);
			
		}
	
	}
	
	/**
	 * Returns a byte buffer used to modify this text box.
	 * 
	 * @return Bytebuffer used to modify this text box.
	 */
	public ByteBuffer textEditorBuffer() {
		
		return textEditorBuffer;
		
	}

	/**
	 * Returns an int buffer containing a single integer that gives the length of the text input buffer.
	 * 
	 * @return The length of the input buffer.
	 */
	public IntBuffer textLengthBuffer() {
		
		return textLengthBuffer;
	
	}

	/**
	 * Sets the source text of this text box to the contents of the text editor buffer.
	 */
	public void setTextFromBuffers() {
		
		String text = memUTF8(textEditorBuffer.slice(0, textLengthBuffer.get(0)));
		setSource(text);
		
	}
	
	private void updateBuffers() {
		
		memSet(textLengthBuffer , 0);
		memSet(textEditorBuffer , 0);		
		
	}

	/**
	 * Returns the height of charaters of this text box.
	 * 
	 * @return Height of characters of this text box.
	 */
	public int charHeight() {
		
		return charHeight;
		
	}

	/**
	 * Sets the height of the characters of this text box.
	 * 
	 * @param charHeight — new height for characters of this text box
	 */
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

	/**
	 * Returns the color of the text and bounder for this text box.
	 * 
	 * @return Color of this text box.
	 */
	public int color() {
		
		return bounder.color;
		
	}
	
	/**
	 * Sets the color of the text and bounder of this text box.
	 * 
	 * @param color — new color of the text and bounder of this text box
	 */
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
