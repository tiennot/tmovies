package humorDetector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KNeighbors {
	private ArrayList<Critic> critics;
	private Map<String, Integer> uniqueWords;
	private double[][] matrix;
	int nbOfWords;
	
	public KNeighbors() throws IOException{
		//reads the corpus from file
		critics = new ArrayList<Critic>();
		String line;
		BufferedReader br = new BufferedReader(new FileReader("data/corpus.txt"));
		while ((line = br.readLine()) != null) {
			Critic c = new Critic(line);
			c.sanitize();
			if(c.getComment().length()<30 || c.getComment().length()>300) continue;
			critics.add(c);
		}
		br.close();
		//Computes set of unique words
		uniqueWords = new HashMap<String, Integer>();
		nbOfWords = 0;
		for(Critic c: critics){
			for(String word: c.getWords()){
				if(!uniqueWords.containsKey(word)){
					uniqueWords.put(word,  nbOfWords);
					nbOfWords++;
				}
			}
		}

		matrix = new double[critics.size()][nbOfWords];
		for(int i=0; i!=critics.size(); i++){
			for(String word: critics.get(i).getWords()){
				matrix[i][uniqueWords.get(word)]++;
			}
			double sum = critics.get(i).getWords().size();
			if(sum!=0){
				for(int j=0; j!=nbOfWords; j++){
					matrix[i][j] /= sum;
				}
			}
		}
		System.out.println(critics.size()+"/"+nbOfWords);
		
		//Prints the matrix
		/*for(double[] row: matrix){
			for(double value: row){
				System.out.print(value+" ");
			}
			System.out.println();
		}*/
	}
	
	//Gives the norm of the difference of two critics
	private double norm(int i1, int i2){
		return normVector(matrix[i1], matrix[i2]);
	}
	
	//Returns norm with vectors
	private double normVector(double[] v1, double[] v2){
		double norm = 0;
		for(int j=0; j!=nbOfWords; j++){
			norm += (v1[j]-v2[j])*(v1[j]-v2[j]);
		}
		//System.err.println("Norm("+i1+","+i2+")="+Math.sqrt(norm));
		return Math.sqrt(norm);
	}
	
	public void blabla(){
		double minNorm = Double.MAX_VALUE;
		Critic[] winners = new Critic[2];
		for(int i1=0; i1!=critics.size(); i1++){
			for(int i2=i1+1; i2!=critics.size(); i2++){
				if(this.norm(i1, i2)<minNorm && critics.get(i1).getScore()!=critics.get(i2).getScore()){
					minNorm = this.norm(i1, i2);
					System.out.println(minNorm);
					winners[0] = critics.get(i1);
					winners[1] = critics.get(i2);
				}
			}
		}
		System.out.println("==========");
		winners[0].out();
		winners[1].out();
	}
	
	public void diagnosizeTweet(String tweet){
		Set<String> words = new HashSet<String>();
		for(String word: tweet.split(" ")){
			words.add(word);
		}
		//Fills the vector for the tweet
		double[] vectorTweet = new double[nbOfWords];
		double sum = 0;
		for(String word: words){
			if(uniqueWords.containsKey(word)){
				vectorTweet[uniqueWords.get(word)]++;
				sum++;
			}
		}
		//Normalize
		if(sum!=0){
			for(int j=0; j!=nbOfWords; j++){
				vectorTweet[j] /= sum;
			}
		}
					
		double minNorm = Double.MAX_VALUE;
		//Compares to all critics
		for(int i=0; i!=critics.size(); i++){
			if(normVector(vectorTweet, matrix[i])<minNorm){
				minNorm = normVector(vectorTweet, matrix[i]);
				System.err.println(minNorm);
				critics.get(i).out();
			}
		}
	}
	
	public static void main(String[] argv) throws IOException{
		KNeighbors k = new KNeighbors();
		k.diagnosizeTweet("weird movie lots of interresting stress though");
	}
}
