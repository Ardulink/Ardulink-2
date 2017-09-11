package org.ardulink.camel;

import static java.util.Collections.unmodifiableList;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.Joiner;
import org.ardulink.util.Joiner.MapJoiner;
import org.ardulink.util.Lists;
import org.ardulink.util.Throwables;
import org.ardulink.util.URIs;

public class ArdulinkEndpoint extends DefaultEndpoint implements
		MultipleConsumersSupport {

	public static class Config {

		private String type;
		private Map<String, Object> typeParams = Collections.emptyMap();
		private List<Pin> pins = Collections.emptyList();

		public void setType(String type) {
			this.type = type;
		}

		public void setLinkParams(Map<String, Object> parameters) {
			this.typeParams = new HashMap<String, Object>(parameters);
		}

		public void setListenTo(List<Pin> pins) {
			this.pins = unmodifiableList(Lists.newArrayList(pins));
		}

	}

	private static final MapJoiner joiner = Joiner.on("&")
			.withKeyValueSeparator("=");

	private Config config;
	private final Link link;

	public ArdulinkEndpoint(String uri, ArdulinkComponent ardulinkComponent,
			Config config) throws IOException {
		super(uri, ardulinkComponent);
		this.config = config;
		this.link = createLink();

		for (Pin pin : config.pins) {
			this.link.startListening(pin);
		}
	}

	private Link createLink() {
		try {
			return Links.getLink(URIs.newURI(appendParams("ardulink://"
					+ checkNotNull(config.type, "type must not be null"),
					config.typeParams)));
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Producer createProducer() throws Exception {
		return new ArdulinkProducer(this, this.link);
	}

	private static String appendParams(String base,
			Map<String, Object> typeParams) {
		return typeParams.isEmpty() ? base : base + "?"
				+ joiner.join(typeParams);
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		return new ArdulinkConsumer(this, processor);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public boolean isMultipleConsumersSupported() {
		return true;
	}

	public Link getLink() {
		return link;
	}

}
