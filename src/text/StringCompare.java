package text;

// abstract class that will compare two strings
public abstract class StringCompare {

	/**
	 * Compares the similarity of two strings.
	 * 
	 * @param str1	The first string to use in the comparison.
	 * @param str2	The second string to use in the comparison.
	 * @return		How similar the two strings are, where 0.0 means not at all, 1.0 means identical.
	 */
	public abstract double compare(String str1, String str2);
	
}
