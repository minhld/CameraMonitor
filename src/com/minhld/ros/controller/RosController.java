package com.minhld.ros.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
	
	String selectedTopic;
	// int selectedTopicIndex;
	
	public void run() {
		mainFrame = new JFrame("Robot Monitor v1.0");
		ImageIcon mainIcon = new ImageIcon("images/monitor.png");
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
		mainFrame.setSize(1390, 980);
		mainFrame.setMinimumSize(new Dimension(1280, 860));
		mainFrame.setLocationRelativeTo(null);
		// mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int response = JOptionPane.showConfirmDialog(RosController.this.mainFrame, 
									"Are you sure you want to quit?", "Confirm", 
									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                	// close all nodes 
                	ROSUtils.shutdownAllNodes();
                	System.exit(0);
                }
			}
		});
//		mainFrame.addWindowListener(new WindowListener() {
//			@Override
//			public void windowOpened(WindowEvent e) { }
//			
//			@Override
//			public void windowIconified(WindowEvent e) { }
//			
//			@Override
//			public void windowDeiconified(WindowEvent e) { }
//			
//			@Override
//			public void windowDeactivated(WindowEvent e) { }
//			
//			@Override
//			public void windowClosing(WindowEvent e) {
//				int response = JOptionPane.showConfirmDialog(RosController.this.mainFrame, 
//									"Are you sure you want to quit?", "Confirm", 
//									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
//                if (response == JOptionPane.YES_OPTION) {
//                	// close all nodes 
//                	ROSUtils.shutdownAllNodes();
//                	System.exit(0);
//                }
//			}
//			
//			@Override
//			public void windowClosed(WindowEvent e) { }
//			
//			@Override
//			public void windowActivated(WindowEvent e) { }
//		});
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
		refreshBtn.setIcon(new ImageIcon("images/refresh.png"));
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
		// String currentIP = AppUtils.getCurrentIP();
		String currentIP = "129.123.7.100"; 
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
	            
	            // String selectedTopic = (String) list.getSelectedValue();
	            RosController.this.selectedTopic = (String) list.getSelectedValue();
	            // RosController.this.selectedTopicIndex = list.getSelectedIndex();
	            
	            if (RosController.this.selectedTopic == null || RosController.this.selectedTopic.equals("")) {
	            	JOptionPane.showMessageDialog(mainFrame, "Please enter a ROS Server IP and subscribe to that server.", 
	            						"Info", JOptionPane.INFORMATION_MESSAGE);
	            	ipText.grabFocus();
	            	ipText.selectAll();
	            	return;
	            }
	            
	            TopicInfo topicInfo = ROSUtils.topics.get(RosController.this.selectedTopic);
		        if (e.getClickCount() == 2) {
		        	// Double-click detected
		        	// int index = list.locationToIndex(e.getPoint());
		            
		        	// open corresponding window for a topic
		            // createFrame(topicInfo, RosController.this.selectedTopicIndex);
		        	createFrame(topicInfo);
		            
		            // update the node list of the topic - temporarily removed  
		            // addTopicsToList();
		        } else if (e.getClickCount() == 1) {
		        	// single-click detected
		        	String topicInfoText = ROSUtils.getTopicInfo(RosController.this.selectedTopic);
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
		topicInfoTree = new TopicTree();
        
		JScrollPane topicInfoScroller = new JScrollPane(topicInfoTree, 
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		topicInfoScroller.setPreferredSize(new Dimension(300, 150));
		topicPanel.add(topicInfoScroller, BorderLayout.SOUTH);
		
		controller.add(topicPanel, BorderLayout.CENTER);

		
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
		infoPanel.add(infoScroller, BorderLayout.CENTER);

		controller.add(infoPanel, BorderLayout.SOUTH);

		return controller;
	}
	
	// private void createFrame(TopicInfo topicInfo, int index) {
	private void createFrame(TopicInfo topicInfo) {
		String nodeName = ROSUtils.getNodeName(topicInfo.name);
		if (!ROSUtils.checkWatchingTopic(nodeName)) {
			ROSInnerFrame f = new ROSInnerFrame(topicInfo);
			
			// add to the desktop pane
			frameContainer.add(f);
			
			// add to the frame list managed by our application
			AppUtils.innerFramesInfo.put(nodeName, frameContainer.getComponentCount() - 1);
			
		} else {
			JOptionPane.showMessageDialog(mainFrame, "This topic has been added", "Info", JOptionPane.WARNING_MESSAGE);
		}
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
		publishers.setUserObject("Publishers");
		root.add(publishers);
	    DefaultMutableTreeNode leaf;
	    for (String pubName : topicInfo.topicState.getPublishers()) {
	    	leaf = new DefaultMutableTreeNode(pubName);
	    	// leaf.setUserObject("Publisher-Node");
	    	publishers.add(leaf);
		}
	    
	    // fetching subscriber list
	    DefaultMutableTreeNode subscribers = new DefaultMutableTreeNode("Subscribers");
	    subscribers.setUserObject("Subscribers");
	    root.add(subscribers);
	    for (String pubName : topicInfo.topicState.getSubscribers()) {
	    	leaf = new DefaultMutableTreeNode(pubName);
	    	// leaf.setUserObject("Subscriber-Node");
	    	subscribers.add(leaf);
		}
	    
	    DefaultTreeModel model = new DefaultTreeModel(root);// (DefaultTreeModel) topicInfoTree.getModel();
	    // model.setRoot(root);
	    topicInfoTree.setModel(model);
	    expandAll(topicInfoTree);
	    
	}
	
	
	/**
	 * this is to expand all nodes of a tree (tree is very annoying)
	 * 
	 * @param tree
	 */
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
			JOptionPane.showMessageDialog(mainFrame, "ROS Server is unable to connect.", "Warning", JOptionPane.WARNING_MESSAGE);
		} 
		topicList.removeAll();
		topicList.setListData(topics);
	}
	
	/**
	 * <b>TopicTree</b> customizes the JTree to support displaying topic
	 * information with a context menu to control the items
	 * 
	 * @author lee
	 *
	 */
	private class TopicTree extends JTree implements ActionListener {
		private static final long serialVersionUID = 1L;
		JPopupMenu popup;
		
		public TopicTree() {
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			setModel(null);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()) {
						popup.show((JComponent) e.getSource(), e.getX(), e.getY());
					}
				}
				
			});
			// decorate the topic tree
			setCellRenderer(new TopicCellRenderer());
			
			// add the context menu
			addTopicTreeContextMenu();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("refresh")) {
				// refresh the list
		    	refreshTopicInfoTree();
				
		    } else if (e.getActionCommand().equals("delete")) {
		    	// delete a topic
		    	DefaultMutableTreeNode selectedTopic = (DefaultMutableTreeNode) topicInfoTree.getSelectionPath().getLastPathComponent();
		    	String selectedTopicText = selectedTopic.getUserObject().toString();
		    	
		    	// confirm deletion
		    	int response = JOptionPane.showConfirmDialog(RosController.this.mainFrame, 
								"Deleting topic \"" + selectedTopicText + "\"?", "Confirm", 
								JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			    if (response == JOptionPane.YES_OPTION) {
			    	// yes, i am sure 
			    	ROSUtils.shutdownNode(selectedTopicText);
			    	
			    	// find and remove the frame
			    	int frameIndex = AppUtils.innerFramesInfo.get(selectedTopicText);
			    	frameContainer.remove(frameIndex);
			    	frameContainer.validate();
			    	frameContainer.repaint();
			    	
			    	// refresh the list
			    	refreshTopicInfoTree();
			    }
		    }
		}
		
		/**
		 * add the context menu
		 */
		private void addTopicTreeContextMenu() {
			popup = new JPopupMenu();
			JMenuItem item = new JMenuItem("Refresh");
			item.addActionListener(this);
			item.setActionCommand("refresh");
			item.setIcon(new ImageIcon("images/refresh.png"));
			popup.add(item);
			popup.addSeparator();
			item = new JMenuItem("Delete Node");
			item.addActionListener(this);
			item.setActionCommand("delete");
			item.setIcon(new ImageIcon("images/remove.png"));
			popup.add(item);
//			item = new JMenuItem("Delete Node");
//			item.setIcon(new ImageIcon("images/remove.png"));
//			popup.add(item);
		}
		
		/**
		 * this class is to decorate the Pub-Sub Tree with new icon
		 * to distinguish the publisher, subscriber and the root.
		 * 
		 * @author lee
		 *
		 */
		@SuppressWarnings("serial")
		class TopicCellRenderer extends DefaultTreeCellRenderer {
			Icon topicIcon = new ImageIcon("images/topic.png");
			Icon publishIcon = new ImageIcon("images/publish.png");
			Icon subscribeIcon = new ImageIcon("images/subscribe.png");
			Icon subPubIcon = new ImageIcon("images/sub_pub.png");
			Icon subSubIcon = new ImageIcon("images/sub_sub.png");
			Icon anyIcon = new ImageIcon("images/any.png");
			
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, 
								boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				
				DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
				String nodeName = (String) ((DefaultMutableTreeNode) value).getUserObject();
		        if (tree.getModel().getRoot().equals(nodo)) {
		            setIcon(topicIcon);
		        } else if (nodeName.equals("Publishers")) {
		            setIcon(publishIcon);
		        } else if (nodeName.equals("Subscribers")) {
		        	setIcon(subscribeIcon);
		        } else if (nodeName.contains("/pub/")) {
		        	setIcon(subPubIcon);
		        } else if (nodeName.contains("/sub/")) {
		        	setIcon(subSubIcon);
		        } else if (nodo.isLeaf()) {
		        	setIcon(anyIcon);
		        }
		        return this;
			}
		}
		
		private void refreshTopicInfoTree() {
			// refresh the topic list
			addTopicsToList();
			
			// reload the topic tree
			TopicInfo topicInfo = ROSUtils.topics.get(RosController.this.selectedTopic);
			getTopicInfoTree(topicInfo);
		}
	}
	
	
	public static void main(String args[]) {
		new RosController().start();
	}
}
