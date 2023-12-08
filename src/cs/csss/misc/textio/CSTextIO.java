/**
 * 
 */
package cs.csss.misc.textio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Class containing common operations for writing text files to disk. These files are human readable and modifyable and are intended to be terse where
 * appropriate. 
 */
public final class CSTextIO implements AutoCloseable {
	
	private static final char SCOPE = '\t' , LINE_FEED = '\n';
	private static final String ENTRY_SEPARATOR = " = " , ITEM_NAME_VALUE_SEPARATOR = " = " , LIST_TITLE_ENDING = ":";
	
	/**
	 * Creates and returns a new {@code CSTFFile} initialized for reading the given file. The given filepath must exist. If a write operation is 
	 * attempted, {@code NullPointerException} is thrown.
	 * 
	 * @param filepath path to read from
	 * @return CSTF file reader.
	 * @throws IOException if filepath does not exist.
	 * @throws NullPointerException if {@code filepath} is <code>null</code>.
	 */
	public static CSTextIO createReader(String filepath) throws IOException {
		
		return createReader(new File(Objects.requireNonNull(filepath)));
		
	}

	/**
	 * Creates and returns a new {@code CSTFFile} initialized for reading from the given file. If a write operation is attempted, 
	 * {@code NullPointerException} is thrown.
	 * 
	 * @param file to read from
	 * @return CSTF file reader.
	 * @throws IOException if filepath does not exist.
	 * @throws NullPointerException if {@code filepath} is <code>null</code>.
	 */
	public static CSTextIO createReader(File file) throws IOException {
		
		CSTextIO reader = new CSTextIO();
		Objects.requireNonNull(file);
		if(!file.exists()) throw new IOException("File " + file.getName() + " does not exist.");
		reader.reader = new BufferedReader(new FileReader(file));
		return reader;
		
	}

	/**
	 * Creates and returns a new {@code CSTFFile} initialized for writing to the given file. If the file described by {@code filepath} does not exist,
	 * a new one is created. If a read operation is attempted, {@code NullPointerException} is thrown.
	 * 
	 * @param filepath the file to write to
	 * @return CSTF file writer
	 * @throws IOException if creating the file described by {@code filepath} fails.
	 * @throws NullPointerException if {@code filepath} is <code>null</code>. 
	 */
	public static CSTextIO createWriter(String filepath) throws IOException {
		
		return createWriter(new File(Objects.requireNonNull(filepath)));
		
	}

	/**
	 * Creates and returns a new {@code CSTFFile} initialized for writing to the given file. If the file described by {@code file} does not exist, a 
	 * new one is created. If a read operation is attempted, {@code NullPointerException} is thrown.
	 * 
	 * @param file the file to write to 
	 * @return CSTF file writer.
	 * @throws IOException if creating the file described by {@code filepath} fails.
	 * @throws NullPointerException if {@code filepath} is <code>null</code>. 
	 */
	public static CSTextIO createWriter(File file) throws IOException {
	
		CSTextIO writer = new CSTextIO();
		Objects.requireNonNull(file);
		if(!file.exists()) file.createNewFile();
		writer.writer = new BufferedWriter(new FileWriter(file));
		return writer;
		
	}	

	private BufferedReader reader;
	private BufferedWriter writer;
	
	private int scope = 0;
	
	/*
	 
	 PUT OPERATIONS
	 
	 */
	
	/**
	 * Puts the given {@link Collection} of items in this file. The list is prefixed with the given title and each item is separated by a newline
	 * and appropriate amount of tabs. Each item in {@code items} is converted to a string via {@code itemToString}.
	 * 
	 * @param <T> type of item in {@code items}
	 * @param title name of the list
	 * @param items {@code Collection} of items
	 * @param itemToString Converter from {@code T} to String. 
	 * @throws IOException if an exception occurs during writing.
	 * @throws NullPointerException if any of the parameters to this method are <code>null</code> or if this CSTextIO is initialized for reading.
	 */
	public <T> void putCollection(String title , Collection<T> items , Function<T , String> itemToString) throws IOException {
		
		Objects.requireNonNull(title);
		Objects.requireNonNull(items);
		Objects.requireNonNull(itemToString);
		
		writeScope();
		writer.write(title + "(" + Integer.toString(items.size()) + ")" + LIST_TITLE_ENDING);
		writeNewline();
		scope++;
		for(T x : items) writeString(x == null ? "null" : itemToString.apply(x));
		scope--;
		
	}
	
	/**
	 * Puts the given {@link Map} of entries in this file. The map is prefixed with the given title, and each entry is laid out as 
	 * {@code key "to" value}, with entries being separated by newlines and tabs. Each key and value is converted to a string by its corresponding 
	 * function. 
	 * 
	 * @param <K> the type of keys in the map
	 * @param <V> the type of values in the map
	 * @param title the title of the map in the file
	 * @param map the map to put in the file
	 * @param keyToString a function which converts items of the key type to {@code String}s.
	 * @param valueToString a function which converts items of the value type to {@code String}s.
	 * @throws IOException if an IO error occurs during the operations of this method
	 * @throws NullPointerException if any of the given parameters are <code>null</code> or if this CSTextIO is initialized for reading.
	 */
	public <K , V> void putMap(
		String title , 
		Map<K , V> map , 
		Function<K , String> keyToString , 
		Function<V , String> valueToString
	) throws IOException {
		
		Objects.requireNonNull(title);
		Objects.requireNonNull(map);
		Objects.requireNonNull(keyToString);
		Objects.requireNonNull(valueToString);
		
		writeScope();
		Set<Entry<K , V>> entries = map.entrySet();
		writer.write(String.format(title + "(%d)%s", entries.size() , LIST_TITLE_ENDING));
		writeNewline();
		scope++;
		for(Entry<K , V> x : entries) writeString(keyToString.apply(x.getKey()) + ENTRY_SEPARATOR + valueToString.apply(x.getValue()));
		scope--;
		
	}
	
	/**
	 * Puts the contents of {@code iterator} into the file. Each element of {@code iterator} is passed to {@code itemToString}, which converts it to a
	 * {@code String} that is ultimately stored. The size of the iteration is not recorded in this file. To read a collection of elements put into an
	 * iteration, use {@link CSTextIO#getCollection(Function, Function) getCollection}.
	 * 
	 * @param <T> type of items returned by {@code iterator}
	 * @param name name of the iteration
	 * @param iterator iterator of elements to put in this file
	 * @param itemToString converts each element of {@code iterator} to {@code String}; this function should handle the case that its input is 
	 * 		  <code>null</code>
	 * @throws IOException if an IO error occurs during writing the iterator to the file.
	 * @throws NullPointerException if any of the parameters are <code>null</code> or if this CSTextIO is initialized for reading.
	 */
	public <T> void putIteration(String name , Iterator<T> iterator , Function<T , String> itemToString) throws IOException {
		
		Objects.requireNonNull(name);
		Objects.requireNonNull(iterator);
		Objects.requireNonNull(itemToString);
		
		writeString(name + LIST_TITLE_ENDING);
		scope++;
		while(iterator.hasNext()) writeString(itemToString.apply(iterator.next()));		
		scope--;
		
	}
	
	/**
	 * <p>
	 * 	<b>First and Foremost:</b> This method must be accompanied by a call to {@link CSTextIO#endPutList() endPutList}.
	 * </p>
	 * Starts a new list within this file. Elements are added via {@link CSTextIO#putString(String) putString}.
	 * 
	 * @param title title of this list
	 * @throws IOException if an IO error occurs during writing the start of this list
	 * @throws NullPointerException if {@code title} is <code>null</code> or if this CSTextIO is initialized for reading.
	 */
	public void startPutList(String title) throws IOException {
		
		Objects.requireNonNull(title);
		
		writeScope();
		writer.write(title + LIST_TITLE_ENDING);
		scope++;
		writeNewline();
		
	}
	
	/**
	 * Puts a string directly into this file. The string is receives before it an appropriate number of tabs and a newline after it.
	 * 
	 * @param item a string to add 
	 * @throws IOException if an IO error occurs during writing the start of this list.
	 * @throws NullPointerException if this CSTextIO is initialized for reading.
	 */
	public void putString(String item) throws IOException {
		
		if(item == null) writeString(String.valueOf(item));
		
	}
	
	/**
	 * Converts an object to a string, then calls {@link CSTextIO#putString(String) putString(String)}.
	 * 
	 * @param item an item to put in this file
	 * @throws IOException if an IO error occurs during writing the start of this list.s
	 * @throws NullPointerException if this CSTextIO is initialized for reading.
	 */
	public void putString(Object item) throws IOException {
	
		putString(String.valueOf(item));
		
	}			
	
	/**
	 * Writes an entry into this file. The entry is formatted as {@code key + ENTRY_SEPARATOR + value}, and {@code key} cannot be <code>null</code>.
	 * 
	 * @param key key for this entry
	 * @param value value for this entry
	 * @throws IOException if an IO error occurs during writing.
	 * @throws CSMalformedText if the value token contains the reserved entry separator token, {@code ENTRY_SEPARATOR}.
	 * @throws NullPointerException if {@code key} is <code>null</code> or if this CSTextIO is initialized for reading.
	 */
	public void putEntry(String key , String value) throws IOException, CSMalformedText {
		
		Objects.requireNonNull(key);		
		if(value != null) if(value.indexOf(ENTRY_SEPARATOR) != -1) throw new CSMalformedText(
			String.format("The given value contains a reserved token, \"%s\".", ENTRY_SEPARATOR)
		);
		writeString(key + ENTRY_SEPARATOR + value);		
		
	}
	
	/**
	 * Converts the given objects to strings, then calls {@link CSTextIO#putEntry(String, String) putEntry(String , String)}.
	 * 
	 * @param key key for this entry
	 * @param value value for this entry
	 * @throws IOException if an IO error occurs during writing.
	 * @throws CSMalformedText if the value token contains the reserved entry separator token, {@code ENTRY_SEPARATOR}.
	 * @throws NullPointerException if {@code key == null} or if this CSTextIO is initialized for reading.
	 */
	public void putEntry(Object key , Object value) throws IOException, CSMalformedText {
		
		Objects.requireNonNull(key);
		putEntry(String.valueOf(key) , String.valueOf(value));
		
	}
	
	/**
	 * Writes an item to this file. An item is formatted as {@code itemName + ITEM_NAME_VALUE_SEPARATOR + value}, and {@code itemName} cannot be 
	 * <code>null</code>. 
	 * 
	 * @param itemName the name of the item to put
	 * @param value the item to put
	 * @throws IOException if an IO error occurs during writing.
	 * @throws CSMalformedText if the {@code value} begins with the ITEM_NAME_VALUE_SEPARATOR.
	 * @throws NullPointerException if {@code itemName} is <code>null</code> or if this CSTextIO is initialized for reading.
	 */
	public void putItem(String itemName , String value) throws IOException, CSMalformedText {
		
		Objects.requireNonNull(itemName);
		int separatorLength = ITEM_NAME_VALUE_SEPARATOR.length();
		if(value != null && value.length() >= separatorLength && value.indexOf(ITEM_NAME_VALUE_SEPARATOR) == 0) {
			
			throw new CSMalformedText("The value for an item cannot start with the ITEM_NAME_VALUE_SEPARATOR");
								
		}
		
		writeString(itemName + ITEM_NAME_VALUE_SEPARATOR + value);
		
	}

	/**
	 * Writes an item to this file. An item is formatted as {@code itemName + ITEM_NAME_VALUE_SEPARATOR + value}, and {@code itemName} cannot be 
	 * <code>null</code>. 
	 * 
	 * @param itemName the name of the item to put
	 * @param value the item to put
	 * @throws IOException if an IO error occurs during writing.
	 * @throws CSMalformedText if the {@code value} begins with the ITEM_NAME_VALUE_SEPARATOR.
	 * @throws NullPointerException if {@code itemName} is <code>null</code> or if this CSTextIO is initialized for reading.
	 */
	public void putItem(String itemName , Object value) throws IOException, CSMalformedText {
		
		putItem(itemName , String.valueOf(value));
		
	}
	
	/**
	 * Ends a list started with a call to {@link CSTextIO#startPutList(String) startPutList}.
	 *  
	 * @throws CSMalformedText if no list was started via {@code startPutList}. 
	 * @throws NullPointerException if this CSTextIO is initialized for reading.
	 */
	public void endPutList() throws CSMalformedText {
		
		if(scope == 0) throw new CSMalformedText("No list was begun, cannot end list.");
		scope--;
		
	}
	
	/*
	 
	 GET OPERATIONS
	 
	 */
	
	/**
	 * <p>
	 * 	<b>First and Foremost:</b> It is possible for your {@code creator} parameter to receive a size value of {@code -1}, so you must prepare for 
	 * 	this case. This means the list written in the file did not store its size.
	 * </p>
	 * Gets and returns a name and list for items of this file. The returned object contains the collection created via {@code creator} and the title
	 * prefixing the list. Each item in the list is converted from {@code String} to {@code T} via {@code parser}.
	 * 
	 * @param <T> type of item in the resulting collection
	 * @param creator callback for creating or returning a collection to serve as the destination for reads
	 * @param parser converts {@code String}s read from the file to {@code T}
	 * @return Container for the name of the list and the collection stored in it.
	 * @throws IOException if an exception occurs during writing.
	 * @throws CSMalformedText if the file is not correctly formed at the reader's current position for getting a map.
	 * @throws NullPointerException if any of the parameters to this method are <code>null</code> or if this CSTextIO is initialized for writing.
	 */
	public <T> CSNamedCollection<T> getCollection(
		Function<Integer , Collection<T>> creator , 
		Function<String , T> parser
	) throws IOException, CSMalformedText {
		
		Objects.requireNonNull(creator);
		Objects.requireNonNull(parser);
		
		readScope();
		TitleAndSize titleAndSize = getTitleAndSize();
		Collection<T> collection = creator.apply(titleAndSize.size);
		
		scope++;
		
		if(titleAndSize.size > -1) for(int j = 0 ; j < titleAndSize.size ; j++) {
			
			readScope();
			String line = reader.readLine();				
			collection.add(parser.apply(line));
			
		} else for(ScopeTestResult result = readAndTestScope() ; result.isValid() ; result = readAndTestScope()) { 
			
			collection.add(parser.apply(result.readString));
			
		}
		
		scope--;		
		return new CSNamedCollection<>(titleAndSize.title , collection);
		
	}
	
	/**
	 * <p>
	 * 	<b>First and Foremost:</b> It is possible for your {@code creator} parameter to receive a size value of {@code -1}, so you must prepare for 
	 * 	this case. This means the map written in the file did not store its size.
	 * </p>
	 * 
	 * Gets and returns a name and map written in the operant file. The resulting {@code NamedMap} contains the name of the map given when 
	 * {@link CSTextIO#putMap(String, Map, Function, Function) putMap} was invoked, and the map that was written to this file. Each entry of the map
	 * which was written in this file is converted back into its corresponding type via {@code keyParser} and {@code valueParser} respectively.
	 * 
	 * @param <K> the type of keys in the map
	 * @param <V> the type of values in the map
	 * @param creator a function which receives the size of the map and returns a map for putting read entries in
	 * @param keyParser a function which converts {@code String}s into items of type {@code K}
	 * @param valueParser a function which converts {@code String}s into items of type {@code V} 
	 * @return Container for the map read from the file and its associated name.
	 * @throws IOException if an IO error occurs during writing.
	 * @throws CSMalformedText if the file is not correctly formed at the reader's current position for getting a map.
	 * @throws NullPointerException if any of the parameters are <code>null</code> or if this CSTextIO is initialized for writing.
	 */
	public <K , V> CSNamedMap<K , V> getMap(
		Function<Integer , Map<K , V>> creator , 
		Function<String , K> keyParser , 
		Function<String , V> valueParser
	) throws IOException, CSMalformedText {
		
		Objects.requireNonNull(creator);
		Objects.requireNonNull(keyParser);
		Objects.requireNonNull(valueParser);
		
		readScope();
		TitleAndSize stats = getTitleAndSize();
		Map<K , V> map = creator.apply(stats.size);
		scope++;

		String[] destination = new String[2];
		if(stats.size >= 0) for(int i = 0 ; i < stats.size ; i++) {
			
			getEntry(destination , 0);
			map.put(keyParser.apply(destination[0]), valueParser.apply(destination[1]));
			
		} else for(ScopeTestResult result = readAndTestScope() ; result.isValid() ; result = readAndTestScope()) {
			
			getEntry(destination , 0);
			map.put(keyParser.apply(destination[0]), valueParser.apply(destination[1]));
							
		}
		
		scope--;
		
		return new CSNamedMap<>(stats.title , map);
		
	}

	/**
	 * <p>
	 * 	<b>First and Foremost:</b> This method must be accompanied by a call to {@link CSTextIO#endList() endList}.
	 * </p>
	 * 
	 * Starts reading a list from the file. 
	 * 
	 * @return The name of the list
	 * @throws IOException if an IO error occurs during writing.
	 * @throws CSMalformedText if the file is not correctly formed at the reader's current position for reading a list.
	 * @throws NullPointerException if this CSTextIO is initialized for writing.
	 */
	public String startGetList() throws IOException, CSMalformedText {
		
		TitleAndSize title = getTitleAndSize();
		scope++;		
		return title.title;
		
	}
	
	/**
	 * Returns a string read from the file. The resulting string is the reader's current line, sans tabs and newline.
	 * 
	 * @return String read from the file.
	 * @throws IOException if an IO error occurs during reading.
	 * @throws CSMalformedText if the file either is at its end or the end of the current list was reached and no call to {@code endXList} was 
	 * 		   made.
	 * @throws NullPointerException if this CSTextIO is initialized for writing.
	 */
	public String getString() throws IOException, CSMalformedText {
		
		ScopeTestResult result = readAndTestScope();
		if(!result.isValid()) throw new CSMalformedText("File not formed for subsequent get String operation.");
		return result.readString;
		
	}
	
	/**
	 * <p>
	 * 	<b>First and Foremost:</b> The string passed to {@code parser} can be <code>null</code>, so {@code parser} should handle this case.
	 * </p>
	 * 
	 * Reads the string on the current line and converts it into an instance of {@code T} via {@code parser}. 
	 * 
	 * @param <T> type of item being parsed into and returned
	 * @param parser function that converts a string to {@code parser}
	 * @return Instance of {@code T} converted via {@code parser}
	 * @throws IOException if an IO error occurs during reading.
	 * @throws CSMalformedText if the file either is at its end or the end of the current list was reached and no call to {@code endXList} was 
	 * 		   made.
	 * @throws NullPointerException if {@code parser} is <code>null</code> or if this CSTextIO is initialized for writing.
	 */
	public <T> T getValue(Function<String , T> parser) throws IOException, CSMalformedText {
		
		return parser.apply(getString());
		
	}

	/**
	 * Puts in the given array the entry read from the file at the current line. The first item put in the array is the key. The key cannot be 
	 * <code>null</code>. The second put item is the value, which can be <code>null</code>.
	 * <p>
	 * 	The given array must contain at least two indices for putting the resulting key and value. The key is put at {@code firstPutIndex}, and the
	 * 	value is put at {@code firstPutIndex + 1}. 
	 * </p>
	 * 
	 * @param destination the array to store the resulting key and value in
	 * @param firstPutIndex the index to put the key in; the value will be at {@code firstPutIndex + 1}.
	 * @throws CSMalformedText if the current line does not contain an entry on it, or it is at an invalid scope, or the key at the current line
	 * 		   is <code>null</code>.
	 * @throws IOException if an IO error occurs during reading the current line.
	 * @throws NullPointerException if {@code destination == null} or if this CSTextIO is initialized for writing.
	 * @throws IndexOutOfBoundsException if {@code destination} does not have enough space to put two elements from {@code firstPutIndex}.
	 */
	public void getEntry(String[] destination , int firstPutIndex) throws IOException, CSMalformedText {

		Objects.requireNonNull(destination);
		Objects.checkFromIndexSize(firstPutIndex, 2, destination.length);
		
		ScopeTestResult result = readAndTestScope();
		if(!result.isValid()) throw new CSMalformedText("Cannot read an entry from this file because: " + result.toString());
		String line = result.readString;
		int toIndex = line.lastIndexOf(ENTRY_SEPARATOR);
		if(toIndex == -1) throw new CSMalformedText(String.format("Entry malformed, no \"%s\" found.", ENTRY_SEPARATOR));
		String keyString = line.substring(0 , toIndex);
		if(keyString.equals("null")) throw new CSMalformedText("Key for the read entry is \"null\" which is invalid.");
		destination[0] = keyString;
		destination[1] = line.substring(toIndex + ENTRY_SEPARATOR.length());
				
	}
	
	/**
	 * Returns an array containing the key and value of the entry at the file reader's current line. The first index of the array is the key. The key
	 * cannot be <code>null</code>. The second index is the value, which can be <code>null</code>.
	 * 
	 * @return Array containing two elements, the key and value of the entry read at the current line.
	 * @throws CSMalformedText if the current line does not contain an entry on it, or it is at an invalid scope, or the key at the current line
	 * 		   is <code>null</code>.
	 * @throws IOException if an IO error occurs during reading the current line.
	 * @throws NullPointerException if this CSTextIO is initialized for writing.
	 */
	public String[] getEntry() throws CSMalformedText, IOException {
		
		String[] entry = new String[2];
		getEntry(entry , 0);
		return entry;
		
	}
	
	/**
	 * <p>
	 * 	<b>First and Foremost:</b> The string passed to {@code valueParser} can be <code>null</code>, so {@code valueParser} should handle this case.
	 * 	However, {@code keyParser} will never receive a <code>null</code> input.
	 * </p>
	 * 
	 * Returns an entry created via {@link Map#entry(Object, Object) Map.entry} containing the entry read from the file reader's current position.
	 * 
	 * @param <K> type of key stored in the resulting entry
	 * @param <V> type of value stored in the resulting entry
	 * @param keyParser function that parses the key read from the file
	 * @param valueParser function that parses the value read from the file
	 * @return {@link Entry} containing the parsed key and value, created from {@code Map.entry()}.
	 * @throws IOException if an IO error occurs while reading the file.
	 * @throws CSMalformedText if the file is malformed for reading an entry from it, either because the current scope is unbalanced or the 
	 * 		   current line does not contain an entry.
	 * @throws NullPointerException if this CSTextIO is initialized for writing.
	 */
	public <K , V> Entry<K , V> getEntry(Function<String , K> keyParser , Function<String , V> valueParser) throws IOException, CSMalformedText {
		
		Objects.requireNonNull(keyParser);
		Objects.requireNonNull(valueParser);		
		String[] entry = getEntry();		
		return Map.entry(keyParser.apply(entry[0]), valueParser.apply(entry[1]));
		
	}
	
	/**
	 * Gets an item from the file at the current line. The item's value's type defaults to {@code String}.
	 * 
	 * @return Named item.
	 * @throws IOException if an IO error occurs while reading the file.
	 * @throws CSMalformedText if the file is malformed for reading an item from it, either because the current scope is unbalanced or the current
	 *  	   line does not contain an item.
	 * @throws NullPointerException if this CSTextIO is initialized for writing.
	 */
	public CSNamedItem<String> getItem() throws IOException, CSMalformedText {
		
		ScopeTestResult result = readAndTestScope();
		if(!result.isValid()) throw new CSMalformedText("Cannot read item from file becase: " + result.toString());
		String line = result.readString;
		int equalIndex = line.lastIndexOf(ITEM_NAME_VALUE_SEPARATOR);
		if(equalIndex == -1) throw new CSMalformedText("Item name-value separator not found.");
		String name = line.substring(0 , equalIndex);
		if(name.equals("null")) throw new CSMalformedText("The name of the item is \"null\" which is disallowed.");
		return new CSNamedItem<>(name , line.substring(equalIndex + ITEM_NAME_VALUE_SEPARATOR.length()));
		
	}
	
	/**
	 * <p>
	 * 	<b>First and Foremost:</b> The string passed to {@code parser} can be <code>null</code>, so {@code parser} should handle this case.
	 * </p>
	 * Gets an item from the file at the current line. The item's value is parsed via {@code parser}.
	 * 
	 * @param <T> type of the item's value
	 * @param parser a function which converts a string to a value
	 * @return Named item containing the name of the item and the item's value
	 * @throws CSMalformedText if the file is malformed for reading an item from it, either because the current scope is unbalanced or the current
	 *  	   line does not contain an item.
	 * @throws IOException if an IO error occurs while reading the file.
	 * @throws NullPointerException if {@code parser} is <code>null</code> or if this CSTextIO is initialized for writing.
	 */
	public <T> CSNamedItem<T> getItem(Function<String , T> parser) throws CSMalformedText, IOException {
		
		Objects.requireNonNull(parser);
		CSNamedItem<String> asString = getItem();		
		return new CSNamedItem<>(asString.itemName() , parser.apply(asString.item()));
		
	}
	
	/**
	 * Ends a list get operation previously started with a call to {@link CSTextIO#startGetList() startGetList}. All items of the list must be read
	 * prior to invoking this method, that is to say, no additional items may be in the list.
	 * 
	 * @throws CSMalformedText if no call to {@code startGetList()} was made prior to this call.
	 * @throws IOException if an IO error occurs during reading.
	 * @throws IllegalStateException if there appear to be more items in the list after the position of the file's reader. 
	 * @throws NullPointerException if this CSTextIO is initialized for writing.
	 */
	public void endGetList() throws CSMalformedText, IOException {
		
		if(scope == 0) throw new CSMalformedText("No list was started with startGetList().");
		reader.mark(999);
		String line = reader.readLine();
		
		if(line != null) {
			
			int scopes = line.lastIndexOf(SCOPE) + 1;
			if(scopes == scope) { 
				
				reader.reset();
				throw new IllegalStateException("There are more elements in the current list that have not been read yet, cannot end list get.");
				
			}
		
		}
		
		reader.reset();
		scope--;
		
	}
	
	/*
	 
	 MISC OPERATIONS
	 
	 */
	
	/**
	 * Returns whether this CSTextIO is initialized for reading a file.
	 * 
	 * @return {@code true} if this CSTextIO is initialized for reading a file.
	 */
	public boolean isReader() {
		
		return reader != null;
		
	}

	/**
	 * Returns whether this CSTextIO is initialized for writing a file.
	 * 
	 * @return {@code true} if this CSTextIO is initialized for writing a file.
	 */
	public boolean isWriter() {
		
		return writer != null;
		
	}
	
	/*
	 
	 PRIVATE METHODS
	 
	 */
	
	private void readScope() throws IOException {
		
		for(int i = 0 ; i < scope ; i++) reader.read();
				
	}
	
	private ScopeTestResult readAndTestScope() throws IOException {
		
		reader.mark(999);
		String line = reader.readLine();
		if(line == null) return ScopeTestResult.END_OF_FILE;
		int numberScopes = line.lastIndexOf(SCOPE) + 1;				
		if(numberScopes != scope) {
			
			reader.reset();
			return ScopeTestResult.INVALID_SCOPE;
			
		}
		
		ScopeTestResult result = ScopeTestResult.VALID_SCOPE;
		result.readString = line.substring(numberScopes); 
		return result;
		
	}
	
	private void writeScope() throws IOException {
		
		for(int i = 0 ; i < scope ; i++) writer.write(SCOPE);
		
	}
	
	private void writeNewline() throws IOException {
		
		writer.write(LINE_FEED);
		
	}
	
	private TitleAndSize getTitleAndSize() throws IOException, CSMalformedText {
		
		readScope();
		String titleLine = reader.readLine();
		if(titleLine == null) throw new CSMalformedText("File empty, read attempted.");
		char[] chars = titleLine.toCharArray();
		//first char is ')'
		int i = titleLine.lastIndexOf('(');
		int length;
		String title;
		if(i == -1) { 
			
			length = -1;
			title = titleLine;
			
		} else {
			
			//the length of the string - 2 for the parentheses, the length of the list ending, and i for the starting place
			int sizeLength = chars.length - (2 + LIST_TITLE_ENDING.length()) - i;
			if(sizeLength <= 0) throw new CSMalformedText("Size component of title line malformed.");	
			try {
				
				length = Integer.parseInt(new String(chars , i + 1 , sizeLength));
				
			} catch(NumberFormatException e) {
				
				throw new CSMalformedText("Failed to parse size at title line.");
				
			}
			
			title = titleLine.substring(0 , i);
			
		}
		return new TitleAndSize(title , length);
		
	}
	
	private void writeString(String x) throws IOException {
		
		writeScope();
		writer.write(x);
		writeNewline();
		
	}
	
	@Override public void close() throws IOException {

		if(reader != null) reader.close();
		if(writer != null) writer.close();
		
	}
	
	private record TitleAndSize(String title , int size) {}
	
	/**
	 * Returned from {@link CSTextIO#readAndTestScope()}. Used to tell whether a get operation was done in a valid scope. 
	 */
	private enum ScopeTestResult {
		
		END_OF_FILE ,
		INVALID_SCOPE ,
		VALID_SCOPE;
		
		String readString = null;

		public boolean isValid() {
			
			return this == VALID_SCOPE;
			
		}
		
	}
	
}
