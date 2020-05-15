package org.ardulink.rest;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.identityHashCode;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.rest.main.RestMain;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.webjars.WebJarAssetLocator;

public class RestRouteBuilder extends RouteBuilder {

	public static final String VAR_TARGET = "to.uri";
	public static final String VAR_HOST = "from.host";
	public static final String VAR_BIND = "from.bind";
	public static final String VAR_PORT = "from.port";

	private static final String target = fromPlaceholder(VAR_TARGET);

	private static final String HEADER_PIN = "Pin";
	private static final String HEADER_TYPE = "Type";

	private static String fromPlaceholder(String var) {
		return "{{" + var + "}}";
	}

	@Override
	public void configure() throws Exception {
		BlockingQueue<FromDeviceMessagePinStateChanged> messages = new ArrayBlockingQueue<FromDeviceMessagePinStateChanged>(
				16);
		String patchAnalog = "direct:patchAnalog-" + identityHashCode(this);
		String patchDigital = "direct:patchDigital-" + identityHashCode(this);
		String readAnalog = "direct:readAnalog-" + identityHashCode(this);
		String readDigital = "direct:readDigital-" + identityHashCode(this);
		String switchAnalog = "direct:switchAnalog-" + identityHashCode(this);
		String switchDigital = "direct:switchDigital-" + identityHashCode(this);

		String apidocs = "/api-docs";
		restConfiguration() //
				.component("jetty") //
//				.bindingMode(RestBindingMode.json) //
				.host(fromPlaceholder(VAR_BIND)) //
				.port(fromPlaceholder(VAR_PORT)) //
//				.contextPath("/") //
//				.setEnableCORS(true) //
		;
		swagger(apidocs);
		swaggerUi(apidocs);

		rest("/pin") //
				.consumes("application/json").produces("application/json") //
				.patch("/analog/{pin}").to(patchAnalog) //
				.patch("/digital/{pin}").to(patchDigital) //
				.get("/analog/{pin}").to(readAnalog) //
				.get("/digital/{pin}").to(readDigital) //
				.post("/analog/{pin}").to(switchAnalog) //
				.post("/digital/{pin}").to(switchDigital) //
		;
		from(patchAnalog).process(exchange -> patchAnalog(exchange)).to(target);
		from(patchDigital).process(exchange -> patchDigital(exchange)).to(target);
		from(readAnalog).process(exchange -> readAnalog(exchange)).process(exchange -> readQueue(exchange, messages));
		from(readDigital).process(exchange -> readDigital(exchange)).process(exchange -> readQueue(exchange, messages));
		from(switchAnalog).process(exchange -> switchAnalog(exchange)).to(target);
		from(switchDigital).process(exchange -> switchDigital(exchange)).to(target);
		writeArduinoMessagesTo(target, messages);
	}

	private void swagger(String apidocs) {
		restConfiguration().apiContextPath(apidocs) //
				.apiProperty("host", "localhost" + ":" + fromPlaceholder(VAR_PORT)) //
				.apiProperty("base.path", "api-docs") //
				.apiProperty("api.title", "User API") //
				.apiProperty("api.version", "1.0.0") //
				.apiProperty("cors", "true") //
		;
	}

	private void swaggerUi(String apidocs) throws URISyntaxException {
		Map<String, String> webJars = new WebJarAssetLocator("META-INF/resources/webjars/").getWebJars();
		String key = "swagger-ui";
		String version = checkNotNull(webJars.get(key), key);
		String name = key + "Handler";

		registerResourceHandler(name,
				RestMain.class.getClassLoader().getResource("META-INF/resources/webjars/").toURI());
		restConfiguration().endpointProperty("handlers", name);
		from("jetty:http://" + fromPlaceholder(VAR_BIND) + ":" + fromPlaceholder(VAR_PORT) + "/api-browser") //
				.process(exchange -> {
					Message message = exchange.getMessage();
					message.setHeader(HTTP_RESPONSE_CODE, 302);
					message.setHeader("location", "/swagger-ui/" + version + "/index.html?url=" + apidocs + "#");
				});
	}

	private void registerResourceHandler(String id, URI resource) throws URISyntaxException {
		getContext().getRegistry().bind(id, ResourceHandler.class, resourceHandler(resource));
	}

	private ResourceHandler resourceHandler(URI resourceURI) throws URISyntaxException {
		ResourceHandler rh = new ResourceHandler();
		rh.setResourceBase(resourceURI.toASCIIString());
		return rh;
	}

	private void readQueue(Exchange exchange, BlockingQueue<FromDeviceMessagePinStateChanged> messages)
			throws InterruptedException {
		FromDeviceMessagePinStateChanged polled = checkNotNull(messages.poll(1, SECONDS),
				"Timout retrieving message from arduino");
		Message message = exchange.getMessage();
		if (Integer.compare(polled.getPin().pinNum(), ((int) message.getHeader(HEADER_PIN))) == 0) {
			message.setBody(polled.getValue(), String.class);
		} else {
			messages.offer(polled);
		}
	}

	private void patchDigital(Exchange exchange) {
		patch(exchange, START_LISTENING_DIGITAL, STOP_LISTENING_DIGITAL);
	}

	private void patchAnalog(Exchange exchange) {
		patch(exchange, START_LISTENING_ANALOG, STOP_LISTENING_ANALOG);
	}

	private void patch(Exchange exchange, ALPProtocolKey startKey, ALPProtocolKey stopKey) {
		Message message = exchange.getMessage();
		Object pinRaw = message.getHeader("pin");
		String stateRaw = message.getBody(String.class);

		String[] split = stateRaw.split("=");
		checkState(split.length == 2, "Could not split %s by =", stateRaw);
		checkState(split[0].equalsIgnoreCase("listen"), "Expected listen=${state} but was %s", stateRaw);

		int pin = tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
		boolean state = parseBoolean(split[1]);
		message.setBody(alpProtocolMessage(state ? startKey : stopKey).forPin(pin).withoutValue());
	}

	private void readAnalog(Exchange exchange) {
		setTypeAndPinHeader(exchange, ANALOG);
	}

	private void readDigital(Exchange exchange) {
		setTypeAndPinHeader(exchange, DIGITAL);
	}

	private void setTypeAndPinHeader(Exchange exchange, Type type) {
		Message message = exchange.getMessage();
		setTypeAndPinHeader(message, type, readPin(type, message));
	}

	private Message setTypeAndPinHeader(Message message, Type type, int pin) {
		message.setHeader(HEADER_PIN, pin);
		message.setHeader(RestRouteBuilder.HEADER_TYPE, type);
		return message;
	}

	private int readPin(Type type, Message message) {
		Object pinRaw = message.getHeader("pin");
		return tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
	}

	private void writeArduinoMessagesTo(String arduino, BlockingQueue<FromDeviceMessagePinStateChanged> messages) {
		Protocol proto = ArdulinkProtocol2.instance();
		from(arduino).process(exchange -> {
			String body = exchange.getMessage().getBody(String.class);
			FromDeviceMessage fromDevice = proto.fromDevice(body.getBytes());
			if (fromDevice instanceof FromDeviceMessagePinStateChanged) {
				messages.add((FromDeviceMessagePinStateChanged) fromDevice);
			}
		});
	}

	private void switchDigital(Exchange exchange) {
		Message message = exchange.getMessage();
		Object pinRaw = message.getHeader("pin");
		String stateRaw = message.getBody(String.class);
		int pin = tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
		boolean state = parseBoolean(stateRaw);
		message.setBody(alpProtocolMessage(DIGITAL_PIN_READ).forPin(pin).withState(state));
	}

	private void switchAnalog(Exchange exchange) {
		Message message = exchange.getMessage();
		Object pinRaw = message.getHeader("pin");
		String valueRaw = message.getBody(String.class);
		int pin = tryParse(String.valueOf(pinRaw)).getOrThrow("Pin %s not parseable", pinRaw);
		int value = tryParse(valueRaw).getOrThrow("Value %s not parseable", valueRaw);
		message.setBody(alpProtocolMessage(ANALOG_PIN_READ).forPin(pin).withValue(value));
	}
}