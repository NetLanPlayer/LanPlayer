package server;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import client.TrackSender;

public class TestFileSend {
	public static void main(String[] args) throws InterruptedException{
		FileReceiver re = new FileReceiver("C:/test/");
		try {
			TrackSender se = new TrackSender("localhost");
			
			re.sendFile(new File("E:/nwp/src/a.mp3"));
			
			Thread.currentThread().sleep(1000);
			
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("try to send");
		re.sendFile(new File("E:/nwp/src/a.mp3"));

	}

}
