package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;

public class ThreadedProxyServer {

	public static class ServerHandler implements Runnable {

		// Class members...
		Queue<ClientHandler> queue;
		// TODO: Reader and Writer for server socket
		// Maybe keep socket around, too?
		
		public ServerHandler(Socket socket) {
			// Initialize members.
		}
		
		public void handleMessage( ClientHandler client, String msg ) throws IOException {
			synchronized(queue) {
				// add it to queue, send it
			}
		}
		
		public void run() {
			// loop ... 
			while(true) {
				// Read a line from the server
				// Need to access queue in thread safe manner!
				ClientHandler handler;
				synchronized(queue) {
					handler = queue.remove();
				}
				// Send message to client
			}
		}
		

		
	}
	
	
	public static class ClientHandler implements Runnable {
		
		// TODO class members:
		// Reader, writer, server handler
		
		public ClientHandler(Socket socket, ServerHandler server) {
			// TODO
		}
		
		public void handleMessage(String msg) {
			// Should be easy
		}
		
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String... args) {
		int localPort = Integer.parseInt(args[0]);
		String serverHost = args[1];
		int remotePort = Integer.parseInt(args[2]);
		
		// Connect socket to server
		
		// Create ServerHandler
		// Start ServerHandler thread: new Thread(server).start()
		
		// Create ServerSocket bound to localPort
		
		// Loop, accepting connections
		while (true) {
			// accept connection
			// create ClientHandler object with connection and server
			// start client handler thread.
		}
	}
	
}
