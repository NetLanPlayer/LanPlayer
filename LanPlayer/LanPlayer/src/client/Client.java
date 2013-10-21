package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import main.ClientGui;

public class Client extends Observable {

	private final ExecutorService pool;
	private final String serverAddress;
	private Socket server;
	AtomicInteger c = new AtomicInteger(1);

	public final static String MSG_REQ_PROPERTY = "Client request property file.";

	public Client(String serverAddress) throws UnknownHostException, IOException {
		this.serverAddress = serverAddress;
		pool = Executors.newCachedThreadPool();
		receiveMessage();
		receiveFile();
	}

	private void sendFile(final File[] files) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (File file : files) {
					try (Socket socket = new Socket(serverAddress, 55000)) {
						BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream(), 1024);
						byte[] buffer = new byte[1024];
						FileInputStream in = new FileInputStream(file);
						while (in.read(buffer) != -1) {
//							System.o<ut.println("sen");
							out.write(buffer);
							out.flush();
						}
						in.close();
						socket.close();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				sendMessage("tracklistsent");
			}
		}).start();
	}

	/**
	 * Sends messages to Server
	 * 
	 * @param String
	 * @return void
	 */
	public void sendMessage(final String message) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Socket server = new Socket(serverAddress, 56000);
					BufferedOutputStream out = new BufferedOutputStream(server.getOutputStream());
					byte[] buffer = new byte[message.getBytes().length];
					int count = 0;
					for (byte b : message.getBytes()) {
						buffer[count++] = b;
					}
					out.write(buffer);
					out.flush();
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	private void receiveMessage() {
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					Socket server = new Socket(serverAddress, 56000);
					byte[] buffer = new byte[1024];
					BufferedInputStream in = new BufferedInputStream(server.getInputStream());
					while (in.read(buffer) != -1) {
						String message = new String(buffer);
					}
					server.close();
					receiveMessage();
				} catch (IOException e) {
				}
			}
		}).start();

	}

	private void receiveFile() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Socket server = new Socket(serverAddress, 58000);
					System.out.println("Client: waiting for file");
					BufferedInputStream in = new BufferedInputStream(server.getInputStream());
					byte[] buffer = new byte[1024];
					FileOutputStream out = new FileOutputStream(ClientGui.LAN_DATA_FILE);
					while (in.read(buffer) != -1) {
						out.write(buffer);
						out.flush();
					}
					FileInputStream fis = new FileInputStream(ClientGui.LAN_DATA_FILE);
					Properties prop = new Properties();
					prop.load(fis);
					setChanged();
					notifyObservers(prop);
					out.close();
					in.close();
					server.close();
					receiveFile();

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

	}

	/**
	 * checks path: if path is MP3 file or a directory with MP3 files in it, it
	 * will upload all this files to a given server.
	 * 
	 * @param String
	 * @return boolean uploaded anything
	 */
	public boolean checkPathAndSend(String path) {
		path = validatePath(path);
		File file = new File(path);
		if (file.isDirectory()) {
			sendFile(file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".mp3");
				}
			}));
			return true;
		} else if (file.isFile()) {
			if (file.getName().endsWith(".mp3")) {
				sendFile(new File[] { file });
			}
			return true;
		}
		return false;
	}

	/**
	 * Takes String and replaces '\' through '/'.
	 * 
	 * @param String
	 * @return String
	 */
	private String validatePath(String path) {
		StringBuilder ret = new StringBuilder("");
		for (char c : path.toCharArray()) {
			if (c == '\\') {
				ret.append('/');
			} else {
				ret.append(c);
			}
		}
		return ret.toString();
	}

	// AtomicInteger counter = new AtomicInteger(1);

	/**
	 * Proper close of ClientApplication. Shutdown threadpool and close socket.
	 * 
	 * @return void
	 */
	public void closeClient() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pool.shutdown();
	}
}