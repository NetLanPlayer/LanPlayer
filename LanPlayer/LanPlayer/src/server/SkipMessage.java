package server;

public class SkipMessage {

	int position = 0;
	public int getPosition() {
		return position;
	}

	public String getSkipIp() {
		return skipIp;
	}

	String skipIp = "";
	
	public SkipMessage(int position, String skipIp) {
		this.position = position;
		this.skipIp = skipIp;
	}
		
}
