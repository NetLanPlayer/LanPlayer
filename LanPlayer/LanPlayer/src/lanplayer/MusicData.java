package lanplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.sound.sampled.UnsupportedAudioFileException;

import utilities.SimpleDate;

public class MusicData implements Comparable<MusicData> {
	
	private MusicInfo musicInfo;
	private File musicFile;

	private String ip = "";
	private String title = null;
	private String artist = null;
	private Album album = null;
	private TrackNumber trackno = null;
	private String duration = null;
	private Date date;
	private Rating rating;
	private Skip skip;
	private int played = 0;
	private int position = -1;
	
	public int getPosition() {
		return position;
	}
	
	public File getMusicFile() {
		return musicFile;
	}

	public String getIp() {
		return ip;
	}

	public Rating getRating() {
		return rating;
	}

	public Skip getSkip() {
		return skip;
	}

	public int getPlayed() {
		return played;
	}

	public String getTitle() {
		if(title != null && !title.isEmpty()) return title;
		title = musicInfo == null ? "" : musicInfo.getTitle();
		return title;
	}

	public Album getAlbum() {
		if(album != null && !album.getAlbum().isEmpty()) return album;
		album = musicInfo == null ? new Album(0,"") : new Album(getTrackNumber().getTrack(), musicInfo.getAlbum());
		return album;
	}

	public String getArtist() {
		if(artist != null && !artist.isEmpty()) return artist;
		artist = musicInfo == null ? "" : musicInfo.getArtist();
		return artist;
	}
	
	public String getDuraction() {
		if(duration != null && !duration.isEmpty()) return duration;
		duration = musicInfo == null ? "" : musicInfo.getDuration();
		return duration;
		
	}
	
	public TrackNumber getTrackNumber() {
		if(trackno != null && album != null) return trackno;
		trackno = musicInfo == null ? new TrackNumber(0,"") : musicInfo.getTrackNumber();
		return trackno;
	}
	
	public SimpleDate getSimpleDate() {
		return new SimpleDate(this.date);
	}
	
	public MusicData() {
	}
	
	/**
	 * MUSICDATA
	 * @param position
	 * @param musicFile
	 * @param title
	 * @param artist
	 * @param album
	 * @param trackno
	 * @param duration
	 * @param played
	 * @param rating
	 * @param skip
	 * @param date
	 * @param ip
	 * @param participants
	 * @param ratedAbove
	 * @throws MalformedURLException
	 * @throws UnsupportedAudioFileException
	 */
	public MusicData(int position, File musicFile, String title, String artist, String album, String trackno, String duration, int played, String rating, String skip, Date date, String ip, int participants, int ratedAbove) throws MalformedURLException, UnsupportedAudioFileException {
		this.position = position;
		this.musicFile = musicFile;
		this.ip = ip;
		this.played = played;
		this.date = date;
		
		this.title = title;
		this.artist = artist;
		
		this.duration = duration;
		
		this.rating = new Rating(gatherRatingIp(rating), ratedAbove);

		this.skip = new Skip(gatherSkipIp(skip), participants);
		
		try {
			int tryTrackNo = Integer.parseInt(trackno);
			this.trackno = new TrackNumber(tryTrackNo, album);
			this.album = new Album(tryTrackNo, album);
		}
		catch(NumberFormatException nfe) {
		}
		
		if(title == null || artist == null || album == null || trackno == null || duration == null) {
			//	|| title.isEmpty() || artist.isEmpty() || album.isEmpty() || trackno.isEmpty() || duration.isEmpty()) {
			String extension = musicFile.getName().substring(musicFile.getName().lastIndexOf("."), musicFile.getName().length());
			if(extension.equals(".mp3")) {
				musicInfo = new MP3Info(musicFile);
			}
			else if(extension.equals(".xm")) {
				musicInfo = new ModInfo(musicFile);
			}
		}

	}
	
	public String toString() {
		return position + "";
	}

	@Override
	public int compareTo(MusicData other) {
		if(other == null) return 1;
		return new Integer(this.getPosition()).compareTo(new Integer(other.getPosition()));
	}

	private HashSet<String> gatherSkipIp(String skip) {
		HashSet<String> retSkipSet = new HashSet<String>();
		if(skip == null || skip.isEmpty()) return retSkipSet;
		String tagBegin = LanData.IP_TAG.substring(0,  LanData.IP_TAG.indexOf("]") + 1);
		while(skip.contains(tagBegin)) {
			String ip = LanData.getValue(LanData.IP_TAG, skip);
			retSkipSet.add(ip);
			skip = LanData.removeFirstTagValue(LanData.IP_TAG, skip);
		}
		return retSkipSet;
	}
	
	private HashMap<String, Integer> gatherRatingIp(String rating) {
		HashMap<String, Integer> retRatingMap = new HashMap<String, Integer>();
		if(rating == null || rating.isEmpty()) return retRatingMap;
		String tagBegin = LanData.IP_TAG.substring(0,  LanData.IP_TAG.indexOf("]") + 1);
		while(rating.contains(tagBegin)) {
			String ipPlusRating = LanData.getValue(LanData.IP_TAG, rating);
			String ip = ipPlusRating.substring(0, ipPlusRating.indexOf("="));
			String ratedStr = ipPlusRating.substring(ipPlusRating.indexOf("=") + 1, ipPlusRating.length());
			Integer rated = null;
			try {
				rated = Integer.parseInt(ratedStr);
			}
			catch(NumberFormatException nfe) {
				continue;
			}
			retRatingMap.put(ip, rated);
			rating = LanData.removeFirstTagValue(LanData.IP_TAG, rating);
		}
		return retRatingMap;
	}
}
