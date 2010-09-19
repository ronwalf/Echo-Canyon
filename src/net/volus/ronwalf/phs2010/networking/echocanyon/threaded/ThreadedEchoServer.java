package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadedEchoServer implements Runnable {
	
	private static class Counter {
		private int count = 1;
		
		public synchronized int next() {
			return count++;
		}
	}
	
	private static class Sender implements Runnable {
		
		private final Socket socket;
		private final BlockingQueue<String> queue;
		
		public Sender( Socket socket ) {
			this.socket = socket;
			queue = new LinkedBlockingQueue<String>();
		}
		
		public void send(String msg) {
			try {
				queue.put(msg);
			} catch (InterruptedException e) {
				send(msg);
			}
		}
		
		public void finish() {
			send("");
		}
		
		public void run() {
			try { runIO(new BufferedWriter(
					new OutputStreamWriter( socket.getOutputStream() ) )); }
			catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void runIO(Writer writer) throws IOException {
			
			while (true) {
				String msg = null;
				try {
					msg = queue.take();
				} catch (InterruptedException e) {
					// Just loop again if it interrupted
					continue;
				}
				
				// Empty string to signal stop.
				if ("".equals(msg))
					break;
				
				writer.write(msg);
				writer.flush();
			}
		}
		
	}
	

	private final Socket socket;
	private final Counter counter;
	
	public ThreadedEchoServer(Socket client, Counter counter) {
		socket = client;
		this.counter = counter;
	}

	public void run() {
		try {
			readLines();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void readLines() throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));
		Sender sender = new Sender(socket);
		Thread senderT = new Thread(sender);
		senderT.start();

		try {
			String line = reader.readLine();
			while (line != null) {
				String msg ="Msg #" + counter.next() + ": " + line + "\r\n";
//				System.out.print(msg);
//				System.out.flush();
				sender.send(msg);
				line = reader.readLine();
			}
		} finally {
			sender.finish();
		}
	}
	
	public static void main(String args[]) throws IOException {
		Counter counter = new Counter();
		ServerSocket acceptor = new ServerSocket(new Integer(args[0]));
		
		while (true) {
			Socket client = acceptor.accept();
			new Thread(new ThreadedEchoServer(client, counter)).start();
		}
		
	}
}
