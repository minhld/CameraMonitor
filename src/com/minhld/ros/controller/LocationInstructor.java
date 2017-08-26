package com.minhld.ros.controller;

import java.awt.Point;

/**
 * this class helps convert a bunch of random points into a single 
 * GPS location point with an error radius (the same way as Google Map) 
 * 
 * @author lee
 *
 */
public class LocationInstructor {
	
	public static class GPSLocation {
		public int radius;
		public Point center;
		
		public GPSLocation() {
			this.radius = 0;
			this.center = new Point(0, 0);
		}
	}
	
	public static GPSLocation getGPSLocation(Point[] points) {
		GPSLocation loc = new GPSLocation();
		
		return loc;
	}
}
