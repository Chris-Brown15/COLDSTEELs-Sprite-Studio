package cs.csss.misc.io;

import static cs.core.utils.CSUtils.require;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;

import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;

public class StreamFileFormat implements FileFormat {

	public static void main(String[] args) throws IOException {
		
		CSFolder root = CSFolder.establishRoot("test_root");
		var st = new StreamFileFormat(root , "example");

		st.chunk("header")
			.addInt("int", 1203);
		
		st.chunk("body")
			.repeatingChunk("Repeating sequence" , 2)
				.addBoolean("repeat" , true)
				.addShort("iron" , (short) 0xfefe)
				.repeatingChunk("ex" , 2)	
					.addByte("meme" , (byte) 12)
					.addByte("test" , (byte) 13);

		st.chunk("footer")
			.addLong("ending", 0xcafe_babe_dead_cacaL);
		
		st
			.chunk("body")
				.repeatingChunk("Repeating sequence")
					.repeatingChunk("ex" , 0)
						.setByte("meme" , (byte)15 , 0);
		
		st.write();
		
		st.read();
		
		st.chunks.values().forEach(x -> System.out.println(x + "\n"));
		
	}
	
	private LinkedHashMap<String , ContainerChunk> chunks = new LinkedHashMap<>();
		
	private CSFolder folder;
	private String fileName;
	private Format currentFormat;
	
	public StreamFileFormat(CSFolder folder , String fileName) {

		require(folder);	
		require(fileName);	
			
		this.folder = folder;
		this.fileName = fileName;
		
	}
	
	public void setFolder(CSFolder newFolder) {

		require(newFolder);		

		this.folder = newFolder;
		
	}

	public void setFileName(String newFileName) {

		require(newFileName);	
		
		this.fileName = newFileName;
		
	}
	
	@Override public ContainerChunk chunk(String name) {
		
		require(name);
		
		ContainerChunk newChunk = chunks.get(name);		
		if(newChunk != null) return newChunk;
		
		newChunk = new ContainerChunk(name);
		addChunk(newChunk);
		
		return newChunk;
		
	}

	@Override public void addChunk(ContainerChunk chunk) {

		if(!chunks.containsKey(chunk.name)) { 
		
			chunks.put(chunk.name , chunk);
			currentFormat = chunk;
			
		}
		
	}

	@Override public void removeChunk(ContainerChunk chunk) {

		if(chunks.containsKey(chunk.name)) {
			
			chunks.remove(chunk.name);
			
			if(chunk == currentFormat) currentFormat = null;
		
		}
		
	}
	
	@Override public void write() throws IOException {

		CSFile.makeFile(folder, fileName);
		
		int largestObjectSize = getLargestEntrySize();
		ByteBuffer writeBuffer = ByteBuffer.wrap(new byte[largestObjectSize]);
		
		try(FileOutputStream stream = new FileOutputStream(folder.getRealPath() + CSFolder.separator + fileName)) {
			
			Collection<ContainerChunk> chunks = this.chunks.values();
			for(Chunk x : chunks) { 
				
				x.write(writeBuffer);
				writeBuffer.rewind();
				stream.write(writeBuffer.array() , 0, x.sizeOf());
				
			}
			
		} catch(FileNotFoundException e) {}
		
	}

	@Override public void read() throws IOException {

		ensureFileExists();
		
		try(FileInputStream stream = new FileInputStream(folder.getRealPath() + CSFolder.separator + fileName)) {

			ByteBuffer readBuffer = ByteBuffer.wrap(stream.readAllBytes());
			
			Collection<ContainerChunk> chunks = this.chunks.values();

			for(Chunk x : chunks) x.read(readBuffer);
			
		} catch(FileNotFoundException e) {}
		
	}
	
	private int getLargestEntrySize() {

		int largest = 0;
		Collection<ContainerChunk> chunks = this.chunks.values();		
		for(Chunk x : chunks) if(x.sizeOf() > largest) largest = x.sizeOf();
		return largest;
		
	}

	private void ensureFileExists() {

		require(Files.exists(Paths.get(folder.getRealPath() + CSFolder.separator + fileName)));
		
	}	

}
