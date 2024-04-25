package cs.csss.project;

import java.util.LinkedList;
import java.util.stream.Stream;

import cs.core.utils.ShutDown;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.shape.Ellipse;
import cs.csss.editor.shape.Rectangle;
import cs.csss.editor.shape.Shape;
import cs.csss.engine.CSSSCamera;
import cs.csss.engine.ColorPixel;

/**
 * Responsible for creating, managing, and freeing shapes.
 */
public class ShapeManager implements ShutDown {

	private LinkedList<Shape> shapes = new LinkedList<>();
	
	private boolean shutDown = false;
	
	ShapeManager() {}

 	@RenderThreadOnly synchronized Ellipse newEllipse(
 	 	Artboard source , 
 	 	int radiusX , 
 	 	int radiusY , 
 	 	ColorPixel borderColor ,
 	 	ColorPixel fillColor ,
 	 	int channelsPerPixel ,
 	 	boolean fill, 
 	 	boolean formatColors
 	 ) {
		
 		assert source != null : "No current artboard";
 		
		Ellipse ellipse = new Ellipse(
			source.midX() , 
			source.midY() , 
			radiusX , 
			radiusY , 
			borderColor ,
			fillColor ,
			channelsPerPixel , 
			fill, 
			formatColors
		);
		
		shapes.add(ellipse);
		
		return ellipse;
		
	}
	
 	@RenderThreadOnly synchronized Rectangle newRectangle(
 		Artboard source ,
 		int width , 
 		int height ,
 		ColorPixel borderColor ,
 		ColorPixel fillColor ,
 		int channelsPerPixel ,
 		boolean fill, 
 		boolean formatColors
 	) {
 		
 		assert source != null : "No current artboard.";
 		
 		Rectangle rectangle = new Rectangle(
 			(int)source.midX() , 
 			(int)source.midY() , 
 			width , 
 			height , 
 			borderColor ,
 			fillColor ,
 			channelsPerPixel , 
 			fill, 
 			formatColors
 		);
 		
 		shapes.add(rectangle);
 		
 		return rectangle;
 		
 	}

 	void add(Shape add) {
 		
 		assert add != null;
 		shapes.add(add);
 		
 	}

 	boolean remove(Shape delete) {
 		
 		assert delete != null;
 		 		 		
 		return shapes.remove(delete); 		
 		 		
 	}
 	
 	boolean contains(Shape shape) {
 		
 		assert shape != null;
 		return shapes.contains(shape);
 		
 	}
 	
 	Stream<Ellipse> ellipses() {
 		
 		return shapes.stream().filter(x -> x instanceof Ellipse).map(x -> (Ellipse)x);
 		
 	}
 	
 	Stream<Rectangle> rectangles() {
 		
 		return shapes.stream().filter(x -> x instanceof Rectangle).map(x -> (Rectangle)x);
 		
 	}
 	
 	Stream<Shape> shapes() {
 		
 		return shapes.stream();
 		
 	}
 	
 	/**
 	 * Renders all shapes in this manager with the given camera.
 	 *  
 	 * @param camera a camera to use for rendering shapes
 	 */
 	@RenderThreadOnly void renderShapes(CSSSCamera camera) {
 		
 		for(Shape x : shapes) x.render(camera);
 		
 	}
 	
	@RenderThreadOnly @Override public synchronized void shutDown() {

		if(isFreed()) return;
		
		shutDown = true;
		
		for(Shape x : shapes) if(x instanceof ShutDown asShutDown) asShutDown.shutDown();
		
	}

	@Override public synchronized boolean isFreed() {

		return shutDown;
		
	}
 	
}
