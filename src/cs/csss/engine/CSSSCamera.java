package cs.csss.engine;

import cs.core.graphics.CSOrthographicCamera;

/**
 * Camera decorator allowing the user to get the zoom level.
 * 
 */
public class CSSSCamera extends CSOrthographicCamera {

	public static CSSSCamera centeredCamera(int viewWidth , int viewHeight , float worldXMid , float worldYMid) {
		
		CSSSCamera camera = new CSSSCamera(viewWidth / 2 , viewHeight / 2);
		camera.translate(-worldXMid, -worldYMid);
		return camera;
		
	}
	
	public CSSSCamera(int windowWidth, int windowHeight) {
		
		super(windowWidth, windowHeight);
			
	}

	public CSSSCamera(int windowWidth, int windowHeight, int nearPlane, int farPlane, float zoomFactor) {
		
		super(windowWidth, windowHeight, nearPlane, farPlane, zoomFactor);
		
	}
	
	public float zoom() {
		
		return zoom;
		
	}

	public void translate(float x , float y) {
		
		translate(x , y , 0);
		
	}
	
}
