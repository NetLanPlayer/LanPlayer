package lanplayer;

public class TrackNumber implements Comparable<TrackNumber> {

	private String album;
	private int track;
	
	public String getAlbum() {
		return album;
	}

	public Integer getTrack() {
		return track;
	}

	public TrackNumber(int track, String album) {
		if(track <= 0) {
			this.track = 0;
		}
		this.track = track;
		this.album = album;
	}
	
	public String toString() {
		if(track <= 0) return "";
		return "" + track;
	}

	@Override
	public int compareTo(TrackNumber other) {
		if(other == null) return 1;
		String otherAlbum = other.getAlbum();
		Integer otherTrack = other.getTrack();		
		if(this.album != null && otherAlbum != null) {
			int compareAlbum = this.album.compareTo(otherAlbum);
			if(compareAlbum == 0) {
				if(this.track > otherTrack) {
					return 1;
				}
				else if(this.track < otherTrack) {
					return -1;
				}
				else {
					return 0;
				}
			}
			else {
				return compareAlbum;
			}
		}
		else if(this.album == null && other.album != null) {
			return -1;
		}
		else if(this.album != null && other.album == null) {
			return 1;
		}
		else {
			if(this.track > otherTrack) {
				return 1;
			}
			else if(this.track < otherTrack) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	
}
