package humorDetector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/*
 * Represents a user critic from metacritic.com
 */
public class Critic {
	//List of Stop Words
	private static Set<String> stopWords = null;
	
	//Score should be between 0 and 10
	private int score; public int getScore(){return score;}
	
	//Comment of the critic (i.e. the text)
	private String comment; public String getComment(){return comment;}
	
	//Set of the words
	private HashSet<String> words = null;
	
	//Gets the words
	public Set<String >getWords(){
		if(words==null){
			words = new HashSet<String>();
			Stemmer s = new Stemmer();
			for(String word: sanitize().split(" ")){
				if(!stopWords.contains(s.stem(word)))
					words.add(s.stem(word));
			}
		}
		return words;
	}
	
	//Constructor
	public Critic(int score, String comment) throws IOException{
		this.score = score;
		this.comment = comment;
		//Initialize stop words for class if necessary
		if(stopWords==null){
			stopWords = new HashSet<String>();
			String stopWord;
			//Read the file and adds each stop word
			BufferedReader br = new BufferedReader(new FileReader("data/stopWords.txt"));
			//Stemmer
			Stemmer stemmer = new Stemmer();
			while ((stopWord = br.readLine()) != null) {
				stopWords.add(stemmer.stem(stopWord));
			}
		}
	}
	
	//Strips all special caracters from the comment
	public String sanitize(){
		return comment
				.toLowerCase()
				.replaceAll("[.!?]", " ")
				.replaceAll("[^a-z\\s]", "")
				.replaceAll("[\\s]{2,}", " ")
				.trim();
	}
	
	//
	public void out(){
		System.out.println("==================="+this.score+"=====================");
		System.out.println(this.comment);
		System.out.println("=======================================");
	}
}
