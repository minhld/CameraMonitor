package com.usu.db;

import java.util.Date;

public class Event {
	public enum Type {
		Nothing,	// light off and no moving
		Moving,		// moving (doesn't care about light)
		LightOn,	// light on, no move
		LightOff,	// light off, no move
	}
	
	public Date time;
	public Type type;
	public String info;
	
	public Event(Type type) {
		this.time = new Date();
		this.type = type;
		this.info = "";
	}
	
	public Event(Type type, String info) {
		this.time = new Date();
		this.type = type;
		this.info = info;
	}
}
