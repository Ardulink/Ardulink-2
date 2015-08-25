package com.github.pfichtner.ardulink.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.github.pfichtner.ardulink.MqttMain;

public class MainStarter {

	private final MqttMain client;
	private final List<Exception> exceptions_ = new ArrayList<Exception>();
	private final List<Exception> exceptions = Collections
			.unmodifiableList(exceptions_);

	public MainStarter(MqttMain mqttMain) {
		this.client = mqttMain;
	}

	public MainStarter startAsync() throws InterruptedException {
		new Thread() {
			{
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				try {
					client.doMain();
				} catch (MqttException e) {
					exceptions_.add(e);
				} catch (InterruptedException e) {
					exceptions_.add(e);
				}
			}
		};
		StopWatch stopWatch = new StopWatch().start();
		while (!client.isConnected()) {
			MILLISECONDS.sleep(250);
			int secs = 5;
			if (stopWatch.getTime() > SECONDS.toMillis(secs)) {
				throw new IllegalStateException("Could not connect within "
						+ secs + " seconds");
			}
		}
		return this;
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}

	public void stop() throws MqttException {
		client.close();
	}

}
