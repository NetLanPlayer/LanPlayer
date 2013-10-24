package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Properties;

import main.ClientGui;

public class ClientHandler extends Observable {

	public final static String MSG_REQ_PROPERTY = "MSG_REQ_PROPERTY";

	private Client client;
	
	public ClientHandler(Client client) {
		this.client = client;
	}
	
	public void handleServerMessage(String message) {
		System.out.println("Client: Received message from server: " + message);
	}
		
	public void handleServerFile(File file) {
		System.out.println("Client: Received file from server");
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			prop.load(fis);
			fis.close();
			FileOutputStream fos = new FileOutputStream(ClientGui.LAN_DATA_FILE);
			prop.store(fos, "LAN DATA");
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setChanged();
		notifyObservers(prop);
	}
	
}
