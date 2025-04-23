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

import static java.awt.Color.RED;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.lang.Math.min;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

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
public class SimplePositionListener extends JPanel implements PositionListener {

	private static final long serialVersionUID = -315437517373209646L;

	private Point position;
	private int internalMaxSize;

	private static final int POINT_DIM = 15;

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g2 = (Graphics2D) graphics;
		g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
		g2.setColor(RED);
		Dimension dimension = getSize();
		int dim = min(dimension.width, dimension.height) / 2;

		boolean canCalc = position != null && internalMaxSize != 0;
		int x = canCalc ? position.x * dim / internalMaxSize : 0;
		int y = canCalc ? position.y * dim / internalMaxSize : 0;
		g2.fillOval((dimension.width / 2) + x - (POINT_DIM / 2), (dimension.height / 2) - y - (POINT_DIM / 2),
				POINT_DIM, POINT_DIM);
	}

	@Override
	public void positionChanged(PositionEvent event) {
		position = event.position();
		internalMaxSize = event.maxSize();
		repaint();
	}
}
