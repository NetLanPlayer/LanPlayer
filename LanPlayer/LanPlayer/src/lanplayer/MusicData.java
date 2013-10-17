package lanplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;

import javax.sound.sampled.UnsupportedAudioFileException;

import utilities.SimpleDate;

public class MusicData implements Comparable<MusicData> {
	
	private MusicInfo musicInfo;
	private File musicFile;

	private String ip = "";
	private Date date;
	private int rating = 0;
	private int skip = 0;
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

	public int getRating() {
		return rating;
	}

	public int getSkip() {
		return skip;
	}

	public int getPlayed() {
		return played;
	}

	public String getTitle() {
		return musicInfo == null ? "" : musicInfo.getTitle();
	}

	public String getAlbum() {
		return musicInfo == null ? "" : musicInfo.getAlbum();
	}

	public String getArtist() {
		return musicInfo == null ? "" : musicInfo.getArtist();
	}
	
	public String getDuraction() {
		return musicInfo == null ? "" : musicInfo.getDuration();
	}
	
	public SimpleDate getSimpleDate() {
		return new SimpleDate(this.date);
	}
	
	public TrackNumber getTrackNumber() {
		return musicInfo == null ? new TrackNumber(0,null) : musicInfo.getTrackNumber();
	}
	
	public MusicData(int position, File musicFile, String ip, int played, int rating, int skip, Date date) throws MalformedURLException, UnsupportedAudioFileException {
		String extension = musicFile.getName().substring(musicFile.getName().lastIndexOf("."), musicFile.getName().length());
		if(extension.equals(".mp3")) {
			musicInfo = new MP3Info(musicFile);
		}
		else if(extension.equals(".xm")) {
			musicInfo = new ModInfo(musicFile);
		}
		this.position = position;
		this.musicFile = musicFile;
		this.ip = ip;
		this.rating = rating;
		this.skip = skip;
		this.played = played;
		this.date = date;
	}
	
	public String toString() {
		return position + "";
	}

	@Override
	public int compareTo(MusicData other) {
		if(other == null) return 1;
		return new Integer(this.getPosition()).compareTo(new Integer(other.getPosition()));
	}

	
}
