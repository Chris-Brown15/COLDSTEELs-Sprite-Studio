package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 
 * This class is delegated to when copying and retrieving copy information is required by the rest of the program. This class only handles
 * shallow copying, not deep copying.
 * 
 * <p>
 * 	Shallow copying artboards means to create a new artboard who shares a texture and palette with some existing artboard. Therefore, when
 * 	changes are made to one of the artboards, they are seen on all other copies or the source. Only original artboards (ones that were not
 * 	created from a shallow copy) can be sources for shallow copy. Shallow copies are used when an artboard is active in more than one 
 * 	animation at a time. When an artboard is already in an animation and a user adds it to another one, a shallow copy gets added to the 
 * 	second one. Shallow copies cannot be added to animations themselves, only sources. When a shallow copy is removed from an animation, it
 * 	is removed from the program. When a source artboard is removed from an animation and there exists shallow copies of it in other 
 * 	animations, the first shallow copy found is removed from the program and the source is put in its place.
 * </p>
 * 
 * @author Chris Brown
 *
 */
class ArtboardCopier {

	/**
	 * This data structure is responsible for tracking artboards that have been used to make shallow copies and from which artboard they 
	 * originate.
	 */
	private ArrayList<CopyEntry> copyEntries = new ArrayList<>();
	
	ArtboardCopier() {}
	
	/**
	 * Returns an {@code Optional} containing the copy entry containing {@code possibleSource} if the source exists.
	 * 
	 * @param possibleSource — an artboard that may be a source or not
	 * @return Optional containing the copy entry attached to this source, if it exists, otherwise, the optional is empty.
	 */
	private Optional<CopyEntry> hasSource(Artboard possibleSource) {
		
		for(CopyEntry x : copyEntries) if(x.source == possibleSource) return Optional.of(x);
		return Optional.empty();
		
	}
	
	/**
	 * Returns a shallow copy of {@code source}, returning the result. 
	 * 
	 * <p>
	 * 	The returned object is a shallow copy of the source. In this context, a shallow copy is an artboard which shares a palette and 
	 * 	texture with another artboard. Therefore, any modifications to {@code source} will be present on the result, and vise versa.
	 * </p>
	 *  
	 * @param source — source artboard for copy
	 * @return Resulting deep copy.
	 */
	Artboard copy(Artboard source) {
		
		//this line makes sure that the source for the copy is not an alias. If the source parameter is a copy, we find the source of that 
		//copy, preventing us from ever having aliases make aliases.
		if(isCopy(source)) source = getSourceOf(source);
		
		Artboard newArtboard = Artboard.shallowCopy(source.name , source);
		Optional<CopyEntry> hasSourceResult = hasSource(source); 
		
		if(hasSourceResult.isPresent()) hasSourceResult.get().add(newArtboard);
		else copyEntries.add(new CopyEntry(source , newArtboard));
		
		return newArtboard;
		
	}
	
	/**
	 * Returns whether {@code isCopy} is a copy of some artboard.
	 * 
	 * @param isCopy — an artboard whose status as a shallow copy is being queried
	 * @return {@code true} if {@code isCopy} is a shallow copy.
	 */
	boolean isCopy(Artboard isCopy) {
		
		for(CopyEntry x : copyEntries) if(x.contains(isCopy)) return true;			
		return false;
		
	}
		
	/**
	 * Removes a shallow copy from the storage of shallow copies.
	 * 
	 * @param copy — artboard to remove
	 */	
	void removeCopy(Artboard copy) {
		
		for(CopyEntry x : copyEntries) if(x.contains(copy)) { 
			
			x.remove(copy);
			break;
			
		}
				
	}
	
	/**
	 * Given a shallow copied artboard, this method returns its source.
	 * 
	 * @param shallowCopy — a shallow copy artboard
	 * @return The source of the shallow copy.
	 */
	Artboard getSourceOf(Artboard shallowCopy) {
		
		for(CopyEntry x : copyEntries) if(x.contains(shallowCopy)) return x.source;		
		throw new IllegalArgumentException(shallowCopy.name + " is not a valid shallow copy");
		
	}
		
	/**
	 * Returns whether the given artboard is a source or not.
	 * 
	 * @param possibleSource — an artboard whose status as a source is being queried
	 * @return {@code true} if {@code possibleSource} is a source.
	 */
	boolean isSource(Artboard possibleSource) {
		
		return hasSource(possibleSource).isPresent();
		
	}
	
	/**
	 * Given some source, {@code callback} is invoked on each copy of that source.
	 * 
	 * @param source — a source artboard
	 * @param callback — a function to invoke on each copy
	 */
	void forEachCopyOf(Artboard source , Consumer<Artboard> callback) {
		
		Optional<CopyEntry> sourceEntry = hasSource(source);
		if(sourceEntry.isPresent()) sourceEntry.get().copies.forEach(callback);
		
	}
	
	/**
	 * 
	 * Record representing instances of sources and the copies they spawned.
	 *
	 */
	private record CopyEntry(Artboard source , LinkedList<Artboard> copies) {
		
		CopyEntry(Artboard source) {
			
			this(source , new LinkedList<>());
			
		}
		
		CopyEntry(Artboard source , Artboard firstCopy) {
			
			this(source);
			add(firstCopy);
			
		}
		
		/**
		 * Adds {@code artboard} to this entry
		 * 
		 * @param artboard — a copied artboard
		 */
		void add(Artboard artboard) {
			
			copies.add(artboard);
			
		}
		
		/**
		 * Removes {@code artboard} from this entry
		 * 
		 * @param artboard — a copied artboard
		 */
		void remove(Artboard artboard) {
			
			specify(copies.remove(artboard) , "Artboard not found in this list of copies.");
			
		}
		
		/**
		 * Returns whether {@code artboard} is in this entry.
		 * 
		 * @param artboard - an artboard to query
		 * @return {@code true} if this entry has {@code artboard}.
		 */
		boolean contains(Artboard artboard) {
			
			return copies.contains(artboard);
			
		}
		
	}
	
}
