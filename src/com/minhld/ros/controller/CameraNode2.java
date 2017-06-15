package com.minhld.ros.controller;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import sensor_msgs.Image;

import com.minhld.utils.ROSUtils;

/**
 * this class creates a node to listen to a camera over ROS server
 * 
 * @author lee
 *
 */
public class CameraNode2 extends AbstractNodeMain {
	// public static String topicTitle = "/rrbot/camera1/image_raw";
	public static String topicTitle = "/camera/image_raw";
	
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

	}
	

	public interface ImageListener {
		public void imageArrived(Image bImage);
	}
}
