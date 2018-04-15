package com.minhld.ui.apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.minhld.opencv.DistanceEstimator;
import com.minhld.opencv.FeatureExtractorRed;
import com.minhld.opencv.ObjectDetectorRed;
import com.minhld.ros.controller.CameraNode;
import com.minhld.ros.controller.LocationInstructor;
import com.minhld.ros.controller.LocationInstructor.GPSLocation;
import com.minhld.ros.movements.MoveInstructor;
import com.minhld.ros.controller.OdomWriter;
import com.minhld.ros.controller.UISupport;
import com.minhld.ros.controller.WheelVelocityListener;
import com.minhld.ui.supports.LocationDrawer;
import com.minhld.ui.supports.SettingsPanel;
import com.minhld.utils.AppUtils;
import com.minhld.utils.Constants;
import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.ROSUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;

/**
 * this is the controller running on the wheelchair which accepts two cameras for
 * the automatic movements. one front-camera to move the wheelchair to the dock
 * and the second one rotates the wheelchair to find the correct direction.  
 * 
 * @author lee
 *
 */
public class ImageAnalyzer extends Thread {
	// main components
	JFrame mainFrame;
	JTextField ipText;
	JTextArea topicInfoText, controlInfoText;
	JButton connectROSButton, stopROSButton;
	JDesktopPane frameContainer;
	JPanel cameraPanel, processPanel, cameraPanel2, templatePanel; 
	JPanel capturedPanel, closedCapturedPanel;
	JLabel processTimeLabel;
	Thread nodeThread;
	
	OdomWriter odomWriter;
	
	// this indicates whether the server with IP (in the server IP textbox)
	// on the right panel has currently been used or not
	boolean isServerInUsed = false;
	
	String settingProfile = Settings.SETTING_2_RED;
	
	Mat buffMat;
	
	public void run() {
		mainFrame = new JFrame("Line Detection Analyzer");
		ImageIcon mainIcon = new ImageIcon("images/monitor2.png");
		mainFrame.setIconImage(mainIcon.getImage());
		
		Container contentPane = mainFrame.getContentPane();
		
		// load UI properties
		// UISupport.loadUIProps();
		UISupport.loadUIProps("2-others");
		
		// load settings (for RED OBJECT configuration)
		Settings.init(settingProfile);
		
		// ------ set Tool-bar and Buttons ------ 
		contentPane.add(buildToolBar(), BorderLayout.NORTH);
	    
	    // ------ set the View panel ------ 
	    contentPane.add(buildViewPanel(), BorderLayout.CENTER);

	    // ------ set Control panel ------ 
	    contentPane.add(buildControlPanel(), BorderLayout.EAST);
	    
		// ------ set Windows Look-n-Feel ------ 
		try {
			UIManager.setLookAndFeel(new LiquidLookAndFeel());
			SwingUtilities.updateComponentTreeUI(mainFrame);
			mainFrame.pack();
		} catch (Exception e) { }
		
		// set window size
		// mainFrame.setSize(UISupport.getUIProp("main-window-width"), UISupport.getUIProp("main-window-height"));
		mainFrame.setSize(1320, 660);
		// mainFrame.setSize(1660, 1060);
		mainFrame.setResizable(false);
		// mainFrame.setMinimumSize(new Dimension(1380, 860));
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int response = JOptionPane.showConfirmDialog(ImageAnalyzer.this.mainFrame, 
									"Are you sure you want to quit?", "Confirm", 
									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			    if (response == JOptionPane.YES_OPTION) {
			    	// close all nodes 
			    	ROSUtils.shutdownAllNodes();
			    	
			    	// clean variables and save properties
			    	prepareCloseApp();
			    	
			    	System.exit(0);
			    }
			}
		});
		
		mainFrame.setVisible(true);
		
		// load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// // initiate some remaining objects
		// startInitObjects();
	}
	
	/**
	 * add a tool-bar with buttons on it
	 * @return
	 */
	private JToolBar buildToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);

		
		JButton settingsBtn = new JButton("Settings");
		settingsBtn.setIcon(new ImageIcon("images/settings.png"));
		settingsBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog();
			}
		});
		settingsBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
		toolbar.add(settingsBtn);
		

		return toolbar;
	}
	
	/**
	 * build the container to contain the inner frames
	 * 
	 * @return
	 */
	private JPanel buildViewPanel() {
		JPanel totalView = new JPanel(new BorderLayout());
		
		// ------ Viewer panel ------
		JPanel viewer = new JPanel(new FlowLayout());
		viewer.setPreferredSize(new Dimension(990, 425));
		viewer.setBorder(BorderFactory.createTitledBorder("Camera View"));

		cameraPanel = new JPanel();
		cameraPanel.setPreferredSize(new Dimension(510, 380));
		cameraPanel.setBackground(new Color(150, 150, 150));
		viewer.add(cameraPanel);
		
		processPanel = new JPanel();
		processPanel.setPreferredSize(new Dimension(510, 380));
		processPanel.setBackground(new Color(200, 200, 200));
		viewer.add(processPanel);
		
		totalView.add(viewer, BorderLayout.NORTH);
		
		// ------ Control + Control Info panel ------
		JPanel control = new JPanel(new BorderLayout()); 
		

		// Control Main Information panel
		JPanel controlInfo = new JPanel(new BorderLayout());
		controlInfo.setBorder(BorderFactory.createTitledBorder("Control Info"));
		
		
		controlInfoText = new JTextArea(10, 50);
		controlInfoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		controlInfoText.setFont(new Font("courier", Font.PLAIN, 10));
		controlInfoText.setEditable(false);
		JScrollPane infoScroller = new JScrollPane(controlInfoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		controlInfo.add(infoScroller, BorderLayout.CENTER);

		processTimeLabel = new JLabel("Processing Time");
		processTimeLabel.setIcon(new ImageIcon("images/settings.png"));
		processTimeLabel.setFont(new Font("", Font.PLAIN, 9));
		controlInfo.add(processTimeLabel, BorderLayout.SOUTH);
		
		control.add(controlInfo, BorderLayout.CENTER);

		// ------ ALL TOPIC PANELS ------  
		JPanel allTopicPanel = new JPanel(new BorderLayout());
		allTopicPanel.setPreferredSize(new Dimension(460, 200));
		
		// ------ Topic Info panel ------ 
		JPanel topicInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		topicInfoPanel.setBorder(BorderFactory.createTitledBorder("Sub Results"));
//		topicInfoPanel.setPreferredSize(new Dimension(330, 200));

		// Centered panel
		JPanel capture = new JPanel(new FlowLayout());
		capture.setPreferredSize(new Dimension(220, 200));
		capture.add(new JLabel("Centered Image"));
		capturedPanel = new JPanel();
		capturedPanel.setPreferredSize(new Dimension(Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT));
		capturedPanel.setBorder(new TitledBorder(""));
		capture.add(capturedPanel);
		
		topicInfoPanel.add(capture);
		
		// Captured panel
		JPanel capture2 = new JPanel(new FlowLayout());
		capture2.setPreferredSize(new Dimension(220, 200));
		capture2.add(new JLabel("Captured Pad"));
		
		closedCapturedPanel = new JPanel();
		closedCapturedPanel.setPreferredSize(new Dimension(Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT));
		closedCapturedPanel.setBorder(new TitledBorder(""));
		capture2.add(closedCapturedPanel);
		
		topicInfoPanel.add(capture2);
		allTopicPanel.add(topicInfoPanel, BorderLayout.WEST);
		
		control.add(allTopicPanel, BorderLayout.EAST);
				
		totalView.add(control, BorderLayout.SOUTH);
		
		return totalView;
	}
	

	/**
	 * call this to start listening to the ROS server
	 */
	private void startListening() {
	
		nodeThread = new Thread() {
			@Override
			public void run() {
				// ====== INITIATING THE FIRST CAMERA ======
				// this will be the name of the subscriber to this topic
				String graphCameraName = ROSUtils.getNodeName(CameraNode.topicTitle);
				
				ROSUtils.execute(graphCameraName, new CameraNode(new CameraNode.ImageListener() {
					@Override
					public void imageArrived(Image image) {
					
						// using image processing to detect the pad 
						long start = System.currentTimeMillis();
						// Object[] results = ObjectDetectorRed.processImage(image);
						Object[] results = ObjectDetectorRed.processImage2(buffMat);
						
						long findPadTime = System.currentTimeMillis() - start;
						
						start = System.currentTimeMillis();
						BufferedImage resultImage = (BufferedImage) results[0];
						BufferedImage processImage = (BufferedImage) results[1];
						BufferedImage capturedImage = (BufferedImage) results[2];
						Rect objectRect = (Rect) results[3];
						Mat padMat = (Mat) results[4];
						double[] timers = (double[]) results[5];
								
						UISupport.drawImage(cameraPanel, resultImage);
						UISupport.drawImage(processPanel, processImage);
						UISupport.drawClearImage(capturedPanel, capturedImage, capturedImage.getWidth(), capturedImage.getHeight());
						
						// using feature detection to find the location of the pad
						Object[] locs = FeatureExtractorRed.detectLocation(padMat);
						UISupport.drawImage(closedCapturedPanel, (BufferedImage) locs[0]);
//						UISupport.drawRatioImage(transformedPanel, (BufferedImage) locs[1]);
						double[] extractTimers = (double[]) locs[2];
						
						long drawTime = System.currentTimeMillis() - start;
						long rate = (long) (1000 / findPadTime);
						ImageAnalyzer.this.processTimeLabel.setText(Constants.CONSOLE_CAM_1 + 
														"Displaying Time: " + drawTime + "ms | " +  
														"Searching Pad Time: " + findPadTime + "ms | " + 
														"Rate: " + rate + "fps");
						
						// teach the wheel-chair how to move
						int moveInstructor = (Integer) MoveInstructor.instruct(resultImage.getWidth(), objectRect);
						double objectDistance = DistanceEstimator.estimateDistance(objectRect);
						double objectAngle = extractTimers[0];
						
						// RosAutoRed.this.controlInfoText.setText("Distance: " + AppUtils.getNumberFormat(objectDistance) + "ft(s)\n" + 
						// 										"Angle: " + AppUtils.getNumberFormat(objectAngle) + "deg");
						ImageAnalyzer.this.topicInfoText.setText("Distance: " + AppUtils.getNumberFormat(objectDistance) + "ft(s)\n" + 
															"Angle: " + AppUtils.getNumberFormat(objectAngle) + "deg(s)\n" + 
															"Wheel Velocity: " + WheelVelocityListener.velocity + "\n" +
															"------------------------------\n" + 
															"Reading: " + timers[0] + "ms\n" + 
															// "Gaussian Blur: " + timers[1] + "ms\n" +
															"HSV Converting: " + timers[2] + "ms\n" +
															"Dilating: " + timers[3] + "ms\n" + 
															"Coutouring: " + timers[4] + "ms\n" + 
															"Bitmap Converting: " + timers[5] + "ms\n" + 
															"------------------------------\n" +
															"Gray Converting: " + extractTimers[1] + "ms\n" + 
															"Threshold: " + extractTimers[2] + "ms\n" + 
															"Contouring Detecting: " + extractTimers[3] + "ms\n" + 
															"Contouring Analysis: " + extractTimers[4] + "ms\n" + 
															"Transformation: " + extractTimers[5] + "ms\n");

						//
						// draw the current location of the wheel-chair on the map 
						drawWheelchairPoint(objectDistance, objectAngle);
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
	

	
	/**
	 * draw the current location of the wheel-chair on to the map
	 * 
	 * @param distance
	 * @param angle
	 */
	private void drawWheelchairPoint(double distance, double angle) {
		Point wcPoint = FeatureExtractorRed.findPointByAngle(distance, angle);
		GPSLocation gpsPoint = LocationInstructor.getGPSLocation(wcPoint);
		// update the location by gps location
		LocationDrawer.updateData(gpsPoint);
		
		// publish to our location channel
		// odomWriter.publish(pos);
	}
	
	
	/**
	 * open Settings dialog
	 */
	private void showSettingsDialog() {
	    JDialog settingsDialog = new JDialog(mainFrame, "Settings", ModalityType.APPLICATION_MODAL);
	    settingsDialog.add(new SettingsPanel());
	    settingsDialog.setSize(660, 660);
	    settingsDialog.setResizable(false);
	    settingsDialog.setLocationRelativeTo(mainFrame);
	    settingsDialog.setVisible(true);
	}
	
	/**
	 * build the right controller panel
	 * 
	 * @return
	 */
	private JPanel buildControlPanel() {
		JPanel config = new JPanel(new BorderLayout());

		// ------ add Network Configuration panel ------ 
		JPanel networkConfig = new JPanel(new BorderLayout());
		networkConfig.setBorder(BorderFactory.createTitledBorder("ROS Core"));
		
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("Server IP: "), BorderLayout.WEST);
		ipText = new JTextField(UISupport.getUIProp("host-text-columns"));
		ipText.grabFocus();
		String currentIP = "129.123.7.41";
		// String currentIP = AppUtils.getCurrentIP();
		ipText.setText(currentIP);
		p1.add(ipText);
		networkConfig.add(p1, BorderLayout.NORTH);

		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		connectROSButton = new JButton("Connect");
		connectROSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initServer();
			}
		});
		p2.add(connectROSButton);
		
		stopROSButton = new JButton("Stop");
		stopROSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopServer();
			}
		});
		stopROSButton.setEnabled(false);
		p2.add(stopROSButton);
		
		networkConfig.add(p2, BorderLayout.CENTER);

		
		config.add(networkConfig, BorderLayout.NORTH);

		// ------ add Location panel ------ 
		JPanel locationPanel = new JPanel();
		locationPanel.setBorder(BorderFactory.createTitledBorder("Location"));
		
		JPanel coordPanel = new JPanel(new BorderLayout()); 
		coordPanel.setPreferredSize(new Dimension(UISupport.getUIProp("location-width"), 
		 											UISupport.getUIProp("location-height")));
		coordPanel.add(LocationDrawer.createLocationSystem());
		
		locationPanel.add(coordPanel, BorderLayout.CENTER);
		config.add(locationPanel, BorderLayout.CENTER);

		// ------ add ROS Topic List panel ------
		JPanel topicPanel = new JPanel(new BorderLayout());
		topicPanel.setBorder(BorderFactory.createTitledBorder("Image Process Info"));
		
		topicInfoText = new JTextArea(UISupport.getUIProp("topic-text-rows"), UISupport.getUIProp("topic-text-columns"));
		topicInfoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		topicInfoText.setFont(new Font("courier", Font.PLAIN, 10));
		topicInfoText.setEditable(false);
		JScrollPane listScroller = new JScrollPane(topicInfoText, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(UISupport.getUIProp("topic-list-width"), 
												UISupport.getUIProp("topic-list-height")));
		topicPanel.add(listScroller, BorderLayout.CENTER);
		config.add(topicPanel, BorderLayout.SOUTH);
		

		return config;
	}
	
	/**
	 * call this when user wants to connect to a new server
	 */
	private void initServer() {
		try {
			// get IP of the current computer
			ROSUtils.myIP = AppUtils.getCurrentIP();
			
			String serverIP = ipText.getText();
			// initiate server
			ROSUtils.startWithServer(serverIP);

			// start listening to the camera topic
			startListening();
			
			// update the controls & variables
			this.isServerInUsed = true;
			ipText.setEditable(false);
			connectROSButton.setEnabled(false);
			stopROSButton.setEnabled(true);
			
			// load sample iamge
			loadSampleImage();
			
		} catch (java.net.ConnectException cEx) {
			controlInfoText.setText("Error @ Server Initiation (" + cEx.getClass().getName() + ": " + cEx.getMessage() + ")");
			JOptionPane.showMessageDialog(mainFrame, "ROS Server is unable to connect [\"" + cEx.getMessage() + "\"]");
		} catch (Exception e) {
			controlInfoText.setText("Error @ Server Initiation (" + e.getClass().getName() + ": " + e.getMessage() + ")");
			JOptionPane.showMessageDialog(mainFrame, "ROS Server is unable to connect [\"" + e.getMessage() + "\"]");
		} finally {
			
		}
	}
	
	/**
	 * call this when user wants to disconnect from the current server
	 */
	private void stopServer() {
		// disable current server 
		ROSUtils.shutdownAllNodes();
		
		// update the controls & variables
		this.isServerInUsed = false;
		ipText.setEditable(true);
		connectROSButton.setEnabled(true);
		stopROSButton.setEnabled(false);
		
	}
	
	private void loadSampleImage() throws Exception {
		// String imgPath = "/home/lee/Documents/images/samples2/1523668544109.png";
		// String imgPath = "/home/lee/Documents/images/samples1/1523677482625.png";
		// String imgPath = "/home/lee/Documents/images/samples1/1523677447782.png";
		String imgPath = "/home/lee/Documents/images/samples1/1523677497634.png";
		BufferedImage buffImage = ImageIO.read(new File(imgPath));
		buffMat = OpenCVUtils.openImage(buffImage);
	}
	
	/**
	 * this function is called to clean all the parameters
	 * as well as save to props file
	 */
	private void prepareCloseApp() {
		Settings.saveProps(settingProfile);
	}
	
	public static void main(String args[]) {
		new ImageAnalyzer().start();
	}
}
