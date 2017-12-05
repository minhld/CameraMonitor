package com.usu.cam;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dialog.ModalityType;
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

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.minhld.ros.controller.CameraNode;
import com.minhld.ros.controller.OdomWriter;
import com.minhld.ros.controller.UISupport;
import com.minhld.ui.supports.SettingsPanel;
import com.minhld.utils.AppUtils;
import com.minhld.utils.ROSUtils;
import com.minhld.utils.Settings;
import com.usu.db.EventDb;

import sensor_msgs.Image;

public class CamObserver extends Thread {
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
		mainFrame = new JFrame("Camera Monitor v2.1");
		ImageIcon mainIcon = new ImageIcon("images/monitor2.png");
		mainFrame.setIconImage(mainIcon.getImage());
		
		Container contentPane = mainFrame.getContentPane();
		
		// load UI properties
		UISupport.loadUIProps("cam");
		
		// load settings
		Settings.init(Settings.SETTING_CAM);
		
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
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int response = JOptionPane.showConfirmDialog(CamObserver.this.mainFrame, 
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
	 * add a tool-bar with buttons are on it
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

		totalView.add(mainView, BorderLayout.NORTH);
		
		
		// ------ Control + Control Info panel ------
		JPanel control = new JPanel(new BorderLayout()); 
		
		
		// Control Main Information panel
		JPanel controlInfo = new JPanel(new BorderLayout());
		controlInfo.setBorder(BorderFactory.createTitledBorder("Control Info"));
		
		controlInfoText = new JTextArea(9, 63);
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
	 * extract movements from the video file 
	 */
	void startListening2() {
		new Runnable() {
			
			@Override
			public void run() {
				String file = "/home/lee/Downloads/walk.mp4";
				VideoCapture vc = new VideoCapture();
				vc.open(file);
				Mat m = new Mat();
				while (vc.read(m)) {
					Object[] a = CamObjectDetector.processImage(m);
					if (a[0] != null) {
						UISupport.drawImage(cameraPanel, (BufferedImage) a[0]);
					}
					
					try {
						Thread.sleep(30);
					} catch (Exception e) { }
				}
			}
		}.run();
		
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
						Object[] results = CamObjectDetector.processImage(image);
						
						if (results == null) return;
						
						BufferedImage resultImage = (BufferedImage) results[0];
						
						long loadImageTime = System.currentTimeMillis() - start;
						if (loadImageTime == 0) loadImageTime = 1;	// too fast!
							
						UISupport.drawImage(cameraPanel, resultImage);

						long drawTime = System.currentTimeMillis() - start;
						long rate = (long) (1000 / loadImageTime);
						CamObserver.this.processTimeLabel.setText("Displaying Time: " + drawTime + "ms | " +  
														"Rate: " + rate + "fps");

						CamObserver.this.controlInfoText.setText(
										"Total Time: " + drawTime + "ms\n" +
										"Gaussian: " + results[1] + "ms\n" +
										"Differentiate: " + results[2] + "ms\n" +
										"Contouring: " + results[3] + "ms\n" +
										"Drawing: " + results[4] + "ms\n");

					}
				}));

				// start the Odometry publisher
				odomWriter = new OdomWriter();
				// String odomTitle = ROSUtils.getNodeName(OdomWriter.topicTitle);
				ROSUtils.execute(OdomWriter.topicTitle, odomWriter);
			}
		};
		nodeThread.start();
		
		
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
			// startListening2();
			
			// add topics to the list
			addTopicsToList();
			
			// init db
			EventDb.init(serverIP);					
			
			// update the controls & variables
			this.isServerInUsed = true;
			ipText.setEditable(false);
			connectROSButton.setEnabled(false);
			stopROSButton.setEnabled(true);
			
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
		
		// shutdown db
		EventDb.close();
		
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
	

	public static void main(String args[]) {
		new CamObserver().start();
	}
}
