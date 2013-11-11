package server;

import java.io.File;
import java.net.Socket;
import java.util.Observable;

import lanplayer.LanData;
import main.ServerGui;
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

	public void handleClientMessage(String message, Socket client) {
		message = message.trim();
		if(message == null || message.isEmpty()) return;
		System.out.println("Server: Received message: " + message);
		
		if(message.startsWith(ClientHandler.MSG_REQ_PROPERTY)) {
			if(client == null) return;
			server.sendFile(ServerGui.LAN_DATA_FILE, client.getInetAddress());
		}
		else if(message.startsWith(ClientHandler.MSG_REQ_SKIP)) {
			String posStr = LanData.getValue(LanData.POS_TAG, message);
			String ip = LanData.getValue(LanData.IP_TAG, message);
			Integer position = null;
			try {
				position = Integer.parseInt(posStr);
			}
			catch(NumberFormatException nfe) {
			}
			
			if(position == null || ip == null || ip.isEmpty()) return;
			
			setChanged();
			notifyObservers(new SkipMessage(position, ip));
		}
		else if(message.startsWith(ClientHandler.MSG_REQ_RATING)) {
			String posStr = LanData.getValue(LanData.POS_TAG, message);
			String ipPlusRating = LanData.getValue(LanData.IP_TAG, message);
			Integer position = null;
			try {
				position = Integer.parseInt(posStr);
			}
			catch(NumberFormatException nfe) {
			}
			
			if(position == null || ipPlusRating == null || ipPlusRating.isEmpty()) return;
			setChanged();
			notifyObservers(new RatingMessage(position, ipPlusRating));
		}
	}
				
}
