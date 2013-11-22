package driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Predicate {
	
	// the name of the file the predicate reads from
	private String filename;
	// the name of the predicate, using the name of the file only (no directory)
	private String name;
	// the confidence value associated with matched predicates from an algorithm
	private double confidence = -1.0;
	// a map relating a word (line of text) to the number of times that word appears
	private Map<String, Integer> words = new HashMap<String, Integer>();
	// an iterator used for looping through the words in the predicate
	Iterator<Entry<String, Integer>> it;
	
	// Predicates with no file are blank
	public Predicate() {
		name = ".NULL";
	}
	
	// creates a predicate tied to a file
	public Predicate(String filename) {
		this.filename = filename;
		populate();
	}
	
	// populates the predicate's word list by reading the filename
	public void populate() {
		try {
			read(filename);
		} catch (IOException e) {	System.out.println("Failed to read file: "+filename);	}
	}
	
	// reads and parses the file, adding word counts to the internal list
	private void read(String filename) throws IOException {
		// store filename information
		File file = new File(filename);
		name = file.getName();
		// create reader for file
		BufferedReader in = new BufferedReader(new FileReader(file));
        String str = null;
        
        // TODO: maybe this should count words using the string comparison metric?
        // or maybe we should just assume strings in the same database are correctly named
        
        // loop through file line by line
        while ((str = in.readLine()) != null) {
        	// count the number of times the word has been seen
        	if (words.containsKey(str))
        		words.put(str, words.get(str) + 1);
        	else
        		words.put(str, 1);
        }
        // cleanup
        in.close();
        it = words.entrySet().iterator();
	}
	
	// retrieve the first word-count pair in the list
	public Map.Entry<String, Integer> getStart() {
		it = words.entrySet().iterator();
		return getNext();
	}
	
	// retrieve the next word-count pair in the list
	public Map.Entry<String, Integer> getNext() {
		if (it.hasNext())
			return it.next();
		else
			return null;
	}
	
	// retrieve the terminator for the word-count pairs in the list
	public Map.Entry<String, Integer> getLast() {
		return null;
	}
	
	// return the name of the predicate
	public String toString() {
		return name;
	}
	
	// return the substring of the name after the last '.' token
	// NOTE: this may and does result in duplicate shortNames for a database.
	public String getShortName() {
		return name.substring(name.lastIndexOf('.')+1);
	}
	
	/**
	 * Retrieve the number of times a word appears in a predicate.
	 * 
	 * @param key	The word to count.
	 * @return		The number of times the word appears.
	 */
	public int getCount(String key) {
		if (words.containsKey(key))
			return words.get(key);
		else
			return 0;
	}
	
	/**
	 * Calculates the probability that a word is chosen from the predicate at random.
	 * 
	 * @param key	The word to get the probability for.
	 * @return		The probability within the range [0.0,1.0].
	 */
	public double getProbability(String key) {
		return ((double)getCount(key) / size());
	}
	
	/**
	 * @return	The number of distinct words in the predicate.
	 */
	public int size() {
		return words.size();
	}
	
	// sets the confidence level for how well the match
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	// retrieve the confidence assiciated with a matched predicate
	public double getConfidence() {
		return this.confidence;
	}
	
	// clear the list of words to save space (may read from file again if necessary)
	public void free() {
		words.clear();
	}

}
