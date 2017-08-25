package com.minhld.ui.apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
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
import com.minhld.ros.controller.MoveInstructor;
import com.minhld.ros.controller.UISupport;
import com.minhld.ros.controller.WheelVelocityListener;
import com.minhld.ui.supports.AdjustSlider;
import com.minhld.ui.supports.LocationDrawer;
import com.minhld.ui.supports.SettingsPanel;
import com.minhld.utils.AppUtils;
import com.minhld.utils.ROSUtils;
import com.minhld.utils.Settings;

import sensor_msgs.Image;

/**
 * the early version of the controller application
 * to detect red pad
 * 
 * @author lee
 *
 */
public class RosAutoRed extends Thread {
	JFrame mainFrame;
	JTextField ipText;
	JTextArea topicInfoText, controlInfoText;
	JButton connectROSButton, stopROSButton, findPadBtn;
	JDesktopPane frameContainer;
	JList<String> topicList;
	JPanel cameraPanel, processPanel, buttonPanel, templatePanel; 
	JPanel capturedPanel, closedCapturedPanel, transformedPanel;
	JLabel keyFocusLabel, processTimeLabel;
	Thread nodeThread;
	
	boolean isAuto = false;
	boolean isMovingNode = false;
	boolean isServerInUsed = false;
	
	public void run() {
		mainFrame = new JFrame("Wheelchair Controller v1.0");
		ImageIcon mainIcon = new ImageIcon("images/monitor2.png");
		mainFrame.setIconImage(mainIcon.getImage());
		
		Container contentPane = mainFrame.getContentPane();
		
		// load UI properties
		UISupport.loadUIProps();
		
		// load settings (for RED OBJECT configuration)
		Settings.init(Settings.SETTING_RED);
		
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
		mainFrame.setSize(UISupport.getUIProp("main-window-width"), UISupport.getUIProp("main-window-height"));
		// mainFrame.setSize(1390, 860);
		// mainFrame.setSize(1660, 1060);
		mainFrame.setResizable(false);
		// mainFrame.setMinimumSize(new Dimension(1380, 860));
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int response = JOptionPane.showConfirmDialog(RosAutoRed.this.mainFrame, 
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

//		// initiate some remaining objects
//		startInitObjects();
	}
	
	/**
	 * add a tool-bar with buttons are on it
	 * @return
	 */
	private JToolBar buildToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);

		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.setIcon(new ImageIcon("images/refresh.png"));
		refreshBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTopicsToList();
			}
		});
		refreshBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
		toolbar.add(refreshBtn);
		
		
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
		
		toolbar.addSeparator();
		
		
		findPadBtn = new JButton("Find Pad");
		findPadBtn.setIcon(new ImageIcon("images/search.png"));
		findPadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (RosAutoRed.this.isAuto) {
					MoveInstructor.move(0, 0);
				} 
				RosAutoRed.this.isAuto = !RosAutoRed.this.isAuto;
				controlInfoText.setText("AUTOMATION IS " + (RosAutoRed.this.isAuto ? "SET" : "CLEARED"));
				findPadBtn.setText(RosAutoRed.this.isAuto ? "Stop Finding" : "Find Pad");
				
			}
		});
		findPadBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
		toolbar.add(findPadBtn);
		
		final JButton initMovingBtn = new JButton("Start Moving");
		initMovingBtn.setIcon(new ImageIcon("images/execute.png"));
		initMovingBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RosAutoRed.this.isMovingNode = !RosAutoRed.this.isMovingNode;
				// controlInfoText.setText("AUTOMATION IS " + (RosAutoRed.this.isMovingNode ? "SET" : "CLEARED"));
				initMovingBtn.setText(RosAutoRed.this.isMovingNode ? "Stop Node" : "Start Moving Node");
				
			}
		});
		initMovingBtn.setEnabled(false);	// temporarily closed
		initMovingBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
		toolbar.add(initMovingBtn);
		
		toolbar.addSeparator();
		
		final JButton debugBtn = new JButton("Debug");
		debugBtn.setIcon(new ImageIcon("images/debug.png"));
		debugBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		debugBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
		toolbar.add(debugBtn);
		
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
		viewer.setPreferredSize(new Dimension(1000, 415));
		viewer.setBorder(BorderFactory.createTitledBorder("Camera View"));

		cameraPanel = new JPanel();
		cameraPanel.setPreferredSize(new Dimension(500, 375));
		cameraPanel.setBackground(new Color(150, 150, 150));
		viewer.add(cameraPanel);
		
		processPanel = new JPanel();
		processPanel.setPreferredSize(new Dimension(500, 375));
		processPanel.setBackground(new Color(200, 200, 200));
		viewer.add(processPanel);
		
		totalView.add(viewer, BorderLayout.NORTH);
		
		// ------ Adjust panel ------
		
		JPanel adjustPanel = new JPanel(new BorderLayout());
		adjustPanel.setBorder(BorderFactory.createTitledBorder("Adjustment"));
		adjustPanel.setPreferredSize(new Dimension(1000, 200));
		
		JPanel slidesPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		slidesPanel.setLayout(new GridLayout(1, 5));
//		slidesPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		// Velocity slider
		JPanel velocityPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		
		AdjustSlider velSlider = new AdjustSlider(Settings.LABEL_VELOCITY, 3, 15);
		velocityPanel.add(velSlider, BorderLayout.NORTH);
		velocityPanel.setPreferredSize(new Dimension(200, 30));
		slidesPanel.add(velocityPanel, BorderLayout.NORTH);
		
		// 1. Captured panel
		JPanel capture = new JPanel(new FlowLayout());
		capture.setPreferredSize(new Dimension(280, 280));
		capture.add(new JLabel("Centered Image"));
		
		capturedPanel = new JPanel();
		capturedPanel.setPreferredSize(new Dimension(Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT));
		capturedPanel.setBorder(new TitledBorder(""));
		capture.add(capturedPanel);
		
		slidesPanel.add(capture);
		
		// 1. Captured panel
		JPanel capture2 = new JPanel(new FlowLayout());
		capture2.setPreferredSize(new Dimension(280, 280));
		capture2.add(new JLabel("Captured Pad"));
		
		closedCapturedPanel = new JPanel();
		closedCapturedPanel.setPreferredSize(new Dimension(Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT));
		closedCapturedPanel.setBorder(new TitledBorder(""));
		capture2.add(closedCapturedPanel);
		
		slidesPanel.add(capture2);
		
		// 1. Captured panel
		JPanel capture3 = new JPanel(new FlowLayout());
		capture3.setPreferredSize(new Dimension(280, 280));
		capture3.add(new JLabel("Transformed Pad"));
		
		transformedPanel = new JPanel();
		transformedPanel.setPreferredSize(new Dimension(Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT));
		transformedPanel.setBorder(new TitledBorder(""));
		capture3.add(transformedPanel);
		
		slidesPanel.add(capture3);
		
		// 1. Captured panel
		JPanel capture4 = new JPanel(new FlowLayout());
		capture4.setPreferredSize(new Dimension(280, 280));
		capture4.add(new JLabel("Captured Image"));
		
		JPanel capturedPanel4 = new JPanel();
		capturedPanel4.setPreferredSize(new Dimension(Settings.TEMPLATE_WIDTH, Settings.TEMPLATE_HEIGHT));
		capturedPanel4.setBorder(new TitledBorder(""));
		capture4.add(capturedPanel4);
		
		slidesPanel.add(capture4);
		
		
		adjustPanel.add(slidesPanel, BorderLayout.CENTER);
		
		totalView.add(adjustPanel, BorderLayout.CENTER);
		
		// ------ Control + Control Info panel ------
		JPanel control = new JPanel(new BorderLayout()); 
		
		JPanel controller = new JPanel(new BorderLayout());
		controller.setBorder(BorderFactory.createTitledBorder("Controller"));
		controller.setPreferredSize(new Dimension(300, UISupport.getUIProp("controller-height")));
		
		// Navigation buttons panel
		this.buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 3));
		buttonPanel.setBorder(new EmptyBorder(UISupport.getUIProp("button-margin-top"), 
										UISupport.getUIProp("button-margin-left"), 
										UISupport.getUIProp("button-margin-top"), 
										UISupport.getUIProp("button-margin-left")));
		buttonPanel.setFocusable(true);
		buttonPanel.requestFocusInWindow();
		buttonPanel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				navButtonClicked(e.getKeyCode());
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() >= 37 && e.getKeyCode() <= 40) {
					// stop moving
					navButtonReleased();
				}
			}
		});
		buttonPanel.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				switchKeyFocus(false);
				if (RosAutoRed.this.isServerInUsed) {
					RosAutoRed.this.buttonPanel.requestFocusInWindow();
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				switchKeyFocus(true);
			}
		});
		
		buttonPanel.add(new JLabel(""));
		
		JButton upBtn = new JButton();
		upBtn.setSize(50, 50);
		upBtn.setIcon(new ImageIcon("images/up.png"));
		upBtn.setFocusable(false);
		upBtn.addMouseListener(new NavButtonClickListener(NavButtonClickListener.KEY_UP));
		buttonPanel.add(upBtn);
		
		buttonPanel.add(new JLabel(""));
		
		JButton leftBtn = new JButton();
		leftBtn.setSize(50, 50);
		leftBtn.setIcon(new ImageIcon("images/left.png"));
		leftBtn.setFocusable(false);
		leftBtn.addMouseListener(new NavButtonClickListener(NavButtonClickListener.KEY_LEFT));
		buttonPanel.add(leftBtn, BorderLayout.WEST);
		
		buttonPanel.add(new JLabel(""));
		
		JButton rightBtn = new JButton();
		rightBtn.setSize(50, 50);
		rightBtn.setIcon(new ImageIcon("images/right.png"));
		rightBtn.setFocusable(false);
		rightBtn.addMouseListener(new NavButtonClickListener(NavButtonClickListener.KEY_RIGHT));
		buttonPanel.add(rightBtn, BorderLayout.SOUTH);
		
		buttonPanel.add(new JLabel(""));
		
		JButton downBtn = new JButton();
		downBtn.setSize(50, 50);
		downBtn.setIcon(new ImageIcon("images/down.png"));
		downBtn.setFocusable(false);
		downBtn.addMouseListener(new NavButtonClickListener(NavButtonClickListener.KEY_DOWN));
		buttonPanel.add(downBtn, BorderLayout.SOUTH);
		
		buttonPanel.add(new JLabel(""));
		
		controller.add(buttonPanel, BorderLayout.CENTER);
		
		keyFocusLabel = new JLabel("Key Focus");
		keyFocusLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		keyFocusLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (RosAutoRed.this.isServerInUsed) {
					RosAutoRed.this.buttonPanel.requestFocusInWindow();
				}
			}
		});
		switchKeyFocus(false);
		controller.add(keyFocusLabel, BorderLayout.SOUTH);
		
		control.add(controller, BorderLayout.WEST);
		
		
		// Control Main Information panel
		JPanel controlInfo = new JPanel(new BorderLayout());
		controlInfo.setBorder(BorderFactory.createTitledBorder("Control Info"));
		
		
		controlInfoText = new JTextArea(10, 63);
		controlInfoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		controlInfoText.setFont(new Font("courier", Font.PLAIN, 11));
		controlInfoText.setEditable(false);
		JScrollPane infoScroller = new JScrollPane(controlInfoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		controlInfo.add(infoScroller, BorderLayout.CENTER);

		processTimeLabel = new JLabel("Processing Time");
		processTimeLabel.setIcon(new ImageIcon("images/settings.png"));
		controlInfo.add(processTimeLabel, BorderLayout.SOUTH);
		
		control.add(controlInfo, BorderLayout.CENTER);
		
		// ------ Coordinate System panel ------ 
		JPanel topicInfoPanel = new JPanel(new BorderLayout());
		topicInfoPanel.setBorder(BorderFactory.createTitledBorder("Topic Info"));
		topicInfoPanel.setPreferredSize(new Dimension(280, 200));
		
		topicInfoText = new JTextArea(UISupport.getUIProp("topic-text-rows"), UISupport.getUIProp("topic-text-columns"));
		topicInfoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		topicInfoText.setFont(new Font("courier", Font.PLAIN, 11));
		topicInfoText.setEditable(false);
		JScrollPane topicInfoScroller = new JScrollPane(topicInfoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		topicInfoPanel.add(topicInfoScroller, BorderLayout.CENTER);

		topicInfoPanel.add(new JLabel(" "), BorderLayout.SOUTH);
		
		control.add(topicInfoPanel, BorderLayout.EAST);
		
		totalView.add(control, BorderLayout.SOUTH);
		
		return totalView;
	}
	
    
	/**
	 * this function is called when user presses on navigation buttons on keyboard
	 * or uses mouse to click on navigation buttons on the application
	 * 
	 * @param keyCode
	 */
	private void navButtonClicked(int keyCode) {
		// skip if server is not set
		if (!this.isServerInUsed) return;
		
		// go otherwise
		float actualVel = (float) Settings.velocity / 10;
		String move = "";
		switch (keyCode) {
			case NavButtonClickListener.KEY_UP: {
				// move up
				MoveInstructor.move(actualVel, 0);
				// MoveInstructor2.moveForward(actualVel);
				move = "FORWARD";
				break;
			} case NavButtonClickListener.KEY_DOWN: {
				// move down
				MoveInstructor.move(-1 * actualVel, 0);
				// MoveInstructor2.moveBackward(actualVel);
				move = "BACKWARD";
				break;
			} case NavButtonClickListener.KEY_LEFT: {
				// move left
				MoveInstructor.move(0, actualVel);
				// MoveInstructor2.moveLeft(actualVel);
				move = "LEFT";
				break;
			} case NavButtonClickListener.KEY_RIGHT: {
				// move right
				MoveInstructor.move(0, -1 * actualVel);
				// MoveInstructor2.moveRight(-1 * actualVel);
				move = "RIGHT";
				break;
			}
		}
		controlInfoText.setText("move: " + move + " | velocity: " + actualVel);
	}
	
	/**
	 * 
	 */
	private void navButtonReleased() {
		// skip if server is not set
		if (!this.isServerInUsed) return;
		
		// go otherwise
		MoveInstructor.move(0, 0);
		controlInfoText.setText("move: STOP");
	}
	
	/**
	 * this class declares mouse pressed and released events for
	 * the navigation buttons. 
	 * 
	 * @author lee
	 *
	 */
	private class NavButtonClickListener extends MouseAdapter {
		public static final int KEY_UP = 38;
		public static final int KEY_DOWN = 40;
		public static final int KEY_LEFT = 37;
		public static final int KEY_RIGHT = 39;
		
		private int keyCode;
		
		public NavButtonClickListener(int keyCode) {
			this.keyCode = keyCode;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			navButtonClicked(this.keyCode);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			navButtonReleased();
		}
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
						// BufferedImage bImage = ROSUtils.messageToBufferedImage(image);
						// BufferedImage bImage = OpenCVUtils.getBufferedImage(image);
						// long loadImageTime = System.currentTimeMillis() - start;
						
						// // draw on the LEFT canvas the original camera image
						// drawImage(cameraPanel, bImage, cameraPanel.getWidth(), cameraPanel.getHeight());
						
						// draw on the RIGHT canvas the modify image
						// Object[] results = OpenCVUtils.processImage(bImage);
						
						// using image processing to detect the pad 
						long start = System.currentTimeMillis();
						Object[] results = ObjectDetectorRed.processImage(image);
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
						UISupport.drawRatioImage(transformedPanel, (BufferedImage) locs[1]);
						double[] extractTimers = (double[]) locs[2];
						
						long drawTime = System.currentTimeMillis() - start;
						long rate = (long) (1000 / findPadTime);
						RosAutoRed.this.processTimeLabel.setText("Displaying Time: " + drawTime + "ms | " +  
														"Searching Pad Time: " + findPadTime + "ms | " + 
														"Rate: " + rate + "fps");
						
						// teach the wheel-chair how to move
						int moveInstructor = (Integer) MoveInstructor.instruct(resultImage.getWidth(), objectRect);
						double objectDistance = DistanceEstimator.estimateDistance(objectRect);
						double objectAngle = extractTimers[0];
						
						// RosAutoRed.this.controlInfoText.setText("Distance: " + AppUtils.getNumberFormat(objectDistance) + "ft(s)\n" + 
						// 										"Angle: " + AppUtils.getNumberFormat(objectAngle) + "deg");
						RosAutoRed.this.topicInfoText.setText("Distance: " + AppUtils.getNumberFormat(objectDistance) + "ft(s)\n" + 
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

						drawWheelchairPoint(objectDistance, objectAngle);
						
						if (RosAutoRed.this.isAuto) {
							// only automatically moving when flag isAuto is set
							double vel = (double) Settings.velocity / 10;
							if (moveInstructor == MoveInstructor.MOVE_SEARCH) {
								controlInfoText.setText("SEARCHING PAD...");
								MoveInstructor.move(0, vel);
								// MoveInstructor2.moveRight(vel);
							} else if (moveInstructor == MoveInstructor.MOVE_LEFT) {
								controlInfoText.setText("FOUND THE PAD ON THE LEFT. MOVING LEFT...");
								MoveInstructor.move(0, vel);
								// MoveInstructor2.moveLeft(vel);
							} else if (moveInstructor == MoveInstructor.MOVE_RIGHT) {
								controlInfoText.setText("FOUND THE PAD ON THE RIGHT. MOVING RIGHT...");
								MoveInstructor.move(0, -1 * vel);
								// MoveInstructor2.moveRight(-1 * vel);
							} else if (moveInstructor == MoveInstructor.MOVE_FORWARD) {
								controlInfoText.setText("MOVING FORWARD...");
								if (objectDistance > 5) {
									MoveInstructor.move(vel, 0);
								} else {
									MoveInstructor.moveForward(vel, objectDistance);
									setFindingPadStatus(false);
								}
								
								// MoveInstructor2.moveForward(vel);
							}
						}
					}
				}));
				
				// start the Movement Instructor
				String graphMoveName = ROSUtils.getNodeName(MoveInstructor.moveTopicTitle);
				ROSUtils.execute(graphMoveName, new MoveInstructor());
			}
		};
		nodeThread.start();
		
		
	}
	
	private void setFindingPadStatus(boolean isAuto) {
		RosAutoRed.this.isAuto = isAuto;
		controlInfoText.setText("AUTOMATION IS " + (RosAutoRed.this.isAuto ? "SET" : "CLEARED"));
		findPadBtn.setText(RosAutoRed.this.isAuto ? "Stop Finding" : "Find Pad");

	}
	
	private void drawWheelchairPoint(double distance, double angle) {
		Point wcPoint = FeatureExtractorRed.findPointByAngle(distance, angle);
		LocationDrawer.updateData(wcPoint, 0);
	}
	
//	int count = 1;
//	double maxX = -100, minX = 100, maxY = -100, minY = 100;
//	
//	private void drawWheelchairPoint(double distance, double angle) {
//		Point wcPoint = FeatureExtractorRed.findPointByAngle(distance, angle);
//		if (count % 5 != 0) {
//			count++;
//			if (maxX < wcPoint.x) {
//				maxX = wcPoint.x;
//			}
//			if (minX > wcPoint.x) {
//				minX = wcPoint.x;
//			}
//			if (maxY < wcPoint.y) {
//				maxY = wcPoint.y;
//			}
//			if (minY > wcPoint.y) {
//				minY = wcPoint.y;
//			}
//		} else {
//			if (count < 50) {
//				double x = (minX + maxX) / 2;
//				double y = (minY + maxY) / 2;
//				double rad = Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxY - minY, 2));
//				LocationDrawer.updateData(new Point(x, y), rad / 2);
//				count++;
//			} else {
//				count = 1;
//				maxX = -100;
//				minX = 100; 
//				maxY = -100;
//				minY = 100;
//			}
//		}
//		
//	}
	
	
//	private void startInitObjects() {
//		// add a template image
//		addTemplateImage();
//        
//	}
	
//	@SuppressWarnings("serial")
//	private void addTemplateImage() {
//		// final BufferedImage templateImage = OpenCVUtils.createAwtImage(ObjectDetector.tplMat);
//		
//		JPanel tplImagePanel = new JPanel() {
////            @Override
////            protected void paintComponent(Graphics g) {
////                super.paintComponent(g);
////                g.drawImage(templateImage, 0, 0, 150, 80, null);
////            }
//        };
//        tplImagePanel.setSize(new Dimension(150, 80));
//        templatePanel.add(tplImagePanel, BorderLayout.CENTER);
//	}
	
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
		String currentIP = "129.123.7.100";
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
		topicPanel.setBorder(BorderFactory.createTitledBorder("ROS Topics"));
		
		topicList = new JList<String>(); 
		topicList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		topicList.setLayoutOrientation(JList.VERTICAL);
		topicList.setVisibleRowCount(-1);
		topicList.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("rawtypes")
			@Override
			public void mouseClicked(MouseEvent e) {
				JList list = (JList) e.getSource();
	            
	            String selectedTopic = (String) list.getSelectedValue();
	            
	            if (selectedTopic == null || selectedTopic.equals("")) {
	            	JOptionPane.showMessageDialog(mainFrame, "Please enter a ROS Server IP and subscribe to that server.", 
	            						"Info", JOptionPane.INFORMATION_MESSAGE);
	            	ipText.grabFocus();
	            	ipText.selectAll();
	            	return;
	            }
	            
		        if (e.getClickCount() == 1) {
		        	// single-click detected
		        	String topicInfo = ROSUtils.getTopicInfo(selectedTopic);
		        	topicInfoText.setText(topicInfo);
		        }
			}
		});
		JScrollPane listScroller = new JScrollPane(topicList, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(UISupport.getUIProp("topic-list-width"), 
													UISupport.getUIProp("topic-list-height")));
		topicPanel.add(listScroller, BorderLayout.CENTER);
		config.add(topicPanel, BorderLayout.SOUTH);
		

		// crawl topic list and add to the swing view list
		// addTopicsToList();
		

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
			
			// add topics to the list
			addTopicsToList();
			
			// update the controls & variables
			this.isServerInUsed = true;
			ipText.setEditable(false);
			connectROSButton.setEnabled(false);
			stopROSButton.setEnabled(true);
			RosAutoRed.this.buttonPanel.requestFocusInWindow();
			
		} catch (Exception e) {
			topicInfoText.setText("Error @ Server Initiation (" + e.getClass().getName() + ": " + e.getMessage() + ")");
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
	
	/**
	 * this function is called to clean all the parameters
	 * as well as save to props file
	 */
	private void prepareCloseApp() {
		Settings.saveProps(Settings.SETTING_RED);
	}
	
	/**
	 * add a topic list to the swing list
	 */
	private void addTopicsToList() {
		String[] topics = ROSUtils.getTopicNameList(ipText.getText(), true);
		
		if (topics.length == 0) {
			JOptionPane.showMessageDialog(mainFrame, "ROS Server is unable to connect.");
		} 
		topicList.removeAll();
		topicList.setListData(topics);
	}
	
	private void switchKeyFocus(boolean isFocused) {
		keyFocusLabel.setIcon(isFocused ? new ImageIcon("images/smile-yellow.png") : 
										new ImageIcon("images/smile-gray.png"));
	}
	
	public static void main(String args[]) {
		new RosAutoRed().start();
	}
}
