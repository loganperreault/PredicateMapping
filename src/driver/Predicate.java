package driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Predicate {
        
    // the name of the file the predicate reads from
    public String filename;
    // the name of the predicate, using the name of the file only (no directory)
    public String name;
    // the confidence value associated with matched predicates from an algorithm
    private double confidence = -1.0;
    // a map relating a word (line of text) to the number of times that word appears
    private Map<String, Integer> words = new HashMap<String, Integer>();
    // an iterator used for looping through the words in the predicate
    Iterator<Entry<String, Integer>> it;
    // an arrayList for getting entries in the predicate
    ArrayList<Map.Entry<String, Integer>> list = null;
    // the percent of lines a string must appear to be considered a constant
    double constantValue = 1.0;
    
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
        } catch (IOException e) {        System.out.println("Failed to read file: "+filename);        }
    }
    
    // reads and parses the file, adding word counts to the internal list
    private void read(String filename) throws IOException {
        // store filename information
        File file = new File(filename);
        name = file.getName();
        // create reader for file
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str = null;

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
        words = new TreeMap<String, Integer>(words);
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
    
    public ArrayList<Map.Entry<String, Integer>> getEntries() {
    	list = new ArrayList();
    	list.addAll(words.entrySet());
    	return list;
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
     * @param key        The word to count.
     * @return                The number of times the word appears.
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
     * @param key        The word to get the probability for.
     * @return                The probability within the range [0.0,1.0].
     */
    public double getProbability(String key) {
        return ((double)getCount(key) / size());
    }
    
    /**
     * @return        The number of distinct words in the predicate.
     */
    public int size() {
        return words.size();
    }
    
    // sets the confidence level for how well the match
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    // retrieve the confidence associated with a matched predicate
    public double getConfidence() {
        return this.confidence;
    }
    
    // clear the list of words to save space (may read from file again if necessary)
    public void free() {
        words.clear();
    }
    
    /**
     * Removes constants, or "stop words". This will  
     * remove text that has been tagged to every field.
     */
    public void stripConstants() {
    	
    	// loop through all lines in the predicate file
    	Iterator<Entry<String, Integer>> it = words.entrySet().iterator();
    	Map.Entry<String, Integer> entry = null;
    	List<String> toRemove = null;
    	List<Integer> toRemoveCount = null;
    	int count = 0;
    	while (it.hasNext()) {
    		entry = it.next();
    		
    		// tokenize the line by whitespace
    		List<String> tokens = new ArrayList<String>(Arrays.asList(entry.getKey().split("\\s+")));
    		
    		// only look for constants in lines with more than 1 token
    		if (tokens.size() > 1) {
	    		if (toRemove == null) {
	    			// if this is the first line, get a list of all the words
	        		// and add to a list of words to remove
					toRemove = new ArrayList<String>();
					toRemoveCount = new ArrayList<Integer>();
					for (String token : tokens) {
		    			toRemove.add(token);
		    			toRemoveCount.add(1);
		    		}
				} else {
					// count the times a word reappears in all lines
					for (int i = 0; i < toRemove.size(); i++) {
						String removeItem = toRemove.get(i);
						if (tokens.contains(removeItem)) {
							toRemoveCount.set(i, toRemoveCount.get(i) + 1);
						}
					}
				}
	    		count++;
    		}
    	}
    	
    	// if all string contained 1 token, return
    	if (toRemove == null)
    		return;
    	
    	// only remove constants that appear often enough
    	List<String> newRemove = new ArrayList<String>();
    	for (int i = 0; i < toRemove.size(); i++) {
    		if (toRemoveCount.get(i) > (count * constantValue)) {
    			newRemove.add(toRemove.get(i));
    			System.out.println("Removing "+toRemove.get(i));
    		}
    	}
    	toRemove = newRemove;
    	
    	// loop through all the lines in the predicate, removing the constants
    	// that are shared by all predicates
    	Map<String, Integer> newWords = new HashMap<String, Integer>();
    	it = words.entrySet().iterator();
    	for (entry = it.next(); it.hasNext(); entry = it.next()) {
    		String key = entry.getKey();
    		Integer value = entry.getValue();
    		// remove constants
    		for (String removeItem : toRemove) {
    			key = key.replace(removeItem, "");
    		}
    		newWords.put(key, value);
    	}
    	
    	// set list to newly constructed list
    	words = newWords;
    	
    }
    
    public void setConstantValue(double constantValue) {
    	this.constantValue = constantValue;
    }
    
    public void printWords() {
    	Iterator<Entry<String, Integer>> it = words.entrySet().iterator();
    	Map.Entry<String, Integer> entry = null;
    	while (it.hasNext()) {
    		entry = it.next(); 
    		System.out.println(entry.getKey()+"    "+entry.getValue());
    	}
    }

}
