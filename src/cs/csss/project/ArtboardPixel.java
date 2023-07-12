package cs.csss.project;

public class ArtboardPixel {

	public final int
		localX ,
		localY ,
		pixelXIndex , 
		pixelYIndex , 
		lookupXIndex , 
		lookupYIndex 
	; 
	
	public final short 
		red , 
		green , 
		blue , 
		alpha
	;
	
	private boolean removed = false;
		
	ArtboardPixel(
		int localX , 
		int localY , 
		int pixelXIndex , 
		int pixelYIndex , 
		int lookupXIndex , 
		int lookupYIndex , 
		short red , 
		short green , 
		short blue , 
		short alpha
	) {
	
		this.localX = localX;
		this.localY = localY;
		this.pixelXIndex = pixelXIndex;
		this.pixelYIndex = pixelYIndex;
		this.lookupXIndex = lookupXIndex;
		this.lookupYIndex = lookupYIndex;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;				
		
	}
		
	void markRemoved() {
		
		removed = true;
		
	}
	
	public boolean removed() {
		
		return removed;
		
	}
	
}
