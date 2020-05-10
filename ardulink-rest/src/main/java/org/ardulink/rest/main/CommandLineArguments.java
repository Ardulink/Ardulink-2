package org.ardulink.rest.main;

import org.kohsuke.args4j.Option;

public class CommandLineArguments {

	@Option(name = "-connection", usage = "Connection URI to the arduino")
	public String connection = "ardulink://serial";

	@Option(name = "-port", usage = "Port for the REST server")
	public int port = 8080;

}
