package humorDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * Retrieves reviews from metacritic.com
 */
public class ReviewCrawler {
	private String htmlCode = "";
	
	//Retrieves the HTML code of the page
	public String HTMLCode(String url) throws IOException{
		BufferedReader in = new BufferedReader(
        	new InputStreamReader(
        		new URL(url).openStream()
        	)
        );
		//HTML code
        String source = "";
        String line = "";
        while ((line = in.readLine()) != null){
        	source += line;
        }
        in.close();
        return source;
	}
	
	/*
	 * Returns the set of Critics given an url
	 */
	public ArrayList<Critic> getCritics(String url) throws Exception{
		this.htmlCode = HTMLCode(url);
		ArrayList<Critic> critics = new ArrayList<Critic>();
		//Parse HTML
		Document doc = Jsoup.parse(this.htmlCode);
		Elements review_bodies = doc.select("div.review_body");
		Elements review_grades = doc.select("div.review_grade div.metascore_w");
		//Loops through comments
		if(review_bodies.size()==review_grades.size()){
			for(int i=review_bodies.size(); i!=0; i--){
				Element r_body = review_bodies.remove(i-1);
				Element r_grade = review_grades.remove(i-1);
				if(r_body.select("span.blurb_expanded").size()==0){
					critics.add(new Critic(Integer.parseInt(r_grade.ownText()),
							r_body.select("span").first().ownText()));
				}else{
					critics.add(new Critic(Integer.parseInt(r_grade.ownText()),
							r_body.select("span.blurb_expanded").first().ownText()));
				}
			}
		}
		return critics;
	}
	
	//Appends to file
	public void appendCriticsToFile(String path, ArrayList<Critic> critics){
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
		    for(Critic c: critics){
		    	out.println(c.getScore() + "\t" + c.getComment());
		    }
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] argv){
		Set<String> urlList = new HashSet<String>();
		ReviewCrawler rc = new ReviewCrawler();
		String letters = "vwxyz";
		for(int i=0; i!=letters.length(); i++){
			String letterPage = "http://www.metacritic.com/browse/movies/title/dvd/" + letters.charAt(i);
			try {
				System.out.println(letterPage);
				String letterPageHTML = rc.HTMLCode(letterPage);
				Matcher m = Pattern.compile("<a href=\"/movie/([a-z0-9-]+)\">")
					     .matcher(letterPageHTML);
				int count = 0;
				while (m.find()) {
					urlList.add("http://www.metacritic.com/movie/"+m.group(1)+"/user-reviews");
					count++;
				}
				System.out.println("\tFound "+count+" movies");
			} catch (IOException e) {
				i--;
				System.err.println("\tError, retry");
			}
		}
		//Retrieves
		for(String url: urlList){
			int i =0;
			while(i<5){
				try {
					ArrayList<Critic> a = rc.getCritics(url);
					System.out.println(url);
					rc.appendCriticsToFile("data/corpus.txt", a);
					break;
				} catch (IOException e) {
					System.err.println("\tError, retry (i="+i+")");
				} catch (Exception e) {
					System.err.println("\tError, couldn't retry (i="+i+")");
					break;
				}
				i++;
			}
		}
	}
}
