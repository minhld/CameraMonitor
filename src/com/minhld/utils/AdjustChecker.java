package com.minhld.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AdjustChecker extends JPanel {
	private static final long serialVersionUID = 8183929984967684854L;
	
	String title;
	JCheckBox checker;
	
	public AdjustChecker(String label, int defaultValue) {
		this.title = label;
		int currVal = Settings.getValue(this.title);
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 60));

		checker = new JCheckBox(label);
		checker.setSize(200, 15);
		checker.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// update setting object
				Settings.setValue(AdjustChecker.this.title, checker.isSelected() ? 1 : 0);
			}
		});
		
		checker.setSelected(currVal == 1);
		checker.setFocusable(false);
		add(checker, BorderLayout.CENTER);

	}
	
	public AdjustChecker(String label, int defaultValue, boolean enabled) {
		this(label, defaultValue);
		checker.setEnabled(enabled);
	}
}
