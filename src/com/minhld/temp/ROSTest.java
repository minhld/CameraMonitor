package com.minhld.temp;

import java.net.URI;
import java.net.URISyntaxException;

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import com.minhld.utils.ROSUtils;

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
		
//		execute("odomListener", new OdomListener());
		String nodeName = ROSUtils.getTalkerName("/cmd_vel");
		VelocityTalker talker = new VelocityTalker(nodeName, "cmd_vel");
		execute(nodeName, talker);
		sleepA(1000);
		talker.move(0, 0.2);
	}
	
	
	
	private void execute(String name, NodeMain node) {
		NodeConfiguration config = NodeConfiguration.newPrivate();
	    try {
			config.setMasterUri(new URI("http://129.123.7.41:11311"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	    config.setNodeName(name);
	    executor.execute(node, config);
	}
	
	private void sleepA(long time) {
		try {
			sleep(time);
		} catch (Exception e) { }
	}
	
	public static void main(String args[]) {
		new ROSTest().start();
	}
}
