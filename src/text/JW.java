package text;

// the package containing the Jaro-Winkler implementation
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

public class JW extends StringCompare {

	// the class for the Jaro-Winkler algorithm
	static JaroWinkler jw = new JaroWinkler();
	
	@Override
	public double compare(String str1, String str2) {
		return jw.getSimilarity(str1, str2);
	}
	
}
