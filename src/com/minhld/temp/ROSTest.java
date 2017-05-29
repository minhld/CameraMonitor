package com.minhld.temp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import com.minhld.ros.controller.OdomListener;

public class ROSTest extends Thread {
	
	private NodeMainExecutor executor = DefaultNodeMainExecutor.newDefault();
	
	public void run() {
//		// initiate ROS-core
//		rosCore = RosCore.newPublic("129.123.7.41", 11311);
//		rosCore.start();
//		
//		try {
//			rosCore.awaitStart();
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		execute("odomListener", new OdomListener());
		
	}
	
	private void execute(String name, NodeMain node) {
		NodeConfiguration config = NodeConfiguration.newPrivate();
	    try {
			config.setMasterUri(new URI("http://129.123.7.100:11311"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	    config.setNodeName(name);
	    executor.execute(node, config);
	}
	
	public static void main(String args[]) {
		new ROSTest().start();
	}
}
