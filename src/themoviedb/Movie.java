package themoviedb;

import twitter4j.JSONException;
import twitter4j.JSONObject;

/*
 * This class represents a movie with all its data
 */
public class Movie {
	//All attributes
	public long id;
	public String title, release_date, overview, poster_path;
	public float popularity;
	
	//Constructor from the json object
	public Movie(JSONObject obj) throws JSONException{
		this.id = obj.getLong("id");
		this.title = obj.getString("title");
		this.release_date = obj.getString("release_date");
		this.overview = obj.getString("overview");
		this.poster_path = obj.getString("poster_path");
		this.popularity = Float.parseFloat(obj.getString("popularity"));
	}
}
