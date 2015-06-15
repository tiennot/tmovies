package analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
	
	public static int[] fakeHash(String text){
		//We want an array with 20 hash values
		int[] hash = new int[20];
		text = sanitizeTweet(text);
		for(int k=0; k!=20; ++k){
			//Defines a basic salt
			String salt = String.valueOf(k);
			//If text not long enough, just takes the hash
			if(text.length()<10){
				try {
					hash[k] = AeSimpleSHA1.SHA1(text+salt).hashCode();
				} catch (Exception e) {
					e.printStackTrace();
					hash[k] = (text+salt).hashCode();
				}
			}
			//Else, takes the minimum value of sub hashes
			int subHash = Integer.MAX_VALUE;
			for(int i=0; i!=text.length()-10; ++i){
				try {
					hash[k] = Math.min(
						AeSimpleSHA1.SHA1(text.substring(i, i+10)+salt).hashCode(),
						subHash
					);
				} catch (Exception e) {
					e.printStackTrace();
					hash[k] = Math.min(
						(text.substring(i, i+10)+salt).hashCode(),
						subHash
					);
				}
			}
		}
		return hash;
	}
	
	
	//
	public static void main(String[] argv){
		int[] fh;
		try {
			fh = fakeHash("I'm the devil and I love metal baby!!!! Do you reckon knowing a Jeff K.?");
			for(int k=0; k!=20; k++)
				System.out.println(fh[k]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
