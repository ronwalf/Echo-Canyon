package net.volus.ronwalf.phs2010.networking.echocanyon.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class MinaEchoClient extends IoHandlerAdapter  {

	public static void main(String args[]) throws IOException {
		IoConnector connector = new NioSocketConnector();

		connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new TextLineCodecFactory(Charset
						.forName("UTF-8"))));

		connector.setHandler(new MinaEchoClient());

		connector.getSessionConfig().setReadBufferSize(2048);
		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);
		
		connector.connect( new InetSocketAddress( args[0], new Integer(args[1]).intValue()) );
	}
	private int counter = 1;
	
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		send(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		send(session);
	}
	
	public void send( IoSession session ) {
		session.write("Ron's Mina Async message number " + (counter++));
	}

	

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		cause.printStackTrace();
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		System.out.println( message.toString() );
	
	}
}
