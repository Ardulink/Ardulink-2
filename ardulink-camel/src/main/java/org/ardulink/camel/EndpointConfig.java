package org.ardulink.camel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;

public class EndpointConfig {

	private final String type;
	private Map<String, Object> typeParams = Map.of();
	private List<Pin> pins = List.of();

	public static EndpointConfig endpointConfigWithType(String type) {
		return new EndpointConfig(type);
	}

	private EndpointConfig(String type) {
		this.type = type;
	}

	public EndpointConfig linkParams(Map<String, Object> parameters) {
		this.typeParams = Map.copyOf(parameters);
		return this;
	}

	public EndpointConfig listenTo(Collection<Pin> pins) {
		this.pins = List.copyOf(pins);
		return this;
	}

	public String getType() {
		return type;
	}

	public Map<String, Object> getTypeParams() {
		return typeParams;
	}

	public List<Pin> getPins() {
		return pins;
	}

	@Override
	public String toString() {
		return "EndpointConfig [type=" + type + ", typeParams=" + typeParams + ", pins=" + pins + "]";
	}

}