package cs.csss.editor;

import static cs.core.graphics.StandardRendererConstants.*;

import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.joml.AxisAngle4f;
import cs.core.graphics.CSGLSL;
import cs.core.graphics.CSOrthographicCamera;
import cs.core.graphics.CSRender;
import cs.core.graphics.CSTexture;
import cs.core.graphics.CSVAO;
import cs.core.graphics.utils.VertexBufferBuilder;
import cs.core.utils.Lambda;
import cs.core.utils.ShutDown;
import cs.core.utils.files.CSGraphic;
import cs.csss.engine.CSSSCamera;
import cs.csss.engine.Position;
import cs.csss.engine.TransformPosition;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.project.CSSSShader;
import cs.csss.project.IndexTexture;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.project.NonVisualLayer;
import cs.csss.project.VisualLayer;
import cs.csss.project.io.ImageGrabber;

/**
 * Class extending {@code CSRender} used for rendering sub regions of an artboard.
 * 
 * <p>
 * 	This class is a {@code CSRender}, which means its {@code shutDown} method will make steps toward freeing any GPU resources attached to
 * 	this object. However, the palette texture needs to last long beyond the lifetime of an instance of this, so it cannot be a member of the
 * 	textures array for this object. This is why it is not part of the textures array. In addition, the shader given is likely a the 
 * 	palette shader. This shader is supposed to upload multiple uniforms when it is used, but that doesn't happen here. It is ok however, 
 * 	because these {@code SelectionAreaRender}s are always rendered after all artboards. Therefore, the uniform values of the palette shader
 * 	are still correct when it is used to render one of these.
 * </p>
 * 
 */
public class SelectionAreaRender extends CSRender implements ShutDown {

	private ArtboardPalette palette;
	private CSSSShader shader;
	
	public final TransformPosition positions;

	public final int 
		startingLeftX ,
		startingBottomY ,
		width , 
		height;
	
	private final LayerPixel[][] regionContents;
	
	private final CSOrthographicCamera camera;
	
	public SelectionAreaRender(
		CSSSShader shader ,
		int startingLeftX ,
		int startingBottomY ,
		int width, 
		int height , 
		int moveToX , 
		int moveToY , 
		LayerPixel[][] region ,
		ArtboardPalette palette ,
		CSOrthographicCamera camera
	) {
		
		this.startingLeftX = startingLeftX;
		this.startingBottomY = startingBottomY;
		this.width = width;
		this.height = height;
		this.shader = shader;
		this.camera = camera;
		this.regionContents = region;
		
		ByteBuffer contents = texels();
		
		VertexBufferBuilder bufferBuilder = new VertexBufferBuilder(POSITION_2D|UV);
		bufferBuilder.size(width , height);
		
		vao = new CSVAO(bufferBuilder.attributes , STATIC_VAO , bufferBuilder.get());
		vao.drawAsElements(6, UINT);
		
		positions = new TransformPosition(bufferBuilder.attribute(POSITION_2D)); 
				
		TexelSubRegionGraphic graphic = new TexelSubRegionGraphic(contents , width , height);
		textures = new CSTexture[] {new CSTexture(graphic , IndexTexture.textureOptions)};
		graphic.shutDown();
			
		initializeVAO(vao);
		initializeShader(shader);
		initializeTextures(textures);
		this.palette = palette;
		
		positions.moveTo(moveToX , moveToY);
		
	}

	public LayerPixel[][] regionContents() {
		
		return regionContents;
		
	}
	
	public void drawDirect() {
		
		vao.activate();
		vao.draw();
					
	}
	
	@Override public void draw() {
		
		if(activate()) {

			palette.hideCheckeredBackground();
			
			shader.updatePassVariables(camera.projection() , camera.viewTranslation() , positions.translation);
						
			shader.updateTextures(palette, textures[0]);
			shader.activate();
			
			super.draw();
						
			palette.showCheckeredBackground();

		}
		
	}
	
	/**
	 * Returns the texel buffer that was passed to the constructor of this instance.
	 * 
	 * @return Texel buffer of this render.
	 */
	public ByteBuffer texels() {
				
		ByteBuffer regionTexelBuffer = memAlloc(width * height * IndexTexture.pixelSizeBytes);
		Layer.toByteBuffer(regionContents, regionTexelBuffer);		
		return regionTexelBuffer;
		
	}

	/**
	 * Hides the pixels from the selected region from the artboard to give the illusion they have been picked up.
	 * 
	 * @param current — the current artboard
	 */
	public void removeSectionFromArtboard(Artboard current , int leftX , int bottomY) {
	
		//remove the section of the artboard that we selected
		
		int[] indices = current.worldToPixelIndices(leftX , bottomY);

		int operationalWidth = width;
		int operationalHeight = height;
		if(indices[0] < 0) {
			
			// the right of the render is left of the left of the artboard, just return
			if(indices[0] + width < 0) return;		
			indices[0] = 0;
			
		}

		if(indices[1] < 0) {
			
			// the top of the render is below the bottom of the artboard
			if(indices[1] + height < 0) return;
			indices[1] = 0;
			
		}

		if(indices[0] + width > current.width()) {
			
			//the left of the render is past the right of the artboard
			if(indices[0] >= current.width()) return;
			operationalWidth -= (indices[0] + width) - current.width();
			
		}
		
		if(indices[1] + height > current.height()) {
		
			//the bottom of the render is above the top of the artboard
			if(indices[1] >= current.height()) return;
			operationalHeight -= (indices[1] + height) - current.height();
			
		}
		
		int maxRow = indices[1] + operationalHeight;
		int maxCol = indices[0] + operationalWidth;
		
		Layer active = current.activeLayer();
			
		if(current.isActiveLayerVisual()) {
			
			VisualLayer activeVisual = (VisualLayer)active;
			int activeLayerRank = current.getLayerRank(activeVisual);
			
			for(int row = indices[1] ; row < maxRow ; row++) for(int col = indices[0] ; col < maxCol ; col++) {
				
				if(!activeVisual.containsModificationTo(col, row) || current.isUpperRankLayerModifying(activeLayerRank, col, row)) continue;
				
				//this is a layer who is lower ranked than the active one but higher than any others below it
				VisualLayer nextHighest = current.getHighestLowerRankLayerModifying(activeLayerRank, col, row);
				if(nextHighest == null) { 
					
					current.writeToIndexTexture(col, row, 1, 1, current.getBackgroundColor(col, row));
					continue;
					
				}
				
				//cant be null
				LayerPixel nextHighestModification = nextHighest.get(col, row);
				current.writeToIndexTexture(col, row, 1, 1, current.getColorPointedToByLayerPixel(nextHighestModification));
				
			}
			
		} else {
			
			NonVisualLayer activeNonVisual = (NonVisualLayer)active;
			
			for(int row = indices[1] ; row < maxRow ; row++) for(int col = indices[0] ; col < maxCol ; col++) {
				
				if(!activeNonVisual.containsModificationTo(col, row)) continue;
				current.writeToIndexTexture(col, row, 1, 1, current.getBackgroundColor(col, row));
								
			}			
			
		}		

		active.remove(indices[0], indices[1] , operationalWidth , operationalHeight);
		
	}
	
	/**
	 * Rotates this render.
	 * 
	 * @param degrees — degrees to rotate
	 */
	public void rotate(float degrees) {
		
		positions.translation.rotate((float)Math.toRadians(degrees) , 0f , 0f , 1.0f);
		
		
	}
	
	/**
	 * Returns the rotation angle in radians this render has been rotated.
	 * 
	 * @return Rotation angle in radians this object has been rotated.
	 */
	public float rotationAngle() {
		
		AxisAngle4f angle = new AxisAngle4f();
		positions.translation.getRotation(angle);		
		return angle.angle;
		
	}
	
	/**
	 * Undoes the rotation transformation of this render.
	 */
	public void resetRotation() {
		
		rotate(-rotationAngle());
		
	}
	
	/**
	 * Gets this render's texture.
	 * 
	 * @return Texture of this render.
	 */
	public CSTexture texture() {
		
		return textures[0];
		
	}
	
	/**
	 * Draws this render with this given parameters.
	 * 
	 * @param camera — camera to render with
	 * @param shader  — shader to render with
	 * @param swapBuffers — callback for swapping buffers after draw, or {@code null} if buffer swap should occur after drawing
	 */
	public void draw(CSOrthographicCamera camera , CSSSShader shader , Lambda swapBuffers) {

		shader.updatePassVariables(camera.projection() , camera.viewTranslation() , positions.translation);
		shader.updateTextures(null, textures[0]);
		shader.activate();
		drawDirect();

		if(swapBuffers != null) swapBuffers.invoke();
		
	}
	
	/**
	 * Draws this render and returns a downloaded buffer from a custom framebuffer to which this render was drawn.
	 * 
	 * @param swapBuffers — callback for swapping buffers, must not be null
	 * @param width — width of the region to draw
	 * @param height — height of the region to draw
	 * @param midX — x coordinate where a camera should center on 
	 * @param midY — y coordinate where a camera should center on
	 * @return {@code ByteBuffer} containing the downloaded result of the render. A pixel of {@code (0 , 0)} notates a pixel the underlying
	 * 		   source texture does not modify; its a background pixel.
	 */
	public ByteBuffer renderAndDownload(Lambda swapBuffers , int width , int height , float midX , float midY) {
		
		ImageGrabber grabber = new ImageGrabber(
			null , 
			null , 
			() -> draw(CSSSCamera.centeredCamera(width, height, midX, midY) , CSSSProject.theTextureShader() , swapBuffers) , 
			width , 
			height
		);
		
		return grabber.renderImage(); 
		
	}

	/**
	 * Draws this render and returns a downloaded buffer from a custom framebuffer to which this render was drawn.
	 * 
	 * @param swapBuffers — callback for swapping buffers, must not be null
	 * @param width — width of the region to draw
	 * @param height — height of the region to draw
	 * @param position — object whose midpoint is used for centering the camera
	 * @return {@code ByteBuffer} containing the downloaded result of the render. A pixel of {@code (0 , 0)} notates a pixel the underlying
	 * 		   source texture does not modify; its a background pixel.
	 */
	public ByteBuffer renderAndDownload(Lambda swapBuffers , int width , int height , Position position) {
		
		return renderAndDownload(swapBuffers , width , height , position.midX() , position.midY()); 
		
	}
	
	@Override public void shutDown() {
		
		if(!vao.isFreed() || !textures[0].isFreed()) {
			
			textures[0].shutDown();
			vao.shutDown();
			
		}
		
	}
	
	public SelectionAreaRender() {
		
		throw new UnsupportedOperationException("Wrong constructor for this object");		
		
	}
	
	public SelectionAreaRender(CSVAO vao) {
		
		throw new UnsupportedOperationException("Wrong constructor for this object");		
		
	}

	public SelectionAreaRender(CSVAO vao, CSTexture... textures) {
		
		throw new UnsupportedOperationException("Wrong constructor for this object");		
		
	}

	public SelectionAreaRender(CSGLSL shader, CSTexture... textures) {
		
		throw new UnsupportedOperationException("Wrong constructor for this object");		
		
	}

	public SelectionAreaRender(CSVAO vertexArray, CSGLSL shader, CSTexture... textures) {
		
		throw new UnsupportedOperationException("Wrong constructor for this object");		
		
	}

	private static class TexelSubRegionGraphic implements CSGraphic {

		private ByteBuffer contents;
		
		private final int
			width ,
			height ,
			channels;
		
		public TexelSubRegionGraphic(ByteBuffer source , int width , int height) {
			
			Objects.requireNonNull(source);
			this.contents = source;
			this.width = width;
			this.height = height;
			channels = 2;
			
		}
		
		@Override public void shutDown() {

			if(isFreed()) return;
			
			memFree(contents);
			contents = null;
			
		}

		@Override public boolean isFreed() {

			return contents == null;
			
		}

		@Override public int width() {

			return width;

		}

		@Override public int height() {

			return height;
			
		}

		@Override public int bitsPerPixel() {

			return channels * 8;
			
		}

		@Override public int bitsPerChannel() {

			return 8;
			
		}

		@Override public int channels() {

			return channels;
			
		}

		@Override public ByteBuffer imageData() {

			return contents;
			
		}
		
	}
	
}
