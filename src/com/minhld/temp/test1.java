package com.minhld.temp;

public class test1 extends Thread {
	public void run() {
		System.out.println("start");
	}
	
	@Override
	public void destroy() {
		System.out.println("destroy");
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		System.out.println("interrupt");
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("finalize");
	}
	
	
	
	public static void main(String args[]) {
		new test1().start();
	}
}
