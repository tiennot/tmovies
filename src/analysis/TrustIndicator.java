package analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import streamingAPI.Tweet;

/*
 * Computes relevance for a tweet
 */
public class TrustIndicator {
	//List of "trust" words, increase confidence
	private static String[] trustWords = new String[]{
		"movie",
		"watch",
		"awsome",
		"watching",
		"watch",
		"best",
		"boring",
		"cinema"
	};
	//List of "red" words, decrease confidence
	private static String[] redWords = new String[]{
		//No red word yet
	};
	
	//Count number of urls in tweet
	private static int nbOfUrl(String lwrText){
        Pattern pattern = Pattern.compile("http[s]{0,1}://[^\\s]+");
        Matcher  matcher = pattern.matcher(lwrText);
        int count = 0;
        while (matcher.find())
            count++;
        return count;
	}
	
	//Count number of hashtags in tweet
	private static int nbOfHashtag(String lwrText){
        Pattern pattern = Pattern.compile("#[a-z0-9]+");
        Matcher  matcher = pattern.matcher(lwrText);
        int count = 0;
        while (matcher.find())
            count++;
        return count;
	}
	
	//Count number of usernames in tweet
	private static int nbOfUsername(String lwrText){
		Pattern pattern = Pattern.compile("@[a-z0-9_]+");
        Matcher  matcher = pattern.matcher(lwrText);
        int count = 0;
        while (matcher.find())
            count++;
        return count;
	}
	
	//Returns text stripped from hashtags, usernames, url, etc.
	private static String stripped(String lwrText){
		return lwrText.replaceAll("http[s]{0,1}://[^\\s]+", "")
				//Removes hashtags
				.replaceAll("#[a-z0-9]+", "")
				//Removes usernames
				.replaceAll("@[a-z0-9_]+", "")
				//Removes symbols other than letters, numbers and spaces
				.replaceAll("[^a-z0-9\\s]+", "")
				//Removes extra spaces
				.replaceAll("[\\s]{2,}", " ");
	}
	
	//Generates ratio stripped text / total
	private static float strippedRatio(String lwrText){
		return (float) stripped(lwrText).length() / (float) lwrText.length();
	}
	
	//Decides weither a tweet is top or not
	public static boolean topTweet(Tweet t){
		String lwrText = t.text.toLowerCase();
		//No url
		if(nbOfUrl(lwrText)!=0) return false;
		//No more than 1 username
		if(nbOfUsername(lwrText)>1) return false;
		//No more than 3 hashtags
		if(nbOfHashtag(lwrText)>3) return false;
		//Minimum ratio true text / total
		if(strippedRatio(lwrText)<0.5) return false;
		//If all conditions satisfied, OK
		return true;
	}
}
