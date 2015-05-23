import streamingAPI.Processor;
import streamingAPI.Retriever;
import themoviedb.Configuration;
import themoviedb.MovieRetriever;
import themoviedb.TrackManager;
import tools.MySQLClient;

/*
 * Main class, supposed to be the one starting the script
 */
public class Main {
	public static void main(String[] args) throws Exception {
		//Instantiate SQL Client
		MySQLClient sql = new MySQLClient("jdbc:mysql://127.0.0.1/", "movies");
		sql.connect("root", "");
		//Instantiate configuration
		Configuration config = new Configuration(sql);
		config.updateConfig();
		//Update movie database
		MovieRetriever movieRetriever = new MovieRetriever(sql);
		movieRetriever.updateDatabase();
		//The track generator to generate movie keywords
		TrackManager trackManager = new TrackManager(sql);
		//Instantiate retriever and load movie keywords
		Processor p = new Processor(sql, trackManager);
		Retriever retriever = new Retriever(args[0], args[1], args[2], args[3], p);
		retriever.setTrackString(trackManager.generateTrackString());
		//Launch the retriever
		(new Thread(retriever)).start();
	}
}
