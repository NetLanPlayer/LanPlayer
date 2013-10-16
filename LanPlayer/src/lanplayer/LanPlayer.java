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

public class LanPlayer extends JFrame {
	public LanPlayer() {
		setTitle("Lan Player");
		
		try {
			Helpers.registerAllClasses(); // essential
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		setMinimumSize(new Dimension(1000, 480));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 100, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		PlaylistPanel playlistPanel = new PlaylistPanel();
		GridBagConstraints gbc_playlistPanel = new GridBagConstraints();
		gbc_playlistPanel.insets = new Insets(0, 0, 0, 5);
		gbc_playlistPanel.fill = GridBagConstraints.BOTH;
		gbc_playlistPanel.gridx = 1;
		gbc_playlistPanel.gridy = 0;
		getContentPane().add(playlistPanel, gbc_playlistPanel);

		final PlayerPanel playerPanel = new PlayerPanel(playlistPanel);
		GridBagConstraints gbc_playerPanel = new GridBagConstraints();
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

	/**
	 * Launch the application.
	 */
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
					LanPlayer window = new LanPlayer();
					window.setLocationRelativeTo(null); // appear in middle of
														// screen
					window.setVisible(true);
					Helpers.setCoding(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
