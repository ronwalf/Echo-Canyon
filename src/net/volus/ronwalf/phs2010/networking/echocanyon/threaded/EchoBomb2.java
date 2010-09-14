package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.volus.ronwalf.phs2010.networking.echocanyon.MessageGenerator;

public class EchoBomb2 {

	private static class Receiver implements Runnable {
		
		private final BufferedReader reader;
		private final BlockingQueue<String> queue;
		
		public Receiver( BufferedReader reader ) {
			this.reader = reader;
			queue = new LinkedBlockingQueue<String>();
		}
		
		public void expect(String msg) {
			try {
				queue.put(msg);
			} catch (InterruptedException e) {
				expect(msg);
			}
		}
		
		public void finish() {
			expect("");
		}
		
		public void run() {
			while (true) {
				String expected = null;
				try {
					expected = queue.take();
				} catch (InterruptedException e) {
					// Just loop again if it interrupted
					continue;
				}
				
				// Empty string to signal stop.
				if ("".equals(expected))
					break;
				
				try {
					String msg = reader.readLine();
					if (!msg.endsWith(expected)) {
						throw new Exception("Invalid server response!\n" +
								"Got: " + msg + "\n" +
								"Expected: " + expected);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		String host = args[0];
		int port = new Integer(args[1]);
		
		final int bmesgs = new Integer(args[2]);
		
		// Initialized message generator to generate a fixed number of messages.
		final MessageGenerator gen = new MessageGenerator(bmesgs);
		
		long start_time = System.currentTimeMillis();
		
		// Connect and setup the input and output streams for reading text.
		Socket socket = new Socket(host, port);
		BufferedReader reader = new BufferedReader(new InputStreamReader( socket.getInputStream() ) );
		// Hey, it makes sense to use a buffered writer this time.
		// We can let Java efficiently manage how to group sends().
		Writer writer = new BufferedWriter(new OutputStreamWriter( socket.getOutputStream() ));
		
		// Our receiving thread.
		// We keep it around so we can watch when it ends -
		// necessary for printing statistics.
		Receiver recv = new Receiver( reader );
		Thread recvThread = new Thread(recv);
		recvThread.start();
		
		
		// Start looping through the messages, sending them as we go.
		// No need to
		for (String msg : gen) {
			recv.expect( msg );
			writer.write(msg + "\r\n");
		}
		// Gotta flush when you're done - Java might just sit around waiting for more data.
		writer.flush();
		
		// Signal recv that we're finished and wait until it returns.
		recv.finish();
		while (true) {
			try {
				recvThread.join();
				break;
			} catch (InterruptedException e) {
				//recvThread.interrupt();
			}
		}
		
		// Close the socket
		socket.close();
		
		long end_time = System.currentTimeMillis();
		long elapsed = end_time - start_time;
		long rate = 1000 * bmesgs / elapsed;
		
		System.out.println( "Elapsed time: " + elapsed + " millis");
		System.out.println( "Message rate: " + rate + " msgs/sec");
		
	}
}
