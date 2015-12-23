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

package org.zu.ardulink.mail.server.links.configuration.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zu.ardulink.Link;
import org.zu.ardulink.mail.server.links.configuration.ALink;
import org.zu.ardulink.mail.server.links.configuration.AParameter;
import org.zu.ardulink.mail.server.links.configuration.ConfigurationFacade;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConfigurationUtility {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtility.class);
	
	public static List<Link> getConnectedLinks(List<String> aLinkNames) {
		List<ALink> aLinks = ConfigurationFacade.getALinks(aLinkNames);
		List<Link> links = new LinkedList<Link>();
		for (ALink aLink : aLinks) {
			Link link = aLink.getLink();
			if(!link.isConnected()) {
				try {
					boolean isConnected = connect(aLink, link);
					if(isConnected) {
						links.add(link);
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.info("Connection failed.");
				}
			} else {
				links.add(link);
			}
		}
		return links;
	}

	public static boolean connect(ALink aLink, Link link) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		List<AParameter> connectParamenter = aLink.getConnectParameters();
		Object[] params = new Object[connectParamenter.size()];
		
		Iterator<AParameter> it = connectParamenter.iterator();
		int index = 0;
		while (it.hasNext()) {
			AParameter aParameter = (AParameter) it.next();
			params[index] = aParameter.getValueForClass();
			index++;
		}
		boolean retvalue = link.connect(params);

		// wait for Arduino bootstrap
		try {
			TimeUnit.SECONDS.sleep(aLink.getWaitSecondsAfterConnection());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return retvalue;
	}		

}
