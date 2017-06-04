package com.minhld.ros.controller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.opencv.core.Core;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.minhld.utils.AppUtils;
import com.minhld.utils.OpenCVUtils;
import com.minhld.utils.ROSUtils;

import sensor_msgs.Image;

public class RosAuto extends Thread {
	JFrame mainFrame;
	JTextField ipText;
	JTextArea infoText, controlInfoText;
	JDesktopPane frameContainer;
	JList<String> topicList;
	JPanel cameraPanel, processPanel;
	Thread nodeThread;
	
	VelocityTalker mover;
	boolean isAuto = false;
	
	public void run() {
		mainFrame = new JFrame("Wheelchair Controller v1.0");
		ImageIcon mainIcon = new ImageIcon("images/monitor2.png");
		mainFrame.setIconImage(mainIcon.getImage());
		
		Container contentPane = mainFrame.getContentPane();
		
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
		mainFrame.setSize(1390, 860);
		mainFrame.setResizable(false);
		// mainFrame.setMinimumSize(new Dimension(1380, 860));
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int response = JOptionPane.showConfirmDialog(RosAuto.this.mainFrame, 
									"Are you sure you want to quit?", "Confirm", 
									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			    if (response == JOptionPane.YES_OPTION) {
			    	// close all nodes 
			    	ROSUtils.shutdownAllNodes();
			    	System.exit(0);
			    }
			}
		});
		mainFrame.setVisible(true);
		
		// load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

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
		toolbar.add(refreshBtn);
		toolbar.addSeparator();
		
		JButton findPadBtn = new JButton("Find Pad");
		findPadBtn.setIcon(new ImageIcon("images/search.png"));
		findPadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RosAuto.this.isAuto = !RosAuto.this.isAuto;
				infoText.setText("AUTOMATION IS " + (RosAuto.this.isAuto ? "SET" : "CLEARED"));
			}
		});
		toolbar.add(findPadBtn);
		
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
		// viewer.setPreferredSize(new Dimension(900, 550));
		viewer.setBorder(BorderFactory.createTitledBorder("Camera View"));

		cameraPanel = new JPanel();
		cameraPanel.setPreferredSize(new Dimension(500, 500));
		cameraPanel.setBackground(new Color(150, 150, 150));
		viewer.add(cameraPanel);
		
		processPanel = new JPanel();
		processPanel.setPreferredSize(new Dimension(500, 500));
		processPanel.setBackground(new Color(200, 200, 200));
		viewer.add(processPanel);
		
		totalView.add(viewer, BorderLayout.CENTER);
		
		// ------ Control + Control Info panel ------
		JPanel control = new JPanel(new BorderLayout()); 
		
		JPanel controller = new JPanel();
		controller.setBorder(BorderFactory.createTitledBorder("Controller"));
		controller.setPreferredSize(new Dimension(300, 300));
		control.add(controller, BorderLayout.WEST);
		
		JPanel controlInfo = new JPanel();
		controlInfo.setBorder(BorderFactory.createTitledBorder("Control Info"));
		// controlInfo.setPreferredSize(new Dimension(600, 300));
		
		controlInfoText = new JTextArea(19, 102);
		controlInfoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		controlInfoText.setFont(new Font("courier", Font.PLAIN, 11));
		controlInfoText.setEditable(false);
		JScrollPane infoScroller = new JScrollPane(controlInfoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		controlInfo.add(infoScroller, BorderLayout.CENTER);

		
		control.add(controlInfo, BorderLayout.EAST);
		
		totalView.add(control, BorderLayout.SOUTH);
		
		return totalView;
	}
	
	
	
	private void startListening() {
	
		final String topicTitle = "/rrbot/camera1/image_raw";
		
		nodeThread = new Thread() {
			@Override
			public void run() {
				// this will be the name of the subscriber to this topic
				String graphName = ROSUtils.getNodeName(topicTitle);
				
				ROSUtils.execute(graphName, new CameraListener3(graphName, topicTitle, new CameraListener3.ImageListener() {
					@Override
					public void imageArrived(Image image) {
						BufferedImage bImage = ROSUtils.messageToBufferedImage(image);
						
						// draw on the LEFT canvas the original camera image
						drawImage(cameraPanel, bImage, cameraPanel.getWidth(), cameraPanel.getHeight());
						
						// draw on the RIGHT canvas the modify image
//						Object[] results = OpenCVUtils.processImage(bImage);
//						long start = System.currentTimeMillis();
						Object[] results = OpenCVUtils.processImage(image);
//						System.out.println("processing time = " + (System.currentTimeMillis() - start));
						BufferedImage bImage2 = (BufferedImage) results[0];
						boolean isAtCenter = (Boolean) results[1];
						drawImage(processPanel, bImage2, processPanel.getWidth(), processPanel.getHeight());
						
						
						if (RosAuto.this.isAuto) {
							// only automatically moving when flag isAuto is set
							
							if (isAtCenter) {
								// stop and move toward
								infoText.setText("FOUND THE PAD. MOVING AHEAD");
								CameraListener3.move(0.15, 0);
							} else {
								// continue rotating
								infoText.setText("SEARCHING THE PAD...");
								CameraListener3.move(0, 0.2);
							}
						}
					}
				}));
				
//				// start the velocity talker
//				String talkerTopic = "/cmd_vel";
//				String talkerNodeName = ROSUtils.getTalkerName(talkerTopic);
//				mover = new VelocityTalker(talkerNodeName, talkerTopic);
//				ROSUtils.execute(talkerNodeName, mover);
			}
		};
		nodeThread.start();
		
		
	}
	
	private void drawImage(JPanel panel, BufferedImage img, int w, int h) {
		Graphics g = panel.getGraphics();
		if (g != null) {
			g.drawImage(img, 0, 0, w, h, null);
		}
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
		ipText = new JTextField(20); 
		String currentIP = "129.123.7.100";
		// String currentIP = AppUtils.getCurrentIP();
		ipText.setText(currentIP);
		p1.add(ipText);
		networkConfig.add(p1, BorderLayout.NORTH);

		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton connectROSButton = new JButton("Connect");
		connectROSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initServer();
			}
		});
		p2.add(connectROSButton);
		
		JButton endROSButton = new JButton("Stop");
		endROSButton.setEnabled(false);
		p2.add(endROSButton);
		
		networkConfig.add(p2, BorderLayout.CENTER);

		
		config.add(networkConfig, BorderLayout.NORTH);

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
		        if (e.getClickCount() == 1) {
		        	// single-click detected
		        	String topicInfo = ROSUtils.getTopicInfo(selectedTopic);
		        	infoText.setText(topicInfo);
		        }
			}
		});
		JScrollPane listScroller = new JScrollPane(topicList, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(300, 250));
		topicPanel.add(listScroller, BorderLayout.CENTER);
		config.add(topicPanel, BorderLayout.CENTER);

		// crawl topic list and add to the swing view list
		// addTopicsToList();
		
		// ------ add Network Info panel ------ 
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Network Log"));

		infoText = new JTextArea(25, 46);
		infoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		infoText.setFont(new Font("courier", Font.PLAIN, 11));
		infoText.setEditable(false);
		JScrollPane infoScroller = new JScrollPane(infoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		infoPanel.add(infoScroller, BorderLayout.CENTER);

		config.add(infoPanel, BorderLayout.SOUTH);

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
		} catch (Exception e) {
			infoText.setText("Error @ Server Initiation (" + e.getClass().getName() + ": " + e.getMessage() + ")");
		}
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
	
	
	public static void main(String args[]) {
		new RosAuto().start();
	}
}
