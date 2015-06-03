package streamingAPI;
import java.util.ArrayList;

import analysis.DuplicateFinder;
import themoviedb.TrackManager;
import tools.MySQLClient;
import tools.Stats;
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
		Stats.TWEETS_TOTAL++;
		if(Stats.TWEETS_TOTAL%100==0) Stats.printStats();
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
			//Inserts into database
			if(tweet.movieId!=-1){
				//Tries to detect similar tweets
				ArrayList<String> similars = sqlClient.getTextsForHash(tweet.hash);
				boolean found = false;
				for(String s: similars){
					if(DuplicateFinder.LevenshteinDistance(s, tweet.text)<50){
						found = true;
						break;
					}
				}
				//Gets the number of tweets from same author for this movie
				int nbOfTweetAuthor = sqlClient.getNbTweetFromSameAuthor(tweet.screen_name, tweet.movieId);
				//Inserts
				if(found){
					Stats.TWEETS_DUPLICATE++;
				}else if(nbOfTweetAuthor>2){
					Stats.TWEETS_TOOMANYFROMUSER++;
				}else{
					sqlClient.insertTweet(tweet);
					Stats.TWEETS_INSERTED++;
					if(tweet.topTweet) Stats.TWEETS_TOP++;
				}
			}else{
				Stats.TWEETS_NOMOVIE++;
			}
		} catch (Exception e) {
			Stats.TWEETS_JSONERROR++;
		}
	}
	
	//Close
	public void close(){
		sqlClient.close();
	}
}
