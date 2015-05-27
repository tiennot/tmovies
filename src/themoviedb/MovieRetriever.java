package themoviedb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		//Stores the list of movies and their popularity
		ArrayList<IdAndPop> movieList = new ArrayList<IdAndPop>();
		
		//URL to crawl
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
					//Ignores movies without popularity
					if(!movieObject.has("popularity")) continue;
					//Adds movie to the list
					double popularity = Double.parseDouble(movieObject.getString("popularity"));
					long movieId = movieObject.getLong("id");
					movieList.add(new IdAndPop(movieId, popularity));
				}
				//Determines if we need to go on
				int total_pages = json.getInt("total_pages");
				if(page>=total_pages) break;
				//Increments page number
				page++;
			}
		}
		//Sorts the list of freshly retrieved movies
		Collections.sort(movieList);
		
		//Gets set of currently referenced movies
		HashSet<Long> dbMoviesIds = sqlClient.getMovieIdsFromDb();
		
		//Loops through the list
		HashSet<Long> chosenMovies = new HashSet<Long>();
		while(chosenMovies.size()<50 && !movieList.isEmpty()){
			try{
				IdAndPop movie = movieList.get(0);
				if(chosenMovies.contains(movie.getId())){
					//Ignores duplicates
				}else if(dbMoviesIds.contains(movie.getId())){
					//Updates the popularity
					sqlClient.updatePopularity(movie.getId(), movie.getPop());
					//Adds to chosen set
					chosenMovies.add(movie.getId());
				}else{
					//Gets the movie detailed JSON
					JSONObject detailedJSON = JSONDownloader.getJSON(
							"http://api.themoviedb.org/3/movie/"
							+ movie.getId()
							+ "?api_key="
							+ Configuration.getApiKey());
					//Inserts movie in the database
					sqlClient.insertMovie(new Movie(detailedJSON));
					//Adds to the set
					chosenMovies.add(movie.getId());
				}
			}catch(Exception e){
				System.out.println("Error while inserting movie candidate");
			}
			movieList.remove(0);
		}
		//Eliminate obsolete movies
		sqlClient.removeMovieNotInSet(chosenMovies);
	}
	
	//Little class to pair a movie id and its popularity
	public static class IdAndPop implements Comparable<IdAndPop>{
		private final long id;
		private final double pop;
		public IdAndPop(long id, double pop){
			this.id = id;
			this.pop = pop;
		}
		public final double getPop(){return pop;}
		public final long getId(){return id;}
		//Comparator to allow sorting
		public int compareTo(IdAndPop that) {
			if(this.pop>that.pop) return -1;
			else if(this.pop<that.getPop()) return 1;
			else return Long.compare(this.id, that.getId());
		}
	}
}
