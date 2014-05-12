package driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kldivergence.KLDivergence;
import kldivergence.Results;
import text.*;

public class Driver {
	
	// the list of predicates to try and match
	private static List<Predicate> localPredicates = new ArrayList<Predicate>();
	// the list of predicates that have been matched by algorithm
	private static List<Predicate> remotePredicates = new ArrayList<Predicate>();
	static List<Results> results;
	
	public static void main(String [] args) {
	
		// select the string comparison metric
//		StringCompare compare = new JW();
		StringCompare compare = new Dice();
		// create the KL Divergence class
		KLDivergence kld = new KLDivergence(compare);
		// set the max number of predicates to look at
		kld.setLimit(500);
		// set the starting threshold for the string comparison (will be decreased dynamically if too strict)
//		kld.setStartingThreshold(0.99);
		kld.setStartingThreshold(0.81);
		
		kld.setThresholdStep(0.02);
		kld.setValidRequired(0);
		
		// test 4: limit 500, DICE compare, starting .85, step .02, required 5
		// test A: limit 1000, JW compare, starting .97, step 0.01, required 5
		// test B: limit 1000, JW compare, starting .97, step 0.01, required 7
		// test C: limit 2000, JW compare, starting .99, step 0.005, required 2
		// test D: limit 2000, JW compare, starting .99, step 0.005, required 3
		
		// choose which predicates will be matched
//		testMoviesSubset();
//		testSpeciesSubset();
		testMoviesSubsetLocal();
		
		stripConstants(1.0);
		
//		kld.setEcho(true);	// prints additional information
		
		// execute the algorithm, matching predicates as well as possible
		kld.select(localPredicates, remotePredicates);
		results = kld.getResults();
		System.out.println();
		
		normalizeResults(results);
		
		printResults(results);
//		saveResults(results, "results/");
		
	}
	
	private static void stripConstants(double constantValue) {
		for (Predicate predicate : localPredicates) {
			predicate.setConstantValue(constantValue);
			predicate.stripConstants();
		}
		for (Predicate predicate : remotePredicates) {
			predicate.setConstantValue(constantValue);
			predicate.stripConstants();
		}
	}
	
	private static void normalizeResults(List<Results> results) {
		for (Results result : results) {
			result.normalize();
		}
	}
	
	private static void printResults(List<Results> results) {
		for (Results result : results) {
			System.out.println(result);
		}
	}
	
	private static void saveResults(List<Results> results, String directory) {
		FileWriter fw;
		BufferedWriter bw;
		for (Results result : results) {
			String fileName = directory + result.getName();
			System.out.println("Writing "+fileName);
			File file = new File(fileName);
			try {
				fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				bw.write(result.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void printExactMatches(Map<Predicate, Predicate> matches) {
		// loop through list of predicates that were searched for in DB2
		for (Predicate predicate : localPredicates) {
			// retrieves the match returned by the algorithm
			Predicate match = matches.get(predicate);
			// print confidence and matched predicates
			System.out.print(match.getConfidence()+": ");
			System.out.println(predicate.getShortName() + " ~ "+ match.getShortName());
		}
	}
	
	// read a subset of dbpedia predicates and compare to all dbpedia predicates
	public static void testMoviesSubsetLocal() {
		String dbpedia_directory = "data/movies/dbpedia/";
		
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.starring"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.director"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.producer"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.writer"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.editor"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.genre"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.precededBy"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.followedBy"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.country"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.artist"));
//			localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.cinematography"));
		
		remotePredicates = getPredicatesFromDirectory(dbpedia_directory);
	}
	
	// read a subset of dbpedia predicates and all taxonomy predicates
	public static void testSpeciesSubset() {
		String local_directory = "data/species/dbpedia/";
		String target_directory = "data/species/taxonomy/";
		
		localPredicates.add(new Predicate(local_directory+"http.dbpedia.org.ontology.class"));
		localPredicates.add(new Predicate(local_directory+"http.dbpedia.org.ontology.family"));
		localPredicates.add(new Predicate(local_directory+"http.dbpedia.org.ontology.genus"));
		localPredicates.add(new Predicate(local_directory+"http.dbpedia.org.ontology.kingdom"));
		localPredicates.add(new Predicate(local_directory+"http.dbpedia.org.ontology.order"));
		localPredicates.add(new Predicate(local_directory+"http.dbpedia.org.ontology.phylum"));
		
		remotePredicates = getPredicatesFromDirectory(target_directory);
	}
	
	// read a subset of dbpedia predicates and all linkedmdb predicates
	public static void testMoviesSubset() {
		String dbpedia_directory = "data/movies/dbpedia/";
		String lmdb_directory = "data/movies/linkedmdb/";
		
		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.starring"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.director"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.producer"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.writer"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.editor"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.genre"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.precededBy"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.followedBy"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.country"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.artist"));
//		localPredicates.add(new Predicate(dbpedia_directory+"http.dbpedia.org.property.cinematography"));
		
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
		if (folder.isDirectory()) {
		    for (File fileEntry : folder.listFiles()) {
		        if (!fileEntry.isDirectory()) {
		            list.add(new Predicate(directory+fileEntry.getName()));
		        }
		    }
		} else {
			throw new IllegalArgumentException(directory+" is not a directory.");
		}
	    return list;
	}
	
}
