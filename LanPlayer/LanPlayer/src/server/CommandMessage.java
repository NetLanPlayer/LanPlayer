package server;

public class CommandMessage {
	
	public static final String NEXT = "NEXT";
	public static final String SKIP = "SKIP";
	public static final String PLAY = "PLAY";
	
	private int position = 0;
	private String command = "";
	
	public int getPosition() {
		return position;
	}

	public String getCommand() {
		return command;
	}

	public CommandMessage(int position, String command) {
		this.position = position;
		this.command = command;
	}
	
	

}
