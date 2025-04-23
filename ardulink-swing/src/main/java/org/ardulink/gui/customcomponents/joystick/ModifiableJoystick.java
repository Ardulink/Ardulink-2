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

package org.ardulink.gui.customcomponents.joystick;

import static org.ardulink.gui.facility.AbstractDocumentListenerAdapter.addDocumentListener;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.ardulink.core.Link;
import org.ardulink.gui.Linkable;
import org.ardulink.gui.event.PositionEvent;
import org.ardulink.gui.event.PositionEvent.Point;
import org.ardulink.gui.event.PositionListener;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class ModifiableJoystick extends JPanel implements Linkable, PositionListener {

	private static final long serialVersionUID = -7958194636043905634L;

	private Joystick joy;
	private JTextField idTextField;
	private JLabel horizontalLabel;
	private JLabel verticalLabel;
	
	
	/**
	 * Create the panel.
	 */
	public ModifiableJoystick() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel coordinatePanel = new JPanel();
		add(coordinatePanel, BorderLayout.NORTH);
		coordinatePanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		horizontalLabel = new JLabel("Horizontal: 0");
		coordinatePanel.add(horizontalLabel);
		
		verticalLabel = new JLabel("Vertical: 0");
		coordinatePanel.add(verticalLabel);
		
		joy = new Joystick();
		add(joy, BorderLayout.CENTER);
		
		JPanel idPanel = new JPanel();
		add(idPanel, BorderLayout.SOUTH);
		
		JLabel lblId = new JLabel("ID:");
		idPanel.add(lblId);
		
		idTextField = new JTextField();
		idTextField.setText("none");
		addDocumentListener(idTextField, __ -> joy.setId(idTextField.getText()));
		idPanel.add(idTextField);
		idTextField.setColumns(10);
		
		JLabel lblMaxValue = new JLabel("Max Value:");
		idPanel.add(lblMaxValue);
		
		JSpinner maxValueSpinner = new JSpinner();
		maxValueSpinner.addChangeListener(e -> joy.setJoyOutputRange((Integer) ((JSpinner) e.getSource()).getValue()));
		maxValueSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(255), Integer.valueOf(1), null, Integer.valueOf(1)));
		((JSpinner.DefaultEditor)maxValueSpinner.getEditor()).getTextField().setColumns(4);
		idPanel.add(maxValueSpinner);
		
		joy.addPositionListener(this);
	}

	@Override
	public void setLink(Link link) {
		joy.setLink(link);
	}

	public boolean addPositionListener(PositionListener positionListener) {
		return joy.addPositionListener(positionListener);
	}

	public boolean removePositionListener(PositionListener positionListener) {
		return joy.removePositionListener(positionListener);
	}

	@Override
	public void positionChanged(PositionEvent event) {
		Point position = event.position();
		horizontalLabel.setText("Horizontal: " + position.x);
		verticalLabel.setText("Vertical: " + position.y);
	}
	
	public void setId(String id) {
		idTextField.setText(id);
	}
	
	public String getId() {
		return idTextField.getText();
	}

}
