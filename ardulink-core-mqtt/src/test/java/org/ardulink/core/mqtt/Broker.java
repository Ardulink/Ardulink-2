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

package org.ardulink.core.mqtt;

import io.moquette.server.Server;

import java.io.IOException;

import org.junit.rules.ExternalResource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class Broker extends ExternalResource {

	private Server mqttServer;
	
	private Broker() {
		super();
	}
	
	public static Broker newBroker() {
		return new Broker();
	}

	@Override
	protected void before() throws IOException, InterruptedException {
		this.mqttServer = new Server();
		start();
	}

	@Override
	protected void after() {
		stop();
	}

	public void start() throws IOException {
		this.mqttServer.startServer();
	}

	public void stop() {
		this.mqttServer.stopServer();
	}

}
