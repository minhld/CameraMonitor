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
	
	int count = 1;
	double maxX = -100, minX = 100, maxY = -100, minY = 100;
	
//	private void drawWheelchairPoint(double distance, double angle) {
//		Point wcPoint = FeatureExtractorRed.findPointByAngle(distance, angle);
//		if (count % 5 != 0) {
//			count++;
//			if (maxX < wcPoint.x) {
//				maxX = wcPoint.x;
//			}
//			if (minX > wcPoint.x) {
//				minX = wcPoint.x;
//			}
//			if (maxY < wcPoint.y) {
//				maxY = wcPoint.y;
//			}
//			if (minY > wcPoint.y) {
//				minY = wcPoint.y;
//			}
//		} else {
//			if (count < 50) {
//				double x = (minX + maxX) / 2;
//				double y = (minY + maxY) / 2;
//				double rad = Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxY - minY, 2));
//				LocationDrawer.updateData(new Point(x, y), rad / 2);
//				count++;
//			} else {
//				count = 1;
//				maxX = -100;
//				minX = 100; 
//				maxY = -100;
//				minY = 100;
//			}
//		}
//		
//	}
	
	public static GPSLocation getGPSLocation(Point[] points) {
		GPSLocation loc = new GPSLocation();
		
		
		
		return loc;
	}
}
