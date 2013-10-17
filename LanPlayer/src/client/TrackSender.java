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
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TrackSender {

	private final ExecutorService pool;
	private final String serverAddress;
	private Socket server;
	private BufferedOutputStream serverOutput;
	private BufferedInputStream serverInput;
	AtomicInteger c = new AtomicInteger(1);

	public TrackSender(String serverAddress) throws UnknownHostException,
			IOException {
		this.serverAddress = serverAddress;
		pool = Executors.newCachedThreadPool();

		server = new Socket(serverAddress, 56000);
		server.setKeepAlive(true);
		serverOutput = new BufferedOutputStream(server.getOutputStream());
		serverInput = new BufferedInputStream(server.getInputStream());
		receiveMessage();
		receiveFile();
	}

	private void sendFile(File[] files) {
		for (final File file : files) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					try (Socket socket = new Socket(serverAddress, 55000)) {
						System.out.println(file.getName());
						BufferedOutputStream out = new BufferedOutputStream(
								socket.getOutputStream(), 1024);
						byte[] buffer = new byte[1024];
						FileInputStream in = new FileInputStream(file);
						while (in.read(buffer) != -1) {
							System.out.println("rec");
							out.write(buffer);
							out.flush();
						}
						System.out.println("track sent");
						in.close();
						socket.close();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Sends messages to Server
	 * 
	 * @param String
	 * @return void
	 */
	public void sendMessage(final String message) {
		pool.submit(new Runnable() {
			@Override
			public void run() {
				byte[] buffer = new byte[1024];
				synchronized (serverOutput) {
					try {
						int count = 0;
						BufferedOutputStream out = new BufferedOutputStream(
								serverOutput);
						for (byte b : message.getBytes())
							buffer[count++] = b;
						out.write(buffer);
						out.flush();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void receiveMessage() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					byte[] buffer = new byte[1024];
					synchronized (serverInput) {
						try {
							BufferedInputStream in = new BufferedInputStream(
									serverInput);
							in.read(buffer);
							String message = new String(buffer);
							System.out.println(c.getAndIncrement() + message);
							// TODO handleMessage(message);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
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

	private void receiveFile() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Socket server = new Socket(serverAddress, 57000);
					System.out.println("waiting for file");
					BufferedInputStream in = new BufferedInputStream(
							server.getInputStream());
					int count = 0;

						synchronized (in) {
							
							byte[] buffer = new byte[1];

							File file = new File("./src/temp" + count++);
							FileOutputStream out = new FileOutputStream(file);
							while (in.read(buffer) != -1) {
								out.write(buffer);
								out.flush();
							}
							Properties prop = new Properties();
							prop.load(new FileInputStream(file));
							System.out.println(prop.get("key"));
							// TODO handle file shit
							System.out.println("file is here");
							out.close();
							in.close();
							server.close();
						receiveFile();

					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

	}

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