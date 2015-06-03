package analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.twitter.hbc.twitter4j.parser.JSONObjectParser;

/*
 * Finds approximate duplicates for tweets
 */
public  class DuplicateFinder {
	
	/*
	 * Computes Levenshtein distance
	 * (taken from http://en.wikipedia.org/wiki/Levenshtein_distance)
	 */
	public static int LevenshteinDistance(String s, String t){
		// Degenerate cases
	    if (s.compareTo(t)==0) return 0;
	    if (s.length()==0) return t.length();
	    if (t.length()==0) return s.length();
	    // Create two work vectors of integer distances
	    int[] v0 = new int[t.length() + 1];
	    int[] v1 = new int[t.length() + 1];
	    // initialize v0 (the previous row of distances)
	    // this row is A[0][i]: edit distance for an empty s
	    // the distance is just the number of characters to delete from t
	    for (int i=0; i < v0.length; i++)
	    	v0[i] = i;
	    for (int i=0; i < s.length(); i++)
	    {
	        // Calculate v1 (current row distances) from the previous row v0
	        // First element of v1 is A[i+1][0]
	        //   edit distance is delete (i+1) chars from s to match empty t
	        v1[0] = i+1;
	        // use formula to fill in the rest of the row
	        for (int j=0; j < t.length(); j++)
	        {
	            int cost = s.charAt(i)==t.charAt(j) ? 0 : 1;
	            v1[j+1] = Math.min(Math.min(v1[j]+1, v0[j+1]+1), v0[j]+cost);
	        }
	 
	        // Copy v1 (current row) to v0 (previous row) for next iteration
	        for (int j=0; j<v0.length; j++)
	            v0[j] = v1[j];
	    }
	    return v1[t.length()];
	}
	
	/*
	 * Strips the tweet from URL, remove spaces and special characters
	 */
	public static String sanitizeTweet(String text){
		return text.toLowerCase()
				.replaceAll("http[s]{0,1}://[^\\s]+", "")
				.replaceAll("[^0-9a-z]+", "");
	}
	
	public static int hash(String text){
		return sanitizeTweet(text).hashCode();
	}
	
	public static int fakeHash(String text){
		text = sanitizeTweet(text);
		if(text.length()<15) return text.hashCode();
		int hash = Integer.MAX_VALUE;
		for(int i=0; i!=text.length()-15; ++i){
			int subHash = text.substring(i, i+15).hashCode();
			hash = Math.min(hash,  subHash);
			//System.out.println(text.substring(i, i+15)+subHash);
		}
		return hash;
	}
}
