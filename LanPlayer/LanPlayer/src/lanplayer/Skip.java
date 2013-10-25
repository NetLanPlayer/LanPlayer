package lanplayer;

import java.util.HashSet;

public class Skip implements Comparable<Skip> {

	private int maxSkipNeeded = 0;
	private HashSet<String> skipIp = new HashSet<String>();
	
	public int getSkip() {
		return skipIp.size();
	}
	
	public Skip(HashSet<String> ips, int participants) {
		this.skipIp = ips;
		this.maxSkipNeeded = (int) Math.ceil( (double) participants / 2);
	}
	
	public boolean hasSkipped(String ip) {
		return skipIp.contains(ip);
	}
	
	public boolean isSkip() {
		return getSkip() >= maxSkipNeeded;
	}
	
	public String toString() {
		return getSkip() + " / " + maxSkipNeeded;
	}

	@Override
	public int compareTo(Skip o) {
		if(o == null || getSkip() > o.getSkip()) return -1;
		if(getSkip() == o.getSkip()) return 0;
		return 1;
	}
	
}

