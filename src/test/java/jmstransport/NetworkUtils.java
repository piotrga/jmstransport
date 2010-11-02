package jmstransport;

import java.io.IOException;
import java.net.ServerSocket;

public class NetworkUtils {
	public static int getFreePort() throws IOException {
		ServerSocket serverSocket = new ServerSocket(0);
		int port = serverSocket.getLocalPort();
		serverSocket.close();
		return port;
	}
}
