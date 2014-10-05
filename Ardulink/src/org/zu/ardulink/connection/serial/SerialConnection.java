package org.zu.ardulink.connection.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.connection.ConnectionContact;

/**
 * [ardulinktitle] [ardulinkversion]
 * Used to simplify communication over a Serial port. Using the RXTX-library
 * (rxtx.qbang.org), one connection per instance of this class can be handled.
 * In addition to handling a connection, information about the available Serial
 * ports can be received using this class.
 * 
 * A separate {@link Thread} is started to handle messages that are being
 * received over the Serial interface.
 * 
 * This class also makes packages out of a stream of bytes received, using a
 * {@link #divider}, and sending these packages as an array of <b>int</b>s (each
 * between 0 and 255) to a function implemented by a class implementing the
 * {@link org.zu.ardulink.connection.ConnectionContact}-interface.
 * 
 * @author Raphael Blatter (raphael@blatter.sg)
 * @author heavily using code examples from the RXTX-website (rxtx.qbang.org)
 * 
 * <p>
 * Luciano Zu Ardulink has added Connection implementation interface to allow multiple connections.
 * </p>
 * <p>
 * Luciano Zu Ardulink has heavily refactored this class (its original name was gnu.io.net.Network)
 * </p> 
 * 
 * [adsense]
 */
public class SerialConnection extends AbstractSerialConnection implements Connection {
	
	private SerialPort serialPort;

	/**
	 * @param id
	 *            <b>int</b> identifying the specific instance of the
	 *            SerialConnection-class. While having only a single instance,
	 *            {@link #id} is irrelevant. However, having more than one open
	 *            connection (using more than one instance of SerialConnection),
	 *            {@link #id} helps identifying which Serial connection a
	 *            message or a log entry came from.
	 * 
	 * @param contact
	 *            Link to the instance of the class implementing
	 *            {@link org.zu.ardulink.connection.ConnectionContact}.
	 * 
	 * @param divider
	 *            A small <b>int</b> representing the number to be used to
	 *            distinguish between two consecutive packages. It can take a
	 *            value between 0 and 255. Note that data is only sent to
	 *            {@link org.zu.ardulink.connection.ConnectionContact#parseInput(int, int, int[])} once the
	 *            following {@link #divider} could be identified.
	 */
	public SerialConnection(String id, ConnectionContact contact, int divider) {
		super(id, contact, divider);
	}

	/**
	 * Just as {@link #SerialConnection(int, ConnectionContact, int)}, but with a default
	 * {@link #divider} of <b>255</b>.
	 * 
	 * @see #SerialConnection(int, ConnectionContact, int)
	 */
	public SerialConnection(String id, ConnectionContact contact) {
		super(id, contact);
	}

	/**
	 * Just as {@link #SerialConnection(int, ConnectionContact, int)}, but with a default
	 * {@link #divider} of <b>255</b> and a default {@link #id} of 0. This
	 * constructor may mainly be used if only one Serial connection is needed at
	 * any time.
	 * 
	 * @see #SerialConnection(int, ConnectionContact, int)
	 */
	public SerialConnection(ConnectionContact contact) {
		super(contact);
	}

	/**
	 * This method is used to get a list of all the available Serial ports
	 * (note: only Serial ports are considered). Any one of the elements
	 * contained in the returned {@link Vector} can be used as a parameter in
	 * {@link #connect(String)} or {@link #connect(String, int)} to open a
	 * Serial connection.
	 * 
	 * @return A {@link Vector} containing {@link String}s showing all available
	 *         Serial ports.
	 *         
	 * Luciano Zu has modified return type from Vector to List
	 */
	@SuppressWarnings("unchecked")
	public List<String> getPortList() {
		Enumeration<CommPortIdentifier> portList;
		Vector<String> portVect = new Vector<String>();
		portList = CommPortIdentifier.getPortIdentifiers();

		CommPortIdentifier portId;
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portVect.add(portId.getName());
			}
		}
		writeLog("found the following ports:");
		for (int i = 0; i < portVect.size(); i++) {
			writeLog("   " + (String) portVect.elementAt(i));
		}

		return portVect;
	}

	/**
	 * Just as {@link #connect(String, int)}, but using 115200 bps as a default
	 * speed of the connection.
	 * 
	 * @param portName
	 *            The name of the port the connection should be opened to (see
	 *            {@link #getPortList()}).
	 * @return <b>true</b> if the connection has been opened successfully,
	 *         <b>false</b> otherwise.
	 * @see #connect(String, int)
	 */
	public boolean connect(String portName) {
		return connect(portName, 115200);
	}

	/**
	 * Opening a connection to the specified Serial port, using the specified
	 * speed. After opening the port, messages can be sent using
	 * {@link #writeSerial(String)} and received data will be packed into
	 * packets (see {@link #divider}) and forwarded using
	 * {@link org.zu.ardulink.connection.ConnectionContact#parseInput(int, int, int[])}.
	 * 
	 * @param portName
	 *            The name of the port the connection should be opened to (see
	 *            {@link #getPortList()}).
	 * @param speed
	 *            The desired speed of the connection in bps.
	 * @return <b>true</b> if the connection has been opened successfully,
	 *         <b>false</b> otherwise.
	 */
	public boolean connect(String portName, int speed) {
		boolean retvalue = false;
		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				writeLog("Error: Port is currently in use");
			} else {
				serialPort = (SerialPort) portIdentifier.open("RTBug_network",2000);
				serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				setInputStream(serialPort.getInputStream());
				setOutputStream(serialPort.getOutputStream());

				startReader();
				writeLog("connection on " + portName + " established");
				getContact().connected(getId(), portName);
				setConnected(true);
				retvalue = true;
			}
		} catch (Exception e) {
			writeLog("the connection could not be made " + e.getMessage());
			e.printStackTrace();
		}
		return retvalue;
	}

	/**
	 * Simple function closing the connection held by this instance of
	 * {@link org.zu.ardulink.connection.serial.SerialConnection}. It also ends the Thread {@link org.zu.ardulink.connection.serial.SerialConnection#reader}.
	 * 
	 * @return <b>true</b> if the connection could be closed, <b>false</b>
	 *         otherwise.
	 */
	public boolean disconnect() {
		if(isConnected()) {
			stopReader();
			serialPort.close();
			setConnected(false);
		}
		getContact().disconnected(getId());
		writeLog("connection disconnected");
		return !isConnected();
	}

	/*
	 * Method added by Luciano Zu - Ardulink
	 */
	@Override
	public boolean connect(Object... params) {
		String portName = null;
		Integer baudRate = null;
		if(params == null || params.length < 1) {
			throw new RuntimeException("This connection accepts a String port name and a Integer baud rate. Only the port name is mandatory. Null or zero arguments passed.");
		}
		if(!(params[0] instanceof String)) {
			throw new RuntimeException("This connection accepts a String port name and a Integer baud rate. Only the port name is mandatory. First argument was not a String");
		} else {
			portName =(String)params[0]; 
		}
		if(params.length > 1 && !(params[1] instanceof Integer)) {
			throw new RuntimeException("This connection accepts a String port name and a Integer baud rate. Only the port name is mandatory. Second argument was not an Integer");
		} else {
			if(params.length > 1) {
				baudRate = (Integer)params[1];
			}
		}

		boolean retvalue = false;
		if(baudRate == null) {
			retvalue = connect(portName);
		} else {
			retvalue = connect(portName, baudRate);
		}
		return retvalue;
	}
}
