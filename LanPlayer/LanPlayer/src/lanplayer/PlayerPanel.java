package lanplayer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.quippy.javamod.io.GaplessSoundOutputStreamImpl;
import de.quippy.javamod.io.SoundOutputStream;
import de.quippy.javamod.main.gui.PlayThread;
import de.quippy.javamod.main.gui.PlayThreadEventListener;
import de.quippy.javamod.main.gui.components.RoundSlider;
import de.quippy.javamod.main.gui.components.SAMeterPanel;
import de.quippy.javamod.main.gui.components.SeekBarPanel;
import de.quippy.javamod.main.gui.components.SeekBarPanelListener;
import de.quippy.javamod.main.gui.components.VUMeterPanel;
import de.quippy.javamod.main.gui.playlist.PlaylistGUIChangeListener;
import de.quippy.javamod.main.playlist.PlayList;
import de.quippy.javamod.main.playlist.PlayListEntry;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.mixer.dsp.AudioProcessor;
import de.quippy.javamod.mixer.dsp.DspProcessorCallBack;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerEvent;
import de.quippy.javamod.multimedia.MultimediaContainerEventListener;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mod.ModContainer;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

import javax.swing.JSlider;
import javax.swing.JScrollBar;

public class PlayerPanel extends JPanel implements DspProcessorCallBack, PlayThreadEventListener, MultimediaContainerEventListener {
	
	private PlaylistPanel playlistPanel;
	
	public static final String BUTTONPLAY_INACTIVE = "/de/quippy/javamod/main/gui/ressources/play.gif";
	public static final String BUTTONPLAY_ACTIVE = "/de/quippy/javamod/main/gui/ressources/play_aktiv.gif";
	public static final String BUTTONPLAY_NORMAL = "/de/quippy/javamod/main/gui/ressources/play_normal.gif";
	public static final String BUTTONPAUSE_INACTIVE = "/de/quippy/javamod/main/gui/ressources/pause.gif";
	public static final String BUTTONPAUSE_ACTIVE = "/de/quippy/javamod/main/gui/ressources/pause_aktiv.gif";
	public static final String BUTTONPAUSE_NORMAL = "/de/quippy/javamod/main/gui/ressources/pause_normal.gif";
	public static final String BUTTONSTOP_INACTIVE = "/de/quippy/javamod/main/gui/ressources/stop.gif";
	public static final String BUTTONSTOP_ACTIVE = "/de/quippy/javamod/main/gui/ressources/stop_aktiv.gif";
	public static final String BUTTONSTOP_NORMAL = "/de/quippy/javamod/main/gui/ressources/stop_normal.gif";
	public static final String BUTTONPREV_INACTIVE = "/de/quippy/javamod/main/gui/ressources/prev.gif";
	public static final String BUTTONPREV_ACTIVE = "/de/quippy/javamod/main/gui/ressources/prev_aktiv.gif";
	public static final String BUTTONPREV_NORMAL = "/de/quippy/javamod/main/gui/ressources/prev_normal.gif";
	public static final String BUTTONNEXT_INACTIVE = "/de/quippy/javamod/main/gui/ressources/next.gif";
	public static final String BUTTONNEXT_ACTIVE = "/de/quippy/javamod/main/gui/ressources/next_aktiv.gif";
	public static final String BUTTONNEXT_NORMAL = "/de/quippy/javamod/main/gui/ressources/next_normal.gif";

	private javax.swing.ImageIcon buttonPlay_Active = null;
	private javax.swing.ImageIcon buttonPlay_Inactive = null;
	private javax.swing.ImageIcon buttonPlay_normal = null;
	private javax.swing.ImageIcon buttonPause_Active = null;
	private javax.swing.ImageIcon buttonPause_Inactive = null;
	private javax.swing.ImageIcon buttonPause_normal = null;
	private javax.swing.ImageIcon buttonStop_Active = null;
	private javax.swing.ImageIcon buttonStop_Inactive = null;
	private javax.swing.ImageIcon buttonStop_normal = null;
	private javax.swing.ImageIcon buttonPrev_Active = null;
	private javax.swing.ImageIcon buttonPrev_Inactive = null;
	private javax.swing.ImageIcon buttonPrev_normal = null;
	private javax.swing.ImageIcon buttonNext_Active = null;
	private javax.swing.ImageIcon buttonNext_Inactive = null;
	private javax.swing.ImageIcon buttonNext_normal = null;

	private javax.swing.JButton button_Play = null;
	private javax.swing.JButton button_Pause = null;
	private javax.swing.JButton button_Stop = null;
	private javax.swing.JButton button_Prev = null;
	private javax.swing.JButton button_Next = null;

	private RoundSlider roundVolumeSlider = null;
	private javax.swing.JLabel volumeLabel = null;
	private RoundSlider roundBalanceSlider = null;
	private javax.swing.JLabel balanceLabel = null;

	private SeekBarPanel seekBarPanel = null;

	private String searchPath;

	private LEDScrollPanel ledScrollPanel = null;

	private VUMeterPanel vuLMeterPanel = null;
	private VUMeterPanel vuRMeterPanel = null;
	private SAMeterPanel saLMeterPanel = null;
	private SAMeterPanel saRMeterPanel = null;

	private MultimediaContainer currentContainer;
	private PlayThread playerThread;
	private PlayList currentPlayList;

	public PlayList getCurrentPlayList() {
		return currentPlayList;
	}

	private AudioProcessor audioProcessor;
	private transient SoundOutputStream soundOutputStream;

	private float currentVolume = 1f; /* 0.0 - 1.0 */
	private float currentBalance = 0; /* -1.0 - 1.0 */
	private JSlider volumeSlider;
	private JSlider balanceSlider;

	public PlayerPanel(PlaylistPanel playlistPanel) {
		
		this.playlistPanel = playlistPanel;
		this.playlistPanel.setPlayerPanel(this);
		
	    audioProcessor = new AudioProcessor(2048, 70);
	    audioProcessor.addListener(this);
		initialize();
		Properties props = new Properties();
		props.setProperty(ModContainer.PROPERTY_PLAYER_ISP, "3");
		props.setProperty(ModContainer.PROPERTY_PLAYER_STEREO, "2");
		props.setProperty(ModContainer.PROPERTY_PLAYER_WIDESTEREOMIX, "TRUE");
		props.setProperty(ModContainer.PROPERTY_PLAYER_NOISEREDUCTION, "TRUE");
		props.setProperty(ModContainer.PROPERTY_PLAYER_NOLOOPS, "0");
		props.setProperty(ModContainer.PROPERTY_PLAYER_MEGABASS, "TRUE");
		props.setProperty(ModContainer.PROPERTY_PLAYER_BITSPERSAMPLE, "16");
		props.setProperty(ModContainer.PROPERTY_PLAYER_FREQUENCY, "48000");
		MultimediaContainerManager.configureContainer(props);
		MultimediaContainerManager.addMultimediaContainerEventListener(this);
		
		reloadPlaylist();
	}

	public void reloadPlaylist() {
		List<MusicData> filesToPlay = playlistPanel.getPlaylist();
		//System.out.println("PlayList Player size: " + playlistPanel.getPlaylist().size());
		if(filesToPlay.isEmpty()) {
			File[] files = { PlaylistPanel.LAN_PLAYER_INIT };
			doOpenFile(files);
		}
		else {
			File[] files = new File[filesToPlay.size()];
			for(int i = 0; i < filesToPlay.size(); i++) {
				files[i] = filesToPlay.get(i).getMusicFile();
			}
			doOpenFile(files);
		}

	}
	
	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 45, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JPanel namePanel = new JPanel();
		namePanel.setBorder(new TitledBorder(null, "Name",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_namePanel = new GridBagConstraints();
		gbc_namePanel.insets = new Insets(5, 5, 5, 5);
		gbc_namePanel.fill = GridBagConstraints.BOTH;
		gbc_namePanel.gridx = 0;
		gbc_namePanel.gridy = 0;
		add(namePanel, gbc_namePanel);
		
		//namePanel.add(getLEDScrollPanel(), Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.CENTER, 0.0, 0.0));
		namePanel.add(getLEDScrollPanel(), gbc_namePanel);
		GridBagLayout gbl_namePanel = new GridBagLayout();
		gbl_namePanel.columnWidths = new int[]{0};
		gbl_namePanel.rowHeights = new int[]{0};
		gbl_namePanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_namePanel.rowWeights = new double[]{Double.MIN_VALUE};
		namePanel.setLayout(gbl_namePanel);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new TitledBorder(null, "Player Data",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_dataPanel = new GridBagConstraints();
		gbc_dataPanel.insets = new Insets(5, 5, 5, 5);
		gbc_dataPanel.fill = GridBagConstraints.BOTH;
		gbc_dataPanel.gridx = 0;
		gbc_dataPanel.gridy = 1;
		add(dataPanel, gbc_dataPanel);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{55, 145, 145, 55, 0};
		gbl_dataPanel.rowHeights = new int[]{100, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		GridBagConstraints gbc_vuLMeterPanel = new GridBagConstraints();
		gbc_vuLMeterPanel.fill = GridBagConstraints.BOTH;
		gbc_vuLMeterPanel.insets = new Insets(5, 5, 5, 5);
		gbc_vuLMeterPanel.gridx = 0;
		gbc_vuLMeterPanel.gridy = 0;
		dataPanel.add(getVULMeterPanel(), gbc_vuLMeterPanel);
		GridBagConstraints gbc_saLMeterPanel = new GridBagConstraints();
		gbc_saLMeterPanel.fill = GridBagConstraints.BOTH;
		gbc_saLMeterPanel.insets = new Insets(5, 5, 5, 5);
		gbc_saLMeterPanel.gridx = 1;
		gbc_saLMeterPanel.gridy = 0;
		dataPanel.add(getSALMeterPanel(), gbc_saLMeterPanel);
		GridBagConstraints gbc_saRMeterPanel = new GridBagConstraints();
		gbc_saRMeterPanel.fill = GridBagConstraints.BOTH;
		gbc_saRMeterPanel.insets = new Insets(5, 5, 5, 5);
		gbc_saRMeterPanel.gridx = 2;
		gbc_saRMeterPanel.gridy = 0;
		dataPanel.add(getSARMeterPanel(), gbc_saRMeterPanel);
		GridBagConstraints gbc_vuRMeterPanel = new GridBagConstraints();
		gbc_vuRMeterPanel.insets = new Insets(5, 5, 5, 5);
		gbc_vuRMeterPanel.fill = GridBagConstraints.BOTH;
		gbc_vuRMeterPanel.gridx = 3;
		gbc_vuRMeterPanel.gridy = 0;
		dataPanel.add(getVURMeterPanel(), gbc_vuRMeterPanel);

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Player Control",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.insets = new Insets(5, 5, 5, 5);
		gbc_controlPanel.fill = GridBagConstraints.BOTH;
		gbc_controlPanel.gridx = 0;
		gbc_controlPanel.gridy = 2;
		add(controlPanel, gbc_controlPanel);
		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWidths = new int[] { 80, 80, 80, 80, 80, 0 };
		gbl_controlPanel.rowHeights = new int[] { 45, 45, 45, 70, 0 };
		gbl_controlPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		gbl_controlPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		controlPanel.setLayout(gbl_controlPanel);

		GridBagConstraints gbc_button_Prev = new GridBagConstraints();
		gbc_button_Prev.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_Prev.anchor = GridBagConstraints.NORTH;
		gbc_button_Prev.insets = new Insets(5, 5, 5, 5);
		gbc_button_Prev.gridx = 0;
		gbc_button_Prev.gridy = 0;
		controlPanel.add(getButton_Prev(), gbc_button_Prev);
		GridBagConstraints gbc_button_Play = new GridBagConstraints();
		gbc_button_Play.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_Play.anchor = GridBagConstraints.NORTH;
		gbc_button_Play.insets = new Insets(5, 5, 5, 5);
		gbc_button_Play.gridx = 1;
		gbc_button_Play.gridy = 0;
		controlPanel.add(getButton_Play(), gbc_button_Play);
		GridBagConstraints gbc_button_Next = new GridBagConstraints();
		gbc_button_Next.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_Next.anchor = GridBagConstraints.NORTH;
		gbc_button_Next.insets = new Insets(5, 5, 5, 5);
		gbc_button_Next.gridx = 2;
		gbc_button_Next.gridy = 0;
		controlPanel.add(getButton_Next(), gbc_button_Next);
		GridBagConstraints gbc_button_Pause = new GridBagConstraints();
		gbc_button_Pause.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_Pause.anchor = GridBagConstraints.NORTH;
		gbc_button_Pause.insets = new Insets(5, 5, 5, 5);
		gbc_button_Pause.gridx = 3;
		gbc_button_Pause.gridy = 0;
		controlPanel.add(getButton_Pause(), gbc_button_Pause);
		GridBagConstraints gbc_button_Stop = new GridBagConstraints();
		gbc_button_Stop.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_Stop.anchor = GridBagConstraints.NORTH;
		gbc_button_Stop.insets = new Insets(5, 5, 5, 5);
		gbc_button_Stop.gridx = 4;
		gbc_button_Stop.gridy = 0;
		controlPanel.add(getButton_Stop(), gbc_button_Stop);
		GridBagConstraints gbc_volumeLabel = new GridBagConstraints();
		gbc_volumeLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_volumeLabel.insets = new Insets(5, 5, 5, 5);
		gbc_volumeLabel.gridx = 0;
		gbc_volumeLabel.gridy = 1;
		controlPanel.add(getVolumeLabel(), gbc_volumeLabel);
		GridBagConstraints gbc_sliderVolume = new GridBagConstraints();
		gbc_sliderVolume.fill = GridBagConstraints.HORIZONTAL;
		gbc_sliderVolume.gridwidth = 3;
		gbc_sliderVolume.insets = new Insets(0, 0, 5, 5);
		gbc_sliderVolume.gridx = 1;
		gbc_sliderVolume.gridy = 1;
		controlPanel.add(getSliderVolume(), gbc_sliderVolume);
		GridBagConstraints gbc_volumeSlider = new GridBagConstraints();
		gbc_volumeSlider.insets = new Insets(5, 5, 5, 5);
		gbc_volumeSlider.gridx = 4;
		gbc_volumeSlider.gridy = 1;
		controlPanel.add(getVolumeSlider(), gbc_volumeSlider);
		GridBagConstraints gbc_balanceLabel = new GridBagConstraints();
		gbc_balanceLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_balanceLabel.insets = new Insets(5, 5, 5, 5);
		gbc_balanceLabel.gridx = 0;
		gbc_balanceLabel.gridy = 2;
		controlPanel.add(getBalanceLabel(), gbc_balanceLabel);
		GridBagConstraints gbc_sliderBalance = new GridBagConstraints();
		gbc_sliderBalance.fill = GridBagConstraints.HORIZONTAL;
		gbc_sliderBalance.gridwidth = 3;
		gbc_sliderBalance.insets = new Insets(0, 0, 5, 5);
		gbc_sliderBalance.gridx = 1;
		gbc_sliderBalance.gridy = 2;
		controlPanel.add(getSliderBalance(), gbc_sliderBalance);
		GridBagConstraints gbc_balanceSlider = new GridBagConstraints();
		gbc_balanceSlider.insets = new Insets(5, 5, 5, 5);
		gbc_balanceSlider.gridx = 4;
		gbc_balanceSlider.gridy = 2;
		controlPanel.add(getBalanceSlider(), gbc_balanceSlider);
		GridBagConstraints gbc_seekBarPanel = new GridBagConstraints();
		gbc_seekBarPanel.insets = new Insets(10, 5, 5, 5);
		gbc_seekBarPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_seekBarPanel.anchor = GridBagConstraints.NORTH;
		gbc_seekBarPanel.gridwidth = 5;
		gbc_seekBarPanel.gridx = 0;
		gbc_seekBarPanel.gridy = 3;
		controlPanel.add(getSeekBarPanel(), gbc_seekBarPanel);
	}

	public void doClose() {
		doStopPlaying();
		getSeekBarPanel().pauseThread();
		getVULMeterPanel().pauseThread();
		getVURMeterPanel().pauseThread();
		getSALMeterPanel().pauseThread();
		getSARMeterPanel().pauseThread();
		getLEDScrollPanel().pauseThread();
		if (audioProcessor!=null) audioProcessor.removeListener(this);
		MultimediaContainerManager.removeMultimediaContainerEventListener(this);
	}

	public LEDScrollPanel getLEDScrollPanel() {
		final int chars = 21; // show 21 chars
		final int brick = 3; // one brick is 3x3 pixel
		if (ledScrollPanel == null) {
			ledScrollPanel = new LEDScrollPanel(30, "Lan Player"
					+ "                  ", chars, Color.GREEN, Color.BLACK);
			Dimension d = new Dimension((chars * brick * 6) + 11,
					(brick * 8) + 11);
			ledScrollPanel.setSize(d);
			ledScrollPanel.setMaximumSize(d);
			ledScrollPanel.setMinimumSize(d);
			ledScrollPanel.setPreferredSize(d);
			ledScrollPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return ledScrollPanel;
	}

	public SAMeterPanel getSALMeterPanel() {
		if (saLMeterPanel == null) {
			saLMeterPanel = new SAMeterPanel(50, 25);
			Dimension d = new Dimension(104, 60);
			saLMeterPanel.setSize(d);
			saLMeterPanel.setMaximumSize(d);
			saLMeterPanel.setMinimumSize(d);
			saLMeterPanel.setPreferredSize(d);
			saLMeterPanel.setDoubleBuffered(true);
			saLMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return saLMeterPanel;
	}

	public SAMeterPanel getSARMeterPanel() {
		if (saRMeterPanel == null) {
			saRMeterPanel = new SAMeterPanel(50, 25);
			Dimension d = new Dimension(104, 60);
			saRMeterPanel.setSize(d);
			saRMeterPanel.setMaximumSize(d);
			saRMeterPanel.setMinimumSize(d);
			saRMeterPanel.setPreferredSize(d);
			saRMeterPanel.setDoubleBuffered(true);
			saRMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return saRMeterPanel;
	}

	public VUMeterPanel getVULMeterPanel() {
		if (vuLMeterPanel == null) {
			vuLMeterPanel = new VUMeterPanel(50);
			Dimension d = new Dimension(20, 60);
			vuLMeterPanel.setSize(d);
			vuLMeterPanel.setMaximumSize(d);
			vuLMeterPanel.setMinimumSize(d);
			vuLMeterPanel.setPreferredSize(d);
			vuLMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return vuLMeterPanel;
	}

	public VUMeterPanel getVURMeterPanel() {
		if (vuRMeterPanel == null) {
			vuRMeterPanel = new VUMeterPanel(50);
			Dimension d = new Dimension(20, 100);
			vuRMeterPanel.setSize(d);
			vuRMeterPanel.setMaximumSize(d);
			vuRMeterPanel.setMinimumSize(d);
			vuRMeterPanel.setPreferredSize(d);
			vuRMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return vuRMeterPanel;
	}

	private SeekBarPanel getSeekBarPanel() {
		if (seekBarPanel == null) {
			seekBarPanel = new SeekBarPanel(30, false);
			GridBagLayout gridBagLayout = (GridBagLayout) seekBarPanel
					.getLayout();
			gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 43 };
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0 };
			gridBagLayout.rowHeights = new int[] { 30, 38 };
			seekBarPanel.setName("SeekBarPanel");
			seekBarPanel.addListener(new SeekBarPanelListener() {
				@Override
				public void valuesChanged(long milliseconds) {
					if (currentPlayList != null && playerThread != null
							&& playerThread.isRunning())
						currentPlayList
								.setCurrentElementByTimeIndex(milliseconds);
				}
			});
		}
		return seekBarPanel;
	}

	private javax.swing.JButton getButton_Play() {
		if (button_Play == null) {
			buttonPlay_normal = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPLAY_NORMAL));
			buttonPlay_Inactive = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPLAY_INACTIVE));
			buttonPlay_Active = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPLAY_ACTIVE));

			button_Play = new javax.swing.JButton();
			button_Play.setName("button_Play");
			button_Play.setText("");
			button_Play.setToolTipText("play");
			button_Play
					.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			button_Play
					.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			button_Play.setIcon(buttonPlay_normal);
			button_Play.setDisabledIcon(buttonPlay_Inactive);
			button_Play.setPressedIcon(buttonPlay_Active);
			button_Play.setMargin(new java.awt.Insets(4, 6, 4, 6));
			button_Play.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doStartPlaying();
				}
			});
		}
		return button_Play;
	}

	private javax.swing.JButton getButton_Pause() {
		if (button_Pause == null) {
			buttonPause_normal = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPAUSE_NORMAL));
			buttonPause_Inactive = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPAUSE_INACTIVE));
			buttonPause_Active = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPAUSE_ACTIVE));

			button_Pause = new javax.swing.JButton();
			button_Pause.setName("button_Pause");
			button_Pause.setText("");
			button_Pause.setToolTipText("pause");
			button_Pause
					.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			button_Pause
					.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			button_Pause.setIcon(buttonPause_normal);
			button_Pause.setDisabledIcon(buttonPause_Inactive);
			button_Pause.setPressedIcon(buttonPause_Active);
			button_Pause.setMargin(new java.awt.Insets(4, 6, 4, 6));
			button_Pause.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doPausePlaying();
				}
			});
		}
		return button_Pause;
	}

	private javax.swing.JButton getButton_Stop() {
		if (button_Stop == null) {
			buttonStop_normal = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONSTOP_NORMAL));
			buttonStop_Inactive = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONSTOP_INACTIVE));
			buttonStop_Active = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONSTOP_ACTIVE));

			button_Stop = new javax.swing.JButton();
			button_Stop.setName("button_Stop");
			button_Stop.setText("");
			button_Stop.setToolTipText("stop");
			button_Stop
					.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			button_Stop
					.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			button_Stop.setIcon(buttonStop_normal);
			button_Stop.setDisabledIcon(buttonStop_Inactive);
			button_Stop.setPressedIcon(buttonStop_Active);
			button_Stop.setMargin(new java.awt.Insets(4, 6, 4, 6));
			button_Stop.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doStopPlaying();
				}
			});
		}
		return button_Stop;
	}

	private javax.swing.JButton getButton_Prev() {
		if (button_Prev == null) {
			buttonPrev_normal = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPREV_NORMAL));
			buttonPrev_Inactive = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPREV_INACTIVE));
			buttonPrev_Active = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONPREV_ACTIVE));

			button_Prev = new javax.swing.JButton();
			button_Prev.setName("button_Prev");
			button_Prev.setText("");
			button_Prev.setToolTipText("previous");
			button_Prev
					.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			button_Prev
					.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			button_Prev.setIcon(buttonPrev_normal);
			button_Prev.setDisabledIcon(buttonPrev_Inactive);
			button_Prev.setPressedIcon(buttonPrev_Active);
			button_Prev.setMargin(new java.awt.Insets(4, 6, 4, 6));
			button_Prev.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doPrevPlayListEntry();
				}
			});
		}
		return button_Prev;
	}

	private javax.swing.JButton getButton_Next() {
		if (button_Next == null) {
			buttonNext_normal = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONNEXT_NORMAL));
			buttonNext_Inactive = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONNEXT_INACTIVE));
			buttonNext_Active = new javax.swing.ImageIcon(getClass()
					.getResource(BUTTONNEXT_ACTIVE));

			button_Next = new javax.swing.JButton();
			button_Next.setName("button_Next");
			button_Next.setText("");
			button_Next.setToolTipText("next");
			button_Next
					.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			button_Next
					.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			button_Next.setIcon(buttonNext_normal);
			button_Next.setDisabledIcon(buttonNext_Inactive);
			button_Next.setPressedIcon(buttonNext_Active);
			button_Next.setMargin(new java.awt.Insets(4, 6, 4, 6));
			button_Next.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doNextPlayListEntry();
				}
			});
		}
		return button_Next;
	}

	public javax.swing.JLabel getVolumeLabel() {
		if (volumeLabel == null) {
			volumeLabel = new JLabel("Volume");
			volumeLabel.setFont(Helpers.DIALOG_FONT);
		}
		return volumeLabel;
	}
	
	private void volumeSliderChanged() {
		int volumeProcent = volumeSlider.getValue();
		currentVolume = ((float) volumeProcent) / 100;
		if (currentVolume < 0)
			currentVolume = 0;
		else if (currentVolume > 1)
			currentVolume = 1;
		volumeSlider.setToolTipText(Float.toString(currentVolume * 100f) + '%');
		roundVolumeSlider.setValue(currentVolume);
		roundVolumeSlider.setToolTipText(Float.toString(currentVolume * 100f) + '%');
		doSetVolumeValue();
	}

	private void balanceSliderChanged() {
		int balanceProcent = balanceSlider.getValue();
		
		currentBalance = (((float) balanceProcent - 50) * 2) / 100;
		if (currentBalance < -1)
			currentBalance = -1;
		else if (currentBalance > 1)
			currentBalance = 1;
		balanceSlider.setToolTipText(Float.toString(currentBalance * 100f) + '%');
		roundBalanceSlider.setValue((currentBalance + 1f) / 2f);
		roundBalanceSlider.setToolTipText(Float.toString(currentBalance * 100f) + '%');
		doSetBalanceValue();
	}
	
	private JSlider getSliderVolume() {
		if (volumeSlider == null) {
			volumeSlider = new JSlider();
			volumeSlider.setValue(100);
			volumeSlider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					volumeSliderChanged();
				}
				
			});
		}
		return volumeSlider;
	}
	private JSlider getSliderBalance() {
		if (balanceSlider == null) {
			balanceSlider = new JSlider();
			balanceSlider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					balanceSliderChanged();
				}
				
			});
		}
		return balanceSlider;
	}
	
	public RoundSlider getVolumeSlider() {
		if (roundVolumeSlider == null) {
			roundVolumeSlider = new RoundSlider();
			roundVolumeSlider.setSize(new Dimension(20, 20));
			roundVolumeSlider.setMinimumSize(new Dimension(20, 20));
			roundVolumeSlider.setMaximumSize(new Dimension(20, 20));
			roundVolumeSlider.setPreferredSize(new Dimension(20, 20));
			roundVolumeSlider.setValue(currentVolume);
			roundVolumeSlider
					.setToolTipText(Float.toString(currentVolume * 100f) + '%');
			roundVolumeSlider.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					RoundSlider slider = (RoundSlider) e.getSource();
					if (e.getClickCount() > 1) {
						slider.setValue(0.5f);
						e.consume();
					}
				}
			});
			roundVolumeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					RoundSlider slider = (RoundSlider) e.getSource();
					currentVolume = slider.getValue();
					if (currentVolume < 0)
						currentVolume = 0;
					else if (currentVolume > 1)
						currentVolume = 1;
					volumeSlider.setValue(new Integer((int) (currentVolume * 100)));
					volumeSlider.setToolTipText(Float.toString(currentVolume * 100f) + '%');
					slider.setToolTipText(Float.toString(currentVolume * 100f) + '%');
					doSetVolumeValue();
				}
			});
		}
		return roundVolumeSlider;
	}

	public javax.swing.JLabel getBalanceLabel() {
		if (balanceLabel == null) {
			balanceLabel = new JLabel("Balance");
			balanceLabel.setFont(Helpers.DIALOG_FONT);
		}
		return balanceLabel;
	}

	public RoundSlider getBalanceSlider() {
		if (roundBalanceSlider == null) {
			roundBalanceSlider = new RoundSlider();
			roundBalanceSlider.setSize(new Dimension(20, 20));
			roundBalanceSlider.setMinimumSize(new Dimension(20, 20));
			roundBalanceSlider.setMaximumSize(new Dimension(20, 20));
			roundBalanceSlider.setPreferredSize(new Dimension(20, 20));
			roundBalanceSlider.setValue((currentBalance + 1f) / 2f);
			roundBalanceSlider
					.setToolTipText(Float.toString(currentBalance * 100f) + '%');
			roundBalanceSlider.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					RoundSlider slider = (RoundSlider) e.getSource();
					if (e.getClickCount() > 1) {
						slider.setValue(0.5f);
						e.consume();
					}
				}
			});
			roundBalanceSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					RoundSlider slider = (RoundSlider) e.getSource();
					currentBalance = (slider.getValue() * 2f) - 1f;
					slider.setToolTipText(Float.toString(currentBalance * 100f) + '%');
					balanceSlider.setValue((int) (slider.getValue() * 100));
					balanceSlider.setToolTipText(Float.toString(currentBalance * 100f) + '%');

					doSetBalanceValue();
				}
			});
		}
		return roundBalanceSlider;
	}

	/**
	 * start playback of a audio file
	 * 
	 * @since 01.07.2006
	 */
	public void doStartPlaying() {
		doStartPlaying(false, 0);
	}

	/**
	 * @param initialSeek
	 * @since 13.02.2012
	 */
	public void doStartPlaying(boolean reuseMixer, long initialSeek) {
		if (currentContainer != null) {
			if (playerThread != null && !reuseMixer) {
				playerThread.stopMod();
				removeMixer();
				playerThread = null;
			}
			// if (inExportMode) {
			// inExportMode = false;
			// doExportToWave();
			// }
			if (playerThread == null) {
				Mixer mixer = createNewMixer();
				if (mixer != null) {
					if (initialSeek > 0)
						mixer.setMillisecondPosition(initialSeek);
					playerThread = new PlayThread(mixer, this);
					playerThread.start();
				}
			} else {
				playerThread.getCurrentMixer().setMillisecondPosition(
						initialSeek);
			}
		}
	}

	/**
	 * Creates a new Mixer for playback
	 * 
	 * @since 01.07.2006
	 * @return
	 */
	private Mixer createNewMixer() {
		Mixer mixer = getCurrentContainer().createNewMixer();
		if (mixer != null) {
			mixer.setAudioProcessor(audioProcessor);
			mixer.setVolume(currentVolume);
			mixer.setBalance(currentBalance);
			mixer.setSoundOutputStream(getSoundOutputStream());
			getSeekBarPanel().setCurrentMixer(mixer);
		}
		return mixer;
	}

	private void removeMixer() {
		getSeekBarPanel().setCurrentMixer(null);
	}

	/**
	 * @since 14.09.2008
	 * @return
	 */
	private MultimediaContainer getCurrentContainer() {
		if (currentContainer == null) {
			try {
				currentContainer = MultimediaContainerManager.getMultimediaContainer(currentPlayList.getCurrentEntry().getFile());
				//currentContainer = MultimediaContainerManager.getMultimediaContainerForType("mod");
			} catch (Exception ex) {
				Log.error("getCurrentContainer()", ex);
			}
		}
		return currentContainer;
	}

	private SoundOutputStream getSoundOutputStream() {
		if (soundOutputStream == null) {
			soundOutputStream = new GaplessSoundOutputStreamImpl();
		}
		return soundOutputStream;
	}

	/**
	 * pause the playing of a mod
	 * 
	 * @since 01.07.2006
	 */
	private void doPausePlaying() {
		if (playerThread != null) {
			playerThread.pausePlay();
		}
	}

	/**
	 * stop playback of a mod
	 * 
	 * @since 01.07.2006
	 */
	private void doStopPlaying() {
		if (playerThread != null) {
			playerThread.stopMod();
			getSoundOutputStream().closeAllDevices();
			playerThread = null;
			removeMixer();
		}
	}

	private void cascadeOtherEntryPlaying() {
		int currentPlayIndex = currentPlayList.getCurrentEntry().getIndexInPlaylist();
		if(currentPlayIndex >= 0 && currentPlayIndex < playlistPanel.getPlaylist().size()) {
			MusicData musicData = playlistPanel.getPlaylist().get(currentPlayIndex);
			int position = musicData.getPosition();
			playlistPanel.getLanData().setAndStoreCurPlayed(position);
		}
	}
	
	private boolean doNextPlayListEntry() {		
		boolean ok = false;
		while (currentPlayList != null && currentPlayList.hasNext() && !ok) {
			currentPlayList.next();
			ok = loadMultimediaFile(currentPlayList.getCurrentEntry(), true);
		}
		cascadeOtherEntryPlaying();
		
		return ok;
	}

	private boolean doPrevPlayListEntry() {
		boolean ok = false;
		while (currentPlayList != null && currentPlayList.hasPrevious() && !ok) {
			currentPlayList.previous();
			ok = loadMultimediaFile(currentPlayList.getCurrentEntry(), true);
		}
		cascadeOtherEntryPlaying();
		
		return ok;
	}

	private void doSetVolumeValue() {
		if (playerThread != null) {
			Mixer currentMixer = playerThread.getCurrentMixer();
			currentMixer.setVolume(currentVolume);
		}
	}

	private void doSetBalanceValue() {
		if (playerThread != null) {
			Mixer currentMixer = playerThread.getCurrentMixer();
			currentMixer.setBalance(currentBalance);
		}
	}

	/**
	 * @since 14.09.2008
	 * @param mediaPLSFileURL
	 */
	private boolean loadMultimediaOrPlayListFile(URL mediaPLSFileURL) {
		Log.info("");
		// addFileToLastLoaded(mediaPLSFileURL);
		currentPlayList = null;
		try {
			currentPlayList = PlayList.createFromFile(mediaPLSFileURL, false, true); // repeating
			if (currentPlayList != null) {
				// getPlaylistGUI().setNewPlaylist(currentPlayList);
				return doNextPlayListEntry();
			}
		} catch (Throwable ex) {
			currentPlayList = null;
		}
		return false;
	}

	/**
	 * load a mod file and display it
	 * 
	 * @since 01.07.2006
	 * @param modFileName
	 * @return boolean if loading succeeded
	 */
	private boolean loadMultimediaFile(PlayListEntry playListEntry, boolean startPlaying) {
		if(playListEntry == null) return false;
		final URL mediaFileURL = playListEntry.getFile();
		final boolean reuseMixer = (currentContainer != null && Helpers.isEqualURL(currentContainer.getFileURL(),
						mediaFileURL) && playerThread != null && playerThread
				.isRunning());
		if (!reuseMixer) {
			try {
				if (mediaFileURL != null) {
					MultimediaContainer newContainer = MultimediaContainerManager.getMultimediaContainer(mediaFileURL);
					if (newContainer != null) {
						currentContainer = newContainer;
						getLEDScrollPanel().setScrollTextTo(
								currentContainer.getSongName()
										+ Helpers.SCROLLY_BLANKS + "               ");
						// getTrayIcon().setToolTip(currentContainer.getSongName());
					}
				}
			} catch (Throwable ex) {
				return false;
			}
			// changeInfoPane();
			// changeConfigPane();
			// changeExportMenu();
		}
		setPlayListIcons();
		// if we are currently playing, start the current piece:
		if (playerThread != null && startPlaying)
			doStartPlaying(reuseMixer, playListEntry.getTimeIndex());
		return true;
	}

	/**
	 * Open a new File
	 * 
	 * @since 22.06.2006
	 */
	public void doOpenURL(String surl) {
		if (surl != null) {
			loadMultimediaOrPlayListFile(Helpers.createURLfromString(surl));
		}
	}

	/**
	 * Open a new File
	 * 
	 * @since 22.06.2006
	 */
	public void doOpenFile(File[] files) {
		if (files != null) {
			if (files.length == 1) {
				File f = files[0];
				if (f.isFile()) {
					String modFileName = f.getAbsolutePath();
					int i = modFileName.lastIndexOf(File.separatorChar);
					searchPath = modFileName.substring(0, i);
					loadMultimediaOrPlayListFile(Helpers.createURLfromFile(f));
				} else if (f.isDirectory()) {
					searchPath = f.getAbsolutePath();
				}
			} else {
				playlistRecieved(null, PlayList.createNewListWithFiles(files, false, true), null);
			}
		}
	}

	private void loadCurrentEntry(boolean startPlaying) {
		boolean ok = false;
		while (currentPlayList != null && !ok) {
			final PlayListEntry entry = currentPlayList.getCurrentEntry();
			ok = loadMultimediaFile(entry, startPlaying);
			if (!ok)
				currentPlayList.next();
			else if (playerThread == null && startPlaying)
				doStartPlaying(true, entry.getTimeIndex());
		}
	}
	
	/**
	 * 
	 * @see de.quippy.javamod.main.gui.playlist.PlaylistGUIChangeListener#userSelectedPlaylistEntry()
	 * @since 13.02.2012
	 */
	public void userSelectedPlaylistEntry(int index) {
		if(currentPlayList != null && index >= 0 && index < currentPlayList.size()) {
			currentPlayList.setCurrentElement(index);
			loadCurrentEntry(true);
			cascadeOtherEntryPlaying();
		}
		
	}

	private void setPlayListIcons() {
		if (currentPlayList == null) {
			getButton_Prev().setEnabled(false);
			getButton_Next().setEnabled(false);
		} else {
			getButton_Prev().setEnabled(currentPlayList.hasPrevious());
			getButton_Next().setEnabled(currentPlayList.hasNext());
		}
		// getPrevItem().setEnabled(getButton_Prev().isEnabled());
		// getNextItem().setEnabled(getButton_Next().isEnabled());
	}

	/**
	 * @param dtde
	 * @param dropResult
	 * @param addToLastLoaded
	 * @see de.quippy.javamod.main.gui.tools.PlaylistDropListenerCallBack#playlistRecieved(java.awt.dnd.DropTargetDropEvent,
	 *      de.quippy.javamod.main.playlist.PlayList, java.net.URL)
	 * @since 08.03.2011
	 */
	public void playlistRecieved(DropTargetDropEvent dtde, PlayList dropResult,
			URL addToLastLoaded) {
		//if (addToLastLoaded != null)
			// addFileToLastLoaded(addToLastLoaded);
			if (dropResult != null) {
				//doStopPlaying();
				
				int position = playlistPanel.getLanData().getCurrentlyPlayed();
				
//				int currentElement = 0;
//				if(currentPlayList != null) {
//					PlayListEntry currentPLE = currentPlayList.getCurrentEntry();
//					if(currentPLE != null) {
//						currentElement = currentPLE.getIndexInPlaylist();
//					}
//				}
	
				currentPlayList = dropResult;			
				currentPlayList.setCurrentElement(position - 1);
				
//				System.out.println("\n");
//				
//				for(PlayListEntry ple : currentPlayList.getAllEntries()) {
//					boolean playing = ple.getIndexInPlaylist() == currentPlayList.getCurrentEntry().getIndexInPlaylist();
//					System.out.println((playing ? ">>>>>>>>>>" : "") + ple.getFormattedName());
//				}
				
				loadCurrentEntry(false);
				
				//loadMultimediaFile(currentPlayList.getCurrentEntry());
				
				// getPlaylistGUI().setNewPlaylist(currentPlayList);
				// boolean ok = doNextPlayListEntry();
				// if (playerThread == null && ok) {
			  	//  doStartPlaying();
				// }
			}
	}

	@Override
	public void playThreadEventOccured(PlayThread thread) {
		if (thread.isRunning()) {
			getButton_Play().setIcon(buttonPlay_Active);
		} else // Signaling: not running-->Piece finished...
		{
			getButton_Play().setIcon(buttonPlay_normal);
			if (thread.getHasFinishedNormaly()) {
				
				int currentPlayIndex = currentPlayList.getCurrentEntry().getIndexInPlaylist();
				if(currentPlayIndex >= 0 && currentPlayIndex < playlistPanel.getPlaylist().size()) {
					MusicData musicData = playlistPanel.getPlaylist().get(currentPlayIndex);
					int position = musicData.getPosition();
					String playedStr = playlistPanel.getLanData().getValue(LanData.PLAYED_TAG, position);
					int played = 0;
					try {
						played = Integer.parseInt(playedStr);
						played++;
					}
					catch(Exception e) {
					}
					
					if(played > 0) {
						playlistPanel.getLanData().storePlayed(position, played);
					}
					
					playlistPanel.getLanData().setAndStoreCurPlayed(position);
				}
				
				boolean ok = doNextPlayListEntry();
				if (!ok)
					doStopPlaying();
			}
		}

		Mixer mixer = thread.getCurrentMixer();
		if (mixer != null) {
			if (mixer.isPaused())
				getButton_Pause().setIcon(buttonPause_Active);
			else
				getButton_Pause().setIcon(buttonPause_normal);
		}
	}

	/* DspAudioProcessor CallBack ------------------------------------------- */
	public void currentSampleChanged(float[] leftSample, float[] rightSample) {
		getVULMeterPanel().setVUMeter(leftSample);
		getVURMeterPanel().setVUMeter(rightSample);

		getSALMeterPanel().setMeter(leftSample);
		getSARMeterPanel().setMeter(rightSample);
	}

	@Override
	public void multimediaContainerEventOccured(MultimediaContainerEvent event) {
		if (event.getType() == MultimediaContainerEvent.SONG_NAME_CHANGED) {
			getLEDScrollPanel().addScrollText(
					event.getEvent().toString() + Helpers.SCROLLY_BLANKS);
		} else if (event.getType() == MultimediaContainerEvent.SONG_NAME_CHANGED_OLD_INVALID) {
			getLEDScrollPanel().setScrollTextTo(
					event.getEvent().toString() + Helpers.SCROLLY_BLANKS);
			// getTrayIcon().setToolTip(event.getEvent().toString());
		}
	}
	
}
