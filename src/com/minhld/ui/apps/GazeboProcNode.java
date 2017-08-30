package com.minhld.ui.apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.opencv.core.Core;
import org.opencv.core.Point;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.minhld.ros.controller.CameraNode;
import com.minhld.ros.controller.MoveInstructor;
import com.minhld.ros.controller.OdomListener;
import com.minhld.ros.controller.OdomWriter;
import com.minhld.ros.controller.UISupport;
import com.minhld.ui.supports.AdjustSlider;
import com.minhld.ui.supports.LocationDrawer;
import com.minhld.utils.AppUtils;
import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.ROSUtils;
import com.minhld.utils.Settings;

import geometry_msgs.Pose;
import geometry_msgs.Twist;
import nav_msgs.Odometry;
import sensor_msgs.Image;

public class GazeboProcNode extends Thread {
	JFrame mainFrame;
	JTextField ipText;
	JTextArea topicInfoText, controlInfoText;
	JButton connectROSButton, stopROSButton;
	JDesktopPane frameContainer;
	JList<String> topicList;
	JPanel cameraPanel, buttonPanel, templatePanel; 
	JLabel keyFocusLabel, processTimeLabel;
	Thread nodeThread;
	
	OdomWriter odomWriter;
	
	boolean isServerInUsed = false;
	
	public void run() {
		mainFrame = new JFrame("Gazebo Monitor v1.1");
		ImageIcon mainIcon = new ImageIcon("images/monitor2.png");
		mainFrame.setIconImage(mainIcon.getImage());
		
		Container contentPane = mainFrame.getContentPane();
		
		// load UI properties
		UISupport.loadUIProps("gaz");
		
		// load settings
		Settings.init(Settings.SETTING_GAZ);
		
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
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int response = JOptionPane.showConfirmDialog(GazeboProcNode.this.mainFrame, 
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

	}
	
	
	/**
	 * build the container to contain the inner frames
	 * 
	 * @return
	 */
	private JPanel buildViewPanel() {
		JPanel totalView = new JPanel(new BorderLayout());
		
		JPanel mainView = new JPanel(new FlowLayout());
		
		// ------ Viewer panel ------
		JPanel viewer = new JPanel(new FlowLayout());
		viewer.setPreferredSize(new Dimension(530, 415));
		viewer.setBorder(BorderFactory.createTitledBorder("Camera View"));

		cameraPanel = new JPanel();
		cameraPanel.setPreferredSize(new Dimension(500, 375));
		cameraPanel.setBackground(new Color(150, 150, 150));
		viewer.add(cameraPanel);
		
		mainView.add(viewer);

		// ------ add Location panel ------ 
		JPanel locationPanel = new JPanel();
		locationPanel.setBorder(BorderFactory.createTitledBorder("Location"));
		
		JPanel coordPanel = new JPanel(new BorderLayout()); 
		coordPanel.setPreferredSize(new Dimension(UISupport.getUIProp("location-width"), 
		 											UISupport.getUIProp("location-height")));
		coordPanel.add(LocationDrawer.createLocationSystem());
		
		locationPanel.add(coordPanel, BorderLayout.CENTER);
		mainView.add(locationPanel);
		
		
		totalView.add(mainView, BorderLayout.NORTH);
		
		
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
				if (GazeboProcNode.this.isServerInUsed) {
					GazeboProcNode.this.buttonPanel.requestFocusInWindow();
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
				if (GazeboProcNode.this.isServerInUsed) {
					GazeboProcNode.this.buttonPanel.requestFocusInWindow();
				}
			}
		});
		switchKeyFocus(false);
		controller.add(keyFocusLabel, BorderLayout.SOUTH);
		
		control.add(controller, BorderLayout.WEST);
		
		
		// Control Main Information panel
		JPanel controlInfo = new JPanel(new BorderLayout());
		controlInfo.setBorder(BorderFactory.createTitledBorder("Control Info"));
		
		JPanel velocityPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		AdjustSlider velSlider = new AdjustSlider(Settings.LABEL_VELOCITY, 3, 15);
		velocityPanel.add(velSlider, BorderLayout.NORTH);
		velocityPanel.setPreferredSize(new Dimension(200, 60));
		controlInfo.add(velocityPanel, BorderLayout.NORTH);
		
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
						long start = System.currentTimeMillis();
						// BufferedImage bImage = ROSUtils.messageToBufferedImage(image);
						BufferedImage bImage = OpenCVUtils.getBufferedImage(image);
						long loadImageTime = System.currentTimeMillis() - start;
						if (loadImageTime == 0) loadImageTime = 1;	// too fast!
							
						UISupport.drawImage(cameraPanel, bImage);

						long drawTime = System.currentTimeMillis() - start;
						long rate = (long) (1000 / loadImageTime);
						GazeboProcNode.this.processTimeLabel.setText("Displaying Time: " + drawTime + "ms | " +  
														"Rate: " + rate + "fps");
						
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
//						if (yaw < 0) {
//							angle = 360 - angle;
//						}
						
						String xyz = "(X=" + AppUtils.getSmallNumberFormat(p.getPosition().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(p.getPosition().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(p.getPosition().getZ()) + ")"; 
						// String o = "(X=" + AppUtils.getSmallNumberFormat(p.getOrientation().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(p.getOrientation().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(p.getOrientation().getZ()) + ",W=" + AppUtils.getSmallNumberFormat(p.getOrientation().getW()) + ")";
						String o = "(X=" + AppUtils.getSmallNumberFormat(p.getOrientation().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(p.getOrientation().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(p.getOrientation().getZ()) + ",W=" + AppUtils.getSmallNumberFormat(p.getOrientation().getW()) + ",A=" + AppUtils.getSmallNumberFormat(angle) + ")";
						String l = "(X=" + AppUtils.getSmallNumberFormat(t.getLinear().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(t.getLinear().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(t.getLinear().getZ()) + ")";
						String a = "(X=" + AppUtils.getSmallNumberFormat(t.getAngular().getX()) + ",Y=" + AppUtils.getSmallNumberFormat(t.getAngular().getY()) + ",Z=" + AppUtils.getSmallNumberFormat(t.getAngular().getZ()) + ")";

						// update location text
						GazeboProcNode.this.topicInfoText.setText(
											"Location: \n" + xyz + "\n" + 
											"Orientation: \n" + o + "\n" +
											"Linear: \n" + l + "\n" + 
											"Angular: \n" + a);
						
						// update location on graph
						LocationDrawer.updateData(new Point(x, y), 0);
						
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
	
	
//	private void drawWheelchairPoint(double distance, double angle) {
//		Point wcPoint = FeatureExtractorRed.findPointByAngle(distance, angle);
//		LocationDrawer.updateData(wcPoint, 0);
//	}
	
	
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

		// ------ Coordinate System panel ------ 
		JPanel topicInfoPanel = new JPanel(new BorderLayout());
		topicInfoPanel.setBorder(BorderFactory.createTitledBorder("Topic Info"));
		topicInfoPanel.setPreferredSize(new Dimension(320, 200));
		
		topicInfoText = new JTextArea(UISupport.getUIProp("topic-text-rows"), UISupport.getUIProp("topic-text-columns"));
		topicInfoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		topicInfoText.setFont(new Font("courier", Font.PLAIN, 11));
		topicInfoText.setEditable(false);
		JScrollPane topicInfoScroller = new JScrollPane(topicInfoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		topicInfoPanel.add(topicInfoScroller, BorderLayout.CENTER);
		config.add(topicInfoPanel, BorderLayout.EAST);
		

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
			GazeboProcNode.this.buttonPanel.requestFocusInWindow();
			
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
		Settings.saveProps(Settings.SETTING_GAZ);
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
		new GazeboProcNode().start();
	}
}
