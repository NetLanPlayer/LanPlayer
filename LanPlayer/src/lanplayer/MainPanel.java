package lanplayer;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import de.quippy.javamod.system.Helpers;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class MainPanel extends JFrame {

	public MainPanel() {
		setTitle("Lan Player");
		
		try {
			Helpers.registerAllClasses(); // essential
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		setMinimumSize(new Dimension(1200, 600));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 100, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);
		
		ControlPanel controlPanel = new ControlPanel();
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.insets = new Insets(0, 0, 5, 0);
		gbc_controlPanel.fill = GridBagConstraints.BOTH;
		gbc_controlPanel.gridx = 1;
		gbc_controlPanel.gridy = 0;
		getContentPane().add(controlPanel, gbc_controlPanel);
		
		PlaylistPanel playlistPanel = new PlaylistPanel(controlPanel);
		GridBagConstraints gbc_playlistPanel = new GridBagConstraints();
		gbc_playlistPanel.fill = GridBagConstraints.BOTH;
		gbc_playlistPanel.gridx = 1;
		gbc_playlistPanel.gridy = 1;
		getContentPane().add(playlistPanel, gbc_playlistPanel);
		
		final PlayerPanel playerPanel = new PlayerPanel(playlistPanel);
		GridBagConstraints gbc_playerPanel = new GridBagConstraints();
		gbc_playerPanel.gridheight = 2;
		gbc_playerPanel.insets = new Insets(0, 0, 0, 5);
		gbc_playerPanel.fill = GridBagConstraints.BOTH;
		gbc_playerPanel.gridx = 0;
		gbc_playerPanel.gridy = 0;
		getContentPane().add(playerPanel, gbc_playerPanel);
				
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				playerPanel.doClose();
				dispose();
				System.exit(0);
			}
		});
	}
	
}
