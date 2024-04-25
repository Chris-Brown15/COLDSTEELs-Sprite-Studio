package cs.csss.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import cs.csss.editor.line.BezierLine;
import cs.csss.editor.line.Line;
import cs.csss.editor.line.LinearLine;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;

class LineManager {

	List<Line> lines = Collections.synchronizedList(new ArrayList<>());
	
	LineManager() {

	}

	LinearLine newLinearLine(ColorPixel color) {
		
		byte z = (byte)0;		
		if(color == null) color = new ChannelBuffer(z , z , z , (byte)0xff);
		else color = (ColorPixel) color.clone();
		
		LinearLine newLine = new LinearLine();
		newLine.color(ChannelBuffer.asChannelBuffer(color));
		
		lines.add(newLine);
		
		return newLine;
		
	}
	
	BezierLine newBezierLine(ColorPixel color) {
		
		byte z = (byte)0;
		if(color == null) color = new ChannelBuffer(z , z , z , (byte)0xff);
		else color = (ColorPixel) color.clone();
		
		BezierLine line = new BezierLine();
		line.color(ChannelBuffer.asChannelBuffer(color));
		
		lines.add(line);
		
		return line;
		
	}
	
	void add(Line line) {
		
		assert line != null;
		assert !lines.contains(line);
		lines.add(line);
		
	}
	
	boolean remove(Line line) {
		
		assert line != null;
		return lines.remove(line);
		
	}
	
	boolean contains(Line line) {
		
		assert line != null;
		return lines.contains(line);
		
	}
	
	Stream<LinearLine> linearLines() {
		
		return lines.stream().filter(line -> line instanceof LinearLine).map(line -> (LinearLine)line);
		
	}
	
	Stream<BezierLine> bezierLines() {
		
		return lines.stream().filter(line -> line instanceof BezierLine).map(line -> (BezierLine)line);
		
	}
	
	Stream<Line> lines() {
		
		return lines.stream();
		
	}
	
}
