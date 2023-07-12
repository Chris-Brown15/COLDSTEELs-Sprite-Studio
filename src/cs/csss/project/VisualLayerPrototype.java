package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

public record VisualLayerPrototype(String name) {

	public VisualLayerPrototype(String name) {

		specify(name , "Must enter a name for a layer.");
	
		this.name = name;
		
	}

	@Override public String toString() {
		
		return "Visual Layer Prototype: " + name; 
	
	}
	
}
