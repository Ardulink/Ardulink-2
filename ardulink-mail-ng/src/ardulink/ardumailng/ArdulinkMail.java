package ardulink.ardumailng;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;

public class ArdulinkMail {

	private final CamelContext context;

	public ArdulinkMail(String from, String... tos) throws Exception {
		context = new DefaultCamelContext();
		context.addRoutes(addRoute(from, tos));
	}

	public ArdulinkMail start() throws Exception {
		context.start();
		return this;
	}

	public void stop() throws Exception {
		context.stop();
	}

	private RouteBuilder addRoute(final String from, final String... tos) {
		return new RouteBuilder() {
			@Override
			public void configure() {
				RouteDefinition routeDef = from(from);
				for (String to : tos) {
					routeDef = routeDef.to(to);
				}
			}
		};
	}

}
