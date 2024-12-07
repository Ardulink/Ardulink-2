package org.ardulink.rest;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.identityHashCode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Matcher.quoteReplacement;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessors.parse;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.START_LISTENING_DIGITAL;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_ANALOG;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.STOP_LISTENING_DIGITAL;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Primitives.tryParseAs;
import static org.ardulink.util.StopWatch.Countdown.createStarted;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.Type;
import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessagePinStateChanged;
import org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2.ALPByteStreamProcessor;
import org.ardulink.util.StopWatch.Countdown;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.EmptyResource;
import org.eclipse.jetty.util.resource.Resource;
import org.webjars.WebJarAssetLocator;

public class RestRouteBuilder extends RouteBuilder {

	private static final String ANALOG_PIN = "/analog/{pin}";
	private static final String DIGITAL_PIN = "/digital/{pin}";
	private static final String APPLICATION_TEXT = "application/text";

	private static final String META_INF_RESOURCES_WEBJARS = "META-INF/resources/webjars";

	public static final String VAR_TARGET = "to.uri";
	public static final String VAR_HOST = "from.host";
	public static final String VAR_BIND = "from.bind";
	public static final String VAR_PORT = "from.port";

	private static final String target = fromPlaceholder(VAR_TARGET);

	private static final String HEADER_PIN = "Pin";
	private static final String HEADER_TYPE = "Type";

	private static String fromPlaceholder(String varName) {
		return "{{" + varName + "}}";
	}

	@Override
	public void configure() throws Exception {
		AtomicReference<FromDeviceMessagePinStateChanged> messageRef = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(1);

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
				.consumes("application/octet-stream").produces("application/json") //
				.patch(ANALOG_PIN).consumes(APPLICATION_TEXT).type(String.class).to(patchAnalog) //
				.patch(DIGITAL_PIN).consumes(APPLICATION_TEXT).type(String.class).to(patchDigital) //
				.get(ANALOG_PIN).to(readAnalog) //
				.get(DIGITAL_PIN).to(readDigital) //
				.put(ANALOG_PIN).consumes(APPLICATION_TEXT).type(String.class).to(switchAnalog) //
				.put(DIGITAL_PIN).consumes(APPLICATION_TEXT).type(String.class).to(switchDigital) //
		;
		from(patchAnalog).process(exchange -> patchAnalog(exchange)).to(target);
		from(patchDigital).process(exchange -> patchDigital(exchange)).to(target);
		from(readAnalog).process(exchange -> readAnalog(exchange))
				.process(exchange -> readQueue(exchange, messageRef, latch));
		from(readDigital).process(exchange -> readDigital(exchange))
				.process(exchange -> readQueue(exchange, messageRef, latch));
		from(switchAnalog).process(exchange -> switchAnalog(exchange)).to(target);
		from(switchDigital).process(exchange -> switchDigital(exchange)).to(target);
		writeArduinoMessagesTo(target, messageRef, latch);
	}

	private void swagger(String apidocs) {
		restConfiguration().apiContextPath(apidocs) //
				.apiProperty("host", "localhost" + ":" + fromPlaceholder(VAR_PORT)) //
				.apiProperty("base.path", "") //
				.apiProperty("api.title", "User API") //
				.apiProperty("api.version", "1.0.0") //
				.apiProperty("cors", "true") //
		;
	}

	private void swaggerUi(String apidocs) throws URISyntaxException {
		WebJarAssetLocator webJarAssetLocator = new WebJarAssetLocator(META_INF_RESOURCES_WEBJARS);
		Map<String, String> webJars = webJarAssetLocator.getWebJars();
		String swaggerUi = "swagger-ui";
		String version = checkNotNull(webJars.get(swaggerUi), swaggerUi);
		String swaggerUiHandler = swaggerUi + "Handler";

		String initializer = "/" + ("swagger-ui/" + version + "/swagger-initializer.js");

		registerResourceHandler(swaggerUiHandler, webJarsURI(), initializer);
		restConfiguration().endpointProperty("handlers", swaggerUiHandler);
		from("jetty:http://" + fromPlaceholder(VAR_BIND) + ":" + fromPlaceholder(VAR_PORT) + initializer)
				.filter(isGet(initializer)).setBody().simple(patch(initializer, apidocs));
		from("jetty:http://" + fromPlaceholder(VAR_BIND) + ":" + fromPlaceholder(VAR_PORT) + "/api-browser")
				.process(exchange -> redirect(exchange.getMessage(), "/swagger-ui/" + version));
	}

	private static String patch(String initializer, String apidocs) {
		return content(META_INF_RESOURCES_WEBJARS + initializer)
				.replaceAll(quoteReplacement("https://petstore.swagger.io/v2/swagger.json"), apidocs);
	}

	private static String content(String in) {
		try {
			return new String(RestRouteBuilder.class.getClassLoader().getResourceAsStream(in).readAllBytes(), UTF_8);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private static Predicate isGet(String initializer) {
		return exchange -> initializer.equals(exchange.getMessage().getHeader("CamelHttpUri"));
	}

	private static URI webJarsURI() throws URISyntaxException {
		return RestRouteBuilder.class.getClassLoader().getResource(META_INF_RESOURCES_WEBJARS).toURI();
	}

	private void registerResourceHandler(String id, URI resource, String ignore) throws URISyntaxException {
		getContext().getRegistry().bind(id, ResourceHandler.class, resourceHandler(resource, ignore));
	}

	private static ResourceHandler resourceHandler(URI resourceURI, String ignore) throws URISyntaxException {
		ResourceHandler rh = new ResourceHandler() {
			@Override
			public Resource getResource(String path) {
				return path.equals(ignore) ? EmptyResource.INSTANCE : super.getResource(path);
			}
		};
		rh.setResourceBase(resourceURI.toASCIIString());
		return rh;
	}

	private static void redirect(Message message, String location) {
		message.setHeader(HTTP_RESPONSE_CODE, 302);
		message.setHeader("location", location);
	}

	private static void readQueue(Exchange exchange, AtomicReference<FromDeviceMessagePinStateChanged> messageRef,
			CountDownLatch latch) throws InterruptedException {
		Message message = exchange.getMessage();
		Pin pinOfMessage = extractPin(message);

		for (Countdown countdown = createStarted(1, SECONDS); !countdown.finished();) {
			if (latch.await(countdown.remaining(MILLISECONDS), MILLISECONDS)) {
				FromDeviceMessagePinStateChanged polled = messageRef.get();
				if (pinOfMessage.equals(polled.getPin())) {
					message.setBody(polled.getValue(), String.class);
					return;
				}
			}
		}
		throw new IllegalStateException("Timeout retrieving message from arduino");
	}

	private static void patchDigital(Exchange exchange) {
		patch(exchange, START_LISTENING_DIGITAL, STOP_LISTENING_DIGITAL);
	}

	private static void patchAnalog(Exchange exchange) {
		patch(exchange, START_LISTENING_ANALOG, STOP_LISTENING_ANALOG);
	}

	private static void patch(Exchange exchange, ALPProtocolKey startKey, ALPProtocolKey stopKey) {
		Message message = exchange.getMessage();
		String stateRaw = message.getBody(String.class);

		String[] split = stateRaw.split("=");
		checkState(split.length == 2, "Could not split %s by =", stateRaw);
		checkState(split[0].equalsIgnoreCase("listen"), "Expected listen=${state} but was %s", stateRaw);

		int pin = extractPinNumber(message);
		boolean state = parseBoolean(split[1]);
		message.setBody(alpProtocolMessage(state ? startKey : stopKey).forPin(pin).withoutValue());
	}

	private static Pin extractPin(Message message) {
		return createPin(extractType(message), extractPinNumber(message));
	}

	private static Pin createPin(Type type, int num) {
		switch (type) {
		case ANALOG:
			return analogPin(num);
		case DIGITAL:
			return digitalPin(num);
		default:
			throw new IllegalStateException("Cannot handle type " + type);
		}
	}

	private static Type extractType(Message message) {
		return message.getHeader(HEADER_TYPE, Type.class);
	}

	private static int extractPinNumber(Message message) {
		return message.getHeader(HEADER_PIN, int.class);
	}

	private static void readAnalog(Exchange exchange) {
		setTypeAndPinHeader(exchange, ANALOG);
	}

	private static void readDigital(Exchange exchange) {
		setTypeAndPinHeader(exchange, DIGITAL);
	}

	private static void setTypeAndPinHeader(Exchange exchange, Type type) {
		Message message = exchange.getMessage();
		setTypeAndPinHeader(message, type, extractPinNumber(message));
	}

	private static Message setTypeAndPinHeader(Message message, Type type, int pin) {
		message.setHeader(HEADER_PIN, pin);
		message.setHeader(HEADER_TYPE, type);
		return message;
	}

	private void writeArduinoMessagesTo(String arduino, AtomicReference<FromDeviceMessagePinStateChanged> messageRef,
			CountDownLatch latch) {
		ALPByteStreamProcessor byteStreamProcessor = new ALPByteStreamProcessor();
		from(arduino).process(exchange -> {
			String body = exchange.getMessage().getBody(String.class);
			FromDeviceMessage fromDevice = getFirst(parse(byteStreamProcessor, byteStreamProcessor.toBytes(body)))
					.orElseThrow(() -> new IllegalStateException("Cannot handle " + body));
			if (fromDevice instanceof FromDeviceMessagePinStateChanged) {
				messageRef.set((FromDeviceMessagePinStateChanged) fromDevice);
				latch.countDown();
			}
		});
	}

	private static void switchDigital(Exchange exchange) {
		Message message = exchange.getMessage();
		boolean state = parseBoolean(message.getBody(String.class));
		message.setBody(alpProtocolMessage(DIGITAL_PIN_READ).forPin(extractPinNumber(message)).withState(state));
	}

	private static void switchAnalog(Exchange exchange) {
		Message message = exchange.getMessage();
		String rawValue = message.getBody(String.class);
		int value = tryParseAs(Integer.class, rawValue)
				.orElseThrow(() -> new IllegalStateException(String.format("Value %s not parseable", rawValue)));
		message.setBody(alpProtocolMessage(ANALOG_PIN_READ).forPin(extractPinNumber(message)).withValue(value));
	}

}
