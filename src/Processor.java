import twitter4j.JSONException;
import twitter4j.JSONObject;

public class Processor {
	private SQLClient mysqlClient;
	
	//Constructor
	public Processor(){
		//Connects to the database
	    mysqlClient = new SQLClient("jdbc:mysql://127.0.0.1/", "twitter");
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
			//Inserts into database
			mysqlClient.insertTweet(new Tweet(obj));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Close
	public void close(){
		mysqlClient.close();
	}
}
