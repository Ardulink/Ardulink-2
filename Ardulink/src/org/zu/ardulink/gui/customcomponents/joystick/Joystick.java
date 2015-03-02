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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.Linkable;
import org.zu.ardulink.protocol.ReplyMessageCallback;

public class Joystick extends JPanel implements Linkable {

	private Link link = Link.getDefaultInstance();
	private ReplyMessageCallback replyMessageCallback = null;
	private List<PositionListener> positionListeners = new LinkedList<PositionListener>();
	private String id = "none";
	
	// TODO rendere parametrico il border size e trasformare questo in un valore di defalut
	private static final int BORDER_SIZE = 40;
	
	private static final long serialVersionUID = 3725139510642524282L;
	//Maximum value for full horiz or vert position where centered is 0:
    private int joyOutputRange;
    private float joySize;     //joystick icon size
    private float joyWidth, joyHeight;
    private float joyCenterX, joyCenterY;  //Joystick displayed Center

    private float curJoyAngle;    //Current joystick angle
    private float curJoySize;     //Current joystick size
    private boolean isMouseTracking;
    private boolean leftMouseButton;
    private int mouseX, mouseY;
    private Stroke lineStroke = new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private final Point position;
    
    public Joystick() {
    	this(255, 128);
    }

    public Joystick(final int joyOutputRange, final int joySize) {
        this.joyOutputRange = joyOutputRange;
        this.position = new Point();
        setJoySize(joySize);

        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {
                leftMouseButton = SwingUtilities.isLeftMouseButton(e);
                mouseCheck(e);
            }

			@Override
			public void mouseDragged(MouseEvent e) {
				leftMouseButton = SwingUtilities.isLeftMouseButton(e);
                mouseCheck(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
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
    
    public void setInternalSize(int width, int height) {
    	
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

	private void mouseCheck(final MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        float dx = mouseX - joyCenterX;
        float dy = mouseY - joyCenterY;
        if (leftMouseButton) {
            isMouseTracking = true;
        } else {
            isMouseTracking = false;
        }
        if (isMouseTracking) {
            curJoyAngle = (float) Math.atan2(dy, dx);
            curJoySize = (float) Point.distance(mouseX, mouseY,
                    joyCenterX, joyCenterY);
        } else {
            curJoySize = 0;
        }
        if (curJoySize > joySize) {
            curJoySize = joySize;
        }
        position.x = (int) (joyOutputRange * (Math.cos(curJoyAngle)
                * curJoySize) / joySize);
        position.y = (int) (joyOutputRange * (-(Math.sin(curJoyAngle)
                * curJoySize) / joySize));
		repaint();
		callPositionListeners();
		sendMessage();
    }
    
	private void mouseRestore() {
		leftMouseButton = false;
		position.x = 0;
		position.y = 0;
		mouseX = 0;
		mouseY = 0;
		curJoyAngle = 0;
		curJoySize = 0;
		repaint();
		callPositionListeners();
		sendMessage();
    }

    private void callPositionListeners() {
    	Iterator<PositionListener> it = positionListeners.iterator();
    	PositionEvent event = new PositionEvent(new Point(position), joyOutputRange);
    	while (it.hasNext()) {
			PositionListener positionListener = (PositionListener) it.next();
			positionListener.positionChanged(event);
		}
		
	}

    private void sendMessage() {
		String message = getId() + "/" + getValueX() + "/" + getValueY();
		link.sendCustomMessage(message, replyMessageCallback);
	}

    private int getValueX() {
		return position.x;
	}

    private int getValueY() {
		return position.y;
	}

	@Override
    protected void paintComponent(final Graphics g) {
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
	public ReplyMessageCallback getReplyMessageCallback() {
		return replyMessageCallback;
	}

	@Override
	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		this.replyMessageCallback = replyMessageCallback;
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
        joyCenterX = getSize().width / 2;
        joyCenterY = getSize().height / 2;
        this.joySize = joyWidth / 2;
	}
}
