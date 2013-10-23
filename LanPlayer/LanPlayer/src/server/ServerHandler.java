package server;

import java.io.File;
import java.util.Observable;

import main.ServerGui;
import client.Client;
import client.ClientHandler;

public class ServerHandler extends Observable  {
	
	private Server server;
	
	public ServerHandler(Server server) {
		this.server = server;
	}
	
	public void handleClientFile(File file, String clientAddress) {
		setChanged();
		notifyObservers(new ReceivedFile(file, clientAddress));
	}

	public void handleClientMessage(String message) {
		if(message.equals(ClientHandler.MSG_REQ_PROPERTY)) {
			System.out.println("Server: Received Property file request");
			//handlePropertyFileReq();
		}
	}
	
	public void handlePropertyFileReq() {
		server.sendProperty(ServerGui.LAN_DATA_FILE);
	}
	
}
