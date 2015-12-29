package ardulink.ardumailng.camel;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.zu.ardulink.util.ListMultiMap;

import ardulink.ardumailng.Command;

public class ArdulinkEndpoint extends DefaultEndpoint implements
		MultipleConsumersSupport {

	private Config config;

	public static class Config {

		private String type;
		private String typeParams;
		private List<String> validfroms;
		private ListMultiMap<String, Command> commands = new ListMultiMap<String, Command>();

		public void setValidFroms(String... validfroms) {
			this.validfroms = Arrays.asList(validfroms.clone());
		}

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

	public ArdulinkEndpoint(String uri, ArdulinkComponent ardulinkComponent,
			Config config) {
		super(uri, ardulinkComponent);
		this.config = config;
	}

	@Override
	public Producer createProducer() throws Exception {
		ArdulinkProducer ardulinkProducer = new ArdulinkProducer(this,
				config.type, config.typeParams);
		ardulinkProducer.setValidFroms(config.validfroms);
		for (Entry<String, List<Command>> entry : config.commands.asMap()
				.entrySet()) {
			ardulinkProducer.setCommands(entry.getKey(), entry.getValue());
		}
		return ardulinkProducer;
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public boolean isMultipleConsumersSupported() {
		return true;
	}

}
