package lanplayer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.table.AbstractTableModel;
import main.ServerGui;
import server.ReceivedFile;
import server.Server;
import server.ServerHandler;

public class PlaylistTableModel extends AbstractTableModel implements Observer {

	private static final long serialVersionUID = 8800273125331888962L;
	
	private ArrayList<MusicData> playList = new ArrayList<MusicData>();
	public ArrayList<MusicData> getPlayList() {
		return playList;
	}
	
	private PlaylistPanel playlistPanel;
	private Server server;
	private LanData lanData;
	private String[] columnNames;
	
	public PlaylistTableModel(Server server, PlaylistPanel playlistPanel, LanData lanData, String[] columnNames) {
		this.playlistPanel = playlistPanel;
		this.server = server;
		this.lanData = lanData;
		this.columnNames = columnNames;
		this.lanData.addObserver(this);
		this.server.getServerHandler().addObserver(this);
		reloadList();
	}
	
	public boolean isCurrentlyPlayed(int row) {
		Integer currentlyPlayedPos = this.lanData.getCurrentlyPlayed();
		if(currentlyPlayedPos == null) return false;
		return row == this.lanData.getCurrentlyPlayed() - 1;
	}
	
	private void reloadList() {
		playList.clear();
		try {
			this.lanData.loadData();
		} catch (IOException e) {
		}
		for(int i = 1; i <= lanData.getLastPosition(); i++) {
			MusicData md = lanData.getMusicData(i);
			if(md != null) {
				playList.add(md);
			}
		}
	}
	
	private boolean columnSortable = true;
	public void setColumnSortable(boolean sortable) {
		columnSortable = sortable;
	}
	
	boolean[] columnEditables = new boolean[] {
		false, false, false, false, false, false, false, false, false, false, false, false
	};
	
	public boolean isCellEditable(int row, int column) {
		return columnEditables[column];
	}
	
	public boolean isColumnSortable() {
		return columnSortable;
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length; // + 1; // adding one column that is invisible
	}

	@Override
	public int getRowCount() {
		return this.playList.size();
	}

	public Class<?> getColumnClass(int column) {
		Class<?> returnValue = Object.class;
        if ((column >= 0) && (column < getColumnCount()) && (getValueAt(0, column) != null)) {
        	Object temp = getValueAt(0, column);
        	if(temp != null) {
        		returnValue = temp.getClass();
        	}
        }
        return returnValue;
    }
	
	public String getColumnName(int column) {
		if(column < columnNames.length) {
		    return columnNames[column];
		}
		else {
			return null; // the invisible column has no name
		}
	}

	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(playList == null || playList.isEmpty() || rowIndex >= playList.size()) return null;
		MusicData musicData = playList.get(rowIndex);
		Object retObj = null;
		if(musicData != null) {
			switch(columnIndex) {
			case 0: retObj = musicData; break;
			case 1: retObj = musicData.getTitle(); break;
			case 2: retObj = musicData.getArtist(); break;
			case 3: retObj = musicData.getAlbum(); break;
			case 4: retObj = musicData.getTrackNumber();	break;
			case 5: retObj = musicData.getDuraction(); break;
			case 6: retObj = musicData.getPlayed(); break;
			case 7: retObj = musicData.getRating(); break;
			case 8: retObj = musicData.getSkip(); break;
			case 9: retObj = musicData.getSimpleDate(); break;
			case 10: retObj = musicData.getIp(); break;
			//case 11: retObj = musicData; break; // invisible column contains musicData itself for convenient data access
			default: retObj = null; break;
			}
		}
		return retObj;
	}

	@Override
	public void update(Observable observable, Object obj) {
		if(observable instanceof LanData) {
			if(obj.equals(LanData.FILE_TAG)) {			
				reloadList();
				fireTableDataChanged();
				playlistPanel.restoreSelection();
				PlayerPanel player = this.playlistPanel.getPlayerPanel();
				if(player != null) {
					player.reloadPlaylist();
				}
				//server.sendFile(lanData.getFile());
			}
			else if(obj.equals(LanData.CURRENTLY_PLAYED_TAG) || obj.equals(LanData.PLAYED_TAG)) {
				reloadList();
				fireTableDataChanged();
				playlistPanel.restoreSelection();
				playlistPanel.setDeleteBtnState();
			}
			else if(obj.equals(LanData.PARTICIPANTS_TAG)) {
				try {
					this.lanData.loadData();
				} catch (IOException e) {
				}
				playlistPanel.getControlPanel().getSkipField().setText("" + lanData.getParticipants());
			}
		}
		else if(observable instanceof ServerHandler) {
			if(obj instanceof ReceivedFile) {
				ReceivedFile rf = (ReceivedFile) obj;
				File rawFile = rf.getFile();
				File newFile = null;
				String extension = rawFile.getName().substring(rawFile.getName().lastIndexOf("."), rawFile.getName().length());
				try {
					MusicData md = new MusicData(this.lanData.getLastPosition() + 1, rawFile, null, null, null, null, null, 0, 0, 0, null, null);
					Integer track = md.getTrackNumber().getTrack();
					String newFileName = (track == null ? "" : track + " - ") +  md.getTitle() + extension;
					newFile = new File(ServerGui.MUSIC_DIR_PATH + newFileName);
					if(!newFile.exists()) {
						rawFile.renameTo(newFile);
					}
					else {
						rawFile.delete();
						return;
					}
				} catch (MalformedURLException | UnsupportedAudioFileException e) {
				}
				if(newFile != null && newFile.exists()) {
					this.lanData.addNewFile(newFile, rf.getIp(), true);
				}
				else {
					this.lanData.addNewFile(rawFile, rf.getIp(), true);
				}
			}
		}
	}

}
