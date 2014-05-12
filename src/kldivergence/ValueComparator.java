package kldivergence;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import driver.Predicate;

class ValueComparator implements Comparator<Predicate> {

    Map<Predicate, Double> base;
    public ValueComparator(Map<Predicate, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(Predicate a, Predicate b) {
        if (base.get(a) <= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}