package ardulink.rest;

import static io.restassured.RestAssured.port;
import static io.restassured.RestAssured.*;
import static java.lang.Boolean.parseBoolean;
import static java.util.Arrays.asList;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.ServerSockets.freePort;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;

public class ArdulinkRestTest {

	@Before
	public void setup() {
		port = freePort();
	}

	@Test
	public void canSwitchDigitalPin() throws Exception {
		String out = "mock:result";
		try (CamelContext context = startCamelRest(out)) {
			int pin = 5;
			boolean state = true;
			given().body(state).post("/pin/digital/{pin}", pin).then().statusCode(200);
			MockEndpoint endpoint = mockEndpoint(context, out);
			endpoint.expectedBodiesReceived(asList(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(state)));
			endpoint.assertIsSatisfied();
			context.stop();
		}
	}

	@Test
	public void canSwitchAnalogPin() throws Exception {
		String out = "mock:result";
		try (CamelContext context = startCamelRest(out)) {
			int pin = 9;
			int value = 123;
			given().body(value).post("/pin/analog/{pin}", pin).then().statusCode(200);
			MockEndpoint endpoint = mockEndpoint(context, out);
			endpoint.expectedBodiesReceived(asList(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value)));
			endpoint.assertIsSatisfied();
			context.stop();
		}
	}

	private MockEndpoint mockEndpoint(CamelContext context, String name) {
		return context.getEndpoint(name, MockEndpoint.class);
	}

	private CamelContext startCamelRest(String out) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				restConfiguration().host("localhost").port(port);
				rest("/pin") //
						.consumes("application/json").produces("application/json") //
						.post("/analog/{pin}").to("direct:switchAnalog") //
						.post("/digital/{pin}").to("direct:switchDigital") //
				;

				from("direct:switchAnalog").process(exchange -> {
					Message message = exchange.getMessage();
					Object pinRaw = message.getHeader("pin");
					String valueRaw = message.getBody(String.class);
					int pin = tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
					int value = tryParse(valueRaw).getOrThrow("Value %s not parseable", valueRaw);
					message.setBody(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value));
				}).to(out);

				from("direct:switchDigital").process(exchange -> {
					Message message = exchange.getMessage();
					Object pinRaw = message.getHeader("pin");
					String stateRaw = message.getBody(String.class);
					int pin = tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
					boolean state = parseBoolean(stateRaw);
					message.setBody(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(state));
				}).to(out);
			}
		});
		context.start();
		return context;
	}

}
