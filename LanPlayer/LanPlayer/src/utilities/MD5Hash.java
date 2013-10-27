package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hash {

	/**
	 * Generates a MD5 checksum. !Note that lines in a file that start with '#' are skipped!
	 * @param file The file to generate checksum from.
	 * @return String checksum.
	 */
	public static String getChecksum(File file) {
		
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			if (file == null || !file.exists()) {
				return null;
			}

			byte[] b = new byte[(int) file.length()];
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String line = br.readLine();
				while(line != null && !line.startsWith("#")) {
					fis.read(b);	
				}
				br.close();
				fis.close();
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e1) {
				return null;
			}
			byte[] digest = md5.digest(b);
			StringBuffer strbuf = new StringBuffer();
			for (int i = 0; i < digest.length; i++) {
      			strbuf.append(toHexString(digest[i]));
			}
			return strbuf.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		
	}
	
	private static String toHexString(byte b) {
		int value = (b & 0x7F) + (b < 0 ? 128 : 0);
		String ret = (value < 16 ? "0" : "");
		ret += Integer.toHexString(value).toUpperCase();
		return ret;
	}
}
