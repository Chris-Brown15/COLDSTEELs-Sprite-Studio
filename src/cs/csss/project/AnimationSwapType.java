/**
 * 
 */
package cs.csss.project;

/**
 * Used to specify what means of swapping frames of a given animation should be employed. 
 * 
 * <p>
 * 	There are two ways to swap frames of an animation, that is to say, two ways to play an animation. The first is based on time. Here, 
 * 	each frame receives a number of milliseconds to be the active frame, and a timer keeps track of how long that frame has been active. 
 * 	Once the frame's swap time has been reached, the next frame may be displayed.
 * </p>
 * <p>
 * 	Alternatively, animations can oriented around frames, or updates. In this approach, an update method is invoked every frame and once 
 * 	it has been invoked a specified number of times, a new frame will be moved to. 
 *</p>
 */
public enum AnimationSwapType {

	SWAP_BY_TIME ,
	SWAP_BY_UPDATES ,
	;
	
	/**
	 * Used in the UI to display the name of the swap type.
	 * 
	 * @return Name of this swap type, replacing underscores with spaces and capitalizing the first letter of each word.
	 */
	public String formattedName() {
		
		String name = toString().toLowerCase();
		char[] chars = name.toCharArray();
		
		boolean capitalize = true;
		
		for(int i = 0 ; i < chars.length ; i++) {
			
			if(capitalize) {
				
				chars[i] = Character.toUpperCase(chars[i]);
				capitalize = false;
				
			}
			
			if(chars[i] == '_') { 
				
				chars[i] = ' ';
				capitalize = true;
				
			}
			
		}
		
		return new String(chars);
		
	}
	
	public String shortenedName() {
		
		char[] chars = toString().toLowerCase().toCharArray();
		for(int i = chars.length - 1 ; i >= 0 ; i--) if(chars[i] == '_') { 
			
			i++;
			chars[i] = Character.toUpperCase(chars[i]);
			return new String(chars , i , chars.length - i);
			
		}
		
		throw new IllegalArgumentException(toString() + " is not a valid name for an enumeration of Animation Swap Types");
		
	}
	
}
