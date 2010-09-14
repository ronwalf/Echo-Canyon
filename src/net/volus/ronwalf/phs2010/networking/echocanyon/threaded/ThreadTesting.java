package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;

public class ThreadTesting implements Runnable {
	
	
	private static int count = 0;
	
	private int id;
	
	public ThreadTesting(int id) {
		this.id = id;
	}
	
	public void run() {
		for (int i = 0; i < 1000; i++) {
			int cval = count;
			System.out.println("(" + id + ") Count is: " + cval);
			count = cval + 1;
		}
	}
	
	
	public static void main(String args[]) throws InterruptedException {
		Thread thread1 = new Thread(new ThreadTesting(0));
		Thread thread2 = new Thread(new ThreadTesting(1));
		Thread thread3 = new Thread(new ThreadTesting(2));
		
		thread1.start();
		thread2.start();
		thread3.start();
		
		thread1.join();
		thread2.join();
		thread3.join();
		
		System.out.println("Final count: " + count);
	}

}
