package server;

import java.io.File;
import java.util.Observable;

import lanplayer.LanData;
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
		message = message.trim();
		System.out.println("Server: Received message: " + message);
		if(ClientHandler.MSG_REQ_PROPERTY.equals(message)) {
			handlePropertyFileReq();
		}
		
	}
		
	private void handlePropertyFileReq() {
		server.sendFile(ServerGui.LAN_DATA_FILE);
	}
	
}
