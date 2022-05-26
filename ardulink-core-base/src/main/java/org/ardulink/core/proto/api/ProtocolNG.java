package org.ardulink.core.proto.api;

import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;

public interface ProtocolNG {

	String getName();

	ByteStreamProcessor newByteStreamProcessor();
	
}