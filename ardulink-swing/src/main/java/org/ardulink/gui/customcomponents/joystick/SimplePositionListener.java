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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import org.ardulink.gui.event.PositionEvent;
import org.ardulink.gui.event.PositionListener;

/**
 * [ardulinktitle] [ardulinkversion]
* project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class SimplePositionListener extends JPanel implements PositionListener {

	private static final long serialVersionUID = -315437517373209646L;
	private Point position;
	private int internalMaxSize = 1;
	
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
        	x = position.x * dim / internalMaxSize;
        	y = position.y * dim / internalMaxSize;
        }
        g2.fillOval((dimension.width / 2) + x - (POINT_DIM / 2), (dimension.height / 2) - y - (POINT_DIM / 2), POINT_DIM, POINT_DIM);
    }

	@Override
	public void positionChanged(PositionEvent e) {
		position = e.getPosition();
		internalMaxSize = e.getMaxSize();
		repaint();
	}
}
