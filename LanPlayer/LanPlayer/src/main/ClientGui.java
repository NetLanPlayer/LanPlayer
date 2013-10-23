package main;

import utilities.IPAddressValidator;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JSeparator;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;

import client.Client;
import client.ClientHandler;
import client.ClientTableModel;
import lanplayer.LanData;
import lanplayer.MusicData;
import lanplayer.PlaylistPanel;
import lanplayer.PlaylistTableModel;

public class ClientGui extends JFrame {

	private final static int INIT_PARTICIPANTS = 1;
	public final static File DATA_DIR = new File("./ClientData/");
	public final static File LAN_DATA_FILE = new File("./ClientData/LanMusicData.property");
	
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
	private JPanel connectPanel;
	private JPanel uploadPanel;
	private JPanel playlistPanel;
	
	private JTable clientTable;
	private ClientTableModel clientTableModel;
	
	private JScrollPane scrollPane;
	private JButton btnSkip;
	private JComboBox<Integer> comboBox;
	private JButton btnRate;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
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
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		connectPanel = new JPanel();
		connectPanel.setBorder(new TitledBorder(null, "Connection",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_connectPanel = new GridBagConstraints();
		gbc_connectPanel.insets = new Insets(0, 0, 5, 0);
		gbc_connectPanel.fill = GridBagConstraints.BOTH;
		gbc_connectPanel.gridx = 0;
		gbc_connectPanel.gridy = 0;
		contentPane.add(connectPanel, gbc_connectPanel);
		GridBagLayout gbl_connectPanel = new GridBagLayout();
		gbl_connectPanel.columnWidths = new int[] { 0, 165, 0 };
		gbl_connectPanel.rowHeights = new int[] { 0, 0 };
		gbl_connectPanel.columnWeights = new double[] { 1.0, 0.0,
				Double.MIN_VALUE };
		gbl_connectPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		connectPanel.setLayout(gbl_connectPanel);

		txtEnterIpAddress = new JTextField();
		GridBagConstraints gbc_txtEnterIpAddress = new GridBagConstraints();
		gbc_txtEnterIpAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnterIpAddress.insets = new Insets(5, 5, 5, 5);
		gbc_txtEnterIpAddress.gridx = 0;
		gbc_txtEnterIpAddress.gridy = 0;
		connectPanel.add(txtEnterIpAddress, gbc_txtEnterIpAddress);
		txtEnterIpAddress.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				txtEnterIpAddress.select(0, txtEnterIpAddress.getText().length());
			}
		});
		txtEnterIpAddress.setText("Enter ServerIP");
		txtEnterIpAddress.setSelectionStart(0);
		txtEnterIpAddress.setColumns(10);

		final JButton btnConnect = new JButton("Connect");
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnConnect.insets = new Insets(5, 5, 5, 5);
		gbc_btnConnect.gridx = 1;
		gbc_btnConnect.gridy = 0;
		connectPanel.add(btnConnect, gbc_btnConnect);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!txtEnterIpAddress.isEditable()) {
					client = null;
					txtEnterIpAddress.setEditable(true);
					txtEnterIpAddress.setText("");
					btnConnect.setText("Connect");
					btnUpload.setEnabled(false);
					txtEnterPath.setEditable(false);
					btnRate.setEnabled(false);
					btnSkip.setEnabled(false);
					clientTable.setEnabled(false);
					comboBox.setEnabled(false);
					clientTableModel.setRowCount(0);
					initLanData();
				} else if (ipVal.validate(txtEnterIpAddress.getText())) {
					try {
						client = new Client(txtEnterIpAddress.getText());
						
						client.getClientHandler().addObserver(clientTableModel); // adding observer here
						
						client.sendMessage(ClientHandler.MSG_REQ_PROPERTY);
						
					} catch (UnknownHostException e) {
						txtEnterIpAddress.setText("Connection failed, try again...");
						txtEnterIpAddress.setEditable(true);
						return;
					} catch (java.net.ConnectException e) {
						txtEnterIpAddress
								.setText("Connection failed, try again...");
						txtEnterIpAddress.setEditable(true);
						return;

					} catch (IOException e) {
						txtEnterIpAddress
								.setText("Connection failed, try again...");
						txtEnterIpAddress.setEditable(true);
						return;
					}
					txtEnterIpAddress.setEditable(false);
					btnConnect.setText("Disconnect");
					clientTable.setEnabled(true);
					comboBox.setEnabled(true);
					btnUpload.setEnabled(true);
					txtEnterPath.setEditable(true);
					btnRate.setEnabled(true);
					btnSkip.setEnabled(true);
				} else {
					txtEnterIpAddress.setText("");
				}

			}
		});

		uploadPanel = new JPanel();
		uploadPanel.setBorder(new TitledBorder(null, "Music File Upload",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_uploadPanel = new GridBagConstraints();
		gbc_uploadPanel.insets = new Insets(0, 0, 5, 0);
		gbc_uploadPanel.fill = GridBagConstraints.BOTH;
		gbc_uploadPanel.gridx = 0;
		gbc_uploadPanel.gridy = 1;
		contentPane.add(uploadPanel, gbc_uploadPanel);
		GridBagLayout gbl_uploadPanel = new GridBagLayout();
		gbl_uploadPanel.columnWidths = new int[] { 0, 165, 0 };
		gbl_uploadPanel.rowHeights = new int[] { 0, 0 };
		gbl_uploadPanel.columnWeights = new double[] { 1.0, 0.0,
				Double.MIN_VALUE };
		gbl_uploadPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		uploadPanel.setLayout(gbl_uploadPanel);

		txtEnterPath = new JTextField();
		GridBagConstraints gbc_txtEnterPath = new GridBagConstraints();
		gbc_txtEnterPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnterPath.insets = new Insets(5, 5, 5, 5);
		gbc_txtEnterPath.gridx = 0;
		gbc_txtEnterPath.gridy = 0;
		uploadPanel.add(txtEnterPath, gbc_txtEnterPath);
		txtEnterPath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				txtEnterPath.select(0, txtEnterPath.getText().length());
			}
		});
		txtEnterPath.setText("Connect to Server");
		txtEnterPath.setEditable(false);
		txtEnterPath.setColumns(10);

		btnUpload = new JButton("Upload");
		GridBagConstraints gbc_btnUpload = new GridBagConstraints();
		gbc_btnUpload.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUpload.insets = new Insets(5, 5, 5, 5);
		gbc_btnUpload.gridx = 1;
		gbc_btnUpload.gridy = 0;
		uploadPanel.add(btnUpload, gbc_btnUpload);
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (client.checkPathAndSend(txtEnterPath.getText()))
					txtEnterPath.setText("");
				else
					txtEnterPath.setText("Path was wrong");
			}
		});
		btnUpload.setEnabled(false);

		playlistPanel = new JPanel();
		playlistPanel.setBorder(new TitledBorder(null, "Playlist",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_playlistPanel = new GridBagConstraints();
		gbc_playlistPanel.fill = GridBagConstraints.BOTH;
		gbc_playlistPanel.gridx = 0;
		gbc_playlistPanel.gridy = 2;
		contentPane.add(playlistPanel, gbc_playlistPanel);
		GridBagLayout gbl_playlistPanel = new GridBagLayout();
		gbl_playlistPanel.columnWidths = new int[] { 0, 60, 90, 0 };
		gbl_playlistPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_playlistPanel.columnWeights = new double[] { 1.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_playlistPanel.rowWeights = new double[] { 0.0, 1.0,
				Double.MIN_VALUE };
		playlistPanel.setLayout(gbl_playlistPanel);

		scrollPane = new JScrollPane();
		scrollPane.setEnabled(false);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		playlistPanel.add(scrollPane, gbc_scrollPane);

		String[] playlistColumnNames = { "Pos", "Title", "Artist", "Album",
				"Track", "Duration", "Played", "Rating", "Skip", "Date",
				"Uploader" };
		clientTable = new JTable();
		clientTableModel = new ClientTableModel(this, this.lanData , playlistColumnNames);
		clientTable.setModel(clientTableModel);
				
		setClientTableColumnSizes();
		
		scrollPane.setViewportView(clientTable);

		btnSkip = new JButton("Skip Request");
		btnSkip.setEnabled(false);
		GridBagConstraints gbc_btnSkip = new GridBagConstraints();
		gbc_btnSkip.gridwidth = 2;
		gbc_btnSkip.anchor = GridBagConstraints.NORTH;
		gbc_btnSkip.insets = new Insets(5, 5, 5, 5);
		gbc_btnSkip.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSkip.gridx = 1;
		gbc_btnSkip.gridy = 0;
		playlistPanel.add(btnSkip, gbc_btnSkip);

		comboBox = new JComboBox<Integer>();
		comboBox.setEnabled(false);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(7, 5, 5, 5);
		gbc_comboBox.anchor = GridBagConstraints.NORTH;
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		playlistPanel.add(comboBox, gbc_comboBox);

		btnRate = new JButton("Rate Track");
		btnRate.setEnabled(false);
		GridBagConstraints gbc_btnRate = new GridBagConstraints();
		gbc_btnRate.insets = new Insets(5, 5, 5, 5);
		gbc_btnRate.anchor = GridBagConstraints.NORTH;
		gbc_btnRate.gridx = 2;
		gbc_btnRate.gridy = 1;
		playlistPanel.add(btnRate, gbc_btnRate);
	}
	
	private void initLanData() {
		if(!DATA_DIR.exists()) {
			DATA_DIR.mkdir();
		}
		else {
			File[] files = DATA_DIR.listFiles();
			for(File f : files) {
				f.delete();
			}
		}
		if(!LAN_DATA_FILE.exists()) {
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
	
}
