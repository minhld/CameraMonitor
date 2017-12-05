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
	public static void raiseEvent(int light, int contours) {
		if (contours == 0 && light == 0) {
			prev = new Event(Event.Type.Nothing);
		} else if (contours == 0 && light == 1) {
			Event e = new Event(Event.Type.LightOn);
			handle(e);
			prev = e;
		} else if (contours > 0) {
			Event e = new Event(Event.Type.Moving);
			handle(e);
			prev = e;
		}
	}
	
	private static void handle(Event e) {
		if (prev != null && prev.type == e.type) return;
		EventDb.addEvent(e);
	}
}
