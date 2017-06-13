package com.minhld.ros.controller;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class MoveInstructor {
	public static final int MOVE_STOP = 0;
	public static final int MOVE_FORWARD = 1;
	public static final int MOVE_BACKWARD = 2;
	public static final int MOVE_LEFT = 3;
	public static final int MOVE_RIGHT = 4;
	public static final int MOVE_SEARCH = 100;
	
	public static final int CENTER_MARGIN = 50;
	
	public static int instruct(Rect orgImg, Rect pad) {
		return MOVE_STOP;
	}
	
	public static int instruct(int orgImgWidth, Point padTopLeft, Point padBottomRight) {
		int avgX = (int) (padTopLeft.x + padBottomRight.x) / 2;
		int centerX = orgImgWidth / 2;
		if (padTopLeft.x == 0 && padTopLeft.y == 0) {
			// pad has yet to be found
			return MOVE_SEARCH;
		} else if (avgX <= centerX - CENTER_MARGIN) {
			return MOVE_LEFT;
		} else if (avgX > centerX - CENTER_MARGIN && avgX < centerX + CENTER_MARGIN) {
			return MOVE_FORWARD;
		} else if (avgX >= centerX + CENTER_MARGIN) {
			return MOVE_RIGHT;
		}
		
		return MOVE_STOP;
	}
}
