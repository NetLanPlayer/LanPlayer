package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import client.TrackSender;

public class TestFileSend {
	public static void main(String[] args) throws InterruptedException{
		FileReceiver re = new FileReceiver("C:/test/");
		Properties prop = new Properties();
		prop.put("key", "hello");
		
		
		try {
			TrackSender se = new TrackSender("localhost");
			
			
			Thread.currentThread().sleep(1000);
			
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("try to send");
		File file = new File("./src/tempmain");
		try {
			prop.store(new FileOutputStream(file)," comments");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		re.sendFile(file);
		re.sendFile(file);
		re.sendFile(file);
		re.sendFile(file);
		re.sendFile(file);
		re.sendFile(file);
		re.sendFile(file);

	}

}
