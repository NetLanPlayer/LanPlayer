package lanplayer;

import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import utilities.MyIp;

public class PlaylistPanel extends JPanel {
	
	private File lanPlayerInit = new File("./src/antipasta.xm");
	
	private File musicDirctory = new File("./LanMusic");
	
	private JTable playlistTable;
	private PlaylistTableModel playlistTableModel;
	
	public final String LAN_DATA_FILE = "./LanMusicData.property";
	
	private LanData lanData = null;
	
	public List<MusicData> getPlaylist() {
		return playlistTableModel.getPlayList();
	}
	
	public LanData getLanData() {
		return lanData;
	}
		
	public PlaylistPanel() {
		initLanData();
		initialize();
	}
	
	private void initLanData() {
		File dataFile = new File(LAN_DATA_FILE);
		if(!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (Exception e) {
			}
		}
		if(!musicDirctory.exists()) {
			musicDirctory.mkdir();
		}
		lanData = new LanData(musicDirctory, dataFile, 1);
		lanData.clearNonExistingFiles();
		
		if(!lanData.hasEntries()) {
			lanData.addNewFile(lanPlayerInit, MyIp.getMyIP());
		}
		lanData.setAndStoreCurPlayed(1);
	}
	
	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);	
		
		JPanel playlistPanel = new JPanel();
		playlistPanel.setBorder(new TitledBorder(null, "Playlist", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_playlistPanel = new GridBagConstraints();
		gbc_playlistPanel.insets = new Insets(5, 5, 5, 5);
		gbc_playlistPanel.fill = GridBagConstraints.BOTH;
		gbc_playlistPanel.gridx = 0;
		gbc_playlistPanel.gridy = 0;
		add(playlistPanel, gbc_playlistPanel);
		GridBagLayout gbl_playlistPanel = new GridBagLayout();
		gbl_playlistPanel.columnWidths = new int[]{0, 0};
		gbl_playlistPanel.rowHeights = new int[]{0, 0};
		gbl_playlistPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_playlistPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		playlistPanel.setLayout(gbl_playlistPanel);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		playlistPanel.add(scrollPane, gbc_scrollPane);
		
		String[] playlistColumnNames = {"Pos", "Title", "Artist", "Album", "Track", "Duration", "Played", "Rating", "Skip", "IP" };
		playlistTableModel = new PlaylistTableModel(lanData , playlistColumnNames);
		playlistTable = new JTable(playlistTableModel);
		playlistTable.getTableHeader().setReorderingAllowed(false);
		playlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setPlaylistTableColumnSizes();
		scrollPane.setViewportView(playlistTable);
	}
	
	private void setPlaylistTableColumnSizes() {
		playlistTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		playlistTable.getColumnModel().getColumn(0).setMinWidth(34);
		
		playlistTable.getColumnModel().getColumn(1).setPreferredWidth(250);
		playlistTable.getColumnModel().getColumn(1).setMinWidth(38);
		
		playlistTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		playlistTable.getColumnModel().getColumn(2).setMinWidth(45);
		
		playlistTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		playlistTable.getColumnModel().getColumn(3).setMinWidth(50);
		
		playlistTable.getColumnModel().getColumn(4).setPreferredWidth(47);
		playlistTable.getColumnModel().getColumn(4).setMinWidth(47);
		
		playlistTable.getColumnModel().getColumn(5).setPreferredWidth(60);
		playlistTable.getColumnModel().getColumn(5).setMinWidth(60);
		
		playlistTable.getColumnModel().getColumn(6).setPreferredWidth(50);
		playlistTable.getColumnModel().getColumn(6).setMinWidth(50);
		
		playlistTable.getColumnModel().getColumn(7).setPreferredWidth(49);
		playlistTable.getColumnModel().getColumn(7).setMinWidth(49);
		
		playlistTable.getColumnModel().getColumn(8).setPreferredWidth(40);
		playlistTable.getColumnModel().getColumn(8).setMinWidth(38);
		
		playlistTable.getColumnModel().getColumn(9).setPreferredWidth(140);
		playlistTable.getColumnModel().getColumn(9).setMinWidth(26);
	}
	
	public File getLanPlayerInit() {
		return lanPlayerInit;
	}
		
}
