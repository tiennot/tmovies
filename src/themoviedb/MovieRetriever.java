package themoviedb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import tools.JSONDownloader;
import tools.MySQLClient;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

/*
 * This class handles the retrieval of the last movies available on themoviedb
 */
public class MovieRetriever {
	//Its sql client
	MySQLClient sqlClient;
	
	//Constructor
	public MovieRetriever(MySQLClient sqlClient){
		this.sqlClient = sqlClient;
	}
	
	//Gets the date where we begin
	private String getStartDate(){
		long time = System.currentTimeMillis() - Long.parseLong("2592000000");
		Date date = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}
	
	//Gets the date where we end
	private String getEndDate(){
		long time = System.currentTimeMillis() + Long.parseLong("2592000000");
		Date date = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}
	
	//Main method, return list of last movies
	public void updateDatabase() throws Exception{
		//Gets currently referenced movies
		HashSet<Long> currentMoviesIds = sqlClient.getMovieIdsFromDb();
		HashSet<Long> movieIds = new HashSet<Long>();
		//Urls
		String[] urls = new String[2];
		urls[0] = "https://api.themoviedb.org/3/movie/now_playing?api_key=" + Configuration.getApiKey();
		urls[1] = "https://api.themoviedb.org/3/movie/upcoming?api_key=" + Configuration.getApiKey();
		for(String baseUrl: urls){
			//Starts with page one
			int page = 1;
			while(page<1000){		
				String url = baseUrl + "&page=" + page;
				System.out.println(url);
				//Gets the JSON from the API
				JSONObject json = JSONDownloader.getJSON(url);
				//Adds the movies to the list
				JSONArray results = json.getJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject movieObject = results.getJSONObject(i);
					//Ignores movies not popular enough
					if(!movieObject.has("popularity") ||
							Double.parseDouble(movieObject.getString("popularity"))<Configuration.min_popularity)
						continue;
					//Only updates popularity for already referenced movies
					long movieId = movieObject.getLong("id");
					if(currentMoviesIds.contains(movieId)){
						//TODO update pop
						movieIds.add(movieId);
						continue;
					}
					//Ignores duplicate movies
					if(movieIds.contains(movieId))
						continue;
					//Gets the movie detailed JSON
					JSONObject detailedJSON = JSONDownloader.getJSON(
							"http://api.themoviedb.org/3/movie/"
							+ movieId
							+ "?api_key="
							+ Configuration.getApiKey());
					//Adds the movie to return list
					sqlClient.insertMovie(new Movie(detailedJSON));
					movieIds.add(movieId);
					System.out.println("movie added");
				}
				//Determines if we need to go on
				int total_pages = json.getInt("total_pages");
				if(page>=total_pages) break;
				//Increments page number
				page++;
			}
		}
		//Eliminates movies we just retrieved
		for(long id: currentMoviesIds){
			if(!movieIds.contains(id)) sqlClient.removeMovie(id);
		}
	}
}
