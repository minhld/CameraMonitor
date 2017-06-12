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

public class CameraNode extends AbstractNodeMain {
	public static String topicTitle = "/rrbot/camera1/image_raw";
	//public static String topicTitle = "/camera/image_raw";
	
	private static Publisher<geometry_msgs.Twist> pub;
	
	private String subscriberName;
	private ImageListener listener;
	
	public CameraNode(ImageListener listener) {
		this.subscriberName = ROSUtils.getNodeName(CameraNode.topicTitle);
		this.listener = listener;
	}
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(this.subscriberName);
	}
	
	@Override
	public void onStart(ConnectedNode connectedNode) {
	    Subscriber<sensor_msgs.Image> subscriber = connectedNode.newSubscriber(CameraNode.topicTitle, sensor_msgs.Image._TYPE);
	    subscriber.addMessageListener(new MessageListener<sensor_msgs.Image>() {
			@Override
			public void onNewMessage(sensor_msgs.Image image) {
				listener.imageArrived(image);
			}
		});

	    pub = connectedNode.newPublisher("cmd_vel", Twist._TYPE);
	    
	}
	
	public static void move(double vel, double rot) {
        Twist twist = pub.newMessage();
        twist.getLinear().setX(vel);
        twist.getAngular().setZ(rot);
        pub.publish(twist);
    }
	
	public interface ImageListener {
		public void imageArrived(Image bImage);
	}
}
