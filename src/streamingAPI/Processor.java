package streamingAPI;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class Processor {
	private MySQLClient mysqlClient;
	
	//Constructor
	public Processor(){
		//Connects to the database
	    mysqlClient = new MySQLClient("jdbc:mysql://127.0.0.1/", "twitter");
	    mysqlClient.connect("root", "");
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
			//If not relevant, over
			//if(tweet.relevant==false) return;
			//Inserts into database
			mysqlClient.insertTweet(tweet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Close
	public void close(){
		mysqlClient.close();
	}
}
