package org.zu.ardulink.connection;


/**
 * [ardulinktitle] [ardulinkversion]
 * An instance of a class implementing this interface has to be passed to the
 * constructor of {@link org.zu.ardulink.connection.serial.SerialConnection}. It will be used by {@link org.zu.ardulink.connection.serial.SerialConnection} to
 * forward received messages, write to a log and take action when the connection
 * is closed.
 * 
 * @see org.zu.ardulink.connection.serial.SerialConnection#SerialConnection(int, ConnectionContact, int)
 * 
 * @author Raphael Blatter (raphael@blatter.sg)
 * <p>
 * Luciano Zu Ardulink has heavily refactored this interface (its original name was gnu.io.net.Network_iface)
 * </p> 
 * [adsense]
 */
public interface ConnectionContact {
	/**
	 * Is called to write connection information to the log. The information can
	 * either be ignored, directed to stdout or written out to a specialized
	 * field or file in the program.
	 * 
	 * @param id
	 *            The <b>String</b> passed to
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection#SerialConnection(String, ConnectionContact, int)} in the
	 *            constructor. It can be used to identify which instance (which
	 *            connection) a message comes from, when several instances of
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection} are connected to the same instance of a
	 *            class implementing this interface.
	 *            
	 *            Luciano Zu has modified variable type from int to String
	 * @param text
	 *            The text to be written into the log in human readable form.
	 *            Corresponds to information about the connection or ports.
	 */
	public void writeLog(String id, String text);

	/**
	 * Is called when sequence of bytes are received over the Serial interface.
	 * It sends the bytes (as <b>int</b>s between 0 and 255) between the two
	 * {@link org.zu.ardulink.connection.serial.SerialConnection#divider}s passed via the constructor of
	 * {@link org.zu.ardulink.connection.serial.SerialConnection} (
	 * {@link org.zu.ardulink.connection.serial.SerialConnection#SerialConnection(int, ConnectionContact, int)}), without the
	 * {@link org.zu.ardulink.connection.serial.SerialConnection#divider}s. Messages are only forwarded using this
	 * function, once a {@link org.zu.ardulink.connection.serial.SerialConnection#divider} has been recognized in the
	 * incoming stream.
	 * 
	 * @param id
	 *            The <b>String</b> passed to
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection#SerialConnection(int, ConnectionContact, int)} in the
	 *            constructor. It can be used to identify which instance a
	 *            message comes from, when several instances of
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection} are connected to the same instance of a
	 *            class implementing this interface.
	 *            
	 *            Luciano Zu has modified variable type from int to String
	 *            
	 * @param numBytes
	 *            Number of valid bytes contained in the message
	 * @param message
	 *            Message received over the Serial interface. The complete array
	 *            of bytes (as <b>int</b>s between 0 and 255) between
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection#divider} is sent (without
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection#divider}s).
	 */
	public void parseInput(String id, int numBytes, int[] message);

	/**
	 * Is called when the network has been disconnected. This call can e.g. be
	 * used to show the connection status in a GUI or inform the user using
	 * other means.
	 * 
	 *            Luciano Zu has modified variable type from int to String
	 * 
	 * @param id
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection#id} of the corresponding
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection} instance (see {@link org.zu.ardulink.connection.serial.SerialConnection#id}).
	 */
	public void disconnected(String id);

	/**
	 * Is called when the network has been connected. This call can e.g. be
	 * used to show the connection status in a GUI or inform the user using
	 * other means.
	 * 
	 * This method is added for Ardulink project.
	 * 
	 * @param id
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection#id} of the corresponding
	 *            {@link org.zu.ardulink.connection.serial.SerialConnection} instance (see {@link org.zu.ardulink.connection.serial.SerialConnection#id}).
	 * @author Luciano Zu
	 */
	public void connected(String id, String portName);
}
