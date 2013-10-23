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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import main.ClientGui;

public class Client {

	private final ExecutorService pool;
	private final String serverAddress;
	private Socket server;
	private BufferedOutputStream serverOutput;
	private BufferedInputStream serverInput;
	//private AtomicInteger c = new AtomicInteger(1);
	
	private static final int BUFFER_SIZE = 4096;
	
	private ClientHandler clientHandler;
	
	public ClientHandler getClientHandler() {
		return clientHandler;
	}

	public Client(String serverAddress) throws UnknownHostException, IOException {
		
		this.clientHandler = new ClientHandler(this);
		
		this.serverAddress = serverAddress;
		pool = Executors.newCachedThreadPool();

		server = new Socket(serverAddress, 56000);
		server.setKeepAlive(true);
		serverOutput = new BufferedOutputStream(server.getOutputStream());
		serverInput = new BufferedInputStream(server.getInputStream());
		receiveMessage();
		receiveProperty();
	}

	private void sendFile(final File[] files) {
		pool.submit(new Runnable() {		
			@Override
			public void run() {
				for (final File file : files) {
					try (Socket socket = new Socket(serverAddress, 55000)) {
						System.out.println("Sending file: " + file.getName());
						BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
						byte[] buffer = new byte[BUFFER_SIZE];
						FileInputStream in = new FileInputStream(file);
						int count = 0;
						while ((count = in.read(buffer)) >= 0) {
							
							out.write(buffer, 0, count);
							//out.flush();
						}
						in.close();
						socket.close();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
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
				byte[] buffer = new byte[BUFFER_SIZE];
				synchronized (serverOutput) {
					try {
						int count = 0;
						BufferedOutputStream out = new BufferedOutputStream(
								serverOutput);
						for (byte b : message.getBytes())
							buffer[count++] = b;
						out.write(buffer);
						//out.flush();

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
				boolean stop = false;
				while (!stop) {
					byte[] buffer = new byte[BUFFER_SIZE];
					synchronized (serverInput) {
						try {
							BufferedInputStream in = new BufferedInputStream(
									serverInput);
							in.read(buffer);
							String message = new String(buffer);
							//System.out.println(c.getAndIncrement() + message);
							
							// handle server message
							clientHandler.handleServerMessage(message);
						} catch (IOException e) {
							stop = true;
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

	//AtomicInteger counter = new AtomicInteger(1);

	private void receiveProperty() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Socket server = new Socket(serverAddress, 57000);
					System.out.println("Client: waiting for file");
					BufferedInputStream in = new BufferedInputStream(server.getInputStream());
					byte[] buffer = new byte[BUFFER_SIZE];
					//File file = new File("./src/temp" + counter.getAndIncrement());
					FileOutputStream out = new FileOutputStream(ClientGui.LAN_DATA_FILE);
					int count = 0;
					while ((count = in.read(buffer)) >= 0) {
						out.write(buffer, 0, count);
						//out.flush();
					}
					FileInputStream fis = new FileInputStream(ClientGui.LAN_DATA_FILE);
					Properties prop = new Properties();
					prop.load(fis);
					out.close();
					in.close();
					server.close();
					
					clientHandler.handleServerProperty(prop);
					
					receiveProperty();

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
		try {
			pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		pool.shutdown();
	}
}