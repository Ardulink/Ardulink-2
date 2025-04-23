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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.ardulink.core.Link;
import org.ardulink.gui.Linkable;
import org.ardulink.gui.event.PositionEvent;
import org.ardulink.gui.event.PositionEvent.Point;
import org.ardulink.gui.event.PositionListener;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion]
* project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class Joystick extends JPanel implements Linkable {

	private transient Link link;
	
	private final List<PositionListener> positionListeners = new ArrayList<>();
	private String id = "none";
	
	// TODO Make the border size parametric and turn this into a default value
	private static final int BORDER_SIZE = 40;
	
	private static final long serialVersionUID = 3725139510642524282L;
	// Maximum value for full horizontal or vertical position where centered is 0:
    private int joyOutputRange;
    private float joySize; // Joystick icon size
    private float joyWidth;
    private float joyHeight;
    private float joyCenterX; // Joystick displayed Center
    private float joyCenterY; // Joystick displayed Center

    private float curJoyAngle; // Current joystick angle
    private float curJoySize;  // Current joystick size
    private boolean isMouseTracking;
    private boolean leftMouseButton;
    private int mouseX;
    private int mouseY;
    private transient Stroke lineStroke = new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private Point position = new Point(0, 0);
    
    public Joystick() {
    	this(255, 128);
    }

    public Joystick(int joyOutputRange, int joySize) {
        this.joyOutputRange = joyOutputRange;
        setJoySize(joySize);

        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent event) {
                leftMouseButton = SwingUtilities.isLeftMouseButton(event);
                mouseCheck(event);
            }

			@Override
			public void mouseDragged(MouseEvent event) {
				mousePressed(event);
			}

			@Override
			public void mouseReleased(MouseEvent __) {
				mouseRestore();
			}
            
        };
        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
    }
    
    @Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		setInternalSize(width, height);
	}
    
    public void setInternalSize(float width, float height) {
    	
    	joyWidth = Math.min(width, height) - BORDER_SIZE;
        joyHeight = joyWidth;

        joyCenterX = width / 2;
        joyCenterY = height;
        setJoySize(joyWidth);		
    }

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		setInternalSize(width, height);
	}

	@Override
	public void setBounds(Rectangle r) {
		super.setBounds(r);
		setInternalSize(r.width, r.height);
	}

	private void mouseCheck(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        float dx = mouseX - joyCenterX;
        float dy = mouseY - joyCenterY;
		isMouseTracking = leftMouseButton;
        if (isMouseTracking) {
            curJoyAngle = (float) Math.atan2(dy, dx);
            curJoySize = (float) Point2D.distance(mouseX, mouseY,
                    joyCenterX, joyCenterY);
        } else {
            curJoySize = 0;
        }
        if (curJoySize > joySize) {
            curJoySize = joySize;
        }
        int x = (int) (joyOutputRange * (Math.cos(curJoyAngle)
                * curJoySize) / joySize);
        int y = (int) (joyOutputRange * (-(Math.sin(curJoyAngle)
                * curJoySize) / joySize));
        this.position = new Point(x, y);
		repaint();
		callPositionListeners();
		sendMessage();
    }
    
	private void mouseRestore() {
		leftMouseButton = false;
		position = new Point(0, 0);
		mouseX = 0;
		mouseY = 0;
		curJoyAngle = 0;
		curJoySize = 0;
		repaint();
		callPositionListeners();
		sendMessage();
    }

    private void callPositionListeners() {
    	PositionEvent event = new PositionEvent(new Point(position.x, position.y), joyOutputRange, id);
    	for (PositionListener positionListener : positionListeners) {
			positionListener.positionChanged(event);
		}
		
	}

    private void sendMessage() {
		if (link != null) {
			try {
				link.sendCustomMessage(getId(), String.valueOf(getValueX()), String.valueOf(getValueY()));
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}
	}

    private int getValueX() {
		return position.x;
	}

    private int getValueY() {
		return position.y;
	}

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillOval((int) (joyCenterX - joyWidth / 2),
                (int) (joyCenterY - joyHeight / 2),
                (int) joyWidth, (int) joyHeight);
        //rotate and draw joystick line segment:
        Graphics2D g3 = (Graphics2D) g2.create();
        g3.translate(joyCenterX, joyCenterY);
        g3.rotate(curJoyAngle);
        g3.setColor(Color.GRAY);
        g3.setStroke(lineStroke);
        g3.drawLine(0, 0, (int) curJoySize, 0);
        g3.dispose();

        g2.setColor(Color.GRAY);
        g2.fillOval((int) joyCenterX - 10, (int) joyCenterY - 10, 20, 20);
    }

	public Point getPosition() {
		return position;
	}

	@Override
	public void setLink(Link link) {
		this.link = link;
	}
	
	public boolean addPositionListener(PositionListener positionListener) {
		return positionListeners.add(positionListener);
	}
	
	public boolean removePositionListener(PositionListener positionListener) {
		return positionListeners.remove(positionListener);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getJoyOutputRange() {
		return joyOutputRange;
	}

	public void setJoyOutputRange(int joyOutputRange) {
		this.joyOutputRange = joyOutputRange;
	}

	public float getJoySize() {
		return joySize;
	}

	public void setJoySize(float joySize) {
        this.joySize = joySize;
        joyWidth = joySize;
        joyHeight = joyWidth;
        setPreferredSize(new Dimension((int) joyWidth + BORDER_SIZE,
                (int) joyHeight + BORDER_SIZE));
        joyCenterX = ((float) getSize().width) / 2;
        joyCenterY = ((float) getSize().height) / 2;
        this.joySize = joyWidth / 2;
	}

}
