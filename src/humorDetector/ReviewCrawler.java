package humorDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
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
	private String url = "";
	private String htmlCode = "";
	
	//Retrieves the HTML code of the page
	private void retrieveHTMLCode() throws IOException{
		BufferedReader in = new BufferedReader(
        	new InputStreamReader(
        		new URL(url).openStream()
        	)
        );
		//Resets HTML code
        htmlCode = "";
        String line = "";
        while ((line = in.readLine()) != null){
        	htmlCode += line;
        }
        in.close();
	}
	
	/*
	 * Returns the set of Critics given an url
	 */
	public ArrayList<Critic> getCritics(String url) throws IOException{
		this.url = url;
		retrieveHTMLCode();
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
		ReviewCrawler rc = new ReviewCrawler();
		String[] urlList = new String[]{
				"http://www.metacritic.com/movie/kingsman-the-secret-service/user-reviews",
				"http://www.metacritic.com/movie/chappie/user-reviews",
				"http://www.metacritic.com/movie/jurassic-park/user-reviews",
				"http://www.metacritic.com/movie/the-dark-knight/user-reviews",
				"http://www.metacritic.com/movie/hellboy/user-reviews",
				"http://www.metacritic.com/movie/daredevil/user-reviews",
				"http://www.metacritic.com/movie/hunger-games/user-reviews",
				"http://www.metacritic.com/movie/fifty-shades-of-grey/user-reviews",
				"http://www.metacritic.com/movie/american-sniper/user-reviews",
				"http://www.metacritic.com/movie/argo/user-reviews",
				"http://www.metacritic.com/movie/a-beautiful-mind/user-reviews",
				"http://www.metacritic.com/movie/500-days-of-summer/user-reviews"
		};
		for(String url: urlList){
			try {
				ArrayList<Critic> a = rc.getCritics(url);
				System.out.println(url);
				rc.appendCriticsToFile("data/corpus.txt", a);
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
	}
}
