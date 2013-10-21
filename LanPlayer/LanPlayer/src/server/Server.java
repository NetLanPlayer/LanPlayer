package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import client.Client;
import lanplayer.PlaylistPanel;

public class Server extends Observable {

	private List<Socket> propertySendClients;
	private List<Socket> communicationClients;

	private ExecutorService pool;
	private String fileLocation;
	private AtomicInteger nameCounter = new AtomicInteger(1);

	public Server(String mp3Location) {
		fileLocation = mp3Location;
		propertySendClients = Collections.synchronizedList(new LinkedList<Socket>());
		communicationClients = Collections.synchronizedList(new LinkedList<Socket>());

		pool = Executors.newCachedThreadPool();
		initServer();
		System.out.println("Server started");
	}

	private void initServer() {
		/**
		 * This serversocket accepts connections for RECEIVING TRACKS FROM CLIENT,
		 * PORT: 55000
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket fileServer = new ServerSocket(55000, 100)) {
					while (true) {
						final Socket client = fileServer.accept();
						pool.submit(new Runnable() {
							public void run() {
								byte[] buffer = new byte[1024];
								File file = new File("C:/test/" + nameCounter.getAndIncrement() + ".mp3");

								try (BufferedInputStream in = new BufferedInputStream(client.getInputStream(), 1024)) {
									FileOutputStream out = new FileOutputStream(file);
									while (in.read(buffer) != -1) {
										out.write(buffer);
										out.flush();
									}
									out.close();
									client.close();
								} catch (IOException e) {
								}
							}
						});
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

		/**
		 * This serversocket accepts connections for RECEIVING MESSAGES FROM
		 * CLIENT, PORT: 56000
		 * 
		 **/
		new Thread(new Runnable() {
			@Override
			public void run() {
				try (final ServerSocket communication = new ServerSocket(56000)) {
					while (true) {
						final Socket client = communication.accept();
						pool.submit(new Runnable() {
							@Override
							public void run() {
								try {
									byte[] buffer = new byte[1024];
									BufferedInputStream in = new BufferedInputStream(client.getInputStream());
									String message = null;
									while (in.read(buffer) != -1) {
										message = new String(buffer);
									}
									handleClientMessages(message);
									client.close();
								} catch (IOException e) {
								}
							}
						});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		/**
		 * This serversocket accepts connections for SENDS MESSAGES TO CLIENT,
		 * PORT: 57000
		 * 
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket propertySendServer = new ServerSocket(57000)) {
					while (true) {
						communicationClients.add(propertySendServer.accept());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

		/**
		 * This serversocket accepts connections for SENDING FILES TO CLIENT,
		 * PORT: 58000
		 * 
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket propertySendServer = new ServerSocket(58000)) {
					while (true) {
						propertySendClients.add(propertySendServer.accept());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	/**
	 * Sends messages to all Clients
	 * 
	 * @param String
	 * @return void
	 */
	public void sendMessage(final String message) {
		if (!communicationClients.isEmpty()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (Socket client : new ArrayList<Socket>(communicationClients)) {
						byte[] buffer = new byte[message.getBytes().length];
						try {
							BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
							int count = 0;
							for (byte b : message.getBytes()) {
								buffer[count++] = b;
							}
							out.write(buffer);
							out.flush();
							communicationClients.remove(client);
							client.close();
						} catch (IOException e) {
							e.printStackTrace();

						}

					}

				}

			}).start();
		}

	}

	public void sendFile(final File file) {
		if (!propertySendClients.isEmpty()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (Socket client : new ArrayList<Socket>(propertySendClients)) {
						byte[] buffer = new byte[1024];
						try {
							BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
							FileInputStream in = new FileInputStream(file);
							while (in.read(buffer) != -1) {
								out.write(buffer);
								out.flush();
							}
							in.close();
							propertySendClients.remove(client);
							client.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}
			}).start();
		}
	}

	private void handleClientMessages(String message) {
		if (message.equals(Client.MSG_REQ_PROPERTY)) {
			System.out.println("Server: Received Property file request");
			handlePropertyFileReq();
		}
		if (message.equals("tracklistsent")) {
			notifyAll();
			notifyObservers(null);
		}
	}

	private void handlePropertyFileReq() {
		sendFile(PlaylistPanel.LAN_DATA_FILE);
	}

	public void closeServer() {
		// TODO shutdown anything

		pool.shutdown();

	}
}
