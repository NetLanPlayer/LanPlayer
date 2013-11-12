package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import server.Server;
import utilities.HideFile;
import main.ClientGui;

public class Client {

	private final String serverAddress;
	private static final int BUFFER_SIZE = 4096;
	private ClientHandler clientHandler;
	private ClientGui clientGui;

	public ClientHandler getClientHandler() {
		return clientHandler;
	}

	public Client(String serverAddress, ClientGui callee) throws UnknownHostException, IOException {

		this.clientHandler = new ClientHandler();
		this.clientGui = callee;
		this.serverAddress = serverAddress;
		receiveMessage();
		receiveFile();
	}

	private void updateProgressBar(final int percent) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				clientGui.getUploadBar().setValue(percent);
			}

		});
	}

	private void sendFile(final File[] files) {
		new Thread(new Runnable() {

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
						in.close();
						socket.close();
					} catch (UnknownHostException e) {
						clientGui.disconnectedState(true);
					} catch (IOException e) {
						clientGui.disconnectedState(true);
					}
					int percent = ((int) Math.ceil((100.0 / size))) * (i + 1);
					if (percent < 0)
						percent = 0;
					if (percent > 100)
						percent = 100;
					updateProgressBar(percent);
					if (percent >= 100) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
						clientGui.getUploadBar().setValue(0);
						clientGui.getBtnUpload().setEnabled(false);
						clientGui.enablePathAndSearch(true);
					}
				}
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
				} catch (ConnectException ce) {
					clientGui.disconnectedState(true);
					return;
				} catch (IOException e) {
					clientGui.disconnectedState(true);
					return;
				}
				// clientGui.connectedState();
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
					if (!temp.exists()) {
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

					try {
						HideFile.hide(temp);
					} catch (InterruptedException e) {
					}

					temp.delete();
					temp.deleteOnExit();

					receiveFile();
				} catch (ConnectException ce) {
					clientGui.disconnectedState(true);
					return;
				} catch (UnknownHostException e) {
					clientGui.disconnectedState(true);
					return;
				} catch (IOException e) {
					clientGui.disconnectedState(true);
					return;
				}
				// clientGui.connectedState();
			}
		}).start();
	}

	/**
	 * checks path: if path is MP3 file or a directory with MP3 files in it.
	 * 
	 * @param path
	 * @return true if valid.
	 */
	public boolean isValidPath(String path) {
		path = validatePath(path);
		File file = new File(path);
		if (file.isDirectory()) {
			try {
				for (File f : file.listFiles()) {
					if (!f.isFile() && f.getName().lastIndexOf(".") < 0)
						continue;
					if (f.getName().substring(f.getName().lastIndexOf("."), f.getName().length()).equals(".mp3")) {
						return true;
					}
				}
			} catch (Exception e) {
				return false;
			}
			return false;
		} else if (file.isFile()) {
			if (file.getName().substring(file.getName().lastIndexOf("."), file.getName().length()).equals(".mp3")) {
				return true;
			}
			return false;
		} else {
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

	public static String findServerAddress() {
		String serverAddress = null;
		try {
			DatagramSocket ds = new DatagramSocket();
			byte[] buf = new String("whatsyourip").getBytes();
			InetAddress ia = InetAddress.getByName("255.255.255.255");
			DatagramPacket packet = new DatagramPacket(buf, buf.length, ia, Server.DATAGRAM_PORT);
			ds.send(packet);
			buf = new byte[256];
			ds.setSoTimeout(1000);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("sent by client");
			ds.receive(packet);
			StringBuilder sb = new StringBuilder("");
			for (char c : packet.getSocketAddress().toString().toCharArray()) {
				if (c != '/') {
					if (c == ':') {
						serverAddress = sb.toString();
						break;
					}
					sb.append(c);
				}
			}
			ds.close();
		} catch (SocketException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return serverAddress;

	}
}
