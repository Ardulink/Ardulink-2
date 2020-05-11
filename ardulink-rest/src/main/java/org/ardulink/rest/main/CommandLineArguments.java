package org.ardulink.rest.main;

import org.kohsuke.args4j.Option;

public class CommandLineArguments {

	@Option(name = "-connection", usage = "Connection URI to the arduino")
	public String connection = "ardulink://serial";

	@Option(name = "-bind", usage = "Name or ip address to bind to")
	public String bind = "localhost";
	
	@Option(name = "-port", usage = "Port to bind to")
	public int port = 8080;

}
