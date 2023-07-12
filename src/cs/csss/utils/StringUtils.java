package cs.csss.utils;

public final class StringUtils {

	/**
	 * Removes any instances of more than one space character, replacing them with a single space.
	 * 
	 * @param text — a string whose instances of multiple contiguous spaces are to be removed 
	 * @return String containing the contents of {@code text}, sans instances of multiple spaces, which are replaced by single spaces.
	 */
	public static String removeMultiSpaces(String text) {

		char[] chars = text.toCharArray();
		StringBuilder sanitized = new StringBuilder(chars.length);
		boolean onSpace = false;
		
		for(int i = 0 ; i < chars.length ; i++) {
			
			if(chars[i] == ' ')  {
				
				if(!onSpace) sanitized.append(chars[i]);				
				onSpace = true;
				
			} else { 
				
				onSpace = false;
				sanitized.append(chars[i]);
				
			}
			
		}
		
		return sanitized.toString();
		
	}
	
	private StringUtils() {

	}

}
