package lanplayer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.ModuleFactory;

public class ModInfo extends MusicInfo {

	private Module mod;
	
	public ModInfo(File musicFile) throws MalformedURLException,
			UnsupportedAudioFileException {
		super(musicFile);
		try {
			mod = ModuleFactory.getInstance(getMultimediaContainer().getFileURL());
		} catch (IOException e) {
		}
	}

	@Override
	public String getArtist() {
		return mod == null ? "" : mod.getSongName();
	}

	@Override
	public String getAlbum() {
		return "";
	}

	@Override
	public TrackNumber getTrackNumber() {
		return new TrackNumber(0, "");
	}

}
