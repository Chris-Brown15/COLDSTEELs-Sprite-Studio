/**
 * 
 */
package cs.csss.editor.brush;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


/**
 * Brush class representing brushes that can create objects. Implementations of this brush type are used to create things like shapes and lines.
 */
public abstract class CSSSObjectBrush<T> extends CSSSBrush {

	/**
	 * Manager for the objects this object brush can create. 
	 */
	protected ObjectBrushItemManager<T> descriptors = new ObjectBrushItemManager<>();

	private final AtomicReference<String> activeShapeSupplierName = new AtomicReference<>();
	
	/**
	 * Creates an object brush with the initial set of creatable objects and names for those objects.
	 * 
	 * @param tooltip tooltip this brush will display
	 * @param stateful whether this brush is stateful
	 * @param initialCreatableObjects map of Strings to {@link Supplier}s for objects this brush can create
	 * @throws NullPointerException if {@code initialCreatableObjects} is <code>null</code>.
	 * @throws IllegalArgumentException if {@code initialCreatableObjects} is empty.
	 */
	public CSSSObjectBrush(String tooltip, boolean stateful , Map<String , Supplier<T>> initialCreatableObjects) {
	
		super(tooltip, stateful);
		
		Objects.requireNonNull(initialCreatableObjects);
		if(initialCreatableObjects.size() == 0) throw new IllegalArgumentException("Cannot have an empty set of creatable objects.");
		initialCreatableObjects.forEach(descriptors::addObject);
		activeDescriptor(initialCreatableObjects.keySet().stream().findAny().get());
				
	}
	
	/**
	 * Returns the name of the object this brush is currently set to create.
	 * 
	 * @return Name of the currently selected item.
	 */
	public final String activeDescriptorName() {
		
		return activeShapeSupplierName.get();
		
	}

	/**
	 * Sets the currently active object this brush will create.
	 * 
	 * @param name name of the currently active object 
	 */
	public final void activeDescriptor(String name) {
		
		activeShapeSupplierName.set(name);
		
	}
	
	/**
	 * Returns the supplier for the active object
	 * 
	 * @return Supplier for the active object.
	 */
	public Supplier<T> activeDescriptor() {
		
		return descriptors.get(activeDescriptorName());
		
	}
	
	/**
	 * Returns a set containing the names of objects this brush can create.
	 * 
	 * @return Set of strings representing names of objects this brush can create.
	 */
	public Set<String> objectNames() {
		
		return descriptors.names();
		
	}
	
}
