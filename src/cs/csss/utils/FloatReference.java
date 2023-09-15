package cs.csss.utils;

/**
 * Contains a float value, allowing for passing a float by reference.
 */
public class FloatReference {

	private float value;
	
	/**
	 * Creates a new float reference with the given value.
	 * 
	 * @param value — initial value of this float reference
	 */
	public FloatReference(float value) {

		this.value = value;
		
	}
	
	/**
	 * Creates a float value with the initial value of 0f.
	 */
	public FloatReference() {
		
		this(0);
		
	}

	/**
	 * Adds 1 to this float.
	 */
	public void inc() {
		
		value++;
		
	}
	
	/**
	 * Adds {@code addend} to this float.
	 * 
	 * @param addend — an amount to add to this float
	 */	
	public void add(float addend) {
		
		value += addend;
		
	}

	/**
	 * Subtracts {@code subtrahend} from this float.
	 * 
	 * @param addend — an amount to subtract from this float
	 */	
	public void sub(float subtrahend) {
		
		value -= subtrahend;
		
	}
	
	/**
	 * Multiplies this float by {@code factor}.
	 * 
	 * @param factor — an amount to multiply this float by
	 */
	public void mul(float factor) {
		
		value *= factor;
		
	}
	
	/**
	 * Divides this float by {@code divisor}.
	 * 
	 * @param divisor — an amount to divide this float by
	 */
	public void div(float divisor) {
		
		value /= divisor;
		
	}
	
	/**
	 * Sets this float value to {@code newValue}.
	 * 
	 * @param newValue — new value for this float
	 */
	public void set(float newValue) {
		
		this.value = newValue;
		
	}
	
	/**
	 * Returns the value of this float.
	 * 
	 * @return Value of this float.
	 */
	public float get() {
		
		return value;
		
	}

}
