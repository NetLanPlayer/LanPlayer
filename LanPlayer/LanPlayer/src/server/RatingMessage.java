package server;

public class RatingMessage {

	int position = 0;
	public int getPosition() {
		return position;
	}
	
	String ipPlusRating = "";
	public String getIpPlusRating() {
		return ipPlusRating;
	}
		
	public RatingMessage(int position, String ipPlusRating) {
		this.position = position;
		this.ipPlusRating = ipPlusRating;
	}
	
	
}
