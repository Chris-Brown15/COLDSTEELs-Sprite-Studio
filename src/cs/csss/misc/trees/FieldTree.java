/**
 * 
 */
package cs.csss.misc.trees;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.LinkedList;
import java.util.Objects;

/**
 * 
 */
public class FieldTree {

	//object whose fields are being made into a tree
	Object _this;
	
	private final LinkedList<FieldTree> nodes = new LinkedList<>();
	
	/**
	 * 
	 */
	public FieldTree(Object _this) {

		this._this = Objects.requireNonNull(_this);
		Field[] fields = _this.getClass().getDeclaredFields();
		for(Field x : fields) { 
			
			try {

				boolean canAccess = x.canAccess(_this);
				x.setAccessible(true);
				nodes.add(new FieldTree(x.get(_this)));
				x.setAccessible(canAccess);
				
			} catch (IllegalAccessException e) {

				e.printStackTrace();
				
			} catch(InaccessibleObjectException e1) {
				
//				nodes.add(new FieldTree(x));
			
			} catch(IllegalArgumentException ia) {}
			
			
		}
		
	}

	public String toString() {
	
		return toString(0);
		
	}			
	
	public String toString(int depth) {

		StringBuilder string = new StringBuilder();
		tab(depth , string);
		string.append(_this.toString()).append('\n');
		tab(depth , string);
		string.append('[').append('\n');

		for(FieldTree x : nodes) string.append(x.toString(depth + 1));
				
		tab(depth , string);
		string.append(']');
		
		if(depth != 0) string.append('\n');
		
		return string.toString();
		
	}
	
	private void tab(int depth , StringBuilder string) {
		
		for(int j = 0 ; j < depth ; j++) string.append('\t');
		
	}
	
}
