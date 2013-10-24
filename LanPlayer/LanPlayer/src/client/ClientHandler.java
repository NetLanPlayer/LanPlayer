package client;

import java.util.Observable;
import java.util.Properties;

public class ClientHandler extends Observable {

	public final static String MSG_REQ_PROPERTY = "Client request property file.";

	private Client client;
	
	public ClientHandler(Client client) {
		this.client = client;
	}
	
	public void handleServerMessage(String message) {
		System.out.println("Client: Received message from server: " + message);
	}
		
	public void handleServerProperty(Properties prop) {
		System.out.println("Client: Received property from server");
		setChanged();
		notifyObservers(prop);
	}
	
}
