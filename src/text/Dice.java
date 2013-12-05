package text;

import java.util.Arrays;

// the package containing the Jaro-Winkler implementation
//import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

public class Dice extends StringCompare {

        // the class for the Dice coeffecient algorithm
        //JaroWinkler jw = new JaroWinkler();
        
        @Override
        public double compare(String str1, String str2) {
            // Verifying the input:
            if (str1 == null || str2 == null)
                    return 0;
            // Quick check to catch identical objects:
            if (str1 == str2)
                    return 1;
            // avoid exception for single character searches
            if (str1.length() < 2 || str2.length() < 2)
                return 0;
     
            // Create the bigrams for string s:
            final int n = str1.length()-1;
            final int[] sPairs = new int[n];
            for (int i = 0; i <= n; i++)
                    if (i == 0)
                            sPairs[i] = str1.charAt(i) << 16;
                    else if (i == n)
                            sPairs[i-1] |= str1.charAt(i);
                    else
                            sPairs[i] = (sPairs[i-1] |= str1.charAt(i)) << 16;
     
            // Create the bigrams for string t:
            final int m = str2.length()-1;
            final int[] tPairs = new int[m];
            for (int i = 0; i <= m; i++)
                    if (i == 0)
                            tPairs[i] = str2.charAt(i) << 16;
                    else if (i == m)
                            tPairs[i-1] |= str2.charAt(i);
                    else
                            tPairs[i] = (tPairs[i-1] |= str2.charAt(i)) << 16;
     
            // Sort the bigram lists:
            Arrays.sort(sPairs);
            Arrays.sort(tPairs);
     
            // Count the matches:
            int matches = 0, i = 0, j = 0;
            while (i < n && j < m)
            {
                    if (sPairs[i] == tPairs[j])
                    {
                            matches += 2;
                            i++;
                            j++;
                    }
                    else if (sPairs[i] < tPairs[j])
                            i++;
                    else
                            j++;
            }
            return (double)matches/(n+m);
        }
        
}
