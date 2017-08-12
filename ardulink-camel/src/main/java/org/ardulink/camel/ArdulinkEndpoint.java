package org.ardulink.camel;

import java.io.IOException;

import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.ardulink.camel.command.Command;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.messages.api.LinkMessageAdapter;
import org.ardulink.util.ListMultiMap;
import org.ardulink.util.Throwables;
import org.ardulink.util.URIs;

public class ArdulinkEndpoint extends DefaultEndpoint implements
		MultipleConsumersSupport {

	public static class Config {

		private String type;
		private String typeParams;
		private ListMultiMap<String, Command> commands = new ListMultiMap<String, Command>();

		public void setType(String type) {
			this.type = type;
		}

		public void addCommand(String name, Command command) {
			this.commands.put(name, command);
		}

		public void setTypeParams(String typeParams) {
			this.typeParams = typeParams;
		}

	}

	private Config config;
	private final Link link;
	private LinkMessageAdapter linkMessageAdapter;
	
	public ArdulinkEndpoint(String uri, ArdulinkComponent ardulinkComponent,
			Config config) {
		super(uri, ardulinkComponent);
		this.config = config;
		link = LinkManager.getInstance().getConfigurer(URIs.newURI(uri))
				.newLink();
		try {
			linkMessageAdapter = new LinkMessageAdapter(link);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
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

	public LinkMessageAdapter getLinkMessageAdapter() {
		return linkMessageAdapter;
	}

}
