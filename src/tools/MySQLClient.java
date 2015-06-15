package tools;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sql.*;

import analysis.DuplicateFinder;
import streamingAPI.Tweet;
import themoviedb.Movie;
import themoviedb.TrackManager;
import themoviedb.TrackManager.IdAndTitle;

public class MySQLClient{
	private String url = "jdbc:mysql://127.0.0.1/?characterEncoding=utf-8&useUnicode=true";
	private String dbName = "movies";
	private String driver = "com.mysql.jdbc.Driver";
	//Connection object
	Connection conn;
	
	//Constructor
	public MySQLClient(String url, String dbName){
		this.url = url;
		this.dbName = dbName;
	}
	
	//Connects to the database given credentials
	public void connect(String username, String password){
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(
					url+dbName+"?characterEncoding=utf-8&character_encoding_server=utf8mb4",
					username,
					password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Inserts a movie into the database
	public boolean insertMovie(Movie movie){
		try{
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO movies (id, title, release_date, overview, poster_path, popularity) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setLong(1, movie.id);
			ps.setString(2, movie.title);
			ps.setString(3, movie.release_date);
			ps.setString(4, movie.overview);
			ps.setString(5, movie.poster_path);
			ps.setFloat(6, movie.popularity);
			return ps.executeUpdate()==1;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	//Gets the list of the id currentl in the database
	public HashSet<Long> getMovieIdsFromDb() throws Exception{
	    HashSet<Long> list = new HashSet<Long>();
		ResultSet rs = conn.createStatement().executeQuery("SELECT id FROM movies");
	    while (rs.next())
	    	list.add(rs.getLong("id"));
	    return list;
	}
	
	//Gets the list of movie titles from the database
	public ArrayList<TrackManager.IdAndTitle> getMovieTitlesFromDb() throws Exception{
		ArrayList<TrackManager.IdAndTitle> list = new ArrayList<TrackManager.IdAndTitle>();
		ResultSet rs = conn.createStatement().executeQuery("SELECT id, title FROM movies");
		while(rs.next())
			list.add(new IdAndTitle(rs.getLong("id"), rs.getString("title")));
		return list;
	}
	
	//Removes movies not in the given set
	public boolean removeMovieNotInSet(Set<Long> ids) throws SQLException{
		String inString = "(";
		for(long id: ids){
			inString += id + ",";
		}
		inString += "0)";
		return conn.createStatement().execute("DELETE FROM movies WHERE id NOT IN "+inString);
	}
	
	//Updates the popularity for a given movie
	public boolean updatePopularity(long movieId, double popularity) throws Exception{
		PreparedStatement ps = conn.prepareStatement("UPDATE movies SET popularity=? WHERE id=?");
		ps.setDouble(1, popularity);
		ps.setLong(2, movieId);
		return ps.executeUpdate()==1;
	}
	
	//Inserts a tweet into the database
	public boolean insertTweet(Tweet tweet) throws Exception{
		//The statement to insert the tweet itself
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO tweets (timestamp, user, screen_name, text, avatar, movieId, top_tweet, score, trust, followers_count, friends_count, statuses_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		ps.setLong(1, tweet.timestamp);
		ps.setString(2, tweet.user);
		ps.setString(3, tweet.screen_name);
		ps.setString(4, tweet.text);
		ps.setString(5, tweet.avatar);
		ps.setFloat(6, tweet.movieId);
		ps.setBoolean(7, tweet.topTweet);
		ps.setDouble(8,  tweet.score);
		ps.setDouble(9,  tweet.trust);
		ps.setInt(10,  tweet.followers_count);
		ps.setInt(11,  tweet.friends_count);
		ps.setInt(12,  tweet.statuses_count);
		//Executes the statement
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 0) {
            throw new SQLException("The tweet couldn't be inserted");
        }
		//Retrieves the id just inserted
		ResultSet generatedKeys = ps.getGeneratedKeys();
		if (generatedKeys.next()) {
            long inserted_id = generatedKeys.getLong(1);
            //The statement to insert the hashes
    		String query_hash = "INSERT INTO hash (tweet_id";
    		for(int k=0; k!=20; k++) query_hash += ", hash_"+k;
    		query_hash += ") VALUES (?";
    		for(int k=0; k!=20; k++) query_hash += ", ?";
    		query_hash += ")";
    		//Sets the hashes 
    		PreparedStatement ps_hash = conn.prepareStatement(query_hash);
    		ps_hash.setLong(1, inserted_id);
    		for(int k=0; k!=20; k++) ps_hash.setLong(k+2,  tweet.hash[k]);
    		return ps_hash.executeUpdate()==1;
        }else {
            throw new SQLException("Couldn't insert the hash for the tweet (no id)");
        }
	}
	
	//Gets text of tweets with the same hash
	public ArrayList<String> getTextsForHash(int[] hash) throws SQLException{
		ArrayList<String> list = new ArrayList<String>();
		//Sets all the 20 hash
		String query = "SELECT text FROM tweets, hash WHERE tweets.id=hash.tweet_id AND (";
		for(int k=0; k!=20; k++){
			query += "hash_"+k+"=? OR ";
		}
		query += " 1=2)";
		//Prepares the statement
		PreparedStatement ps = conn.prepareStatement(query);
		for(int k=0; k!=20; k++){
			ps.setInt(k+1, hash[k]);
		}
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			list.add(rs.getString("text"));
		return list;
	}
	
	//Gets the number of tweets from the same author
	public int getNbTweetFromSameAuthor(String screen_name, long movieId) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("SELECT count(screen_name) as count FROM tweets WHERE movieId = ? GROUP BY screen_name");
		ps.setLong(1, movieId);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
			return rs.getInt("count");
		return 0;
	}
	
	//Generates the relative popularity, flow and rating
	public void generateRelFigures() throws SQLException{
		//Lists
		ArrayList<Long> listIds = new ArrayList<Long>();
		ArrayList<Integer> listCounts = new ArrayList<Integer>();
		ArrayList<Float> listAvgScore = new ArrayList<Float>();
		ArrayList<Float> listPopularity = new ArrayList<Float>();
		//Retrieves data
		ResultSet rs = conn.createStatement().executeQuery(
				"SELECT movies.id, count(tweets.text) as count, SUM(tweets.score) as sum_scores, popularity "
				+"FROM movies "
				+"LEFT JOIN tweets ON tweets.movieId=movies.id "
				+"GROUP BY tweets.movieId "
		);
		while(rs.next()){
			listIds.add(rs.getLong("id"));
			listCounts.add(rs.getInt("count"));
			listAvgScore.add( (float) (rs.getInt("count")>0 ? rs.getDouble("sum_scores") / (double) rs.getInt("count") : 0));
			listPopularity.add(rs.getFloat("popularity"));
		}
		//Updates movies with values
		for(int i=0; i!=listIds.size(); ++i){
			//Computes rel figures
			class RelGenerator{
				float computeInt(ArrayList<Integer> c, int i){
					if((Collections.max(c)==Collections.min(c))) return 0;
					return 100*((float)(c.get(i) - Collections.min(c)))
							/((float)(Collections.max(c)-Collections.min(c)));
				}
				float computeFloat(ArrayList<Float> c, int i){
					if((Collections.max(c)==Collections.min(c))) return 0;
					return 100*((c.get(i) - Collections.min(c)))
							/((Collections.max(c)-Collections.min(c)));
				}
			}
			float relPop = new RelGenerator().computeFloat(listPopularity, i);
			float relFlow = new RelGenerator().computeInt(listCounts, i);
			float relRating = new RelGenerator().computeFloat(listAvgScore, i);
			//Statement
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE movies SET rel_pop=?, rel_flow=?, rel_rating=? WHERE id=?");
			ps.setFloat(1, relPop);
			ps.setFloat(2, relFlow);
			ps.setFloat(3, relRating);
			ps.setLong(4, listIds.get(i));
			ps.executeUpdate();
		}
	}
	
	//Close the client
	public void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}