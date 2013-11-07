package main;

import java.awt.EventQueue;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import server.Server;
import lanplayer.MainPanel;
import de.quippy.javamod.system.Helpers;

public class ServerGui {
	
	public final static int INIT_PARTICIPANTS = 1;
	public final static File LAN_PLAYER_INIT = new File("./ServerData/lanplayer.xm");
	public final static String MUSIC_DIR_PATH = "./ServerData/Music/";
	public final static File MUSIC_DIR = new File(MUSIC_DIR_PATH);
	public final static File LAN_DATA_FILE = new File("./ServerData/LanMusicData.property");
	
	private final static ImageIcon serverIcon = new ImageIcon("./ServerData/PlayerServer.png");
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() { 
			 public void uncaughtException(Thread thread, final Throwable throwable) {
				 //System.out.println("Error in thread " + thread + ": " + throwable.getMessage());
				 //throwable.printStackTrace();
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
					window.setLocationRelativeTo(null); // appear in middle of screen
					window.setVisible(true);
					window.setIconImage(serverIcon.getImage());
					Helpers.setCoding(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
