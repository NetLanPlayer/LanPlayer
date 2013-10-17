package main;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lanplayer.MainPanel;
import de.quippy.javamod.system.Helpers;

public class ServerGui {
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
					MainPanel window = new MainPanel();
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
