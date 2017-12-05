package com.usu.db;

import java.util.Date;

public class Event {
	public enum Type {
		Motion,
		Light
	}
	
	public Date time;
	public Type type;
	public String info;
}
