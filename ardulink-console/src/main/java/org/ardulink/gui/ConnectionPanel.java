/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.ardulink.gui;

import static java.awt.Color.RED;
import static java.awt.event.ItemEvent.SELECTED;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.linkmanager.LinkManager.extractNameFromURI;
import static org.ardulink.gui.GridBagConstraintsBuilder.constraints;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.gui.facility.UtilityGeometry;
import org.ardulink.legacy.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConnectionPanel extends JPanel implements Linkable {

	private static final long serialVersionUID = 1290277902714226253L;

	private final JComboBox uris = createURICombo();

	private final PanelBuilder fallback = new GenericPanelBuilder();

	private Link link;

	private Configurer configurer;

	private JPanel panel;

	private JComboBox createURICombo() {
		JComboBox uris = new JComboBox();
		uris.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 2756587449741341859L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				try {
					return super.getListCellRendererComponent(list,
							extractNameFromURI(new URI((String) value)), index,
							isSelected, cellHasFocus);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
		});
		uris.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED) {
					replaceSubpanel();
				}
			}
		});
		return uris;
	}

	/**
	 * Create the panel.
	 */
	public ConnectionPanel() {
		setLayout(new GridBagLayout());
		add(new JLabel("Type"), constraints(0, 0).build());
		add(uris, constraints(0, 1).fillHorizontal().build());
		add(refreshButton(), constraints(0, 2).build());

		LinkManager linkManager = LinkManager.getInstance();
		for (URI uri : linkManager.listURIs()) {
			uris.addItem(uri.toASCIIString());
		}
	}

	private Component refreshButton() {
		JButton refreshButton = new JButton("refresh");
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				replaceSubpanel();
			}
		});
		return refreshButton;
	}

	private void replaceSubpanel() {
		Component windowAncestor = SwingUtilities.getRoot(this);
		final WaitDialog waitDialog = new WaitDialog(
				windowAncestor instanceof Window ? (Window) windowAncestor
						: null);
		UtilityGeometry.setAlignmentCentered(waitDialog);

		new SwingWorker<JPanel, Void>() {

			@Override
			protected void done() {
				try {
					exchangePanel(get());
				} catch (InterruptedException e) {
					errorPanel(e);
				} catch (ExecutionException e) {
					errorPanel(e);
				} finally {
					add(ConnectionPanel.this.panel, constraints(1, 0)
							.gridwidth(3).fillBoth().build());
					synchronized (this) {
						waitDialog.dispose();
					}
				}
			}

			private void errorPanel(Exception e) {
				JPanel newPanel = new JPanel();
				newPanel.setBackground(RED);
				newPanel.add(new JLabel(e.getMessage()));
				exchangePanel(newPanel);
				throw new RuntimeException(e);
			}

			private void exchangePanel(JPanel newPanel) {
				if (ConnectionPanel.this.panel != null) {
					remove(ConnectionPanel.this.panel);
				}
				ConnectionPanel.this.panel = newPanel;
				add(ConnectionPanel.this.panel, constraints(1, 0).fillBoth().build());
				revalidate();
			}

			@Override
			protected JPanel doInBackground() {
				displayIn(waitDialog, 1, SECONDS);
				return createSubpanel();
			}

			private void displayIn(final WaitDialog waitDialog,
					final int timeout, final TimeUnit tu) {
				Executors.newCachedThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						try {
							tu.sleep(timeout);
							synchronized (this) {
								if (!isDone()) {
									waitDialog.setVisible(true);
								}
							}
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}

					}
				});
			}

		}.execute();
	}

	private JPanel createSubpanel() {
		try {
			URI uri = new URI(String.valueOf(uris.getSelectedItem()));
			JPanel subpanel = findPanelBuilder(uri).createPanel(
					configurer = LinkManager.getInstance().getConfigurer(uri));
			subpanel.setBorder(BorderFactory.createLoweredBevelBorder());
			return subpanel;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private PanelBuilder findPanelBuilder(URI uri) {
		// Here we could place a discover mechanism
		// ServiceLoader<PanelBuilder> loader =
		// ServiceLoader.load(PanelBuilder.class);
		// if none of the loaded PanelBuilder supports the URI we would fall
		// back to GenericPanelBuilder.
		// To not create circular dependencies PanelBuilder should be moved to
		// an own module (e.g. ardulink-ui-support) so ardulink-console would
		// depend on ardulink-ui-support and the module providing a specific
		// PanelBuilder would depend on ardulink-ui-support, too.
		if (fallback.canHandle(uri)) {
			return fallback;
		}
		throw new IllegalStateException("No PanelBuilder found for " + uri);
	}

	public org.ardulink.core.Link createLink() throws Exception {
		return this.configurer == null ? null : this.configurer.newLink();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Component[] components = getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(enabled);
		}
	}

	private Link getLink() {
		return link;
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
}
