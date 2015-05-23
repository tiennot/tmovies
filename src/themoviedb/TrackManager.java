package themoviedb;

import java.util.ArrayList;
import java.util.HashMap;

import tools.MySQLClient;

/*
 * This class is in charge for generating keywords for the streaming API
 * Using our internal movie database. Should be called regularly to ensure 
 * keywords for new movies are added
 */
public class TrackManager {
	private MySQLClient sqlClient;
	//Keeps the list of keywords
	private ArrayList<String> keywordList;
	//Maps keywords to movies id
	private HashMap<String, Long> keywordTracker;
	
	//Constructor takes a SQL client
	public TrackManager(MySQLClient sqlClient){
		this.sqlClient = sqlClient;
		keywordTracker = new HashMap<String, Long>();
		keywordList = new ArrayList<String>();
	}
	
	//This method tries to get a hashtag for a title
	private String createHashtag(String title){
		return "#"+ title.toLowerCase().replaceAll("[^a-z0-9]+", "");
	}
	
	//This method is supposed to sanitize the title of a movie
	private String sanitizeTitle(String title){
		System.out.println("[[[["+title.toLowerCase().replaceAll("[:!?]+", "")+"]]]]");
		return title.toLowerCase().replaceAll("[:!?]+", "");
	}
	
	//Method to be called from outside
	public String generateTrackString() throws Exception{
		StringBuffer trackString = new StringBuffer();
		//Get list of ids and titles from db
		ArrayList<IdAndTitle> idsAndTitles = sqlClient.getMovieTitlesFromDb();
		//Add titles and hashtags
		for(IdAndTitle idAndTitle: idsAndTitles){
			//Generate the classic term
			/*String classic = sanitizeTitle(idAndTitle.getTitle());
			keywordList.add(classic);
			keywordTracker.put(classic,  idAndTitle.getId());
			trackString.append( classic + ",");*/
			//Generate the hashtag
			String hashtag = createHashtag(idAndTitle.getTitle());
			keywordList.add(hashtag);
			keywordTracker.put(hashtag,  idAndTitle.getId());
			trackString.append( hashtag + ", ");
		}
		//Removes last comma and returns
		if(trackString.length()==0) return "";
		return trackString.substring(0, trackString.length()-2);
	}
	
	//Looks for the movie associated with a text
	public long findMovieIdForText(String text){
		text = text.toLowerCase();
		for(String keyword: keywordList){
			if(text.indexOf(keyword)!=-1){
				return keywordTracker.get(keyword);
			}
		}
		return -1;
	}
	
	//Little class to store a pair id-title
	public static class IdAndTitle{
		private final long id;
		private final String title;
		public IdAndTitle(long id, String title){
			this.id = id;
			this.title = title;
		}
		public final String getTitle(){return title;}
		public final long getId(){return id;}
	}
}
