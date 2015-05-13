public class Processor {
	private static long nbTweetProcessed = 0;
	
	//Main method for processing
	public static void processTweet(String msg){
		System.out.println(msg);
		nbTweetProcessed++;
	}
}
