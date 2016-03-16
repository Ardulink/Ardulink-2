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
package org.zu.ardulink.connection.proxy;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class NetworkProxyMessages {

	public static final String NUMBER_OF_PORTS = "NUMBER_OF_PORTS=";
	public static final String OK = "OK";
	public static final String KO = "KO";

	private static final String PREFIX = "ardulink:networkproxyserver:";
	public static final String STOP_SERVER_CMD = PREFIX + "stop_server";
	public static final String GET_PORT_LIST_CMD = PREFIX + "get_port_list";
	public static final String CONNECT_CMD = PREFIX + "connect";

}
