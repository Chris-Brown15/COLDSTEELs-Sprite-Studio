package cs.csss.misc.nio;

import java.io.IOException;
import java.util.LinkedHashMap;

import cs.csss.misc.files.CSFolder;

public class StreamFileFormat implements FileFormat , FileFormatWriter {

	public static void main(String[] args) {
		
		CSFolder root = CSFolder.establishRoot("test_root_2");
		
		System.out.println(root.getRealPath());
		
		StreamFileFormat format = new StreamFileFormat(root, "example");
		
		format
			.addChunk("header")
				.addInt("int")
				.addShort("short")
			.endFormat()
			.addChunk("body")
				.addRepetition("repetition")
					.addLong("example")
					.addByte("caca")
					.addString("meme")
					.addRepetition("second repetition")
						.addByteArray("layer")
					.endFormat()
				.endFormat()
			.endFormat()
			.addChunk("footer")
				.addString("this is a string");
	
	}
	
	protected LinkedHashMap<String , FormatItem> format = new LinkedHashMap<>();
	
	private FormatItem currentItem;
	
	public StreamFileFormat(CSFolder location , String fileName) {

	}

	@Override public void write() throws IOException {

		
		
	}

	@Override public void read() throws IOException {

		
		
	}

	@Override public FileFormat addByte(String entryName) {

		if(currentItem == null) format.put(entryName, currentItem = new FormatItem(entryName , FormatItem.BYTE , this));
		else currentItem.addByte(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addShort(String entryName) {

		if(currentItem == null) format.put(entryName, currentItem = new FormatItem(entryName , FormatItem.SHORT , this));
		else currentItem.addShort(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addInt(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.INT , this));
		else currentItem.addInt(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addLong(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.LONG , this));
		else currentItem.addLong(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addFloat(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.FLOAT , this));
		else currentItem.addFloat(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addDouble(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.DOUBLE , this));
		else currentItem.addDouble(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addBoolean(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.BOOLEAN , this));
		else currentItem.addBoolean(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addChar(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.CHAR , this));
		else currentItem.addChar(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addString(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.STRING , this));
		else currentItem.addString(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addByteArray(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.BYTE_ARRAY , this));
		else currentItem.addByteArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addShortArray(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.SHORT_ARRAY , this));
		else currentItem.addShortArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addIntArray(String entryName) {

		if(currentItem == null)  format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.INT_ARRAY , this));
		else currentItem.addIntArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addLongArray(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.LONG_ARRAY , this));
		else currentItem.addLongArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addFloatArray(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.FLOAT_ARRAY , this));
		else currentItem.addFloatArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addDoubleArray(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.DOUBLE_ARRAY , this));
		else currentItem.addDoubleArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addBooleanArray(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.BOOLEAN_ARRAY , this));
		else currentItem.addBooleanArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addCharArray(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.CHAR_ARRAY , this));
		else currentItem.addCharArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addStringArray(String entryName) {

		if(currentItem == null) format.put(entryName , currentItem = new FormatItem(entryName , FormatItem.STRING_ARRAY , this));
		else currentItem.addStringArray(entryName);		
		return currentItem;
		
	}

	@Override public FileFormat addChunk(String name) {

		format.put(name, currentItem = new FormatItem(name , FormatItem.EMPTY , this));
		return currentItem;

	}

	@Override public FileFormat addRepetition(String name) {

		if(currentItem == null) format.put(name, currentItem = new FormatItem(name , FormatItem.REPETITION , this));
		else currentItem.addRepetition(name);
		return currentItem;

	}

	@Override public FileFormat endFormat() {
		
		return currentItem.parent.orElseThrow();
	
	}

}
