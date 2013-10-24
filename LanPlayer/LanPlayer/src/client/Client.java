package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import server.Server;
import main.ClientGui;

public class Client {

	//private final ExecutorService pool;
	private final String serverAddress;
	private Socket server;
	private static final int BUFFER_SIZE = 4096;
	private ClientHandler clientHandler;
	private ClientGui clientGui;
	
	
	public ClientHandler getClientHandler() {
		return clientHandler;
	}

	public Client(String serverAddress, ClientGui callee) throws UnknownHostException, IOException {

		this.clientHandler = new ClientHandler(this);
		this.clientGui = callee;
		this.serverAddress = serverAddress;
		//pool = Executors.newCachedThreadPool();
		receiveMessage();
		receiveFile();
	}

	private void sendFile(final File[] files) {
		new Thread(new Runnable() {
			
			private void updateProgressBar(final int percent) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						clientGui.getUploadBar().setValue(percent);
						if(percent >= 95) {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
							}
							clientGui.getUploadBar().setValue(0);
							clientGui.getBtnUpload().setEnabled(false);
							clientGui.enablePathAndSearch(true);
						}
					}
					
				});
			}
			
			@Override
			public void run() {
				
				int size = files.length;
				for (int i = 0; i < files.length; i++) {
					final File file = files[i];
					try (Socket socket = new Socket(serverAddress, Server.REC_FILE_PORT)) {
						BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
						byte[] buffer = new byte[BUFFER_SIZE];
						FileInputStream in = new FileInputStream(file);
						int count = 0;
						while ((count = in.read(buffer)) >= 0) {
							out.write(buffer, 0, count);
						}
						System.out.println("Client: File " + file.getName() + " sent.");
						clientGui.enablePathAndSearch(false);
						in.close();
						socket.close();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					int percent = (100 / size) * (i + 1);
					if(percent < 0) percent = 0;
					if(percent > 100) percent = 100;
					updateProgressBar(percent);
				}
//				try {
//					sendMessage(ClientHandler.MSG_UPLOAD_FINISHED);	
//				}
//				catch(ConnectException ce) {
//					//TODO
//				}
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
					Socket server = new Socket(serverAddress, Server.REC_MESSAGE_PORT);
					BufferedOutputStream out = new BufferedOutputStream(server.getOutputStream());
					byte[] buffer = new byte[message.getBytes().length];
					int count = 0;
					for (byte b : message.getBytes()) {
						buffer[count++] = b;
					}
					out.write(buffer);
					out.flush();
					server.close();
				} catch(ConnectException ce) {
					clientGui.disconnectedState();
					return;
				} catch (IOException e) {
					clientGui.disconnectedState();
					return;
				}
				clientGui.connectedState();
			}
		}).start();
	}

	private void receiveMessage() {
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					Socket server = new Socket(serverAddress, Server.SEND_MESSAGE_PORT);
					byte[] buffer = new byte[BUFFER_SIZE];
					BufferedInputStream in = new BufferedInputStream(server.getInputStream());
					String message = null;

					while (in.read(buffer) != -1) {
						message = new String(buffer);
					}
					clientHandler.handleServerMessage(message);
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
					Socket server = new Socket(serverAddress, Server.SEND_FILE_PORT);
					System.out.println("Client: waiting for file");
					BufferedInputStream in = new BufferedInputStream(server.getInputStream());
					byte[] buffer = new byte[1024];
					File temp = new File("./ClientData/temp");
					if(!temp.exists()) {
						temp.createNewFile();
					}
					
					FileOutputStream out = new FileOutputStream(temp);
					int count = 0;
					while ((count = in.read(buffer)) >= 0) {
						out.write(buffer, 0, count);
					}
					clientHandler.handleServerFile(temp);
					
					out.close();
					in.close();
					server.close();
					
					temp.delete();
					
					receiveFile();
				} catch (ConnectException ce) {
					clientGui.disconnectedState();
					return;
				} catch (UnknownHostException e) {
					clientGui.disconnectedState();
					return;
				} catch (IOException e) {
					clientGui.disconnectedState();
					return;
				}
				clientGui.connectedState();
			}
		}).start();
	}

	/**
	 * checks path: if path is MP3 file or a directory with MP3 files in it.
	 * @param path
	 * @return true if valid.
	 */
	public boolean isValidPath(String path) {
		path = validatePath(path);
		File file = new File(path);
		if(file.isDirectory()) {
			try {
				for(File f : file.listFiles()) {
					if(!f.isFile() && f.getName().lastIndexOf(".") < 0) continue;
					if(f.getName().substring(f.getName().lastIndexOf("."), f.getName().length()).equals(".mp3")) {
						return true;
					}
				}
			}
			catch(Exception e) {
				return false;
			}
			return false;
		}
		else if(file.isFile()) {
			if(file.getName().substring(file.getName().lastIndexOf("."), file.getName().length()).equals(".mp3")) {
				return true;
			}
			return false;
		}
		else {
			return false;
		}
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
//		try {
//			pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
//		} catch (InterruptedException e) {
//		}
//		pool.shutdown();
	}
}