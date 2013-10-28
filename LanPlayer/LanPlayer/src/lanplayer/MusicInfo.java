package lanplayer;

import java.io.File;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;

abstract class MusicInfo {
	
	private File musicFile;
	private String title = "";
	private String duration = "";
		
	public MultimediaContainer getMultimediaContainer() {
		try {
			return MultimediaContainerManager.getMultimediaContainer(musicFile);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public abstract String getArtist();
	
	public abstract String getAlbum();
	
	public abstract TrackNumber getTrackNumber();
	
	public MusicInfo(File musicFile) {
		this.musicFile = musicFile;
		if(getMultimediaContainer() != null) {
			this.title = getMultimediaContainer().getSongName();
			Object[] infos = getMultimediaContainer().getSongInfosFor(getMultimediaContainer().getFileURL());
			this.duration =  Helpers.getTimeStringFromMilliseconds((long) infos[1]);
		}	
		
	}
	
}
