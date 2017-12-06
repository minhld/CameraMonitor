package com.usu.db;

public class EventHandler {
	static Event prev;
	
	public static void init(String dbIp) {
		EventDb.init(dbIp);
	}
	
	public static void close() {
		EventDb.close();
	}
	
	/**
	 * raise an event
	 * 
	 * @param light
	 * @param contours
	 */
	public static void raiseEvent(int contours) {
		if (!EventDb.isDbAvail()) return;
		
		Event e = new Event(Event.Type.Moving);
		handle(e);
		prev = e;

		/*
		if (contours == 0) {
			prev = new Event(Event.Type.Nothing);
		} else if (contours > 0) {
			Event e = new Event(Event.Type.Moving);
			handle(e);
			prev = e;
		}
		*/
	}
	
	private static void handle(Event e) {
		// if (prev != null && prev.type == e.type) return;
		
		// if the messages are in the same type and 
		// are consecutive, then it shouldn't be added
		if (prev != null && prev.type == e.type && 
				e.currentTime - prev.currentTime <= 1000) return;
		
		EventDb.addEvent(e);
	}
}
