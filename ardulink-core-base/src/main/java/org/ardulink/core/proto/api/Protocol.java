package org.ardulink.core.proto.api;

import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;

public interface Protocol {

	String getName();

	ByteStreamProcessor newByteStreamProcessor();
	
}