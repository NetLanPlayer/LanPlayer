package main;

import utilities.IPAddressValidator;
import utilities.MyIp;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import client.Client;
import client.ClientHandler;
import client.ClientTableModel;
import lanplayer.LanData;
import lanplayer.MusicData;
import lanplayer.PlaylistTableCellRenderer;
import javax.swing.JProgressBar;

public class ClientGui extends JFrame {

	private final static int INIT_PARTICIPANTS = 1;
	public final static File DATA_DIR = new File("./ClientData/");
	public final static File LAN_DATA_FILE = new File("./ClientData/LanMusicData.property");

	private final static String INIT_IP_TEXT = "localhost";

	private final static String MY_IP = MyIp.getMyIP();

	private LanData lanData = null;

	public LanData getLanData() {
		return lanData;
	}

	private static final long serialVersionUID = 3886409992076543386L;
	private JPanel contentPane;

	private JTextField txtEnterIpAddress;
	private JTextField txtEnterPath;

	private Client client;
	private IPAddressValidator ipVal = new IPAddressValidator();

	private JButton btnUpload;

	public JButton getBtnUpload() {
		return btnUpload;
	}

	private JPanel connectPanel;
	private JPanel uploadPanel;
	private JPanel playlistPanel;

	private JTable clientTable;

	private ClientTableModel clientTableModel;

	private JScrollPane scrollPane;
	private JButton btnSkip;
	private JComboBox<Integer> ratingBox;
	private JButton btnRate;
	final private JButton btnConnect;
	private JProgressBar uploadBar;
	private JButton btnSearch;
	private JButton btnRefresh;
	private String[] playlistColumnNames = new String[] { "Pos", "Title", "Artist", "Album", "Track", "Duration", "Played", "Rating", "Skip", "Date", "Uploader" };

	public JProgressBar getUploadBar() {
		return uploadBar;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGui frame = new ClientGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientGui() {
		setTitle("Frame");
		btnConnect = new JButton("Connect");
		initLanData();
		initialize();
	}

	private void initialize() {
		setMinimumSize(new Dimension(1200, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 490, 163);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 480, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		connectPanel = new JPanel();
		connectPanel.setBorder(new TitledBorder(null, "Connection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_connectPanel = new GridBagConstraints();
		gbc_connectPanel.insets = new Insets(0, 0, 5, 0);
		gbc_connectPanel.fill = GridBagConstraints.BOTH;
		gbc_connectPanel.gridx = 0;
		gbc_connectPanel.gridy = 0;
		contentPane.add(connectPanel, gbc_connectPanel);
		GridBagLayout gbl_connectPanel = new GridBagLayout();
		gbl_connectPanel.columnWidths = new int[] { 0, 165, 0 };
		gbl_connectPanel.rowHeights = new int[] { 30, 0 };
		gbl_connectPanel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_connectPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		connectPanel.setLayout(gbl_connectPanel);

		txtEnterIpAddress = new JTextField();
		GridBagConstraints gbc_txtEnterIpAddress = new GridBagConstraints();
		gbc_txtEnterIpAddress.fill = GridBagConstraints.BOTH;
		gbc_txtEnterIpAddress.insets = new Insets(5, 5, 5, 5);
		gbc_txtEnterIpAddress.gridx = 0;
		gbc_txtEnterIpAddress.gridy = 0;
		connectPanel.add(txtEnterIpAddress, gbc_txtEnterIpAddress);
		txtEnterIpAddress.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				if (txtEnterIpAddress.getText() == null)
					return;
				txtEnterIpAddress.select(0, txtEnterIpAddress.getText().length());
			}

		});

		txtEnterIpAddress.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (txtEnterIpAddress.isEditable())
					if (arg0.getKeyCode() == KeyEvent.VK_ENTER)
						connectButtonAction();

			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}

		});
		txtEnterIpAddress.setText(INIT_IP_TEXT);
		txtEnterIpAddress.setSelectionStart(0);
		txtEnterIpAddress.setColumns(10);
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnConnect.insets = new Insets(5, 5, 5, 5);
		gbc_btnConnect.gridx = 1;
		gbc_btnConnect.gridy = 0;
		connectPanel.add(btnConnect, gbc_btnConnect);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connectButtonAction();
			}
		});

		uploadPanel = new JPanel();
		uploadPanel.setBorder(new TitledBorder(null, "Music File Upload", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_uploadPanel = new GridBagConstraints();
		gbc_uploadPanel.insets = new Insets(0, 0, 5, 0);
		gbc_uploadPanel.fill = GridBagConstraints.BOTH;
		gbc_uploadPanel.gridx = 0;
		gbc_uploadPanel.gridy = 1;
		contentPane.add(uploadPanel, gbc_uploadPanel);
		GridBagLayout gbl_uploadPanel = new GridBagLayout();
		gbl_uploadPanel.columnWidths = new int[] { 0, 165, 0 };
		gbl_uploadPanel.rowHeights = new int[] { 30, 20, 0 };
		gbl_uploadPanel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_uploadPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		uploadPanel.setLayout(gbl_uploadPanel);

		txtEnterPath = new JTextField();
		GridBagConstraints gbc_txtEnterPath = new GridBagConstraints();
		gbc_txtEnterPath.fill = GridBagConstraints.BOTH;
		gbc_txtEnterPath.insets = new Insets(5, 5, 5, 5);
		gbc_txtEnterPath.gridx = 0;
		gbc_txtEnterPath.gridy = 0;
		uploadPanel.add(txtEnterPath, gbc_txtEnterPath);
		// txtEnterPath.addFocusListener(new FocusAdapter() {
		//
		// @Override
		// public void focusGained(FocusEvent arg0) {
		// if (txtEnterPath.getText() == null)
		// return;
		// txtEnterPath.select(0, txtEnterPath.getText().length());
		// }
		// });

		txtEnterPath.addKeyListener(new KeyAdapter() {

			public void keyReleased(KeyEvent ke) {
				if (client == null)
					return;
				if (client.isValidPath(txtEnterPath.getText())) {
					btnUpload.setEnabled(true);
				} else {
					btnUpload.setEnabled(false);
				}
			}

		});

		txtEnterPath.setText("");
		txtEnterPath.setEditable(false);
		txtEnterPath.setColumns(10);

		btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Search track or Directory");
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.isDirectory())
							return true;
						return f.getName().endsWith(".mp3");
					}

					@Override
					public String getDescription() {
						return "*.mp3";
					}

				});
				int choosen = chooser.showOpenDialog(txtEnterPath);
				if (choosen == JFileChooser.APPROVE_OPTION) {
					txtEnterPath.setText(chooser.getCurrentDirectory().toString() + "\\" + chooser.getSelectedFile().getName());
				}
				if (client != null && client.isValidPath(txtEnterPath.getText())) {
					btnUpload.setEnabled(true);
				} else {
					btnUpload.setEnabled(false);
				}
			}
		});
		btnSearch.setEnabled(false);
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSearch.insets = new Insets(5, 5, 5, 5);
		gbc_btnSearch.gridx = 1;
		gbc_btnSearch.gridy = 0;
		uploadPanel.add(btnSearch, gbc_btnSearch);

		uploadBar = new JProgressBar();
		uploadBar.setEnabled(false);
		uploadBar.setStringPainted(true);
		GridBagConstraints gbc_uploadBar = new GridBagConstraints();
		gbc_uploadBar.fill = GridBagConstraints.BOTH;
		gbc_uploadBar.insets = new Insets(5, 5, 5, 5);
		gbc_uploadBar.gridx = 0;
		gbc_uploadBar.gridy = 1;
		uploadPanel.add(uploadBar, gbc_uploadBar);

		btnUpload = new JButton("Upload");
		GridBagConstraints gbc_btnUpload = new GridBagConstraints();
		gbc_btnUpload.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUpload.insets = new Insets(5, 5, 5, 5);
		gbc_btnUpload.gridx = 1;
		gbc_btnUpload.gridy = 1;
		uploadPanel.add(btnUpload, gbc_btnUpload);
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (client.checkPathAndSend(txtEnterPath.getText())) {
					enablePathAndSearch(false);
					btnUpload.setEnabled(false);
				} else {
					txtEnterPath.setText("Path was wrong");
					enablePathAndSearch(true);
				}
			}
		});
		btnUpload.setEnabled(false);

		playlistPanel = new JPanel();
		playlistPanel.setBorder(new TitledBorder(null, "Playlist", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_playlistPanel = new GridBagConstraints();
		gbc_playlistPanel.fill = GridBagConstraints.BOTH;
		gbc_playlistPanel.gridx = 0;
		gbc_playlistPanel.gridy = 2;
		contentPane.add(playlistPanel, gbc_playlistPanel);
		GridBagLayout gbl_playlistPanel = new GridBagLayout();
		gbl_playlistPanel.columnWidths = new int[] { 0, 60, 90, 0 };
		gbl_playlistPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_playlistPanel.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_playlistPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		playlistPanel.setLayout(gbl_playlistPanel);

		scrollPane = new JScrollPane();
		scrollPane.setEnabled(false);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		playlistPanel.add(scrollPane, gbc_scrollPane);
		clientTable = new JTable();
		clientTableModel = new ClientTableModel(this, this.lanData, playlistColumnNames);
		clientTable.setModel(clientTableModel);
		clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		clientTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO
				if (clientTable.getSelectionModel().isSelectionEmpty()) {
					ratingBox.setEnabled(false);
					btnRate.setEnabled(false);
					btnSkip.setEnabled(false);
				} else {
					ratingBox.setEnabled(true);
					btnRate.setEnabled(true);

					int viewRowIndex = clientTable.getSelectedRow();
					int modelRowIndex = clientTable.convertRowIndexToModel(viewRowIndex);
					MusicData md = (MusicData) clientTableModel.getValueAt(modelRowIndex, 0);
					if (md.getSkip().hasSkipped(MY_IP)) {
						btnSkip.setEnabled(false);
					} else {
						btnSkip.setEnabled(true);
					}

				}

				// if(selectedRow != -1) {
				// selectedRow = e.getFirstIndex();
				// }
				// else {
				// controlPanel.getBtnDeleteTrack().setEnabled(false);
				// }
				// if(playlistTable.getSelectionModel().isSelectionEmpty()) {
				// return;
				// }
				// setDeleteBtnState();
			}
		});

		addRowSorter();

		scrollPane.setViewportView(clientTable);

		for (int i = 0; i < clientTable.getColumnCount(); i++) {
			clientTable.getColumnModel().getColumn(i).setCellRenderer(new PlaylistTableCellRenderer());
		}

		setClientTableColumnSizes();

		btnRefresh = new JButton("Refresh Playlist");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (client == null)
					return;
				client.sendMessage(ClientHandler.MSG_REQ_PROPERTY);
			}
		});
		btnRefresh.setEnabled(false);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridwidth = 2;
		gbc_btnNewButton.insets = new Insets(5, 5, 5, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 0;
		playlistPanel.add(btnRefresh, gbc_btnNewButton);

		btnSkip = new JButton("Skip Request");
		btnSkip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int viewRowIndex = clientTable.getSelectedRow();
				int modelRowIndex = clientTable.convertRowIndexToModel(viewRowIndex);
				MusicData md = (MusicData) clientTableModel.getValueAt(modelRowIndex, 0);
				if (md.getSkip().hasSkipped(MY_IP))
					return;
				String message = ClientHandler.MSG_REQ_SKIP + "=" + LanData.setValue(LanData.POS_TAG, "" + md.getPosition()) + LanData.setValue(LanData.IP_TAG, MY_IP);
				client.sendMessage(message);
			}
		});
		btnSkip.setEnabled(false);
		GridBagConstraints gbc_btnSkip = new GridBagConstraints();
		gbc_btnSkip.gridwidth = 2;
		gbc_btnSkip.anchor = GridBagConstraints.NORTH;
		gbc_btnSkip.insets = new Insets(20, 5, 5, 5);
		gbc_btnSkip.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSkip.gridx = 1;
		gbc_btnSkip.gridy = 1;
		playlistPanel.add(btnSkip, gbc_btnSkip);

		ratingBox = new JComboBox<Integer>();
		ratingBox.setEnabled(false);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(6, 5, 5, 5);
		gbc_comboBox.anchor = GridBagConstraints.NORTH;
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 2;
		playlistPanel.add(ratingBox, gbc_comboBox);
		fillRatingBox();

		btnRate = new JButton("Rate Track");
		btnRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// TODO implement rating
				// client.sendMessage(message);
			}
		});
		btnRate.setEnabled(false);
		GridBagConstraints gbc_btnRate = new GridBagConstraints();
		gbc_btnRate.insets = new Insets(5, 5, 5, 5);
		gbc_btnRate.anchor = GridBagConstraints.NORTH;
		gbc_btnRate.gridx = 2;
		gbc_btnRate.gridy = 2;
		playlistPanel.add(btnRate, gbc_btnRate);
	}

	public void addRowSorter() {
		TableRowSorter<TableModel> playlistSorter = new TableRowSorter<TableModel>(clientTable.getModel()) {
			public boolean isSortable(int column) {
				ClientTableModel ctm = (ClientTableModel) clientTable.getModel();
				if (ctm.isColumnSortable()) {
					return super.isSortable(column);
				} else {
					return false;
				}
			}
		};
		clientTable.setRowSorter(playlistSorter);
		// playlistSorter.toggleSortOrder(0);
		// playlistSorter.setSortsOnUpdates(true);
	}

	private void removeRowSorter() {
		clientTable.setRowSorter(null);
	}

	public void disconnectedState() {
		initLanData();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				client = null;
				txtEnterIpAddress.setEditable(true);
				btnConnect.setText("Connect");
				btnUpload.setEnabled(false);
				btnRefresh.setEnabled(false);
				txtEnterPath.setEditable(false);
				btnRate.setEnabled(false);
				btnSkip.setEnabled(false);
				clientTable.setEnabled(false);
				ratingBox.setEnabled(false);
				clientTableModel.setRowCount(0);
				txtEnterIpAddress.setText("Connection failed or it disconnected");
				uploadBar.setEnabled(false);
				enablePathAndSearch(false);
				removeRowSorter();
			}

		});

	}

	public void connectedState() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				txtEnterIpAddress.setEditable(false);
				btnConnect.setText("Disconnect");
				clientTable.setEnabled(true);
				btnRefresh.setEnabled(true);
				btnUpload.setEnabled(false);
				uploadBar.setEnabled(true);
				enablePathAndSearch(true);
				addRowSorter();
			}

		});

	}

	public void enablePathAndSearch(boolean enable) {
		if (!enable) {
			txtEnterPath.setText("");
		}
		txtEnterPath.setEnabled(enable);
		txtEnterPath.setEditable(enable);
		btnSearch.setEnabled(enable);
	}

	private void connect() {
		try {
			client = new Client(txtEnterIpAddress.getText(), this);

			client.getClientHandler().addObserver(clientTableModel); // adding
																						// observer
																						// here!
			client.sendMessage(ClientHandler.MSG_REQ_PROPERTY);

		} catch (UnknownHostException e) {
			disconnectedState();
			txtEnterIpAddress.setText("Connection failed, try again...");
			txtEnterIpAddress.setEditable(true);
			return;
		} catch (IOException e) {
			disconnectedState();
			txtEnterIpAddress.setText("Connection failed, try again...");
			txtEnterIpAddress.setEditable(true);
			return;
		}
		connectedState();

	}

	protected void connectButtonAction() {
		if (!txtEnterIpAddress.isEditable()) {
			disconnectedState();
		} else if (ipVal.validate(txtEnterIpAddress.getText())) {
			connect();
		}
		// else {
		// txtEnterIpAddress.setText(INIT_IP_TEXT);
		// }
	}

	private void initLanData() {
		if (!DATA_DIR.exists()) {
			DATA_DIR.mkdirs();
		} else {
			File[] files = DATA_DIR.listFiles();
			for (File f : files) {
				f.delete();
			}
		}
		if (!LAN_DATA_FILE.exists()) {
			try {
				LAN_DATA_FILE.createNewFile();
			} catch (Exception e) {
			}
		}
		lanData = new LanData(DATA_DIR, LAN_DATA_FILE, INIT_PARTICIPANTS, false);
	}

	private void setClientTableColumnSizes() {
		clientTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		clientTable.getColumnModel().getColumn(0).setMinWidth(34);

		clientTable.getColumnModel().getColumn(1).setPreferredWidth(250);
		clientTable.getColumnModel().getColumn(1).setMinWidth(38);

		clientTable.getColumnModel().getColumn(2).setPreferredWidth(170);
		clientTable.getColumnModel().getColumn(2).setMinWidth(45);

		clientTable.getColumnModel().getColumn(3).setPreferredWidth(120);
		clientTable.getColumnModel().getColumn(3).setMinWidth(50);

		clientTable.getColumnModel().getColumn(4).setPreferredWidth(47);
		clientTable.getColumnModel().getColumn(4).setMinWidth(47);

		clientTable.getColumnModel().getColumn(5).setPreferredWidth(60);
		clientTable.getColumnModel().getColumn(5).setMinWidth(60);

		clientTable.getColumnModel().getColumn(6).setPreferredWidth(50);
		clientTable.getColumnModel().getColumn(6).setMinWidth(50);

		clientTable.getColumnModel().getColumn(7).setPreferredWidth(49);
		clientTable.getColumnModel().getColumn(7).setMinWidth(49);

		clientTable.getColumnModel().getColumn(8).setPreferredWidth(40);
		clientTable.getColumnModel().getColumn(8).setMinWidth(38);

		clientTable.getColumnModel().getColumn(9).setPreferredWidth(140);
		clientTable.getColumnModel().getColumn(9).setMinWidth(38);

		clientTable.getColumnModel().getColumn(10).setPreferredWidth(100);
		clientTable.getColumnModel().getColumn(10).setMinWidth(65);
	}

	private void fillRatingBox() {
		for (int i = 1; i <= 5; i++) {
			ratingBox.addItem(i);
		}
	}

}
