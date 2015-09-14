/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink.gui.customcomponents.joystick;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class SimplePositionListener extends JPanel implements PositionListener {

	private static final long serialVersionUID = -315437517373209646L;
	private Point position = null;
	private int maxSize = 1;
	
	private static final int POINT_DIM = 15;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.RED);
        int x = 0;
        int y = 0;
        Dimension dimension = getSize();
        int dim = Math.min(dimension.width, dimension.height) / 2;
        if(position != null) {
        	x = position.x * dim / maxSize;
        	y = position.y * dim / maxSize;
        }
        g2.fillOval((dimension.width / 2) + x - (POINT_DIM / 2), (dimension.height / 2) - y - (POINT_DIM / 2), POINT_DIM, POINT_DIM);
        // g2.drawRect(0, 0, dimension.width - 1, dimension.height - 1);
    }

	@Override
	public void positionChanged(PositionEvent e) {
		position = e.getPosition();
		maxSize = e.getMaxSize();
		repaint();
	}
}
