package tools;

public class Stats {
	//All tweets got from API
	public static long TWEETS_TOTAL = 0;
	//Tweets whose movie couldn't be found
	public static long TWEETS_NOMOVIE = 0;
	//Tweets found as duplicates (hash)
	public static long TWEETS_DUPLICATE = 0;
	//Tweets where author posted too many tweets
	public static long TWEETS_TOOMANYFROMUSER = 0;
	//Tweets inserted to database
	public static long TWEETS_INSERTED = 0;
	//Tweets marked as top tweets
	public static long TWEETS_TOP = 0;
	//Tweets error JSON
	public static long TWEETS_JSONERROR = 0;
	
	public static void printStats(){
		System.out.println("===============Stats===============");
		System.out.println("TWEETS_TOTAL..............."+TWEETS_TOTAL);
		System.out.println("TWEETS_DUPLICATE..........."+TWEETS_DUPLICATE);
		System.out.println("TWEETS_TOOMANYFROMUSER....."+TWEETS_TOOMANYFROMUSER);
		System.out.println("TWEETS_INSERTED............"+TWEETS_INSERTED);
		System.out.println("TWEETS_TOP................."+TWEETS_TOP);
		System.out.println("TWEETS_JSONERROR..........."+TWEETS_JSONERROR);
		System.out.println("===================================");
	}
}
