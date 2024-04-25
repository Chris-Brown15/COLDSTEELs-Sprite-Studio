package cs.csss.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import cs.csss.editor.shape.Shape;

class ProjectShapesIterator implements Iterator<Shape> {

	private final List<Iterator<Shape>> iterators = new ArrayList<>();
	
	private int index;
	
	ProjectShapesIterator(CSSSProject project) {

		project.forEachNonShallowCopiedArtboard(artboard -> {
			
			artboard.forAllLayers(layer -> {
				
				Iterator<Shape> shapeIterator = layer.shapesIterator();
				if(!shapeIterator.hasNext()) return;				
				iterators.add(shapeIterator);
				
			});
			
		});
		
	}

	@Override public boolean hasNext() {

		return index < iterators.size() && iterators.get(index).hasNext();
		
	}

	@Override public Shape next() {

		if(!hasNext()) throw new NoSuchElementException("This iteration has no remaining elements.");
		
		Iterator<Shape> iterator = iterators.get(index);
		Shape x = iterator.next();
		
		if(!iterator.hasNext()) index++;
		
		return x;
		
	}

}
