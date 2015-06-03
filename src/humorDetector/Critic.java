package humorDetector;

import java.util.HashSet;
import java.util.Set;

/*
 * Represents a user critic from metacritic.com
 */
public class Critic {
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
			for(String word: sanitize().split(" ")){
				words.add(word);
			}
		}
		return words;
	}
	
	//Constructor
	public Critic(int score, String comment){
		this.score = score;
		this.comment = comment;
	}
	
	//Constructor from file string
	public Critic(String s){
		this.score = Integer.parseInt(s.split("\t")[0]);
		this.comment = s.split("\t")[1];
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
