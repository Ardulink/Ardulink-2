package ardulink.ardumailng;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class ArdulinkMail {

	private final CamelContext context;

	public ArdulinkMail(String from, String to) throws Exception {
		context = new DefaultCamelContext();
		context.addRoutes(addRoute(from, to));
	}

	public ArdulinkMail start() throws Exception {
		context.start();
		return this;
	}

	public void stop() throws Exception {
		context.stop();
	}

	private RouteBuilder addRoute(final String from, final String to) {
		return new RouteBuilder() {
			@Override
			public void configure() {
				from(from).to(to);
			}
		};
	}

}
