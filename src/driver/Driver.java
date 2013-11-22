package driver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import text.StringCompare;

import text.JW;
import kldivergence.KLDivergence;

public class Driver {
	
	// the list of predicates to try and match
	private static List<Predicate> localPredicates = new ArrayList<Predicate>();
	// the list of predicates that have been matched by algorithm
	private static List<Predicate> remotePredicates = new ArrayList<Predicate>();
	
	public static void main(String [] args) {
	
		// select the string comparison metric
		StringCompare compare = new JW();
		// create the KL Divergence class
		KLDivergence kld = new KLDivergence(compare);
		// set the max number of predicates to look at
		kld.setLimit(500);
		// set the starting threshold for the string comparison (will be decreased dynamically if too strict)
		kld.setStartingThreshold(0.91);
		
		// choose which predicates will be matched
		testSemanticSubset();
		
		//kld.setEcho(true);	// prints additional information
		
		// execute the algorithm, matching predicates as well as possible
		Map<Predicate, Predicate> matches = kld.select(localPredicates, remotePredicates);
		System.out.println();
		
		// loop through list of predicates that were searched for in DB2
		for (Predicate predicate : localPredicates) {
			// retrieves the match returned by the algorithm
			Predicate match = matches.get(predicate);
			// print confidence and matched predicates
			System.out.print(match.getConfidence()+": ");
			System.out.println(predicate.getShortName() + " ~ "+ match.getShortName());
		}
		
		
	}
	
	// read a subset of dbpedia predicates and all linkedmdb predicates
	public static void testSemanticSubset() {
		String dbpedia_directory = "data/semantic/dbpedia/";
		String lmdb_directory = "data/semantic/lmdb/";
		
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.starring"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.director"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.producer"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.writer"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.editor"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.cinematography"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.genre"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.precededBy"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.followedBy"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.language"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.country"));
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.rating"));
		
		remotePredicates = getPredicatesFromDirectory(lmdb_directory);
	}
	
	// read all possible predicates in dbpedia and linkedmdb
	public static void testSemanticAll() {
		String dbpedia_directory = "data/semantic/dbpedia/";
		String lmdb_directory = "data/semantic/lmdb/";
		
		localPredicates = getPredicatesFromDirectory(dbpedia_directory);
		
		remotePredicates = getPredicatesFromDirectory(lmdb_directory);
	}
	
	// read a small subset of predicates for testing
	public static void testSmall() {
		localPredicates.add(new Predicate("data/small/p1.txt"));
		remotePredicates.add(new Predicate("data/small/p2.txt"));
		remotePredicates.add(new Predicate("data/small/p3.txt"));
	}
	
	/**
	 * Creates predicates for every file in a directory.
	 * 
	 * @param directory	The directory to read from.
	 * @return			The list of predicates generated.
	 */
	private static List<Predicate> getPredicatesFromDirectory(String directory) {
		List<Predicate> list = new ArrayList<Predicate>();
		File folder = new File(directory);
	    for (File fileEntry : folder.listFiles()) {
	        if (!fileEntry.isDirectory()) {
	            list.add(new Predicate(directory+fileEntry.getName()));
	        }
	    }
	    return list;
	}
	
}
