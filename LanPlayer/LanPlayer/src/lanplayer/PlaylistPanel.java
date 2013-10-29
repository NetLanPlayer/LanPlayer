package lanplayer;

import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import main.ServerGui;
import server.Server;

public class PlaylistPanel extends JPanel {
		
	
	private static final long serialVersionUID = 6634646189831098458L;
	private PlayerPanel playerPanel;
	private ControlPanel controlPanel;
	private Server server;
	
	public void setPlayerPanel(PlayerPanel playerPanel) {
		this.playerPanel = playerPanel;
	}
	
	public PlayerPanel getPlayerPanel() {
		return this.playerPanel;
	}
	
	public ControlPanel getControlPanel() {
		return controlPanel;
	}
	
	private JTable playlistTable;
	
	public JTable getPlaylistTable() {
		return this.playlistTable;
	}
	
	private PlaylistTableModel playlistTableModel;
	private int selectedRow = -1;
	
	private LanData lanData = null;
	
	public ArrayList<MusicData> getPlaylist() {
		
		ArrayList<MusicData> tempList = new ArrayList<MusicData>();
		
		for(int i = 0; i < playlistTable.getRowCount(); i++) {
			MusicData md = (MusicData) playlistTable.getValueAt(i, 0);
			tempList.add(md);
		}
		//System.out.println("Temp List: " + tempList.toString());
		return tempList;
		//return playlistTableModel.getPlayList();
	}
	
	public LanData getLanData() {
		return lanData;
	}
		
	public PlaylistPanel(Server server, ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
		this.server = server;
		initLanData();
		initialize();
	}
		
	private void initLanData() {
		if(!ServerGui.MUSIC_DIR.exists()) {
			ServerGui.MUSIC_DIR.mkdirs();
		}
		if(!ServerGui.LAN_DATA_FILE.exists()) {
			try {
				ServerGui.LAN_DATA_FILE.createNewFile();
			} catch (Exception e) {
			}
		}
		lanData = new LanData(ServerGui.MUSIC_DIR, ServerGui.LAN_DATA_FILE, ServerGui.INIT_PARTICIPANTS, true);
			
		if(!lanData.hasEntries()) {
			lanData.addNewFile(ServerGui.LAN_PLAYER_INIT, "LAN PLAYER", true);
		}
		else {
			//lanData.clear();
			//lanData.addNewFile(ServerGui.LAN_PLAYER_INIT, "LAN PLAYER", true);
			//lanData.refresh();
		}
		lanData.clearNonExistingFiles();
		//lanData.setAndStoreCurPlayed(1);
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
		
		String[] playlistColumnNames = {"Pos", "Title", "Artist", "Album", "Track", "Duration", "Played", "Rating", "Skip", "Date", "Uploader" };
		playlistTable = new JTable();
		playlistTableModel = new PlaylistTableModel(server, this, lanData , playlistColumnNames);
		playlistTable.setModel(playlistTableModel);
		//playlistTable.getTableHeader().setReorderingAllowed(false);
		playlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setPlaylistTableColumnSizes();
		scrollPane.setViewportView(playlistTable);
		
		for(int i = 0; i < playlistTable.getColumnCount(); i++) {
			playlistTable.getColumnModel().getColumn(i).setCellRenderer(new PlaylistTableCellRenderer());
		}
		
		playlistTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2) {
					//int modelIndex = playlistTable.convertRowIndexToModel(playlistTable.getSelectedRow());
					playerPanel.userSelectedPlaylistEntry(playlistTable.getSelectedRow());
		        }
		    }
		});
		
		// Save selected row table
		playlistTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent e) {
		        if(selectedRow != -1) {
		        	selectedRow = e.getFirstIndex();
		        }
		        else {
		        	controlPanel.getBtnDeleteTrack().setEnabled(false);
		        }
		        if(playlistTable.getSelectionModel().isSelectionEmpty()) {
		        	return;
		        }
		        setDeleteBtnState();
		    }
		});
			
//		// Restore selected raw table
//		playlistTable.getModel().addTableModelListener(new TableModelListener() {      
//		    @Override
//		    public void tableChanged(TableModelEvent e) {
//		        SwingUtilities.invokeLater(new Runnable() {
//		            @Override
//		            public void run() {
//		                if (selectedRow >= 0 && selectedRow < playlistTable.getRowCount()) {
//		                	playlistTable.addRowSelectionInterval(selectedRow, selectedRow);
//		                }
//		             }
//		        });
//		    }
//		});
		
		TableRowSorter<TableModel> playlistSorter = new TableRowSorter<TableModel>(playlistTable.getModel()) {			
			public boolean isSortable(int column) {
				PlaylistTableModel ptm = (PlaylistTableModel) playlistTable.getModel();
				if(ptm.isColumnSortable()) {
					return super.isSortable(column);
				}
				else {
					return false;
				}
			}
		};
		playlistTable.setRowSorter(playlistSorter);
		playlistSorter.toggleSortOrder(0);
		playlistSorter.setSortsOnUpdates(true);
		playlistSorter.addRowSorterListener(new RowSorterListener() {

			@Override
			public void sorterChanged(RowSorterEvent arg0) {
				playerPanel.reloadPlaylist();				
			}
			
		});
		
		controlPanel.getSkipField().setText(this.lanData.getParticipants() + "");
		controlPanel.getSkipField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				int prevPartValue = lanData.getParticipants();
				String cont = controlPanel.getSkipField().getText();
				if(!cont.matches("[0-9]")) {
					cont = cont.replaceAll("[^0-9]", "");
				}
				
				int newPartValue = prevPartValue;
				try {
					newPartValue = Integer.parseInt(cont);
				}
				catch(NumberFormatException nfe) {
					controlPanel.getBtnSetSkip().setEnabled(false);
					return;
				}
				
				if(newPartValue < 1) {
					newPartValue = 1;
				}
				
				if(newPartValue != prevPartValue) {
					controlPanel.getBtnSetSkip().setEnabled(true);
				}
				else {
					controlPanel.getBtnSetSkip().setEnabled(false);
				}
				
				controlPanel.getSkipField().setText(newPartValue + "");
			}
		});
		
		controlPanel.getBtnSetSkip().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newPartValue = lanData.getParticipants();
				try {
					newPartValue = Integer.parseInt(controlPanel.getSkipField().getText());
				}
				catch(NumberFormatException nfe) {
					controlPanel.getBtnSetSkip().setEnabled(false);
					return;
				}
				lanData.setAndStoreParticipants(newPartValue);
				controlPanel.getBtnSetSkip().setEnabled(false);
			}
		});
		
		controlPanel.getChckbxShuffle().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				playerPanel.doShuffle(controlPanel.getChckbxShuffle().isSelected());				
			}
		});
		
		controlPanel.getBtnRefreshPlaylist().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lanData.refresh();
			}
		});
		
		controlPanel.getBtnDeleteTrack().addActionListener(new ActionListener()  {
			@Override
			public void actionPerformed(ActionEvent e) {
				int viewRow = playlistTable.getSelectedRow();
				int modelRow = playlistTable.convertRowIndexToModel(viewRow);
				MusicData md = (MusicData) playlistTableModel.getValueAt(modelRow, 0);
				File toDelete = md.getMusicFile();
				toDelete.delete();
				selectedRow = -1;
				restoreSelection();
				lanData.refresh();
			}
		});
		
	}
	
	public void restoreSelection() {
		if (selectedRow >= 0 && selectedRow < playlistTable.getRowCount()) {
			playlistTable.addRowSelectionInterval(selectedRow, selectedRow);
        }
		else {
			playlistTable.clearSelection();
		}
	}
	
	public void setDeleteBtnState() {
		try {
			int modelIndex = playlistTable.convertRowIndexToModel(playlistTable.getSelectedRow());
			if(modelIndex != lanData.getCurrentlyPlayed() - 1 && modelIndex != 0) {
	        	controlPanel.getBtnDeleteTrack().setEnabled(true);
	        }
	        else {
	        	controlPanel.getBtnDeleteTrack().setEnabled(false);
	        }
		}
		catch(Exception e) {
			controlPanel.getBtnDeleteTrack().setEnabled(false);
		}
	}
	
	private void setPlaylistTableColumnSizes() {
		playlistTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		playlistTable.getColumnModel().getColumn(0).setMinWidth(34);
		
		playlistTable.getColumnModel().getColumn(1).setPreferredWidth(250);
		playlistTable.getColumnModel().getColumn(1).setMinWidth(38);
		
		playlistTable.getColumnModel().getColumn(2).setPreferredWidth(170);
		playlistTable.getColumnModel().getColumn(2).setMinWidth(45);
		
		playlistTable.getColumnModel().getColumn(3).setPreferredWidth(120);
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
		playlistTable.getColumnModel().getColumn(9).setMinWidth(38);
		
		playlistTable.getColumnModel().getColumn(10).setPreferredWidth(100);
		playlistTable.getColumnModel().getColumn(10).setMinWidth(65);
	}
		
		
}
