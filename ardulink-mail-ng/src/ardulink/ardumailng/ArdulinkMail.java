package ardulink.ardumailng;

import static org.zu.ardulink.util.MapBuilder.newMapBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
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
		String makeURI() {
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
		String makeURI() {
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

	public static class Builder {

		public String from;
		public List<String> tos = new ArrayList<String>();

		public static ImapBuilder imap() {
			return new ImapBuilder();
		}

		public static ArdulinkBuilder ardulink(String uri) {
			return new ArdulinkBuilder().uri(uri);
		}

		public ArdulinkMail build() throws Exception {
			return new ArdulinkMail(this);
		}

		public ArdulinkMail start() throws Exception {
			return build().start();
		}

		public Builder from(EndpointURIBuilder builder) {
			return from(builder.makeURI());
		}

		public Builder from(String uri) {
			Builder.this.from = uri;
			return Builder.this;
		}

		public Builder to(EndpointURIBuilder builder) {
			return to(builder.makeURI());
		}

		public Builder to(String uri) {
			Builder.this.tos.add(uri);
			return Builder.this;
		}

	}

	private final CamelContext context;

	public ArdulinkMail(String from, String... tos) throws Exception {
		context = new DefaultCamelContext();
		context.addRoutes(addRoute(from, tos));
	}

	public ArdulinkMail(Builder builder) throws Exception {
		this(builder.from, builder.tos.toArray(new String[builder.tos.size()]));
	}

	public ArdulinkMail start() throws Exception {
		context.start();
		return this;
	}

	public void stop() throws Exception {
		context.stop();
	}

	private RouteBuilder addRoute(final String from, final String... tos) {
		return new RouteBuilder() {
			@Override
			public void configure() {
				RouteDefinition routeDef = from(from);
//				routeDef = routeDef.pipeline(tos);
				for (String to : tos) {
					routeDef = routeDef.to(to);
				}
			}
		};
	}

	public static Builder builder() {
		return new Builder();
	}

}
