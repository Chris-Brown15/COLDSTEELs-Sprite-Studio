package cs.csss.misc.files;

import static cs.core.utils.CSUtils.specify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Consumer;

import cs.core.utils.data.CSCHashMap;

/**
 * Representation of a directory within a virutal file system. This is used in software to easily locate directories or files within virtual
 * root directories as well as create both subdirectories and files.
 * 
 * <p>
 * Directories as modeled by this class follow two rules of their existence.
 * <ol>
 * 	<li> 
 * 		They must always exist.
 * 	</li>
 * 	<li>
 * 		They must map directly to a corresponding directory of the underlying file system.
 * 	</li>
 * 	</ol>
 * The same rules are followed by the {@link cs.csss.misc.files.CSFile CSFile} class, used to model files within the virtual file system. 
 * </p>
 * 
 * 
 * @author Chris Brown
 *
 */
public final class CSFolder {

	public static final String separator = File.separator;
	
	private static final CSCHashMap<CSFolder , String> roots = new CSCHashMap<>(7);
	
	public static synchronized final CSFolder establishRoot(final String rootName) {
		
		CSFolder root = new CSFolder(rootName);
		root.seekExistingFiles();  
		roots.put(root , rootName);
		return root;		
		
	}
	
	public static synchronized final CSFolder getRoot(final String name) {
		
		specify(roots.hasKey(name) , name + " is not a root.");
		
		return roots.get(name);
		
	}
	
	public final String name;
	
	private final CSFolder parent;
	
	private final ConcurrentHashMap<String , CSFolder> subdirectories = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String , CSFile> files = new ConcurrentHashMap<>();
	
	/**
	 * Creates a root directory.
	 * 
	 * @param name — name of this directory
	 */
	private CSFolder(final String name) {
		
		this.name = name;
		parent = null;
		mkDir();
		
	}

	/**
	 * Creates a directory with the given name and parent directory.
	 * 
	 * @param name — name of this directory
	 * @param parent — parent to this directory, who will have a reference to this directory and who will be this directory's parent.
	 */
	public CSFolder(final String name , final CSFolder parent) {

		specify(parent , "This directory must have a parent directory.");
		
		this.parent = parent;
		this.name = name;
		parent.addSubdirectory(this);
		mkDir();
		
	}
	
	/**
	 * Creates a subdirectory of the calling directory with the given name.
	 * 
	 * @param name — name of a new directory
	 * @return Subdirectory created by this method.
	 */
	public CSFolder createSubdirectory(String name) { 
		
		CSFolder newSub = new CSFolder(name , this);
		addSubdirectory(newSub);		
		return newSub;
		
	}
	
	/**
	 * Adds a file to this directory.
	 * 
	 * <p>
	 * @see {@link cs.csss.misc.files.CSFile CSFile}.
	 * </p>
	 * 
	 * @param name — name of a file to add to this directory
	 * @param fileContents — file composition object modeling the contents of this file
	 * @return CSFile object added to this directory.
	 * @throws IOException if creating a the file throws an IO execption.
	 */
	public CSFile createFile(String name , FileComposition fileContents) {
		
		specify(fileContents , "Files must not be null.");
		
		CSFile file = new CSFile(this , name , fileContents);
		file.write();		
		files.put(name , file);
		
		return file;
		
	}
	
	/**
	 * Bulk creates subdirectories from the given names.
	 * 
	 * <p>
	 * 	The created subdirectories are returned as an array and they are ordered according to the order of names passed to this method.
	 * </p>
	 * 
	 * @param names — variadic list of names
	 * @return Array containing created directories.
	 */
	public CSFolder[] createSubdirectories(String...names) {
		
		CSFolder[] directories = new CSFolder[names.length];
		
		for(int i = 0 ; i < names.length ; i++) {
			
			directories[i] = createSubdirectory(names[i]);
			addSubdirectory(directories[i]);
			
		}
		
		return directories;
		
	}
	
	/**
	 * Seeks through the real directory this file maps to and finds any files that are not already present in this directory, adding them.
	 */
	public void seekExistingFiles() {
		
		File asFile = asFile();
		File[] realDirectories = asFile.listFiles(name -> Files.isDirectory(name.toPath()));
		File[] realFiles = asFile.listFiles(name -> !Files.isDirectory(name.toPath()));
		
		KeySetView<String, CSFile> fileNames = files.keySet();
		KeySetView<String, CSFolder> directoryNames = subdirectories.keySet();
		
		for(File x : realFiles) { 
			
			if(fileNames.contains(x.getName())) continue;
			addFile(new CSFile(this , x.getName()));
			
		}
		
		for(File x : realDirectories) {
			
			if(directoryNames.contains(x.getName())) continue;;
			CSFolder asDirectory = createSubdirectory(x.getName());
			asDirectory.seekExistingFiles();
			
		}
		
	}
	
	
	/**
	 * Bulk creates files whose compositions are modeled by the single {@code FileComposition} composition and whose names are 
	 * {@code names}. 
	 * 
	 * <p>
	 * 	The created files are returned as an array and they are ordered according to the order of names passed to this method.
	 * </p>
	 * 
	 * @param composition — file composition each file will share
	 * @param names — names of files
	 * @return Array containing the created files.
	 */
	public CSFile[] createFiles(FileComposition composition , String...names) {
		
		CSFile[] files = new CSFile[names.length];		
		for(int i = 0 ; i < names.length ; i++) files[i] = createFile(names[i] , composition);		
		return files;
		
	}
	
	/**
	 * Bulk creates files whose names and compositions are modeled by the provided arrays.
	 * 
	 * <p>
	 * 	The create files are returned as an array. The files within the array are made by selecting the name and composition of the 
	 *	corresponding arrays at the same index and creating a file from them.  
	 * </p>
	 * 
	 * @param names — array of names for files
	 * @param compositions — file compositions for files
	 * @return Array containing files created from the given arrays.
	 */
	public CSFile[] createFiles(String[] names , FileComposition[] compositions) {
		
		specify(names.length == compositions.length , "Inequal number of elements for file name array and file composition array.");

		CSFile[] files = new CSFile[names.length];		
		for(int i = 0 ; i < names.length ; i++) files[i] = createFile(names[i] , compositions[i]);
					
		return files;
		
	}
	
	/**
	 * <b>
	 * 	First and foremost: <br>
	 * </b>
	 * The directory calling this method must be asigned to its result, as in 
	 * {@code directory = directory.moveToSubdirectoryOf(newParent);}.
	 * 
	 * <p>
	 * 	This method moves this directory into a subdirectory of another directory, removing it from the directory it is currently in.
	 * 	This method can therefore not be called on a root directory. This directory is copied, as in 
	 * 	{@linkplain CSFolder#copy(CSFolder) copy}, and then deleted. Like with {@link CSFolder#delete() delete}, the object who called
	 *  this method is no longer valid, and as shown above, this method's return is the moved directory.
	 * </p>
	 * 
	 * @param newParent — the directory to move this directory into
	 * @return The new instance of this directory.
	 * @throws IOException if any IO exception occurs.
	 */
	public CSFolder moveToSubdirectoryOf(CSFolder newParent) throws IOException {
		
		specify(parent , "Cannot make a root directory a subdirectory.");
		specify(newParent , "New parent directory must not be null.");
		
		CSFolder newInstance = copy(newParent);
		
		delete();
		
		return newInstance;
		
	}
		
	/**
	 * Iteratively constructs and returns the virtual file path of this directory. 
	 * 
	 * <p>
	 * 	Virtual file paths will be file paths naming directories descending from the root. The root will be the first file name and any 
	 * 	subdirectories of it will appear after each other, separated by the {@link CSFolder#separator separator}.
	 * </p>
	 * 
	 * @return The virtual file path of this directory. 
	 */
	public String getVirtualPath() {
		
		String path = separator;
				
		CSFolder iter = this;

		do {

			path = iter.name + path;
			if(iter.parent != null) path = separator + path;
			iter = iter.parent;
			
		} while(iter != null);
		
		return path;
		
	}
	
	/**
	 * Determines the real path of this directory.
	 * 
	 * <p>
	 * 	Determines the real path of this directory by creating a {@link java.io.File File} object and getting it's absolute path.
	 * </p>
	 * 
	 * @return The real path of this directory.
	 */
	public String getRealPath() {
		
		return asFile().getAbsolutePath();
		
	}
	
	/**
	 * Creates a {@link java.io.File File} representing this object.
	 * 
	 * @return {@code File} representation of this object.
	 */
	public File asFile() {
		
		return new File(getVirtualPath() + separator);
		
	}

	/**
	 * Returns whether the given directory is a subdirectory of this. A directory is for the purposes of this method a subdirectory if it is
	 * either a subdirectory of this directory or one of this directory's subdirectories. 
	 * 
	 * @param subdirectory — a directory
	 * @return {@code true} if {@code subdirectory} is a subdirectory of this.
	 */
	public boolean isSubdirectory(CSFolder subdirectory) {
		
		if(subdirectories.containsKey(subdirectory.name)) return true;
		for(CSFolder sub : subdirectories.values()) if(sub.isSubdirectory(subdirectory)) return true;		
		return false;
		
	}
	
	/**
	 * Deletes the given directory who is a subdirectory of this directory, requiring that {@code subdirectory} is indeed a subdirectory.
	 * 
	 * @param subdirectory — a subdirectory to delete
	 * @throws IOException if any exception is thrown during the deletion process.
	 */
	public void deleteSubdirectory(CSFolder subdirectory) throws IOException {
		
		specify(isSubdirectory(subdirectory) , subdirectory.name + " is not a subdirectory of this directory.");		
		subdirectory.delete();
		
	}

	/**
	 * Copies this directory into the given directory, returning the copy.
	 * 
	 * <p>
	 * 	The exact contents of this directory are duplicated into a directory of the same name as this one which will be a subdirectory of
	 * 	{@code copyInto}. It is valid for {@code copyInto} to be {@code null}, in which the given directory has no parent, but this does
	 * 	<b>NOT</b> make it a root directory. Most operations such a directory will result in a null pointer exception being thrown.
	 * </p>
	 * 
	 * @param copyInto — a directory to make a copy of this directory into as a subdirectory
	 * @return The copied directory, whose parent is {@code copyInto}.
	 */
	public CSFolder copy(CSFolder copyInto) {
		
		CSFolder copy = new CSFolder(name , copyInto);
		
		for(CSFile file : files.values()) copy.createFile(file.name, file.composition);
		
		for(CSFolder subdirectory : subdirectories.values()) subdirectory.copy(copy);
		
		return copy;
		
	}
	
	/**
	 * Permanently deletes this directory and all subdirectories and files within it. 
	 * 
	 * <p>
	 * 	Any directory that calls this method is no longer valid and points to nothing. Any attempts to use it will fail and throw errors.
	 * </p>
	 * 
	 *  @throws IOException if an exception occurs in the process of deleting this directory.
	 */
	public void delete() throws IOException {
		
		String realPath = getRealPath();
		
		//deletes all nondirectories
		for(CSFile file : files.values()) {
			
			File asFile = new File(realPath + separator + file.name);
			asFile.delete();
			
		}
		
		files.clear();
		
		//delete directories by recursively going down the subdirectory tree and deleting directories that have no subdirectories, then
		//delete the current one
		
		for(CSFolder directory : subdirectories.values()) directory.delete();
		
		//not needed but used anyway
		subdirectories.clear();

		//use the Files.getRealPath() to delete because we want to get IO exceptions that occur.
		Files.delete(Paths.get(getRealPath()));		
		remove();
				
	}
	
	/**
	 * Gets the subdirectory of the given name, requiring such a directory to be found.
	 * 
	 * @param name — name of a subdirectory within this directory
	 * @return The given subdirectory.
	 */
	public CSFolder getSubdirectory(String name) {
		
		CSFolder result = subdirectories.get(name);
		specify(result , name + " is not in this directory.");		 
		return result;
		
	}
	 
	/**
	 * Gets an existing subdirectory of the given name, or creates a new, empty one if none is found.
	 * 
	 * @param name — name of a subdirectory within this directory
	 * @return An existing subdirectory if one exists already, or a new empty one.
	 */
	public CSFolder getOrCreateSubdirectory(String name) {
		
		CSFolder result = subdirectories.get(name);
		if(result == null) return createSubdirectory(name);
		return result;
		
	}
	 
	public CSFile getFile(String name) {
		
		CSFile file = files.get(name);
		specify(file , name + " is not in this directory.");
		return file;
		
	}
	
	/**
	 * Returns an iterator over the subdirectories of this directory. 
	 * 
	 * <p>
	 * 	This iterator supports all methods of {@code Iterator}, but note that {@code remove} deletes the directory last returned via 
	 * {@code next}.
	 * </p>
	 * 
	 * @return Iterator over the subdirectories of this directory.
	 */
	public Iterator<CSFolder> subdirectories() {
		
		return new Iterator<>() {
			
			private final Iterator<CSFolder> sourceIter = subdirectories.values().iterator();

			private CSFolder next = null;
			
			@Override public boolean hasNext() {

				return sourceIter.hasNext();
				
			}

			@Override public CSFolder next() {

				return next = sourceIter.next();
				
			}
			
			@Override public void remove() {
				
				try {
					
					deleteSubdirectory(next);
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}			
		 	
		};
		
	}
	
	/**
	 * Invokes the given callback for each subdirectory of this directory, not including this directory, in a preorder traversal approach.
	 * 
	 * <p>
	 * 	The bottom subdirectories of this file structure are reached before the invokations to {@code callback} begin. 
	 * </p>
	 * 
	 * @param callback — callback for each subdirectory of this directory
	 */
	public void forEachSubdirectoryPreorder(Consumer<CSFolder> callback) {
		
		//preorder traversal of subdirectories of this by first reaching the end of the subdirectory tree and unwinding recursion while
		//invoking the callback
		subdirectories().forEachRemaining(directory -> {

			directory.forEachSubdirectoryPreorder(callback);
			callback.accept(directory);
			
		});
		
	}
	
	/**
	 * Invokes the given callback for each subdirectory of this directory, not including this directory, in a postorder traversal approach.
	 * 
	 * <p>
	 * 	The bottom subdirectories of this file structure are reached last and will be the last directories to be accepted by the callback.
	 * </p>
	 * 
	 * @param callback — callback for each subdirectory of this directory
	 */
	public void forEachSubdirectoryPostorder(Consumer<CSFolder> callback) {
		
		//postorder traversal of subdirectories for all subdirectories. invoke on a sub and then recurse to its subdirectories.
		subdirectories().forEachRemaining(directory -> {

			callback.accept(directory);
			directory.forEachSubdirectoryPostorder(callback);
			
		});
		
	}

	/**
	 * Returns an iterator over the files within this directory.
	 * 
	 * @return Iterator over the files within this directory.
	 */
	public Iterator<CSFile> files() {
		
		return files.values().iterator();
		
	}
	
	public String toString() {
		
		return "Directory " + name;
		
	}
	
	/**
	 * Removes this directory from view of its parent and sibling directories but does not delete this directory.
	 */
	protected void remove() {
		
		specify(parent != null , "Cannot remove a root directory.");
		parent.subdirectories.remove(this.name);
		
	}
	
	protected final void mkDir() {
		
		if(Files.exists(Paths.get(getRealPath()))) return;
		
		try {
			
			if(parent != null) Files.createDirectory(Paths.get(parent.getRealPath() + separator + name));
			else Files.createDirectory(Paths.get(name));
			
		} catch (IOException e) {

			e.printStackTrace() ; throw new IllegalStateException("Failed to create directory.");
			
		}
		
	}

	/**
	 * Adds an existing Directory to the calling directory. 
	 * 
	 * @param directory — a directory to add.
	 */
	private void addSubdirectory(CSFolder directory) {
		
		specify(directory != this , "A file cannot be its own subdirectory.");
		
		subdirectories.put(directory.name , directory);
		
	}
	
	private void addFile(CSFile file) {
				
		files.put(file.name, file);
		
	}

}
