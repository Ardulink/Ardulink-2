package org.ardulink.core.proto.api.bytestreamproccesors;

public interface State {
	AbstractState process(byte b);
}