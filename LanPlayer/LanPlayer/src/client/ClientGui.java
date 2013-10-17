package client;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JSeparator;
import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.swing.JSlider;
import javax.swing.JLabel;

public class ClientGui extends JFrame {

	private static final long serialVersionUID = 3886409992076543386L;
	private JPanel contentPane;

	private JTextField txtEnterIpAddress;
	private JTextField txtEnterPath;

	private JSeparator separator_underIpInput;

	private TrackSender sender;
	private IPAddressValidator ipVal = new IPAddressValidator();

	private JButton btnUpload;
	private JPanel panel;
	private JLabel ratingMaxLabel;
	private JLabel ratingMinLabel;
	private JSlider ratingSlider;
	private JButton btnRate;
	private JSeparator separator_underPathInput;
	private JSeparator separator;
	private JButton btnSkip;
	private JLabel lblRatingCounter;

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

	/**
	 * Create the frame.
	 */
	public ClientGui() {
		setMinimumSize(new Dimension(490,163));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 490, 163);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 480, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 0.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		txtEnterIpAddress = new JTextField();
		txtEnterIpAddress.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				txtEnterIpAddress.select(0, txtEnterIpAddress.getText()
						.length());
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

		});
		txtEnterIpAddress.setText("Enter ServerIP");
		txtEnterIpAddress.setSelectionStart(0);
		GridBagConstraints gbc_txtEnterIpAddress = new GridBagConstraints();
		gbc_txtEnterIpAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnterIpAddress.insets = new Insets(0, 0, 5, 5);
		gbc_txtEnterIpAddress.anchor = GridBagConstraints.NORTH;
		gbc_txtEnterIpAddress.gridx = 0;
		gbc_txtEnterIpAddress.gridy = 0;
		contentPane.add(txtEnterIpAddress, gbc_txtEnterIpAddress);
		txtEnterIpAddress.setColumns(10);

		final JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!txtEnterIpAddress.isEditable()) {
					sender = null;
					txtEnterIpAddress.setEditable(true);
					txtEnterIpAddress.setText("");
					btnConnect.setText("Connect");
					btnUpload.setEnabled(false);
					txtEnterPath.setEditable(false);
				} else if (ipVal.validate(txtEnterIpAddress.getText())) {
					try {
						sender = new TrackSender(txtEnterIpAddress.getText());
					} catch (UnknownHostException e) {
						txtEnterIpAddress
								.setText("Connection failed, try again...");
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
					btnUpload.setEnabled(true);
					txtEnterPath.setEditable(true);
					btnRate.setEnabled(true);
					btnSkip.setEnabled(true);
				} else {
					txtEnterIpAddress.setText("");
				}

			}
		});
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.insets = new Insets(0, 0, 5, 0);
		gbc_btnConnect.gridx = 1;
		gbc_btnConnect.gridy = 0;
		contentPane.add(btnConnect, gbc_btnConnect);

		separator_underIpInput = new JSeparator();
		separator_underIpInput.setForeground(Color.BLACK);
		GridBagConstraints gbc_separator_underPathInput = new GridBagConstraints();
		gbc_separator_underPathInput.gridwidth = 2;
		gbc_separator_underPathInput.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator_underPathInput.insets = new Insets(0, 0, 5, 0);
		gbc_separator_underPathInput.gridx = 0;
		gbc_separator_underPathInput.gridy = 1;
		contentPane.add(separator_underIpInput, gbc_separator_underPathInput);

		txtEnterPath = new JTextField();
		txtEnterPath.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				txtEnterPath.select(0, txtEnterPath.getText().length());
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

		});
		txtEnterPath.setText("Connect to Server");
		GridBagConstraints gbc_txtEnterPaht = new GridBagConstraints();
		gbc_txtEnterPaht.insets = new Insets(0, 0, 5, 5);
		gbc_txtEnterPaht.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnterPaht.gridx = 0;
		gbc_txtEnterPaht.gridy = 2;
		txtEnterPath.setEditable(false);
		contentPane.add(txtEnterPath, gbc_txtEnterPaht);
		txtEnterPath.setColumns(10);

		btnUpload = new JButton("Upload");
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (sender.checkPathAndSend(txtEnterPath.getText()))
					txtEnterPath.setText("");
				else
					txtEnterPath.setText("Path was wrong");
			}
		});
		btnUpload.setEnabled(false);
		GridBagConstraints gbc_btnUpload = new GridBagConstraints();
		gbc_btnUpload.insets = new Insets(0, 0, 5, 0);
		gbc_btnUpload.gridx = 1;
		gbc_btnUpload.gridy = 2;
		contentPane.add(btnUpload, gbc_btnUpload);

		separator_underPathInput = new JSeparator();
		separator_underPathInput.setForeground(Color.BLACK);

		GridBagConstraints gbc_separator_underPathInput_1 = new GridBagConstraints();
		gbc_separator_underPathInput_1.gridwidth = 2;
		gbc_separator_underPathInput_1.insets = new Insets(0, 0, 5, 0);
		gbc_separator_underPathInput_1.gridx = 0;
		gbc_separator_underPathInput_1.gridy = 3;
		gbc_separator_underPathInput_1.fill = GridBagConstraints.HORIZONTAL;

		contentPane.add(separator_underPathInput,
				gbc_separator_underPathInput_1);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		ratingMinLabel = new JLabel("1");
		GridBagConstraints gbc_ratingMinLabel = new GridBagConstraints();
		gbc_ratingMinLabel.insets = new Insets(0, 0, 0, 5);
		gbc_ratingMinLabel.gridx = 0;
		gbc_ratingMinLabel.gridy = 0;
		panel.add(ratingMinLabel, gbc_ratingMinLabel);

		ratingSlider = new JSlider();
		ratingSlider.setValue(1);
		ratingSlider.setMinimum(1);
		ratingSlider.setMaximum(5);
		GridBagConstraints gbc_ratingSlider = new GridBagConstraints();
		gbc_ratingSlider.insets = new Insets(0, 0, 0, 5);
		gbc_ratingSlider.gridx = 1;
		gbc_ratingSlider.gridy = 0;
		panel.add(ratingSlider, gbc_ratingSlider);

		ratingMaxLabel = new JLabel("5");
		GridBagConstraints gbc_ratingMaxLabel = new GridBagConstraints();
		gbc_ratingMaxLabel.gridx = 2;
		gbc_ratingMaxLabel.gridy = 0;
		panel.add(ratingMaxLabel, gbc_ratingMaxLabel);

		btnRate = new JButton("Rate");
		btnRate.setEnabled(false);
		btnRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sender.sendMessage("Rate - " + ratingSlider.getValue());
				btnRate.setEnabled(false);
			}
		});
		GridBagConstraints gbc_btnRate = new GridBagConstraints();
		gbc_btnRate.insets = new Insets(0, 0, 5, 0);
		gbc_btnRate.gridx = 1;
		gbc_btnRate.gridy = 4;
		contentPane.add(btnRate, gbc_btnRate);
		
		separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 2;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 5;
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;

		contentPane.add(separator, gbc_separator);
		
		btnSkip = new JButton("Skip");
		btnSkip.setEnabled(false);
		btnSkip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sender.sendMessage("SKIP");
				btnSkip.setEnabled(false);
			}
		});
		
		lblRatingCounter = new JLabel("5/15");
		GridBagConstraints gbc_lblRatingCounter = new GridBagConstraints();
		gbc_lblRatingCounter.anchor = GridBagConstraints.EAST;
		gbc_lblRatingCounter.insets = new Insets(0, 0, 0, 5);
		gbc_lblRatingCounter.gridx = 0;
		gbc_lblRatingCounter.gridy = 6;
		contentPane.add(lblRatingCounter, gbc_lblRatingCounter);
		GridBagConstraints gbc_btnSkip = new GridBagConstraints();
		gbc_btnSkip.gridx = 1;
		gbc_btnSkip.gridy = 6;
		contentPane.add(btnSkip, gbc_btnSkip);
	}

}
