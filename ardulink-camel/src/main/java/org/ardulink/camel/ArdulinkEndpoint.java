package org.ardulink.camel;

import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.URIs;

public class ArdulinkEndpoint extends DefaultEndpoint implements
		MultipleConsumersSupport {

	public static class Config {

		private String type;
		private String typeParams;

		public void setType(String type) {
			this.type = type;
		}

		public void setTypeParams(String typeParams) {
			this.typeParams = typeParams;
		}

	}

	private Config config;
	private final Link link;

	public ArdulinkEndpoint(String uri, ArdulinkComponent ardulinkComponent,
			Config config) {
		super(uri, ardulinkComponent);
		this.config = config;
		this.link = Links.getLink(URIs.newURI(uri));
	}

	@Override
	public Producer createProducer() throws Exception {
		return new ArdulinkProducer(this, config.type, config.typeParams);
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
