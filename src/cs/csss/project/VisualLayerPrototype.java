package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

/**
 * Prototype of a visual layer. Visual layers can be created from prototypes and receive from the prototype their name.
 */
public class VisualLayerPrototype {

	private final String name;
	
	VisualLayerPrototype(String name) {

		specify(name , "Must enter a name for a layer.");
	
		this.name = name;
		
	}
	
	public String name() {
		
		return name;
		
	}

	@Override public String toString() {
		
		return "Visual Layer Prototype: " + name; 
	
	}
	
}
