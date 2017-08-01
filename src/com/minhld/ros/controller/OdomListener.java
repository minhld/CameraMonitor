package com.minhld.ros.controller;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import nav_msgs.Odometry;
import sensor_msgs.Image;

public class OdomListener extends AbstractNodeMain {
	public static final String topicTitle = "/odom";

	private OdomUpdater listener;
	
	public OdomListener(OdomUpdater listener) {
		this.listener = listener;
	}
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("wheelchair/odom");
	}
	
	@Override
	public void onStart(ConnectedNode connectedNode) {
	    Subscriber<Odometry> subscriber = connectedNode.newSubscriber(topicTitle, Odometry._TYPE);
	    subscriber.addMessageListener(new MessageListener<Odometry>() {
			@Override
			public void onNewMessage(Odometry pos) {
				OdomListener.this.listener.odomUpdated(pos);
			}
		});

	}

	public interface OdomUpdater {
		public void odomUpdated(Odometry pos);
	}

}
