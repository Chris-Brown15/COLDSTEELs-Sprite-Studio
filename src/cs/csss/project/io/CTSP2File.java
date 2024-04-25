package cs.csss.project.io;

import static cs.csss.misc.files.FileOperations.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.joml.Vector2f;

import cs.csss.editor.line.BezierLine;
import cs.csss.editor.line.Line;
import cs.csss.editor.line.LinearLine;
import cs.csss.editor.shape.Ellipse;
import cs.csss.editor.shape.Rectangle;
import cs.csss.editor.shape.Shape;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.project.Layer;
import cs.csss.project.VisualLayer;

/**
 * Extension of {@link CTSPFile} which allows for saving and loading of shapes and lines.
 */
public class CTSP2File extends CTSPFile {

	/**
	 * Version for the project file.
	 */
	public static final int FILE_VERSION = 20240401;
	
	/**
	 * String written at the beginning of all CTSP2 files. 
	 */
	public static final String FILE_NAME = "CTSP2";
	
	/**
	 * File extension for all CTSP2 files, 
	 */
	public static final String FILE_EXTENSION = ".ctsp2"; 

	private ArtboardShapesAndLinesChunk[] artboardShapeAndLineChunks;
	
	/**
	 * Write constructor for a CTSP2 file. 
	 * 
	 * @param project project to write
	 * @param saveAs name for the resulting file
	 */
	public CTSP2File(CSSSProject project , String saveAs) {

		super(project , saveAs , FILE_EXTENSION);
		
	}

	/**
	 * Read constructor for a CTSP2 file.
	 * 
	 * @param fileName name of the file to load
	 */
	public CTSP2File(String fileName) {
		
		super(fileName , FILE_EXTENSION);
		
	}

	/**
	 * Returns the artboard shapes and lines chunks read from disk. A call {@link #read()} must precede this method. 
	 * 
	 * @return Array containing the artboard shape and line chunks.
	 */
	public ArtboardShapesAndLinesChunk[] artboardShapesAndLinesChunks() {
		
		verifyReadValid();
		return artboardShapeAndLineChunks;
		
	}

	/*
	 * WRITE METHODS
	 * TODO
	 */

	@Override public void write(FileOutputStream writer) throws IOException {
		
		putString(FILE_NAME , writer);
		putInt(FILE_VERSION , writer);
		super.write(writer);
		putInt(numberArtboardShapeAndLineChunks() , writer);
		writeArtboardShapesAndLinesChunks(writer);
		
	}

	@Override public void write() throws FileNotFoundException, IOException {
		
		try(FileOutputStream writer = new FileOutputStream(fileName())) {
			
			write(writer);	
			writer.flush();
			
		}
		
	}

	protected void writeArtboardShapesAndLinesChunks(FileOutputStream writer) throws IOException {
		
		Iterator<Artboard> artboards = project.nonShallowCopiedArtboards();
		
		while(artboards.hasNext()) {
			
			Artboard x = artboards.next();			
			int numberEllipses = x.numberEllipses();
			int numberRectangles = x.numberRectangles();
			
			int numberLinearLines = x.numberLinearLines();
			int numberBezierLines = x.numberBezierLines();
			
			if(numberEllipses + numberRectangles + numberLinearLines + numberBezierLines == 0) continue;
			
			putString(x.name , writer);
			putInt(numberEllipses , writer);			
			putInt(numberRectangles , writer);
			putInt(numberLinearLines , writer);
			putInt(numberBezierLines , writer);

 			writeEllipsesForLayers(x , x.visualLayers() , writer);
 			writeEllipsesForLayers(x , x.nonVisualLayers() , writer);
 			writeRectanglesForLayers(x , x.visualLayers() , writer);
 			writeRectanglesForLayers(x , x.nonVisualLayers() , writer);
			
			writeLinearLinesForLayers(x.visualLayers(), writer);				
			writeLinearLinesForLayers(x.nonVisualLayers(), writer);
			
			writeBezierLinesForLayers(x.visualLayers(), writer);
			writeBezierLinesForLayers(x.nonVisualLayers(), writer);
			
		}
		
	}

	private void writeEllipsesForLayers(Artboard artboard , Iterator<? extends Layer> layers , FileOutputStream writer) throws IOException {
		
		int index = 0;
		while(layers.hasNext()) {
			
			Layer next = layers.next();			
			Iterator<Ellipse> ellipses = next.ellipsesIterator();
			while(ellipses.hasNext()) writeEllipseChunk(artboard , index , next , ellipses.next() , writer);			
			index++;
			
		}
	
	}
	
	private void writeRectanglesForLayers(Artboard artboard , Iterator<? extends Layer> layers , FileOutputStream writer) throws IOException {
		
		int index = 0;
		while(layers.hasNext()) {
			
			Layer next = layers.next();
			Iterator<Rectangle> rectangles = next.rectanglesIterator();
			while(rectangles.hasNext()) writeRectangleChunk(artboard , index , next , rectangles.next() , writer);
			index++;
			
		}
		
	}

	private void writeShapeChunk(Artboard artboard , int layerIndex , Layer owner , Shape writeThis , FileOutputStream writer) throws IOException {
		
		putBoolean(writeThis.fill(), writer);
		putByte(writeThis.borderColor().r() , writer);
		putByte(writeThis.borderColor().g() , writer);
		putByte(writeThis.borderColor().b() , writer);
		putByte(writeThis.borderColor().a() , writer);
		putByte(writeThis.fillColor().r() , writer);
		putByte(writeThis.fillColor().g() , writer);
		putByte(writeThis.fillColor().b() , writer);
		putByte(writeThis.fillColor().a() , writer);
		putBoolean(owner instanceof VisualLayer , writer);
		putInt(layerIndex , writer);
		putBoolean(writeThis.hide() , writer);	
		putFloat(writeThis.xOffsetFrom(artboard) , writer);
		putFloat(writeThis.yOffsetFrom(artboard) , writer);
		putInt(writeThis.shapeWidth() , writer);
		putInt(writeThis.shapeHeight() , writer);
		
	}

	private void writeEllipseChunk(Artboard artboard , int layerIndex , Layer owner , Ellipse writeThis , FileOutputStream writer) throws IOException {
		
		writeShapeChunk(artboard , layerIndex , owner , writeThis , writer);
		putInt(writeThis.xRadius() , writer);
		putInt(writeThis.yRadius() , writer);
		putFloat(writeThis.iterations() , writer);
		
	}
	
	private void writeRectangleChunk(
		Artboard artboard , 
		int layerIndex , 
		Layer owner , 
		Rectangle writeThis , 
		FileOutputStream writer
	) throws IOException {
		
		writeShapeChunk(artboard , layerIndex , owner , writeThis , writer);
				
	}
	
	private void writeLinearLinesForLayers(Iterator<? extends Layer> layers , FileOutputStream writer) throws IOException {

		for(int i = 0; layers.hasNext() ; i++) {
			
			Layer next = layers.next();
			Iterator<LinearLine> lines = next.linearLinesIterator();
			while(lines.hasNext()) writeLinearLineChunk(i , next , lines.next() , writer);
						
		}
		
	}
	
	private void writeBezierLinesForLayers(Iterator<? extends Layer> layers , FileOutputStream writer) throws IOException {

		for(int i = 0; layers.hasNext() ; i++) {
			
			Layer next = layers.next();
			Iterator<BezierLine> lines = next.bezierLinesIterator();
			while(lines.hasNext()) writeBezierLineChunk(i , next , lines.next() , writer);
						
		}
		
	}
	
	private void writeLinearLineChunk(int layerIndex , Layer owner , LinearLine writeThis , FileOutputStream writer) throws IOException {
		
		writeLineChunk(layerIndex , owner , writeThis , writer);
		
	}
	
	private void writeBezierLineChunk(int layerIndex , Layer owner , BezierLine writeThis , FileOutputStream writer) throws IOException {
		
		writeLineChunk(layerIndex , owner , writeThis , writer);
		
		putFloat(0f , writer);
		putInt(writeThis.numberControlPoints() , writer);
		
		for(Iterator<Vector2f> points = writeThis.controlPoints() ; points.hasNext() ; ) { 
		
			Vector2f point = points.next();
			putFloat(point.x , writer);
			putFloat(point.y , writer);

		}
			
	}
	
	private void writeLineChunk(int layerIndex , Layer owner , Line writeThis , FileOutputStream writer) throws IOException {
		
		putBoolean(owner instanceof VisualLayer , writer);
		putInt(layerIndex , writer);
		putInt(writeThis.endpoint1X() , writer);
		putInt(writeThis.endpoint1Y() , writer);
		putInt(writeThis.endpoint2X() , writer);
		putInt(writeThis.endpoint2Y() , writer);
		putByte(writeThis.color().r() , writer);
		putByte(writeThis.color().g() , writer);
		putByte(writeThis.color().b() , writer);
		putByte(writeThis.color().a() , writer);
		putInt(writeThis.thickness() , writer);
		
	}
	
	/*
	 * READ METHODS
	 * TODO
	 */
	
	@Override public void read(FileInputStream reader) throws IOException {
	
		String fileName = getString(reader);
		if(!fileName.equals(FILE_NAME)) throw new IOException("File format invalid.");
		
		int fileVersion = getInt(reader);
		if(fileVersion != FILE_VERSION) throw new IOException("File format invalid.");
		
		super.read(reader);
		artboardShapeAndLineChunks = readArtboardShapesAndLinesChunks(reader);
		
	}
	
	@Override public void read() throws FileNotFoundException, IOException {
		
		try(FileInputStream reader = new FileInputStream(fileName())) {
			
			read(reader);
			
		}
		
	}
	
	protected ArtboardShapesAndLinesChunk[] readArtboardShapesAndLinesChunks(FileInputStream reader) throws IOException {
		
		int numberArtboardChunks = getInt(reader);
		ArtboardShapesAndLinesChunk[] chunks = new ArtboardShapesAndLinesChunk[numberArtboardChunks];		
		for(int i = 0 ; i < chunks.length ; i++) chunks[i] = readArtboardShapesAndLinesChunk(reader);		
		return chunks;
		
	}
	
	private ArtboardShapesAndLinesChunk readArtboardShapesAndLinesChunk(FileInputStream reader) throws IOException {
		
		String name = getString(reader);
		int numberEllipses = getInt(reader);
		int numberRectangles = getInt(reader);
		int numberLinears = getInt(reader);
		int numberBezier = getInt(reader);
		
		EllipseChunk[] ellipses = new EllipseChunk[numberEllipses];
		RectangleChunk[] rectangles = new RectangleChunk[numberRectangles];
		LinearChunk[] linears = new LinearChunk[numberLinears];
		BezierChunk[] beziers = new BezierChunk[numberBezier];
		
		readEllipses(ellipses, reader);
		readRectangles(rectangles, reader);
		readLinears(linears , reader);
		readBeziers(beziers , reader);
		
		return new ArtboardShapesAndLinesChunk(
			name , 
			numberEllipses , 
			numberRectangles , 
			numberLinears , 
			numberBezier , 
			ellipses , 
			rectangles , 
			linears , 
			beziers
		);
		
	}

	private void readEllipses(EllipseChunk[] destination , FileInputStream reader) throws IOException {
		
		for(int i = 0 ; i < destination.length ; i++) destination[i] = new EllipseChunk(
			readShapeChunk(reader) ,
			getInt(reader) ,
			getInt(reader) ,
			getFloat(reader)
		);
		
	}
		
	private void readRectangles(RectangleChunk[] destination , FileInputStream reader) throws IOException {
		
		for(int i = 0 ; i < destination.length ; i++) destination[i] = new RectangleChunk(readShapeChunk(reader));

	}
	
	private ShapeChunk readShapeChunk(FileInputStream reader) throws IOException {
		
		boolean fill = getBoolean(reader);
		byte borderRed = getByte(reader);
		byte borderGreen = getByte(reader);
		byte borderBlue = getByte(reader);
		byte borderAlpha = getByte(reader);
		byte fillRed = getByte(reader);
		byte fillGreen = getByte(reader);
		byte fillBlue = getByte(reader);
		byte fillAlpha = getByte(reader);
		boolean visual = getBoolean(reader);
		int layerIndex = getInt(reader);
		boolean hide = getBoolean(reader);
		float midX = getFloat(reader);
		float midY = getFloat(reader);
		int textureWidth = getInt(reader);
		int textureHeight = getInt(reader);
		
		return new ShapeChunk(
			fill , 
			new ChannelBuffer(borderRed , borderGreen , borderBlue , borderAlpha) ,
			new ChannelBuffer(fillRed , fillGreen , fillBlue , fillAlpha) ,			
			visual ,
			layerIndex ,
			hide ,
			midX ,
			midY ,
			textureWidth ,
			textureHeight
		);
		
	}

	private void readLinears(LinearChunk[] destination , FileInputStream reader) throws IOException {
		
		for(int i = 0 ; i < destination.length ; i++) { 
			
			destination[i] = new LinearChunk(LineChunk.read(reader));
			
		}
		
	}

	private void readBeziers(BezierChunk[] destination , FileInputStream reader) throws IOException {
		
		for(int i = 0 ; i < destination.length ; i++) destination[i] = BezierChunk.read(reader);
		
	}

	private int numberArtboardShapeAndLineChunks() {
		
		Iterator<Artboard> artboards = project.nonShallowCopiedArtboards();
		int number = 0;
		while(artboards.hasNext()) {
			
			Artboard next = artboards.next();
			if(next.activeLayerShapes().hasNext()) {
				
				number++;
				continue;
				
			}
			
			if(next.lines().hasNext()) number++;
			
		}
		
		return number;
		
	}
	
	/**
	 * Contains all the data about the shapes and lines belonging to an artboard.
	 */
	public record ArtboardShapesAndLinesChunk(
		String artboardName ,
		int numberEllipses ,
		int numberRectangles ,
		int numberLinearLines ,
		int numberBezierLines ,
		EllipseChunk[] ellipses ,
		RectangleChunk[] rectangles,
		LinearChunk[] linearLines ,
		BezierChunk[] bezierLines
	) {}
	
	/**
	 * Contains loaded data common to all shapes.
	 */
	public record ShapeChunk(
		boolean fill ,
		ColorPixel borderColor ,
		ColorPixel fillColor ,
		boolean belongsToVisualLayer ,
		int layerIndex ,
		boolean hide ,
		float offsetX ,
		float offsetY,
		int width , 
		int height
	) {}
	
	/**
	 * Contains loaded data for an ellipse.
	 */
	public record EllipseChunk(
		ShapeChunk shape ,
		int xRadius , 
		int yRadius ,
		float iterations
	) {}
	
	/**
	 * Contains loaded data for a rectangle.
	 */
	public record RectangleChunk(
		ShapeChunk shape
	) {}
	
	/**
	 * Contains loaded data common to all lines.
	 */
	public record LineChunk(
		boolean belongsToVisualLayer ,
		int layerIndex ,
		int endpoint1X , 
		int endpoint1Y ,
		int endpoint2X , 
		int endpoint2Y ,
		ColorPixel color ,
		int thickness
	) {
		
		private static LineChunk read(FileInputStream reader) throws IOException {
			
			return new LineChunk(
				getBoolean(reader) ,
				getInt(reader) ,
				getInt(reader) ,
				getInt(reader) ,
				getInt(reader) , 
				getInt(reader) ,
				new ChannelBuffer(getByte(reader) , getByte(reader) , getByte(reader) , getByte(reader)) ,
				getInt(reader)
			);
			
		}
		
		
	}
	
	/**
	 * Contains loaded data common to all linear lines.
	 */
	public record LinearChunk(
		LineChunk line
	) {}
	
	/**
	 * Contains loaded data common to all bezier lines.
	 */
	public record BezierChunk(
		LineChunk line ,
		float iterations , 
		int numberControlPoints ,
		Vector2f[] controlPoints
	) {
		
		private static BezierChunk read(FileInputStream reader) throws IOException {
			
			LineChunk line = LineChunk.read(reader);
			float iterations = getFloat(reader);
			int numberControlPoints = getInt(reader);
			
			Vector2f[] controlPoints = new Vector2f[numberControlPoints];
			for(int i = 0 ; i < numberControlPoints ; i++) controlPoints[i] = new Vector2f(getFloat(reader) , getFloat(reader));

			return new BezierChunk(line , iterations , numberControlPoints , controlPoints);
			
		}
		
	}
	
}
