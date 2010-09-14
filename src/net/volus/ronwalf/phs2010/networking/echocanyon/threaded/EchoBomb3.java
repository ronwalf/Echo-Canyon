package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.volus.ronwalf.phs2010.networking.echocanyon.MessageGenerator;

public class EchoBomb3 {

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
	
	public static class Bomber implements Runnable {
		
		final private int id;
		final private String host;
		final private int port;
		final private MessageGenerator gen;
		
		public Bomber(int id, String host, int port, MessageGenerator gen) {
			this.id = id;
			this.host = host;
			this.port = port;
			this.gen = gen;
		}
		
		public void run() {
			try {
				runIO();
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		public void runIO() throws IOException, UnknownHostException {
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
			// Because gen implements Iterator, which has two calls:
			// hasNext() and next()
			// We can't block other threads from accessing gen easily with 
			// the iterator syntax, so we might get hasNext() == true, 
			// but then next() returns null!
			//
			// So here we just use gen.next() and check for null
			String msg = gen.next();
			while( msg != null ) {
				// Add our ID
				msg = "(" + id + ") " + msg;
				recv.expect( msg );
				writer.write(msg + "\r\n");
				msg = gen.next();
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
			
			socket.close();
			
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		final String host = args[0];
		final int port = new Integer(args[1]);
		final int bmesgs = new Integer(args[2]);
		final int threadCount = new Integer(args[3]);
		
		// Initialized message generator to generate a fixed number of messages.
		final MessageGenerator gen = new MessageGenerator(bmesgs);
		
		long start_time = System.currentTimeMillis();
		
		// Start up a new thread for each bomber.
		List<Thread> threads = new ArrayList<Thread>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(new Bomber( i, host, port, gen ));
			t.start();
			threads.add(t);
		}
		
		// Join threads as they finish.
		for (Thread t : threads) {
			while (true) {
				try {
					t.join();
					break;
				} catch (InterruptedException e) {
				}
			}
		}
		
		
		long end_time = System.currentTimeMillis();
		long elapsed = end_time - start_time;
		long rate = 1000 * bmesgs / elapsed;
		
		System.out.println( "Elapsed time: " + elapsed + " millis");
		System.out.println( "Message rate: " + rate + " msgs/sec");
		
	}
}
