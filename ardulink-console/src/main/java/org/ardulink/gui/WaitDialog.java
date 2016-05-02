package org.ardulink.gui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class WaitDialog extends JDialog {

	private static final long serialVersionUID = -7897193872896320730L;

	private final JPanel contentPanel = new JPanel();
	private final JProgressBar progressBar;

	/**
	 * Create the dialog.
	 */
	public WaitDialog() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setModal(true);
		setTitle("Searching...");
		setBounds(100, 100, 335, 112);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			progressBar = new JProgressBar(0, 1);
			progressBar.setIndeterminate(true);
			contentPanel.add(progressBar);
		}
	}

	public void stopProgressBar() {
		progressBar.setIndeterminate(false);
		progressBar.setValue(1);
	}
}
