package com.minhld.ros.controller;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import com.minhld.utils.ROSUtils;

import geometry_msgs.Twist;
import sensor_msgs.Image;

public class CameraNode2 extends AbstractNodeMain {
	public static final double MOVE_STEP = 0.02;
	
	// public static String topicTitle = "/rrbot/camera1/image_raw";
	public static String topicTitle = "/camera/image_raw";
	
	private static Publisher<geometry_msgs.Twist> pub;
	
	private String subscriberName;
	private ImageListener listener;
	
	public CameraNode2(ImageListener listener) {
		this.subscriberName = ROSUtils.getNodeName(CameraNode2.topicTitle);
		this.listener = listener;
	}
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(this.subscriberName);
	}
	
	@Override
	public void onStart(ConnectedNode connectedNode) {
	    Subscriber<sensor_msgs.Image> subscriber = connectedNode.newSubscriber(CameraNode2.topicTitle, sensor_msgs.Image._TYPE);
	    subscriber.addMessageListener(new MessageListener<sensor_msgs.Image>() {
			@Override
			public void onNewMessage(sensor_msgs.Image image) {
				listener.imageArrived(image);
			}
		});

	    pub = connectedNode.newPublisher("cmd_vel", Twist._TYPE);
	    
	}
	
	static double cVel = 0, cRot = 0;
	
	public static void move(double vel, double rot) {
        Twist twist = pub.newMessage();
        twist.getLinear().setX(vel);
        twist.getAngular().setZ(rot);
        pub.publish(twist);
    }
	
	public static void moveForward(double vel) {
		// increase to expected speed
		if (vel > 0 && cVel < vel) {
			cVel += MOVE_STEP;
		}
		
		// slow down rotating -> 0
		if (cRot < 0) {
			cRot += MOVE_STEP;
		} else if (cRot > 0) {
			cRot -= MOVE_STEP;
		}
		
		move(cVel, cRot);
	}
	
	public static void moveBackward(double vel) {
		// increase to expected speed
		if (vel < 0 && cVel > vel) {
			cVel -= MOVE_STEP;
		}
		
		// slow down rotating -> 0
		if (cRot < 0) {
			cRot += MOVE_STEP;
		} else if (cRot > 0) {
			cRot -= MOVE_STEP;
		}
		
		move(cVel, cRot);
	}
	
	public static void moveLeft(double rot) {
		// slow down moving back and forth
		if (cVel < 0) {
			cVel += MOVE_STEP;
		} else if (cVel > 0) {
			cVel -= MOVE_STEP;
		}
		
		// increase to expected speed
		if (rot > 0 && cRot < rot) {
			cRot += MOVE_STEP;
		}
		
		move(cVel, cRot);
	}

	public static void moveRight(double rot) {
		// slow down moving back and forth
		if (cVel < 0) {
			cVel += MOVE_STEP;
		} else if (cVel > 0) {
			cVel -= MOVE_STEP;
		}
		
		// increase to expected speed
		if (rot < 0 && cRot > rot) {
			cRot -= MOVE_STEP;
		}
		
		move(cVel, cRot);
	}

	
	public interface ImageListener {
		public void imageArrived(Image bImage);
	}
}
