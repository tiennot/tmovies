package themoviedb;

import java.sql.Timestamp;

import tools.JSONDownloader;
import twitter4j.JSONException;
import twitter4j.JSONObject;

/*
 * Allows to get the configuration from themoviedb
 */
public class Configuration {
	//Static, API key
	private static String api_key = "76b1ee24ab9d445ff9849c97f44e8724";
	public static String getApiKey(){ return api_key; }
	
	//JSON configuration from themoviedb
	private JSONObject json_config;
	private long config_last_update = 0;
	
	//Loads the JSON from the server of themoviedb
	public void reloadConfig(){
		try {
			json_config = JSONDownloader.getJSON("http://api.themoviedb.org/3/configuration?api_key="+Configuration.getApiKey());
			config_last_update = System.currentTimeMillis();
		} catch (Exception e) {
			System.err.println("Error while trying to retrieve the configuration file. Will try again later.");
		}
	}
	
	//Tells if configuration file has expired
	private boolean isConfigExpired(){
		//Basic expiration time is one day
		return (System.currentTimeMillis()-config_last_update)>3600*24*1000;
	}
	
	//Provides the image base URL
	public String getImageBaseUrl() throws JSONException{
		if(isConfigExpired()) reloadConfig();
		return json_config.getJSONObject("images").getString("base_url");
	}
	
}
