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
		/*
		 * Receives Files from Client
		 */
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket fileServer = new ServerSocket(55000,
						1000)) {
					while (true) {
						final Socket client = fileServer.accept();
						pool.submit(new Runnable() {
							public void run() {
								byte[] buffer = new byte[BUFFER_SIZE];
								File file = new File(fileLocation + nameCounter.getAndIncrement() + ".mp3");
																
								try (BufferedInputStream in = new BufferedInputStream(client.getInputStream(), BUFFER_SIZE)) {
									FileOutputStream out = new FileOutputStream(file);
									while (in.read(buffer) >= 0) {
										out.write(buffer);
										//out.flush();
									}
									out.close();
									client.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								serverHandler.handleClientFile(file, client.getInetAddress().getHostAddress());
							}
						});
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

		/**
		 * Receives Messages from Client
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket communication = new ServerSocket(56000)) {
					while (true) {
						final Socket client = communication.accept();
						client.setKeepAlive(true);
						communicationClients.add(client);
						final int clientIndex = communicationClients.indexOf(client);
						pool.submit(new Runnable() {
							@Override
							public void run() {
								boolean stop = false;
								while (!stop) {
									//if(!communicationClients.get(clientIndex).isClosed()) {
										byte[] buffer = new byte[BUFFER_SIZE];
										try {
											BufferedInputStream in = new BufferedInputStream(communicationClients.get(clientIndex).getInputStream());
											in.read(buffer);
											String message = new String(buffer);
											
											//handle received message
											serverHandler.handleClientMessage(message);
										} catch (IOException e) {
											communicationClients.remove(clientIndex);
											stop = true;
										}
									//}
									//else {
									//	communicationClients.remove(clientIndex);
									//	stop = true;
									//}
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
		 * send file to client
		 * 
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket propertySendServer = new ServerSocket(57000)) {
					while (true) {
						final Socket client = propertySendServer.accept();
						client.setKeepAlive(true);
						propertySendClients.add(client);
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
					for (int i = 0; i < communicationClients.size(); i++) {
						byte[] buffer = new byte[BUFFER_SIZE];
						try {
							BufferedOutputStream out = new BufferedOutputStream(communicationClients.get(i).getOutputStream());
							int count = 0;
							for (byte b : message.getBytes()) {
								buffer[count++] = b;
							}
							out.write(buffer);
							//out.flush();
						} catch (IOException e) {
							e.printStackTrace();

						}

					}

				}

			}).start();
		}

	}

	public void sendProperty(final File file) {
		for (final Socket client : new ArrayList<Socket>(propertySendClients)) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println("Server: Starting to send from server");
						BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
						byte[] buffer = new byte[BUFFER_SIZE];
						FileInputStream in = new FileInputStream(file);
						int count = 0;
						while ((count = in.read(buffer)) >= 0) {
							System.out.println("Server: Sending file");
							out.write(buffer, 0, count);
							//out.flush();
						}
						in.close();
						propertySendClients.remove(client);
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

			});
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
		pool.shutdown();

	}
}
