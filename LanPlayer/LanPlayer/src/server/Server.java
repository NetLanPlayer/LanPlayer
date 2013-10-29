package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

	public static final int REC_FILE_PORT = 55000;
	public static final int SEND_FILE_PORT = 58000;
	public static final int REC_MESSAGE_PORT = 56000;
	public static final int SEND_MESSAGE_PORT = 57000;

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
		 * This serversocket accepts connections for RECEIVING TRACKS FROM CLIENT,
		 * PORT: 55000
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket fileServer = new ServerSocket(REC_FILE_PORT, 1000)) {
					while (true) {
						final Socket client = fileServer.accept();
						new Thread(new Runnable() {
							public void run() {
								byte[] buffer = new byte[BUFFER_SIZE];
								File loc = new File(fileLocation);
								if (!loc.exists()) {
									loc.mkdirs();
								}
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
									serverHandler.handleClientFile(file, client.getInetAddress().getHostAddress());
								} catch (IOException e) {
									e.printStackTrace();
								}
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
				try (final ServerSocket communication = new ServerSocket(REC_MESSAGE_PORT)) {
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
									serverHandler.handleClientMessage(message, client.getInetAddress());
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
				try (final ServerSocket propertySendServer = new ServerSocket(SEND_MESSAGE_PORT)) {
					while (true) {
						communicationClients.add(propertySendServer.accept());
					}
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
				try (final ServerSocket propertySendServer = new ServerSocket(SEND_FILE_PORT)) {
					while (true) {
						propertySendClients.add(propertySendServer.accept());
					}
				} catch (IOException e) {
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
							communicationClients.remove(client);
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
							int count = 0;
							while ((count = in.read(buffer)) >= 0) {
								out.write(buffer, 0, count);
							}
							out.close();
							in.close();
							propertySendClients.remove(client);
							client.close();
						} catch (SocketException se) {
							propertySendClients.remove(client);
						} catch (IOException e) {
							propertySendClients.remove(client);
						}

					}
				}
			}).start();
		}
	}

	public void sendFile(final File file, final InetAddress specClient) {
		if (!propertySendClients.isEmpty()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					byte[] buffer = new byte[BUFFER_SIZE];
					Socket client = null;
					for (Socket s : propertySendClients) {
						if (s.getInetAddress().equals(specClient))
							client = s;
					}
					try {
						BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
						FileInputStream in = new FileInputStream(file);
						int count = 0;
						while ((count = in.read(buffer)) >= 0) {
							out.write(buffer, 0, count);
						}
						out.close();
						in.close();
						propertySendClients.remove(client);
						client.close();
					} catch (SocketException se) {
						propertySendClients.remove(client);
					} catch (IOException e) {
						propertySendClients.remove(client);
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
