package cs.csss.misc.nio;

import static cs.core.utils.CSUtils.require;

import java.util.LinkedHashMap;
import java.util.Optional;

public class FormatItem implements FileFormat {

	public final byte type;
	public final String name;
	
	protected Optional<FileFormat> parent;
	
	protected LinkedHashMap<String , FormatItem> subChunks = new LinkedHashMap<>();
	
	protected FormatItem(String name , byte type) {

		require(name);
		
		FileFormat.verifyType(type);
		
		this.type = type;
		
		this.name = name;
		parent = Optional.empty();
		
	}

	protected FormatItem(String name , byte type , FileFormat parent) {

		require(parent != this);

		FileFormat.verifyType(type);
		
		this.type = type;
		
		this.name = name;
		this.parent = Optional.of(parent);
		
	}

	@Override public FileFormat addByte(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.BYTE , this));
		return this;
		
	}

	@Override public FileFormat addShort(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.SHORT , this));
		return this;
		
	}

	@Override public FileFormat addInt(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.INT , this));
		return this;
		
	}

	@Override public FileFormat addLong(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.LONG , this));
		return this;
		
	}

	@Override public FileFormat addFloat(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.FLOAT , this));
		return this;
		
	}

	@Override public FileFormat addDouble(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.DOUBLE , this));
		return this;
		
	}

	@Override public FileFormat addBoolean(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.BOOLEAN , this));
		return this;
		
	}

	@Override public FileFormat addChar(String entryName) {

		require(!subChunks.containsKey(entryName));
		subChunks.put(entryName, new FormatItem(name , FileFormat.CHAR , this));
		return this;
		
	}

	@Override public FileFormat addString(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.STRING , this));
		return this;
		
	}

	@Override public FileFormat addByteArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.BYTE_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addShortArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.SHORT_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addIntArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.INT_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addLongArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.LONG_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addFloatArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.FLOAT_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addDoubleArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.DOUBLE_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addBooleanArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.BOOLEAN_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addCharArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.CHAR_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addStringArray(String entryName) {

		setupConditions(name);
		subChunks.put(entryName, new FormatItem(name , FileFormat.STRING_ARRAY , this));
		return this;
		
	}

	@Override public FileFormat addChunk(String name) {

		setupConditions(name);
		FormatItem newItem = new FormatItem(name , FileFormat.EMPTY , this);
		subChunks.put(name, newItem);
		return newItem;
		
	}

	@Override public FileFormat addRepetition(String name) {
		
		setupConditions(name);
		FormatItem newItem = new FormatItem(name , FormatItem.REPETITION , this);
		return newItem;
	
	}

	@Override public FileFormat endFormat() {
		
		require(parent.isPresent());
		return parent.get();
		
	}
	
	private void setupConditions(String name) {
		
		if(subChunks == null) subChunks = new LinkedHashMap<>();
		require(!subChunks.containsKey(name));
		
	}

}
