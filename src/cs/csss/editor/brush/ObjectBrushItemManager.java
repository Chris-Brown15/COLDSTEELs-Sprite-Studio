package cs.csss.editor.brush;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Class used to manage {@link Supplier}s of the objects a {@link CSSSObjectBrush} can create and names for those objects which are displayed to the
 * user.
 * 
 * @param <T> type of object suppliers return
 */
public final class ObjectBrushItemManager<T> {

	private ConcurrentHashMap<String , Supplier<T>> descriptors = new ConcurrentHashMap<>();
	
	/**
	 * Creates a new object brush item manager.
	 */
	public ObjectBrushItemManager() {}

	/**
	 * Adds a supplier and name to the list of objects.
	 * 
	 * @param name name of the object {@code creator} can make
	 * @param creator supplier of the object the brush can now make
	 * @throws NullPointerException if either {@code name} or {@code creator} is <code>null</code>.
	 */
	public void addObject(String name , Supplier<T> creator) {

		descriptors.put(Objects.requireNonNull(name), Objects.requireNonNull(creator));
		
	}
	
	/**
	 * Gets a supplier for the object named {@code name}. 
	 * 
	 * @param name name of an object the owning object brush can make
	 * @return Supplier for the corresponding object
	 * @throws NullPointerException if {@code name} is <code>null</code> or it does not correspond to any supplier.
	 */
	public Supplier<T> get(String name) {
		
		return Objects.requireNonNull(descriptors.get(Objects.requireNonNull(name)));
		
	}
	
	/**
	 * Removes the given supplier and name from the list of objects the owning brush can create.
	 * 
	 * @param name name corresponding to the supplier for the item
	 * @throws NullPointerException if {@code name} is <code>null</code> or it does not name an item the owning brush can create
	 */
	public void remove(String name) {
		
		Objects.requireNonNull(descriptors.remove(Objects.requireNonNull(name)));
		
	}

	/**
	 * Returns a set of the names objects of this manager. 
	 * 
	 * @return Set of names of this item manager.
	 */
	public Set<String> names() {
		
		return descriptors.keySet();
		
	}
	
}
