package org.ardulink.camel;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.util.Iterables;

public class EndpointConfig {

	private final String type;
	private Map<String, Object> typeParams = emptyMap();
	private List<Pin> pins = emptyList();

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

	/**
	 * @deprecated use {@link #listenTo(Collection)}
	 * @param pins the pins to listen to
	 * @return this {@link EndpointConfig}
	 */
	@Deprecated
	public EndpointConfig listenTo(Iterable<Pin> pins) {
		return listenTo(Iterables.stream(pins).collect(toList()));
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

}