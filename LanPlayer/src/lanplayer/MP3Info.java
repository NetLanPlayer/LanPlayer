package lanplayer;

import java.io.File;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.quippy.javamod.multimedia.mp3.id3.MP3FileID3Controller;

public class MP3Info extends MusicInfo {
	
	private MP3FileID3Controller mp3FileIDTags = null;

	public MP3Info(File musicFile) throws MalformedURLException, UnsupportedAudioFileException {
		super(musicFile);
		mp3FileIDTags = new MP3FileID3Controller(getMultimediaContainer().getFileURL());
		
	}
	
	@Override
	public String getTitle() {
		if (mp3FileIDTags.id3v1Exists()) {
			return mp3FileIDTags.getTitle(MP3FileID3Controller.ID3V1);
		}
		else if(mp3FileIDTags.id3v2Exists()) {
			return mp3FileIDTags.getTitle(MP3FileID3Controller.ID3V2);
		}
		return "";
	}

	@Override
	public String getArtist() {
		if (mp3FileIDTags.id3v1Exists()) {
			return mp3FileIDTags.getArtist(MP3FileID3Controller.ID3V1);
		}
		else if(mp3FileIDTags.id3v2Exists()) {
			return mp3FileIDTags.getArtist(MP3FileID3Controller.ID3V2);
		}
		return "";
	}

	@Override
	public String getAlbum() {
		if (mp3FileIDTags.id3v1Exists()) {
			return mp3FileIDTags.getAlbum(MP3FileID3Controller.ID3V1);
		}
		else if(mp3FileIDTags.id3v2Exists()) {
			return mp3FileIDTags.getAlbum(MP3FileID3Controller.ID3V2);
		}
		return "";
	}

	@Override
	public String getTrackNumber() {
		if (mp3FileIDTags.id3v1Exists()) {
			return mp3FileIDTags.getTrack(MP3FileID3Controller.ID3V1);
		}
		else if(mp3FileIDTags.id3v2Exists()) {
			return mp3FileIDTags.getTrack(MP3FileID3Controller.ID3V2);
		}
		return "";
	}

}
