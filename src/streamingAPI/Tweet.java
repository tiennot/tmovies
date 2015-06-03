package streamingAPI;
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
	//Hash of the sanitized text
	public int hash;
	//Tells if relevant tweet or not
	public boolean topTweet;
	
	//Constructor
	public Tweet(JSONObject obj) throws JSONException{
		//Parse values
		timestamp = obj.getLong("timestamp_ms");
		text = obj.getString("text");
		JSONObject userObj = obj.getJSONObject("user");
		user = userObj.getString("name");
		screen_name = userObj.has("screen_name") ? userObj.getString("screen_name") : "";
		avatar = userObj.getString("profile_image_url");
		hash = DuplicateFinder.hash(this.text);
		topTweet = TrustIndicator.topTweet(this);
	}
	
	//Analysis method to tell if the tweet should go to top tweets
	public boolean topTweet(){
		return TrustIndicator.topTweet(this);
	}

}
