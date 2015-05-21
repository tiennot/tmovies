package tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

import themoviedb.Configuration;
import twitter4j.JSONObject;

public class JSONDownloader {
	//Last call
	private static LinkedList<Long> lastCalls = new LinkedList<Long>();
	
	public static JSONObject getJSON(String url) throws Exception{
        //Waits if necessary
		if(lastCalls.size()==30){
			long time = System.currentTimeMillis();
			if(time-lastCalls.getFirst()<30000){
				Thread.sleep(time - lastCalls.getFirst() +500);
			}
		}
		//Reads the JSON URL
		BufferedReader in = new BufferedReader(
        	new InputStreamReader(
        		new URL(url).openStream()
        	)
        );
        String source = "", line = "";
        while ((line = in.readLine()) != null){
        	source += line;
        }
        in.close();
        //Records last call
        if(lastCalls.size()==30) lastCalls.removeFirst();
        lastCalls.addLast(System.currentTimeMillis());
        //Returns it as a JSONObject
        return new JSONObject(source);
	}
}
