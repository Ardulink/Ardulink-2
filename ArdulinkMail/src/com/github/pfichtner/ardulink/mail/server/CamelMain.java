package com.github.pfichtner.ardulink.mail.server;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelMain {

	public static void main(String[] args) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			public void configure() {
				from(from()).process(processor()).to(to());
			}

			private Processor processor() {
				return new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						// TODO Auto-generated method stub
					}
				};
			}

			private String from() {
				return "imaps://" + "username" + "@" + "host" + "&password="
						+ "secret";
			}

			private String to() {
				return "paho://" + "home/devices/ardulink/";
			}

		});
		context.start();
	}

}
