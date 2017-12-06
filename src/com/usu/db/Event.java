package com.usu.db;

import java.util.Date;

public class Event {
	public enum Type {
		Nothing,	// light off and no moving
		Moving,		// moving (doesn't care about light)
		LightOn,	// light on, no move
		LightOff,	// light off, no move
	}
	
	public long currentTime;
	public long time;
	public String type;
	public String info;
	
	public Event(Type type) {
		this.currentTime = System.currentTimeMillis();
		this.time = new Date().getTime();
		this.type = type.toString();
		this.info = "";
	}
	
	public Event(Type type, String info) {
		this.currentTime = System.currentTimeMillis();
		this.time = new Date().getTime();
		this.type = type.toString();
		this.info = info;
	}
	
	/**
	 * returns the enum type
	 * @return
	 */
	public Type getType() {
		return Type.valueOf(this.type);
	}
}
