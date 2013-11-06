package gnu.io.net;

/**
 * [ardulinktitle]
 * An instance of a class implementing this interface has to be passed to the
 * constructor of {@link gnu.io.net.Network}. It will be used by {@link gnu.io.net.Network} to
 * forward received messages, write to a log and take action when the connection
 * is closed.
 * 
 * @see gnu.io.net.Network#Network(int, Network_iface, int)
 * 
 * @author Raphael Blatter (raphael@blatter.sg)
 * 
 * [adsense]
 */
public interface Network_iface {
	/**
	 * Is called to write connection information to the log. The information can
	 * either be ignored, directed to stdout or written out to a specialized
	 * field or file in the program.
	 * 
	 * @param id
	 *            The <b>String</b> passed to
	 *            {@link gnu.io.net.Network#Network(String, Network_iface, int)} in the
	 *            constructor. It can be used to identify which instance (which
	 *            connection) a message comes from, when several instances of
	 *            {@link gnu.io.net.Network} are connected to the same instance of a
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
	 * {@link gnu.io.net.Network#divider}s passed via the constructor of
	 * {@link gnu.io.net.Network} (
	 * {@link gnu.io.net.Network#Network(int, Network_iface, int)}), without the
	 * {@link gnu.io.net.Network#divider}s. Messages are only forwarded using this
	 * function, once a {@link gnu.io.net.Network#divider} has been recognized in the
	 * incoming stream.
	 * 
	 * @param id
	 *            The <b>String</b> passed to
	 *            {@link gnu.io.net.Network#Network(int, Network_iface, int)} in the
	 *            constructor. It can be used to identify which instance a
	 *            message comes from, when several instances of
	 *            {@link gnu.io.net.Network} are connected to the same instance of a
	 *            class implementing this interface.
	 *            
	 *            Luciano Zu has modified variable type from int to String
	 *            
	 * @param numBytes
	 *            Number of valid bytes contained in the message
	 * @param message
	 *            Message received over the Serial interface. The complete array
	 *            of bytes (as <b>int</b>s between 0 and 255) between
	 *            {@link gnu.io.net.Network#divider} is sent (without
	 *            {@link gnu.io.net.Network#divider}s).
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
	 *            {@link gnu.io.net.Network#id} of the corresponding
	 *            {@link gnu.io.net.Network} instance (see {@link gnu.io.net.Network#id}).
	 */
	public void networkDisconnected(String id);

	/**
	 * Is called when the network has been connected. This call can e.g. be
	 * used to show the connection status in a GUI or inform the user using
	 * other means.
	 * 
	 * This method is added for Ardulink project.
	 * 
	 * @param id
	 *            {@link gnu.io.net.Network#id} of the corresponding
	 *            {@link gnu.io.net.Network} instance (see {@link gnu.io.net.Network#id}).
	 * @author Luciano Zu
	 */
	public void networkConnected(String id, String portName);
}
