package org.ardulink.gui.connectionpanel;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class WaitDialog extends JDialog {

	private static final long serialVersionUID = -7897193872896320730L;

	public WaitDialog(Window window) {
		super(window);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle("Searching...");
		setBounds(100, 100, 335, 112);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createContentPanel(), BorderLayout.CENTER);
	}

	private JPanel createContentPanel() {
		JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.add(createProgressBar());
		return contentPanel;
	}

	private JProgressBar createProgressBar() {
		JProgressBar progressBar = new JProgressBar(0, 1);
		progressBar.setIndeterminate(true);
		return progressBar;
	}

}
