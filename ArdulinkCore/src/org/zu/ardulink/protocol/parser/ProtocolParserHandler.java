package org.zu.ardulink.protocol.parser;

import static java.util.Collections.synchronizedMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.zu.ardulink.protocol.ALProtocol;
import org.zu.ardulink.protocol.IProtocol;

/**
 * [ardulinktitle] [ardulinkversion]
 * Class to manage the protocol parser available set. With this class you can install a new protocol parser for ardulink.
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @see IProtocol
 * @see ALProtocol
 * 
 * [adsense]
 *
 */
public class ProtocolParserHandler {
	
	private static final Map<String, IProtocolParser> installedParserImplementations = synchronizedMap(new HashMap<String, IProtocolParser>());
	
	static {
		ALProtocolParser alProtocolParser = new ALProtocolParser();
		installProtocolParserImplementation(alProtocolParser);
	}
	
	/**
	 * @param implementationName
	 * @return return the protocol implementation called implementationName (if installed)
	 */
	public static IProtocolParser getProtocolParserImplementation(String implementationName) {
		return installedParserImplementations.get(implementationName);
	}

	/**
	 * @return all available parser protocol implementations
	 */
	public static Set<String> getInstalledProtocolParserImplementationNames() {
		return installedParserImplementations.keySet();
	}
	
	/**
	 * Install a protocol.
	 * @param protocol
	 * @return
	 */
	public static boolean installProtocolParserImplementation(IProtocolParser parserProtocol) {
		boolean retvalue = false;
		if(parserProtocol != null) {
			installedParserImplementations.put(parserProtocol.getProtocolName(), parserProtocol);
			retvalue = true;
		}
		return retvalue;
	}
}
