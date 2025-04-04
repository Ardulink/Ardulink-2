package org.ardulink.camel;

import static java.lang.String.format;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.DefaultEndpoint;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.Joiner;
import org.ardulink.util.Joiner.MapJoiner;
import org.ardulink.util.Throwables;

public class ArdulinkEndpoint extends DefaultEndpoint implements MultipleConsumersSupport {

	private static final MapJoiner joiner = Joiner.on("&").withKeyValueSeparator("=");

	private final EndpointConfig config;
	private final Link link;

	public ArdulinkEndpoint(String uri, Component ardulinkComponent, EndpointConfig config) throws IOException {
		super(uri, ardulinkComponent);
		this.config = config;
		this.link = createLink();
		for (Pin pin : config.getPins()) {
			this.link.startListening(pin);
		}
	}

	private Link createLink() {
		try {
			String type = checkNotNull(config.getType(), "type must not be null");
			String url = format("ardulink://%s", type);
			Map<String, Object> typeParams = config.getTypeParams();
			return Links.getLink(typeParams.isEmpty() ? url : url + "?" + joiner.join(typeParams));
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Producer createProducer() throws Exception {
		return new ArdulinkProducer(this, this.link);
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		return new ArdulinkConsumer(this, processor, link);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public boolean isMultipleConsumersSupported() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(config, link);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArdulinkEndpoint other = (ArdulinkEndpoint) obj;
		return Objects.equals(config, other.config) && Objects.equals(link, other.link);
	}

}
