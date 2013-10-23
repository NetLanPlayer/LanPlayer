package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import client.Client;
import lanplayer.PlaylistPanel;
import main.ServerGui;

public class Server {

	private List<Socket> communicationClients;
	private List<Socket> propertySendClients;
	private ExecutorService pool;
	private String fileLocation;
	private AtomicInteger nameCounter = new AtomicInteger(1);
	private static final int BUFFER_SIZE = 4096;

	private ServerHandler serverHandler;

	public ServerHandler getServerHandler() {
		return serverHandler;
	}

	public Server(String mp3Location) {
		this.serverHandler = new ServerHandler(this);
		fileLocation = mp3Location;
		propertySendClients = Collections.synchronizedList(new LinkedList<Socket>());
		communicationClients = Collections.synchronizedList(new LinkedList<Socket>());
		pool = Executors.newCachedThreadPool();
		initServer();
		System.out.println("Server started");
	}

	private void initServer() {
		/**
		 * This serversocket accepts connections for RECEIVING TRACKS FROM
		 * CLIENT, PORT: 55000
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket fileServer = new ServerSocket(55000, 1000)) {
					while (true) {
						final Socket client = fileServer.accept();
						new Thread(new Runnable() {
							public void run() {
								byte[] buffer = new byte[BUFFER_SIZE];
								File file = new File(fileLocation + nameCounter.getAndIncrement() + ".mp3");

								try (BufferedInputStream in = new BufferedInputStream(client.getInputStream(), BUFFER_SIZE)) {
									FileOutputStream out = new FileOutputStream(file);
									int count = 0;
									while ((count = in.read(buffer)) >= 0) {
										out.write(buffer, 0, count);
										// out.flush();
									}
									out.close();
									client.close();
								} catch (IOException e) {
									e.printStackTrace();
								}

								serverHandler.handleClientFile(file, client.getInetAddress().getHostAddress());
							}
						}).start();
					}

				} catch (java.net.BindException be) {
					System.out.println("Application is already running. Address already in use: JVM_Bind");
					System.exit(0);
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
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									byte[] buffer = new byte[BUFFER_SIZE];
									BufferedInputStream in = new BufferedInputStream(client.getInputStream());
									String message = null;
									while (in.read(buffer) != -1) {
										message = new String(buffer);
									}
									serverHandler.handleClientMessage(message);
									client.close();
								} catch (IOException e) {
								}
							}
						}).start();
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
						communicationClients.add(propertySendServer.accept());					}
				} catch (java.net.BindException be) {
					System.exit(0);
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
						byte[] buffer = new byte[BUFFER_SIZE];
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

	public void closeServer() {
		// TODO shutdown anything
		for (Socket s : communicationClients) {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		pool.shutdown();

	}
}
