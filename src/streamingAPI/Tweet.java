package streamingAPI;
import humorDetector.KNeighbors;

import java.io.IOException;
import java.util.ArrayList;

import analysis.DuplicateFinder;
import analysis.TrustIndicator;
import twitter4j.JSONException;
import twitter4j.JSONObject;

/*
 * This class represents a Tweet as stored in the database
 */
public class Tweet {
	//Attributes of the tweet
	public String text, user, screen_name, avatar;
	//The detected location
	public int city = 0;
	//Time
	public long timestamp;
	//The movie id identified for the tweet
	public long movieId;
	//Array of 20 Hash of the sanitized text
	public int[] hash;
	//Tells if relevant tweet or not
	public boolean topTweet;
	//Score from 0 to 10, trust from 0 to 10 + static kneighbor
	public double score = -1, trust = 0;
	private static KNeighbors kNeighbors = new KNeighbors();
	//Info about user
	public int followers_count, friends_count, statuses_count;
	
	//Constructor
	public Tweet(JSONObject obj) throws JSONException{
		//Parse values
		timestamp = obj.getLong("timestamp_ms");
		text = obj.getString("text");
		JSONObject userObj = obj.getJSONObject("user");
		user = userObj.getString("name");
		screen_name = userObj.has("screen_name") ? userObj.getString("screen_name") : "";
		followers_count = userObj.has("followers_count") ? userObj.getInt("followers_count") : 0;
		friends_count = userObj.has("friends_count") ? userObj.getInt("friends_count") : 0;
		statuses_count = userObj.has("statuses_count") ? userObj.getInt("statuses_count") : 0;
		avatar = userObj.getString("profile_image_url");
		hash = DuplicateFinder.fakeHash(this.text);
		topTweet = TrustIndicator.topTweet(this);
		/*try {
			score = kNeighbors.kNeighbors(text);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	//Analysis method to tell if the tweet should go to top tweets
	public boolean topTweet(){
		return TrustIndicator.topTweet(this);
	}

}
