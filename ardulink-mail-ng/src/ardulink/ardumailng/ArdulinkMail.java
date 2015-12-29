package ardulink.ardumailng;

import static org.zu.ardulink.util.MapBuilder.newMapBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.zu.ardulink.util.Joiner;
import org.zu.ardulink.util.MapBuilder;

public class ArdulinkMail {

	public static abstract class EndpointURIBuilder {
		abstract String makeURI();
	}

	public static class ImapBuilder extends EndpointURIBuilder {

		private String user;
		private String login;
		private String password;
		private String host = "localhost";
		private int port = 143;
		private String folderName = "INBOX";
		private boolean unseen = true;
		private boolean delete;
		private long delay;

		public ImapBuilder user(String user) {
			this.user = user;
			return this;
		}

		public ImapBuilder login(String login) {
			this.login = login;
			return this;
		}

		public ImapBuilder password(String password) {
			this.password = password;
			return this;
		}

		public ImapBuilder host(String host) {
			this.host = host;
			return this;
		}

		public ImapBuilder port(int port) {
			this.port = port;
			return this;
		}

		public ImapBuilder folderName(String folderName) {
			this.folderName = folderName;
			return this;
		}

		public ImapBuilder unseen(boolean unseen) {
			this.unseen = unseen;
			return this;
		}

		public ImapBuilder delete(boolean delete) {
			this.delete = delete;
			return this;
		}

		public ImapBuilder consumerDelay(int delay, TimeUnit timeUnit) {
			this.delay = timeUnit.toMillis(delay);
			return this;
		}

		@Override
		public String makeURI() {
			Map<Object, Object> values = newMapBuilder().put("host", host)
					.put("port", port).put("username", login)
					.put("password", password).put("folderName", folderName)
					.put("unseen", unseen).put("delete", delete)
					.put("consumer.delay", delay).build();
			return "imap://" + user + "?"
					+ Joiner.on("&").withKeyValueSeparator("=").join(values);
		}

	}

	public static class ArdulinkBuilder extends EndpointURIBuilder {

		private String uri;
		private String linkParams;
		private String validFroms;
		private final Map<String, String> scenarios = new HashMap<String, String>();

		public ArdulinkBuilder uri(String uri) {
			this.uri = uri;
			return this;
		}

		public ArdulinkBuilder linkParams(String linkParams) {
			this.linkParams = linkParams;
			return this;
		}

		public ArdulinkBuilder validFroms(String validFroms) {
			this.validFroms = validFroms;
			return this;
		}

		public ArdulinkBuilder addScenario(String name, String content) {
			this.scenarios.put("scenario." + name, content);
			return this;
		}

		@Override
		public String makeURI() {
			MapBuilder<Object, Object> kv = newMapBuilder().put("validfroms",
					validFroms).putAll(scenarios);
			if (linkParams != null) {
				kv = kv.put("linkparams", encode(linkParams));
			}
			return uri
					+ "?"
					+ Joiner.on("&").withKeyValueSeparator("=")
							.join(kv.build());
		}

		private String encode(String string) {
			return "RAW(" + string + ")";
		}

	}

	public static final class Builder {

		private Builder() {
			super();
		}

		public static ImapBuilder imap() {
			return new ImapBuilder();
		}

		public static ArdulinkBuilder ardulink(String uri) {
			return new ArdulinkBuilder().uri(uri);
		}

	}

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
