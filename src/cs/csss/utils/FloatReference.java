package cs.csss.utils;

public class FloatReference {

	private float value;
	
	public FloatReference(float value) {

		this.value = value;
		
	}
	
	public FloatReference() {
		
		this(0);
		
	}

	public void inc() {
		
		value++;
		
	}
	
	public void add(float addend) {
		
		value += addend;
		
	}
	
	public void sub(float subtrahend) {
		
		value -= subtrahend;
		
	}
	
	public void mul(float factor) {
		
		value *= factor;
		
	}
	
	public void div(float divisor) {
		
		value /= divisor;
		
	}
	
	public void set(float newValue) {
		
		this.value = newValue;
		
	}
	
	public float get() {
		
		return value;
		
	}

}
