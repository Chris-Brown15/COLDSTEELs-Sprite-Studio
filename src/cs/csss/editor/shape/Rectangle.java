package cs.csss.editor.shape;

import java.nio.ByteBuffer;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.LookupPixel;
import cs.csss.misc.utils.FlexableGraphic;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.utils.Artboards;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Class for creating rectangular shapes.
 */
public class Rectangle extends Shape {
	
	/**
	 * Creates a new rectangle from the given parameters.
	 * 
	 * @param midX horizontal midpoint of this rectangle
	 * @param midY vertical midpoint of this rectangle
	 * @param width width of this rectangle
	 * @param height height of this rectangle
	 * @param defaultColor default color for the border and fill of this rectangle
	 * @param channelsPerPixel channels per pixel of colors for this rectangle
	 * @param fill whether to initially fill this rectangle
	 * @param formatColors whether to reformat {@code borderColor} and {@code fillColor} according to the semantics of 
	 * 					   {@link Shape#formatColor(ColorPixel, ChannelBuffer, int)}
	 */
	public Rectangle(int midX , int midY , int width , int height , ColorPixel defaultColor , int channelsPerPixel , boolean fill, boolean formatColors) {

		this(midX , midY , width , height , defaultColor , defaultColor , channelsPerPixel , fill, formatColors);
		
	}

	/**
	 * Creates a new rectangle from the given parameters with the specified border color and fill color.
	 * 
	 * @param midX horizontal midpoint of this rectangle
	 * @param midY vertical midpoint of this rectangle
	 * @param width width of this rectangle
	 * @param height height of this rectangle
	 * @param borderColor color of the border of this rectangle
	 * @param fillColor color of the inside of this rectangle
	 * @param channelsPerPixel channels per pixel of colors for this rectangle
	 * @param fill whether to initially fill this rectangle
	 * @param formatColors whether to reformat {@code borderColor} and {@code fillColor} according to the semantics of 
	 * 					   {@link Shape#formatColor(ColorPixel, ChannelBuffer, int)}
	 */
	public Rectangle(
		int midX , 
		int midY , 
		int width , 
		int height , 
		ColorPixel borderColor , 
		ColorPixel fillColor , 
		int channelsPerPixel , 
		boolean fill, 
		boolean formatColors
	) {

		super(borderColor , fillColor , channelsPerPixel, formatColors);

		initializeRendererData(midX , midY , width , height);
		
		this.fill = fill;
		
		reset();

	}
	
	@RenderThreadOnly @Override public void reset() {
		
		byte z = (byte)0;
		FlexableGraphic graphic = new FlexableGraphic(textureWidth, textureHeight , channels() , new ChannelBuffer(z , z , z , z));
		
		ByteBuffer image = graphic.imageData();
		
		//vertical lines
		for(int row = 0 ; row < textureHeight ; row++) {
			
			int offset = row * textureWidth * channelsPerPixel;
			putColor(image , offset , borderColor);
			
			int rightOffsetAddend = ((textureWidth - 1) * channelsPerPixel);
			if(fill) {
				
				int iter = offset + channelsPerPixel;
				int end = offset + rightOffsetAddend;
				
				while(iter < end) {
				
					putColor(image , iter , fillColor);					
					iter += channelsPerPixel;
					
				}				
				
			}
			
			offset += rightOffsetAddend;
			putColor(image , offset , borderColor);
			
		}
		
		//horizontal lines                                  
		for(int col = 0 ; col < textureWidth ; col++) {            
			                                                
			//put the bottom point                          
			int off = col * channelsPerPixel;               
			putColor(image , off , borderColor);            
			                                                
			//put the top point                             
			off += (textureHeight - 1) * textureWidth * channelsPerPixel; 
			putColor(image , off , borderColor);            
			                                                
		}                                                   
		
		defaultReset(graphic , textureWidth , textureHeight);
		
	}

	@SuppressWarnings("unchecked") @Override public Rectangle copy() {

		Rectangle newRectangle = new Rectangle(
			(int)midX() , 
			(int)midY() , 
			textureWidth() , 
			textureHeight() , 
			borderColor , 
			channelsPerPixel , 
			fill, 
			false
		);
		
		newRectangle.borderColor(borderColor);
		newRectangle.fillColor(fillColor);
		
		return newRectangle;
		
	}

	@Override public void rasterize(Artboard target) {

//		defaultRasterize(target, width , height);

		CorrectedResult correct = Artboards.worldCoordinatesToCorrectArtboardCoordinates(
			target, 
			(int)leftX(), 
			(int)bottomY(), 
			textureWidth, 
			textureHeight
		);
		
		//image is completely out of bounds
		if(correct == null) {
			
			hide(true);
			return;
			
		}
		
		LookupPixel[][] region = new LookupPixel[textureHeight][textureWidth];

		ArtboardPalette owningPalette = target.layerOwningShape(this).palette(); 
		LookupPixel borderColorLookup;
		LookupPixel fillColorLookup;
		
		if(channelsPerPixel == 2) {

			if(fillColor.g() == 0 || !fill) fillColorLookup = null;
			else fillColorLookup = owningPalette.putOrGetColors(fillColor);

			if(borderColor.g() == 0) borderColorLookup = null;
			else borderColorLookup = owningPalette.putOrGetColors(borderColor);
		
		} else {

			if(fillColor.a() == 0 || !fill) fillColorLookup = null;
			else fillColorLookup = owningPalette.putOrGetColors(fillColor);

			if(borderColor.a() == 0) borderColorLookup = null;
			else borderColorLookup = owningPalette.putOrGetColors(borderColor);
		
		}
		
		//assume width and height >= 2.
		
		//bottom and top row
		int lastRow = textureHeight - 1;
		for(int col = 0 ; col < textureWidth ; col++) { 
		
			region[0][col] = borderColorLookup;
			region[lastRow][col] = borderColorLookup;
			
		}
				
		//iterates over every row except the first and last		
		for(int row = 1 ; row < lastRow ; row++) {
			
			region[row][0] = borderColorLookup;
			
			for(int col = 1 ; col < textureWidth - 1 ; col++) region[row][col] = fillColorLookup;
			
			region[row][textureWidth - 1] = borderColorLookup;
			
		}
		
		hide(true);
		
		target.putColorsInImage(correct, region);
	
	}
	
	@Override public Rectangle shapeWidth(int newWidth) {
		
		assert newWidth > 0;		
		textureWidth = newWidth;		
		return this;
		
	}

	@Override public Rectangle shapeHeight(int newHeight) {

		assert newHeight > 0;		
		this.textureHeight = newHeight;
		return this;
				
	}
	
	@Override public int shapeWidth() {
		
		return textureWidth;
		
	}
	
	@Override public int shapeHeight() {
		
		return textureHeight;
		
	}

	@Override public void dimensions(int x, int y) {
		
		shapeWidth(x);
		shapeHeight(y);
		
	}
		
	@Override public void moveTo(float x , float y) {
		
		super.moveTo(x , y);
		if(leftX() % 1 != 0) translation.translate(.5f , 0 , 0);		
		if(topY() % 1 != 0) translation.translate(0 , .5f , 0);
		
	}
	
	private void putColor(ByteBuffer destination , int offset , ColorPixel color) { 
	
		for(int i = 0 ; i < channelsPerPixel ; i++) destination.put(offset + i , color.i(i));
		
	}
	
	@Override public String toString() {
		
		return String.format("Rectangle at (%f , %f)", midX() , midY());
		
	}

}