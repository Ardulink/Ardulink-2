package ardulink.ardumailng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.zu.ardulink.util.Joiner;

public class ArdulinkMail {

	public static class Builder {

		public String from;
		public List<String> tos = new ArrayList<String>();

		public abstract class EndpointBuilder {

			abstract String makeURI();

			public Builder useAsFrom() {
				Builder.this.from = makeURI();
				return Builder.this;
			}

			public Builder addAsTo() {
				Builder.this.tos.add(makeURI());
				return Builder.this;
			}

		}

		public class ImapBuilder extends EndpointBuilder {

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
				return "imap://" + user + "?host=" + host + "&port=" + port
						+ "&username=" + login + "&password=" + password
						+ "&folderName=" + folderName + "&unseen=" + unseen
						+ "&delete=" + delete + "&consumer.delay=" + delay;
			}

		}

		public class ArdulinkBuilder extends EndpointBuilder {

			private String uri;
			private String linkParams;
			private String validFroms;
			private final List<String> scenarios = new ArrayList<String>();

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
				this.scenarios.add("scenario." + name + "=" + content);
				return this;
			}

			@Override
			String makeURI() {
				return uri + "?validfroms=" + validFroms + "&"
						+ Joiner.on("&").join(scenarios) + linkparams();
			}

			private String linkparams() {
				return linkParams == null ? "" : "&linkparams="
						+ encode(linkParams);
			}

			private String encode(String string) {
				return "RAW(" + string + ")";
			}

		}

		public ImapBuilder imap() {
			return new ImapBuilder();
		}

		public ArdulinkBuilder ardulink() {
			return new ArdulinkBuilder();
		}

		public ArdulinkMail build() throws Exception {
			return new ArdulinkMail(this);
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
