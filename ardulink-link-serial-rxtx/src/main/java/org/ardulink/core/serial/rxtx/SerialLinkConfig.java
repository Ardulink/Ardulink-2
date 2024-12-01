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

package org.ardulink.core.serial.rxtx;

import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static gnu.io.CommPortIdentifier.getPortIdentifiers;
import static java.util.Collections.list;
import static org.ardulink.core.featureflags.PreviewFeature.isBoardPasswordFeatureEnabled;
import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.ardulink.core.proto.api.Protocols.protocolNames;
import static org.ardulink.core.proto.api.Protocols.tryProtoByName;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Optionals.or;

import java.util.List;
import java.util.stream.Stream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkConfig.I18n;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocols;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

import gnu.io.CommPortIdentifier;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@I18n("message")
public class SerialLinkConfig implements LinkConfig {
	
	private static final String NAMED_PROTO = "proto";

	private static final String NAMED_PORT = "port";

	private static final String NAMED_SECRET = "secret";
	
	@Named(NAMED_PORT)
	public String port;

	@Named("baudrate")
	@Positive
	public int baudrate = 115200;

	private Protocol protocol = useProtoOrFallback(ArdulinkProtocol2.NAME);

	@Named("qos")
	public boolean qos;

	@PositiveOrZero
	@Max(59)
	@Named("waitsecs")
	public int waitsecs = 10;

	@Named("pingprobe")
	public boolean pingprobe = true;

	@Named(NAMED_SECRET)
	public String secret;

	private Protocol useProtoOrFallback(String prefered) {
		return or(tryProtoByName(prefered), () -> getFirst(Protocols.protocols())).orElse(null);
	}

	@ChoiceFor(NAMED_PORT)
	public Stream<String> listPorts() {
		return portIdentifiers().stream().filter(this::isSerial).map(CommPortIdentifier::getName);
	}

	private boolean isSerial(CommPortIdentifier identifier) {
		return identifier.getPortType() == PORT_SERIAL;
	}

	@ChoiceFor(NAMED_PROTO)
	public List<String> availableProtos() {
		return protocolNames();
	}

	@Named(NAMED_PROTO)
	public String getProtoName() {
		return protocol == null ? null : protocol.getName();
	}

	@Named(NAMED_PROTO)
	public void setProtoName(String protoName) {
		this.protocol = protoByName(protoName);
	}

	public Protocol protocol() {
		return protocol;
	}

	@SuppressWarnings("unchecked")
	private List<CommPortIdentifier> portIdentifiers() {
		return list(getPortIdentifiers());
	}

	@Override
	public boolean isDisabled(String attributeName) {
		return !isBoardPasswordFeatureEnabled() && NAMED_SECRET.equals(attributeName);
	}

}
