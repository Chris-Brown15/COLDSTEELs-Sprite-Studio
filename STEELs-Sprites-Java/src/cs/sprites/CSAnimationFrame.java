/**
 * Licenced under MIT.
 */
package cs.sprites;

/**
 * Container for animation frame data.
 */
public record CSAnimationFrame(float time , int updates , byte swapType) {
	
	public static final byte 
		SWAP_BY_TIME = 0 ,
		SWAP_BY_UPDATES = 1
	;
		
}
