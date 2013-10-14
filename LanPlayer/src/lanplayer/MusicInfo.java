package lanplayer;

import java.io.File;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;

abstract class MusicInfo {
	
	private MultimediaContainer mc;
	
	public MultimediaContainer getMultimediaContainer() {
		return mc;
	}
	
	public String getTitle() {
		return mc.getSongName();
	}
	
	public String getDuration() {
		Object[] infos = mc.getSongInfosFor(mc.getFileURL());
		return Helpers.getTimeStringFromMilliseconds((long) infos[1]);
	}
	
	public abstract String getArtist();
	
	public abstract String getAlbum();
	
	public abstract String getTrackNumber();
	
	public MusicInfo(File musicFile) throws MalformedURLException, UnsupportedAudioFileException {
		mc = MultimediaContainerManager.getMultimediaContainer(musicFile);
	}
	
}
