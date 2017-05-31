package com.minhld.ros.controller;
import java.awt.image.BufferedImage;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import com.minhld.utils.ROSUtils;

import sensor_msgs.Image;

public class CameraListener2 extends AbstractNodeMain {
	private String subscriberName;
	private String topicTitle;
	private ImageListener listener;
	
	public CameraListener2(String subscriberName, String topicTitle, ImageListener listener) {
		this.subscriberName = subscriberName;
		this.topicTitle = topicTitle;
		this.listener = listener;
	}
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(this.subscriberName);
	}
	
	@Override
	public void onStart(ConnectedNode connectedNode) {
	    Subscriber<sensor_msgs.Image> subscriber = connectedNode.newSubscriber(this.topicTitle, sensor_msgs.Image._TYPE);
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
