import java.sql.*;

import javax.sql.*;

public class SQLClient{
	private String url = "jdbc:mysql://127.0.0.1/";
	private String dbName = "twitter";
	private String driver = "com.mysql.jdbc.Driver";
	private String userName = "root";
	private String password = "";
	//Connection object
	Connection conn;
	
	//Constructor
	public SQLClient(String url, String dbName){
		this.url = url;
		this.dbName = dbName;
	}
	
	public void connect(String username, String password){
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbName,userName,password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean insertTweet(Tweet tweet) throws Exception{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO tweets (timestamp, user, text, avatar) VALUES (?, ?, ?, ?)");
		ps.setLong(1, tweet.timestamp);
		ps.setString(2, tweet.user);
		ps.setString(3, tweet.text);
		ps.setString(4, tweet.avatar);
		int val = ps.executeUpdate();
		return val==1;
	}
	
	public void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}