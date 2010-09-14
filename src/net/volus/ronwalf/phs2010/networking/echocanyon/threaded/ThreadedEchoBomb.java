package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.volus.ronwalf.phs2010.networking.echocanyon.MessageGenerator;

public class ThreadedEchoBomb implements Runnable {
	
	
	public class EchoReceiver implements Runnable {

		private BufferedReader reader;
		public EchoReceiver( BufferedReader reader ) {
			this.reader = reader;
		}
		
		public void run() {
			try {
				String msg = reader.readLine();
				String expected = queue.take();
				if (!msg.endsWith(expected)) {
					System.err.println("BAD MESSAGE: \n" + msg + 
							"\nExpected:\n" + expected);
				}
				System.out.println(reader.readLine());
			} catch (Exception e) {
				
			}
		}
		
	}
	
	private final String host;
	private final int port;
	private final MessageGenerator gen;
	
	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
	public ThreadedEchoBomb(String host, int port, MessageGenerator gen) {
		this.host = host;
		this.port = port;
		this.gen = gen;
	}
	
	public void run() {
		try {
			connectAndRun();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void connectAndRun() throws IOException, InterruptedException {
		Socket socket = new Socket(host, port);
		
		BufferedReader reader = new BufferedReader(
			new InputStreamReader( socket.getInputStream() ) );
		
		new Thread(new EchoReceiver( reader ) ).start();
		
		Writer writer = new OutputStreamWriter(
				socket.getOutputStream() );
		
		
		while (true) {
			String msg = gen.next();
			queue.put(msg);
			writer.write(gen.next());
		}
			
	}
	
	public static void main(String[] args) throws IOException {
	
			String host = args[0];
			int port = new Integer(args[1]);
			
			final int bthreads = new Integer(args[2]);
			final int bmesgs = new Integer(args[3]);
			final MessageGenerator gen = new MessageGenerator(bmesgs);
			
			for (int i = 0; i < bthreads; i++) {
				ThreadedEchoBomb client = new ThreadedEchoBomb(host, port, gen);
				new Thread(client).start();
			}
			
	}
	
}
