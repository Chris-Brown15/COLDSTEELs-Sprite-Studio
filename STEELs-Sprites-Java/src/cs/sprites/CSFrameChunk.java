/**
 * Licenced under MIT.
 */
package cs.sprites;

/**
 * Helper record for CTSAFile to store data loaded from disk.
 */
public record CSFrameChunk(float time , int updates , byte swapType) {}
