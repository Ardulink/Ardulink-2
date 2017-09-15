package org.ardulink.camel;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ardulink.core.Pin;
import org.ardulink.util.Lists;

public class EndpointConfig {

	private String type;
	private Map<String, Object> typeParams = Collections.emptyMap();
	private List<Pin> pins = Collections.emptyList();

	public EndpointConfig type(String type) {
		this.type = type;
		return this;
	}

	public EndpointConfig linkParams(Map<String, Object> parameters) {
		this.typeParams = unmodifiableMap(new HashMap<String, Object>(
				parameters));
		return this;
	}

	public EndpointConfig listenTo(Iterable<Pin> pins) {
		this.pins = unmodifiableList(Lists.newArrayList(pins));
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