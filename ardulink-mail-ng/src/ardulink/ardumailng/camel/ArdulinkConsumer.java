package ardulink.ardumailng.camel;

import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

public class ArdulinkConsumer extends DefaultConsumer {

	public ArdulinkConsumer(ArdulinkEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}
	


}
