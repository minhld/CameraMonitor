package com.minhld.ros.controller;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

public class WheelVelocityListener extends AbstractNodeMain {
	public static String topicTitle = "/wheelVel";
	public static int velocity = 0;
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("wheelchair/wheel_velocity");
	}
	
	@Override
	public void onStart(ConnectedNode connectedNode) {
	    Subscriber<std_msgs.Int16> subscriber = connectedNode.newSubscriber(topicTitle, std_msgs.Int16._TYPE);
	    subscriber.addMessageListener(new MessageListener<std_msgs.Int16>() {
			@Override
			public void onNewMessage(std_msgs.Int16 pos) {
				WheelVelocityListener.velocity = (int) pos.getData();
			}
		});

	}
}
