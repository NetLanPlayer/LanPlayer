package main;

import java.awt.EventQueue;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.BindException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import server.Server;
import lanplayer.MainPanel;
import lanplayer.PlaylistPanel;
import de.quippy.javamod.system.Helpers;

public class ServerGui {
	
	public final static int INIT_PARTICIPANTS = 1;
	public final static File  LAN_PLAYER_INIT = new File("./src/antipasta.xm");
	public final static String MUSIC_DIR_PATH = "./ServerData/Music/";
	public final static File DATA_DIR = new File(MUSIC_DIR_PATH);
	public final static File LAN_DATA_FILE = new File("./ServerData/LanMusicData.property");
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() { 
			 public void uncaughtException(Thread thread, final Throwable throwable) {
				 System.out.println("Error in thread " + thread + ": " + throwable.getMessage());
				 throwable.printStackTrace();
			 }
		 });
		
		final Server server = new Server(MUSIC_DIR_PATH);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
	

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainPanel window = new MainPanel(server);
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
