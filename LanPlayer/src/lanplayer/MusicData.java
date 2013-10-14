package lanplayer;

import java.io.File;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicData {
	
	private MusicInfo musicInfo;
	private File musicFile;
	private String title = "";
	private String album = "";
	private String artist = "";
	private String trackNumber = "";
	private String duration = "";
	private String ip = "";
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
	
	public String getTrackNumber() {
		return musicInfo == null ? "" : musicInfo.getTrackNumber();
	}
	
	public MusicData(int position, File musicFile, String ip, int played, int rating, int skip) throws MalformedURLException, UnsupportedAudioFileException {
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
	}
	
	public String toString() {
		return position + "";
	}

	
}
