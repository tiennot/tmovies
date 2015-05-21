package themoviedb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import streamingAPI.MySQLClient;
import tools.JSONDownloader;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

/*
 * This class handles the retrieval of the last movies available on themoviedb
 */
public class MovieRetriever {
	//Its sql client
	MySQLClient sqlClient;
	
	//Constructor
	MovieRetriever(MySQLClient sqlClient){
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
	public ArrayList<Movie> getNewMovies() throws Exception{
		//Gets currently referenced movies
		HashSet<Long> currentMoviesIds = sqlClient.getIdsInDatabase();
		//The object to return
		ArrayList<Movie> movies = new ArrayList<Movie>();
		//Starts with page one
		int page = 1;
		while(page<10){
			//Computes the URL
			String url = "https://api.themoviedb.org/3/discover/movie?api_key="
					+ Configuration.getApiKey()
					+ "&release_date.gte="
					+ getStartDate()
					+ "&release_date.lte="
					+ getEndDate()
					+ "&page="
					+ page;
			System.out.println(url);
			//Gets the JSON from the API
			JSONObject json = JSONDownloader.getJSON(url);
			//Adds the movies to the list
			JSONArray results = json.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject movieObject = results.getJSONObject(i);
				//Ignores already referenced movies
				long movieId = movieObject.getLong("id");
				if(currentMoviesIds.contains(movieId)){
					System.out.println("Already in db");
					continue;
				}
				//Gets the movie detailed JSON
				JSONObject detailedJSON = JSONDownloader.getJSON(
						"http://api.themoviedb.org/3/movie/"
						+ movieId
						+ "?api_key="
						+ Configuration.getApiKey());
				//Adds the movie to return list
				movies.add(new Movie(detailedJSON));
				System.out.println("movie fetched");
			}
			//Determines if we need to go on
			int total_pages = json.getInt("total_pages");
			if(page>=total_pages) break;
			//Increments page number
			page++;
			
		}
		return movies;
	}
	
	//Updates the database movies
	public void updateDatabase() throws Exception{
		//Removes too old movies
		sqlClient.removeOlderMovies(getStartDate());
		//Adds new movies
		ArrayList<Movie> newMovies = getNewMovies();
		for(Movie movie: newMovies)
			sqlClient.insertMovie(movie);
	}
	
	public static void main(String[] args) throws Exception {
		MySQLClient sql = new MySQLClient("jdbc:mysql://127.0.0.1/", "movies");
		sql.connect("root", "");
		ArrayList<Movie> movies = new MovieRetriever(sql).getNewMovies();
		for(Movie movie: movies){
			System.out.println(movie.title + " - " + movie.release_date);
		}
	}
}
