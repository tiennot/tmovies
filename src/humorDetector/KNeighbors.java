package humorDetector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KNeighbors {
	private static int k = 10;
	private ArrayList<Critic> critics;
	public ArrayList<Critic> testCritics;
	private Map<String, ArrayList<Critic>> criticsForWords;
	private double[][] matrix;
	int nbOfWords;
	
	public KNeighbors(){
		//reads the corpus from file
		critics = new ArrayList<Critic>();
		testCritics = new ArrayList<Critic>();
		String line;
		int count = 0;
		try{
			BufferedReader br = new BufferedReader(new FileReader("data/corpus.txt"));
			while ((line = br.readLine()) != null) {
				if(line.split("\t").length!=2) continue;
				Critic c = new Critic(Integer.parseInt(line.split("\t")[0]), line.split("\t")[1]);
				c.sanitize();
				if(c.getComment().length()<30 || c.getComment().length()>150) continue;
				if(count%10==8 || count%10==9) testCritics.add(c);
				else critics.add(c);
				count++;
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//Computes set of unique words
		criticsForWords = new HashMap<String, ArrayList<Critic>>(); 
		nbOfWords = 0;
		for(Critic c: critics){
			for(String word: c.getWords()){
				if(!criticsForWords.containsKey(word)){
					criticsForWords.put(word,  new ArrayList<Critic>());
					nbOfWords++;
				}
				criticsForWords.get(word).add(c);
			}
		}
		System.out.println(critics.size()+"/"+nbOfWords);
	}
	
	//Gives the norm of the difference of two critics
	private double norm(Critic c1, Critic c2){
		double norm = 0;
		double count1 = (double) c1.getWords().size(), count2 = (double) c2.getWords().size();
		for(String word: c1.getWords()){
			if(c2.getWords().contains(word)){
				norm += (1./count1 - 1./count2) * (1./count1 - 1./count2);
			}else{
				norm += 1./count1 * 1./count1;
			}
		}
		for(String word: c2.getWords()){
			if(!c1.getWords().contains(word)){
				norm += 1./count2 * 1./count2;
			}
		}
		return Math.sqrt(norm);
	}
	
	public double kNeighbors(String tweet) throws IOException{
		//Represents a potential neighbor
		class Neighbor implements Comparable<Neighbor>{
			Critic c;
			double distance;
			Neighbor(Critic c, double distance){
				this.c = c;
				this.distance = distance;
			}
			public int compareTo(Neighbor that) {
				return Double.compare(this.distance, that.distance);
			}
		}
		//The tweet
		Critic cTweet = new Critic(5, tweet);
		//Compares to all critics
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
		//Explores candidates
		Set<Critic> explored = new HashSet<Critic>();
		for(String word: cTweet.getWords()){
			if(!criticsForWords.containsKey(word)) continue;
			ArrayList<Critic> criticsForWord = criticsForWords.get(word);
			for(Critic c: criticsForWord){
				if(explored.contains(c)) continue;
				explored.add(c);
				neighbors.add(new Neighbor(c, norm(cTweet, c)));
			}
			if(neighbors.size()>k){
				Collections.sort(neighbors);
				neighbors = new ArrayList<Neighbor>(neighbors.subList(0, k));
			}
		}
		
		double score = 0, sumDist = 0;
		for(Neighbor n: neighbors){
			//System.out.println(n.distance + " / "+n.c.getScore()+" / "+n.c.getComment());
			score += n.distance*((double)n.c.getScore());
			sumDist += n.distance;
		}
		if(sumDist!=0) score /= sumDist;
		return score;
	}
	
	public static void main(String[] argv) throws IOException{
		KNeighbors k = new KNeighbors();
		for(KNeighbors.k=3; KNeighbors.k!=51; KNeighbors.k+=2){
			double sumEcarts = 0;
			int count = 0;
			int nbSample = Math.max(300, k.testCritics.size());
			for(Critic c: k.testCritics){
				double computedScore = Math.round(k.kNeighbors(c.getComment()));
				sumEcarts += Math.abs(computedScore-c.getScore());
				//System.out.println("\t"+count+"/"+k.testCritics.size());
				count++;
				if(count==nbSample) break;
			}
			System.out.println("Ecart moyen pour k="+KNeighbors.k+": "+ (sumEcarts/((double)nbSample)));
		}
	}
}
