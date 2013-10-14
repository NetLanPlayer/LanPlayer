package lanplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;

abstract class MusicInfo {
	
	private File musicFile;
		
	public MultimediaContainer getMultimediaContainer() {
		try {
			return MultimediaContainerManager.getMultimediaContainer(musicFile);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getTitle() {
		return getMultimediaContainer() == null ? "" : getMultimediaContainer().getSongName();
	}
	
	public String getDuration() {
		if(getMultimediaContainer() == null) return "";
		Object[] infos = getMultimediaContainer().getSongInfosFor(getMultimediaContainer().getFileURL());
		return Helpers.getTimeStringFromMilliseconds((long) infos[1]);
	}
	
	public abstract String getArtist();
	
	public abstract String getAlbum();
	
	public abstract String getTrackNumber();
	
	public MusicInfo(File musicFile) {
		this.musicFile = musicFile;
	}
	
}
