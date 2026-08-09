/**
Copyright 2013 project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package org.ardulink.mqtt;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;
import static org.ardulink.mqtt.MqttBroker.builder;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Strings.nullOrEmpty;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.component.paho.PahoComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.RouteController;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.MqttCamelRouteBuilder.MqttConnectionProperties;
import org.ardulink.util.Strings;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttMain {

	public static final String CREDENTIALS_ENV_NAME = "ARDULINK_MQTT_CREDENTIALS";
	public static final String CREDENTIALS_FILE_NAME = "mqtt-credentials";

	private final CommandLineArguments args;

	private final String credentials;

	private CamelContext context;

	private MqttBroker standaloneServer;

	private MqttClient mqttClient;

	private CamelContext createCamelContext(Topics topics) throws Exception {
		return addRoutes(topics, new DefaultCamelContext());
	}

	private CamelContext addRoutes(Topics topics, CamelContext context) throws Exception {
		MqttCamelRouteBuilder rb = new MqttCamelRouteBuilder(context, topics);
		if (args.throttleMillis > 0 && args.compactStrategy != null) {
			rb = rb.compact(args.compactStrategy, args.throttleMillis, MILLISECONDS);
		}
		String ardulink = appendListenTo(args.connection);
		MqttConnectionProperties mqtt = appendAuth(
				new MqttConnectionProperties().name("mqttMain").brokerHost(args.brokerHost).ssl(args.ssl))
				.brokerPort(args.brokerPort);
		if (mqtt.hasAuth()) {
			// inject a preconfigured (and connected) client so that credentials never
			// appear in the endpoint URI (which would get logged by Camel)
			mqttClient = mqtt.newClient();
			((PahoComponent) context.getComponent("paho")).setClient(mqttClient);
		}
		rb.fromSomethingToMqtt(ardulink, mqtt).andReverse();
		return context;
	}

	private String appendListenTo(String connection) {
		String listenTo = listenTo();
		return listenTo.isEmpty() ? connection : connection + separator(connection) + "listenTo=" + listenTo;
	}

	private String separator(String connection) {
		return connection.contains("?") ? "&" : "?";
	}

	protected MqttConnectionProperties appendAuth(MqttConnectionProperties properties) {
		if (nullOrEmpty(credentials)) {
			return properties;
		}
		String[] auth = credentials.split(":");
		checkState(auth.length == 2, "Credentials not in format user:password");
		return properties.user(auth[0]).password(auth[1].getBytes());
	}

	private String listenTo() {
		return Stream.concat(format("A%s", args.analogs), format("D%s", args.digitals)).collect(joining(","));
	}

	private Stream<String> format(String format, int[] pins) {
		return IntStream.of(pins).mapToObj(pin -> String.format(format, pin));
	}

	public static void main(String[] args) throws Exception {
		tryParse(args).map(MqttMain::new).ifPresent(m -> {
			try {
				m.connectToMqttBroker();
				try {
					wait4ever();
				} finally {
					m.close();
				}
			} catch (Exception e) {
				// we have to System#exit because the camel context keeps MqttMain
				// running (even when calling Context#stop)
				e.printStackTrace();
				System.exit(1);
			}
		});
	}

	public MqttMain(CommandLineArguments args) {
		this.args = args.normalize();
		this.credentials = nullOrEmpty(args.credentials) ? resolveCredentials() : args.credentials;
		if (args.standalone) {
			standaloneServer = addCredentials(builder().host(args.brokerHost).useSsl(args.ssl).port(args.brokerPort),
					credentials).startBroker();
		}
	}

	private static String resolveCredentials() {
		String fromEnv = System.getenv(CREDENTIALS_ENV_NAME);
		if (!nullOrEmpty(fromEnv)) {
			return fromEnv;
		}
		Path file = Path.of(System.getProperty("user.home"), ".ardulink", CREDENTIALS_FILE_NAME);
		if (!Files.isReadable(file)) {
			return null;
		}
		checkState(isOwnerOnlyReadable(file), "Refusing to read credentials file %s (must not be readable by group/others)",
				file);
		try {
			return Files.readAllLines(file).stream() //
					.map(String::trim) //
					.filter(Predicate.not(String::isEmpty)) //
					.findFirst() //
					.orElse(null);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private static boolean isOwnerOnlyReadable(Path file) {
		try {
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
			return !permissions.contains(PosixFilePermission.GROUP_READ)
					&& !permissions.contains(PosixFilePermission.OTHERS_READ)
					&& !permissions.contains(PosixFilePermission.GROUP_WRITE)
					&& !permissions.contains(PosixFilePermission.OTHERS_WRITE)
					&& !permissions.contains(PosixFilePermission.GROUP_EXECUTE)
					&& !permissions.contains(PosixFilePermission.OTHERS_EXECUTE);
		} catch (UnsupportedOperationException e) {
			// not a POSIX filesystem, nothing we can check here
			return true;
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private static Builder addCredentials(Builder builder, String credentials) {
		if (Strings.nullOrEmpty(credentials)) {
			return builder;
		}
		String[] split = credentials.split(":");
		return builder.addAuthenication(split[0], split[1].getBytes());
	}

	private static Optional<CommandLineArguments> tryParse(String... args) {
		CommandLineArguments cmdLineArgs = new CommandLineArguments();
		CmdLineParser cmdLineParser = new CmdLineParser(cmdLineArgs);
		try {
			cmdLineParser.parseArgument(args);
			return Optional.of(cmdLineArgs);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return Optional.empty();
		}
	}

	public void connectToMqttBroker() throws Exception {
		Topics topics = args.separateTopics ? Topics.withSeparateReadWriteTopics(args.brokerTopic)
				: Topics.basedOn(args.brokerTopic);
		this.context = createCamelContext(args.control ? topics.withControlChannelEnabled() : topics);
		this.context.start();
	}

	public boolean isConnected() {
		RouteController routeController = context.getRouteController();
		return context.getRoutes().stream() //
				.map(Route::getId) //
				.map(routeController::getRouteStatus) //
				.allMatch(ServiceStatus::isStarted);
	}

	public void close() throws IOException {
		Optional.ofNullable(this.context).ifPresent(CamelContext::stop);
		if (this.mqttClient != null) {
			try {
				this.mqttClient.disconnect();
			} catch (MqttException e) {
				throw propagate(e);
			}
		}
		Optional.ofNullable(this.standaloneServer).ifPresent(MqttBroker::stop);
	}

	private static void wait4ever() throws InterruptedException {
		new CountDownLatch(1).await();
	}

}
