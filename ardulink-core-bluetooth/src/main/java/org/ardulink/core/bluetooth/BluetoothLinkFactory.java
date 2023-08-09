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

package org.ardulink.core.bluetooth;

import static javax.bluetooth.ServiceRecord.NOAUTHENTICATE_NOENCRYPT;
import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;

import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;

import org.ardulink.core.ByteStreamProcessorProvider;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class BluetoothLinkFactory implements LinkFactory<BluetoothLinkConfig> {

	@Override
	public String getName() {
		return "bluetooth";
	}

	@Override
	public ConnectionBasedLink newLink(BluetoothLinkConfig config) throws IOException {
		String url = getURL(config);
		checkState(url != null, "The connection could not be made. Connection url not found");
		javax.microedition.io.StreamConnection streamConnection = getStreamConnection(Connector.open(url));
		return new ConnectionBasedLink(new StreamConnection(streamConnection.openInputStream(),
				streamConnection.openOutputStream(), protoByName(ArdulinkProtocol2.NAME).newByteStreamProcessor()));
	}

	public String getURL(BluetoothLinkConfig config) {
		return getServiceRecord(config).getConnectionURL(NOAUTHENTICATE_NOENCRYPT, false);
	}

	public ServiceRecord getServiceRecord(BluetoothLinkConfig config) {
		ServiceRecord serviceRecord = BluetoothDiscoveryUtil.getDevices().get(config.getDeviceName());
		checkState(serviceRecord != null, "The connection could not be made. Device not discovered");
		return serviceRecord;
	}

	public javax.microedition.io.StreamConnection getStreamConnection(javax.microedition.io.Connection connection)
			throws IOException {
		if (connection instanceof StreamConnectionNotifier) {
			return ((StreamConnectionNotifier) connection).acceptAndOpen();
		} else if (connection instanceof ByteStreamProcessorProvider) {
			return (javax.microedition.io.StreamConnection) connection;
		} else {
			throw new IllegalStateException("Connection class not known. " + connection.getClass().getName());
		}
	}

	@Override
	public BluetoothLinkConfig newLinkConfig() {
		return new BluetoothLinkConfig();
	}

}
