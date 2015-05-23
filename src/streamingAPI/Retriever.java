package streamingAPI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import tools.MySQLClient;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class Retriever implements Runnable{
	//The blocking queue
	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
	//The credentials //TODO Load from file
	private String consumerKey;
	private String consumerSecret;
	private String token;
	private String secret;
	
	//The track keywords
	private String trackString = "";
	public void setTrackString(String s){trackString=s;}
	
	//Tweet processor
	Processor p;
	
	//Constructor
	public Retriever(String consumerKey, String consumerSecret, String token, String secret, Processor p){
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.token = token;
		this.secret = secret;
		this.p = p;
	}
	
	//Run method
	public void run() {
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
	    // add some track terms
		endpoint.addPostParameter("track", this.trackString);
		
		//Restricts to English tweets
		endpoint.addPostParameter("language", "en");
	    
		//Authenticate with credentials
	    Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);

	    // Create a new BasicClient. By default gzip is enabled.
	    Client client = new ClientBuilder()
	            .hosts(Constants.STREAM_HOST)
	            .endpoint(endpoint)
	            .authentication(auth)
	            .processor(new StringDelimitedProcessor(queue))
	            .build();

	    // Establish a connection
	    client.connect();
	    
	    // Do whatever needs to be done with messages
	    while(true){
			try {
				String msg;
				msg = queue.take();
				//Processes the tweet
				p.processTweet(msg);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				break;
			}
	    }
	    client.stop();
	    p.close();
	}
	
	//Main method
	public static void main(String[] args) {
		
	}
}
