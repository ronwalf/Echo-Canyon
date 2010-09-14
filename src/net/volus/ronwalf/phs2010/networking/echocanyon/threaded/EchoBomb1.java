package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import net.volus.ronwalf.phs2010.networking.echocanyon.MessageGenerator;

public class EchoBomb1 {

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
		Writer writer = new OutputStreamWriter( socket.getOutputStream() );
		
		for (String msg : gen) {
			writer.write(msg + "\r\n");
			String recv = reader.readLine();
			if (!recv.endsWith(msg)) {
				System.err.println("Bad server response!");
				System.exit(1);
			}
		}
		
		long end_time = System.currentTimeMillis();
		long elapsed = end_time - start_time;
		double rate = ((double) bmesgs) / ((double) elapsed);
		
		System.out.println( "Elapsed time: " + elapsed );
		System.out.println( "Message rate: " + rate );
		
	}
}
