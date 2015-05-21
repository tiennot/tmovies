package streamingAPI;
import java.sql.*;
import java.util.HashSet;

import javax.sql.*;

import themoviedb.Movie;

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
	public boolean insertMovie(Movie movie) throws Exception{
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO movies (id, title, release_date, overview, poster_path) VALUES (?, ?, ?, ?, ?, ?)");
		ps.setString(1, movie.id);
		ps.setString(2, movie.release_date);
		ps.setString(3, movie.overview);
		ps.setString(4, movie.poster_path);
		return ps.executeUpdate()==1;
	}
	
	//Gets the list of the id currentl in the database
	public HashSet<Long> getIdsInDatabase() throws Exception{
	    HashSet<Long> list = new HashSet<Long>();
		ResultSet rs = conn.createStatement().executeQuery("SELECT id FROM movies");
	    while (rs.next())
	    	list.add(rs.getLong("id"));
	    return list;
	}
	
	//Removes movies older than given date
	public void removeOlderMovies(String date) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("DELETE FROM movies WHERE release_date < ?");
		ps.setString(1, date);
	}
	
	//Inserts a tweet into the database
	public boolean insertTweet(Tweet tweet) throws Exception{
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO tweets (timestamp, user, text, avatar, likelihood_sun, likelihood_rain, likelihood_cloud) VALUES (?, ?, ?, ?, ?, ?, ?)");
		ps.setLong(1, tweet.timestamp);
		ps.setString(2, tweet.user);
		ps.setString(3, tweet.text);
		ps.setString(4, tweet.avatar);
		ps.setFloat(5, tweet.likelihood_sun);
		ps.setFloat(6, tweet.likelihood_rain);
		ps.setFloat(7, tweet.likelihood_cloud);
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