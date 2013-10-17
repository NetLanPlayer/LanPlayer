package lanplayer;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import utilities.MyIp;
import utilities.SimpleDate;

public class PlaylistPanel extends JPanel {
		
	private final static int INIT_PARTICIPANTS = 10;
	public final static File  LAN_PLAYER_INIT = new File("./src/antipasta.xm");
	private final static File MUSIC_DIR = new File("./LanMusic");
	public final static String LAN_DATA_FILE = "./LanMusicData.property";
	
	private PlayerPanel playerPanel;
	private ControlPanel controlPanel;
	
	public void setPlayerPanel(PlayerPanel playerPanel) {
		this.playerPanel = playerPanel;
	}
	
	public PlayerPanel getPlayerPanel() {
		return this.playerPanel;
	}
	
	private JTable playlistTable;
	
	public JTable getPlaylistTable() {
		return this.playlistTable;
	}
	
	private PlaylistTableModel playlistTableModel;
	private int selectedRow = -1;
	
	private LanData lanData = null;
	
	public ArrayList<MusicData> getPlaylist() {
		return playlistTableModel.getPlayList();
	}
	
	public LanData getLanData() {
		return lanData;
	}
		
	public PlaylistPanel(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
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
		if(!MUSIC_DIR.exists()) {
			MUSIC_DIR.mkdir();
		}
		lanData = new LanData(MUSIC_DIR, dataFile, INIT_PARTICIPANTS);
			
		if(!lanData.hasEntries()) {
			lanData.addNewFile(LAN_PLAYER_INIT, "LAN PLAYER", true);
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
		playlistTableModel = new PlaylistTableModel(this, lanData , playlistColumnNames);
		playlistTable.setModel(playlistTableModel);
		playlistTable.getTableHeader().setReorderingAllowed(false);
		playlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setPlaylistTableColumnSizes();
		scrollPane.setViewportView(playlistTable);
		
		for(int i = 0; i < playlistTable.getColumnCount(); i++) {
			playlistTable.getColumnModel().getColumn(i).setCellRenderer(new PlaylistTableCellRenderer());
		}
		
		playlistTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2) {
					int modelIndex = playlistTable.convertRowIndexToModel(playlistTable.getSelectedRow());
					playerPanel.userSelectedPlaylistEntry(modelIndex);
		        }
		    }
		});
		
		// Save selected row table
		playlistTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent e) {
		        selectedRow = e.getFirstIndex();
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
//		playlistSorter.addRowSorterListener(new RowSorterListener() {
//
//			@Override
//			public void sorterChanged(RowSorterEvent arg0) {
//				playerPanel.reloadPlaylist();				
//			}
//			
//		});
		
	}
	
	public void restoreSelection() {
		if (selectedRow >= 0 && selectedRow < playlistTable.getRowCount()) {
			playlistTable.addRowSelectionInterval(selectedRow, selectedRow);
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
		
	class PlaylistTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8149911584155489952L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        
	        int modelIndex = table.convertRowIndexToModel(row);
	        if(!isSelected && playlistTableModel.isCurrentlyPlayed(modelIndex)) {
	        	label.setBackground(Color.GREEN);
	        }
	        else if(!isSelected) {
	        	label.setBackground(Color.WHITE);
	        }
	        
	        if(value instanceof String) {
	            label.setToolTipText((String) value);
	            label.setIconTextGap(5);
	            label.setHorizontalAlignment(SwingConstants.LEADING);
	            label.setText(" " + (String) value);
	        }
	        else if(value instanceof Number) {
	            int countDigits = value.toString().length();
	            label.setHorizontalAlignment(SwingConstants.TRAILING);
	            label.setIconTextGap(table.getColumnModel().getColumn(0).getWidth() - (countDigits * 5) - 19);
	            label.setText(value.toString() + " ");
	        }
	        else if(value instanceof SimpleDate) {
	        	 label.setToolTipText(value.toString());
		         label.setIconTextGap(5);
		         label.setHorizontalAlignment(SwingConstants.LEADING);
		         //label.setHorizontalAlignment(SwingConstants.CENTER);
		         label.setText(" " + value.toString());
	        	
	        }
	        else {
	        	if(value != null) {
	        		label.setToolTipText(value.toString());
	        		label.setText(" " + value.toString());
	        	} 
	            label.setIconTextGap(5);
	            label.setHorizontalAlignment(SwingConstants.LEADING);
	            
	        }
	        return label;
	    }
		
	}
		
}
