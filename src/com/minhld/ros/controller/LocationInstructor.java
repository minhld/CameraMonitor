package com.minhld.ros.controller;

import org.opencv.core.Point;

/**
 * this class helps convert a bunch of random points into a single 
 * GPS location point with an error radius (the same way as Google Map) 
 * 
 * @author lee
 *
 */
public class LocationInstructor {
	
	public static class GPSLocation {
		public double radius;
		public Point center;
		public boolean hasData = false;
		
		public GPSLocation() {
			this.radius = 0;
			this.center = new Point(0, 0);
			hasData = false;
		}
		
		public GPSLocation(Point c, double r) {
			this.radius = r;
			this.center = c;
			hasData = true;
		}
	}
	
	static int count = 1;
	static double maxX = -100, minX = 100, maxY = -100, minY = 100;
	
	
	public static GPSLocation getGPSLocation(Point p) {
		GPSLocation loc = new GPSLocation();
		
		if (count % 5 != 0) {
			count++;
			if (maxX < p.x) {
				maxX = p.x;
			}
			if (minX > p.x) {
				minX = p.x;
			}
			if (maxY < p.y) {
				maxY = p.y;
			}
			if (minY > p.y) {
				minY = p.y;
			}
		} else {
			if (count < 50) {
				double x = (minX + maxX) / 2;
				double y = (minY + maxY) / 2;
				double rad = Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxY - minY, 2));
				loc = new GPSLocation(new Point(x, y), rad / 2);
				count++;
			} else {
				count = 1;
				maxX = -100;
				minX = 100; 
				maxY = -100;
				minY = 100;
			}
		}
		
		return loc;
	}
}
