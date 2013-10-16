package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FileReceiver {

	private List<Socket> communicationClients;
	private List<Socket> propertySendClients;
	private ExecutorService pool;
	private String fileLocation;
	private AtomicInteger nameCounter = new AtomicInteger(0);

	public FileReceiver(String mp3Location) {
		fileLocation = mp3Location;
		propertySendClients = Collections
				.synchronizedList(new ArrayList<Socket>());
		communicationClients = Collections
				.synchronizedList(new ArrayList<Socket>());
		pool = Executors.newCachedThreadPool();
		initServer();
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
								byte[] buffer = new byte[1024];
								File file = new File(fileLocation
										+ nameCounter.getAndIncrement()
										+ ".mp3");
								try (BufferedInputStream in = new BufferedInputStream(
										client.getInputStream(), 1024)) {
									FileOutputStream out = new FileOutputStream(
											file);
									while (in.read(buffer) != -1) {
										out.write(buffer);
										out.flush();
									}
									out.close();
									client.close();
								} catch (IOException e) {
									e.printStackTrace();
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
						final int clientIndex = communicationClients
								.indexOf(client);
						pool.submit(new Runnable() {
							@Override
							public void run() {
								while (true) {
									byte[] buffer = new byte[1024];
									try {
										BufferedInputStream in = new BufferedInputStream(
												communicationClients.get(
														clientIndex)
														.getInputStream());
										in.read(buffer);
										String message = new String(buffer);
										System.out.println(message);
										// TODO handleString(message);
									} catch (IOException e) {
										e.printStackTrace();
									}
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
		 * 
		 **/
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (final ServerSocket propertySendServer = new ServerSocket(
						57000)) {
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
						byte[] buffer = new byte[1024];
						try {
							BufferedOutputStream out = new BufferedOutputStream(
									communicationClients.get(i)
											.getOutputStream());
							int count = 0;
							for (byte b : message.getBytes())
								buffer[count++] = b;

							out.write(buffer);
							out.flush();
						} catch (IOException e) {
							e.printStackTrace();

						}

					}

				}

			}).start();
		}

	}

	public void sendFile(final File file) {
		for (final Socket client : propertySendClients) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
						byte[] buffer = new byte[1024];
						FileInputStream in = new FileInputStream(file);
						while (in.read(buffer) != -1) {
							out.write(buffer);
							out.flush();
						}
						in.close();
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
