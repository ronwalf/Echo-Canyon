package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;

public class ThreadedProxyServer {

	public static class ServerHandler implements Runnable {

		// Class members...
		Queue<ClientHandler> queue;
		// TODO: Reader and Writer for server socket
		// Maybe keep socket around, too?
		
		public ServerHandler(Socket socket) throws IOException {
			// Initialize members.
		}
		
		public void handleMessage( ClientHandler client, String msg ) throws IOException {
			synchronized(queue) {
				// add it to queue, send it
				// Always remember to flush (and lines end with \r\n)
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
		
		public ClientHandler(Socket socket, ServerHandler server) throws IOException {
			// TODO
		}
		
		public void handleMessage(String msg) {
			// Should be easy
			// Always remember to flush 
			// (plus, lines on the internet end with "\r\n")
		}
		
		public void run() {
			// loop while readLine() doesn't return null
			//  call server.handleMessage() with self and message
		}
		
	}
	
	public static void main(String... args) throws IOException, UnknownHostException {
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
			// close client socket if creating it caused an io exception
		}
	}
	
}
