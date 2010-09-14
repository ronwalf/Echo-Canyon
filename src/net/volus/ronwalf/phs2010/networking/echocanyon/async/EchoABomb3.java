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
package net.volus.ronwalf.phs2010.networking.echocanyon.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

import net.volus.ronwalf.phs2010.networking.echocanyon.MessageGenerator;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * EchoABomb1:
 * Mimic EchoBomb2 (better than EchoABomb2, anyway) - 
 * send as many lines as possible, wait until all arrived.
 * @author ronwalf
 */
public class EchoABomb3 extends IoHandlerAdapter {

	private final MessageGenerator gen;
	private final long start_time;
	
	private final Queue<String> expectedQueue;
	
	
	public EchoABomb3(MessageGenerator gen) {
		super();
		this.gen = gen;
		this.expectedQueue = new LinkedList<String>();
		start_time = System.currentTimeMillis();
	}
	

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		System.exit(1);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		// It's really a string.  That's what the TextLineCodec factory does for us.
		String msg = message.toString();
		String expected = expectedQueue.remove();
		if (!msg.endsWith(expected)) {
			throw new Exception("Bad server response!\n" +
					"Expected: " + expected +
					"\nReceived: " + msg);
		}
		
		// Close session if we're done.
		if (!gen.hasNext() && expectedQueue.isEmpty()) {
			session.close(false);
			return;
		}
			

	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// Nothing to do... 
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {

		long end_time = System.currentTimeMillis();
		long elapsed = end_time - start_time;
		long rate = 1000 * gen.total / elapsed;
		
		System.out.println( "Elapsed time: " + elapsed + " millis");
		System.out.println( "Message rate: " + rate + " msgs/sec" );
		
		System.exit(0);
	}
	
	
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		
		for (String msg : gen) {
			expectedQueue.add(msg);
			session.write(msg);
		}
	}

	public static void main(String args[]) throws IOException {
		String host = args[0];
		int port = new Integer(args[1]);
		final int bmesgs = new Integer(args[2]);
		
		MessageGenerator gen = new MessageGenerator(bmesgs);
		
		
		IoConnector connector = new NioSocketConnector();

		connector.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new TextLineCodecFactory(Charset
						.forName("UTF-8"))));

		connector.setHandler(new EchoABomb3(gen));

		connector.getSessionConfig().setReadBufferSize(2048);
		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);
		
		connector.connect( new InetSocketAddress( host, port ) );
		
	}
	
}
