import twitter4j.JSONException;
import twitter4j.JSONObject;

public class Processor {
	private static long nbTweetProcessed = 0;
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
			String text = obj.getString("text");
			String user = obj.getJSONObject("user").getString("name");
			//Inserts into database
			mysqlClient.insertTweet(text, user);
			nbTweetProcessed++;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Close
	public void close(){
		mysqlClient.close();
	}
}
