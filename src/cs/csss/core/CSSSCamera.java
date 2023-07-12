package cs.csss.core;

import cs.core.graphics.CSOrthographicCamera;

/**
 * Camera decorator allowing the user to get the zoom level.
 * 
 */
public class CSSSCamera extends CSOrthographicCamera {

	public CSSSCamera(int windowWidth, int windowHeight) {
		
		super(windowWidth, windowHeight);
			
	}

	public CSSSCamera(int windowWidth, int windowHeight, int nearPlane, int farPlane, float zoomFactor) {
		
		super(windowWidth, windowHeight, nearPlane, farPlane, zoomFactor);
		
	}
	
	public float zoom() {
		
		return zoom;
		
	}

}