import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
	
	//Constructor
	public Retriever(String consumerKey, String consumerSecret, String token, String secret){
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.token = token;
		this.secret = secret;
	}
	
	//Run method
	public void run() {
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
	    // add some track terms
	    //endpoint.trackTerms(Lists.newArrayList("#sunny", "#rain", "#cloudy"));
		endpoint.addPostParameter("track", "sunny,rain,cloudy");

		//Filters, only tweets from Paris area
		//endpoint.addPostParameter("locations", "1.87,48.5,2.75,49.1");
	    
	    Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
	    // Authentication auth = new BasicAuth(username, password);

	    // Create a new BasicClient. By default gzip is enabled.
	    Client client = new ClientBuilder()
	            .hosts(Constants.STREAM_HOST)
	            .endpoint(endpoint)
	            .authentication(auth)
	            .processor(new StringDelimitedProcessor(queue))
	            .build();

	    // Establish a connection
	    client.connect();

	    //Creates processor object to handle the tweets
	    Processor p = new Processor();
	    
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
		//Launch the retriever on its own thread
		(new Thread(new Retriever(args[0], args[1], args[2], args[3]))).start();
	}
}
