/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink.mail.server;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public interface ArdulinkMailConstants {

	public static final String TRUE = "true";
	public static final String FALSE = "false";

	public static final String MAIL_CONF_PROPERTIES_FILENAME = "ardulinkmail-conf.properties";
	public static final String MAIL_STORE_PROTOCOL_KEY = "mail.store.protocol";
	public static final String MAIL_HOST_KEY = "host";
	public static final String MAIL_USER_KEY = "user";
	public static final String MAIL_PASSWORD_KEY = "password";
	
	public static final String MAIL_VALIDATE_FROM_KEY= "validateFrom";
	public static final String MAIL_FROM_ADDRESSES_KEY= "fromAddresses";

	public static final String MAIL_VALIDATE_CONTENT_PASSWORD_KEY = "validateContentPassword";
	public static final String MAIL_CONTENT_PASSWORD_KEY = "contentPassword";
	
}
