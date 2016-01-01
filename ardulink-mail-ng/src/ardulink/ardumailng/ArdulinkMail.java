package ardulink.ardumailng;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class ArdulinkMail {

	private final CamelContext context;

	public ArdulinkMail(RouteBuilder routeBuilder) throws Exception {
		this(new DefaultCamelContext(), routeBuilder);
	}

	public ArdulinkMail(CamelContext context, RouteBuilder routeBuilder)
			throws Exception {
		this.context = context;
		this.context.addRoutes(routeBuilder);
	}

	public ArdulinkMail start() throws Exception {
		context.start();
		return this;
	}

	public void stop() throws Exception {
		context.stop();
	}

}
