package org.ardulink.rest.main;

import static org.ardulink.rest.RestRouteBuilder.VAR_BIND;
import static org.ardulink.rest.RestRouteBuilder.VAR_PORT;
import static org.ardulink.rest.RestRouteBuilder.VAR_TARGET;
import static org.ardulink.util.MapBuilder.newMapBuilder;

import java.util.Optional;
import java.util.Properties;

import org.apache.camel.main.Main;
import org.ardulink.rest.RestRouteBuilder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class RestMain implements AutoCloseable {

	private final Main main;

	public static void main(String... args) {
		tryParse(args).ifPresent(RestMain::new);
	}

	static Optional<CommandLineArguments> tryParse(String... args) {
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

	public RestMain(CommandLineArguments args) {
		this(toCamelProperties(args));
	}

	public RestMain(Properties properties) {
		main = new Main();
		main.setInitialProperties(properties);
		main.configure().addRoutesBuilder(new RestRouteBuilder());
		main.start();
	}

	private static Properties toCamelProperties(CommandLineArguments args) {
		return newMapBuilder() //
				.put(VAR_TARGET, args.connection) //
				.put(VAR_BIND, args.bind) //
				.put(VAR_PORT, args.port) //
				.asProperties();
	}

	@Override
	public void close() {
		main.stop();
	}

}
