/**
 * Licenced under MIT.
 */
package cs.sprites;

/**
 * Timer class using {@link java.lang.System#currentTimeMillis() System.currentTimeMillis} to track time.
 */
public class CSTimer {

	private double startTime;
	private boolean started = false;
	
	public boolean started() {
		
		return started;
		
	}
	
	/**
	 * Resets the timer, ending its status as running and setting its time value to 0.
	 */
	public void reset() {
		
		started = false;
		startTime = 0;
				
	}
	
	/**
	 * Starts the timer, causing it to track how much time has passed from the moment this method is called.
	 */
	public void start(){
		
		started = true;
		startTime = System.currentTimeMillis();
		
	}

	/**
	 * Returns the number of milliseconds that have elapsed from the time {@link CSTimer#start() start} was invoked.
	 * 
	 * @return Number of elapsed milliseconds since {@code start()}.
	 */
	public double elapsed(){

		return (System.currentTimeMillis() - startTime);

	}

}
