package net.volus.ronwalf.phs2010.networking.echocanyon.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class MinaProxyServer {

	/**
	 * A handler for our connection to the server.
	 */
	public static class ServerHandler implements IoHandler {

		IoSession serverSession;
		Queue<IoSession> queue = new LinkedList<IoSession>();
		
		public void exceptionCaught(IoSession session, Throwable e)
				throws Exception {
			e.printStackTrace();
			System.exit(1);
		}

		public void messageReceived(IoSession session, Object arg)
				throws Exception {
			// Our message
			String msg = (String) arg;
			// pop message, send it away
			// Flushing is handled for you.
			// Synchronization not an issue (everything here executes in the same thread)
			// No blocking calls to worry about.
		}

		public void messageSent(IoSession session, Object arg1) throws Exception {
			// woo, you can tell when your message actually got sent
			// (remember, non-blocking means your session.write(...) is a request
			// to send.  It returns before the request is completed.
		}

		public void sessionClosed(IoSession session) throws Exception {
			// Whoops, someone closed our server connection!
			System.exit(1);
		}

		public void sessionCreated(IoSession session) throws Exception {
			// Don't worry about this one - if you want to know
			// when your handler is ready to receive data, override the
			// sessionOpened() method
		}

		public void sessionIdle(IoSession session, IdleStatus arg1)
				throws Exception {
			// count sheep?
		}

		public void sessionOpened(IoSession session) throws Exception {
			// We know that this will only be called once, since we only
			// ask the nio connector to connect once.
			serverSession = session;
		}
		
		public void handleMessage(IoSession client, String msg) {
			// TODO: Add message to queue, write msg to serverSession
		}
		
	}
	
	
	// For client, extend IoHandlerAdapter to hide the methods we
	// don't care about.  We could have done this for
	// ServerHandler, too.
	public static class ClientHandler extends IoHandlerAdapter {

		private final ServerHandler server;
		
		public ClientHandler(ServerHandler server) {
			this.server = server;
		}
		
		public void exceptionCaught(IoSession session, Throwable e)
				throws Exception {
			e.printStackTrace();
			session.close(false);
		}

		public void messageReceived(IoSession session, Object arg)
				throws Exception {
			// TextLine code factory ensures that arg is actually a string.
			String msg = (String) arg;
			//TODO: Call server!
		}
		
	}
	
	public static void main(String... args) throws IOException {
		int localPort = Integer.parseInt(args[0]);
		String serverHost = args[1];
		int remotePort = Integer.parseInt(args[2]);
		
		ServerHandler server = new ServerHandler();
		// Just because of the way Mina works, there's only one Client
		// handler for all the clients.  The IoSession given as a
		// parameter to each of its messages is what lets you 
		// distinguish between clients.
		ClientHandler clients = new ClientHandler(server);
		
		// Set up the filter chain so we can send and receive whole lines of text:
		DefaultIoFilterChainBuilder chainBuilder = new DefaultIoFilterChainBuilder();
		chainBuilder.addLast(
				"codec",
				new ProtocolCodecFilter(new TextLineCodecFactory(Charset
						.forName("UTF-8"))));
		
		// Set up the connection to the server
		SocketConnector connector = new NioSocketConnector();
		// Tell the connection to marshal reads and writes to and from Strings lines
		connector.setFilterChainBuilder(chainBuilder);
		
		// Set the handler
		connector.setHandler(server);
		
		// Connect! (then wait)
		ConnectFuture connection = connector.connect(new InetSocketAddress(serverHost, remotePort));
		if (!connection.awaitUninterruptibly().isConnected()) {
			System.err.println("Failed to connect to server!");
			connection.getException().printStackTrace();
			System.exit(1);
		}
		
		// Set up our client handlers
		SocketAcceptor acceptor = new NioSocketAcceptor();
		// Same thing w.r.t. lines as server connection
		acceptor.setFilterChainBuilder(chainBuilder);
		
		// Set the handler
		acceptor.setHandler(clients);
		
		// Bind! (and start listening)
		acceptor.bind(new InetSocketAddress(localPort));
		
	}
}
