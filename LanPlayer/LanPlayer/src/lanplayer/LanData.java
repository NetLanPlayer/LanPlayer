package lanplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import javax.sound.sampled.UnsupportedAudioFileException;

import main.ServerGui;
import utilities.MD5Hash;
import utilities.SimpleDate;

public class LanData extends Observable {
	
	private int participants = ServerGui.INIT_PARTICIPANTS;
	private int currentlyPlayed = 1;
	private int lastPosition = 1;
	
//	private final static int SEARCH_TIMER_MS = 5000;
	
	public final static String PARTICIPANTS_TAG = "[participants]";
	public final static String LAST_POSITION_TAG = "[lastPos]";
	public final static String CURRENTLY_PLAYED_TAG = "[currentlyPlayed]";
	public final static String PLACEHOLDER = "%";
	public final static String POS_TAG = "[pos]" + PLACEHOLDER + "[/pos]";
	public final static String PLAYED_TAG = "[played]" + PLACEHOLDER + "[/played]";
	public final static String RATING_TAG = "[rating]" + PLACEHOLDER + "[/rating]";
	public final static String SKIP_TAG = "[skip]" + PLACEHOLDER + "[/skip]";
	public final static String IP_TAG = "[ip]" + PLACEHOLDER + "[/ip]";
	public final static String FILE_TAG = "[file]" + PLACEHOLDER + "[/file]";
	public final static String TITLE_TAG = "[title]" + PLACEHOLDER + "[/title]";
	public final static String ARTIST_TAG = "[artist]" + PLACEHOLDER + "[/artist]";
	public final static String ALBUM_TAG = "[album]" + PLACEHOLDER + "[/album]";
	public final static String TRACKNO_TAG = "[trackno]" + PLACEHOLDER + "[/trackno]";
	public final static String DURATION_TAG = "[duration]" + PLACEHOLDER + "[/duration]";
	public final static String DATE_TAG = "[date]" + PLACEHOLDER + "[/date]";
	
	private File propertyFile = null;
	
	public File getFile() {
		return propertyFile;
	}

	private File musicDirectory = null;
	private HashMap<String, Integer> loadedFiles = new HashMap<String, Integer>();
	private Properties data = null;
	
	
	public LanData(File musicDirectory, File propertyFile, int participants, boolean server) throws IllegalArgumentException {
		
		if(!musicDirectory.exists() && !musicDirectory.isDirectory()) {
			throw new IllegalArgumentException("Need musicDirectory to exist and be a directory.");
		}
		this.musicDirectory = musicDirectory;
		
		if(participants < 1) {
			throw new IllegalArgumentException("Need participants > 1");
		}
		this.participants = participants;
		if(propertyFile == null || !propertyFile.exists() || propertyFile.isDirectory()) {
			throw new IllegalArgumentException("Unrecogniced Data File");
		}
		
		String fileName = propertyFile.getName();
		String extension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		if(extension == null || !extension.equals(".property")) {
			throw new IllegalArgumentException("Unrecogniced Data File");
		}
		this.propertyFile = propertyFile;
		
		try {
			loadData();
		} catch (IOException e) {
			this.propertyFile = null;
			throw new IllegalArgumentException("Unrecogniced Data File");
		}
		
		loadClassValues();
		
		try {
			storeData();
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to store Data File");
		}
		
		resetLoadedFiles();
		
		refresh();
		
		// polling disabled for now.
//		if(server) {
//			new Thread(new MusicDirectoryPolling(musicDirectory)).start();
//		}
		
	}
	
	private void loadClassValues() {
		boolean loadPos = loadLastPosition();
		if(!loadPos) {
			data.setProperty(LAST_POSITION_TAG, "" + this.lastPosition);
		}
		
		boolean loadCurrently = loadCurrentlyPlayed();
		if(!loadCurrently) {
			data.setProperty(CURRENTLY_PLAYED_TAG, "" + this.currentlyPlayed);
		}
		
		boolean participants = loadParticipants();
		if(!participants) {
			data.setProperty(PARTICIPANTS_TAG, "" + this.participants);
		}
	}
	
	private void resetLoadedFiles() {
		try {
			loadData();
		} catch (IOException e) {
			loadedFiles.clear();
			return;
		}
		loadedFiles.clear();
		int lastPos = getLastPosition();
		for(int i = 1; i <= lastPos; i++) {
			String formValue = data.getProperty(setValue(POS_TAG, "" + i));
			if(formValue != null && !formValue.isEmpty()) {
				String file = getValue(FILE_TAG, formValue);
				if(file != null && !file.isEmpty()) {
					loadedFiles.put(file, i);
				}
			}			
		}
	}
	
	/**
	 * Checks if this property file has entries.
	 * @return True if it has entries.
	 */
	public boolean hasEntries() {
		String firstEntry = data.getProperty(setValue(POS_TAG, 1 + ""));
		return firstEntry != null;
	}
	
	
	/**
	 * Creates MusicData container for the music file at position. I recommend using loadData() prior
	 * calling this method, to refresh the data.
	 * @param position int position of music file.
	 * @return The MusicData file or null if file was not found or not supported.
	 */
	public MusicData getMusicData(int position) {
		if(position < 0 || position > lastPosition) return null;
		String formValue = data.getProperty(setValue(POS_TAG, "" + position));
		if(formValue == null || formValue.isEmpty()) return null;
		String file = getValue(FILE_TAG, formValue);
		if(file == null || file.isEmpty()) return null;
		
		String ip = getValue(IP_TAG, formValue);
		if(ip == null || ip.isEmpty()) return null;
		
		String playedStr = getValue(PLAYED_TAG, formValue);
		String ratingStr = getValue(RATING_TAG, formValue);
		String skipStr = getValue(SKIP_TAG, formValue);
		String dateStr = getValue(DATE_TAG, formValue);
		Date date = SimpleDate.parseDate(dateStr);

		String titleStr = getValue(TITLE_TAG, formValue);
		String artistStr = getValue(ARTIST_TAG, formValue);
		String albumStr = getValue(ALBUM_TAG, formValue);
		String tracknoStr = getValue(TRACKNO_TAG, formValue);
		String durationStr = getValue(DURATION_TAG, formValue);
		
		int played = 0;
		try {
			played = Integer.parseInt(playedStr);
		}
		catch(Exception e) {
			return null;
		}
		
		File musicFile = new File(file);
		MusicData musicData = null;
		try {
			musicData = new MusicData(position, musicFile, titleStr, artistStr, albumStr, tracknoStr, durationStr, played, ratingStr, skipStr, date, ip, this.participants);
		} catch (Exception e) {
			return null;
		}
		return musicData;
	}
		
	/**
	 * Sets the played number for the file at position in property file.
	 * @param position int position.
	 * @param played int to set.
	 * @return True if successful, false otherwise.
	 */
	public boolean storePlayed(int position, int played) {
		return storeValue(PLAYED_TAG, position, "" + played, true);
	}
	
	/**
	 * Sets the skip value for the file at position in property file.
	 * @param position int position.
	 * @param skip String ip.
	 * @return True if successful, false otherwise.
	 */
	public boolean storeSkip(int position, String ip) {
		return storeValue(SKIP_TAG, position, setValue(LanData.IP_TAG, ip), false);
	}
	
	/**
	 * Sets the rating for the file at position in property file.
	 * @param position int position.
	 * @param ipPlusRating ip and rating to set ('=' separated).
	 * @return True if successful, false otherwise.
	 */
	public boolean storeRating(int position, String ipPlusRating) {
		return storeValue(RATING_TAG, position, setValue(LanData.IP_TAG, ipPlusRating), false);
	}
	
	private boolean storeValue(String tag, int position, String value, boolean overwrite) {
		if(tag == null || tag.isEmpty() || position < 1 || position > lastPosition) return false;
		if(position < 0 || position > lastPosition) return false;
		String formValue = data.getProperty(setValue(POS_TAG, "" + position));
		if(formValue == null || formValue.isEmpty()) return false;
		
		String tagBegin = tag.substring(0, tag.indexOf("]") + 1);
		String tagEnd = tag.substring(tag.lastIndexOf("["), tag.length());
		
		int indexBegin = formValue.indexOf(tagBegin);
		int indexEnd = formValue.indexOf(tagEnd);
		if(indexBegin == -1 || indexEnd == -1) return false;
		
		String pre = formValue.substring(0, indexBegin);
		String post = formValue.substring(indexEnd + tagEnd.length(), formValue.length());
		
		if(!overwrite) {
			String prevValue = formValue.substring(indexBegin + tagBegin.length(), indexEnd) ;
			if(value.contains("=")) {
				String valIp = value.substring(4, value.lastIndexOf("="));
				if(prevValue.contains(valIp)) {
					int indexStart = prevValue.indexOf(valIp);
					String tail = prevValue.substring(indexStart, prevValue.length());
					String prevIpPlusRaiting = tail.substring(0, tail.indexOf("[/ip]"));
					prevValue = prevValue.replaceAll(prevIpPlusRaiting, getValue(IP_TAG, value));
					value = prevValue;
				}
				else {
					value = prevValue + value;
				}
			}
			else {
				value = prevValue + value;
			}
			
			data.setProperty(setValue(POS_TAG, "" + position), pre + tagBegin + value + tagEnd + post);
		}
		else {
			data.setProperty(setValue(POS_TAG, "" + position), pre + tagBegin + value + tagEnd + post);
		}	
		
		try {
			storeData();
		} catch (IOException e) {
			data.setProperty(setValue(POS_TAG, "" + position), formValue);
			return false;
		}
		setChanged();
		notifyObservers(tag);
		return true;
	}
		
	/**
	 * Adds a new file to LanData properties.
	 * @param file File to add.
	 * @param ip Ip of uploader.
	 * @return True if successful, false if problem occurred while storing.
	 */
	public boolean addNewFile(File file, String ip, boolean notify) {
		String path = "";
		try {
			path = file.getCanonicalPath();
			path = path.replaceAll("\\\\", "/");
		} catch (IOException e) {
			return false;
		}
		loadLastPosition();
		boolean posOk = setAndStoreLastPos(lastPosition + 1);
		if(!posOk) {
			return false;
		}
			
		MusicData temp = new MusicData();
		try {
			temp = new MusicData(lastPosition, file, null, null, null, null, null, 0, null, null, new Date(), ip, this.participants);
		} catch (MalformedURLException | UnsupportedAudioFileException e1) {
		}
		
		String value = formBasicValueString(path, temp.getTitle(), temp.getArtist(), temp.getAlbum().getAlbum(), temp.getTrackNumber().getTrack() + "", temp.getDuraction() , 0, "", "", ip);
		data.setProperty(setValue(POS_TAG, "" + lastPosition), value);
		try {
			storeData();
		} catch (IOException e) {
			setAndStoreLastPos(lastPosition - 1);
			return false;
		}
		
		resetLoadedFiles();	
		
		if(notify) {
			setChanged();
			notifyObservers(FILE_TAG);
		}
		
		return true;
	}
		
	private String formBasicValueString(String filePath, String title, String artist, String album, String trackno, String duration, int played, String rating, String skip, String ip) {
		StringBuilder form = new StringBuilder();
		String modPath = filePath.replaceAll("\\\\", "/");
		form.append(setValue(FILE_TAG, modPath));

		form.append(setValue(TITLE_TAG, title));
		form.append(setValue(ARTIST_TAG, artist));
		form.append(setValue(ALBUM_TAG, album));
		form.append(setValue(TRACKNO_TAG, trackno));
		form.append(setValue(DURATION_TAG, duration));
		form.append(setValue(IP_TAG, ip)); // must be before rating and skip!
		form.append(setValue(PLAYED_TAG, "" + played));
		form.append(setValue(RATING_TAG, rating));
		form.append(setValue(SKIP_TAG, skip));
		form.append(setValue(DATE_TAG, new SimpleDate(new Date()).toString()));
		
		return form.toString();
	}
	
	public static String setValue(String tag, String value) {
		if(value == null) value = "";
		return tag.replaceAll(PLACEHOLDER, value);
	}
	
	/**
	 * Acquire value from file at 'position' for 'tag'.
	 * @param tag String tag.
	 * @param position int position of file.
	 * @return String value or null if not found.
	 */
	public String getValue(String tag, int position) {
		if(tag == null || tag.isEmpty() || position < 1 || position > lastPosition) return null;
		String formValue = data.getProperty(setValue(POS_TAG, "" + position));
		return getValue(tag, formValue);
	}
	
	public static String getValue(String tag, String formValue) {
		if(tag == null || tag.isEmpty() || formValue == null || formValue.isEmpty()) return null;
		String tagBegin = tag.substring(0, tag.indexOf("]") + 1);
		String tagEnd = tag.substring(tag.lastIndexOf("["), tag.length());
		
		int indexBegin = formValue.indexOf(tagBegin);
		int indexEnd = formValue.indexOf(tagEnd);
		if(indexBegin == -1 || indexEnd == -1) return null;		
		return formValue.substring(indexBegin + tagBegin.length(), indexEnd);
	}
	
	public static String removeFirstTagValue(String tag, String formValue) {
		if(tag == null || tag.isEmpty() || formValue == null || formValue.isEmpty()) return null;
		String tagBegin = tag.substring(0, tag.indexOf("]") + 1);
		String tagEnd = tag.substring(tag.lastIndexOf("["), tag.length());
		
		int indexBegin = formValue.indexOf(tagBegin);
		int indexEnd = formValue.indexOf(tagEnd);
		if(indexBegin == -1 || indexEnd == -1) return null;
		
		return formValue.substring(indexEnd + tagEnd.length(), formValue.length());
	}
	
	/**
	 * Clears the property data. Note that it is not stored.
	 */
	public void clear() {
		data.clear();
	}
	
	/**
	 * Loads the LanData properties file into this class, should be called when the properties file has changed.
	 * @throws IOException Is thrown when the file couldn't be loaded.
	 */
	public void loadData() throws IOException {
		Properties temp = new Properties();
		try(FileInputStream fis = new FileInputStream(propertyFile)) {
			temp.load(fis);
		}
		data = temp;
		loadCurrentlyPlayed();
		loadLastPosition();
		loadParticipants();
	}
	
	/**
	 * Stores the LanData properties to the file. 
	 * @throws IOException Is thrown then the data couldn't be stored to file.
	 */
	public void storeData() throws IOException {
		try(FileOutputStream out = new FileOutputStream(propertyFile)) {
			data.store(out, "LAN DATA");
		}
	}
	
	/**
	 * Sets and stores the currently played position in class and property file.
	 * @param position int position.
	 * @return True if successful, false otherwise. Needs position > 0 and position <= lastPosition. 
	 */
	public boolean setAndStoreCurPlayed(int position) {
		if(position < 1 || position > lastPosition) return false;
		data.setProperty(CURRENTLY_PLAYED_TAG, position + "");
		try {
			storeData();
		} catch (IOException e) {
			return false;
		}
		currentlyPlayed = position;
		setChanged();
		notifyObservers(CURRENTLY_PLAYED_TAG);
		return true;
	}
	
	/**
	 * @return Integer of the position currently played.
	 */
	public Integer getCurrentlyPlayed() {
		//boolean success = loadCurrentlyPlayed();
		//if(success) {
			return currentlyPlayed;
		//}
		//return null;
	}
	
	/**
	 * Loads the currently played position from properties file into this class.
	 * @return boolean success.
	 */
	private boolean loadCurrentlyPlayed() {
		if(data == null) return false;
		String currentlyPlayedStr = data.getProperty(CURRENTLY_PLAYED_TAG);
		int temp = currentlyPlayed;
		try {
			temp = Integer.parseInt(currentlyPlayedStr);
		} catch(NumberFormatException nfe) {
			return false;
		}
		currentlyPlayed = temp;
		return true;
	}
	
	private boolean setAndStoreLastPos(int lastPosToSet) {
		data.setProperty(LAST_POSITION_TAG, "" + lastPosToSet);
		try {
			storeData();
		} catch (IOException e) {
			return false;
		}
		lastPosition = lastPosToSet;
		setChanged();
		notifyObservers(LAST_POSITION_TAG);
		return true;
	}
	

	/**
	 * 	@return Integer of the last position. Is null if the loading was not successful.
	 */
	public Integer getLastPosition() {
		//boolean success = loadLastPosition();
		//if(success) {
			return lastPosition;
		//}
		//return null;
	}
	
	/**
	 * Loads the number of files from properties file into this class.
	 * @return Integer of the last position. Is null if the loading was not successful.
	 */
	private boolean loadLastPosition() {
		if(data == null) return false;
		String lastPosStr = data.getProperty(LAST_POSITION_TAG);
		int temp = lastPosition;
		try {
			temp = Integer.parseInt(lastPosStr);
		} catch(NumberFormatException nfe) {
			return false;
		}
		lastPosition = temp;
		return true;
	}
	
	/**
	 * Sets and stores the participants value in class and property file.
	 * @param participants int max skip needed.
	 * @return True if successful, false otherwise.
	 */
	public boolean setAndStoreParticipants(int participants) {
		data.setProperty(PARTICIPANTS_TAG, participants + "");
		try {
			storeData();
		} catch (IOException e) {
			return false;
		}
		this.participants = participants;
		setChanged();
		notifyObservers(PARTICIPANTS_TAG);
		return true;
	}
	
	/**
	 * @return Integer number of participants.
	 */
	public Integer getParticipants() {
		//boolean success = loadParticipants();
		//if(success) {
			return participants;
		//}
		//return null;
	}
	
	/**
	 * Loads the number participants needed from properties file into this class.
	 * @return Integer of the max skip needed. Is null if the loading was not successful.
	 */
	private boolean loadParticipants() {
		if(data == null) return false;
		String partiStr = data.getProperty(PARTICIPANTS_TAG);
		int temp = participants;
		try {
			temp = Integer.parseInt(partiStr);
		} catch(NumberFormatException nfe) {
			return false;
		}
		participants = temp;
		return true;
	}
	
	/**
	 * This method clears the property file from all files that do not exist (anymore).
	 * Also note, that currentlyPlayed and participants are reset.
	 */
	public void clearNonExistingFiles() {
		
		List<String> validFiles = new ArrayList<String>();
		
		try {
			loadData();
		} catch (IOException e) {
		}

		int lastPos = getLastPosition();
		int participants = getParticipants();
		int currentlyPlayed = getCurrentlyPlayed();
		
		for(int i = 1; i <= lastPos; i++) {
			String formValue = data.getProperty(setValue(POS_TAG, "" + i));
			String filePath = getValue(FILE_TAG, formValue);
			if(filePath != null) {
				File test = new File(filePath);
				if(test.exists()) {
					validFiles.add(formValue);
				}
				else {
					if(i < currentlyPlayed) {
						currentlyPlayed--;
					}
					else if(i == currentlyPlayed) { // a currentlyPlayed file has been deleted
						currentlyPlayed++;
					}
				}
			}

		}
		
		if(currentlyPlayed > validFiles.size()) {
			currentlyPlayed = 1;
		}
		
		String originalChecksum = MD5Hash.getChecksum(propertyFile);
		
		data.clear();
			
		data.setProperty(CURRENTLY_PLAYED_TAG, currentlyPlayed + "");
		data.setProperty(LAST_POSITION_TAG, validFiles.size() + "");
		data.setProperty(PARTICIPANTS_TAG, participants + "");
				
		for(int x = 0; x < validFiles.size(); x++) {
			data.setProperty(setValue(POS_TAG, "" + (x + 1)), validFiles.get(x));
		}
		try {
			storeData();
		} catch (IOException e) {
		}
		
		resetLoadedFiles();
		
		String newChecksum = MD5Hash.getChecksum(propertyFile);
		
		if(originalChecksum != null && !originalChecksum.equals(newChecksum)) {
			setChanged();
			notifyObservers(FILE_TAG);
		}

	}
	
	public void refresh() {
		HashSet<String> allowedExt = new HashSet<String>();
		allowedExt.add(".mp3");
		allowedExt.add(".xm");
		
		List<File> gatherList = new ArrayList<File>();
		gatherMusicFiles(allowedExt, gatherList, musicDirectory);
//		boolean notify = false;
		for(File f : gatherList) {
			String path;
			try {
				path = f.getCanonicalPath();
				path = path.replaceAll("\\\\", "/");
			} catch(IOException e) {
				continue;
			}
			if(!loadedFiles.keySet().contains(path)) {
				addNewFile(f, "Polling", false);
//				notify = true;
			}
		}
		//if(notify) {
			setChanged();
			notifyObservers(FILE_TAG);
		//}
		clearNonExistingFiles();
	}
	
	private void gatherMusicFiles(HashSet<String> allowedExt, List<File> gatherList, File directory) {
		if(directory == null || !directory.isDirectory()) return;
		File[] files = directory.listFiles();
		if(files == null) return;
		for(File f : files) {
			if(f.isDirectory()) {
				gatherMusicFiles(allowedExt, gatherList, f);
			}
			else {
				if(f.getName().contains(".")) {
					String extension = f.getName().substring(f.getName().lastIndexOf("."), f.getName().length());
					if(allowedExt.contains(extension)) {
						gatherList.add(f);
					}
				}
			}
		}
	}
	
//	class MusicDirectoryPolling implements Runnable {
//		
//		private File musicDirectory;
//		
//		private HashSet<String> allowedExt = new HashSet<String>();
//		
//		public MusicDirectoryPolling(File musicDirectory) {
//			this.musicDirectory = musicDirectory;
//			allowedExt.add(".mp3");
//			allowedExt.add(".xm");
//		}
//		
//		@Override
//		public void run() {
//			while(true) {
//				try {
//					Thread.sleep(SEARCH_TIMER_MS);
//				} catch (InterruptedException e) {
//				}
//				List<File> gatherList = new ArrayList<File>();
//				gatherMusicFiles(gatherList, musicDirectory);
//				boolean notify = false;
//				for(File f : gatherList) {
//					String path;
//					try {
//						path = f.getCanonicalPath();
//						path = path.replaceAll("\\\\", "/");
//					} catch(IOException e) {
//						continue;
//					}
//					if(!loadedFiles.contains(path)) {
//						addNewFile(f, "Polling", false);
//						notify = true;
//					}
//				}
//				if(notify) {
//					setChanged();
//					notifyObservers(FILE_TAG);
//				}
//				clearNonExistingFiles();
//			}
//		}
//		
//		private void gatherMusicFiles(List<File> gatherList, File directory) {
//			File[] files = directory.listFiles();
//			for(File f : files) {
//				if(f.isDirectory()) {
//					gatherMusicFiles(gatherList, f);
//				}
//				else {
//					String extension = f.getName().substring(f.getName().lastIndexOf("."), f.getName().length());
//					if(allowedExt.contains(extension)) {
//						gatherList.add(f);
//					}
//				}
//			}
//		}
//		
//		
//	}

}
