package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import javax.swing.table.AbstractTableModel;
import lanplayer.ITableModel;
import lanplayer.LanData;
import lanplayer.MusicData;
import main.ClientGui;

public class ClientTableModel extends AbstractTableModel implements ITableModel, Observer {

	private static final long serialVersionUID = 8800273125331888962L;
	
	private ArrayList<MusicData> playList = new ArrayList<MusicData>();
	public ArrayList<MusicData> getPlayList() {
		return playList;
	}
	
	private LanData lanData;
	private String[] columnNames;
	
	private int rowCount = 0;
	
	public ClientTableModel(ClientGui callee, LanData lanData, String[] columnNames) {
		this.lanData = lanData;
		this.columnNames = columnNames;
		//this.lanData.addObserver(this);
		reloadList();
	}
	
	public void setRowCount(int rowCount) {
		if(rowCount <= 0) {
			playList.clear();
			lanData.clear();
		}
		this.rowCount = rowCount;
		fireTableDataChanged();
	}
	
	public boolean isCurrentlyPlayed(int row) {
		Integer currentlyPlayedPos = this.lanData.getCurrentlyPlayed();
		if(currentlyPlayedPos == null) return false;
		return row == this.lanData.getCurrentlyPlayed() - 1;
	}
	
	private void reloadList() {
		playList.clear();
		ArrayList<MusicData> intermediateList = new ArrayList<MusicData>();
		try {
			this.lanData.loadData();
		} catch (IOException e) {
		}
		for(int i = 1; i <= lanData.getLastPosition(); i++) {
			MusicData md = lanData.getMusicData(i);
			if(md != null) {
				intermediateList.add(md);
			}
		}
		playList = intermediateList;
		rowCount = this.playList.size();
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
		return rowCount;
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
		if(obj instanceof Properties) {
			try {
//				clientGui.getClientTable().clearSelection();
//				rowCount = 0;
//				fireTableDataChanged();
				reloadList();
				fireTableDataChanged();
			}
			catch(Exception e) {
				//zomg
				fireTableDataChanged();
			}
			System.out.println("Client: Refreshing Table Playlist");
		}
	}

}
