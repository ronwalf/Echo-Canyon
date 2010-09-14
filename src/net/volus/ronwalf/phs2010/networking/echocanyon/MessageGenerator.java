package net.volus.ronwalf.phs2010.networking.echocanyon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageGenerator {

	private final List<String> messages;
	int count;
	
	public MessageGenerator() {
		messages = new ArrayList<String>(Arrays.asList(
				"This is the default message.",
				"It tells me what to say.",
				"It must have been some great sage",
				"who wrote me up this way."
		));
		count = 0;
	}
	
	public String next() {
		
		String msg  ="(" + count + ") " + messages.get( count++ % messages.size() );
		
		return msg;
	}
	
}
