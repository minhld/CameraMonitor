package com.minhld.ros.controller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.DesktopPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.minhld.utils.AppUtils;
import com.minhld.utils.ROSInnerFrame;
import com.minhld.utils.ROSUtils;
import com.minhld.utils.TopicInfo;

public class RosController extends Thread {
	JFrame mainFrame;
	JTextField ipText;
	JTextArea infoText;
	JDesktopPane frameContainer;
	JList<String> topicList;
	JTree topicInfoTree;
	
	public void run() {
		mainFrame = new JFrame("Robot Monitor v1.0");
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
		mainFrame.setSize(1390, 980);
		mainFrame.setMinimumSize(new Dimension(1280, 860));
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) { }
			
			@Override
			public void windowIconified(WindowEvent e) { }
			
			@Override
			public void windowDeiconified(WindowEvent e) { }
			
			@Override
			public void windowDeactivated(WindowEvent e) { }
			
			@Override
			public void windowClosing(WindowEvent e) {
				// close all nodes 
				ROSUtils.shutdownAllNodes();
			}
			
			@Override
			public void windowClosed(WindowEvent e) { }
			
			@Override
			public void windowActivated(WindowEvent e) { }
		});
		mainFrame.setVisible(true);
	}
	
	/**
	 * add a tool-bar with buttons are on it
	 * @return
	 */
	private JToolBar buildToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);

		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTopicsToList();
			}
		});
		toolbar.add(refreshBtn);
		toolbar.addSeparator();
		
//		JButton findPadBtn = new JButton("Find Pad");
//		findPadBtn.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				
//			}
//		});
//		toolbar.add(findPadBtn);
		
		return toolbar;
	}
	
	/**
	 * build the container to contain the inner frames
	 * 
	 * @return
	 */
	private JDesktopPane buildViewPanel() {
		frameContainer = new JDesktopPane();
		frameContainer.setBackground(new Color(220, 220, 220));
		frameContainer.setBorder(new javax.swing.border.BevelBorder(BevelBorder.LOWERED));
		frameContainer.setAutoscrolls(true);
		
		frameContainer.setUI(new DesktopPaneUI() {
		    @Override
		        public void installUI(JComponent c) {
		            try {
		            	LiquidLookAndFeel liquid = new LiquidLookAndFeel();
		                UIManager.setLookAndFeel(liquid);
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		            super.installUI(c);
		        }   
		    });

		return frameContainer;
	}
	
	/**
	 * build the right controller panel
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private JPanel buildControlPanel() {
		JPanel controller = new JPanel(new BorderLayout());

		// ------ add Network Configuration panel ------ 
		JPanel networkConfig = new JPanel(new BorderLayout());
		networkConfig.setBorder(BorderFactory.createTitledBorder("ROS Core"));
		
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("Server IP: "), BorderLayout.WEST);
		ipText = new JTextField(20); 
		String currentIP = AppUtils.getCurrentIP();
		ipText.setText(currentIP);
		p1.add(ipText);
		networkConfig.add(p1, BorderLayout.NORTH);

		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton connectROSButton = new JButton("Start");
		connectROSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initServer();
			}
		});
		// connectROSButton.setEnabled(false);
		p2.add(connectROSButton);
		
		JButton endROSButton = new JButton("End");
		endROSButton.setEnabled(false);
		endROSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		p2.add(endROSButton);
		
		networkConfig.add(p2, BorderLayout.CENTER);

		
		controller.add(networkConfig, BorderLayout.NORTH);

		// ------ add ROS Topic List panel ------
		JPanel topicPanel = new JPanel(new BorderLayout());
		topicPanel.setBorder(BorderFactory.createTitledBorder("ROS Topics"));
		
		topicList = new JList<String>(); 
		topicList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		topicList.setLayoutOrientation(JList.VERTICAL);
		topicList.setVisibleRowCount(-1);
		topicList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
		        JList list = (JList) e.getSource();
	            
	            String selectedTopic = (String) list.getSelectedValue();
	            if (selectedTopic == null || selectedTopic.equals("")) {
	            	JOptionPane.showMessageDialog(mainFrame, "Please enter a ROS Server IP and subscribe to that server.", "Info", JOptionPane.INFORMATION_MESSAGE);
	            	ipText.grabFocus();
	            	ipText.selectAll();
	            	return;
	            }
	            
	            TopicInfo topicInfo = ROSUtils.topics.get(selectedTopic);
		        if (e.getClickCount() == 2) {
		        	// Double-click detected
		        	int index = list.locationToIndex(e.getPoint());
		            
		        	// open corresponding window for a topic
		            createFrame(topicInfo, index);
		        } else if (e.getClickCount() == 1) {
		        	// single-click detected
		        	String topicInfoText = ROSUtils.getTopicInfo(selectedTopic);
		        	infoText.setText(topicInfoText);
		        	
		        	// add info to the tree 
		        	getTopicInfoTree(topicInfo);
		        }
			}
		});
		JScrollPane listScroller = new JScrollPane(topicList, 
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(300, 250));
		topicPanel.add(listScroller, BorderLayout.CENTER);
		
		// ------ add ROS Topic Info panel  ------
		topicInfoTree = new JTree();
		
		JScrollPane topicInfoScroller = new JScrollPane(topicInfoTree, 
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		topicInfoScroller.setPreferredSize(new Dimension(300, 150));
		topicPanel.add(topicInfoScroller, BorderLayout.SOUTH);
		
		controller.add(topicPanel, BorderLayout.CENTER);

		// // crawl topic list and add to the swing view list
		// addTopicsToList();
		
		// ------ add Network Info panel ------ 
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Network Log"));

		infoText = new JTextArea(20, 46);
		infoText.setBorder(BorderFactory.createLineBorder(Color.gray));
		infoText.setFont(new Font("courier", Font.PLAIN, 11));
		infoText.setEditable(false);
		JScrollPane infoScroller = new JScrollPane(infoText, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		infoScroller.setPreferredSize(new Dimension(300, 250));
		infoPanel.add(infoScroller, BorderLayout.CENTER);

		controller.add(infoPanel, BorderLayout.SOUTH);

		return controller;
	}
	
//	private void createFrame(String title) {
//		ROSInnerFrame f = new ROSInnerFrame(title);
//		frameContainer.add(f, 1000);
//	}
	
	private void createFrame(TopicInfo topicInfo, int index) {
		String nodeName = ROSUtils.getNodeName(topicInfo.name);
		if (!ROSUtils.checkWatchingTopic(nodeName)) {
			ROSInnerFrame f = new ROSInnerFrame(topicInfo);
			frameContainer.add(f, index);
		} else {
			JOptionPane.showMessageDialog(mainFrame, "This topic has been added", "Monitor", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * call this when user wants to connect to a new server
	 */
	private void initServer() {
		try {
			String serverIP = ipText.getText();
			// initiate server
			ROSUtils.startWithServer(serverIP);
			
			// add topics to the list
			addTopicsToList();
		} catch (Exception e) {
			infoText.setText("Error @ Server Initiation (" + e.getClass().getName() + ": " + e.getMessage() + ")");
		}
	}
	
	private void getTopicInfoTree(TopicInfo topicInfo) {
		// // clear the tree before adding the new info
		// topicInfoTree.setModel(null);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(topicInfo.name);
		
	    //fetching publisher list
		DefaultMutableTreeNode publishers = new DefaultMutableTreeNode("Publishers");
		root.add(publishers);
	    DefaultMutableTreeNode leaf;
	    for (String pubName : topicInfo.topicState.getPublishers()) {
	    	leaf = new DefaultMutableTreeNode(pubName);
	    	publishers.add(leaf);
		}
	    
	    // fetching subscriber list
	    DefaultMutableTreeNode subscribers = new DefaultMutableTreeNode("Subscribers");
	    root.add(subscribers);
	    for (String pubName : topicInfo.topicState.getSubscribers()) {
	    	leaf = new DefaultMutableTreeNode(pubName);
	    	subscribers.add(leaf);
		}
	    
	    DefaultTreeModel model = (DefaultTreeModel) topicInfoTree.getModel();
	    model.setRoot(root);
	    topicInfoTree.setModel(model);
	    expandAll(topicInfoTree);
	    
	}
	
	
	public void expandAll(JTree tree) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		expandAll(tree, new TreePath(root));
	}
	
	@SuppressWarnings("rawtypes")
	private void expandAll(JTree tree, TreePath parent) {
	    TreeNode node = (TreeNode) parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
	      for (Enumeration e = node.children(); e.hasMoreElements();) {
	        TreeNode n = (TreeNode) e.nextElement();
	        TreePath path = parent.pathByAddingChild(n);
	        expandAll(tree, path);
	      }
	    }
	    tree.expandPath(parent);
	  }

	/**
	 * add a topic list to the swing list
	 */
	private void addTopicsToList() {
		// update the main frame title
		mainFrame.setTitle("ROS Monitor v1.0 - " + ipText.getText());
		
		String[] topics = ROSUtils.getTopicNameList(true);
		
		if (topics.length == 0) {
			JOptionPane.showMessageDialog(mainFrame, "ROS Server is unable to connect.");
		} 
		topicList.removeAll();
		topicList.setListData(topics);
	}
	
	
	
	public static void main(String args[]) {
		new RosController().start();
	}
}
