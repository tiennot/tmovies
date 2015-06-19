package humorDetector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

public class ReviewEqualizer {
	public static void main(String[] argv){
		//reads the corpus from file
		ArrayList<Critic> critics = new ArrayList<Critic>();
		String line;
		int[] count = new int[]{0,0,0,0,0,0,0,0,0,0,0};
		try{
			BufferedReader br = new BufferedReader(new FileReader("data/corpusRaw.txt"));
			while ((line = br.readLine()) != null) {
				if(line.split("\t").length!=2) continue;
				String comment = line.split("\t")[1];
				int score = Integer.parseInt(line.split("\t")[0]);
				if(score<0 || score>10) continue;
				Critic c = new Critic(score, comment);
				c.sanitize();
				if(c.getComment().length()<40 || c.getComment().length()>500) continue;
				critics.add(c);
				count[c.getScore()]++;
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		int nbToKeepForEach = Integer.MAX_VALUE;
		for(int i=0; i!=11; i++)
			if(count[i]<nbToKeepForEach) nbToKeepForEach = count[i];
		System.out.println(nbToKeepForEach);
		
		Random random = new Random();
		for(int i=0; i!=11; i++){
			int toRemove = count[i]-nbToKeepForEach;
			System.out.println("Removes for "+i);
			while(toRemove!=0){
				System.out.println("\ttoRemove="+toRemove+"\tLength="+critics.size());
				int randomNumber = random.nextInt(critics.size());
				if(critics.get(randomNumber).getScore()==i){
					critics.remove(randomNumber);
					toRemove--;
				}
			}
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("data/corpusEqualPlus", "UTF-8");
			for(Critic c: critics){
				writer.println(c.getScore()+"\t"+c.getComment());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
