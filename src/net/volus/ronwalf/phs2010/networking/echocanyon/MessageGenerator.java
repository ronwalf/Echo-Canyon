package net.volus.ronwalf.phs2010.networking.echocanyon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MessageGenerator implements Iterable<String>, Iterator<String> {

	private final List<String> messages;
	public final int total;
	private int count;
	
	public MessageGenerator(int total) {
		messages = new ArrayList<String>(Arrays.asList(
				"This is the default message.",
				"It tells me what to say.",
				"It must have been some great sage",
				"who wrote me up this way."
		));
		count = 0;
		this.total = total;
	}
	
	public String next() {
		String msg = null;

		// Since no other method modifies count, we 
		// could have just used the synchronized keyword on the method, ie:
		// public synchronized String next() {...}
		synchronized (this) {
			if (count < total) {
				msg = "(" + count + ") "
						+ messages.get(count++ % messages.size());
			}
		}
		return msg;
	}

	public Iterator<String> iterator() {
		return this;
	}

	public boolean hasNext() {
		return count < total;
	}

	public void remove() {
		throw new UnsupportedOperationException("Cannot remove from MessageGenerator!");
	}

}
