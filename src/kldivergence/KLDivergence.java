package kldivergence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import text.StringCompare;
import driver.Predicate;

public class KLDivergence {
        
    // the smoothing term
    private double smooth = 10E-300;
    // the maximum number of words viewed in a predicate (0 = all)
    private int limit = 500;
    // the threshold for string similarity to start each predicate at
    private double thresholdStart = 0.75;
    // the minimum number of predicate candidates necessary to run algorithm
    int validRequired = 5;
    // the amount by which the threshold decreases if enough candidates were not found
    double thresholdStep = 0.02;
    // the minimum string similarity threshold to use before giving up (predicate does not exist)
    double thresholdMin = 0.50;
    // the list of results for each mapping made by the algorithm
    List<Results> results = new ArrayList<Results>();
    
    
    // global variables
    private double currentThreshold;
    private StringCompare compare;
    boolean echo = false;
    double percentFound;
    
    /**
     * Creates an instance of the algorithm.
     *
     * @param compare        The string similarity metric to be used.
     */
    public KLDivergence(StringCompare compare) {
        this.compare = compare;
    }
    
    /**
     * Runs the algorithm on a list of predicates, matching all local predicates
     * to remote predicates as accurately as possible.
     *
     * @param localPredicates        The list of predicates to search for in the remote database.
     * @param remotePredicates        The list of all possible predicates in the remote database.
     * @return                                        A mapping from local predicate to proposed remote predicate.
     */
    public Map<Predicate, Predicate> select(List<Predicate> localPredicates, List<Predicate> remotePredicates) {
        Map<Predicate, Predicate> matches = new HashMap<Predicate, Predicate>();
        // loop through list of local predicates
        for (Predicate predicate : localPredicates) {
            // run the algorithm, attempting to match the current local predicate to a predicate in the remote list
            Predicate match = select(predicate, remotePredicates);
            // free the word list for the current local predicate (not needed anymore)
            predicate.free();
            // add the new mapping
            matches.put(predicate, match);
            // print as we go (we can print the mapping that is returned, but it all prints at once)
            System.out.print(currentThreshold+": ");
            System.out.print(match.getConfidence()+": ");
            System.out.println(predicate.getShortName() + " ~ "+ match.getShortName());
        }
        return matches;
    }
    
    /**
     * Chooses a predicate's closest match based on a list of remote predicates.
     *
     * @param p1                        The local predicate that will be matched.
     * @param remotePredicates        The list of candidates the algorithm must choose from.
     * @return                                The predicate from the remote list which has been chosen.
     */
    public Predicate select(Predicate p1, List<Predicate> remotePredicates) {

        // set up
        Predicate best = null;
        List<Double> divergences = new ArrayList<Double>(remotePredicates.size());
        int validFound = 0;
        currentThreshold = thresholdStart;
        double currentThresholdStep = thresholdStep;
        double minDivergence = Double.MAX_VALUE;
        
        // print some information about the current run
        if (echo) {
            System.out.println("Testing "+remotePredicates.size()+" predicates.");
            System.out.println(p1);
        }
        
        // create a results structure for the current predicate
        Results currentResults = new Results(p1);
        
        // repeat process until a sufficient number of valid predicates have been found
        while (validFound < validRequired && currentThreshold > thresholdMin) {
        	
        	// reset results each time
        	currentResults.clear();
    	
            // setup
            validFound = 0;
            divergences.clear();
            minDivergence = Double.MAX_VALUE;
            
            // print what the current threshold is
            if (echo)
            	System.out.println("THRESH: "+currentThreshold+"\n");
            
            // loop through all remote predicates
            for (Predicate remotePredicate : remotePredicates) {
                    
                // compares local and remote predicates, storing the percent of shared words
                double divergence = compare(p1, remotePredicate);
                if (divergence < 1.0)
                    validFound++;
                
                // add to the current predicate's results structure
                currentResults.add(remotePredicate, divergence);
                
                // if there was actually words in common, store the divergence value in a list
                if (divergence < Double.MAX_VALUE) {
                    divergences.add(divergence);
                }
                
                // calculates the best predicate as the one with the lowest divergence
                if (divergence < minDivergence) {
                    minDivergence = divergence;
                    best = remotePredicate;
                }
            }
            
            // decrease the string similarity threshold to allow for more candidate predicates
            currentThreshold -= currentThresholdStep;
            
        }

        // if no best was found, use a NULL predicate
        if (best == null) {
            best = new Predicate();
        // store the best, flagging it as confident (1.0) if it was
        // an outlier and not confident (0.0) if it was not.
        } else {
            best.setConfidence(1.0);
        }
        
        results.add(currentResults);
                
        return best;
    }
    
    /**
     * Compare one predicate with another, calculating how similar
     * the two predicates really are.
     * This is where the actual KL-Divergence algorithm starts.
     *
     * @param p1        The first predicate to run on KL-Divergence
     * @param p2        The second predicate to run on KL-Divergence
     * @return                The value returned by KL-Divergence (smaller = more accurate)
     */
    public double compare(Predicate p1, Predicate p2) {
        
    	// setup
    	double divergence = 0.0;
    	Map.Entry<String, Integer> p1Word, p2Word;
    	int found = 0;
    	int i;
    
    	double norm = 0.0;
    	
    	// loop through all words in predicate 1
		for (i = 0, p1Word = p1.getStart(); p1Word != p1.getLast(); p1Word = p1.getNext(), i++) {
			
			//System.out.println(p1Word.getKey());
			// break if past our limit
			if (limit > 0 && i > limit)
				break;
        
			// if this word is empty, ignore and continue
			if (p1Word.getKey().isEmpty())
                continue;
        
			// find all similar words in predicate 2 for the current word
			p2Word = findSimilar(p2, p1Word);
        
			// increase the count for number of similar words found
			found++;
        
			// store the probabilities that each of these words have for being chosen
			double prob1 = p1.getProbability(p1Word.getKey());
			double prob2 = smooth;
			if(p2Word != null) prob2 = p2.getProbability(p2Word.getKey());
        
			// do not allow divide by zero, so smooth by dividing by small value
			if (prob2 == 0.0)
                prob2 = smooth;
        
			// perform the arithmetic operation required to make the KLDivergence method work.
			double term = (prob1-prob2) * Math.log(prob1 / prob2);
			double normTerm = (prob1-smooth) * Math.log(prob1 / smooth);

			divergence += term;
			norm += normTerm;
		}
    
		// keep track of the percent that have been found
		double currentPercentFound = ((double) found / i);
		percentFound = Math.max(percentFound, currentPercentFound);
    
		divergence = divergence/(norm);
    
		//if(!p2.name.equals(".NULL")) System.out.println(p1.name + " ~ " + p2.name + " KLD:" + divergence);
    
		return divergence;
    }
    
    /**
     * Finds the number of 'matches' for the words in two predicates.
     * The match actually uses a thresholded value returned by the
     * string similarity metric.
     *
     * @param predicate        
     * @param pair1
     * @return                        
     */
    private Map.Entry<String, Integer> findSimilar(Predicate predicate, Map.Entry<String, Integer> word) {
            
        // setup
        Map.Entry<String, Integer> pair2, bestMatch = null;
        String pair1value = word.getKey();
        double bestMatchValue = 0.0;
        
        // loop through all words in the target predicate
        int i;
        for (i = 0, pair2 = predicate.getStart(); pair2 != predicate.getLast(); pair2 = predicate.getNext(), i++) {
        	
        	// break if past our limit
//			if (limit > 0 && i > limit)
//				break;
        	
            // get the similarity value from the string comparison class
            double value = compare.compare(pair1value, pair2.getKey());
            // store the most similar word so far in a variable
            if (value > bestMatchValue) {
                    bestMatchValue = value;
                    bestMatch = pair2;
            }
        }
        
        // return the best
        if (bestMatchValue > currentThreshold)
                return bestMatch;
        else
                return null;
        
    }
    
    /**
     * @param echo        Whether or not to print additional information to the screen.
     */
    public void setEcho(boolean echo) {
            this.echo = echo;
    }
    
    /**
     * @param limit                The maximum number of words to use from a predicate.
     */
    public void setLimit(int limit) {
            this.limit = limit;
    }
    
    /**
     * @param thresholdStart        The starting string similarity threshold to use for each predicate.
     */
    public void setStartingThreshold(double thresholdStart) {
            this.thresholdStart = thresholdStart;
    }
    
    public void setThresholdStep(double thresholdStep) {
    	this.thresholdStep = thresholdStep;
    }
    
    public void setValidRequired(int validRequired) {
    	this.validRequired = validRequired;
    }
    
    public List<Results> getResults() {
    	return results;
    }
        
}
