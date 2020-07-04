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
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Strings.nullOrEmpty;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.RouteController;
import org.ardulink.mqtt.MqttBroker.Builder;
import org.ardulink.mqtt.MqttCamelRouteBuilder.MqttConnectionProperties;
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

	private final CommandLineArguments args;
	
	private CamelContext context;

	private MqttBroker standaloneServer;
	
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
		rb.fromSomethingToMqtt(ardulink, mqtt).andReverse();
		return context;
	}

	private String appendListenTo(String connection) {
		String listenTo = listenTo();
		return listenTo.isEmpty() ? connection
				: connection + (connection.contains("?") ? "&" : "?") + "listenTo=" + listenTo;
	}

	private MqttConnectionProperties appendAuth(MqttConnectionProperties properties) {
		if (nullOrEmpty(args.credentials)) {
			return properties;
		}
		String[] auth = args.credentials.split(":");
		checkState(auth.length == 2, "Credentials not in format user:password");
		return properties.auth(auth[0], auth[1].getBytes());
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
		this.args = args;
		ensureBrokerTopicIsnormalized(args);
		if (args.standalone) {
			standaloneServer = createBroker().startBroker();
		}
	}

	// TODO PF move to CommandLineArgs
	private static void ensureBrokerTopicIsnormalized(CommandLineArguments args) {
		if (args.brokerTopic != null && !args.brokerTopic.endsWith("/")) {
			args.brokerTopic = args.brokerTopic + '/';
		}
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

	protected Builder createBroker() {
		return MqttBroker.builder().host(args.brokerHost).useSsl(args.ssl).port(args.brokerPort);
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
		Optional.ofNullable(this.standaloneServer).ifPresent(MqttBroker::stop);
	}

	private static void wait4ever() throws InterruptedException {
		Object blocker = new Object();
		synchronized (blocker) {
			while (true) {
				blocker.wait();
			}
		}
	}

}
