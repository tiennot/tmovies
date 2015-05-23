package streamingAPI;
import themoviedb.TrackManager;
import tools.MySQLClient;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class Processor {
	private MySQLClient sqlClient;
	private TrackManager trackManager;
	
	//Constructor
	public Processor(MySQLClient sqlClient, TrackManager trackManager){
		this.sqlClient = sqlClient;
		this.trackManager = trackManager;
	}
	
	//Main method for processing
	public void processTweet(String msg){
		try {
			//Creates JSON Object
			JSONObject obj;
			obj = new JSONObject(msg);
			//Ignores retweets
			if(obj.has("retweeted_status")){
				return;
			}
			Tweet tweet = new Tweet(obj);
			tweet.movieId = trackManager.findMovieIdForText(tweet.text);
			//If not relevant, over
			//if(tweet.relevant==false) return;
			//Inserts into database
			if(tweet.movieId!=-1) sqlClient.insertTweet(tweet);
			else System.out.println("No movie found for tweet");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Close
	public void close(){
		sqlClient.close();
	}
}
