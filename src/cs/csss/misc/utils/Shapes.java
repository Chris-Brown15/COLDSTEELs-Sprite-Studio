package cs.csss.misc.utils;

import static cs.core.graphics.StandardRendererConstants.*;

import static org.lwjgl.opengl.GL30.GL_POINTS;
import static org.lwjgl.opengl.GL30.glDrawElements;

import org.lwjgl.system.Configuration;

import cs.core.CSDisplay;
import cs.core.graphics.CSGLSL;
import cs.core.graphics.CSRender;
import cs.core.graphics.CSVAO;
import cs.core.graphics.ThreadedRenderer;
import cs.core.graphics.utils.GLSLTypes;
import cs.core.graphics.utils.GLSLVersions;
import cs.core.graphics.utils.ShaderBuilder;
import cs.core.graphics.utils.ShaderBuilder.GLSLFunction;
import cs.core.utils.threads.Await;

/**
 * 
 * Class designed to make it easy and efficient to draw primitive shapes such as points and lines.
 * 
 * @author littl
 *
 */
public class Shapes {
	
	private static CSGLSL 
		pointShader ,
		lineShader ,
		triangleShader ,
		quadShader ,
		circleShader  
	;
	
	public static void initialize(ThreadedRenderer renderer) {
		
		/*
		 * programmatically generate shaders and initialize them
		 */
		renderer.post(() -> {
			
			ShaderBuilder pointShaderVertex = new ShaderBuilder();
			
			GLSLFunction main = pointShaderVertex.startFunction("main")
				.addLine("gl_Position = vec4(vertexPosition , 0 , 1);")
			;
			
			pointShaderVertex
				.version(GLSLVersions.version_330 , true)
				.vertexLayout(POSITION_2D)
				.writeFunction(main)
			;
			
			ShaderBuilder pointShaderFragment = new ShaderBuilder();
			
			main = pointShaderFragment.startFunction("main")
				.addLine("pixel = vec4(1.0f , 0.0f , 0.0f , 1.0f);")
			;
			
			pointShaderVertex
				.version(GLSLVersions.version_330, true)
				.output(GLSLTypes.vec4 , "pixel")
				.writeFunction(main)
			;
			
		}).await();
		
	}
	
	public static void initialize(
		CSGLSL _pointShader , 
		CSGLSL _lineShader , 
		CSGLSL _triangleShader , 
		CSGLSL _quadShader , 
		CSGLSL _circleShader
	) {
		
		pointShader = _pointShader;
		lineShader = _lineShader;
		triangleShader = _triangleShader;
		quadShader = _quadShader;
		circleShader = _circleShader;  
		
	}

	public static CSVAO point(int usageHint , float xPosition , float yPosition) {

		CSVAO pointVAO = new CSVAO(POSITION_2D , usageHint , new float[] {xPosition , yPosition});
//		pointVAO.drawOverload(() -> glDrawElements(GL_POINTS , 0 , 1));
		return pointVAO;
		
	}
	

	public static CSRender point() {
		
		return null;
		
	}
	
	public static CSRender line() {
		
		return null;
		
	}
	
	public static CSRender triangle() {
		
		return null;
		
	}
	
	public static CSRender quad() {
		
		return null;
		
	}
	
	public static CSRender circle() {
		
		return null;
		
	}
	
	private Shapes() {}

	public static void main(String[] args) {
		
		Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
		
		CSDisplay display = new CSDisplay(true , "lol");
		
		Await render = display.renderer.post(display.renderer::render);
		
		CSRender point = new CSRender();
		
		while(display.persist()) {
			
			if(render.isFinished()) { 
			
				display.pollInputs();
				render = display.renderer.post(display.renderer::render);
				
			}
			
		}
		
		display.window.detachContext();
		
		display.shutDown();
		
		CSDisplay.finalShutDown();
		
	}
		
}
