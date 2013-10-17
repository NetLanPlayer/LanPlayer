package utilities;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MyIp {
	
	public static String getMyIP() {
	   
		String ip;
	    try {
	        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;

	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            InetAddress addr = addresses.nextElement();
                ip = addr.getHostAddress();
                return ip;
//	            while(addresses.hasMoreElements()) {
//	                InetAddress addr = addresses.nextElement();
//	                ip = addr.getHostAddress();
//	                //System.out.println(iface.getDisplayName() + " " + ip);
//	            }
	        }
	    } catch (SocketException e) {
	        throw new RuntimeException(e);
	    }
	    
	    return "127.0.0.1";
	}
	
}
