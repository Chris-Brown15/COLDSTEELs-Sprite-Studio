/**
 * 
 */
package cs.csss.engine;

import java.util.Objects;

/**
 * Storage for channel values of a color. This is essentially a mutable color value. The bytes that make of a color are stored here and can be 
 * changed.
 */
public class ChannelBuffer implements ColorPixel {

	/**
	 * Convers the given pixel to a channel store. If {@code pixel} is already {@code instanceof ChannelStore}, it is casted and returned. 
	 * Otherwise, a new channel store is created with the same contents and returned.
	 * 
	 * @param pixel — source pixel
	 * @return {@code ChannelStore} view of {@code pixel}.
	 */
	public static ChannelBuffer asChannelStore(ColorPixel pixel) {
		
		if(pixel instanceof ChannelBuffer asChannelStore) return asChannelStore;
		return new ChannelBuffer(pixel.r() , pixel.g() , pixel.b() , pixel.a());
		
	}
	
	private volatile byte r , g , b , a;
	
	/**
	 * Creates a new channel store.
	 */
	public ChannelBuffer() {
		
	}

	/**
	 * Creates a new channel store with the given initial values.
	 * 
	 * @param r — initial red channel value
	 * @param g — initial green channel value
	 * @param b — initial blue channel value
	 * @param a — initial alpha channel value
	 */
	public ChannelBuffer(byte r , byte g , byte b , byte a) {
		
		set(r , g , b , a);
		
	}
	
	/**
	 * Sets this channel store to the given values.
	 * 
	 * @param r — initial red channel value
	 * @param g — initial green channel value
	 * @param b — initial blue channel value
	 * @param a — initial alpha channel value
	 */
	public synchronized void set(byte r , byte g , byte b , byte a) {

		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		
	}

	/**
	 * Sets the channel values of this pixel to {@code other}.
	 * 
	 * @param other — another color pixel
	 */
	public synchronized void set(ColorPixel other) {
		
		Objects.requireNonNull(other);
		
		set(other.r() , other.g() , other.b() , other.a());
		
	}
	
	/**
	 * Sets the red channel value of this pixel.
	 * 
	 * @param r — new red channel value
	 */
	public synchronized void r(byte r) {
		
		this.r = r;
		
	}

	/**
	 * Sets the green channel value of this pixel.
	 * 
	 * @param g — new green channel value
	 */
	public synchronized void g(byte g) {
		
		this.g = g;
		
	}

	/**
	 * Sets the blue channel value of this pixel.
	 * 
	 * @param b — new blue channel value
	 */
	public synchronized void b(byte b) {
		
		this.b = b;
		
	}

	/**
	 * Sets the alpha channel value of this pixel.
	 * 
	 * @param a — new alpha channel value
	 */
	public synchronized void a(byte a) {
		
		this.a = a;
		
	}
	
	/**
	 * Sets the channel value indexed by {@code i}. 
	 *  
	 * @param i — index of a color value to set
	 * @param color — color value to store
	 */
	public synchronized void i(byte i , byte color) {
		
		switch(i) {
			case 0 -> r = color;
			case 1 -> g = color;
			case 2 -> b = color;
			case 3 -> a = color;
		}
		
	}
	
	@Override public byte r() {
		
		return r;
	
	}

	@Override public byte g() {
		
		return g;
	
	}

	@Override public byte b() {
		
		return b;
	
	}

	@Override public byte a() {

		return a;

	}

	@Override public short ur() {
		
		return (short)Byte.toUnsignedInt(r);
	
	}

	@Override public short ug() {
		
		return (short)Byte.toUnsignedInt(g);
	
	}

	@Override public short ub() {
		
		return (short)Byte.toUnsignedInt(b);
	
	}

	@Override public short ua() {
		
		return (short)Byte.toUnsignedInt(a);
	
	}

	@Override public String toString() {
		return "ChannelBuffer [r = " + ur() + ", g = " + ug() + ", b = " + ub() + ", a = " + ua() + "]";
	}

	@Override public Pixel clone() {

		return new ChannelBuffer(r , g , b , a);
		
	}	

}
