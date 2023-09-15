package cs.csss.engine;

import cs.core.graphics.CSOrthographicCamera;

/**
 * Camera decorator allowing the user to get the zoom level.
 * 
 */
public class CSSSCamera extends CSOrthographicCamera {

	/**
	 * Creates a camera that is also initialized by being moved to the given coordinates. 
	 * 
	 * @param viewWidth — width of the view
	 * @param viewHeight — height of the view
	 * @param worldXMid — x midpoint of the region
	 * @param worldYMid — y midpoint of the region
	 * @return Newly created camera.
	 */
	public static CSSSCamera centeredCamera(int viewWidth , int viewHeight , float worldXMid , float worldYMid) {
		
		CSSSCamera camera = new CSSSCamera(viewWidth / 2 , viewHeight / 2);
		camera.translate(-worldXMid, -worldYMid);
		return camera;
		
	}
	
	/**
	 * Creates a camera.
	 * 
	 * @param viewWidth — width of the view
	 * @param viewHeight — height of the view
	 */
	public CSSSCamera(int windowWidth, int windowHeight) {
		
		super(windowWidth, windowHeight);
			
	}

	/**
	 * Creates a camera.
	 * 
	 * @param viewWidth — width of the view
	 * @param viewHeight — height of the view
	 * @param nearPlane — near viewing pane 
	 * @param farPlane — far viewing pane 
	 * @param zoomFactor — how much the camera zooms in or out somewhere between {@code 0 < zoomFactor < 1}
	 */
	public CSSSCamera(int windowWidth, int windowHeight, int nearPlane, int farPlane, float zoomFactor) {
		
		super(windowWidth, windowHeight, nearPlane, farPlane, zoomFactor);
		
	}
	
	/**
	 * Gets the zoom factor.
	 * 
	 * @return Zoom factor.
	 */
	public float zoom() {
		
		return zoom;
		
	}

	/**
	 * translates the camera by {@code (x , y)}
	 * 
	 * @param x — x translation
	 * @param y — y translation
	 */
	public void translate(float x , float y) {
		
		translate(x , y , 0);
		
	}
	
}
