package kldivergence;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import driver.Predicate;

public class Results {
	
	Predicate localPredicate = null;
	boolean useShortName = false;
	boolean displayHeader = true;
	double minValue = 1.0;
	double maxValue = 0.0;
	
	HashMap<Predicate,Double> remotePredicates = new HashMap<Predicate,Double>();
    ValueComparator bvc =  new ValueComparator(remotePredicates);
    TreeMap<Predicate,Double> sortedRemotePredicates = new TreeMap<Predicate,Double>(bvc);
	
	public Results(Predicate localPredicate) {
		this.localPredicate = localPredicate;
	}
	
	public void add(Predicate remotePredicate, Double value) {
		remotePredicates.put(remotePredicate, value);
		minValue = Math.min(minValue, value);
		maxValue = Math.max(maxValue, value);
	}
	
	public void clear() {
		remotePredicates.clear();
	}
	
	public String getPredicateName(Predicate predicate) {
		if (useShortName)
			return predicate.getShortName();
		else
			return predicate.toString();
	}
	
	public void normalize() {
		for(Map.Entry<Predicate,Double> entry : remotePredicates.entrySet()) {
			entry.setValue((entry.getValue() - minValue) / (maxValue - minValue));
		}
	}
	
	private String getPlainRepresentation() {
		return sortedRemotePredicates.toString();
	}
	
	private String getTabularRepresentation() {
		String results = "";
		if (displayHeader)
			results += getPredicateName(localPredicate) + "\n";
		for(Map.Entry<Predicate,Double> entry : sortedRemotePredicates.entrySet()) {
		  Predicate predicate = entry.getKey();
		  Double value = entry.getValue();
		  results += getPredicateName(predicate) + "\t" + value + "\n";
		}
		results += "\n";
		return results;
	}
	
	// TODO: figure out a way to reify predicate
	private String getTripleRepresentation() {
		String results = "";
		String local = getPredicateName(localPredicate);
		for(Map.Entry<Predicate,Double> entry : sortedRemotePredicates.entrySet()) {
		  Predicate predicate = entry.getKey();
		  Double value = entry.getValue();
		  String tripleName = "pmapping:"+local+"_to_"+getPredicateName(predicate);
		  results += local + " owl:equivalentProperty " + getPredicateName(predicate);
		  results += local + " " + getPredicateName(predicate) + "\t" + value + "\n";
		}
		results += "\n";
		return results;
	}
	
	public String toString() {
		sort();
		return getTabularRepresentation();
	}
	
	public String getName() {
		return localPredicate.toString();
	}
	
	private void sort() {
		sortedRemotePredicates.clear();
		sortedRemotePredicates.putAll(remotePredicates);
	}
	
	public int size() {
		return remotePredicates.size();
	}

}
