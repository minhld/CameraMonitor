package com.minhld.ros.movements;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import com.minhld.utils.ROSUtils;

import geometry_msgs.Twist;

/**
 * this class provides movement instruction to the object
 * via ROS server.  
 * 
 * @author lee
 *
 */
public class MoveInstructor extends AbstractNodeMain {
	public static final double MOVE_STEP = 0.05;

	public static final int MOVE_STOP = 0;
	public static final int MOVE_FORWARD = 1;
	public static final int MOVE_BACKWARD = 2;
	public static final int MOVE_LEFT = 3;
	public static final int MOVE_RIGHT = 4;
	public static final int MOVE_SEARCH = 100;
	public static final int CENTER_MARGIN = 50;
	
	public static final String moveTopicTitle = "/cmd_vel";

	
	private static Publisher<geometry_msgs.Twist> pub;
	
	private String publisherName;

	static double cVel = 0, cRot = 0;
	
	public MoveInstructor() {
		this.publisherName = ROSUtils.getNodeName(MoveInstructor.moveTopicTitle);
	}

	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(this.publisherName);
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		pub = connectedNode.newPublisher("cmd_vel", Twist._TYPE);
	}
	
	public static int instruct(Rect orgImg, Rect pad) {
		return MOVE_STOP;
	}
	
	public static void move(double vel, double rot) {
        Twist twist = pub.newMessage();
        twist.getLinear().setX(vel);
        twist.getAngular().setZ(rot);
        pub.publish(twist);
    }
	
	public static void moveForward(double vel, double distance) {
        Twist twist = pub.newMessage();
        twist.getLinear().setX(vel);
        twist.getAngular().setZ(0);
        pub.publish(twist);
        
        // estimate time to move
        try {
        	long sleepTime = (long) (distance * 1000 / (vel * 3.5));
        	Thread.sleep(sleepTime);
        } catch (Exception e) { }
        
        // then stop
        move(0, 0);
    }
	
//	public static void moveForward(double vel) {
//		// increase to expected speed
//		if (vel > 0 && cVel < vel) {
//			cVel += MOVE_STEP;
//		}
//		
//		// slow down rotating -> 0
//		if (cRot < 0) {
//			cRot += MOVE_STEP;
//		} else if (cRot > 0) {
//			cRot -= MOVE_STEP;
//		}
//		
//		move(cVel, cRot);
//	}
//	
//	public static void moveBackward(double vel) {
//		// increase to expected speed
//		if (vel < 0 && cVel > vel) {
//			cVel -= MOVE_STEP;
//		}
//		
//		// slow down rotating -> 0
//		if (cRot < 0) {
//			cRot += MOVE_STEP;
//		} else if (cRot > 0) {
//			cRot -= MOVE_STEP;
//		}
//		
//		move(cVel, cRot);
//	}
//	
//	public static void moveLeft(double rot) {
//		// slow down moving back and forth
//		if (cVel < 0) {
//			cVel += MOVE_STEP;
//		} else if (cVel > 0) {
//			cVel -= MOVE_STEP;
//		}
//		
//		// increase to expected speed
//		if (rot > 0 && cRot < rot) {
//			cRot += MOVE_STEP;
//		}
//		
//		move(cVel, cRot);
//	}
//
//	public static void moveRight(double rot) {
//		// slow down moving back and forth
//		if (cVel < 0) {
//			cVel += MOVE_STEP;
//		} else if (cVel > 0) {
//			cVel -= MOVE_STEP;
//		}
//		
//		// increase to expected speed
//		if (rot < 0 && cRot > rot) {
//			cRot -= MOVE_STEP;
//		}
//		
//		move(cVel, cRot);
//	}
	
	public static int instruct(int orgImgWidth, Rect objectRect) {
		int avgX = objectRect.x + (int) (objectRect.width / 2);
		int centerX = orgImgWidth / 2;
		if (objectRect.x == 0 && objectRect.y == 0) {
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
