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
	//Detected likelihood
	public float likelihood_sun, likelihood_rain, likelihood_cloud;
	//Is the tweet relevant?
	public boolean relevant = false;
	
	//Constructor
	public Tweet(JSONObject obj) throws JSONException{
		//Parse values
		timestamp = obj.getLong("timestamp_ms");
		text = obj.getString("text");
		user = obj.getJSONObject("user").getString("name");
		avatar = obj.getJSONObject("user").getString("profile_image_url");
		//Generates the three likelihoods
		computeLikelihood();
	}
	
	//Computes the three likelihood given the text of the tweet
	private void computeLikelihood(){
		//Initialize count for three words
		int[] count = new int[3];
		
		//The list of words to match
		String[][] words = new String[3][];
		words[0] = new String[]{"sunny", "sun", "soleil", "sunlight"};
		words[1] = new String[]{"rain", "raindrop", "storm", "pluie", "shower", "flooding"};
		words[2] = new String[]{"cloudy", "overcast", "nuageux", "nuages"};
		
		//Counts
		String lwrText = text.toLowerCase();
		for(int i=0; i<3; ++i){
			for(String word: words[i]){
				if(lwrText.indexOf(word)!=-1){
					if(lwrText.indexOf("#"+word)!=-1) count[i] += 10;
					else count[i] += 1;
				}
			}
		}
		
		int sum = 0;
		for(int i=0; i<3; i++){
			sum += count[i];
		}
		if(sum!=0) relevant = true;
		sum = Math.max(sum, 7);
		
		this.likelihood_sun = ((float) count[0])/((float)sum);
		this.likelihood_rain = ((float) count[1])/((float)sum);
		this.likelihood_cloud = ((float) count[2])/((float)sum);
	}
	
	

}
