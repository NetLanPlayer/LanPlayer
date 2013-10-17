package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import server.Server;
import client.Client;

public class TestFileSend {
	public static void main(String[] args) throws InterruptedException{
		Server re = new Server("C:/test/");
		Properties prop = new Properties();
		prop.put("key", "hello");
		
		
		try {
			Client se = new Client("localhost");
			
			
			Thread.currentThread().sleep(1000);
			
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File("./src/tempmain");
		try {
			prop.store(new FileOutputStream(file)," comments");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		re.sendFile(file);
		re.sendFile(file);
		re.sendFile(file);
//		re.sendFile(file);
//		re.sendFile(file);
//		re.sendFile(file);
//		re.sendFile(file);

	}

}
