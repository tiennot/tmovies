package streamingAPI;
import java.util.ArrayList;

import twitter4j.JSONException;
import twitter4j.JSONObject;

/*
 * This class represents a Tweet as stored in the database
 */
public class Tweet {
	//Attributes of the tweet
	public String text, user, avatar;
	//The detected location
	public int city = 0;
	//Time
	public long timestamp;
	//The movie id identified for the tweet
	public long movieId;
	
	//Constructor
	public Tweet(JSONObject obj) throws JSONException{
		//Parse values
		timestamp = obj.getLong("timestamp_ms");
		text = obj.getString("text");
		user = obj.getJSONObject("user").getString("name");
		avatar = obj.getJSONObject("user").getString("profile_image_url");
	}	

}
