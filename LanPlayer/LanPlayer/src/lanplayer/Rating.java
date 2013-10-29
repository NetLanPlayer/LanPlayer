package lanplayer;

import java.util.HashMap;
import java.util.Iterator;

public class Rating implements Comparable<Rating> {
	
	private HashMap<String, Integer> ratingMap = new HashMap<String, Integer>();
	private int rating = 0;
	
	public int getRating() {
		return rating;
	}
	
	public boolean isRatedAbove(int rating) {
		return this.rating >= rating;
	}
	
	public Integer hasRated(String ip) {
		return ratingMap.get(ip);
	}
	
	public Rating(HashMap<String, Integer> ratingMap) {
		this.ratingMap = ratingMap;
		int noRated = ratingMap.keySet().size();
		if(noRated == 0) {
			rating = 0;
		}
		else {
			Iterator<String> it = ratingMap.keySet().iterator();
			int sum = 0;
			while(it.hasNext()) {
				String ip = it.next();
				Integer rated = ratingMap.get(ip);
				sum = sum + rated;
			}
			rating = sum / noRated;
			if(rating > 5) rating = 5;
		}

	}
	
	public String toString() {
		return rating + " / 5";
	}

	@Override
	public int compareTo(Rating o) {
		if(rating == o.rating) return 0;
		if(rating < o.rating) return -1;
		if(rating > o.rating) return 1;
		return -1;
	}


	

}
