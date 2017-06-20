package com.minhld.ros.controller;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import nav_msgs.Odometry;

public class OdomListener extends AbstractNodeMain {

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("wheelchair/odom");
	}
	
	@Override
	public void onStart(ConnectedNode connectedNode) {
	    Subscriber<Odometry> subscriber = connectedNode.newSubscriber("odom", Odometry._TYPE);
	    subscriber.addMessageListener(new MessageListener<Odometry>() {
			@Override
			public void onNewMessage(Odometry pos) {
				System.out.println("velocity: " + pos.getTwist().getTwist().getLinear().getX() + ", rotate: " + pos.getTwist().getTwist().getAngular().getZ());
			}
		});

	}

}
