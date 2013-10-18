package server;

import java.io.File;

public class ReceivedFile {

	private File file;
	private String ip;
	
	public File getFile() {
		return file;
	}

	public String getIp() {
		return ip;
	}
	
	public ReceivedFile(File file, String ip) {
		this.file = file;
		this.ip = ip;
	}
	
}
