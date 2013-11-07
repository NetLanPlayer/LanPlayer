package utilities;

import java.io.File;
import java.io.IOException;

public class HideFile {
	public static void hide(File src) throws InterruptedException, IOException {
	    if(src == null || !src.exists()) return;
		// win32 command line variant
	    Process p = Runtime.getRuntime().exec("attrib +h " + src.getPath());
	    p.waitFor(); // p.waitFor() important, so that the file really appears as hidden immediately after function exit.
	}
}
