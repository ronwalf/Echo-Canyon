/*
Copyright (c) 2010 Ron Alford <ronwalf@volus.net>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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
			// Gotta flush when you're done - Java might just sit around waiting for more data.
			writer.flush();
			String recv = reader.readLine();
			if (!recv.endsWith(msg)) {
				System.err.println("Bad server response!");
				System.exit(1);
			}
		}
		
		// Close the socket
		socket.close();
		
		long end_time = System.currentTimeMillis();
		long elapsed = end_time - start_time;
		long rate = 1000 * bmesgs / elapsed;
		
		System.out.println( "Elapsed time: " + elapsed + " millis");
		System.out.println( "Message rate: " + rate + " msgs/sec" );
		
	}
}
