package com.minhld.ros.controller;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import org.opencv.core.Point;

/**
 * this class helps convert a bunch of random points into a single 
 * GPS location point with an error radius (the same way as Google Map) 
 * 
 * @author lee
 *
 */
public class LocationInstructor {
	public static final int REPEATED_COUNT = 20;  
	
	/**
	 * holds a GPS location with a center point and radius
	 *  
	 * @author lee
	 *
	 */
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
	
	static Queue<Point> queue = new LinkedList<Point>();
	
	/**
	 * finds the GPS style location from the list of the point
	 * 
	 * @param p
	 * @return
	 */
	public static GPSLocation getGPSLocation(Point p) {
		GPSLocation loc = new GPSLocation();
		
		if (queue.size() < LocationInstructor.REPEATED_COUNT) {
			queue.add(p);
		} else {
			queue.poll();
			queue.add(p);
			double[] b = getBoundary();
			double x = (b[0] + b[1]) / 2;
			double y = (b[2] + b[3]) / 2;
			double rad = Math.sqrt(Math.pow(b[1] - b[0], 2) + Math.pow(b[3] - b[2], 2));
			loc = new GPSLocation(new Point(x, y), rad / 2);
		}
		
		return loc;
	}
	
	/**
	 * finds the rectangle boundary of all the points in the queue
	 * 
	 * @return
	 */
	public static double[] getBoundary() {
		double maxX = -100, minX = 100, maxY = -100, minY = 100;
		
		Iterator<Point> pList = queue.iterator();
		while (pList.hasNext()) {
			Point p = pList.next();
			
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
		}
		
		return new double[] { minX, maxX, minY, maxY };
	}
}
