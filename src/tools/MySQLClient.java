package tools;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

import javax.sql.*;

import streamingAPI.Tweet;
import themoviedb.Movie;
import themoviedb.TrackManager;
import themoviedb.TrackManager.IdAndTitle;

public class MySQLClient{
	private String url = "jdbc:mysql://127.0.0.1/";
	private String dbName = "movies";
	private String driver = "com.mysql.jdbc.Driver";
	private String userName = "root";
	private String password = "";
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
			conn = DriverManager.getConnection(url+dbName,userName,password);
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
	
	
	//Removes movies older than given date
	public boolean removeMovie(long id) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("DELETE FROM movies WHERE id = ?");
		ps.setLong(1, id);
		return ps.execute();
	}
	
	//Inserts a tweet into the database
	public boolean insertTweet(Tweet tweet) throws Exception{
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO tweets (timestamp, user, text, avatar, movieId) VALUES (?, ?, ?, ?, ?)");
		ps.setLong(1, tweet.timestamp);
		ps.setString(2, tweet.user);
		ps.setString(3, tweet.text);
		ps.setString(4, tweet.avatar);
		ps.setFloat(5, tweet.movieId);
		return ps.executeUpdate()==1;
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