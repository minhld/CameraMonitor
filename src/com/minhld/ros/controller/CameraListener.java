package com.minhld.ros.controller;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

public class CameraListener extends AbstractNodeMain {
	private String subscriberName;
	private String topicTitle;
	
	public CameraListener(String subscriberName, String topicTitle) {
		this.subscriberName = subscriberName;
		this.topicTitle = topicTitle;
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
				
				System.out.println("velocity: " + image.getWidth() + "," + image.getHeight());
			}
		});

	}

}
