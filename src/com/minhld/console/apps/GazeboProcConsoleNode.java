package com.minhld.console.apps;

import java.awt.image.BufferedImage;

import org.opencv.core.Core;

import com.minhld.ros.controller.CameraNode;
import com.minhld.ros.controller.OdomListener;
import com.minhld.ros.controller.OdomWriter;
import com.minhld.ros.movements.MoveInstructor;
import com.minhld.utils.AppUtils;
import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.ROSUtils;
import com.minhld.utils.Settings;

import geometry_msgs.Pose;
import geometry_msgs.Twist;
import nav_msgs.Odometry;
import sensor_msgs.Image;

public class GazeboProcConsoleNode extends Thread {
	
	Thread nodeThread;
	OdomWriter odomWriter;

	static String serverIP;
	boolean isServerInUsed = false;
	
	public void run() {
		// load settings
		Settings.init(Settings.SETTING_GAZ);
		
		// load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// initiate server
		initServer();
	}
	
	@Override
	public void destroy() {
    	// close all nodes 
    	ROSUtils.shutdownAllNodes();
    	
    	// clean variables and save properties
    	prepareCloseApp();
    	
    	System.exit(0);
	}
	
	/**
	 * call this to start listening to the ROS server
	 */
	private void startListening() {
	
		nodeThread = new Thread() {
			@Override
			public void run() {
				// this will be the name of the subscriber to this topic
				String graphCameraName = ROSUtils.getNodeName(CameraNode.topicTitle);
				
				ROSUtils.execute(graphCameraName, new CameraNode(new CameraNode.ImageListener() {
					@Override
					public void imageArrived(Image image) {
						// long start = System.currentTimeMillis();
						BufferedImage bImage = OpenCVUtils.getBufferedImage(image);
						// long loadImageTime = System.currentTimeMillis() - start;
							
						// long drawTime = System.currentTimeMillis() - start;
						// long rate = (long) (1000 / loadImageTime);
						// GazeboProcNode.this.processTimeLabel.setText("Displaying Time: " + drawTime + "ms | " +  
						// 								"Rate: " + rate + "fps");
						
					}
				}));

				// start Odometry listener
				String graphOdomName = ROSUtils.getNodeName(OdomListener.topicTitle);
				ROSUtils.execute(graphOdomName, new OdomListener(new OdomListener.OdomUpdater() {
					@Override
					public void odomUpdated(Odometry pos) {
						Pose p = pos.getPose().getPose();
						Twist t = pos.getTwist().getTwist();
						
						double x = p.getPosition().getX(), y = p.getPosition().getY();
						
						double yaw = p.getOrientation().getW();
						
						double angle = 0;
						angle = Math.acos((yaw + 1) / 2) * 360 / Math.PI;
						// if (yaw < 0) {
						// 	angle = 360 - angle;
						// }
						
						String xyz = "(X=" + AppUtils.getSmallNumberFormat(p.getPosition().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(p.getPosition().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(p.getPosition().getZ()) + ")"; 
						// String o = "(X=" + AppUtils.getSmallNumberFormat(p.getOrientation().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(p.getOrientation().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(p.getOrientation().getZ()) + ",W=" + AppUtils.getSmallNumberFormat(p.getOrientation().getW()) + ")";
						String o = "(X=" + AppUtils.getSmallNumberFormat(p.getOrientation().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(p.getOrientation().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(p.getOrientation().getZ()) + ",W=" + AppUtils.getSmallNumberFormat(p.getOrientation().getW()) + ",A=" + AppUtils.getSmallNumberFormat(angle) + ")";
						String l = "(X=" + AppUtils.getSmallNumberFormat(t.getLinear().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(t.getLinear().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(t.getLinear().getZ()) + ")";
						String a = "(X=" + AppUtils.getSmallNumberFormat(t.getAngular().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(t.getAngular().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(t.getAngular().getZ()) + ")";

						// // update location text
						// GazeboProcNode.this.topicInfoText.setText(
						//					"Location: \n" + xyz + "\n" + 
						//					"Orientation: \n" + o + "\n" +
						//					"Linear: \n" + l + "\n" + 
						//					"Angular: \n" + a);
						
						
						// publish to our location channel
						odomWriter.publish(pos);
					}
				}));
				
				// start the Movement Instructor
				String graphMoveName = ROSUtils.getNodeName(MoveInstructor.moveTopicTitle);
				ROSUtils.execute(graphMoveName, new MoveInstructor());

				// start the Odometry publisher
				odomWriter = new OdomWriter();
				// String odomTitle = ROSUtils.getNodeName(OdomWriter.topicTitle);
				ROSUtils.execute(OdomWriter.topicTitle, odomWriter);
			}
		};
		nodeThread.start();
		
		
	}
	
//	/**
//	 * this function is called when user presses on navigation buttons on keyboard
//	 * or uses mouse to click on navigation buttons on the application
//	 * 
//	 * @param keyCode
//	 */
//	private void navButtonClicked(int keyCode) {
//		// skip if server is not set
//		if (!this.isServerInUsed) return;
//		
//		// go otherwise
//		float actualVel = (float) Settings.velocity / 10;
//		String move = "";
//		switch (keyCode) {
//			case NavButtonClickListener.KEY_UP: {
//				// move up
//				MoveInstructor.move(actualVel, 0);
//				// MoveInstructor2.moveForward(actualVel);
//				move = "FORWARD";
//				break;
//			} case NavButtonClickListener.KEY_DOWN: {
//				// move down
//				MoveInstructor.move(-1 * actualVel, 0);
//				// MoveInstructor2.moveBackward(actualVel);
//				move = "BACKWARD";
//				break;
//			} case NavButtonClickListener.KEY_LEFT: {
//				// move left
//				MoveInstructor.move(0, actualVel);
//				// MoveInstructor2.moveLeft(actualVel);
//				move = "LEFT";
//				break;
//			} case NavButtonClickListener.KEY_RIGHT: {
//				// move right
//				MoveInstructor.move(0, -1 * actualVel);
//				// MoveInstructor2.moveRight(-1 * actualVel);
//				move = "RIGHT";
//				break;
//			}
//		}
//		// controlInfoText.setText("move: " + move + " | velocity: " + actualVel);
//	}
	
//	/**
//	 * 
//	 */
//	private void navButtonReleased() {
//		// skip if server is not set
//		if (!this.isServerInUsed) return;
//		
//		// go otherwise
//		MoveInstructor.move(0, 0);
//		
//		// controlInfoText.setText("move: STOP");
//	}
	
	/**
	 * call this when user wants to connect to a new server
	 */
	private void initServer() {
		try {
			// initiate server
			ROSUtils.startWithServer(serverIP);
			
			// start listening to the camera topic
			startListening();
			
			// update the controls & variables
			this.isServerInUsed = true;
		} catch (Exception e) {
			System.err.println("Error @ Server Initiation (" + e.getClass().getName() + ": " + e.getMessage() + ")");
		}
	}
	
//	/**
//	 * call this when user wants to disconnect from the current server
//	 */
//	private void stopServer() {
//		// disable current server 
//		ROSUtils.shutdownAllNodes();
//		
//		// update the controls & variables
//		this.isServerInUsed = false;
//	}
	
	/**
	 * this function is called to clean all the parameters
	 * as well as save to props file
	 */
	private void prepareCloseApp() {
		Settings.saveProps(Settings.SETTING_GAZ);
	}
	
//	/**
//	 * this class declares mouse pressed and released events for
//	 * the navigation buttons. 
//	 * 
//	 * @author lee
//	 *
//	 */
//	private class NavButtonClickListener {
//		public static final int KEY_UP = 38;
//		public static final int KEY_DOWN = 40;
//		public static final int KEY_LEFT = 37;
//		public static final int KEY_RIGHT = 39;
//		
//		private int keyCode;
//		
//		public NavButtonClickListener(int keyCode) {
//			this.keyCode = keyCode;
//		}
//	}
	
	/**
	 * entry point
	 * @param args
	 */
	public static void main(String args[]) {
		if (args.length == 0) {
			// get IP of the current computer
			// serverIP = "129.123.7.41";
			serverIP = AppUtils.getCurrentIP();
			ROSUtils.myIP = serverIP; 
		} else {
			serverIP = args[0];
		}
		new GazeboProcConsoleNode().start();
	}
}
